package com.sangyunpark.product.infrastructure.kafka;

import com.sangyunpark.product.constant.OutboxType;
import com.sangyunpark.product.infrastructure.kafka.event.StockDeductedEvent;
import com.sangyunpark.product.constant.OutboxStatus;
import com.sangyunpark.product.domain.entity.StockOutbox;
import com.sangyunpark.product.infrastructure.kafka.event.StockIncreasedEvent;
import com.sangyunpark.product.infrastructure.repository.StockOutboxRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class OutboxSchedulerTest {

    @InjectMocks
    private OutboxScheduler outboxScheduler;

    @Mock
    private StockOutboxRepository stockOutboxRepository;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("DECR 타입 Outbox 이벤트를 전송하고 상태를 SEND로 변경한다")
    void resendPendingMessages_DECR() {
        // given
        StockOutbox outbox = StockOutbox.builder()
                .orderId(100L)
                .productId(1L)
                .quantity(3L)
                .type(OutboxType.DECR)
                .status(OutboxStatus.PENDING)
                .build();

        when(stockOutboxRepository.findPendingOutboxEvent(eq(OutboxStatus.PENDING), any()))
                .thenReturn(List.of(outbox));

        when(kafkaTemplate.send(anyString(), anyString(), any(StockDeductedEvent.class)))
                .thenReturn(CompletableFuture.completedFuture(null));

        // when
        outboxScheduler.resendPendingMessages();

        // then
        verify(kafkaTemplate).send(anyString(), anyString(), any(StockDeductedEvent.class));
        verify(stockOutboxRepository).updateStatusByOrderId(eq(100L), eq(OutboxStatus.SEND), any(LocalDateTime.class));
    }


    @Test
    @DisplayName("INCR 타입 Outbox 이벤트를 전송하고 상태를 SEND로 변경한다")
    void resendPendingMessages_INCR() {
        // given
        StockOutbox outbox = StockOutbox.builder()
                .eventId("event-123")
                .productId(2L)
                .quantity(5L)
                .type(OutboxType.INCR)
                .status(OutboxStatus.PENDING)
                .build();

        when(stockOutboxRepository.findPendingOutboxEvent(eq(OutboxStatus.PENDING), any()))
                .thenReturn(List.of(outbox));

        when(kafkaTemplate.send(anyString(), anyString(), any(StockIncreasedEvent.class)))
                .thenReturn(CompletableFuture.completedFuture(null));

        // when
        outboxScheduler.resendPendingMessages();

        // then
        verify(kafkaTemplate).send(anyString(), anyString(), any(StockIncreasedEvent.class));
        verify(stockOutboxRepository).updateStatusByEventId(eq("event-123"), eq(OutboxStatus.SEND), any(LocalDateTime.class));
    }


    @Test
    @DisplayName("Kafka 전송 실패 시 상태를 변경하지 않는다")
    void KafKa_전송_실패시_상태_변경안함() {
        // given
        StockOutbox event = StockOutbox.builder()
                .id(1L)
                .orderId(10L)
                .productId(1L)
                .quantity(3L)
                .status(OutboxStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(stockOutboxRepository.findPendingOutboxEvent(OutboxStatus.SEND, LocalDateTime.now()))
                .thenReturn(List.of(event));

        when(kafkaTemplate.send(anyString(), anyString(), any(StockDeductedEvent.class)))
                .thenThrow(new RuntimeException("Kafka send failed"));

        // when
        outboxScheduler.resendPendingMessages();

        // then
        verify(stockOutboxRepository, never()).bulkUpdateStatus(any(), any(), any());
    }

}