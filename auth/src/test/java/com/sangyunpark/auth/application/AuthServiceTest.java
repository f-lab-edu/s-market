package com.sangyunpark.auth.application;

import com.sangyunpark.auth.client.UserClient;
import com.sangyunpark.auth.client.dto.response.FeignUserSelectResponseDto;
import com.sangyunpark.auth.constants.enums.RegisterType;
import com.sangyunpark.auth.constants.enums.UserStatus;
import com.sangyunpark.auth.constants.enums.UserType;
import com.sangyunpark.auth.exception.BusinessException;
import com.sangyunpark.auth.infrastructure.repository.RedisTokenRepository;
import com.sangyunpark.auth.jwt.TokenProvider;
import com.sangyunpark.auth.presentation.dto.request.LoginRequestDto;
import com.sangyunpark.auth.presentation.dto.response.LoginResponseDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserClient userClient;

    @Mock
    private TokenProvider tokenProvider;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RedisTokenRepository redisTokenRepository;

    @InjectMocks
    private AuthService authService;

    private LoginRequestDto loginRequest;
    private FeignUserSelectResponseDto feignUser;

    @BeforeEach
    void setUp() {
        loginRequest = new LoginRequestDto("test@example.com", "password123");
        feignUser = new FeignUserSelectResponseDto(
                1L,
                "test@example.com",
                "encodedPassword",
                "상윤",
                UserType.NORMAL,
                UserStatus.ACTIVE,
                RegisterType.EMAIL,
                "010-1234-5678"
        );
    }

    @Test
    @DisplayName("로그인 성공 시 토큰을 생성하여 반환한다.")
    void 로그인_성공() {
        // given
        when(userClient.findUserByEmail(loginRequest.email())).thenReturn(feignUser);
        when(passwordEncoder.matches(loginRequest.password(), feignUser.password())).thenReturn(true);
        when(tokenProvider.createAccessToken(feignUser.email(), feignUser.userType())).thenReturn("access-token");
        when(tokenProvider.createRefreshToken(feignUser.email(), feignUser.userType())).thenReturn("refresh-token");

        // when
        LoginResponseDto response = authService.login(loginRequest);

        // then
        assertThat(response.token().accessToken()).isEqualTo("access-token");
        assertThat(response.token().refreshToken()).isEqualTo("refresh-token");
        verify(redisTokenRepository).save(feignUser.email(), "refresh-token");
    }

    @Test
    @DisplayName("비밀번호가 일치하지 않으면 예외를 던진다.")
    void 비밀번호_불일치_로그인_실패() {
        // given
        when(userClient.findUserByEmail(loginRequest.email())).thenReturn(feignUser);
        when(passwordEncoder.matches(loginRequest.password(), feignUser.password())).thenReturn(false);

        // when & then
        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(BusinessException.class);
    }
}