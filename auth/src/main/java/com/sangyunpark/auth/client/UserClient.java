package com.sangyunpark.auth.client;

import com.sangyunpark.auth.client.dto.response.FeignUserSelectResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "user-service", url = "${service.user.url}")
public interface UserClient {

    @GetMapping("/api/v1/users")
    FeignUserSelectResponseDto findUserByEmail(@RequestParam("email") String email);
}
