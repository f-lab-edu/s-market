package com.sangyunpark.product.presentation.dto;

import java.time.LocalDateTime;

public record ProductDto(
        Long id,
        String title,
        Long categoryId,
        Long price,
        LocalDateTime createdAt,
        String description
) {
}
