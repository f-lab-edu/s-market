package com.sangyunpark.product.presentation;

import com.sangyunpark.product.application.CategoryService;
import com.sangyunpark.product.presentation.dto.request.CategoryRequestDto;
import com.sangyunpark.product.presentation.dto.response.CategoryResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/categories")
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping
    public Long create(@RequestBody @Valid final CategoryRequestDto dto) {
        return categoryService.createCategory(dto);
    }

    @GetMapping("/{id}")
    public CategoryResponseDto findById(@PathVariable final Long id) {
        return categoryService.findCategoryDtoById(id);
    }

    @GetMapping
    public List<CategoryResponseDto> findAll() {
        return categoryService.findAllCategories();
    }

    @PutMapping("/{id}")
    public void update(@PathVariable final Long id, @RequestBody @Valid final CategoryRequestDto dto) {
        categoryService.updateCategory(id, dto);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable final Long id) {
        categoryService.deleteCategory(id);
    }
}
