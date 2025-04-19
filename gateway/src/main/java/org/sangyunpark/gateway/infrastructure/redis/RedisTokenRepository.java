package org.sangyunpark.gateway.infrastructure.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Repository
@RequiredArgsConstructor
public class RedisTokenRepository {

    private final String BLACKLIST_KEY = "black_list:";
    private final String AUTH_TOKEN_PREFIX = "auth_token:";
    private final String REFRESH_TOKEN_KEY = "refresh_token:";

    private final ReactiveStringRedisTemplate redisTemplate;

    public Mono<Boolean> isBlackList(final String token) {
        return redisTemplate.hasKey(BLACKLIST_KEY + token);
    }

    public Mono<String> getCachedUser(final String token) {
        return redisTemplate.opsForValue().get(AUTH_TOKEN_PREFIX+token);
    }

    public Mono<Boolean> saveAuthToken(final String key, final String value, final Duration duration) {
        return redisTemplate.opsForValue().set(key, value, duration);
    }

    public Mono<Boolean> removeAuthToken(final String token, final String email) {
        Mono<Boolean> deleteRefresh = redisTemplate.opsForValue().delete(REFRESH_TOKEN_KEY + email);
        Mono<Boolean> deleteAccess = redisTemplate.opsForValue().delete(AUTH_TOKEN_PREFIX + token);

        return Mono.zip(deleteRefresh, deleteAccess)
                .map(tuple -> tuple.getT1() && tuple.getT2()); // 둘 다 삭제되어야 true
    }
}
