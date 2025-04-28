package com.sangyunpark.product.infrastructure.kafka.dlt;

import com.sangyunpark.product.application.event.StockDeductedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class StockDLTListener {

    @KafkaListener(topics = "stock.deducted.DLT", groupId = "product-service-dlt")
    public void listenStockDLT(final StockDeductedEvent event) {
        log.error("DLT 수신 - event: {}", event);
    }
}
