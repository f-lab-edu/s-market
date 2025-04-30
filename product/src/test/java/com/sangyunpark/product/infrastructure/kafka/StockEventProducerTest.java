package com.sangyunpark.product.infrastructure.kafka;

import com.sangyunpark.product.application.event.StockDeductedEvent;
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
    private KafkaTemplate<String, StockDeductedEvent> kafkaTemplate;

    private StockEventProducer stockEventProducer;

    @BeforeEach
    void setUp() {
        stockEventProducer = new StockEventProducer(kafkaTemplate);
    }

    @Test
    @DisplayName("재고 차감 이벤트가 정상적으로 전송되는지 확인한다.")
    void 재고_차감_이벤트_전송_성공_확인() {
        // given
        StockDeductedEvent event = new StockDeductedEvent(1L, 5L, 100L);

        // when
        when(kafkaTemplate.send("stock.deducted", String.valueOf(event.productId()), event))
                .thenReturn(CompletableFuture.completedFuture(null));
        stockEventProducer.sendStockDeductedEvent(event);

        // then
        verify(kafkaTemplate).send("stock.deducted", String.valueOf(event.productId()), event);
    }
}