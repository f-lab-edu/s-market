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
    private static final String TEST_ACCESS_TOKEN = "test-access-token";
    private static final long REMAINING_TIME = 500L;

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

    @Test
    @DisplayName("로그아웃시 토큰이 Redis에 저장된다.")
    void 로그아웃시_토큰이_레디스에_저장() {
        // given, when
        redisTokenRepository.saveLogOutToken(TEST_ACCESS_TOKEN, REMAINING_TIME);

        // then
        assertThat(redisTokenRepository.exists(TEST_ACCESS_TOKEN)).isTrue();
    }

    @Test
    @DisplayName("로그아웃 토큰이 Redis에 존재하지 않으면 false를 반환한다.")
    void 로그아웃시_토큰이_레디스에_저장된_경우_true_반환() {
        // given
        redisTokenRepository.saveLogOutToken(TEST_ACCESS_TOKEN, REMAINING_TIME);

        // when
        boolean result = redisTokenRepository.isLogOutToken(TEST_ACCESS_TOKEN);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("로그아웃시 블랙리스트에 등록된 토큰이 만료된 경우 false를 반환한다.")
    void 로그아웃시_블랙리스트에_등록된_토큰이_만료된_경우_false_반환() throws InterruptedException {
        //given
        redisTokenRepository.saveLogOutToken(TEST_ACCESS_TOKEN, 1L);

        Thread.sleep(3L);

        // when
        boolean result = redisTokenRepository.isLogOutToken(TEST_ACCESS_TOKEN);

        // then
        assertThat(result).isFalse();
    }

    @AfterEach
    void tearDown() {
        redisTokenRepository.delete(EMAIL);
    }
}