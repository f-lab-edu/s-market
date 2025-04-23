package com.sangyunpark.product.infrastructure.kafka;

import com.sangyunpark.product.application.event.StockDeductedEvent;
import com.sangyunpark.product.infrastructure.redis.StockRedisRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class StockEventProducer {

    private final String TOPIC = "stock.deducted";

    private final StockRedisRepository stockRedisRepository;
    private final KafkaTemplate<String, StockDeductedEvent> kafkaTemplate;

    public void sendStockDeductedEvent(final StockDeductedEvent stockDeductedEvent) {

        kafkaTemplate.send(TOPIC, stockDeductedEvent)
                .exceptionally(ex -> {
                    stockRedisRepository.increase(stockDeductedEvent.productId(), stockDeductedEvent.quantity());
                    log.error("Kafka 메시지 전송 실패로 재고 복구 수행");
                    return null;
                });
    }
}
