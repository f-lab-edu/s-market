package com.sangyunpark.product.infrastructure.kafka.dlt;

import com.sangyunpark.product.infrastructure.kafka.event.StockDeductedEvent;
import com.sangyunpark.product.infrastructure.redis.StockRedisRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class StockDLTListener {

    private final StockRedisRepository stockRedisRepository;

    @KafkaListener(topics = "stock.deducted.DLT", groupId = "product-service-dlt")
    public void listenStockDLT(final StockDeductedEvent event) {
        log.error("Dead Letter 수신 - 재고 복구 진행. event: {}", event);
        stockRedisRepository.increase(event.productId(), event.quantity());
    }
}
