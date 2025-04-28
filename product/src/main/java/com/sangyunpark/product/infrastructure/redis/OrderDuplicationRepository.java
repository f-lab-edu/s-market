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

    public boolean isAlreadyProcessed(final Long orderId) {
        return Boolean.TRUE.equals(stringRedisTemplate.hasKey(PREFIX + orderId));
    }

    public void saveProcessed(final Long orderId) {
        stringRedisTemplate.opsForValue().set(PREFIX + orderId, "done", Duration.ofMinutes(10));
    }
}
