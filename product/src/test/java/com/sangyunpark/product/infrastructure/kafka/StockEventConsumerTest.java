package com.sangyunpark.product.infrastructure.kafka;

import com.sangyunpark.product.application.event.StockDeductedEvent;
import com.sangyunpark.product.constant.ErrorCode;
import com.sangyunpark.product.exception.BusinessException;
import com.sangyunpark.product.infrastructure.redis.OrderDuplicationRepository;
import com.sangyunpark.product.infrastructure.repository.StockJpaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StockEventConsumerTest {

    @Mock
    private StockJpaRepository stockJpaRepository;

    @InjectMocks
    private StockEventConsumer stockEventConsumer;

    @Test
    @DisplayName("재고 차감 성공 시 예외가 발생하지 않는다")
    void consumeStockDeductedEvent_성공() {
        // given
        StockDeductedEvent event = new StockDeductedEvent(1L, 5L, 100L);

        when(stockJpaRepository.decreaseStock(5L, 100L)).thenReturn(1); // 업데이트 성공

        // when & then
        assertThatCode(() -> stockEventConsumer.consumeStockDeductedEvent(event))
                .doesNotThrowAnyException();

        verify(stockJpaRepository).decreaseStock(event.productId(), event.quantity());
    }

    @Test
    @DisplayName("재고가 부족하여 차감에 실패하면 예외를 던진다")
    void consumeStockDeductedEvent_재고부족_예외() {
        // given
        StockDeductedEvent event = new StockDeductedEvent(2L, 10L, 101L);

        when(stockJpaRepository.decreaseStock(10L, 101L)).thenReturn(0); // 재고 부족

        // when & then
        assertThatThrownBy(() -> stockEventConsumer.consumeStockDeductedEvent(event))
                .isInstanceOf(BusinessException.class)
                        .extracting("errorCode").isEqualTo(ErrorCode.STOCK_NOT_ENOUGH);

        verify(stockJpaRepository).decreaseStock(10L, 101L);
    }
}