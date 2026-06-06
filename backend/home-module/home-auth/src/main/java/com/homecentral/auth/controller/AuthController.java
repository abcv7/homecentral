package com.homecentral.auth.controller;

import com.homecentral.auth.api.dto.ChangePasswordRequest;
import com.homecentral.auth.api.dto.LoginRequest;
import com.homecentral.auth.api.dto.RefreshTokenRequest;
import com.homecentral.auth.api.dto.RegisterRequest;
import com.homecentral.auth.api.dto.SendCodeRequest;
import com.homecentral.auth.api.dto.UpdateProfileRequest;
import com.homecentral.auth.api.dto.VerifyEmailCodeRequest;
import com.homecentral.auth.api.vo.LoginResponse;
import com.homecentral.auth.api.vo.MemberVO;
import com.homecentral.auth.service.IAuthService;
import com.homecentral.common.model.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@Tag(name = "认证管理", description = "用户注册、登录、Token刷新、个人资料")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final IAuthService authService;

    public AuthController(IAuthService authService) {
        this.authService = authService;
    }

    @Operation(summary = "用户注册", description = "注册新用户账号，返回登录凭证")
    @PostMapping("/register")
    public Result<LoginResponse> register(@Valid @RequestBody RegisterRequest request) {
        return Result.ok(authService.register(request));
    }

    @Operation(summary = "用户登录", description = "使用用户名密码登录，返回访问令牌和刷新令牌")
    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return Result.ok(authService.login(request));
    }

    @Operation(summary = "刷新Token", description = "使用刷新令牌获取新的访问令牌")
    @PostMapping("/refresh")
    public Result<LoginResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return Result.ok(authService.refresh(request));
    }

    @Operation(summary = "获取用户信息", description = "根据用户ID获取用户信息（内部调用）")
    @GetMapping("/member/{id}")
    public Result<MemberVO> getMemberById(@PathVariable Long id) {
        return Result.ok(authService.getMemberById(id));
    }

    @Operation(summary = "查询用户邮箱（内部调用）", description = "用于跨服务发邮件时获取 user 对应 email")
    @GetMapping("/member/{id}/email")
    public Result<String> getMemberEmail(@PathVariable Long id) {
        return Result.ok(authService.getMemberEmail(id));
    }

    @Operation(summary = "获取当前登录用户资料", description = "从 X-User-Id 头解析当前用户")
    @GetMapping("/me")
    public Result<MemberVO> me(
        @Parameter(description = "当前用户ID，由网关注入") @RequestHeader("X-User-Id") Long userId
    ) {
        return Result.ok(authService.getCurrentProfile(userId));
    }

    @Operation(summary = "更新昵称/手机号", description = "直接 UPDATE，不需验证码")
    @PutMapping("/me/profile")
    public Result<MemberVO> updateProfile(
        @RequestHeader("X-User-Id") Long userId,
        @Valid @RequestBody UpdateProfileRequest request
    ) {
        return Result.ok(authService.updateProfile(userId, request));
    }

    @Operation(summary = "发送邮箱变更验证码", description = "向 newEmail 发 6 位码，5min TTL")
    @PostMapping("/me/email/code")
    public Result<Void> sendEmailCode(
        @RequestHeader("X-User-Id") Long userId,
        @Valid @RequestBody SendCodeRequest request
    ) {
        authService.sendEmailChangeCode(userId, request.getEmail());
        return Result.ok(null);
    }

    @Operation(summary = "确认邮箱变更", description = "校验 6 位码 + 写入新邮箱")
    @PutMapping("/me/email")
    public Result<MemberVO> verifyEmailChange(
        @RequestHeader("X-User-Id") Long userId,
        @Valid @RequestBody VerifyEmailCodeRequest request
    ) {
        return Result.ok(authService.verifyEmailChange(userId, request.getEmail(), request.getCode()));
    }

    @Operation(summary = "发送密码变更验证码", description = "向当前邮箱发 6 位码，5min TTL")
    @PostMapping("/me/password/code")
    public Result<Void> sendPasswordCode(@RequestHeader("X-User-Id") Long userId) {
        authService.sendPasswordChangeCode(userId);
        return Result.ok(null);
    }

    @Operation(summary = "确认密码变更", description = "校验 6 位码 + 写入新密码（BCrypt）")
    @PutMapping("/me/password")
    public Result<Void> verifyPasswordChange(
        @RequestHeader("X-User-Id") Long userId,
        @Valid @RequestBody ChangePasswordRequest request
    ) {
        authService.verifyPasswordChange(userId, request);
        return Result.ok(null);
    }
}
