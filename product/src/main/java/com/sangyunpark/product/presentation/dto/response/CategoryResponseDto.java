package com.sangyunpark.product.presentation.dto.response;

public record CategoryResponseDto(
        Long id,
        String name,
        Long parentId
) {
}
