package com.sangyunpark.product.infrastructure.kafka;

import com.sangyunpark.product.infrastructure.kafka.event.StockDeductedEvent;
import com.sangyunpark.product.infrastructure.kafka.event.StockIncreasedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class StockEventProducer {

    private final String INCR_TOPIC = "stock.increased";
    private final String DECR_TOPIC = "stock.deducted";
    private final KafkaTemplate<String, StockDeductedEvent> kafkaDecrTemplate;
    private final KafkaTemplate<String, StockIncreasedEvent> kafkaIncrTemplate;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void sendStockDeductedEvent(final StockDeductedEvent event) {
        kafkaDecrTemplate.send(DECR_TOPIC, String.valueOf(event.productId()), event)
                .exceptionally(ex -> {
                    log.error("Kafka DB 재고 동기화 메시지 발행 실패");
                    return null;
                });
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void sendStockIncreasedEvent(final StockIncreasedEvent event) {
        kafkaIncrTemplate.send(INCR_TOPIC, String.valueOf(event.productId()), event)
                .exceptionally(ex -> {
                    log.error("Kafka DB 재고 동기화 메시지 발행 실패");
                    return null;
                });
    }
}
