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

import static com.sangyunpark.product.constant.ErrorCode.*;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryJpaRepository categoryJpaRepository;
    private final CategoryMapper categoryMapper;

    private static final int MAX_DEPTH = 3;

    public List<CategoryResponseDto> findAllCategories() {
        return categoryMapper.toDtoList(categoryJpaRepository.findAll());
    }

    public CategoryResponseDto findCategoryDtoById(final Long id) {
        return categoryMapper.toDto(findCategoryById(id));
    }

    public Category findCategoryById(final Long id) {
        return categoryJpaRepository.findById(id)
                .orElseThrow(() -> new BusinessException(CATEGORY_NOT_FOUND));
    }

    public Category findCategoryWithChildrenById(final Long id) {
        return categoryJpaRepository.findWithChildrenById(id)
                .orElseThrow(() -> new BusinessException(CATEGORY_NOT_FOUND));
    }

    @Transactional
    public Long createCategory(final CategoryRequestDto dto) {
        final Category parent = findParentIfPresent(dto.parentId());
        final int newDepth = calculateDepth(parent);
        validateDepth(newDepth);

        final Category newCategory = Category.builder()
                .name(dto.name())
                .depth(newDepth)  // 여기 newDepth 사용
                .build();

        if (parent != null) {
            parent.addChild(newCategory);
        }

        return categoryJpaRepository.save(newCategory).getId();
    }

    @Transactional
    public void updateCategory(final Long id, final CategoryRequestDto dto) {
        final Category category = findCategoryWithChildrenById(id);
        category.updateName(dto.name());

        if (dto.parentId() == null) {
            return;
        }

        final Category newParent = findCategoryById(dto.parentId());
        validateCircularReference(category, newParent);

        final int newDepth = calculateDepth(newParent);
        validateDepth(newDepth);

        category.updateParent(newParent);
        category.updateDepth(newDepth);
        updateChildrenDepthRecursively(category);
    }

    @Transactional
    public void deleteCategory(final Long id) {
        final Category category = findCategoryWithChildrenById(id);
        categoryJpaRepository.delete(category);
    }

    private Category findParentIfPresent(final Long parentId) {
        return parentId == null ? null : findCategoryById(parentId);
    }

    private void validateDepth(final int depth) {
        if (depth > MAX_DEPTH) {
            throw new BusinessException(CATEGORY_DEPTH_EXCEEDED);
        }
    }

    private void validateCircularReference(final Category category, final Category newParent) {
        Category current = newParent;
        while (current != null) {
            if (current.equals(category)) {
                throw new BusinessException(CATEGORY_SELF_PARENT);
            }
            current = current.getParent();
        }
    }

    private void updateChildrenDepthRecursively(final Category parent) {
        for (Category child : parent.getChildren()) {
            child.updateDepth(parent.getDepth() + 1);
            updateChildrenDepthRecursively(child);
        }
    }

    private int calculateDepth(final Category parent) {
        return parent == null ? 0 : parent.getDepth() + 1;
    }
}