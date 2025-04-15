package com.sangyunpark.auth.infrastructure.repository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Repository
public class RedisTokenRepository {

    private static final String BLACK_LIST_KEY = "black_list:";

    private final StringRedisTemplate redisTemplate;
    private final long refreshTokenExpireTime;

    public RedisTokenRepository(final StringRedisTemplate redisTemplate, @Value("${jwt.refreshToken-expiration}") final Long expireTime) {
        this.redisTemplate = redisTemplate;
        this.refreshTokenExpireTime = expireTime;
    }

    public void save(final String email, final String refreshToken) {
        redisTemplate.opsForValue().set(email, refreshToken, refreshTokenExpireTime, TimeUnit.MILLISECONDS);
    }

    public Optional<String> findByEmail(final String email) {
        return Optional.ofNullable(redisTemplate.opsForValue().get(email));
    }

    public void delete(final String email) {
        redisTemplate.delete(email);
    }

    public boolean exists(final String email) {
        return redisTemplate.hasKey(email);
    }

    public void saveLogOutToken(final String accessToken, final long remainingTime) {
        redisTemplate.opsForValue().set(accessToken, BLACK_LIST_KEY + accessToken, remainingTime, TimeUnit.MILLISECONDS);
    }

    public boolean isLogOutToken(final String accessToken) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(accessToken));
    }
}
