package com.sangyunpark.product.infrastructure.kafka;

import com.sangyunpark.product.infrastructure.kafka.event.StockDeductedEvent;
import com.sangyunpark.product.constant.ErrorCode;
import com.sangyunpark.product.constant.OutboxStatus;
import com.sangyunpark.product.exception.BusinessException;
import com.sangyunpark.product.infrastructure.redis.OrderDuplicationRepository;
import com.sangyunpark.product.infrastructure.repository.StockJpaRepository;
import com.sangyunpark.product.infrastructure.repository.StockOutboxRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StockEventConsumerTest {

    @Mock
    private StockJpaRepository stockJpaRepository;

    @Mock
    private OrderDuplicationRepository orderDuplicationRepository;

    @Mock
    private StockOutboxRepository stockOutboxRepository;

    @InjectMocks
    private StockEventConsumer stockEventConsumer;

    @Test
    @DisplayName("이미 처리된 주문이면 아무 동작도 하지 않는다")
    void consumeStockDeductedEvent_중복처리() {
        // given
        StockDeductedEvent event = new StockDeductedEvent(1L, 5L, 100L);

        // when
        stockEventConsumer.consumeStockDeductedEvent(event);

        // then
        verify(stockJpaRepository, never()).decreaseStock(5L, 5L);
        verify(stockOutboxRepository, never()).updateStatusByOrderId(
                eq(event.orderId()), eq(OutboxStatus.SEND), any()
        );
    }

    @Test
    @DisplayName("재고 차감 성공 시 상태를 SEND로 변경한다")
    void consumeStockDeductedEvent_성공() {
        // given
        StockDeductedEvent event = new StockDeductedEvent(1L, 5L, 100L);

        // orderId는 100L로 일관되게 사용됨
        when(orderDuplicationRepository.saveIfAbsentByOrderId(eq(1L), any())).thenReturn(true);
        when(stockJpaRepository.decreaseStock(event.productId(), event.quantity())).thenReturn(1);

        // when & then
        assertThatCode(() -> stockEventConsumer.consumeStockDeductedEvent(event))
                .doesNotThrowAnyException();

        verify(stockJpaRepository).decreaseStock(event.productId(), event.quantity());
        verify(stockOutboxRepository).updateStatusByOrderId(
                eq(event.orderId()), eq(OutboxStatus.SEND), any()
        );
    }

    @Test
    @DisplayName("재고 부족 시 BusinessException 발생")
    void consumeStockDeductedEvent_재고부족() {
        // given
        StockDeductedEvent event = new StockDeductedEvent(1L, 5L, 100L);

        when(orderDuplicationRepository.saveIfAbsentByOrderId(eq(1L), any())).thenReturn(true);
        when(stockJpaRepository.decreaseStock(event.productId(), event.quantity())).thenReturn(0);

        // when & then
        assertThatThrownBy(() -> stockEventConsumer.consumeStockDeductedEvent(event))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> {
                    BusinessException ex = (BusinessException) e;
                    assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.STOCK_NOT_ENOUGH);
                });

        verify(stockJpaRepository).decreaseStock(event.productId(), event.quantity());
    }
}