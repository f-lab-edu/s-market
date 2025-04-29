package com.sangyunpark.product.infrastructure.kafka;

import com.sangyunpark.product.application.event.StockDeductedEvent;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@EmbeddedKafka(partitions = 3, topics = "stock.deducted")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class StockEventOrderingTest {
    private static final String TOPIC = "stock.deducted";
    private static final int MESSAGE_COUNT = 100;

    @Autowired
    private StockEventProducer stockEventProducer;

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    private Consumer<String, StockDeductedEvent> consumer;

    @BeforeEach
    void setUp() {
        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps(
                "ordering-test-group", "true", embeddedKafkaBroker
        );
        consumer = new DefaultKafkaConsumerFactory<>(
                consumerProps,
                new StringDeserializer(),
                new JsonDeserializer<>(StockDeductedEvent.class, false)
        ).createConsumer();

        embeddedKafkaBroker.consumeFromAnEmbeddedTopic(consumer, TOPIC);
    }

    @AfterEach
    void tearDown() {
        consumer.close();
    }

    @Test
    void 재고차감_이벤트_순서보장_검증() throws Exception {

        for (int i = 0; i < MESSAGE_COUNT; i++) {
            stockEventProducer.sendStockDeductedEvent(new StockDeductedEvent((long) i, 1L, 1L));
        }

        List<ConsumerRecord<String, StockDeductedEvent>> consumedRecords = new ArrayList<>();
        long endTime = System.currentTimeMillis() + 5000;

        while (System.currentTimeMillis() < endTime && consumedRecords.size() < MESSAGE_COUNT) {
            consumer.poll(java.time.Duration.ofMillis(500)).forEach(consumedRecords::add);
        }

        assertThat(consumedRecords).hasSize(MESSAGE_COUNT);

        List<Long> orderIds = consumedRecords.stream()
                .map(record -> record.value().orderId())
                .toList();

        List<Long> expectedOrderIds = IntStream.range(0, MESSAGE_COUNT)
                .mapToObj(i -> (long) i)
                .toList();

        assertThat(orderIds)
                .isEqualTo(expectedOrderIds);
    }
}
