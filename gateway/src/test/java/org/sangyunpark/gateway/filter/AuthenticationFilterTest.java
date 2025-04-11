package org.sangyunpark.gateway.filter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.sangyunpark.gateway.TokenValidator;
import org.sangyunpark.gateway.constant.code.ErrorCode;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SuppressWarnings("NonAsciiCharacters")
class AuthenticationFilterTest {

    private static final String USER_URL = "/api/v1/users";
    private static final String SECURE_ENDPOINT = "/secure-endpoint";
    private static final String BEARER = "Bearer ";
    private static final String INVALID_TOKEN = "invalid-token";

    private TokenValidator tokenValidator;
    private AuthenticationFilter filter;
    private GatewayFilterChain filterChain;

    @BeforeEach
    void setUp() {
        tokenValidator = mock(TokenValidator.class);
        filter = new AuthenticationFilter(tokenValidator);
        filterChain = mock(GatewayFilterChain.class);
    }

    @Test
    @DisplayName("화이트리스트 경로는 토큰 없이 통과해야 한다")
    void 화이트리스트_경로는_토큰_없이_통과() {
        MockServerHttpRequest request = MockServerHttpRequest
                .post(USER_URL)
                .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        when(filterChain.filter(exchange)).thenReturn(Mono.empty());

        Mono<Void> result = filter.filter(exchange, filterChain);

        StepVerifier.create(result).verifyComplete();
    }

    @Test
    @DisplayName("Authorization 헤더가 없으면 401 반환")
    void Authorization_헤더가_없으면_401_반환() {
        MockServerHttpRequest request = MockServerHttpRequest
                .get(SECURE_ENDPOINT)
                .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        Mono<Void> result = filter.filter(exchange, filterChain);

        StepVerifier.create(result)
                .then(() -> {
                    HttpStatus status = (HttpStatus) exchange.getResponse().getStatusCode();
                    assert status == ErrorCode.INVALID_TOKEN.getStatus();
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("토큰이 유효하지 않으면 401 반환")
    void 토큰이_유효하지_않으면_401_반환() {
        MockServerHttpRequest request = MockServerHttpRequest
                .get(SECURE_ENDPOINT)
                .header(HttpHeaders.AUTHORIZATION, BEARER + INVALID_TOKEN)
                .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        when(tokenValidator.validateToken(INVALID_TOKEN)).thenReturn(false);

        Mono<Void> result = filter.filter(exchange, filterChain);

        StepVerifier.create(result)
                .then(() -> {
                    HttpStatus status = (HttpStatus) exchange.getResponse().getStatusCode();
                    assert status == ErrorCode.INVALID_TOKEN.getStatus();
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("유효한 토큰이면 필터 체인을 통과함")
    void 유효한_토큰이면_필터체인_통과() {
        MockServerHttpRequest request = MockServerHttpRequest
                .get(SECURE_ENDPOINT)
                .header(HttpHeaders.AUTHORIZATION, BEARER + INVALID_TOKEN)
                .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        when(tokenValidator.validateToken(INVALID_TOKEN)).thenReturn(true);
        when(filterChain.filter(exchange)).thenReturn(Mono.empty());

        Mono<Void> result = filter.filter(exchange, filterChain);

        StepVerifier.create(result).verifyComplete();
    }
}