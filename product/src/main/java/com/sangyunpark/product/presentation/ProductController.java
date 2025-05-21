package com.sangyunpark.product.presentation;

import com.sangyunpark.product.application.ProductService;
import com.sangyunpark.product.constant.SortOption;
import com.sangyunpark.product.infrastructure.repository.condition.ProductFilterCondition;
import com.sangyunpark.product.presentation.dto.ProductDto;
import com.sangyunpark.product.presentation.dto.request.ProductCursorRequestDto;
import com.sangyunpark.product.presentation.dto.request.ProductRequestDto;
import com.sangyunpark.product.presentation.dto.response.ProductCursorResponseDto;
import com.sangyunpark.product.presentation.dto.response.ProductResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/products")
public class ProductController {

    private final ProductService productService;

    @PostMapping
    public Long create(@RequestBody final ProductRequestDto dto) {
        return productService.createProduct(dto);
    }

    @GetMapping
    public ProductCursorResponseDto<ProductDto> getPagedProducts(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) final LocalDateTime cursor,
            @RequestParam(required = false) final Long lastId,
            @RequestParam(defaultValue = "10") final int size
    ) {
        ProductCursorRequestDto request = new ProductCursorRequestDto(cursor, lastId, size);
        return productService.getPagedProducts(request);
    }

    @GetMapping("/search")
    public Page<ProductDto> searchProducts(
            @RequestParam(required = false) final Long categoryId,
            @RequestParam(required = false) final Long minPrice,
            @RequestParam(required = false) final Long maxPrice,
            @RequestParam(required = false) final String keyword,
            @RequestParam(required = false) final String sort,
            final Pageable pageable
    ) {

        ProductFilterCondition condition = new ProductFilterCondition(
                categoryId,
                minPrice,
                maxPrice,
                keyword,
                SortOption.from(sort)
        );
        return productService.getFilteredProducts(condition, pageable);
    }

    @GetMapping("/{id}")
    public ProductResponseDto findById(@PathVariable final Long id) {
        return productService.findProductDtoById(id);
    }

    @PutMapping("/{id}")
    public void update(@PathVariable final Long id, @RequestBody final ProductRequestDto dto) {
        productService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable final Long id) {
        productService.deleteById(id);
    }

    @PostMapping("/exists")
    Map<Long,Boolean> checkProductExists(@RequestBody List<Long> productsId) {
        return productService.checkExistence(productsId);
    }
}
