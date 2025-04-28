package com.sangyunpark.product.infrastructure.repository.condition;

import com.sangyunpark.product.constant.SortOption;

public record ProductFilterCondition(
        Long categoryId,
        Long minPrice,
        Long maxPrice,
        String keyword,
        SortOption sortOption
) {
}
