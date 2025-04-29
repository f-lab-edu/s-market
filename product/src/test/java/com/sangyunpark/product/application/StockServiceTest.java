package com.sangyunpark.product.application;

import com.sangyunpark.product.application.event.StockDeductedEvent;
import com.sangyunpark.product.constant.ErrorCode;
import com.sangyunpark.product.exception.BusinessException;
import com.sangyunpark.product.infrastructure.kafka.StockEventProducer;
import com.sangyunpark.product.infrastructure.redis.StockRedisRepository;
import com.sangyunpark.product.infrastructure.repository.StockJpaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class StockServiceTest {

    @InjectMocks
    private StockService stockService;

    @Mock
    private StockJpaRepository stockJpaRepository;

    @Mock
    private StockRedisRepository stockRedisRepository;

    @Mock
    private StockEventProducer stockEventProducer;

    private final String ERROR_CODE = "errorCode";

    @Test
    @DisplayName("Redis 캐시 없음 → DB에서 조회 후 캐시에 저장 + 재고 차감 성공")
    void 캐시없음_정상차감() {
        // given
        Long productId = 1L, orderId = 100L, quantity = 2L;
        given(stockRedisRepository.isExisted(productId)).willReturn(false);
        given(stockJpaRepository.findQuantityByProductId(productId)).willReturn(Optional.of(10L));
        given(stockRedisRepository.decrease(productId, quantity)).willReturn(8L);

        // when
        stockService.decreaseStockAndPublish(productId, quantity, orderId);

        // then
        verify(stockRedisRepository).setIfAbsentWithTTL(eq(productId), eq(10L), any());
        verify(stockEventProducer).sendStockDeductedEvent(
                new StockDeductedEvent(orderId, productId, quantity)
        );
    }

    @Test
    @DisplayName("Redis 캐시 있음 → 재고 차감 정상")
    void 캐시있음_정상차감() {
        // given
        Long productId = 2L, orderId = 200L, quantity = 3L;
        given(stockRedisRepository.isExisted(productId)).willReturn(true);
        given(stockRedisRepository.decrease(productId, quantity)).willReturn(7L);

        // when
        stockService.decreaseStockAndPublish(productId, quantity, orderId);

        // then
        verify(stockRedisRepository, never()).setIfAbsentWithTTL(any(), any(), any());
        verify(stockEventProducer).sendStockDeductedEvent(
                new StockDeductedEvent(orderId, productId, quantity)
        );
    }

    @Test
    @DisplayName("재고 부족 시 예외 발생")
    void 재고부족_예외() {
        // given
        Long productId = 3L, orderId = 300L, quantity = 20L;
        given(stockRedisRepository.isExisted(productId)).willReturn(true);
        given(stockRedisRepository.decrease(productId, quantity)).willReturn(-1L);

        // expect
        assertThatThrownBy(() -> stockService.decreaseStockAndPublish(productId, quantity, orderId))
                .isInstanceOf(BusinessException.class)
                .extracting(ERROR_CODE).isEqualTo(ErrorCode.STOCK_NOT_ENOUGH);
    }

    @Test
    @DisplayName("상품이 존재하지 않으면 예외 발생")
    void 상품없음_예외() {
        // given
        Long productId = 99L, orderId = 400L, quantity = 1L;
        given(stockRedisRepository.isExisted(productId)).willReturn(false);
        given(stockJpaRepository.findQuantityByProductId(productId)).willReturn(Optional.empty());

        // expect
        assertThatThrownBy(() -> stockService.decreaseStockAndPublish(productId, quantity, orderId))
                .isInstanceOf(BusinessException.class)
                .extracting(ERROR_CODE).isEqualTo(ErrorCode.PRODUCT_NOT_FOUND);
    }
}