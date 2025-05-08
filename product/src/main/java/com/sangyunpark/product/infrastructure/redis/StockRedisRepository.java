package com.sangyunpark.product.infrastructure.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class StockRedisRepository {

    private static final String STOCK_KEY = "stock:";

    private final RedisTemplate<String, String> redisTemplate;

    private final String DECREASE_SCRIPT = """
            local key = KEYS[1]
            local decrease = tonumber(ARGV[1])
            local current = tonumber(redis.call('get', key))
            if current and current >= decrease then
                return redis.call('decrby', key, decrease)
            else
                return -1
            end
            """;

    private final String INCREASE_SCRIPT = """
            if redis.call("exists", KEYS[1]) == 1 then
              return redis.call("incrby", KEYS[1], ARGV[1])
            else
              return -1
            end
            """;

    private String getKey(final Long productId) {
        return STOCK_KEY + productId;
    }

    public void setIfAbsentWithTTL(final Long productId, final Long quantity, final Duration ttl) {
        String key = getKey(productId);
        redisTemplate.opsForValue().setIfAbsent(key, String.valueOf(quantity), ttl);
    }

    public void setQuantity(final Long productId, final Long quantity, final Duration ttl) {
        redisTemplate.opsForValue().set(getKey(productId), String.valueOf(quantity), ttl);
    }

    public Long decrease(final Long productId, final Long amount) {
        return redisTemplate.execute(
                new DefaultRedisScript<>(DECREASE_SCRIPT, Long.class),
                List.of(getKey(productId)),
                String.valueOf(amount)
        );
    }

    public boolean isExisted(final Long productId) {
        String key = getKey(productId);
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    public Long increase(final Long productId, final Long amount) {
        return redisTemplate.execute(
                new DefaultRedisScript<>(INCREASE_SCRIPT, Long.class),
                List.of(getKey(productId)),
                String.valueOf(amount)
        );
    }

    public Long getQuantity(Long productId) {
        String key = getKey(productId);
        Object value = redisTemplate.opsForValue().get(key);
        return value != null ? Long.parseLong(value.toString()) : null;
    }
}
