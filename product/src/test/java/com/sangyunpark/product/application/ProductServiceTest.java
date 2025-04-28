package com.sangyunpark.product.application;

import com.sangyunpark.product.constant.ErrorCode;
import com.sangyunpark.product.constant.SortOption;
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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("NonAsciiCharacters")
class ProductServiceTest {

    @InjectMocks
    private ProductService productService;

    @Mock
    private ProductJpaRepository productJpaRepository;

    @Mock
    private CategoryService categoryService;

    @Mock
    private ProductMapper productMapper;

    @Mock
    private ProductQueryRepository productQueryRepository;

    @Test
    @DisplayName("상품 조회 성공")
    void 상품_조회_성공() {
        // given
        final Long id = 1L;
        final Product product = mock(Product.class);
        final ProductResponseDto responseDto = mock(ProductResponseDto.class);

        when(productJpaRepository.findById(id)).thenReturn(Optional.of(product));
        when(productMapper.toDto(product)).thenReturn(responseDto);

        // when
        final ProductResponseDto result = productService.findProductDtoById(id);

        // then
        assertThat(result).isEqualTo(responseDto);
    }

    @Test
    @DisplayName("상품 생성 성공")
    void 상품_생성_성공() {
        // given
        final ProductRequestDto dto = mock(ProductRequestDto.class);
        final Product product = mock(Product.class);
        final Category category = mock(Category.class);

        when(dto.categoryId()).thenReturn(1L);
        when(categoryService.findCategoryById(1L)).thenReturn(category);

        when(productMapper.toEntity(dto, category)).thenReturn(product);
        when(productJpaRepository.save(product)).thenReturn(product);
        when(product.getId()).thenReturn(100L);

        // when
        final Long result = productService.createProduct(dto);

        // then
        assertThat(result).isEqualTo(100L);
    }

    @Test
    @DisplayName("상품 수정 성공")
    void 상품_수정_성공() {
        // given
        final Long id = 1L;
        final ProductRequestDto dto = mock(ProductRequestDto.class);
        final Product product = mock(Product.class);
        final Category category = mock(Category.class);

        when(productJpaRepository.findById(id)).thenReturn(Optional.of(product));
        when(dto.categoryId()).thenReturn(10L);
        when(categoryService.findCategoryById(10L)).thenReturn(category);

        when(dto.title()).thenReturn("New Title");
        when(dto.description()).thenReturn("New Description");
        when(dto.price()).thenReturn(2000L);
        when(dto.visible()).thenReturn(false);
        when(dto.startAt()).thenReturn(LocalDateTime.now().plusDays(1));
        when(dto.endAt()).thenReturn(LocalDateTime.now().plusDays(2));

        // when
        productService.update(id, dto);

        // then
        verify(product).updateTitle("New Title");
        verify(product).updateDescription("New Description");
        verify(product).updatePrice(2000L);
        verify(product).updateCategory(category);
        verify(product).updateVisible(false);
        verify(product).updateStartAt(dto.startAt());
        verify(product).updateEndAt(dto.endAt());
        verify(product).updateUpdatedAt(any(LocalDateTime.class));
    }

    @Test
    @DisplayName("존재하지 않는 상품 수정 실패")
    void 존재하지_않는_상품_수정_실패() {
        // given
        final Long id = 1L;
        final ProductRequestDto dto = mock(ProductRequestDto.class);

        // when, then
        assertThatThrownBy(() -> productService.update(id, dto))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.PRODUCT_NOT_FOUND);
    }

    @Test
    @DisplayName("상품 삭제 성공")
    void 상품_삭제_성공() {
        // given
        final Long id = 1L;
        final Product product = mock(Product.class);

        when(productJpaRepository.findById(id)).thenReturn(Optional.of(product));

        // when
        productService.deleteById(id);

        // then
        verify(productJpaRepository).delete(product);
    }

    @Test
    @DisplayName("존재하지 않는 상품 삭제시 실패")
    void 존재하지_않는_상품_삭제시_실패() {
        // given
        Long id = 999L;
        when(productJpaRepository.findById(id)).thenReturn(Optional.empty());

        // when, then
        assertThatThrownBy(() -> productService.deleteById(id))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.PRODUCT_NOT_FOUND);
    }


    @Test
    @DisplayName("커서 기반 상품 페이지 조회 성공")
    void 커서_기반_상품_조회_성공() {
        // given
        LocalDateTime now = LocalDateTime.now();
        ProductCursorRequestDto request = new ProductCursorRequestDto(null, null, 3);
        List<ProductDto> mockResult = List.of(
                new ProductDto(1L, "상품1", 1000L,10000L, now.minusMinutes(3), "설명"),
                new ProductDto(2L, "상품2", 2000L, 10000L, now.minusMinutes(2), "설명"),
                new ProductDto(3L, "상품3", 3000L, 10000L, now.minusMinutes(1), "설명")
        );

        when(productQueryRepository.findByCursor(isNull(), isNull(), eq(3), any(LocalDateTime.class))).thenReturn(mockResult);

        // when
        ProductCursorResponseDto<ProductDto> result = productService.getPagedProducts(request);

        // then
        assertThat(result.content()).hasSize(3);
        assertThat(result.nextCursor()).isEqualTo(mockResult.get(2).createdAt());
        assertThat(result.nextId()).isEqualTo(mockResult.get(2).id());
    }
    @Test
    @DisplayName("필터 조건 기반 상품 조회 성공")
    void 필터_조건_기반_상품_조회_성공() {
        // given
        ProductFilterCondition condition = new ProductFilterCondition(1L, 1000L, 5000L, "노트북", SortOption.PRICE_DESC);
        Pageable pageable = PageRequest.of(0, 10);

        List<ProductDto> content = List.of(
                new ProductDto(1L, "노트북 1", 4500L, 10000L, LocalDateTime.now(), "고성능 노트북"),
                new ProductDto(2L, "노트북 2", 3000L, 10000L, LocalDateTime.now(), "휴대용 노트북")
        );
        Page<ProductDto> mockPage = new PageImpl<>(content, pageable, content.size());

        when(productQueryRepository.searchByFilter(eq(condition), eq(pageable))).thenReturn(mockPage);

        // when
        Page<ProductDto> result = productService.getFilteredProducts(condition, pageable);

        // then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().get(0).title()).contains("노트북");
        assertThat(result.getTotalElements()).isEqualTo(2);

        verify(productQueryRepository).searchByFilter(eq(condition), eq(pageable));
    }

}