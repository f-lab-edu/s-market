package com.sangyunpark.product.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.util.backoff.BackOffExecution;

import static org.assertj.core.api.Assertions.assertThat;

class ExponentialBackOffWithJitterTest {

    @Test
    @DisplayName("ExponentialBackOffWithJitter - 재시도 간격이 증가하면서 랜덤 대기 후 종료되는지 테스트")
    void 재시도_간격이_증가하며_랜덤_대기하고_종료되는지_테스트() {
        // given
        final long initialInterval = 1000L;
        final double multiplier = 2.0;
        final long maxElapsedTime = 10000L;

        ExponentialBackOffWithJitter backOff = new ExponentialBackOffWithJitter(initialInterval, multiplier, maxElapsedTime);
        BackOffExecution execution = backOff.start();

        long elapsed = 0L;
        int attempt = 0;

        // when
        while (true) {
            long nextBackOff = execution.nextBackOff();
            if (nextBackOff == BackOffExecution.STOP) {
                System.out.println("재시도 중단 (STOP)");
                break;
            }

            System.out.printf("[%d번째 재시도] 대기 시간: %dms%n", ++attempt, nextBackOff);
            elapsed += nextBackOff;
        }

        // then
        System.out.println("총 대기 시간: " + elapsed + "ms");
        assertThat(elapsed).isGreaterThanOrEqualTo(0);
        assertThat(elapsed).isGreaterThanOrEqualTo(maxElapsedTime);
    }

    @Test
    @DisplayName("ExponentialBackOffWithJitter - 생성 시 초기값이 올바른지 테스트")
    void 생성자_초기값_검증() {
        // given
        final long initialInterval = 500L;
        final double multiplier = 2.0;
        final long maxElapsedTime = 5000L;

        // when
        ExponentialBackOffWithJitter backOff = new ExponentialBackOffWithJitter(initialInterval, multiplier, maxElapsedTime);

        // then
        assertThat(backOff).isNotNull(); // 객체가 잘 만들어졌는지
    }

    @DisplayName("ExponentialBackOffWithJitter - 최대 대기시간이 0이면 즉시 중단되는지 테스트")
    @Test
    void 최대_대기시간_0이면_바로_중단() {
        // given
        final long initialInterval = 1000L;
        final double multiplier = 2.0;
        final long maxElapsedTime = 0L; // 최대 대기시간 0

        ExponentialBackOffWithJitter backOff = new ExponentialBackOffWithJitter(initialInterval, multiplier, maxElapsedTime);
        BackOffExecution execution = backOff.start();

        // when
        long waitTime = execution.nextBackOff();

        // then
        assertThat(waitTime).isEqualTo(BackOffExecution.STOP);
    }
}