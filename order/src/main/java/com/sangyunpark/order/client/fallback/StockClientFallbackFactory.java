package com.sangyunpark.order.client.fallback;

import com.sangyunpark.order.client.StockClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;

import java.util.stream.Collectors;

@Slf4j
public class StockClientFallbackFactory implements FallbackFactory<StockClient> {

    @Override
    public StockClient create(Throwable cause) {
        log.error("stockServiceClient 호출 실패", cause);
        return productsId -> productsId.stream()
                .collect(Collectors.toMap(id -> id,  id -> 0L));
    }
}
