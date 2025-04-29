package com.sangyunpark.product.infrastructure.kafka;

import com.sangyunpark.product.application.event.StockDeductedEvent;
import com.sangyunpark.product.constant.ErrorCode;
import com.sangyunpark.product.exception.BusinessException;
import com.sangyunpark.product.infrastructure.redis.OrderDuplicationRepository;
import com.sangyunpark.product.infrastructure.repository.StockJpaRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class StockEventConsumer {

    private final String TOPIC = "stock.deducted";
    private final String GROUP_ID = "product-service";

    private final StockJpaRepository stockJpaRepository;
    private final OrderDuplicationRepository orderDuplicationRepository;

    @Transactional
    @KafkaListener(topics = TOPIC, groupId = GROUP_ID)
    public void consumeStockDeductedEvent(final StockDeductedEvent event) {

        if(orderDuplicationRepository.isAlreadyProcessed(event.orderId())) {
            log.info("이미 처리된 주문 orderId: {}", event.orderId());
            return;
        }

        int updatedRows = stockJpaRepository.decreaseStock(event.productId(), event.quantity());
        if(updatedRows == 0) {
            throw new BusinessException(ErrorCode.STOCK_NOT_ENOUGH);
        }

        orderDuplicationRepository.saveProcessed(event.orderId());
    }
}