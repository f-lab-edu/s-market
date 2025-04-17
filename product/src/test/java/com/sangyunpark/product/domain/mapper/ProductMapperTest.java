package com.sangyunpark.product.domain.mapper;

import com.sangyunpark.product.domain.entity.Category;
import com.sangyunpark.product.domain.entity.Product;
import com.sangyunpark.product.presentation.dto.request.ProductRequestDto;
import com.sangyunpark.product.presentation.dto.response.ProductResponseDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("NonAsciiCharacters")
class ProductMapperTest {

    private final ProductMapper productMapper = Mappers.getMapper(ProductMapper.class);

    @Test
    @DisplayName("ProductRequestDto → Product 매핑 테스트")
    void toEntity_매핑_성공() {
        // given
        LocalDateTime now = LocalDateTime.now();
        ProductRequestDto dto = new ProductRequestDto(
                1L, "상품명", "설명입니다", 10000L, true, now, now
        );

        // when
        Product product = productMapper.toEntity(dto, new Category());

        // then
        assertThat(product.getTitle()).isEqualTo("상품명");
        assertThat(product.getDescription()).isEqualTo("설명입니다");
        assertThat(product.getPrice()).isEqualTo(10000L);
        assertThat(product.getVisible()).isTrue();
        assertThat(product.getStartAt()).isEqualTo(now);
        assertThat(product.getEndAt()).isEqualTo(now);
        assertThat(product.getCreatedAt()).isNotNull(); // @AfterMapping 설정 확인
        assertThat(product.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Product → ProductResponseDto 매핑 테스트")
    void toDto_매핑_성공() {
        // given
        Category category = new Category();
        ReflectionTestUtils.setField(category, "id", 3L);

        Product product = new Product();
        ReflectionTestUtils.setField(product, "id", 100L);
        ReflectionTestUtils.setField(product, "title", "상품명");
        ReflectionTestUtils.setField(product, "description", "설명입니다");
        ReflectionTestUtils.setField(product, "price", 15000L);
        ReflectionTestUtils.setField(product, "visible", false);
        ReflectionTestUtils.setField(product, "category", category);
        LocalDateTime now = LocalDateTime.now();
        ReflectionTestUtils.setField(product, "startAt", now);
        ReflectionTestUtils.setField(product, "endAt", now);
        ReflectionTestUtils.setField(product, "createdAt", now);
        ReflectionTestUtils.setField(product, "updatedAt", now);

        // when
        ProductResponseDto dto = productMapper.toDto(product);

        // then
        assertThat(dto.id()).isEqualTo(100L);
        assertThat(dto.categoryId()).isEqualTo(3L);
        assertThat(dto.title()).isEqualTo("상품명");
        assertThat(dto.description()).isEqualTo("설명입니다");
        assertThat(dto.price()).isEqualTo(15000L);
        assertThat(dto.visible()).isFalse();
        assertThat(dto.startAt()).isEqualTo(now);
        assertThat(dto.endAt()).isEqualTo(now);
        assertThat(dto.createdAt()).isEqualTo(now);
        assertThat(dto.updatedAt()).isEqualTo(now);
    }
}