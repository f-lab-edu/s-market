package com.sangyunpark.product.presentation.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record ProductRequestDto(

        @NotNull
        Long categoryId,

        @NotBlank
        String title,

        @NotBlank
        String description,

        @NotNull @Min(1)
        Long price,

        @NotNull
        Boolean visible,

        @NotNull
        LocalDateTime startAt,

        @NotNull
        LocalDateTime endAt
) {

}
