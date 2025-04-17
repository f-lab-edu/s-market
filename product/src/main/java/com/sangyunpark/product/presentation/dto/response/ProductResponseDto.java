package com.sangyunpark.product.presentation.dto.response;

import java.time.LocalDateTime;

public record ProductResponseDto(
        Long id,
        Long categoryId,
        String title,
        String description,
        Long price,
        Boolean visible,
        LocalDateTime startAt,
        LocalDateTime endAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
