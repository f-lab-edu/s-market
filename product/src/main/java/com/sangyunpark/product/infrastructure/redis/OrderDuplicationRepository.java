package com.sangyunpark.product.infrastructure.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;

@Repository
@RequiredArgsConstructor
public class OrderDuplicationRepository {

    private final StringRedisTemplate stringRedisTemplate;
    private final String DECR_PREFIX = "stock:deducted:";
    private final String INCR_PREFIX = "stock:increased:";

    public boolean saveIfAbsentByOrderId(final Long orderId, final Duration ttl) {
        return Boolean.TRUE.equals(stringRedisTemplate.opsForValue().setIfAbsent(DECR_PREFIX + orderId, "done", ttl));
    }

    public boolean saveIfAbsentByEventId(final String eventId, final Duration ttl) {
        return Boolean.TRUE.equals(stringRedisTemplate.opsForValue().setIfAbsent(INCR_PREFIX + eventId, "done", ttl));
    }
}
