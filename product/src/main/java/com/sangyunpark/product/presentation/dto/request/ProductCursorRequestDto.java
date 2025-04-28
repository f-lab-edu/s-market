package com.sangyunpark.product.presentation.dto.request;

import java.time.LocalDateTime;

public record ProductCursorRequestDto(
        LocalDateTime cursor,
        Long lastId,
        int size
) {
}
