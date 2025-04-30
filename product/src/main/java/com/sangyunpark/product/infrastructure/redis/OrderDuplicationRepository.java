package com.sangyunpark.product.infrastructure.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;

@Repository
@RequiredArgsConstructor
public class OrderDuplicationRepository {

    private final StringRedisTemplate stringRedisTemplate;
    private final String PREFIX = "stock:deducted:";

    private String getKey(final Long orderId) {
        return PREFIX + orderId;
    }

    public boolean saveIfAbsent(final Long orderId, final Duration ttl) {
        return Boolean.TRUE.equals(stringRedisTemplate.opsForValue().setIfAbsent(getKey(orderId), "done", ttl));
    }
}
