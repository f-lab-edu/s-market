package com.sangyunpark.auth.client;

import com.sangyunpark.auth.client.dto.response.FeignUserResponseDto;
import com.sangyunpark.auth.client.fallback.UserClientFallbackFactory;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import jakarta.validation.constraints.Email;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "user-service", url = "${service.user.url}",fallbackFactory = UserClientFallbackFactory.class)
public interface UserClient {
    @GetMapping("/api/v1/users")
    @CircuitBreaker(name = "userService")
    FeignUserResponseDto findUserByEmail(@RequestParam("email") @Email String email);
}