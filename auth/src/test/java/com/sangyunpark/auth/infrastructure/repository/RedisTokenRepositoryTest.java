package com.sangyunpark.auth.infrastructure.repository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("NonAsciiCharacters")
@SpringBootTest
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
    @DisplayName("리프레시 토큰을 삭제하고, 삭제된 토큰의 존재 여부를 조회합니다.")
    void 리프레쉬_토큰_삭제후_삭제한_토큰_조회_여부_확인() {
        redisTokenRepository.save(EMAIL, REFRESH_TOKEN);
        redisTokenRepository.delete(EMAIL);

        assertThat(redisTokenRepository.exists(EMAIL)).isFalse();
    }

    @AfterEach
    void tearDown() {
        redisTokenRepository.delete(EMAIL);
    }
}