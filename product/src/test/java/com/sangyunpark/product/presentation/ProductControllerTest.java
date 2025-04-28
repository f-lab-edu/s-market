package com.sangyunpark.product.presentation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sangyunpark.product.application.ProductService;
import com.sangyunpark.product.presentation.dto.ProductDto;
import com.sangyunpark.product.presentation.dto.request.ProductRequestDto;
import com.sangyunpark.product.presentation.dto.response.ProductCursorResponseDto;
import com.sangyunpark.product.presentation.dto.response.ProductResponseDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

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

        when(productService.findProductDtoById(1L)).thenReturn(responseDto);

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

    @Test
    @DisplayName("커서 기반 상품 조회 성공")
    void 커서_기반_상품_조회_성공() throws Exception {
        ProductDto product1 = new ProductDto(1L, "상품1", 100L, 1000L, LocalDateTime.now(), "설명1");
        ProductDto product2 = new ProductDto(2L, "상품2", 100L, 2000L, LocalDateTime.now(), "설명2");

        ProductCursorResponseDto<ProductDto> response = new ProductCursorResponseDto<>(
                List.of(product1, product2),
                product2.createdAt(),
                product2.id()
        );

        when(productService.getPagedProducts(any())).thenReturn(response);

        mockMvc.perform(get("/api/v1/admin/products")
                        .param("cursor", LocalDateTime.now().toString())
                        .param("lastId", "2")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].id").value(1));
    }


    @Test
    @DisplayName("조건 기반 상품 검색 성공")
    void 조건_기반_상품_검색_성공() throws Exception {
        ProductDto product1 = new ProductDto(1L, "상품1", 100L, 1000L, LocalDateTime.now(), "설명1");
        ProductDto product2 = new ProductDto(2L, "상품2", 100L, 2000L, LocalDateTime.now(), "설명2");

        Page<ProductDto> page = new PageImpl<>(List.of(product1, product2));

        when(productService.getFilteredProducts(any(), any())).thenReturn(page);

        mockMvc.perform(get("/api/v1/admin/products/search")
                        .param("categoryId", "1")
                        .param("minPrice", "500000")
                        .param("maxPrice", "1500000")
                        .param("keyword", "노트북")
                        .param("sort", "price_desc")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].title").value("상품1"))
                .andExpect(jsonPath("$.content[1].title").value("상품2"));
    }
}