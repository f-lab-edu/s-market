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

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductJpaRepository productJpaRepository;
    private final ProductQueryRepository productQueryRepository;
    private final CategoryService categoryService;
    private final ProductMapper productMapper;

    public ProductResponseDto findProductDtoById(final Long id) {
        return productMapper.toDto(findProductById(id));
    }

    public Product findProductById(final Long id) {
        return productJpaRepository.findById(id).orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));
    }

    public ProductCursorResponseDto<ProductDto> getPagedProducts(final ProductCursorRequestDto request) {
        final LocalDateTime now = LocalDateTime.now();
        final List<ProductDto> content = productQueryRepository.findByCursor(request.cursor(), request.lastId(), request.size(), now);

        if (content.isEmpty()) {
            return new ProductCursorResponseDto<>(content, null, null);
        }

        ProductDto last = content.get(content.size() - 1);
        return new ProductCursorResponseDto<>(content, last.createdAt(), last.id());
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
        final Product product = findProductById(id);
        final Category category = categoryService.findCategoryById(dto.categoryId());

        product.updateTitle(dto.title());
        product.updateDescription(dto.description());
        product.updatePrice(dto.price());
        product.updateCategory(category);
        product.updateStartAt(dto.startAt());
        product.updateEndAt(dto.endAt());
        product.updateVisible(dto.visible());
        product.updateUpdatedAt(LocalDateTime.now());
    }

    @Transactional
    public void deleteById(final Long id) {
        final Product product = findProductById(id);
        productJpaRepository.delete(product);
    }
}