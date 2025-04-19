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

    private final RequestMutationUtils requestMutationUtils = new RequestMutationUtils();
    private final String EMAIL = "user@example.com";
    private final String USER_TYPE = "userType";
    private final String USER_STATUS = "userStatus";
    private final String NORMAL = "NORMAL";
    private final String ACTIVE = "ACTIVE";

    private final String URL = "/api/v1/resource";

    @Test
    @DisplayName("Claims 정보를 헤더에 추가하여 요청을 변경한다")
    void mutateRequestWithClaims_addsHeaders() {
        // given
        Claims claims = mock(Claims.class);
        when(claims.getSubject()).thenReturn(EMAIL);
        when(claims.get(USER_TYPE, String.class)).thenReturn(NORMAL);
        when(claims.get(USER_STATUS, String.class)).thenReturn(ACTIVE);

        MockServerHttpRequest request = MockServerHttpRequest.get(URL).build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        // when
        ServerWebExchange mutatedExchange = requestMutationUtils.mutateRequestWithClaims(exchange, new CachedUser(EMAIL, NORMAL, ACTIVE));

        ServerHttpRequest mutatedRequest = mutatedExchange.getRequest();

        // then
        assertThat(mutatedRequest.getHeaders().getFirst(requestMutationUtils.X_USER_EMAIL)).isEqualTo(EMAIL);
        assertThat(mutatedRequest.getHeaders().getFirst(requestMutationUtils.X_USER_TYPE)).isEqualTo(NORMAL);
        assertThat(mutatedRequest.getHeaders().getFirst(requestMutationUtils.X_USER_STATUS)).isEqualTo(ACTIVE);
    }
}