package org.sangyunpark.gateway.filter;

import lombok.RequiredArgsConstructor;
import org.sangyunpark.gateway.TokenValidator;
import org.sangyunpark.gateway.constant.code.ErrorCode;
import org.sangyunpark.gateway.filter.vo.WhiteListVo;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;


@Component
@RequiredArgsConstructor
public class AuthenticationFilter implements GlobalFilter, Ordered {

    private static final List<WhiteListVo> WHITELIST = List.of(
            new WhiteListVo(HttpMethod.POST, "/api/v1/users"),
            new WhiteListVo(HttpMethod.POST, "/api/v1/auth/login")
    );
    private static final String JSON_CONTENT_TYPE = "application/json";
    private static final String BEARER_PREFIX = "Bearer";
    private static final String ERROR_RESPONSE_FORMAT = "{\"code\":\"%s\"}";

    private final TokenValidator tokenValidator;

    @Override
    public Mono<Void> filter(final ServerWebExchange exchange, final GatewayFilterChain chain) {

        final ServerHttpRequest request = exchange.getRequest();

        if(isWhitelisted(request)) {
            return chain.filter(exchange);
        }

        final String token = resolveToken(exchange.getRequest());

        if(token == null || !tokenValidator.validateToken(token)) {
            return unauthorized(exchange);
        }

        return chain.filter(exchange);
    }

    private boolean isWhitelisted(ServerHttpRequest request) {
        final HttpMethod method = request.getMethod();
        final String path = request.getURI().getPath();

        return WHITELIST.stream()
                .anyMatch(entry -> entry.method().equals(method) && entry.path().equals(path));
    }

    private String resolveToken(final ServerHttpRequest request) {
        final String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            return null;
        }
        return authHeader.substring(BEARER_PREFIX.length());
    }

    private Mono<Void> unauthorized(final ServerWebExchange exchange) {
        final ServerHttpResponse response = exchange.getResponse();
        setJsonHeaders(response);

        final byte[] bytes = createErrorBody(ErrorCode.INVALID_TOKEN).getBytes();
        final DataBuffer buffer = response.bufferFactory().wrap(bytes);

        return response.writeWith(Mono.just(buffer));
    }

    private void setJsonHeaders(final ServerHttpResponse response) {
        response.setStatusCode(ErrorCode.INVALID_TOKEN.getStatus());
        response.getHeaders().add(HttpHeaders.CONTENT_TYPE, JSON_CONTENT_TYPE);
    }

    private String createErrorBody(final ErrorCode errorCode) {
        return ERROR_RESPONSE_FORMAT.formatted(errorCode.getCode());
    }

    @Override
    public int getOrder() {
        return -1;
    }
}
