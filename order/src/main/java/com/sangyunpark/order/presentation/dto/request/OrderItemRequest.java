package com.sangyunpark.order.presentation.dto.request;

public record OrderItemRequest(
        Long productId,
        int quantity
) {
}
