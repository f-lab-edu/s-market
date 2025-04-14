package org.sangyunpark.gateway.filter.utils;

import io.jsonwebtoken.Claims;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;

public class RequestMutationUtils {

    public static final String X_USER_EMAIL = "X-User-Email";
    public static final String X_USER_TYPE = "X-User-Type";
    public static final String X_USER_STATUS = "X-User-Status";
    private static final String USER_TYPE = "userType";
    private static final String USER_STATUS = "userStatus";

    public static ServerWebExchange mutateRequestWithClaims(ServerWebExchange exchange, Claims claims) {
        final ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                .header(X_USER_EMAIL, claims.getSubject())
                .header(X_USER_TYPE, claims.get(USER_TYPE, String.class))
                .header(X_USER_STATUS, claims.get(USER_STATUS, String.class))
                .build();

        final ServerWebExchange mutatedExchange = exchange.mutate()
                .request(mutatedRequest)
                .build();

        return mutatedExchange.mutate()
                .request(mutatedRequest)
                .build();
    }
}