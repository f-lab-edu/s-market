package com.sangyunpark.product.integration;

import com.sangyunpark.product.domain.entity.Stock;
import com.sangyunpark.product.infrastructure.redis.StockRedisRepository;
import com.sangyunpark.product.infrastructure.repository.StockJpaRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.testcontainers.shaded.org.awaitility.Awaitility.await;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
public class StockIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private StockJpaRepository stockJpaRepository;

    @Autowired
    private StockRedisRepository stockRedisRepository;

    @Container
    static KafkaContainer kafkaContainer = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.4.0"))
            .withReuse(true);

    @Autowired
    private RedisTemplate<Object, Object> redisTemplate;


    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", kafkaContainer::getBootstrapServers);
        registry.add("spring.kafka.consumer.auto-offset-reset", () -> "earliest");
        registry.add("spring.kafka.consumer.enable-auto-commit", () -> "false");
        registry.add("spring.kafka.listener.ack-mode", () -> "manual");
    }

    @BeforeEach
    void setUp() {
        redisTemplate.getConnectionFactory().getConnection().flushAll();
        Stock stock1 = Stock.builder().productId(1L).quantity(10L).build();
        Stock stock2 = Stock.builder().productId(2L).quantity(10L).build();
        Stock stock3 = Stock.builder().productId(3L).quantity(101L).build();
        stockJpaRepository.saveAll(List.of(stock1, stock2, stock3));
        stockRedisRepository.setQuantity(1L, 10L, Duration.ofSeconds(100));
        stockRedisRepository.setQuantity(2L, 10L, Duration.ofSeconds(100));
        stockRedisRepository.setQuantity(3L, 101L, Duration.ofSeconds(100));
    }

    @AfterEach
    void tearDown() {
        redisTemplate.getConnectionFactory().getConnection().flushAll();
    }

    @Test
    @DisplayName("재고 차감 Redis-Kafka-Database 요청 흐름입니다.")
    void 재고차감_요청_전체흐름_검증() throws Exception {

        // when, then
        mockMvc.perform(patch("/api/v1/stocks/{productId}/decrease", "1")
                        .param("quantity", "3")
                        .param("orderId", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        await()
                .atMost(Duration.ofSeconds(30))
                .untilAsserted(() -> {
                    Stock stock = stockJpaRepository.findById(1L).orElseThrow();
                    assertEquals(7, stock.getQuantity());
                });

        Stock stock = stockJpaRepository.findById(1L).get();
        assertEquals(7, stock.getQuantity());
    }

    @Test
    @DisplayName("재고가 부족할 경우 차감 요청은 실패해야 한다.")
    void 재고부족_차감요청_실패() throws Exception {

        // when, then
        mockMvc.perform(patch("/api/v1/stocks/{productId}/decrease", "2")
                        .param("quantity", "999")
                        .param("orderId", "11")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("같은 주문 ID로 중복 요청 시 재고가 한 번만 차감돼야 한다.")
    void 중복요청_한번만차감() throws Exception {
        // when, then
        for (int i = 0; i < 100; i++) {
            mockMvc.perform(patch("/api/v1/stocks/{productId}/decrease", "3")
                            .param("quantity", "1")
                            .param("orderId", "12")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        }

        await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> {
            Stock findStock = stockJpaRepository.findById(3L).orElseThrow();
            assertEquals(100, findStock.getQuantity());
        });
    }

    @Test
    @DisplayName("100개의 동시 재고 차감 요청 시 재고가 음수가 되지 않아야 한다.")
    void 동시에_재고차감_재고_음수_방지() throws Exception {

        // given
        int threadCount = 1000;
        ExecutorService executor = Executors.newFixedThreadPool(10);
        CountDownLatch latch = new CountDownLatch(threadCount);

        // when
        for (int i = 0; i < threadCount; i++) {
            int orderId = 200 + i;
            executor.submit(() -> {
                try {
                    mockMvc.perform(patch("/api/v1/stocks/{productId}/decrease", 3)
                                    .param("quantity", "1")
                                    .param("orderId", String.valueOf(orderId))
                                    .contentType(MediaType.APPLICATION_JSON))
                            .andExpect(status().isOk());
                } catch (Exception e) {

                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        TimeUnit.SECONDS.sleep(5);

        // then
        Stock stock = stockJpaRepository.findById(3L).orElseThrow();
        long finalQuantity = stock.getQuantity();
        assertTrue(finalQuantity >= 0);
    }

    @Test
    @DisplayName("재고 증가 요청 정상 동작 테스트")
    void 재고증가_정상동작() throws Exception {
        mockMvc.perform(patch("/api/v1/stocks/{productId}/increase", 1)
                        .param("quantity", "5")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> {
            Stock stock = stockJpaRepository.findById(1L).orElseThrow();
            assertEquals(15, stock.getQuantity());
        });
    }

    @Test
    @DisplayName("존재하지 않는 상품 재고 증가 요청 시 404 오류 반환")
    void 재고증가_상품없음() throws Exception {
        mockMvc.perform(patch("/api/v1/stocks/{productId}/increase", 999)
                        .param("quantity", "3")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("100개의 동시 재고 증가 요청 시 재고가 정확히 반영되어야 한다.")
    void 동시에_재고증가_정합성확인() throws Exception {
        int threadCount = 100;
        ExecutorService executor = Executors.newFixedThreadPool(10);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            int finalI = i;
            executor.submit(() -> {
                try {
                    mockMvc.perform(patch("/api/v1/stocks/{productId}/increase", 3)
                            .param("quantity", "1")
                            .contentType(MediaType.APPLICATION_JSON));
                } catch (Exception e) {

                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        TimeUnit.SECONDS.sleep(5);

        Stock stock = stockJpaRepository.findById(3L).orElseThrow();
        assertEquals(101 + threadCount, stock.getQuantity());
    }
}