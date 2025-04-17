package com.sangyunpark.product.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CategoryRequestDto(
        @NotBlank
        String name,
        Long parentId
) {
}
