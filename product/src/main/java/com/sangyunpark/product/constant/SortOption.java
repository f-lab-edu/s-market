package com.sangyunpark.product.constant;

import com.querydsl.core.types.OrderSpecifier;
import com.sangyunpark.product.domain.entity.QProduct;

import java.util.function.Function;

import static com.sangyunpark.product.domain.entity.QProduct.product;

public enum SortOption {
    PRICE_ASC(p -> new OrderSpecifier[]{
            p.price.asc(),
            p.id.desc()
    }),
    PRICE_DESC(p -> new OrderSpecifier[]{
            p.price.desc(),
            p.id.desc()
    }),
    LATEST(p -> new OrderSpecifier[]{
            p.createdAt.desc(),
            p.id.desc()
    });

    private final Function<QProduct, OrderSpecifier<?>[]> sortFunction;

    SortOption(Function<QProduct, OrderSpecifier<?>[]> sortFunction) {
        this.sortFunction = sortFunction;
    }

    public static SortOption from(String value) {
        if (value == null || value.isBlank()) {
            return LATEST; // 기본값
        }
        return SortOption.valueOf(value.toUpperCase()); // 안전하게 처리
    }

    public OrderSpecifier<?>[] toOrderSpecifiers() {
        return sortFunction.apply(product);
    }
}