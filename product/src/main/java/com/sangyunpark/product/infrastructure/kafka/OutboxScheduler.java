package com.sangyunpark.product.infrastructure.kafka;

import com.sangyunpark.product.constant.OutboxType;
import com.sangyunpark.product.infrastructure.kafka.event.StockDeductedEvent;
import com.sangyunpark.product.constant.OutboxStatus;
import com.sangyunpark.product.domain.entity.StockOutbox;
import com.sangyunpark.product.infrastructure.kafka.event.StockIncreasedEvent;
import com.sangyunpark.product.infrastructure.repository.StockOutboxRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxScheduler {

    private final String DECR_TOPIC = "stock.deducted";
    private final String INCR_TOPIC = "stock.increased";
    private final int SCHEDULER_DELAY = 1000;

    private final StockOutboxRepository stockOutboxRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Scheduled(fixedDelay = SCHEDULER_DELAY)
    @SchedulerLock(name = "outboxScheduler_resendPendingMessages", lockAtMostFor = "10s", lockAtLeastFor = "1s")
    @Transactional
    public void resendPendingMessages() {

        final List<StockOutbox> outboxes = stockOutboxRepository.findPendingOutboxEvent(OutboxStatus.PENDING, LocalDateTime.now());
        for(StockOutbox outbox : outboxes) {
            try {

                if(OutboxType.INCR == outbox.getType()) {
                    processIncrease(outbox);
                }else if(OutboxType.DECR == outbox.getType()) {
                    processDecrease(outbox);
                }

            } catch (Exception e) {
                if (OutboxType.INCR.equals(outbox.getType())) {
                    log.error("Kafka 재시도 실패 - eventId: {}", outbox.getEventId(), e);
                } else {
                    log.error("Kafka 재시도 실패 - orderId: {}", outbox.getOrderId(), e);
                }
            }
        }
    }

    private void processIncrease(StockOutbox outbox) throws Exception {
        StockIncreasedEvent event = new StockIncreasedEvent(
                outbox.getEventId(),
                outbox.getProductId(),
                outbox.getQuantity()
        );

        kafkaTemplate.send(INCR_TOPIC, String.valueOf(event.productId()), event).get();
        stockOutboxRepository.updateStatusByEventId(event.eventId(), OutboxStatus.SEND, LocalDateTime.now());
    }

    private void processDecrease(StockOutbox outbox) throws Exception {
        StockDeductedEvent event = new StockDeductedEvent(
                outbox.getOrderId(),
                outbox.getProductId(),
                outbox.getQuantity()
        );

        kafkaTemplate.send(DECR_TOPIC, String.valueOf(event.productId()), event).get();
        stockOutboxRepository.updateStatusByOrderId(event.orderId(), OutboxStatus.SEND, LocalDateTime.now());
    }
}