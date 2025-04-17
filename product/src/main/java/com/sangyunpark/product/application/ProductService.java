package com.sangyunpark.product.application;

import com.sangyunpark.product.constant.ErrorCode;
import com.sangyunpark.product.domain.entity.Category;
import com.sangyunpark.product.domain.entity.Product;
import com.sangyunpark.product.domain.mapper.ProductMapper;
import com.sangyunpark.product.exception.BusinessException;
import com.sangyunpark.product.infrastructure.repository.ProductJpaRepository;
import com.sangyunpark.product.presentation.dto.request.ProductRequestDto;
import com.sangyunpark.product.presentation.dto.response.ProductResponseDto;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductJpaRepository productJpaRepository;
    private final CategoryService categoryService;
    private final ProductMapper productMapper;

    public ProductResponseDto findById(final Long id) {
        return productMapper.toDto(productJpaRepository.findById(id).orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND)));
    }

    @Transactional
    public Long createProduct(final ProductRequestDto dto) {
        final Category category = categoryService.findCategoryById(dto.categoryId());
        return productJpaRepository.save(productMapper.toEntity(dto, category)).getId();
    }

    @Transactional
    public void update(final Long id, final ProductRequestDto dto) {
        Product product = productJpaRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));
        Category category = categoryService.findCategoryById(dto.categoryId());
        product.update(dto, category);
    }

    @Transactional
    public void deleteById(final Long id) {
        if(!productJpaRepository.existsById(id)) {
            throw new BusinessException(ErrorCode.PRODUCT_NOT_FOUND);
        }
        productJpaRepository.deleteById(id);
    }
}
