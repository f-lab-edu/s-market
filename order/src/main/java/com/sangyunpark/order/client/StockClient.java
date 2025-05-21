package com.sangyunpark.order.client;

import com.sangyunpark.order.client.fallback.StockClientFallbackFactory;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;

@Component
@FeignClient(name = "stock-service", url = "${client.stock-service.url}", fallbackFactory = StockClientFallbackFactory.class)
public interface StockClient {

    @Retry(name = "stockService")
    @CircuitBreaker(name = "stockService")
    @GetMapping("/exists")
    Map<Long, Long> getQuantitiesByProductIds(final @RequestBody List<Long> productsId);
}
