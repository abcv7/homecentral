package com.homecentral.auth.service;

import com.homecentral.auth.api.dto.ChangePasswordRequest;
import com.homecentral.auth.api.dto.LoginRequest;
import com.homecentral.auth.api.dto.RefreshTokenRequest;
import com.homecentral.auth.api.dto.RegisterRequest;
import com.homecentral.auth.api.dto.UpdateProfileRequest;
import com.homecentral.auth.api.vo.LoginResponse;
import com.homecentral.auth.api.vo.MemberVO;

public interface IAuthService {

    LoginResponse login(LoginRequest request);

    LoginResponse refresh(RefreshTokenRequest request);

    LoginResponse register(RegisterRequest request);

    MemberVO getMemberById(Long id);

    String getMemberEmail(Long id);

    MemberVO getCurrentProfile(Long userId);

    MemberVO updateProfile(Long userId, UpdateProfileRequest request);

    void sendEmailChangeCode(Long userId, String newEmail);

    MemberVO verifyEmailChange(Long userId, String newEmail, String code);

    void sendPasswordChangeCode(Long userId);

    void verifyPasswordChange(Long userId, ChangePasswordRequest request);
}
