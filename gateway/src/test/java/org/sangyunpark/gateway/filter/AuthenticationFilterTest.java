package org.sangyunpark.gateway.filter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.sangyunpark.gateway.application.AuthenticationService;
import org.sangyunpark.gateway.constant.code.ErrorCode;
import org.sangyunpark.gateway.infrastructure.redis.TokenBlackListRepository;
import org.sangyunpark.gateway.jwt.TokenValidator;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.*;

@SuppressWarnings("NonAsciiCharacters")
class AuthenticationFilterTest {

    private static final String BEARER = "Bearer ";
    private static final String VALID_TOKEN = "valid-token";
    private static final String INVALID_TOKEN = "invalid-token";

    private AuthenticationService authenticationService;
    private GatewayFilterChain filterChain;
    private AuthenticationFilter filter;
    private TokenBlackListRepository tokenBlackListRepository;
    private TokenValidator tokenValidator;

    @BeforeEach
    void setUp() {
        tokenValidator = mock(TokenValidator.class);
        tokenBlackListRepository= mock(TokenBlackListRepository.class);
        authenticationService = new AuthenticationService(tokenValidator,tokenBlackListRepository);
        filterChain = mock(GatewayFilterChain.class);
        filter = new AuthenticationFilter(authenticationService);
    }

    @Test
    @DisplayName("화이트리스트 경로는 토큰 없이 통과")
    void 화이트리스트_경로는_토큰_없이_통과() {
        ServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.post("/api/v1/users").build()
        );

        when(filterChain.filter(exchange)).thenReturn(Mono.empty());

        StepVerifier.create(filter.filter(exchange, filterChain))
                .verifyComplete();

        verify(filterChain, times(1)).filter(exchange);
    }

    @Test
    @DisplayName("Authorization 헤더가 없으면 401 반환")
    void 헤더_없으면_401() {
        ServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/secure").build()
        );

        StepVerifier.create(filter.filter(exchange, filterChain))
                .then(() -> {
                    HttpStatus status = (HttpStatus) exchange.getResponse().getStatusCode();
                    assert status == ErrorCode.INVALID_TOKEN.getStatus();
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("토큰이 유효하지 않으면 401 반환")
    void 유효하지_않은_토큰은_401() {
        ServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/secure")
                        .header(HttpHeaders.AUTHORIZATION, BEARER + INVALID_TOKEN)
                        .build()
        );

        when(tokenValidator.validateToken(anyString())).thenReturn(true);
        when(tokenBlackListRepository.isBlackList(anyString())).thenReturn(Mono.just(false));

        StepVerifier.create(filter.filter(exchange, filterChain))
                .then(() -> {
                    HttpStatus status = (HttpStatus) exchange.getResponse().getStatusCode();
                    assert status == ErrorCode.INVALID_TOKEN.getStatus();
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("유효한 토큰이면 필터 체인 통과")
    void 유효한_토큰은_통과() {
        ServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/secure")
                        .header(HttpHeaders.AUTHORIZATION, BEARER + VALID_TOKEN)
                        .build()
        );

        when(tokenValidator.validateToken(anyString())).thenReturn(true);
        when(tokenBlackListRepository.isBlackList(anyString())).thenReturn(Mono.just(true));
        when(filterChain.filter(exchange)).thenReturn(Mono.empty());

        StepVerifier.create(filter.filter(exchange, filterChain))
                .verifyComplete();

        verify(filterChain, times(1)).filter(exchange);
    }
}