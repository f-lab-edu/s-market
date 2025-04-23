package com.sangyunpark.product.application.event;

public record StockDeductedEvent(
        Long orderId,
        Long productId,
        Long quantity
) {
}
