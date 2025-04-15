package org.sangyunpark.gateway.filter.utils;

import org.sangyunpark.gateway.filter.dto.CachedUser;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;

public class RequestMutationUtils {

    public static final String X_USER_EMAIL = "X-User-Email";
    public static final String X_USER_TYPE = "X-User-Type";
    public static final String X_USER_STATUS = "X-User-Status";

    public static ServerWebExchange mutateRequestWithClaims(final ServerWebExchange exchange, final CachedUser user) {
        final ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                .header(X_USER_EMAIL, user.email())
                .header(X_USER_TYPE, user.userType())
                .header(X_USER_STATUS, user.userStatus())
                .build();

        final ServerWebExchange mutatedExchange = exchange.mutate()
                .request(mutatedRequest)
                .build();

        return mutatedExchange.mutate()
                .request(mutatedRequest)
                .build();
    }
}