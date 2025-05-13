package com.sangyunpark.product.infrastructure.kafka.event;

public record StockDeductedEvent(
        Long orderId,
        Long productId,
        Long quantity
) {
}
