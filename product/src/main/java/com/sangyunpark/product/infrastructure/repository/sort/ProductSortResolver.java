package com.sangyunpark.product.infrastructure.repository.sort;

import com.querydsl.core.types.OrderSpecifier;
import com.sangyunpark.product.constant.SortOption;

import static com.sangyunpark.product.domain.entity.QProduct.product;

public class ProductSortResolver {

    public static OrderSpecifier<?>[] resolveSort(final SortOption sortOptions) {

        return switch(sortOptions) {
            case PRICE_ASC -> new OrderSpecifier[] {
                    product.price.asc(),
                    product.id.desc()
            };
            case PRICE_DESC -> new OrderSpecifier[] {
                    product.price.desc(),
                    product.id.desc()
            };
            case LATEST -> new OrderSpecifier[] {
                    product.createdAt.desc(),
                    product.id.desc()
            };
        };
    }
}
