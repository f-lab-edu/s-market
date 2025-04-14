package org.sangyunpark.gateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.sangyunpark.gateway.application.AuthenticationService;
import org.sangyunpark.gateway.constant.code.ErrorCode;
import org.sangyunpark.gateway.infrastructure.redis.TokenBlackListRepository;
import org.sangyunpark.gateway.jwt.TokenProvider;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@SuppressWarnings("NonAsciiCharacters")
class AuthenticationFilterTest {

    private static final String BEARER = "Bearer ";
    private static final String VALID_TOKEN = "valid-token";
    private static final String INVALID_TOKEN = "invalid-token";
    private static final String EMAIL = "user@example.com";

    private GatewayFilterChain filterChain;
    private AuthenticationFilter filter;
    private TokenBlackListRepository tokenBlackListRepository;
    private TokenProvider tokenProvider;
    private AuthenticationService authenticationService;

    @BeforeEach
    void setUp() {
        tokenBlackListRepository= mock(TokenBlackListRepository.class);
        tokenProvider = mock(TokenProvider.class);
        authenticationService = new AuthenticationService(tokenProvider, tokenBlackListRepository);
        filterChain = mock(GatewayFilterChain.class);
        filter = new AuthenticationFilter(authenticationService, tokenProvider);
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
        // given
        Claims mockClaims = Jwts.claims();
        mockClaims.setSubject(EMAIL);
        mockClaims.put("userType", "NORMAL");
        mockClaims.put("userStatus", "ACTIVE");

        ServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/v1/products") // 화이트리스트에 포함되지 않은 URL
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + VALID_TOKEN)
                        .build()
        );

        // when
        when(tokenProvider.resolveToken(any())).thenReturn(VALID_TOKEN);
        when(tokenBlackListRepository.isBlackList(VALID_TOKEN)).thenReturn(Mono.just(false));
        when(tokenProvider.parseClaims(VALID_TOKEN)).thenReturn(mockClaims);
        when(filterChain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());

        // then
        StepVerifier.create(filter.filter(exchange, filterChain))
                .verifyComplete();

        verify(filterChain, times(1)).filter(any(ServerWebExchange.class));
    }

    @Test
    @DisplayName("일반 유저가 정상 API 접근 허용")
    void 일반_유저가_정상_API_접근_허용() {
        Claims claims = createClaims(EMAIL, "NORMAL");

        when(tokenProvider.resolveToken(any())).thenReturn(VALID_TOKEN);
        when(tokenProvider.parseClaims(VALID_TOKEN)).thenReturn(claims);
        when(tokenBlackListRepository.isBlackList(VALID_TOKEN)).thenReturn(Mono.just(false));
        when(filterChain.filter(any())).thenReturn(Mono.empty());

        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/v1/users")
                .header(HttpHeaders.AUTHORIZATION, BEARER + VALID_TOKEN)
                .build()
        );

        Mono<Void> result = filter.filter(exchange, filterChain);

        StepVerifier.create(result)
                .verifyComplete();

        verify(filterChain, times(1)).filter(any(ServerWebExchange.class));
    }


    @Test
    @DisplayName("관리자 권한 없이 관리자 API에 접근하면 401 Unauthorized를 반환한다")
    void 관리자_권한_없으면_인가_실패() {
        // given
        Claims claims = createClaims(EMAIL, "NORMAL");

        when(tokenProvider.resolveToken(any())).thenReturn(VALID_TOKEN);
        when(tokenBlackListRepository.isBlackList(VALID_TOKEN)).thenReturn(Mono.just(false));
        when(tokenProvider.parseClaims(VALID_TOKEN)).thenReturn(claims);

        ServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/v1/admin/test")
                        .header(HttpHeaders.AUTHORIZATION, BEARER + VALID_TOKEN)
                        .build()
        );

        // when
        Mono<Void> result = filter.filter(exchange, filterChain);

        // then
        StepVerifier.create(result)
                .then(() -> assertThat(exchange.getResponse().getStatusCode())
                        .isEqualTo(HttpStatus.UNAUTHORIZED))
                .verifyComplete();

        verify(filterChain, never()).filter(any());
    }

    private Claims createClaims(final String email, final String role) {
        Claims claims = mock(Claims.class);
        when(claims.getSubject()).thenReturn(email);
        when(claims.get("userType", String.class)).thenReturn(role);
        when(claims.get("userStatus", String.class)).thenReturn("ACTIVE");
        return claims;
    }

}