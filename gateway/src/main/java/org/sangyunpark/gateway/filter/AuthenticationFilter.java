package org.sangyunpark.gateway.filter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sangyunpark.gateway.application.AuthenticationService;
import org.sangyunpark.gateway.constant.code.ErrorCode;
import org.sangyunpark.gateway.constant.code.UserType;
import org.sangyunpark.gateway.filter.dto.CachedUser;
import org.sangyunpark.gateway.filter.matcher.UrlMatcher;
import org.sangyunpark.gateway.filter.matcher.WhitelistMatcher;
import org.sangyunpark.gateway.filter.utils.HttpResponseUtils;
import org.sangyunpark.gateway.filter.utils.RequestMutationUtils;
import org.sangyunpark.gateway.jwt.TokenProvider;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthenticationFilter implements GlobalFilter {

    private final AuthenticationService authenticationService;
    private final TokenProvider tokenProvider;
    private final WhitelistMatcher whitelistMatcher;
    private final HttpResponseUtils httpResponseUtils;
    private final UrlMatcher urlMatcher;
    private final RequestMutationUtils requestMutationUtils;

    @Override
    public Mono<Void> filter(final ServerWebExchange exchange, final GatewayFilterChain chain) {
        final ServerHttpRequest request = exchange.getRequest();

        if (whitelistMatcher.isWhitelisted(request)) {
            return chain.filter(exchange);
        }

        final String token = tokenProvider.resolveToken(request);
        if (token.isBlank()) {
            return httpResponseUtils.unauthorized(exchange, ErrorCode.INVALID_TOKEN);
        }

        if (urlMatcher.isLogOutUrl(request)) {
            return handleLogoutRequest(token, chain, exchange);
        }

        return handleAuthorizedRequest(token, chain, exchange);
    }

    private Mono<Void> handleLogoutRequest(final String token, final GatewayFilterChain chain, final ServerWebExchange exchange) {
        return authenticationService.logout(token)
                .then(chain.filter(exchange))
                .onErrorResume(e -> httpResponseUtils.unauthorized(exchange, ErrorCode.INVALID_TOKEN));
    }

    private Mono<Void> handleAuthorizedRequest(final String token, final GatewayFilterChain chain, final ServerWebExchange exchange) {
        return authenticationService.getAuthenticatedUser(token)
                .flatMap(cachedUser -> authorizeAndMutateRequest(exchange, chain, cachedUser))
                .onErrorResume(e -> httpResponseUtils.unauthorized(exchange, ErrorCode.INVALID_TOKEN));
    }

    private Mono<Void> authorizeAndMutateRequest(final ServerWebExchange exchange, final GatewayFilterChain chain, final CachedUser user) {
        final ServerHttpRequest request = exchange.getRequest();
        final String role = user.userType();

        if (urlMatcher.isAdminUrl(request) && !UserType.ADMIN.name().equals(role)) {
            return httpResponseUtils.unauthorized(exchange, ErrorCode.INVALID_TOKEN);
        }

        return chain.filter(requestMutationUtils.mutateRequestWithClaims(exchange, user));
    }
}