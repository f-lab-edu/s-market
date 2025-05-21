package com.sangyunpark.order.config;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableFeignClients(basePackages = "com.sangyunpark.order.client")
public class FeignConfig {
}
