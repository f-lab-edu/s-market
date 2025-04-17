package com.sangyunpark.product.presentation;

import com.sangyunpark.product.application.ProductService;
import com.sangyunpark.product.presentation.dto.request.ProductRequestDto;
import com.sangyunpark.product.presentation.dto.response.ProductResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/products")
public class ProductController {

    private final ProductService productService;

    @PostMapping
    public Long create(@RequestBody final ProductRequestDto dto) {
        return productService.createProduct(dto);
    }

    @GetMapping("/{id}")
    public ProductResponseDto findById(@PathVariable final Long id) {
        return productService.findById(id);
    }

    @PutMapping("/{id}")
    public void update(@PathVariable final Long id, @RequestBody final ProductRequestDto dto) {
        productService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable final Long id) {
        productService.deleteById(id);
    }
}
