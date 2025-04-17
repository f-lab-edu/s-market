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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.sangyunpark.product.constant.ErrorCode.CATEGORY_NOT_FOUND;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@SuppressWarnings("NonAsciiCharacters")
@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryJpaRepository categoryJpaRepository;

    @Mock
    private CategoryMapper categoryMapper;

    @InjectMocks
    private CategoryService categoryService;

    @Test
    @DisplayName("카테고리 전체 조회")
    void 카테고리_전체_조회() {
        List<Category> categories = Arrays.asList(new Category(), new Category());
        List<CategoryResponseDto> responseDtos = Arrays.asList(mock(CategoryResponseDto.class), mock(CategoryResponseDto.class));

        when(categoryJpaRepository.findAll()).thenReturn(categories);
        when(categoryMapper.toDtoList(categories)).thenReturn(responseDtos);

        List<CategoryResponseDto> result = categoryService.findAllCategories();
        assertThat(result).hasSize(2);
        verify(categoryJpaRepository).findAll();
    }

    @Test
    @DisplayName("ID로 카테고리 조회 성공")
    void ID로_카테고리_조회_성공() {
        Category category = new Category();
        CategoryResponseDto responseDto = mock(CategoryResponseDto.class);

        when(categoryJpaRepository.findById(1L)).thenReturn(Optional.of(category));
        when(categoryMapper.toDto(category)).thenReturn(responseDto);

        CategoryResponseDto result = categoryService.findCategoryDtoById(1L);
        assertThat(result).isEqualTo(responseDto);
    }

    @Test
    @DisplayName("ID로 카테고리 조회 실패")
    void ID로_카테고리_조회_실패() {
        when(categoryJpaRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.findCategoryDtoById(1L))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode").isEqualTo(CATEGORY_NOT_FOUND);
    }

    @Test
    @DisplayName("카테고리 생성 성공")
    void 카테고리_생성_성공() {
        CategoryRequestDto dto = mock(CategoryRequestDto.class);
        when(dto.parentId()).thenReturn(10L);
        Category parent = new Category();
        Category toSave = mock(Category.class);
        when(categoryJpaRepository.findById(10L)).thenReturn(Optional.of(parent));
        when(categoryMapper.toEntity(dto, parent)).thenReturn(toSave);
        when(categoryJpaRepository.save(toSave)).thenReturn(toSave);
        when(toSave.getId()).thenReturn(100L);

        Long result = categoryService.createCategory(dto);
        assertThat(result).isEqualTo(100L);
    }

    @Test
    @DisplayName("카테고리 수정 성공")
    void 카테고리_수정_성공() {
        CategoryRequestDto dto = mock(CategoryRequestDto.class);
        Category category = mock(Category.class);
        when(categoryJpaRepository.findById(1L)).thenReturn(Optional.of(category));
        when(dto.parentId()).thenReturn(null);

        categoryService.updateCategory(1L, dto);
        verify(category).update(dto, null);
    }

    @Test
    @DisplayName("카테고리 삭제 성공")
    void 카테고리_삭제_성공() {
        Category category = mock(Category.class);
        when(categoryJpaRepository.findById(1L)).thenReturn(Optional.of(category));
        when(category.getChildren()).thenReturn(Collections.emptyList());

        categoryService.deleteCategory(1L);

        verify(category).getChildren();
        verify(categoryJpaRepository).delete(category);
    }
}