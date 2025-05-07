package com.sangyunpark.product.application;

import com.sangyunpark.product.constant.OutboxType;
import com.sangyunpark.product.infrastructure.kafka.event.StockDeductedEvent;
import com.sangyunpark.product.constant.ErrorCode;
import com.sangyunpark.product.constant.OutboxStatus;
import com.sangyunpark.product.domain.entity.StockOutbox;
import com.sangyunpark.product.exception.BusinessException;
import com.sangyunpark.product.infrastructure.kafka.event.StockIncreasedEvent;
import com.sangyunpark.product.infrastructure.redis.StockRedisRepository;
import com.sangyunpark.product.infrastructure.repository.StockJpaRepository;
import com.sangyunpark.product.infrastructure.repository.StockOutboxRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockService {

    private final ApplicationEventPublisher applicationEventPublisher;
    private final StockJpaRepository stockJpaRepository;
    private final StockRedisRepository stockRedisRepository;
    private final StockOutboxRepository stockOutboxRepository;

    @Transactional
    public void decreaseStockAndPublish(final Long productId, final Long quantity, final Long orderId) {

        if(!stockRedisRepository.isExisted(productId)) {
            final Long dbQuantity = stockJpaRepository.findQuantityByProductId(productId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));
            stockRedisRepository.setIfAbsentWithTTL(productId, dbQuantity, Duration.ofMinutes(3));
        }

        Long remainStock;

        try {
            remainStock =  stockRedisRepository.decrease(productId, quantity);
        } catch (Exception e) {
            log.error("Redis 재고 감소 실패: productId={}, quantity={}, orderId={}", productId, quantity, orderId);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }

        if(remainStock == -1) {
            throw new BusinessException(ErrorCode.STOCK_NOT_ENOUGH);
        }

        final StockOutbox stockOutbox = StockOutbox.builder()
                .orderId(orderId)
                .productId(productId)
                .quantity(quantity)
                .status(OutboxStatus.PENDING)
                .type(OutboxType.DECR)
                .build();

        stockOutboxRepository.save(stockOutbox);
        applicationEventPublisher.publishEvent(new StockDeductedEvent(orderId, productId, quantity));
    }

    @Transactional
    public void increaseStock(final Long productId, final Long quantity) {
        Long result = stockRedisRepository.increase(productId, quantity);
        if (result == -1) {
            throw new BusinessException(ErrorCode.PRODUCT_NOT_FOUND);
        }

        final String eventId = UUID.randomUUID().toString();

        StockOutbox stockOutbox = StockOutbox.builder()
                .productId(productId)
                .eventId(eventId)
                .quantity(quantity)
                .status(OutboxStatus.PENDING)
                .type(OutboxType.INCR)
                .build();

        stockOutboxRepository.save(stockOutbox);
        applicationEventPublisher.publishEvent(new StockIncreasedEvent(eventId, productId, quantity));
    }
}