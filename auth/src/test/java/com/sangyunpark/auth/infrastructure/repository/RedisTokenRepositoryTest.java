package com.sangyunpark.auth.infrastructure.repository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("NonAsciiCharacters")
@SpringBootTest
@ActiveProfiles("test")
class RedisTokenRepositoryTest {

    private static final String EMAIL = "test@example.com";
    private static final String REFRESH_TOKEN = "refreshToken123";

    @Autowired
    private RedisTokenRepository redisTokenRepository;

    @Test
    @DisplayName("리프레쉬 토큰을 저장하고, 저장한 토큰 조회를 성공합니다.")
    void 리프레쉬_토큰_저장후_저장한_토큰_조회_성공() {
        redisTokenRepository.save(EMAIL, REFRESH_TOKEN);
        Optional<String> result = redisTokenRepository.findByEmail(EMAIL);

        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(REFRESH_TOKEN);
    }

    @Test
    void deleteAndExists() {
        redisTokenRepository.save(EMAIL, REFRESH_TOKEN);
        redisTokenRepository.delete(EMAIL);

        assertThat(redisTokenRepository.exists(EMAIL)).isFalse();
    }

    @AfterEach
    void tearDown() {
        redisTokenRepository.delete(EMAIL);
    }
}