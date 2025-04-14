package org.sangyunpark.gateway.filter.utils;

import org.sangyunpark.gateway.constant.code.ErrorCode;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

public class HttpResponseUtils {

    private static final String JSON_CONTENT_TYPE = "application/json";
    private static final String ERROR_RESPONSE_FORMAT = "{\"code\":\"%s\"}";

    public static Mono<Void> unauthorized(final ServerWebExchange exchange, final ErrorCode errorCode) {
        final ServerHttpResponse response = exchange.getResponse();
        setJsonHeaders(response, errorCode);
        final byte[] bytes = createErrorBody(errorCode).getBytes();
        final DataBuffer buffer = response.bufferFactory().wrap(bytes);
        return response.writeWith(Mono.just(buffer));
    }

    public static void setJsonHeaders(ServerHttpResponse response, ErrorCode errorCode) {
        response.setStatusCode(errorCode.getStatus());
        response.getHeaders().add(HttpHeaders.CONTENT_TYPE, JSON_CONTENT_TYPE);
    }

    private static String createErrorBody(ErrorCode errorCode) {
        return ERROR_RESPONSE_FORMAT.formatted(errorCode.getCode());
    }
}