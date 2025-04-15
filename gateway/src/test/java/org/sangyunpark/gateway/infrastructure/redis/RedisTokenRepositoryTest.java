package org.sangyunpark.gateway.infrastructure.redis;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.redis.DataRedisTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import reactor.test.StepVerifier;

import java.time.Duration;

@Import(RedisTokenRepository.class)
@DataRedisTest
class RedisTokenRepositoryTest {

    @Autowired
    RedisTokenRepository redisTokenRepository;

    @Autowired
    ReactiveStringRedisTemplate redisTemplate;

    private static final String TOKEN = "sample-token";
    private static final String BLACKLIST_KEY = "black_list:" + TOKEN;
    private static final String AUTH_TOKEN_KEY = "auth_token:" + TOKEN;
    private static final String VALUE = "{\"email\":\"test@example.com\",\"userType\":\"NORMAL\",\"userStatus\":\"ACTIVE\"}";

    @Test
    @DisplayName("블랙리스트 키 저장 후 존재 여부 확인")
    void 블랙리스트_저장_및_존재_확인() {
        redisTokenRepository.saveAuthToken(BLACKLIST_KEY, "logout", Duration.ofMinutes(5)).block();

        StepVerifier.create(redisTokenRepository.isBlackList(TOKEN))
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    @DisplayName("캐시된 사용자 정보 저장 후 조회")
    void 캐시_저장_후_조회() {
        redisTokenRepository.saveAuthToken(AUTH_TOKEN_KEY, VALUE, Duration.ofMinutes(10)).block();

        StepVerifier.create(redisTokenRepository.getCachedUser(TOKEN))
                .expectNext(VALUE)
                .verifyComplete();
    }

    @AfterEach
    void tearDown() {
        redisTemplate.delete(BLACKLIST_KEY).block();
        redisTemplate.delete(AUTH_TOKEN_KEY).block();
    }
}