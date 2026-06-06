package com.homecentral.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.homecentral.auth.api.dto.ChangePasswordRequest;
import com.homecentral.auth.api.dto.LoginRequest;
import com.homecentral.auth.api.dto.RefreshTokenRequest;
import com.homecentral.auth.api.dto.RegisterRequest;
import com.homecentral.auth.api.dto.UpdateProfileRequest;
import com.homecentral.auth.api.vo.LoginResponse;
import com.homecentral.auth.api.vo.MemberVO;
import com.homecentral.auth.entity.Member;
import com.homecentral.auth.mapper.MemberMapper;
import com.homecentral.auth.security.JwtTokenProvider;
import com.homecentral.auth.service.IAuthService;
import com.homecentral.auth.service.VerificationCodeService;
import com.homecentral.notification.api.email.EmailChangeCodeEmail;
import com.homecentral.notification.api.email.EmailMessage;
import com.homecentral.notification.api.email.PasswordChangeCodeEmail;
import com.homecentral.notification.api.feign.MailClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.concurrent.TimeUnit;

@Service
public class AuthServiceImpl implements IAuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);

    private final MemberMapper memberMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final StringRedisTemplate redisTemplate;
    private final VerificationCodeService verificationCodeService;
    private final MailClient mailClient;

    public AuthServiceImpl(MemberMapper memberMapper, PasswordEncoder passwordEncoder,
                           JwtTokenProvider jwtTokenProvider, StringRedisTemplate redisTemplate,
                           VerificationCodeService verificationCodeService, MailClient mailClient) {
        this.memberMapper = memberMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.redisTemplate = redisTemplate;
        this.verificationCodeService = verificationCodeService;
        this.mailClient = mailClient;
    }

    private static final String REDIS_ACCESS_KEY_PREFIX = "auth:access:";
    private static final String REDIS_REFRESH_KEY_PREFIX = "auth:refresh:";
    private static final long ACCESS_TOKEN_EXPIRE_HOURS = 1;
    private static final long REFRESH_TOKEN_EXPIRE_DAYS = 7;

    private static final String PURPOSE_EMAIL_CHANGE = "EMAIL_CHANGE";
    private static final String PURPOSE_PASSWORD_CHANGE = "PASSWORD_CHANGE";

    @Override
    public LoginResponse register(RegisterRequest request) {
        Member existing = memberMapper.selectOne(
            new LambdaQueryWrapper<Member>().eq(Member::getUsername, request.getUsername())
        );
        if (existing != null) {
            throw new RuntimeException("用户名已存在");
        }

        Member member = new Member();
        member.setUsername(request.getUsername());
        member.setPassword(passwordEncoder.encode(request.getPassword()));
        member.setNickname(request.getNickname() != null ? request.getNickname() : request.getUsername());
        member.setPhone(request.getPhone());
        member.setRole("USER");
        member.setEnabled(true);
        member.setCreatedAt(OffsetDateTime.now());
        member.setUpdatedAt(OffsetDateTime.now());
        memberMapper.insert(member);

        String accessToken = jwtTokenProvider.generateAccessToken(member.getId(), member.getUsername());
        String refreshToken = jwtTokenProvider.generateRefreshToken(member.getId(), member.getUsername());

        redisTemplate.opsForValue().set(REDIS_ACCESS_KEY_PREFIX + member.getId(), accessToken, ACCESS_TOKEN_EXPIRE_HOURS, TimeUnit.HOURS);
        redisTemplate.opsForValue().set(REDIS_REFRESH_KEY_PREFIX + member.getId(), refreshToken, REFRESH_TOKEN_EXPIRE_DAYS, TimeUnit.DAYS);

        LoginResponse response = new LoginResponse();
        response.setUserId(member.getId());
        response.setUsername(member.getUsername());
        response.setAccessToken(accessToken);
        response.setRefreshToken(refreshToken);
        return response;
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        Member member = memberMapper.selectOne(
            new LambdaQueryWrapper<Member>().eq(Member::getUsername, request.getUsername())
        );

        if (member == null || !passwordEncoder.matches(request.getPassword(), member.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }

        if (!member.getEnabled()) {
            throw new RuntimeException("账号已被禁用");
        }

        String accessToken = jwtTokenProvider.generateAccessToken(member.getId(), member.getUsername());
        String refreshToken = jwtTokenProvider.generateRefreshToken(member.getId(), member.getUsername());

        redisTemplate.opsForValue().set(REDIS_ACCESS_KEY_PREFIX + member.getId(), accessToken, ACCESS_TOKEN_EXPIRE_HOURS, TimeUnit.HOURS);
        redisTemplate.opsForValue().set(REDIS_REFRESH_KEY_PREFIX + member.getId(), refreshToken, REFRESH_TOKEN_EXPIRE_DAYS, TimeUnit.DAYS);

        LoginResponse response = new LoginResponse();
        response.setUserId(member.getId());
        response.setUsername(member.getUsername());
        response.setAccessToken(accessToken);
        response.setRefreshToken(refreshToken);
        return response;
    }

    @Override
    public LoginResponse refresh(RefreshTokenRequest request) {
        if (!jwtTokenProvider.validateToken(request.getRefreshToken())) {
            throw new RuntimeException("无效的refreshToken");
        }

        if (!"refresh".equals(jwtTokenProvider.getTokenType(request.getRefreshToken()))) {
            throw new RuntimeException("token类型错误");
        }

        Long userId = jwtTokenProvider.getUserId(request.getRefreshToken());
        String username = jwtTokenProvider.getUsername(request.getRefreshToken());

        String storedRefreshToken = redisTemplate.opsForValue().get(REDIS_REFRESH_KEY_PREFIX + userId);
        if (storedRefreshToken == null || !storedRefreshToken.equals(request.getRefreshToken())) {
            throw new RuntimeException("refreshToken已过期或无效");
        }

        String newAccessToken = jwtTokenProvider.generateAccessToken(userId, username);
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(userId, username);

        redisTemplate.opsForValue().set(REDIS_ACCESS_KEY_PREFIX + userId, newAccessToken, ACCESS_TOKEN_EXPIRE_HOURS, TimeUnit.HOURS);
        redisTemplate.opsForValue().set(REDIS_REFRESH_KEY_PREFIX + userId, newRefreshToken, REFRESH_TOKEN_EXPIRE_DAYS, TimeUnit.DAYS);

        LoginResponse response = new LoginResponse();
        response.setUserId(userId);
        response.setUsername(username);
        response.setAccessToken(newAccessToken);
        response.setRefreshToken(newRefreshToken);
        return response;
    }

    @Override
    public MemberVO getMemberById(Long id) {
        Member member = memberMapper.selectById(id);
        return member == null ? null : toVO(member);
    }

    @Override
    public String getMemberEmail(Long id) {
        Member member = memberMapper.selectById(id);
        return member == null ? null : member.getEmail();
    }

    @Override
    public MemberVO getCurrentProfile(Long userId) {
        Member member = memberMapper.selectById(userId);
        if (member == null) {
            throw new RuntimeException("用户不存在");
        }
        return toVO(member);
    }

    @Override
    public MemberVO updateProfile(Long userId, UpdateProfileRequest request) {
        if (request.getNickname() == null && request.getPhone() == null) {
            throw new RuntimeException("至少需要修改一个字段");
        }
        Member member = memberMapper.selectById(userId);
        if (member == null) {
            throw new RuntimeException("用户不存在");
        }
        if (request.getNickname() != null) member.setNickname(request.getNickname());
        if (request.getPhone() != null) member.setPhone(request.getPhone());
        member.setUpdatedAt(OffsetDateTime.now());
        memberMapper.updateById(member);
        return getCurrentProfile(userId);
    }

    @Override
    public void sendEmailChangeCode(Long userId, String newEmail) {
        if (newEmail == null || newEmail.isBlank()) {
            throw new RuntimeException("新邮箱不能为空");
        }
        Member member = memberMapper.selectById(userId);
        if (member == null) {
            throw new RuntimeException("用户不存在");
        }
        if (newEmail.equalsIgnoreCase(member.getEmail())) {
            throw new RuntimeException("新邮箱与当前邮箱相同");
        }
        Long otherCount = memberMapper.selectCount(
            new LambdaQueryWrapper<Member>().eq(Member::getEmail, newEmail).ne(Member::getId, userId)
        );
        if (otherCount != null && otherCount > 0) {
            throw new RuntimeException("该邮箱已被其他账号使用");
        }

        String code = verificationCodeService.generateAndStore(PURPOSE_EMAIL_CHANGE, newEmail);
        sendCodeMail(newEmail, code, "邮箱变更");
    }

    @Override
    public MemberVO verifyEmailChange(Long userId, String newEmail, String code) {
        if (!verificationCodeService.verify(PURPOSE_EMAIL_CHANGE, newEmail, code)) {
            throw new RuntimeException("验证码错误或已过期");
        }
        Member member = memberMapper.selectById(userId);
        if (member == null) {
            throw new RuntimeException("用户不存在");
        }
        member.setEmail(newEmail);
        member.setUpdatedAt(OffsetDateTime.now());
        memberMapper.updateById(member);
        return getCurrentProfile(userId);
    }

    @Override
    public void sendPasswordChangeCode(Long userId) {
        Member member = memberMapper.selectById(userId);
        if (member == null) {
            throw new RuntimeException("用户不存在");
        }
        String email = member.getEmail();
        if (email == null || email.isBlank()) {
            throw new RuntimeException("当前账号未设置邮箱，请先在资料页设置邮箱");
        }
        String code = verificationCodeService.generateAndStore(PURPOSE_PASSWORD_CHANGE, email);
        sendCodeMail(email, code, "密码变更");
    }

    @Override
    public void verifyPasswordChange(Long userId, ChangePasswordRequest request) {
        Member member = memberMapper.selectById(userId);
        if (member == null) {
            throw new RuntimeException("用户不存在");
        }
        String email = member.getEmail();
        if (email == null || email.isBlank()) {
            throw new RuntimeException("当前账号未设置邮箱");
        }
        if (!verificationCodeService.verify(PURPOSE_PASSWORD_CHANGE, email, request.getCode())) {
            throw new RuntimeException("验证码错误或已过期");
        }
        member.setPassword(passwordEncoder.encode(request.getNewPassword()));
        member.setUpdatedAt(OffsetDateTime.now());
        memberMapper.updateById(member);
    }

    private void sendCodeMail(String to, String code, String purpose) {
        EmailMessage email;
        if ("邮箱变更".equals(purpose)) {
            email = new EmailChangeCodeEmail(to, code, 5, to);
        } else if ("密码变更".equals(purpose)) {
            email = new PasswordChangeCodeEmail(to, code, 5, to);
        } else {
            throw new IllegalArgumentException("未支持的验证码用途: " + purpose);
        }
        try {
            mailClient.sendMessage(email);
        } catch (Exception e) {
            log.error("[auth-code-mail-fail] to={} purpose={}", to, purpose, e);
            throw new RuntimeException("邮件发送失败：" + e.getMessage());
        }
    }

    private MemberVO toVO(Member member) {
        MemberVO vo = new MemberVO();
        vo.setId(member.getId());
        vo.setUsername(member.getUsername());
        vo.setNickname(member.getNickname());
        vo.setPhone(member.getPhone());
        vo.setEmail(member.getEmail());
        vo.setRole(member.getRole());
        return vo;
    }
}
