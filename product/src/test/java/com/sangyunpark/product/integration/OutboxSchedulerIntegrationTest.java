package com.sangyunpark.product.integration;


import com.sangyunpark.product.constant.OutboxType;
import com.sangyunpark.product.infrastructure.kafka.event.StockDeductedEvent;
import com.sangyunpark.product.constant.OutboxStatus;
import com.sangyunpark.product.domain.entity.Stock;
import com.sangyunpark.product.domain.entity.StockOutbox;
import com.sangyunpark.product.infrastructure.kafka.OutboxScheduler;
import com.sangyunpark.product.infrastructure.repository.StockJpaRepository;
import com.sangyunpark.product.infrastructure.repository.StockOutboxRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.testcontainers.shaded.org.awaitility.Awaitility.await;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@EnableScheduling
public class OutboxSchedulerIntegrationTest {

    @Autowired
    private StockOutboxRepository stockOutboxRepository;

    @Autowired
    private StockJpaRepository stockJpaRepository;

    @Autowired
    private KafkaTemplate<String, StockDeductedEvent> kafkaTemplate;

    @Autowired
    private OutboxScheduler outboxScheduler;

    @Autowired
    private EntityManager em;

    @Container
    static KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.4.0"));

    @DynamicPropertySource
    static void kafkaProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
    }

    @BeforeEach
    void setUp() {
        stockOutboxRepository.deleteAll();
    }

    @Test
    @DisplayName("Outbox 이벤트가 Kafka로 전송되고 상태가 SEND로 업데이트된다")
    void Outbox_이벤트가_Kafka로_전송되고_상태가_SEND로_업데이트된다() {
        // given
        StockOutbox outbox = StockOutbox.builder()
                .orderId(1L)
                .productId(1L)
                .eventId("")
                .quantity(5L)
                .type(OutboxType.DECR)
                .status(OutboxStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        stockOutboxRepository.save(outbox);

        stockJpaRepository.save(new Stock(null, 5L, 1L));

        // when
        outboxScheduler.resendPendingMessages();

        // then
        await()
                .atMost(Duration.ofSeconds(5))
                .untilAsserted(() -> {
                    StockOutbox updated = stockOutboxRepository.findById(outbox.getId()).orElseThrow();
                    Assertions.assertEquals(OutboxStatus.SEND, updated.getStatus());
                });
    }
}
