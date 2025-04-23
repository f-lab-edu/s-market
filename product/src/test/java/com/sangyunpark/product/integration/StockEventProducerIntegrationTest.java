package com.sangyunpark.product.integration;

import com.sangyunpark.product.application.event.StockDeductedEvent;
import com.sangyunpark.product.infrastructure.kafka.StockEventProducer;
import com.sangyunpark.product.infrastructure.redis.OrderDuplicationRepository;
import com.sangyunpark.product.infrastructure.redis.StockRedisRepository;
import com.sangyunpark.product.infrastructure.repository.StockJpaRepository;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
public class StockEventProducerIntegrationTest {

    private final String TOPIC = "stock.deducted";

    @Autowired
    private StockJpaRepository stockJpaRepository;

    @Autowired
    private StockRedisRepository stockRedisRepository;

    @Autowired
    private OrderDuplicationRepository orderDuplicationRepository;

    @Autowired
    private StockEventProducer stockEventProducer;

    private Consumer<String, StockDeductedEvent> consumer;

    @Container
    static KafkaContainer kafkaContainer = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.4.0"));

    @Container
    static GenericContainer<?> redisContainer = new GenericContainer<>(DockerImageName.parse("redis:7.0.5"))
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void setKafkaProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", kafkaContainer::getBootstrapServers);
        registry.add("spring.data.redis.host", redisContainer::getHost);
        registry.add("spring.data.redis.port", () -> redisContainer.getMappedPort(6379));
    }

    @BeforeEach
    void setUp() {
        Map<String, Object> props = KafkaTestUtils.consumerProps(
                kafkaContainer.getBootstrapServers(),
                "test-group",
                "true"
        );
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "*");

        consumer = new DefaultKafkaConsumerFactory<>(
                props,
                new StringDeserializer(),
                new JsonDeserializer<>(StockDeductedEvent.class, false)
        ).createConsumer();

        consumer.subscribe(java.util.Collections.singletonList(TOPIC));
    }

    @Test
    @DisplayName("StockDeductedEvent를 카프카로 정상 발행해야 한다.")
    void StockDeductedEvent_카프카_발행_성공_검증() {
        // given
        StockDeductedEvent event = new StockDeductedEvent(1L, 10L, 100L);

        // when
        stockEventProducer.sendStockDeductedEvent(event);

        // then
        ConsumerRecord<String, StockDeductedEvent> record = KafkaTestUtils.getSingleRecord(consumer, TOPIC);
        StockDeductedEvent received = record.value();

        assertThat(received.orderId()).isEqualTo(event.orderId());
        assertThat(received.productId()).isEqualTo(event.productId());
        assertThat(received.quantity()).isEqualTo(event.quantity());
    }
}