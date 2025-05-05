package com.sangyunpark.product.application;

import com.sangyunpark.product.application.event.StockDeductedEvent;
import com.sangyunpark.product.constant.ErrorCode;
import com.sangyunpark.product.exception.BusinessException;
import com.sangyunpark.product.infrastructure.kafka.StockEventProducer;
import com.sangyunpark.product.infrastructure.redis.OrderDuplicationRepository;
import com.sangyunpark.product.infrastructure.redis.StockRedisRepository;
import com.sangyunpark.product.infrastructure.repository.StockJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockService {

    private final StockJpaRepository stockJpaRepository;
    private final StockRedisRepository stockRedisRepository;
    private final StockEventProducer stockEventProducer;
    private final OrderDuplicationRepository orderDuplicationRepository;

    public void decreaseStockAndPublish(final Long productId, final Long quantity, final Long orderId) {

        if(!orderDuplicationRepository.saveIfAbsent(orderId, Duration.ofSeconds(30L))) {
            log.info("이미 처리된 주문 orderId: {}", orderId);
            return;
        }

        if(!stockRedisRepository.isExisted(productId)) {
            Long dbQuantity = stockJpaRepository.findQuantityByProductId(productId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));
            stockRedisRepository.setIfAbsentWithTTL(productId, dbQuantity, Duration.ofMinutes(3));
        }

        final Long remainStock =  stockRedisRepository.decrease(productId, quantity);

        if(remainStock == -1) {
            throw new BusinessException(ErrorCode.STOCK_NOT_ENOUGH);
        }

        final StockDeductedEvent event = new StockDeductedEvent(orderId, productId, quantity);
        stockEventProducer.sendStockDeductedEvent(event);
    }
}
