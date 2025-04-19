package org.sangyunpark.gateway.filter.utils;

import org.sangyunpark.gateway.filter.dto.CachedUser;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

@Component
public class RequestMutationUtils {

    public final String X_USER_EMAIL = "X-User-Email";
    public final String X_USER_TYPE = "X-User-Type";
    public final String X_USER_STATUS = "X-User-Status";

    public ServerWebExchange mutateRequestWithClaims(final ServerWebExchange exchange, final CachedUser user) {
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