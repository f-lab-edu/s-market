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

        if(accessToken == null || accessToken.isBlank()) {
            return false;
        }

        if(!tokenProvider.validateToken(accessToken)) {
            return false;
        }

        if(redisTokenRepository.isLogOutToken(accessToken)) {
            return false;
        }

        return true;
    }
}
