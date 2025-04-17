package com.sangyunpark.product.application;

import com.sangyunpark.product.domain.entity.Category;
import com.sangyunpark.product.domain.mapper.CategoryMapper;
import com.sangyunpark.product.exception.BusinessException;
import com.sangyunpark.product.infrastructure.repository.CategoryJpaRepository;
import com.sangyunpark.product.presentation.dto.request.CategoryRequestDto;
import com.sangyunpark.product.presentation.dto.response.CategoryResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.sangyunpark.product.constant.ErrorCode.CATEGORY_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryJpaRepository categoryJpaRepository;
    private final CategoryMapper categoryMapper;

    public List<CategoryResponseDto> findAllCategories() {
        return categoryMapper.toDtoList(categoryJpaRepository.findAll());
    }

    public CategoryResponseDto findCategoryDtoById(final Long id) {
        Category category = categoryJpaRepository.findById(id).orElseThrow(() -> new BusinessException(CATEGORY_NOT_FOUND));
        return categoryMapper.toDto(category);
    }

    public Category findCategoryById(final Long id) {
        return categoryJpaRepository.findById(id).orElseThrow(() -> new BusinessException(CATEGORY_NOT_FOUND));
    }

    @Transactional
    public Long createCategory(final CategoryRequestDto dto) {
        final Long parentId = dto.parentId();
        final Category parentCategory = categoryJpaRepository.findById(parentId).orElseThrow(() -> new BusinessException(CATEGORY_NOT_FOUND));
        return categoryJpaRepository.save(categoryMapper.toEntity(dto, parentCategory)).getId();
    }

    @Transactional
    public void updateCategory(final Long id, final CategoryRequestDto dto) {
        final Category category = categoryJpaRepository.findById(id)
                .orElseThrow(() -> new BusinessException(CATEGORY_NOT_FOUND));

        Category parentCategory = null;
        if (dto.parentId() != null) {
            parentCategory = categoryJpaRepository.findById(dto.parentId())
                    .orElseThrow(() -> new BusinessException(CATEGORY_NOT_FOUND));
        }

        category.update(dto, parentCategory);
    }

    @Transactional
    public void deleteCategory(final Long id) {
        Category category = categoryJpaRepository.findById(id).orElseThrow(() -> new BusinessException(CATEGORY_NOT_FOUND));
        category.getChildren().clear();
        categoryJpaRepository.delete(category);
    }
}
