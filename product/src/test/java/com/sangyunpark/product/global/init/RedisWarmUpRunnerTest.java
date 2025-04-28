package com.sangyunpark.product.global.init;

import com.sangyunpark.product.domain.entity.Stock;
import com.sangyunpark.product.infrastructure.repository.StockJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.ApplicationArguments;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.List;

import static org.mockito.Mockito.*;

public class RedisWarmUpRunnerTest {

    private StockJpaRepository stockJpaRepository;
    private StringRedisTemplate stringRedisTemplate;
    private RedisWarmUpRunner redisWarmUpRunner;

    @BeforeEach
    void setUp() {
        stockJpaRepository = mock(StockJpaRepository.class);
        stringRedisTemplate = mock(StringRedisTemplate.class);
        redisWarmUpRunner = new RedisWarmUpRunner(stockJpaRepository, stringRedisTemplate);
    }

    @Test
    @DisplayName("재고데이터가 존재 redis 저장 테스트")
    void run_재고데이터가_redis에_저장된다() {
        // given
        Stock stock1 = Stock.builder().productId(1L).quantity(10L).build();
        Stock stock2 = Stock.builder().productId(2L).quantity(20L).build();
        when(stockJpaRepository.findAll()).thenReturn(List.of(stock1, stock2));

        // when
        redisWarmUpRunner.run(mock(ApplicationArguments.class));

        // then
        verify(stringRedisTemplate, times(1))
                .executePipelined(any(RedisCallback.class));
    }

    @Test
    @DisplayName("제고데이터 존재 하지 않음 redis 저장 테스트")
    void run_재고가_없으면_redis에_저장하지_않는다() {
        // given
        when(stockJpaRepository.findAll()).thenReturn(List.of());

        // when
        redisWarmUpRunner.run(mock(ApplicationArguments.class));

        // then
        verify(stringRedisTemplate, never())
                .executePipelined(any(RedisCallback.class));
    }
}