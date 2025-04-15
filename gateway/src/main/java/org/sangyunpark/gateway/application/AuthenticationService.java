package org.sangyunpark.gateway.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.sangyunpark.gateway.filter.dto.CachedUser;
import org.sangyunpark.gateway.infrastructure.redis.RedisTokenRepository;
import org.sangyunpark.gateway.jwt.TokenProvider;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private static final String AUTH_TOKEN_KEY = "auth_token:";
    private static final String USER_TYPE = "userType";
    private static final String USER_STATUS = "userStatus";

    private final TokenProvider tokenProvider;
    private final RedisTokenRepository redisTokenRepository;
    private final ObjectMapper objectMapper;

    public Mono<Void> logout(final String token) {
        return checkBlackList(token, redisTokenRepository.removeAuthToken(token).then());
    }

    public Mono<CachedUser> getAuthenticatedUser(final String token) {
        return checkBlackList(token, Mono.defer(() -> getCachedUser(token).switchIfEmpty(parseAndCacheUser(token))));
    }

    private <T> Mono<T> checkBlackList(String token, Mono<T> nextAction) {
        return redisTokenRepository.isBlackList(token)
                .flatMap(isBlacklisted -> {
                    if (isBlacklisted) {
                        return Mono.error(new RuntimeException());
                    }
                    return nextAction;
                });
    }

    public Mono<CachedUser> getCachedUser(final String token) {
        return redisTokenRepository.getCachedUser(token)
                .flatMap(json -> {
                    try {
                        CachedUser cachedUser = objectMapper.readValue(json, CachedUser.class);
                        return Mono.just(cachedUser);
                    }catch (Exception e) {
                        return Mono.error(new RuntimeException());
                    }
                });
    }

    public Mono<CachedUser> parseAndCacheUser(final String token) {
        return  parseClaims(token)
                .flatMap(cachedUser -> cacheUser(token, cachedUser).thenReturn(cachedUser));
    }

    public Mono<CachedUser> parseClaims(final String token) {
        return Mono.fromCallable(() -> {
            Claims claims = tokenProvider.parseClaims(token);
            return new CachedUser(
                    claims.getSubject(),
                    claims.get(USER_TYPE, String.class),
                    claims.get(USER_STATUS, String.class)
            );
        });
    }

    public Mono<Boolean> cacheUser(final String token, final CachedUser cachedUser) {
        try {
            final String key = AUTH_TOKEN_KEY + token;
            final String value = objectMapper.writeValueAsString(cachedUser);
            return redisTokenRepository.saveAuthToken(key, value, Duration.ofMinutes(10));
        }catch (Exception e) {
            return Mono.error(new RuntimeException());
        }
    }
}