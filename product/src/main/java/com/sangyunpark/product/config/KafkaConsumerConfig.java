package com.sangyunpark.product.config;

import com.sangyunpark.product.application.event.StockDeductedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

@Slf4j
@Configuration
public class KafkaConsumerConfig {

    private final String DLT = ".DLT";
    private final int MAX_RETRY_COUNT = 5;

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, StockDeductedEvent> kafkaListenerContainerFactory(
            ConsumerFactory<String, StockDeductedEvent> consumerFactory,
            DefaultErrorHandler errorHandler
    ) {
        ConcurrentKafkaListenerContainerFactory<String, StockDeductedEvent> factory = new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(consumerFactory);
        factory.setCommonErrorHandler(errorHandler);

        return factory;
    }

    @Bean
    public DefaultErrorHandler errorHandler(KafkaTemplate<String, StockDeductedEvent> kafkaTemplate) {
        DeadLetterPublishingRecoverer recover = new DeadLetterPublishingRecoverer(kafkaTemplate,
                (record, ex) -> new org.apache.kafka.common.TopicPartition(record.topic() + DLT, record.partition()));

        FixedBackOff backOff = new FixedBackOff(1000L, MAX_RETRY_COUNT);

        DefaultErrorHandler errorHandler = new DefaultErrorHandler(recover, backOff);

        errorHandler.setRetryListeners((record, ex, deliveryAttempt) -> {
            log.warn("카프카 컨슈머 재시도 횟수 {} 기록 내용: {}", deliveryAttempt, record);
        });

        return errorHandler;
    }
}
