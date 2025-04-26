package com.sangyunpark.product.application;

import com.sangyunpark.product.constant.ErrorCode;
import com.sangyunpark.product.domain.entity.Category;
import com.sangyunpark.product.domain.entity.Product;
import com.sangyunpark.product.domain.mapper.ProductMapper;
import com.sangyunpark.product.exception.BusinessException;
import com.sangyunpark.product.infrastructure.repository.ProductJpaRepository;
import com.sangyunpark.product.infrastructure.repository.ProductQueryRepository;
import com.sangyunpark.product.infrastructure.repository.condition.ProductFilterCondition;
import com.sangyunpark.product.presentation.dto.ProductDto;
import com.sangyunpark.product.presentation.dto.request.ProductCursorRequestDto;
import com.sangyunpark.product.presentation.dto.request.ProductRequestDto;
import com.sangyunpark.product.presentation.dto.response.ProductCursorResponseDto;
import com.sangyunpark.product.presentation.dto.response.ProductResponseDto;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductJpaRepository productJpaRepository;
    private final ProductQueryRepository productQueryRepository;
    private final CategoryService categoryService;
    private final ProductMapper productMapper;

    public ProductResponseDto findById(final Long id) {
        return productMapper.toDto(productJpaRepository.findById(id).orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND)));
    }

    public ProductCursorResponseDto<ProductDto> getPagedProducts(final ProductCursorRequestDto request) {
        final LocalDateTime now = LocalDateTime.now();
        final List<ProductDto> content = productQueryRepository.findByCursor(request.cursor(), request.lastId(), request.size(), now);
        return Optional.of(content)
                .filter(list -> !list.isEmpty())
                .map(list -> {
                    ProductDto last = list.get(list.size() - 1);
                    return new ProductCursorResponseDto<>(content, last.createdAt(), last.id());
                })
                .orElseGet(() -> new ProductCursorResponseDto<>(content,null,null));
    }

    public Page<ProductDto> getFilteredProducts(final ProductFilterCondition condition, final Pageable pageable) {
        return productQueryRepository.searchByFilter(condition, pageable);
    }

    @Transactional
    public Long createProduct(final ProductRequestDto dto) {
        final Category category = categoryService.findCategoryById(dto.categoryId());
        return productJpaRepository.save(productMapper.toEntity(dto, category)).getId();
    }

    @Transactional
    public void update(final Long id, final ProductRequestDto dto) {
        final Product product = productJpaRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));
        final Category category = categoryService.findCategoryById(dto.categoryId());
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
