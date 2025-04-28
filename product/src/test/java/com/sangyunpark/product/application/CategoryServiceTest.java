package com.sangyunpark.product.application;

import com.sangyunpark.product.domain.entity.Category;
import com.sangyunpark.product.domain.mapper.CategoryMapper;
import com.sangyunpark.product.exception.BusinessException;
import com.sangyunpark.product.infrastructure.repository.CategoryJpaRepository;
import com.sangyunpark.product.presentation.dto.request.CategoryRequestDto;
import com.sangyunpark.product.presentation.dto.response.CategoryResponseDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static com.sangyunpark.product.constant.ErrorCode.CATEGORY_NOT_FOUND;
import static com.sangyunpark.product.constant.ErrorCode.CATEGORY_SELF_PARENT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @InjectMocks
    private CategoryService categoryService;

    @Mock
    private CategoryJpaRepository categoryJpaRepository;

    @Mock
    private CategoryMapper categoryMapper;

    @Test
    @DisplayName("카테고리 전체 조회 성공")
    void 카테고리_전체_조회_성공() {
        // given
        List<Category> categories = List.of(mock(Category.class));
        when(categoryJpaRepository.findAll()).thenReturn(categories);
        when(categoryMapper.toDtoList(categories)).thenReturn(List.of(mock(CategoryResponseDto.class)));

        // when
        List<CategoryResponseDto> result = categoryService.findAllCategories();

        // then
        assertThat(result).hasSize(1);
        verify(categoryJpaRepository).findAll();
    }

    @Test
    @DisplayName("카테고리 단건 조회 성공")
    void 카테고리_단건_조회_성공() {
        // given
        Long id = 1L;
        Category category = mock(Category.class);
        CategoryResponseDto responseDto = mock(CategoryResponseDto.class);

        when(categoryJpaRepository.findById(id)).thenReturn(Optional.of(category));
        when(categoryMapper.toDto(category)).thenReturn(responseDto);

        // when
        CategoryResponseDto result = categoryService.findCategoryDtoById(id);

        // then
        assertThat(result).isEqualTo(responseDto);
        verify(categoryJpaRepository).findById(id);
    }

    @Test
    @DisplayName("카테고리 조회 실패 - 존재하지 않음")
    void 카테고리_조회_실패() {
        // given
        Long id = 1L;
        when(categoryJpaRepository.findById(id)).thenReturn(Optional.empty());

        // when, then
        assertThatThrownBy(() -> categoryService.findCategoryById(id))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", CATEGORY_NOT_FOUND);
    }

    @Test
    @DisplayName("카테고리 생성 성공")
    void 카테고리_생성_성공() {
        // given
        CategoryRequestDto dto = new CategoryRequestDto("전자제품", null);
        Category newCategory = Category.builder().name(dto.name()).depth(0).build();

        when(categoryJpaRepository.save(any(Category.class))).thenReturn(newCategory);

        // when
        Long id = categoryService.createCategory(dto);

        // then
        assertThat(id).isEqualTo(newCategory.getId());
        verify(categoryJpaRepository).save(any(Category.class));
    }

    @Test
    @DisplayName("카테고리 수정 성공 - 이름 변경")
    void 카테고리_수정_성공_이름변경() {
        // given
        Long id = 1L;
        CategoryRequestDto dto = new CategoryRequestDto("새로운이름", null);
        Category category = Category.builder().id(id).name("기존이름").depth(0).build();

        when(categoryJpaRepository.findWithChildrenById(id)).thenReturn(Optional.of(category));

        // when
        categoryService.updateCategory(id, dto);

        // then
        assertThat(category.getName()).isEqualTo("새로운이름");
        verify(categoryJpaRepository).findWithChildrenById(id);
    }

    @Test
    @DisplayName("카테고리 수정 실패 - 순환 참조 발생")
    void 카테고리_수정_실패_순환참조() {
        // given
        Long id = 1L;
        CategoryRequestDto dto = new CategoryRequestDto("새이름", id); // 자기 자신을 부모로 설정
        Category category = Category.builder().id(id).name("기존이름").depth(0).build();

        when(categoryJpaRepository.findWithChildrenById(id)).thenReturn(Optional.of(category));
        when(categoryJpaRepository.findById(id)).thenReturn(Optional.of(category));

        // when, then
        assertThatThrownBy(() -> categoryService.updateCategory(id, dto))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", CATEGORY_SELF_PARENT);
    }

    @Test
    @DisplayName("카테고리 삭제 성공")
    void 카테고리_삭제_성공() {
        // given
        Long id = 1L;
        Category category = mock(Category.class);
        when(categoryJpaRepository.findWithChildrenById(id)).thenReturn(Optional.of(category));

        // when
        categoryService.deleteCategory(id);

        // then
        verify(categoryJpaRepository).delete(category);
    }
}