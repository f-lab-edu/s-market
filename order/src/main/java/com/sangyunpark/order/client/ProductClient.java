package com.sangyunpark.order.client;

import com.sangyunpark.order.client.fallback.ProductClientFallbackFactory;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;

@Component
@FeignClient(name = "product-service", url = "${client.product-service.url}", fallbackFactory = ProductClientFallbackFactory.class)
public interface ProductClient {

    @Retry(name = "productService")
    @CircuitBreaker(name = "productService")
    @GetMapping("/exists")
    Map<Long, Boolean> checkProductsExist(@RequestBody List<Long> productsId);
}
