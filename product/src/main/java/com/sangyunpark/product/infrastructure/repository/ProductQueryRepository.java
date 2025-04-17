package com.sangyunpark.product.infrastructure.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sangyunpark.product.domain.entity.QProduct;
import com.sangyunpark.product.infrastructure.repository.condition.ProductFilterCondition;
import com.sangyunpark.product.presentation.dto.ProductDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static com.querydsl.core.types.Projections.constructor;
import static com.sangyunpark.product.domain.entity.QProduct.product;
import static com.sangyunpark.product.infrastructure.repository.condition.ProductConditionBuilder.buildCursorCondition;
import static com.sangyunpark.product.infrastructure.repository.condition.ProductConditionBuilder.buildSearchFilter;
import static com.sangyunpark.product.infrastructure.repository.sort.ProductSortResolver.resolveSort;

@Repository
@RequiredArgsConstructor
public class ProductQueryRepository {

    private final JPAQueryFactory queryFactory;

    public List<ProductDto> findByCursor(final LocalDateTime cursor, final Long lastId, final int limit, final LocalDateTime now) {
        QProduct product = QProduct.product;

        return queryFactory
                .select(constructor(ProductDto.class,
                        product.id,
                        product.title,
                        product.category.id,
                        product.price,
                        product.createdAt,
                        product.description
                ))
                .from(product)
                .where(
                        product.visible.isTrue(),
                        product.startAt.loe(now),
                        product.endAt.gt(now),
                        buildCursorCondition(cursor, lastId)
                )
                .orderBy(product.createdAt.desc(), product.id.desc())
                .limit(limit)
                .fetch();
    }

    public Page<ProductDto> searchByFilter(final ProductFilterCondition condition, final Pageable pageable) {

        List<ProductDto> products = queryFactory
                .select(constructor(ProductDto.class,
                        product.id,
                        product.title,
                        product.category.id,
                        product.price,
                        product.createdAt,
                        product.description
                ))
                .from(product)
                .where(buildSearchFilter(condition))
                .orderBy(resolveSort(condition.sortOption()))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        long total = Optional.ofNullable(
                queryFactory
                        .select(product.count())
                        .from(product)
                        .where(buildSearchFilter(condition))
                        .fetchOne()
        ).orElse(0L);

        return new PageImpl<>(products, pageable, total);
    }
}
