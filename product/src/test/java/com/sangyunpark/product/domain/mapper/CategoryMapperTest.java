package com.sangyunpark.product.domain.mapper;

import com.sangyunpark.product.domain.entity.Category;
import com.sangyunpark.product.presentation.dto.request.CategoryRequestDto;
import com.sangyunpark.product.presentation.dto.response.CategoryResponseDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("NonAsciiCharacters")
class CategoryMapperTest {

    private final CategoryMapper categoryMapper = Mappers.getMapper(CategoryMapper.class);

    @Test
    @DisplayName("Category → CategoryResponseDto 매핑 테스트")
    void toDto_매핑_성공() {
        // given
        Category parent = new Category(1L, "부모", null, null);
        Category category = new Category(2L, "자식", parent, null);

        // when
        CategoryResponseDto dto = categoryMapper.toDto(category);

        // then
        assertThat(dto.id()).isEqualTo(2L);
        assertThat(dto.name()).isEqualTo("자식");
        assertThat(dto.parentId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Category 리스트 → CategoryResponseDto 리스트 매핑 테스트")
    void toDtoList_매핑_성공() {
        // given
        Category category1 = new Category(1L, "카테고리1", null, null);
        Category category2 = new Category(2L, "카테고리2", null, null);

        // when
        List<CategoryResponseDto> dtoList = categoryMapper.toDtoList(List.of(category1, category2));

        // then
        assertThat(dtoList).hasSize(2);
        assertThat(dtoList.get(0).name()).isEqualTo("카테고리1");
        assertThat(dtoList.get(1).name()).isEqualTo("카테고리2");
    }

    @Test
    @DisplayName("CategoryResponseDto → Category 매핑 테스트")
    void toEntity_매핑_성공() {
        // given
        final CategoryRequestDto dto = new CategoryRequestDto("디지털", 1L);
        final Category parentCategory = Category.builder().id(5L).build();

        // when
        Category category = categoryMapper.toEntity(dto, parentCategory);

        // then
        assertThat(category.getName()).isEqualTo("디지털");
        assertThat(category.getId()).isNull();
        assertThat(category.getChildren()).isEmpty();
        assertThat(category.getParent()).isEqualTo(parentCategory);
    }
}