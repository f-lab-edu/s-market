package com.sangyunpark.product.infrastructure.kafka;

import com.sangyunpark.product.application.event.StockDeductedEvent;
import com.sangyunpark.product.constant.OutboxStatus;
import com.sangyunpark.product.domain.entity.StockOutbox;
import com.sangyunpark.product.infrastructure.repository.StockOutboxRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxScheduler {

    private final String TOPIC = "stock.deducted";
    private final int SCHEDULER_DELAY = 1000;

    private final StockOutboxRepository stockOutboxRepository;
    private final KafkaTemplate<String, StockDeductedEvent> kafkaTemplate;

    @Scheduled(fixedDelay = SCHEDULER_DELAY)
    @Transactional
    public void resendPendingMessages() {

        List<StockOutbox> outboxes = stockOutboxRepository.findPendingOutboxEvent(OutboxStatus.PENDING, LocalDateTime.now());
        for(StockOutbox outbox : outboxes) {
            try {
                StockDeductedEvent event = new StockDeductedEvent(
                        outbox.getProductId(),
                        outbox.getQuantity(),
                        outbox.getOrderId()
                );

                kafkaTemplate.send(TOPIC, String.valueOf(event.productId()), event).get();
                stockOutboxRepository.updateStatusByOrderId(event.orderId(), OutboxStatus.SEND, LocalDateTime.now());

            } catch (Exception e) {
                log.error("Kafka 재시도 실패 - orderId: {}", outbox.getOrderId(), e);
            }
        }
    }
}