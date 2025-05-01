package com.sangyunpark.product.integration;


import com.sangyunpark.product.application.event.StockDeductedEvent;
import com.sangyunpark.product.constant.OutboxStatus;
import com.sangyunpark.product.domain.entity.StockOutbox;
import com.sangyunpark.product.infrastructure.repository.StockOutboxRepository;
import jakarta.transaction.Transactional;
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
public class OutboxKafkaPublisherIntegrationTest {

    @Autowired
    private StockOutboxRepository stockOutboxRepository;

    @Autowired
    private KafkaTemplate<String, StockDeductedEvent> kafkaTemplate;

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
    void outboxEventIsPublishedAndUpdated() {
        // given
        StockOutbox outbox = StockOutbox.builder()
                .orderId(123L)
                .productId(1L)
                .quantity(5L)
                .status(OutboxStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        stockOutboxRepository.save(outbox);

        await()
                .atMost(Duration.ofSeconds(5))
                .untilAsserted(() -> {
                    StockOutbox updated = stockOutboxRepository.findById(outbox.getId()).orElseThrow();
                    Assertions.assertEquals(OutboxStatus.SEND, updated.getStatus());
                });
    }
}
