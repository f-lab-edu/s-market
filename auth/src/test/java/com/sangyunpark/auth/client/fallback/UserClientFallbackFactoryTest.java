package com.sangyunpark.auth.client.fallback;

import com.sangyunpark.auth.client.UserClient;
import com.sangyunpark.auth.client.dto.response.FeignUserResponseDto;
import feign.FeignException;
import feign.Request;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("NonAsciiCharacters")
class UserClientFallbackFactoryTest {

    @Test
    @DisplayName("FeignClient가 실패하면 FallbackFactory가 지정된 기본 응답을 반환한다")
    void FeginClient_실패하는경우_FallbackFactory_지정된_기본응답을_반환() {
        // given
        Request mockRequest = Request.create(
                Request.HttpMethod.GET,
                "http://localhost/api/v1/users?email=test@example.com",
                new HashMap<>(),
                null,
                null,
                null
        );

        Throwable cause = new FeignException.BadRequest(
                "400 Bad Request",
                mockRequest,
                null,
                new HashMap<>()
        );

        UserClientFallbackFactory fallbackFactory = new UserClientFallbackFactory();

        // when
        UserClient fallbackClient = fallbackFactory.create(cause);
        FeignUserResponseDto response = fallbackClient.findUserByEmail("test@example.com");

        // then
        assertThat(response).isNotNull();
        assertThat(response.email()).isEqualTo("fallback@test@example.com");
        assertThat(response.id()).isEqualTo(-1L);
    }
}