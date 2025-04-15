package org.sangyunpark.gateway.filter.utils;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.sangyunpark.gateway.filter.dto.CachedUser;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RequestMutationUtilsTest {
    @Test
    @DisplayName("Claims 정보를 헤더에 추가하여 요청을 변경한다")
    void mutateRequestWithClaims_addsHeaders() {
        // given
        Claims claims = mock(Claims.class);
        when(claims.getSubject()).thenReturn("user@example.com");
        when(claims.get("userType", String.class)).thenReturn("NORMAL");
        when(claims.get("userStatus", String.class)).thenReturn("ACTIVE");

        MockServerHttpRequest request = MockServerHttpRequest.get("/api/v1/resource").build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        // when
        ServerWebExchange mutatedExchange = RequestMutationUtils.mutateRequestWithClaims(exchange, new CachedUser("user@example.com", "NORMAL", "ACTIVE"));
        ServerHttpRequest mutatedRequest = mutatedExchange.getRequest();

        // then
        assertThat(mutatedRequest.getHeaders().getFirst(RequestMutationUtils.X_USER_EMAIL)).isEqualTo("user@example.com");
        assertThat(mutatedRequest.getHeaders().getFirst(RequestMutationUtils.X_USER_TYPE)).isEqualTo("NORMAL");
        assertThat(mutatedRequest.getHeaders().getFirst(RequestMutationUtils.X_USER_STATUS)).isEqualTo("ACTIVE");
    }
}