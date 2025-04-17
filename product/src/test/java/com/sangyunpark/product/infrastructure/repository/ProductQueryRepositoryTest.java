package com.sangyunpark.product.infrastructure.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sangyunpark.product.constant.SortOption;
import com.sangyunpark.product.domain.entity.Category;
import com.sangyunpark.product.domain.entity.Product;
import com.sangyunpark.product.infrastructure.repository.condition.ProductFilterCondition;
import com.sangyunpark.product.presentation.dto.ProductDto;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("NonAsciiCharacters")
@Transactional
@DataJpaTest
class ProductQueryRepositoryTest {

    @Autowired
    private EntityManager em;

    private ProductQueryRepository productQueryRepository;

    @BeforeEach
    void setUp() {
        productQueryRepository = new ProductQueryRepository(new JPAQueryFactory(em));
        Category category = Category.builder().name("전자기기").build();
        em.persist(category);

        for (int i = 1; i <= 10; i++) {
            LocalDateTime now = LocalDateTime.now();
            Product p = Product.builder()
                    .title("상품 " + i)
                    .description("설명 " + i)
                    .price(10000L + i)
                    .visible(true)
                    .startAt(now.minusDays(1))
                    .endAt(now.plusDays(1))
                    .createdAt(now)
                    .updatedAt(now)
                    .category(category)
                    .build();
            em.persist(p);
        }

        em.flush();
        em.clear();
    }

    @Test
    @DisplayName("필터 조건 없이 전체 조회가 성공해야 한다")
    void 필터_조건_없이_전체_조회가_성공() {
        ProductFilterCondition cond = new ProductFilterCondition(null, null, null, null, SortOption.LATEST);
        PageRequest pageRequest = PageRequest.of(0, 5);

        Page<ProductDto> result = productQueryRepository.searchByFilter(cond, pageRequest);

        assertThat(result.getContent()).hasSize(5);
        assertThat(result.getTotalElements()).isEqualTo(10);
    }

    @Test
    @DisplayName("가격 필터로 조회가 성공해야 한다")
    void 가격_필터로_조회가_성공() {
        ProductFilterCondition cond = new ProductFilterCondition(null, 10005L, 10007L, null, SortOption.LATEST);
        PageRequest pageRequest = PageRequest.of(0, 10);

        Page<ProductDto> result = productQueryRepository.searchByFilter(cond, pageRequest);

        assertThat(result.getContent()).hasSize(3);
    }

    @Test
    @DisplayName("커서 기반 조회가 성공해야 한다")
    void 커서_기반_조회_성공() {
        List<ProductDto> firstPage = productQueryRepository.findByCursor(LocalDateTime.now(), Long.MAX_VALUE, 5, LocalDateTime.now());

        assertThat(firstPage).hasSize(5);

        ProductDto last = firstPage.get(4);
        List<ProductDto> secondPage = productQueryRepository.findByCursor(last.createdAt(), last.id(), 5, LocalDateTime.now());

        assertThat(secondPage).hasSize(5);
        assertThat(secondPage.get(0).id()).isLessThan(last.id());
    }

    @Test
    @DisplayName("상품이 없는 경우 빈 결과를 반환한다")
    void 상품이_없는_경우_빈_결과_반환() {
        // given
        em.createQuery("DELETE FROM Product").executeUpdate();
        ProductFilterCondition cond = new ProductFilterCondition(null, null, null, null, SortOption.LATEST);
        PageRequest pageRequest = PageRequest.of(0, 5);

        // when
        Page<ProductDto> result = productQueryRepository.searchByFilter(cond, pageRequest);

        // then
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();
    }

    @Test
    @DisplayName("카테고리 ID로 필터 조회가 성공해야 한다")
    void 카테고리_필터_조회() {
        Long categoryId = em.createQuery("SELECT c.id FROM Category c WHERE c.name = '전자기기'", Long.class)
                .getSingleResult();

        ProductFilterCondition cond = new ProductFilterCondition(categoryId, null, null, null, SortOption.LATEST);
        PageRequest pageRequest = PageRequest.of(0, 10);

        Page<ProductDto> result = productQueryRepository.searchByFilter(cond, pageRequest);

        assertThat(result.getTotalElements()).isEqualTo(10);
    }

