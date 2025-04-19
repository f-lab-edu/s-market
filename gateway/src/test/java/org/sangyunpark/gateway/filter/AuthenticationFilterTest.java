package org.sangyunpark.gateway.filter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.sangyunpark.gateway.application.AuthenticationService;
import org.sangyunpark.gateway.constant.code.ErrorCode;
import org.sangyunpark.gateway.filter.dto.CachedUser;
import org.sangyunpark.gateway.filter.matcher.UrlMatcher;
import org.sangyunpark.gateway.filter.matcher.WhitelistMatcher;
import org.sangyunpark.gateway.filter.utils.HttpResponseUtils;
import org.sangyunpark.gateway.filter.utils.RequestMutationUtils;
import org.sangyunpark.gateway.infrastructure.redis.RedisTokenRepository;
import org.sangyunpark.gateway.jwt.TokenProvider;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@SuppressWarnings("NonAsciiCharacters")
class AuthenticationFilterTest {

    private final String BEARER = "Bearer ";
    private final String VALID_TOKEN = "valid-token";
    private final String INVALID_TOKEN = "invalid-token";
    private final String EMAIL = "user@example.com";
    private final String USER_TYPE = "userType";
    private final String USER_STATUS = "userStatus";

    private GatewayFilterChain filterChain;
    private AuthenticationFilter filter;
    private RedisTokenRepository redisTokenRepository;
    private TokenProvider tokenProvider;
    private AuthenticationService authenticationService;
    private WhitelistMatcher whitelistMatcher;
    private HttpResponseUtils httpResponseUtils;
    private UrlMatcher urlMatcher;
    private RequestMutationUtils requestMutationUtils;

    @BeforeEach
    void setUp() {
        redisTokenRepository = mock(RedisTokenRepository.class);
        tokenProvider = mock(TokenProvider.class);
        authenticationService = mock(AuthenticationService.class);
        filterChain = mock(GatewayFilterChain.class);
        whitelistMatcher = mock(WhitelistMatcher.class);
        httpResponseUtils = new HttpResponseUtils();
        urlMatcher = new UrlMatcher();
        requestMutationUtils = new RequestMutationUtils();
        filter = new AuthenticationFilter(authenticationService, tokenProvider, whitelistMatcher,httpResponseUtils, urlMatcher, requestMutationUtils);
    }

    @Test
    @DisplayName("화이트리스트 경로는 토큰 없이 통과")
    void 화이트리스트_경로는_토큰_없이_통과() {
        ServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.post("/api/v1/users").build()
        );

        when(filterChain.filter(exchange)).thenReturn(Mono.empty());
        when(whitelistMatcher.isWhitelisted(any(ServerHttpRequest.class))).thenReturn(true);

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

        when(tokenProvider.resolveToken(any(ServerHttpRequest.class))).thenReturn(" ");

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

        when(redisTokenRepository.isBlackList(anyString())).thenReturn(Mono.just(false));
        when(tokenProvider.resolveToken(any(ServerHttpRequest.class))).thenReturn(" ");

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
        CachedUser user = new CachedUser(EMAIL, USER_TYPE, USER_STATUS);

        ServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/v1/products") // 화이트리스트가 아닌 URL
                        .header(HttpHeaders.AUTHORIZATION, BEARER + VALID_TOKEN)
                        .build()
        );

        when(tokenProvider.resolveToken(any())).thenReturn(VALID_TOKEN);
        when(authenticationService.getAuthenticatedUser(VALID_TOKEN)).thenReturn(Mono.just(user));
        when(filterChain.filter(any())).thenReturn(Mono.empty());

        // when, then
        StepVerifier.create(filter.filter(exchange, filterChain))
                .verifyComplete();

        verify(filterChain, times(1)).filter(any());
    }

    @Test
    @DisplayName("일반 유저가 정상 API 접근 허용")
    void 일반_유저가_정상_API_접근_허용() {
        // given
        CachedUser user = new CachedUser(EMAIL, USER_TYPE, USER_STATUS);

        when(tokenProvider.resolveToken(any())).thenReturn(VALID_TOKEN);
        when(authenticationService.getAuthenticatedUser(VALID_TOKEN)).thenReturn(Mono.just(user));
        when(filterChain.filter(any())).thenReturn(Mono.empty());

        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/v1/users") // 관리자 전용 X
                        .header(HttpHeaders.AUTHORIZATION, BEARER + VALID_TOKEN)
                        .build()
        );

        // when
        Mono<Void> result = filter.filter(exchange, filterChain);

        // then
        StepVerifier.create(result).verifyComplete();
        verify(filterChain, times(1)).filter(any(ServerWebExchange.class));
    }


    @Test
    @DisplayName("관리자 권한 없이 관리자 API에 접근하면 401 Unauthorized를 반환한다")
    void 관리자_권한_없으면_인가_실패() {
        // given
        CachedUser user = new CachedUser(EMAIL, USER_TYPE, USER_STATUS);

        when(tokenProvider.resolveToken(any())).thenReturn(VALID_TOKEN);
        when(authenticationService.getAuthenticatedUser(VALID_TOKEN)).thenReturn(Mono.just(user));

        ServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/v1/admin/test") // 관리자 전용 URL
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
}