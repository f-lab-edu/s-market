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
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxKafkaPublisher {

    private final String TOPIC = "stock.deducted";
    private final int SCHEDULER_DELAY = 500;

    private final StockOutboxRepository stockOutboxRepository;
    private final KafkaTemplate<String, StockDeductedEvent> stockOutboxKafkaTemplate;

    @Scheduled(fixedDelay = SCHEDULER_DELAY)
    @Transactional
    public void publishPendingEvent() {
        List<StockOutbox> events = stockOutboxRepository.findPendingOutboxEvent(OutboxStatus.PENDING.name());
        List<StockOutbox> successEvents = new ArrayList<>();

        for (StockOutbox event : events) {
            try {
                StockDeductedEvent stockDeductedEvent = new StockDeductedEvent(event.getOrderId(), event.getProductId(), event.getQuantity());
                stockOutboxKafkaTemplate.send(TOPIC, stockDeductedEvent.productId().toString(), stockDeductedEvent).get();
                successEvents.add(event);
            } catch (Exception e) {
                log.error("Kafka 전송 실패 - eventId: {}, orderId: {}, productId: {}", event.getId(), event.getOrderId(), event.getProductId(), e);
            }
        }

        if (!successEvents.isEmpty()) {
            List<Long> successIds = successEvents.stream().map(StockOutbox::getId).toList();
            stockOutboxRepository.bulkUpdateStatus(OutboxStatus.SEND, LocalDateTime.now(), successIds);
        }
    }
}