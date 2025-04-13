package com.sangyunpark.auth.presentation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sangyunpark.auth.application.AuthService;
import com.sangyunpark.auth.constants.code.ErrorCode;
import com.sangyunpark.auth.domain.vo.Token;
import com.sangyunpark.auth.exception.BusinessException;
import com.sangyunpark.auth.presentation.dto.request.LoginRequestDto;
import com.sangyunpark.auth.presentation.dto.response.TokenResponseDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("로그인 성공 시 토큰 정보를 응답한다.")
    void loginSuccess() throws Exception {
        // given
        LoginRequestDto request = new LoginRequestDto("test@example.com", "password123");
        TokenResponseDto response = new TokenResponseDto(new Token("access-token", "refresh-token"));

        when(authService.login(any(LoginRequestDto.class))).thenReturn(response);

        // when & then
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token.accessToken").value("access-token"))
                .andExpect(jsonPath("$.token.refreshToken").value("refresh-token"));
    }

    @Test
    @DisplayName("로그인 요청 시 이메일이 비어 있으면 400 Bad Request를 반환한다.")
    void loginFail_invalidEmail() throws Exception {
        LoginRequestDto request = new LoginRequestDto("", "password123");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("로그인 요청 시 비밀번호가 비어 있으면 400 Bad Request를 반환한다.")
    void loginFail_invalidPassword() throws Exception {
        LoginRequestDto request = new LoginRequestDto("test@example.com", "");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("잘못된 자격 증명으로 로그인하면 401 Unauthorized를 반환한다.")
    void loginFail_invalidCredentials() throws Exception {
        LoginRequestDto request = new LoginRequestDto("wrong@example.com", "wrongPassword");

        when(authService.login(any(LoginRequestDto.class)))
                .thenThrow(new BusinessException(ErrorCode.INVALID_CREDENTIALS));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("로그아웃 요청 시 토큰이 블랙리스트에 추가된다.")
    void logoutTest() throws Exception {
        // given
        String accessToken = "valid-access-token";

        // when & then
        mockMvc.perform(post("/api/v1/auth/logout")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(authService).logout(accessToken);
    }
}