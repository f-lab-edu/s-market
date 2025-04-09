package com.sangyunpark.auth.jwt;

import com.sangyunpark.auth.infrastructure.repository.RedisTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class TokenValidator {

    private final TokenProvider tokenProvider;
    private final RedisTokenRepository redisTokenRepository;

    public boolean validateAccessToken(final String accessToken) {
        return accessToken != null && !accessToken.isBlank() && tokenProvider.validateToken(accessToken) && !redisTokenRepository.isLogOutToken(accessToken);
    }
}
