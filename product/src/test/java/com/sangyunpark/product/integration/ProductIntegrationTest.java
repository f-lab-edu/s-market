package com.sangyunpark.product.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sangyunpark.product.domain.entity.Category;
import com.sangyunpark.product.infrastructure.repository.CategoryJpaRepository;
import com.sangyunpark.product.infrastructure.repository.ProductJpaRepository;
import com.sangyunpark.product.presentation.dto.request.ProductRequestDto;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@SuppressWarnings("NonAsciiCharacters")
class ProductIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CategoryJpaRepository categoryJpaRepository;

    @Autowired
    private ProductJpaRepository productJpaRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Long categoryId;

    @BeforeEach
    void setUp() {
        Category saved = categoryJpaRepository.save(Category.builder()
                .name("전자기기")
                .build());

        categoryId = saved.getId();
    }

    @Test
    @DisplayName("상품 생성 및 단건 조회 통합 테스트")
    void 상품_생성_및_조회_성공() throws Exception {
        // given
        LocalDateTime now = LocalDateTime.now().withNano(0); // JSON 비교 시 미세 시간 차이 방지
        ProductRequestDto request = new ProductRequestDto(
                categoryId,
                "노트북",
                "고성능 노트북입니다.",
                1500000L,
                true,
                now,
                now.plusDays(30)
        );

        // when
        String response = mockMvc.perform(post("/api/v1/admin/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long savedId = Long.parseLong(response);

        // then
        mockMvc.perform(get("/api/v1/admin/products/" + savedId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedId))
                .andExpect(jsonPath("$.categoryId").value(categoryId))
                .andExpect(jsonPath("$.title").value("노트북"))
                .andExpect(jsonPath("$.description").value("고성능 노트북입니다."))
                .andExpect(jsonPath("$.price").value(1500000))
                .andExpect(jsonPath("$.visible").value(true))
                .andExpect(jsonPath("$.startAt").value(now.toString()))
                .andExpect(jsonPath("$.endAt").value(now.plusDays(30).toString()))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.updatedAt").exists());
    }

    @Test
    @DisplayName("상품 삭제 후 조회 시 404 응답을 반환한다")
    void 상품_삭제_후_조회실패_확인() throws Exception {

        ProductRequestDto requestDto = new ProductRequestDto(
                categoryId,
                "노트북",
                "고성능 노트북입니다",
                1500000L,
                true,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(10)
        );

        String response = mockMvc.perform(post("/api/v1/admin/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long productId = Long.parseLong(response);

        // when
        mockMvc.perform(delete("/api/v1/admin/products/{id}", productId))
                .andExpect(status().isOk());

        // then
        mockMvc.perform(get("/api/v1/admin/products/{id}", productId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("PRODUCT_NOT_FOUND"));
    }

    @Test
    @DisplayName("카테고리가 존재하지 않는 경우 상품 등록 실패")
    void 상품_등록_실패_카테고리없음() throws Exception {
        // given
        ProductRequestDto dto = new ProductRequestDto(
                9999L, "상품명", "설명", 1000L, true,
                LocalDateTime.now(), LocalDateTime.now()
        );

        // when & then
        mockMvc.perform(post("/api/v1/admin/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("CATEGORY_NOT_FOUND"));
    }

    @Test
    @DisplayName("존재하지 않는 상품 조회 시 404 응답")
    void 상품_조회_실패_존재하지않음() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/admin/products/{id}", 99999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("PRODUCT_NOT_FOUND"));
    }

}