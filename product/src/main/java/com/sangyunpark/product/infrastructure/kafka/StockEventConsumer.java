package com.sangyunpark.product.infrastructure.kafka;

import com.sangyunpark.product.application.event.StockDeductedEvent;
import com.sangyunpark.product.constant.ErrorCode;
import com.sangyunpark.product.constant.OutboxStatus;
import com.sangyunpark.product.exception.BusinessException;
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

    private final String TOPIC = "stock.deducted";
    private final String GROUP_ID = "product-service";

    private final OrderDuplicationRepository orderDuplicationRepository;
    private final StockJpaRepository stockJpaRepository;
    private final StockOutboxRepository stockOutboxRepository;

    @Transactional
    @KafkaListener(topics = TOPIC, groupId = GROUP_ID)
    public void consumeStockDeductedEvent(final StockDeductedEvent event) {

        final Long orderId = event.orderId();

        if (!orderDuplicationRepository.saveIfAbsent(orderId, Duration.ofMinutes(3))) {
            log.info("이미 처리된 이벤트입니다. orderId={}", orderId);
            return;
        }

        final int updatedRows = stockJpaRepository.decreaseStock(event.productId(), event.quantity());
        if(updatedRows == 0) {
            throw new BusinessException(ErrorCode.STOCK_NOT_ENOUGH);
        }

        stockOutboxRepository.updateStatusByOrderId(event.orderId(), OutboxStatus.SEND, LocalDateTime.now());
    }
}