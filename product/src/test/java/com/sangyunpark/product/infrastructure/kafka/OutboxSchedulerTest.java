package com.sangyunpark.product.infrastructure.kafka;

import com.sangyunpark.product.application.event.StockDeductedEvent;
import com.sangyunpark.product.constant.OutboxStatus;
import com.sangyunpark.product.domain.entity.StockOutbox;
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
    private KafkaTemplate<String, StockDeductedEvent> kafkaTemplate;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("PENDING 상태 이벤트를 성공적으로 전송하고 상태를 SEND로 변경한다")
    void PENDING_상태_이벤트_성공적_전송후_상태_SEND_변경() throws Exception {
        // given
        LocalDateTime fixedNow = LocalDateTime.of(2025, 5, 5, 22, 59, 29); // 고정 시간

        StockOutbox event = StockOutbox.builder()
                .id(1L)
                .orderId(10L)
                .productId(1L)
                .quantity(3L)
                .status(OutboxStatus.PENDING)
                .createdAt(fixedNow)
                .updatedAt(fixedNow)
                .build();

        when(stockOutboxRepository.findPendingOutboxEvent(eq(OutboxStatus.PENDING), any()))
                .thenReturn(List.of(event));

        when(kafkaTemplate.send(anyString(), anyString(), any(StockDeductedEvent.class)))
                .thenReturn(CompletableFuture.completedFuture(null));

        // when
        outboxScheduler.resendPendingMessages();

        // then
        verify(kafkaTemplate, times(1))
                .send(anyString(), anyString(), any(StockDeductedEvent.class));

        verify(stockOutboxRepository, times(1))
                .updateStatusByOrderId(eq(event.getId()), eq(OutboxStatus.SEND), any());
    }

    @Test
    @DisplayName("Kafka 전송 실패 시 상태를 변경하지 않는다")
    void KafKa_전송_실패시_상태_변경안함() throws Exception {
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