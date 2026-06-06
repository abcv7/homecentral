package com.homecentral.auth.service.impl;

import com.homecentral.auth.api.dto.ChangePasswordRequest;
import com.homecentral.auth.api.dto.LoginRequest;
import com.homecentral.auth.api.dto.RefreshTokenRequest;
import com.homecentral.auth.api.dto.UpdateProfileRequest;
import com.homecentral.auth.api.vo.LoginResponse;
import com.homecentral.auth.api.vo.MemberVO;
import com.homecentral.auth.entity.Member;
import com.homecentral.auth.mapper.MemberMapper;
import com.homecentral.auth.security.JwtTokenProvider;
import com.homecentral.auth.service.VerificationCodeService;
import com.homecentral.common.model.Result;
import com.homecentral.notification.api.email.EmailChangeCodeEmail;
import com.homecentral.notification.api.email.EmailMessage;
import com.homecentral.notification.api.email.PasswordChangeCodeEmail;
import com.homecentral.notification.api.feign.MailClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.OffsetDateTime;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private MemberMapper memberMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private VerificationCodeService verificationCodeService;

    @Mock
    private MailClient mailClient;

    @InjectMocks
    private AuthServiceImpl authService;

    private Member createMember(Long id, String username, String password, boolean enabled) {
        Member member = new Member();
        member.setId(id);
        member.setUsername(username);
        member.setPassword(password);
        member.setEmail("test@qq.com");
        member.setEnabled(enabled);
        member.setCreatedAt(OffsetDateTime.now());
        return member;
    }

    @Test
    void shouldLoginSuccessWhenCredentialsValid() {
        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword("password123");

        Member member = createMember(1L, "testuser", "encoded-password", true);

        when(memberMapper.selectOne(any())).thenReturn(member);
        when(passwordEncoder.matches("password123", "encoded-password")).thenReturn(true);
        when(jwtTokenProvider.generateAccessToken(1L, "testuser")).thenReturn("access-token");
        when(jwtTokenProvider.generateRefreshToken(1L, "testuser")).thenReturn("refresh-token");
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        LoginResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals(1L, response.getUserId());
        assertEquals("testuser", response.getUsername());
        assertEquals("access-token", response.getAccessToken());
        assertEquals("refresh-token", response.getRefreshToken());

        verify(valueOperations).set(eq("auth:access:1"), eq("access-token"), eq(1L), eq(TimeUnit.HOURS));
        verify(valueOperations).set(eq("auth:refresh:1"), eq("refresh-token"), eq(7L), eq(TimeUnit.DAYS));
    }

    @Test
    void shouldThrowWhenMemberNotFound() {
        LoginRequest request = new LoginRequest();
        request.setUsername("unknown");
        request.setPassword("password123");

        when(memberMapper.selectOne(any())).thenReturn(null);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> authService.login(request));
        assertEquals("用户名或密码错误", ex.getMessage());
    }

    @Test
    void shouldThrowWhenPasswordWrong() {
        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword("wrong-password");

        Member member = createMember(1L, "testuser", "encoded-password", true);

        when(memberMapper.selectOne(any())).thenReturn(member);
        when(passwordEncoder.matches("wrong-password", "encoded-password")).thenReturn(false);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> authService.login(request));
        assertEquals("用户名或密码错误", ex.getMessage());
    }

    @Test
    void shouldThrowWhenAccountDisabled() {
        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword("password123");

        Member member = createMember(1L, "testuser", "encoded-password", false);

        when(memberMapper.selectOne(any())).thenReturn(member);
        when(passwordEncoder.matches("password123", "encoded-password")).thenReturn(true);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> authService.login(request));
        assertEquals("账号已被禁用", ex.getMessage());
    }

    @Test
    void shouldRefreshSuccessWhenTokenValid() {
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("valid-refresh-token");

        when(jwtTokenProvider.validateToken("valid-refresh-token")).thenReturn(true);
        when(jwtTokenProvider.getTokenType("valid-refresh-token")).thenReturn("refresh");
        when(jwtTokenProvider.getUserId("valid-refresh-token")).thenReturn(1L);
        when(jwtTokenProvider.getUsername("valid-refresh-token")).thenReturn("testuser");
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("auth:refresh:1")).thenReturn("valid-refresh-token");
        when(jwtTokenProvider.generateAccessToken(1L, "testuser")).thenReturn("new-access-token");
        when(jwtTokenProvider.generateRefreshToken(1L, "testuser")).thenReturn("new-refresh-token");

        LoginResponse response = authService.refresh(request);

        assertNotNull(response);
        assertEquals("new-access-token", response.getAccessToken());
        assertEquals("new-refresh-token", response.getRefreshToken());

        verify(valueOperations).set(eq("auth:access:1"), eq("new-access-token"), eq(1L), eq(TimeUnit.HOURS));
        verify(valueOperations).set(eq("auth:refresh:1"), eq("new-refresh-token"), eq(7L), eq(TimeUnit.DAYS));
    }

    @Test
    void shouldThrowWhenRefreshTokenInvalid() {
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("invalid-token");

        when(jwtTokenProvider.validateToken("invalid-token")).thenReturn(false);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> authService.refresh(request));
        assertEquals("无效的refreshToken", ex.getMessage());
    }

    @Test
    void shouldThrowWhenTokenTypeNotRefresh() {
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("access-token");

        when(jwtTokenProvider.validateToken("access-token")).thenReturn(true);
        when(jwtTokenProvider.getTokenType("access-token")).thenReturn("access");

        RuntimeException ex = assertThrows(RuntimeException.class, () -> authService.refresh(request));
        assertEquals("token类型错误", ex.getMessage());
    }

    @Test
    void shouldThrowWhenStoredTokenMismatch() {
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("valid-refresh-token");

        when(jwtTokenProvider.validateToken("valid-refresh-token")).thenReturn(true);
        when(jwtTokenProvider.getTokenType("valid-refresh-token")).thenReturn("refresh");
        when(jwtTokenProvider.getUserId("valid-refresh-token")).thenReturn(1L);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("auth:refresh:1")).thenReturn("different-token");

        RuntimeException ex = assertThrows(RuntimeException.class, () -> authService.refresh(request));
        assertEquals("refreshToken已过期或无效", ex.getMessage());
    }

    @Test
    void shouldReturnProfileWhenGetCurrentProfile() {
        Member member = createMember(1L, "testuser", "pwd", true);
        member.setNickname("Test");
        member.setPhone("13800000000");
        when(memberMapper.selectById(1L)).thenReturn(member);

        MemberVO vo = authService.getCurrentProfile(1L);

        assertNotNull(vo);
        assertEquals(1L, vo.getId());
        assertEquals("testuser", vo.getUsername());
        assertEquals("Test", vo.getNickname());
        assertEquals("13800000000", vo.getPhone());
        assertEquals("test@qq.com", vo.getEmail());
    }

    @Test
    void shouldThrowWhenGetCurrentProfileNotFound() {
        when(memberMapper.selectById(99L)).thenReturn(null);
        RuntimeException ex = assertThrows(RuntimeException.class, () -> authService.getCurrentProfile(99L));
        assertEquals("用户不存在", ex.getMessage());
    }

    @Test
    void shouldUpdateProfileWhenFieldsProvided() {
        UpdateProfileRequest req = new UpdateProfileRequest();
        req.setNickname("NewName");
        req.setPhone("13900000000");

        Member existing = createMember(1L, "u", "p", true);
        when(memberMapper.selectById(1L)).thenReturn(existing);

        Member updated = createMember(1L, "u", "p", true);
        updated.setNickname("NewName");
        updated.setPhone("13900000000");
        when(memberMapper.selectById(1L)).thenReturn(existing, updated);

        MemberVO vo = authService.updateProfile(1L, req);

        assertEquals("NewName", vo.getNickname());
        assertEquals("13900000000", vo.getPhone());
        verify(memberMapper).updateById(any(Member.class));
    }

    @Test
    void shouldThrowWhenUpdateProfileEmpty() {
        UpdateProfileRequest req = new UpdateProfileRequest();
        RuntimeException ex = assertThrows(RuntimeException.class, () -> authService.updateProfile(1L, req));
        assertEquals("至少需要修改一个字段", ex.getMessage());
    }

    @Test
    void shouldSendEmailChangeCode() {
        Member member = createMember(1L, "u", "p", true);
        member.setEmail("old@qq.com");
        when(memberMapper.selectById(1L)).thenReturn(member);
        when(memberMapper.selectCount(any())).thenReturn(0L);
        when(verificationCodeService.generateAndStore("EMAIL_CHANGE", "new@qq.com")).thenReturn("123456");
        when(mailClient.sendMessage(any(EmailMessage.class))).thenReturn(Result.ok(null));

        authService.sendEmailChangeCode(1L, "new@qq.com");

        verify(mailClient).sendMessage(argThat(m ->
            m instanceof EmailChangeCodeEmail
                && "new@qq.com".equals(m.getTo())
                && "123456".equals(((EmailChangeCodeEmail) m).getCode())
                && m.getDefaultSubject().contains("更换绑定邮箱")
        ));
    }

    @Test
    void shouldThrowWhenEmailChangeNewEmailSame() {
        Member member = createMember(1L, "u", "p", true);
        member.setEmail("same@qq.com");
        when(memberMapper.selectById(1L)).thenReturn(member);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> authService.sendEmailChangeCode(1L, "same@qq.com"));
        assertEquals("新邮箱与当前邮箱相同", ex.getMessage());
    }

    @Test
    void shouldThrowWhenEmailChangeAlreadyUsed() {
        Member member = createMember(1L, "u", "p", true);
        member.setEmail("old@qq.com");
        when(memberMapper.selectById(1L)).thenReturn(member);
        when(memberMapper.selectCount(any())).thenReturn(1L);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> authService.sendEmailChangeCode(1L, "occupied@qq.com"));
        assertEquals("该邮箱已被其他账号使用", ex.getMessage());
    }

    @Test
    void shouldVerifyEmailChangeWhenCodeCorrect() {
        Member member = createMember(1L, "u", "p", true);
        member.setEmail("old@qq.com");
        when(verificationCodeService.verify("EMAIL_CHANGE", "new@qq.com", "654321")).thenReturn(true);
        when(memberMapper.selectById(1L)).thenReturn(member);

        Member updated = createMember(1L, "u", "p", true);
        updated.setEmail("new@qq.com");
        when(memberMapper.selectById(1L)).thenReturn(member, updated);

        MemberVO vo = authService.verifyEmailChange(1L, "new@qq.com", "654321");

        assertEquals("new@qq.com", vo.getEmail());
        verify(memberMapper).updateById(any(Member.class));
    }

    @Test
    void shouldThrowWhenVerifyEmailCodeWrong() {
        when(verificationCodeService.verify("EMAIL_CHANGE", "new@qq.com", "000000")).thenReturn(false);
        RuntimeException ex = assertThrows(RuntimeException.class, () -> authService.verifyEmailChange(1L, "new@qq.com", "000000"));
        assertEquals("验证码错误或已过期", ex.getMessage());
    }

    @Test
    void shouldSendPasswordChangeCode() {
        Member member = createMember(1L, "u", "p", true);
        member.setEmail("user@qq.com");
        when(memberMapper.selectById(1L)).thenReturn(member);
        when(verificationCodeService.generateAndStore("PASSWORD_CHANGE", "user@qq.com")).thenReturn("111111");
        when(mailClient.sendMessage(any(EmailMessage.class))).thenReturn(Result.ok(null));

        authService.sendPasswordChangeCode(1L);

        verify(mailClient).sendMessage(argThat(m ->
            m instanceof PasswordChangeCodeEmail
                && "user@qq.com".equals(m.getTo())
                && "111111".equals(((PasswordChangeCodeEmail) m).getCode())
                && m.getDefaultSubject().contains("修改登录密码")
        ));
    }

    @Test
    void shouldThrowWhenSendPasswordCodeButEmailEmpty() {
        Member member = createMember(1L, "u", "p", true);
        member.setEmail(null);
        when(memberMapper.selectById(1L)).thenReturn(member);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> authService.sendPasswordChangeCode(1L));
        assertEquals("当前账号未设置邮箱，请先在资料页设置邮箱", ex.getMessage());
    }

    @Test
    void shouldVerifyPasswordChangeWhenCodeCorrect() {
        Member member = createMember(1L, "u", "p", true);
        member.setEmail("user@qq.com");
        when(memberMapper.selectById(1L)).thenReturn(member);
        when(verificationCodeService.verify("PASSWORD_CHANGE", "user@qq.com", "111111")).thenReturn(true);
        when(passwordEncoder.encode("newpass123")).thenReturn("new-encoded");

        ChangePasswordRequest req = new ChangePasswordRequest();
        req.setCode("111111");
        req.setNewPassword("newpass123");

        authService.verifyPasswordChange(1L, req);

        verify(memberMapper).updateById(any(Member.class));
    }

    @Test
    void shouldThrowWhenVerifyPasswordCodeWrong() {
        Member member = createMember(1L, "u", "p", true);
        member.setEmail("user@qq.com");
        when(memberMapper.selectById(1L)).thenReturn(member);
        when(verificationCodeService.verify("PASSWORD_CHANGE", "user@qq.com", "000000")).thenReturn(false);

        ChangePasswordRequest req = new ChangePasswordRequest();
        req.setCode("000000");
        req.setNewPassword("newpass123");

        RuntimeException ex = assertThrows(RuntimeException.class, () -> authService.verifyPasswordChange(1L, req));
        assertEquals("验证码错误或已过期", ex.getMessage());
    }
}
