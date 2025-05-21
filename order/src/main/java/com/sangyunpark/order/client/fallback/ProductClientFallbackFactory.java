package com.sangyunpark.order.client.fallback;

import com.sangyunpark.order.client.ProductClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;

import java.util.stream.Collectors;

@Slf4j
public class ProductClientFallbackFactory implements FallbackFactory<ProductClient> {

    @Override
    public ProductClient create(Throwable cause) {
        log.error("ProductServiceClient 호출 실패", cause);
        return productsId -> productsId.stream()
                .collect(Collectors.toMap(id -> id, id -> false));
    }
}
