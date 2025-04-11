package org.sangyunpark.gateway.infrastructure.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
@RequiredArgsConstructor
public class TokenBlackListRepository {

    private final ReactiveStringRedisTemplate redisTemplate;

    public Mono<Boolean> isBlackList(final String token) {
        return redisTemplate.hasKey(token);
    }
}
