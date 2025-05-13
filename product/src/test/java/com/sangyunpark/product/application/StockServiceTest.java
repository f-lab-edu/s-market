package com.sangyunpark.product.application;

import com.sangyunpark.product.infrastructure.kafka.event.StockDeductedEvent;
import com.sangyunpark.product.constant.ErrorCode;
import com.sangyunpark.product.domain.entity.StockOutbox;
import com.sangyunpark.product.exception.BusinessException;
import com.sangyunpark.product.infrastructure.kafka.StockEventProducer;
import com.sangyunpark.product.infrastructure.kafka.event.StockIncreasedEvent;
import com.sangyunpark.product.infrastructure.redis.OrderDuplicationRepository;
import com.sangyunpark.product.infrastructure.redis.StockRedisRepository;
import com.sangyunpark.product.infrastructure.repository.StockJpaRepository;
import com.sangyunpark.product.infrastructure.repository.StockOutboxRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.Duration;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StockServiceTest {

    @InjectMocks
    private StockService stockService;

    @Mock
    private StockOutboxRepository stockOutboxRepository;

    @Mock
    private StockJpaRepository stockJpaRepository;

    @Mock
    private StockRedisRepository stockRedisRepository;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @Mock
    private StockEventProducer stockEventProducer;

    @Mock
    private OrderDuplicationRepository orderDuplicationRepository;

    private final String ERROR_CODE = "errorCode";

    @Test
    @DisplayName("Redis 캐시 없음 → DB에서 조회 후 캐시에 저장 + 재고 차감 성공")
    void 캐시없음_정상차감() {
        // given
        Long productId = 1L, orderId = 100L, quantity = 2L;
        Duration duplicationTtl = Duration.ofSeconds(30L);
        Duration cacheTtl = Duration.ofMinutes(3L);

        given(stockRedisRepository.isExisted(productId)).willReturn(false); //
        given(stockJpaRepository.findQuantityByProductId(productId)).willReturn(Optional.of(10L));
        given(stockRedisRepository.decrease(productId, quantity)).willReturn(8L);

        // when
        stockService.decreaseStockAndPublish(productId, quantity, orderId);

        // then
        verify(stockRedisRepository).setIfAbsentWithTTL(productId, 10L, cacheTtl); //
        verify(stockOutboxRepository).save(any());
    }

    @Test
    @DisplayName("Redis 캐시 있음 → 재고 차감 정상 → Outbox 저장 → 이벤트 발행")
    void 캐시있음_정상차감() {
        // given
        Long productId = 2L;
        Long orderId = 200L;
        Long quantity = 3L;

        given(stockRedisRepository.isExisted(productId)).willReturn(true);
        given(stockRedisRepository.decrease(productId, quantity)).willReturn(7L);

        // when
        stockService.decreaseStockAndPublish(productId, quantity, orderId);

        // then
        verify(stockRedisRepository, never()).setIfAbsentWithTTL(any(), any(), any()); // 캐시 이미 있음
        verify(stockRedisRepository).decrease(productId, quantity);                    // Redis 차감 호출됨
        verify(stockOutboxRepository).save(any(StockOutbox.class));                    // Outbox 저장됨
        verify(applicationEventPublisher).publishEvent(any(StockDeductedEvent.class)); // 이벤트 발행됨
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

    @Test
    @DisplayName("Redis 캐시 없음 → DB 조회 후 캐싱 → 재고 차감 → Outbox 저장 → 이벤트 발행")
    void 캐시없음_DB조회_캐싱_차감_이벤트발행() {
        // given
        Long productId = 2L;
        Long orderId = 200L;
        Long quantity = 3L;
        Long dbQuantity = 10L;

        given(stockRedisRepository.isExisted(productId))
                .willReturn(false);
        given(stockJpaRepository.findQuantityByProductId(productId))
                .willReturn(Optional.of(dbQuantity));
        given(stockRedisRepository.decrease(productId, quantity))
                .willReturn(dbQuantity - quantity);

        // when
        assertDoesNotThrow(() ->
                stockService.decreaseStockAndPublish(productId, quantity, orderId)
        );

        // then
        verify(stockJpaRepository, times(1)).findQuantityByProductId(productId); // DB 조회
        verify(stockRedisRepository, times(1))
                .setIfAbsentWithTTL(eq(productId), eq(dbQuantity), any()); // Redis 캐싱
        verify(stockRedisRepository, times(1))
                .decrease(productId, quantity); // 차감
        verify(stockOutboxRepository, times(1))
                .save(any(StockOutbox.class)); // Outbox 저장
        verify(applicationEventPublisher, times(1))
                .publishEvent(any(StockDeductedEvent.class)); // 이벤트 발행
    }

    @Test
    @DisplayName("재고 증가 성공 → Redis 증가 → Outbox 저장 → 이벤트 발행")
    void 재고증가_정상처리() {
        // given
        Long productId = 5L;
        Long quantity = 4L;

        given(stockRedisRepository.increase(productId, quantity)).willReturn(14L);

        // when & then
        assertDoesNotThrow(() -> stockService.increaseStock(productId, quantity));

        verify(stockRedisRepository).increase(productId, quantity);         // Redis 증가
        verify(stockOutboxRepository).save(any(StockOutbox.class));         // Outbox 저장
        verify(applicationEventPublisher).publishEvent(any(StockIncreasedEvent.class)); // 이벤트 발행
    }

    @Test
    @DisplayName("재고 증가 실패 → 상품 없음 → 예외 발생")
    void 재고증가_상품없음_예외() {
        // given
        Long productId = 6L;
        Long quantity = 3L;

        given(stockRedisRepository.increase(productId, quantity)).willReturn(-1L);

        // when & then
        assertThatThrownBy(() -> stockService.increaseStock(productId, quantity))
                .isInstanceOf(BusinessException.class)
                .extracting(ERROR_CODE).isEqualTo(ErrorCode.PRODUCT_NOT_FOUND);
    }
}