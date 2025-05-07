package com.sangyunpark.product.infrastructure.kafka;

import com.sangyunpark.product.infrastructure.kafka.event.StockDeductedEvent;
import com.sangyunpark.product.infrastructure.kafka.event.StockIncreasedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.concurrent.CompletableFuture;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings("NonAsciiCharacters")
@ExtendWith(MockitoExtension.class)
class StockEventProducerTest {

    @Mock
    private KafkaTemplate<String, StockDeductedEvent> kafkaDecrTemplate;

    @Mock
    private KafkaTemplate<String, StockIncreasedEvent> kafkaIncrTemplate;

    private StockEventProducer stockEventProducer;

    @BeforeEach
    void setUp() {
        stockEventProducer = new StockEventProducer(kafkaDecrTemplate, kafkaIncrTemplate);
    }

    @Test
    @DisplayName("재고 차감 이벤트가 정상적으로 전송되는지 확인한다.")
    void 재고_차감_이벤트_전송_성공_확인() {
        // given
        StockDeductedEvent event = new StockDeductedEvent(1L, 5L, 100L);

        // when
        when(kafkaDecrTemplate.send("stock.deducted", String.valueOf(event.productId()), event))
                .thenReturn(CompletableFuture.completedFuture(null));
        stockEventProducer.sendStockDeductedEvent(event);

        // then
        verify(kafkaDecrTemplate).send("stock.deducted", String.valueOf(event.productId()), event);
    }

    @Test
    @DisplayName("재고 증가 이벤트가 정상적으로 전송되는지 확인한다.")
    void 재고_증가_이벤트_전송_성공_확인() {
        // given
        StockIncreasedEvent event = new StockIncreasedEvent("event-123", 1L, 5L);

        // when
        when(kafkaIncrTemplate.send("stock.increased", String.valueOf(event.productId()), event))
                .thenReturn(CompletableFuture.completedFuture(null));
        stockEventProducer.sendStockIncreasedEvent(event);

        // then
        verify(kafkaIncrTemplate).send("stock.increased", String.valueOf(event.productId()), event);
    }
}