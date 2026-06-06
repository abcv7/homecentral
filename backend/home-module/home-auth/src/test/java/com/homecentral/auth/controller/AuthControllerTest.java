package com.homecentral.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.homecentral.auth.api.dto.ChangePasswordRequest;
import com.homecentral.auth.api.dto.LoginRequest;
import com.homecentral.auth.api.dto.RefreshTokenRequest;
import com.homecentral.auth.api.dto.SendCodeRequest;
import com.homecentral.auth.api.dto.UpdateProfileRequest;
import com.homecentral.auth.api.dto.VerifyEmailCodeRequest;
import com.homecentral.auth.api.vo.LoginResponse;
import com.homecentral.auth.api.vo.MemberVO;
import com.homecentral.auth.security.JwtAuthenticationFilter;
import com.homecentral.auth.security.JwtTokenProvider;
import com.homecentral.auth.service.IAuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = AuthController.class, properties = {
    "spring.cloud.nacos.config.enabled=false",
    "spring.cloud.nacos.discovery.enabled=false",
    "spring.cloud.config.import-check.enabled=false"
})
@Import(JwtAuthenticationFilter.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private IAuthService authService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    private MemberVO sampleProfile() {
        MemberVO vo = new MemberVO();
        vo.setId(1L);
        vo.setUsername("admin");
        vo.setNickname("管理员");
        vo.setPhone("13800000000");
        vo.setEmail("abcv7@qq.com");
        vo.setRole("ADMIN");
        return vo;
    }

    @Test
    void shouldLoginSuccess() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword("password123");

        LoginResponse response = new LoginResponse();
        response.setUserId(1L);
        response.setUsername("testuser");
        response.setAccessToken("access-token");
        response.setRefreshToken("refresh-token");

        when(authService.login(any())).thenReturn(response);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").value("access-token"));
    }

    @Test
    void shouldReturn400WhenLoginRequestInvalid() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsername("");
        request.setPassword("");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRefreshSuccess() throws Exception {
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("valid-refresh-token");

        LoginResponse response = new LoginResponse();
        response.setUserId(1L);
        response.setUsername("testuser");
        response.setAccessToken("new-access-token");
        response.setRefreshToken("new-refresh-token");

        when(authService.refresh(any())).thenReturn(response);

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").value("new-access-token"));
    }

    @Test
    void shouldReturn400WhenRefreshTokenBlank() throws Exception {
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("");

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldGetMeSuccess() throws Exception {
        when(authService.getCurrentProfile(1L)).thenReturn(sampleProfile());

        mockMvc.perform(get("/api/auth/me").header("X-User-Id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("abcv7@qq.com"))
                .andExpect(jsonPath("$.data.username").value("admin"));
    }

    @Test
    void shouldUpdateProfileSuccess() throws Exception {
        UpdateProfileRequest req = new UpdateProfileRequest();
        req.setNickname("新昵称");
        req.setPhone("13900000000");

        MemberVO vo = sampleProfile();
        vo.setNickname("新昵称");
        vo.setPhone("13900000000");
        when(authService.updateProfile(eq(1L), any())).thenReturn(vo);

        mockMvc.perform(put("/api/auth/me/profile")
                        .header("X-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.nickname").value("新昵称"))
                .andExpect(jsonPath("$.data.phone").value("13900000000"));
    }

    @Test
    void shouldSendEmailCodeSuccess() throws Exception {
        SendCodeRequest req = new SendCodeRequest();
        req.setPurpose("EMAIL_CHANGE");
        req.setEmail("new@qq.com");
        doNothing().when(authService).sendEmailChangeCode(anyLong(), any());

        mockMvc.perform(post("/api/auth/me/email/code")
                        .header("X-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void shouldReturn400WhenEmailCodePurposeInvalid() throws Exception {
        SendCodeRequest req = new SendCodeRequest();
        req.setPurpose("INVALID");
        req.setEmail("new@qq.com");

        mockMvc.perform(post("/api/auth/me/email/code")
                        .header("X-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldVerifyEmailChangeSuccess() throws Exception {
        VerifyEmailCodeRequest req = new VerifyEmailCodeRequest();
        req.setPurpose("EMAIL_CHANGE");
        req.setEmail("new@qq.com");
        req.setCode("123456");

        MemberVO vo = sampleProfile();
        vo.setEmail("new@qq.com");
        when(authService.verifyEmailChange(eq(1L), eq("new@qq.com"), eq("123456"))).thenReturn(vo);

        mockMvc.perform(put("/api/auth/me/email")
                        .header("X-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value("new@qq.com"));
    }

    @Test
    void shouldSendPasswordCodeSuccess() throws Exception {
        doNothing().when(authService).sendPasswordChangeCode(1L);

        mockMvc.perform(post("/api/auth/me/password/code").header("X-User-Id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void shouldVerifyPasswordChangeSuccess() throws Exception {
        ChangePasswordRequest req = new ChangePasswordRequest();
        req.setCode("111111");
        req.setNewPassword("newpass123");
        doNothing().when(authService).verifyPasswordChange(eq(1L), any());

        mockMvc.perform(put("/api/auth/me/password")
                        .header("X-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void shouldReturn400WhenPasswordChangeInvalid() throws Exception {
        ChangePasswordRequest req = new ChangePasswordRequest();
        req.setCode("11");
        req.setNewPassword("123");

        mockMvc.perform(put("/api/auth/me/password")
                        .header("X-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }
}
