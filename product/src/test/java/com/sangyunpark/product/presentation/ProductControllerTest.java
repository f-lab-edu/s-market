package com.sangyunpark.product.presentation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sangyunpark.product.application.ProductService;
import com.sangyunpark.product.presentation.dto.request.ProductRequestDto;
import com.sangyunpark.product.presentation.dto.response.ProductResponseDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SuppressWarnings("NonAsciiCharacters")
@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductService productService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("상품 생성 성공")
    void 상품_생성_성공() throws Exception {
        ProductRequestDto dto = new ProductRequestDto(
                1L, "상품명", "설명", 10000L, true,
                LocalDateTime.now(), LocalDateTime.now()
        );

        when(productService.createProduct(any())).thenReturn(1L);

        mockMvc.perform(post("/api/v1/admin/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(content().string("1"));
    }

    @Test
    @DisplayName("상품 조회 성공")
    void 상품_조회_성공() throws Exception {
        ProductResponseDto responseDto = new ProductResponseDto(
                1L, 1L, "상품명", "설명", 10000L, true,
                LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now()
        );

        when(productService.findById(1L)).thenReturn(responseDto);

        mockMvc.perform(get("/api/v1/admin/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("상품명"));
    }

    @Test
    @DisplayName("상품 수정 성공")
    void 상품_수정_성공() throws Exception {
        ProductRequestDto dto = new ProductRequestDto(
                1L, "수정된상품", "수정된설명", 20000L, false,
                LocalDateTime.now(), LocalDateTime.now()
        );

        doNothing().when(productService).update(eq(1L), any());

        mockMvc.perform(put("/api/v1/admin/products/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("상품 삭제 성공")
    void 상품_삭제_성공() throws Exception {
        doNothing().when(productService).deleteById(1L);

        mockMvc.perform(delete("/api/v1/admin/products/1"))
                .andExpect(status().isOk());
    }
}