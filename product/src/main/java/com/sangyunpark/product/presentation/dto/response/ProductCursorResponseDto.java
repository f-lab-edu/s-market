package com.sangyunpark.product.presentation.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record ProductCursorResponseDto<T>(
        List<T> content,
        LocalDateTime nextCursor,
        Long nextId
) {
}
