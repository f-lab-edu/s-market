package com.sangyunpark.product.infrastructure.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class RedisStockRepository {

    private static final String STOCK_KEY = "stock:";

    private final RedisTemplate<String, String> redisTemplate;

    private String getKey(final Long productId) {
        return STOCK_KEY + productId;
    }

    public Optional<Long> getQuantity(final Long productId) {
        final String value = redisTemplate.opsForValue().get(getKey(productId));
        return value == null ? Optional.empty() : Optional.of(Long.parseLong(value));
    }

    public void setQuantity(final Long productId, final Long quantity, final Duration ttl) {
        redisTemplate.opsForValue().set(getKey(productId), String.valueOf(quantity), ttl);
    }

    public Long decrease(final Long productId, final Long amount) {
        final String lua = """
        local key = KEYS[1]
        local decrease = tonumber(ARGV[1])
        local current = tonumber(redis.call('get', key))
        if current and current >= decrease then
            return redis.call('decrby', key, decrease)
        else
            return -1
        end
        """;

        return redisTemplate.execute(
                new DefaultRedisScript<>(lua, Long.class),
                List.of(getKey(productId)),
                String.valueOf(amount)
        );
    }

    public void increase(final Long productId, final Long amount) {
        redisTemplate.opsForValue().increment(getKey(productId),amount);
    }
}