    @Test
    @DisplayName("상품명 키워드로 조회가 성공해야 한다")
    void 키워드_검색_조회() {
        ProductFilterCondition cond = new ProductFilterCondition(null, null, null, "상품 1", SortOption.LATEST);
        PageRequest pageRequest = PageRequest.of(0, 10);

        Page<ProductDto> result = productQueryRepository.searchByFilter(cond, pageRequest);

        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent().get(0).title()).contains("상품 1");
    }

    @Test
    @DisplayName("마지막 페이지일 경우 적은 수의 데이터가 조회된다")
    void 마지막_페이지_조회() {
        ProductFilterCondition cond = new ProductFilterCondition(null, null, null, null, SortOption.LATEST);
        PageRequest pageRequest = PageRequest.of(1, 7);

        Page<ProductDto> result = productQueryRepository.searchByFilter(cond, pageRequest);

        assertThat(result.getContent()).hasSize(3);
    }

    @Test
    @DisplayName("동일한 createdAt을 가진 상품 커서 조건 조회된다.")
    void 동일한_createdAt_조건_조회() {
        LocalDateTime sameTime = LocalDateTime.of(2025, 4, 17, 12, 0);

        for (int i = 11; i <= 15; i++) {
            Product p = Product.builder()
                    .title("상품 " + i)
                    .description("동일시간 상품")
                    .price(10000L + i)
                    .visible(true)
                    .startAt(sameTime.minusDays(1))
                    .endAt(sameTime.plusDays(1))
                    .createdAt(sameTime)
                    .updatedAt(sameTime)
                    .category(em.createQuery("SELECT c FROM Category c", Category.class).getSingleResult())
                    .build();
            em.persist(p);
        }
        em.flush();
        em.clear();

        List<ProductDto> result = productQueryRepository.findByCursor(sameTime, Long.MAX_VALUE, 10, sameTime.plusSeconds(1));

        assertThat(result).allSatisfy(dto ->
                assertThat(dto.createdAt()).isAfterOrEqualTo(sameTime)
        );
    }

    @Test
    @DisplayName("조건에 맞는 상품이 없으면 빈 리스트가 반환")
    void 조건에_맞는_상품이_없을_때() {
        ProductFilterCondition cond = new ProductFilterCondition(null, 99999L, 100000L, "없는상품", SortOption.LATEST);
        PageRequest pageRequest = PageRequest.of(0, 5);

        Page<ProductDto> result = productQueryRepository.searchByFilter(cond, pageRequest);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("가격 오름차순 정렬이 잘 적용되어야 한다")
    void 가격_오름차순_정렬() {
        ProductFilterCondition cond = new ProductFilterCondition(null, null, null, null, SortOption.PRICE_ASC);
        PageRequest pageRequest = PageRequest.of(0, 10);

        Page<ProductDto> result = productQueryRepository.searchByFilter(cond, pageRequest);

        List<ProductDto> content = result.getContent();
        for (int i = 1; i < content.size(); i++) {
            assertThat(content.get(i).price()).isGreaterThan(content.get(i - 1).price());
        }
    }

    @Test
    @DisplayName("가격 내림차순 정렬이 잘 적용되어야 한다")
    void 가격_내림차순_정렬() {
        ProductFilterCondition cond = new ProductFilterCondition(null, null, null, null, SortOption.PRICE_DESC);
        PageRequest pageRequest = PageRequest.of(0, 10);

        Page<ProductDto> result = productQueryRepository.searchByFilter(cond, pageRequest);

        List<ProductDto> content = result.getContent();
        for (int i = 1; i < content.size(); i++) {
            assertThat(content.get(i).price()).isLessThan(content.get(i - 1).price());
        }
    }

    @Test
    @DisplayName("특정 카테고리 ID로 조회 시 해당 상품만 나와야 한다")
    void 카테고리_필터링_정확성_검증() {
        Long categoryId = em.createQuery("SELECT c.id FROM Category c", Long.class)
                .setMaxResults(1)
                .getSingleResult();

        ProductFilterCondition cond = new ProductFilterCondition(categoryId, null, null, null, SortOption.LATEST);
        PageRequest pageRequest = PageRequest.of(0, 5);

        Page<ProductDto> result = productQueryRepository.searchByFilter(cond, pageRequest);

        assertThat(result).allMatch(dto -> dto.categoryId().equals(categoryId));
    }


    @Test
    @DisplayName("커서가 없는 경우 최신순으로 첫 페이지가 나와야 한다")
    void 커서_없을_때_최신순_첫_페이지() {
        List<ProductDto> result = productQueryRepository.findByCursor(null, null, 5, LocalDateTime.now());

        assertThat(result).hasSize(5);
        for (int i = 1; i < result.size(); i++) {
            assertThat(result.get(i).createdAt()).isBeforeOrEqualTo(result.get(i - 1).createdAt());
        }
    }


    @Test
    @DisplayName("중간 상품 삭제 후에도 커서 페이징이 일관되게 동작해야 한다")
    void 상품_삭제_후_커서_페이징_일관성_검증() {

        List<ProductDto> firstPage = productQueryRepository.findByCursor(LocalDateTime.now(), Long.MAX_VALUE, 5, LocalDateTime.now());
        assertThat(firstPage).hasSize(5);

        ProductDto toDelete = firstPage.get(2);
        em.createQuery("DELETE FROM Product p WHERE p.id = :id")
                .setParameter("id", toDelete.id())
                .executeUpdate();
        em.flush();
        em.clear();

        ProductDto last = firstPage.get(4);
        List<ProductDto> secondPage = productQueryRepository.findByCursor(last.createdAt(), last.id(), 5, LocalDateTime.now());

        assertThat(secondPage).isNotEmpty();
        for (ProductDto dto : secondPage) {
            boolean isBefore = dto.createdAt().isBefore(last.createdAt());
            boolean isSameAndSmallerId = dto.createdAt().isEqual(last.createdAt()) && dto.id() < last.id();
            assertThat(isBefore || isSameAndSmallerId).isTrue();
        }
    }

    @Test
    @DisplayName("비가시 상품은 조회되지 않아야 한다")
    void 비가시_상품_제외_확인() {
        // given
        Product invisibleProduct = Product.builder()
                .title("숨김 상품")
                .description("노출되지 않아야 함")
                .price(9999L)
                .visible(false)
                .startAt(LocalDateTime.now().minusDays(1))
                .endAt(LocalDateTime.now().plusDays(1))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .category(em.createQuery("SELECT c FROM Category c", Category.class).getSingleResult())
                .build();
        em.persist(invisibleProduct);
        em.flush();
        em.clear();

        // when
        PageRequest pageRequest = PageRequest.of(0, 10);
        ProductFilterCondition cond = new ProductFilterCondition(null, null, null, null, SortOption.LATEST);
        Page<ProductDto> result = productQueryRepository.searchByFilter(cond, pageRequest);

        // then
        assertThat(result.getContent()).noneMatch(dto -> "숨김 상품".equals(dto.title()));
    }
}