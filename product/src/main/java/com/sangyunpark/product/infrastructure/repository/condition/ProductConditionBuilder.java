package com.sangyunpark.product.infrastructure.repository.condition;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;

import java.time.LocalDateTime;
import static com.sangyunpark.product.domain.entity.QProduct.product;

public class ProductConditionBuilder {

    public static BooleanBuilder buildSearchFilter(final ProductFilterCondition condition) {

        BooleanBuilder booleanBuilder = new BooleanBuilder();

        booleanBuilder.and(product.visible.isTrue());

        if(condition.categoryId() != null) {
            booleanBuilder.and(product.category.id.eq(condition.categoryId()));
        }

        if(condition.minPrice() != null) {
            booleanBuilder.and(product.price.goe(condition.minPrice()));
        }

        if(condition.maxPrice() != null) {
            booleanBuilder.and(product.price.loe(condition.maxPrice()));
        }

        if(condition.keyword() != null && !condition.keyword().isBlank()) {
            booleanBuilder.and(product.title.containsIgnoreCase(condition.keyword()));
        }

        return booleanBuilder;
    }

    public static BooleanExpression buildCursorCondition(final LocalDateTime cursor, final Long lastId) {
        if (cursor == null) return null;

        return product.createdAt.lt(cursor)
                .or(product.createdAt.eq(cursor).and(product.id.lt(lastId)));
    }
}
