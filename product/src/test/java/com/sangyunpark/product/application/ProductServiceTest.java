package com.sangyunpark.product.application;

import com.sangyunpark.product.constant.ErrorCode;
import com.sangyunpark.product.domain.entity.Category;
import com.sangyunpark.product.domain.entity.Product;
import com.sangyunpark.product.domain.mapper.ProductMapper;
import com.sangyunpark.product.exception.BusinessException;
import com.sangyunpark.product.infrastructure.repository.ProductJpaRepository;
import com.sangyunpark.product.presentation.dto.request.ProductRequestDto;
import com.sangyunpark.product.presentation.dto.response.ProductResponseDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
        final ProductResponseDto result = productService.findById(id);

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
        when(categoryService.findCategoryById(dto.categoryId())).thenReturn(category);

        // when
        productService.update(id, dto);

        // then
        verify(product).update(dto, category);
    }

    @Test
    @DisplayName("존재하지 않는 상품 수정 실패")
    void 존재하지_않는_상품_수정_실패() {
        // given
        final Long id = 1L;
        final ProductRequestDto dto = mock(ProductRequestDto.class);

        // when
        when(productJpaRepository.findById(id)).thenThrow(new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));

        // then
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
        when(productJpaRepository.existsById(id)).thenReturn(true);

        // when
        productService.deleteById(id);

        // then
        verify(productJpaRepository).deleteById(id);
    }

    @Test
    @DisplayName("존재하지 않는 상품 삭제시 실패")
    void 존재하지_않는_상품_삭제시_실패() {
        // given
        Long id = 999L;
        when(productJpaRepository.existsById(id)).thenReturn(false);

        // when, then
        assertThatThrownBy(() -> productService.deleteById(id))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.PRODUCT_NOT_FOUND);
    }
}