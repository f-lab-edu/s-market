package com.sangyunpark.auth.client.fallback;

import com.sangyunpark.auth.client.UserClient;
import com.sangyunpark.auth.client.dto.response.FeignUserResponseDto;
import org.springframework.cloud.openfeign.FallbackFactory;

public class UserClientFallbackFactory implements FallbackFactory<UserClient> {

    private static final String EMAIL  = "fallback@";

    @Override
    public UserClient create(Throwable cause) {
        return email -> FeignUserResponseDto.builder()
                .id(-1L)
                .email(EMAIL + email)
                .build();
    }
}
