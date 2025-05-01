package com.sangyunpark.product.infrastructure.kafka;

import com.sangyunpark.product.application.event.StockDeductedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class StockEventProducer {

    private final String TOPIC = "stock.deducted";
    private final KafkaTemplate<String, StockDeductedEvent> kafkaTemplate;

    public void sendStockDeductedEvent(final StockDeductedEvent stockDeductedEvent) {
        kafkaTemplate.send(TOPIC, String.valueOf(stockDeductedEvent.productId()), stockDeductedEvent)
                .exceptionally(ex -> {
                    log.error("Kafka DB 재고 동기화 메시지 발행 실패");
                    return null;
                });
    }
}
