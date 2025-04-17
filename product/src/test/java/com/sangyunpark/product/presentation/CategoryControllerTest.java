package com.sangyunpark.product.presentation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sangyunpark.product.application.CategoryService;
import com.sangyunpark.product.presentation.dto.request.CategoryRequestDto;
import com.sangyunpark.product.presentation.dto.response.CategoryResponseDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SuppressWarnings("NonAsciiCharacters")
@WebMvcTest(CategoryController.class)
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CategoryService categoryService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("카테고리 생성 성공")
    void 카테고리_생성_성공() throws Exception {
        CategoryRequestDto dto = new CategoryRequestDto("디지털", 1L);
        when(categoryService.createCategory(any())).thenReturn(1L);

        mockMvc.perform(post("/api/v1/admin/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(content().string("1"));
    }

    @Test
    @DisplayName("카테고리 단건 조회 성공")
    void 카테고리_단건_조회_성공() throws Exception {
        CategoryResponseDto responseDto = new CategoryResponseDto(1L, "디지털", null);
        when(categoryService.findCategoryDtoById(1L)).thenReturn(responseDto);

        mockMvc.perform(get("/api/v1/admin/categories/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("디지털"));
    }

    @Test
    @DisplayName("카테고리 전체 조회 성공")
    void 카테고리_전체_조회_성공() throws Exception {
        List<CategoryResponseDto> list = List.of(
                new CategoryResponseDto(1L, "디지털", null),
                new CategoryResponseDto(2L, "가전", null)
        );

        when(categoryService.findAllCategories()).thenReturn(list);

        mockMvc.perform(get("/api/v1/admin/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @DisplayName("카테고리 수정 성공")
    void 카테고리_수정_성공() throws Exception {
        CategoryRequestDto dto = new CategoryRequestDto("가전",1L);
        doNothing().when(categoryService).updateCategory(any(Long.class), any());

        mockMvc.perform(put("/api/v1/admin/categories/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("카테고리 삭제 성공")
    void 카테고리_삭제_성공() throws Exception {
        doNothing().when(categoryService).deleteCategory(1L);

        mockMvc.perform(delete("/api/v1/admin/categories/1"))
                .andExpect(status().isOk());
    }
}