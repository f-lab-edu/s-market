package com.sangyunpark.product.global.init;

import com.sangyunpark.product.domain.entity.Stock;
import com.sangyunpark.product.infrastructure.repository.StockJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class RedisWarmUpRunner implements ApplicationRunner {

    private final StockJpaRepository stockJpaRepository;
    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public void run(ApplicationArguments args) {
        List<Stock> stocks = stockJpaRepository.findAll();
        if(stocks.isEmpty()) return;
        stringRedisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            for (Stock stock : stocks) {
                byte[] key = stringRedisTemplate.getStringSerializer().serialize(stock.getProductId().toString());
                byte[] value = stringRedisTemplate.getStringSerializer().serialize(stock.getQuantity().toString());
                connection.setCommands().sAdd(Objects.requireNonNull(key),value);
            }

            return null;
        });
    }
}
