package com.sangyunpark.product.infrastructure.kafka.event;

public record StockIncreasedEvent(
        String eventId,
        Long productId,
        Long quantity
) {
}