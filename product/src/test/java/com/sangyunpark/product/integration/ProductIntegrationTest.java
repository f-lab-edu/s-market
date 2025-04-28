package com.sangyunpark.product.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sangyunpark.product.domain.entity.Category;
import com.sangyunpark.product.infrastructure.repository.CategoryJpaRepository;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
                .andExpect(jsonPath("$.startAt").value(now.toString()))
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

    @Test
    @DisplayName("상품 수정 후 변경된 내용이 조회되어야 한다")
    void 상품_수정_성공_확인() throws Exception {
        LocalDateTime now = LocalDateTime.now();
        ProductRequestDto request = new ProductRequestDto(
                categoryId, "노트북", "기존 설명", 1500000L, true, now, now.plusDays(1)
        );

        String response = mockMvc.perform(post("/api/v1/admin/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Long productId = Long.parseLong(response);

        ProductRequestDto update = new ProductRequestDto(
                categoryId, "게이밍 노트북", "변경된 설명", 1800000L, false, now, now.plusDays(10)
        );

        mockMvc.perform(put("/api/v1/admin/products/" + productId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/admin/products/" + productId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("게이밍 노트북"))
                .andExpect(jsonPath("$.description").value("변경된 설명"))
                .andExpect(jsonPath("$.price").value(1800000))
                .andExpect(jsonPath("$.visible").value(false));
    }

    @Test
    @DisplayName("커서 기반 조회 - 첫 페이지 조회 성공")
    void 커서_기반_첫_페이지_조회() throws Exception {
        LocalDateTime now = LocalDateTime.now();

        for (int i = 1; i <= 5; i++) {
            ProductRequestDto dto = new ProductRequestDto(
                    categoryId,
                    "상품 " + i,
                    "설명 " + i,
                    1000L + i,
                    true,
                    now.minusDays(1),
                    now.plusDays(1)
            );
            mockMvc.perform(post("/api/v1/admin/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isOk());
        }

        mockMvc.perform(get("/api/v1/admin/products")
                        .param("size", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(3))
                .andExpect(jsonPath("$.nextCursor").exists())
                .andExpect(jsonPath("$.nextId").exists());
    }

    @Test
    @DisplayName("페이지 기반 필터 조회 - 카테고리 조건 적용")
    void 페이지_기반_필터_조회() throws Exception {
        LocalDateTime now = LocalDateTime.now();

        for (int i = 1; i <= 8; i++) {
            ProductRequestDto dto = new ProductRequestDto(
                    categoryId,
                    "필터 상품 " + i,
                    "필터 설명 " + i,
                    1000L + i,
                    true,
                    now.minusDays(1),
                    now.plusDays(1)
            );
            mockMvc.perform(post("/api/v1/admin/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isOk());
        }

        mockMvc.perform(get("/api/v1/admin/products/search")
                        .param("categoryId", String.valueOf(categoryId))
                        .param("page", "0")
                        .param("size", "5")
                        .param("sortOptions", "LATEST")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(5))
                .andExpect(jsonPath("$.totalElements").value(8));
    }

    @Test
    @DisplayName("페이지 기반 조회 - 정렬 옵션(price_asc) 적용")
    void 페이지_기반_조회_정렬_오름차순() throws Exception {
        for (int i = 1; i <= 5; i++) {
            ProductRequestDto dto = new ProductRequestDto(
                    categoryId,
                    "상품 " + i,
                    "가격 테스트",
                    10000L + i * 10,
                    true,
                    LocalDateTime.now().minusDays(1),
                    LocalDateTime.now().plusDays(1)
            );
            mockMvc.perform(post("/api/v1/admin/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isOk());
        }

        mockMvc.perform(get("/api/v1/admin/products/search")
                        .param("sort", "PRICE_ASC")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].price").value(10010L));
    }

    @Test
    @DisplayName("페이지 기반 조회 - 가격 필터(minPrice, maxPrice) 적용")
    void 페이지_기반_가격_필터() throws Exception {
        for (int i = 1; i <= 5; i++) {
            ProductRequestDto dto = new ProductRequestDto(
                    categoryId,
                    "가격 필터 상품 " + i,
                    "가격 조건",
                    10000L + i * 1000,
                    true,
                    LocalDateTime.now().minusDays(1),
                    LocalDateTime.now().plusDays(1)
            );
            mockMvc.perform(post("/api/v1/admin/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isOk());
        }

        mockMvc.perform(get("/api/v1/admin/products/search")
                        .param("minPrice", "11000")
                        .param("maxPrice", "13000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(3));
    }

    @Test
    @DisplayName("페이지 기반 조회 - 키워드 필터(keyword) 적용")
    void 페이지_기반_키워드_필터() throws Exception {
        for (int i = 1; i <= 3; i++) {
            ProductRequestDto dto = new ProductRequestDto(
                    categoryId,
                    "노트북 " + i,
                    "전자제품",
                    10000L,
                    true,
                    LocalDateTime.now().minusDays(1),
                    LocalDateTime.now().plusDays(1)
            );
            mockMvc.perform(post("/api/v1/admin/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isOk());
        }

        mockMvc.perform(get("/api/v1/admin/products/search")
                        .param("keyword", "노트북"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(3))
                .andExpect(jsonPath("$.content[0].title").value(org.hamcrest.Matchers.containsString("노트북")));
    }

    @Test
    @DisplayName("비가시 상품은 커서 기반 조회에서 제외되어야 한다")
    void 커서_기반_조회_비가시_상품_제외() throws Exception {
        // given
        ProductRequestDto hiddenDto = new ProductRequestDto(
                categoryId, "숨김 상품", "비공개", 1000L, false,
                LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(1)
        );

        mockMvc.perform(post("/api/v1/admin/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(hiddenDto)))
                .andExpect(status().isOk());

        // when & then
        mockMvc.perform(get("/api/v1/admin/products")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].title").value(org.hamcrest.Matchers.not(org.hamcrest.Matchers.hasItem("숨김 상품"))));
    }

    @Test
    @DisplayName("비가시 상품은 필터 기반 조회에서도 제외되어야 한다")
    void 필터_기반_조회_비가시_상품_제외() throws Exception {
        // given
        ProductRequestDto hiddenDto = new ProductRequestDto(
                categoryId, "숨김 상품", "비공개", 1000L, false,
                LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(1)
        );

        mockMvc.perform(post("/api/v1/admin/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(hiddenDto)))
                .andExpect(status().isOk());

        // when & then
        mockMvc.perform(get("/api/v1/admin/products/search")
                        .param("keyword", "숨김"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(0));
    }
}