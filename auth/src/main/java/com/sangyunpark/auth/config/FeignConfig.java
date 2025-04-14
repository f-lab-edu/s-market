package com.sangyunpark.auth.config;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableFeignClients(basePackages = "com.sangyunpark.auth.client")
public class FeignConfig {
}
