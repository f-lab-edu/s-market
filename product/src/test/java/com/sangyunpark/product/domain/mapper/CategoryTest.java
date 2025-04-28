package com.sangyunpark.product.domain.mapper;

import com.sangyunpark.product.application.CategoryService;
import com.sangyunpark.product.domain.entity.Category;
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
    void findAllCategories_success() {
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
    void findCategoryDtoById_success() {
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
    @DisplayName("카테고리 조회 실패 - 존재하지 않는 ID")
    void findCategoryById_fail() {
        // given
        Long id = 1L;
        when(categoryJpaRepository.findById(id)).thenReturn(Optional.empty());

        // when, then
        assertThatThrownBy(() -> categoryService.findCategoryById(id))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", CATEGORY_NOT_FOUND);
    }

    @Test
    @DisplayName("카테고리 생성 성공 - 부모 없음")
    void createCategory_success_noParent() {
        // given
        CategoryRequestDto dto = new CategoryRequestDto("전자제품", null);
        Category savedCategory = Category.builder()
                .id(1L)
                .name("전자제품")
                .depth(0)
                .build();

        when(categoryJpaRepository.save(any(Category.class))).thenReturn(savedCategory);

        // when
        Long result = categoryService.createCategory(dto);

        // then
        assertThat(result).isEqualTo(savedCategory.getId());
        verify(categoryJpaRepository).save(any(Category.class));
    }

    @Test
    @DisplayName("카테고리 생성 성공 - 부모 있음")
    void createCategory_success_withParent() {
        // given
        Category parent = Category.builder().id(1L).name("부모").depth(0).build();
        CategoryRequestDto dto = new CategoryRequestDto("자식", parent.getId());

        when(categoryJpaRepository.findById(parent.getId())).thenReturn(Optional.of(parent));
        when(categoryJpaRepository.save(any(Category.class)))
                .thenReturn(Category.builder()
                        .id(2L)
                        .name("자식")
                        .parent(parent)
                        .depth(1)
                        .build());

        // when
        Long result = categoryService.createCategory(dto);

        // then
        assertThat(result).isNotNull();
        verify(categoryJpaRepository).save(any(Category.class));
        verify(categoryJpaRepository).findById(parent.getId());
    }

    @Test
    @DisplayName("카테고리 수정 성공 - 이름 변경만")
    void updateCategory_success_changeNameOnly() {
        // given
        Long id = 1L;
        Category category = Category.builder().id(id).name("기존이름").depth(0).build();
        CategoryRequestDto dto = new CategoryRequestDto("새이름", null);

        when(categoryJpaRepository.findById(id)).thenReturn(Optional.of(category));

        // when
        categoryService.updateCategory(id, dto);

        // then
        assertThat(category.getName()).isEqualTo("새이름");
    }

    @Test
    @DisplayName("카테고리 수정 성공 - 부모 변경")
    void updateCategory_success_changeParent() {
        // given
        Long id = 1L;
        Long newParentId = 2L;
        Category category = Category.builder().id(id).name("카테고리").depth(0).build();
        Category newParent = Category.builder().id(newParentId).name("새부모").depth(0).build();
        CategoryRequestDto dto = new CategoryRequestDto("카테고리", newParentId);

        when(categoryJpaRepository.findById(id)).thenReturn(Optional.of(category));
        when(categoryJpaRepository.findById(newParentId)).thenReturn(Optional.of(newParent));

        // when
        categoryService.updateCategory(id, dto);

        // then
        assertThat(category.getParent()).isEqualTo(newParent);
        assertThat(category.getDepth()).isEqualTo(newParent.getDepth() + 1);
    }

    @Test
    @DisplayName("카테고리 수정 실패 - 순환 참조")
    void updateCategory_fail_circularReference() {
        // given
        Long id = 1L;
        Category category = Category.builder().id(id).name("카테고리").depth(0).build();
        CategoryRequestDto dto = new CategoryRequestDto("카테고리", id); // 자기 자신을 부모로 설정

        when(categoryJpaRepository.findById(id)).thenReturn(Optional.of(category));

        // when, then
        assertThatThrownBy(() -> categoryService.updateCategory(id, dto))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", CATEGORY_SELF_PARENT);
    }

    @Test
    @DisplayName("카테고리 삭제 성공")
    void deleteCategory_success() {
        // given
        Long id = 1L;
        Category category = mock(Category.class);
        when(categoryJpaRepository.findById(id)).thenReturn(Optional.of(category));

        // when
        categoryService.deleteCategory(id);

        // then
        verify(category).getChildren();
        verify(categoryJpaRepository).delete(category);
    }
}