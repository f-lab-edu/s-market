package com.sangyunpark.product.infrastructure.kafka;

import com.sangyunpark.product.infrastructure.kafka.event.StockDeductedEvent;
import com.sangyunpark.product.constant.ErrorCode;
import com.sangyunpark.product.constant.OutboxStatus;
import com.sangyunpark.product.exception.BusinessException;
import com.sangyunpark.product.infrastructure.kafka.event.StockIncreasedEvent;
import com.sangyunpark.product.infrastructure.redis.OrderDuplicationRepository;
import com.sangyunpark.product.infrastructure.repository.StockJpaRepository;
import com.sangyunpark.product.infrastructure.repository.StockOutboxRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import java.time.Duration;
import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class StockEventConsumer {

    private final String DECR_TOPIC = "stock.deducted";
    private final String INCR_TOPIC = "stock.increased";
    private final String DECR_GROUP_ID = "product-service-decrease";
    private final String INCR_GROUP_ID = "product-service-increase";

    private final OrderDuplicationRepository orderDuplicationRepository;
    private final StockJpaRepository stockJpaRepository;
    private final StockOutboxRepository stockOutboxRepository;

    @Transactional
    @KafkaListener(topics = DECR_TOPIC, groupId = DECR_GROUP_ID)
    public void consumeStockDeductedEvent(final StockDeductedEvent event) {

        final Long orderId = event.orderId();

        if (!orderDuplicationRepository.saveIfAbsentByOrderId(orderId, Duration.ofSeconds(30))) {
            log.info("이미 처리된 이벤트입니다. orderId={}", orderId);
            return;
        }

        final int updatedRows = stockJpaRepository.decreaseStock(event.productId(), event.quantity());
        if(updatedRows == 0) {
            throw new BusinessException(ErrorCode.STOCK_NOT_ENOUGH);
        }

        stockOutboxRepository.updateStatusByOrderId(event.orderId(), OutboxStatus.SEND, LocalDateTime.now());
    }

    @Transactional
    @KafkaListener(topics = INCR_TOPIC, groupId = INCR_GROUP_ID)
    public void consumeStockIncreasedEvent(final StockIncreasedEvent event) {

        final String eventId = event.eventId();

        if(!orderDuplicationRepository.saveIfAbsentByEventId(eventId, Duration.ofSeconds(30))) {
            log.info("이미 처리된 이벤트입니다. eventId={}", eventId);
            return;
        }

        int updatedRows = stockJpaRepository.increaseStock(event.productId(), event.quantity());
        if (updatedRows == 0) {
            throw new BusinessException(ErrorCode.PRODUCT_NOT_FOUND);
        }

        stockOutboxRepository.updateStatusByEventId(eventId, OutboxStatus.SEND, LocalDateTime.now());
    }
}