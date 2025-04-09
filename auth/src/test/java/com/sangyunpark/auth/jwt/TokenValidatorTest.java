package com.sangyunpark.auth.jwt;

import com.sangyunpark.auth.infrastructure.repository.RedisTokenRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TokenValidatorTest {

    @Mock
    private TokenProvider tokenProvider;

    @Mock
    private RedisTokenRepository redisTokenRepository;

    @InjectMocks
    private TokenValidator tokenValidator;

    private static final String TOKEN = "token";

    @Test
    @DisplayName("토큰이 null 이거나 빈 문자열일 경우 false 반환")
    void validateToken_whenTokenIsNullOrBlank_returnsFalse() {
        // when & then
        assertFalse(tokenValidator.validateAccessToken(null));
        assertFalse(tokenValidator.validateAccessToken(""));
    }

    @Test
    @DisplayName("유효하지 않은 토큰일 경우 false 반환")
    void validateToken_whenTokenIsInvalid_returnsFalse() {
        // given
        when(tokenProvider.validateToken(TOKEN)).thenReturn(false);

        // when & then
        assertFalse(tokenValidator.validateAccessToken(TOKEN));
    }

    @Test
    @DisplayName("블랙리스트에 있는 토큰일 경우 false 반환")
    void validateToken_whenTokenIsInBlackList_returnsFalse() {
        // given
        when(tokenProvider.validateToken(TOKEN)).thenReturn(true);
        when(redisTokenRepository.isLogOutToken(TOKEN)).thenReturn(true);

        // when & then
        assertFalse(tokenValidator.validateAccessToken(TOKEN));
    }

    @Test
    @DisplayName("정상적인 토큰일 경우 true 반환")
    void validateToken_whenTokenIsValid_returnsTrue() {
        // given
        when(tokenProvider.validateToken(TOKEN)).thenReturn(true);
        when(redisTokenRepository.isLogOutToken(TOKEN)).thenReturn(false);

        // when & then
        assertTrue(tokenValidator.validateAccessToken(TOKEN));
    }
}