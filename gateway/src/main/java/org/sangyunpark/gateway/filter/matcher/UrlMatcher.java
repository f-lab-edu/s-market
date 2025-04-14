package org.sangyunpark.gateway.filter.matcher;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

@Component
public class UrlMatcher {
    private static final String ADMIN = "admin";
    private static final String LOGOUT_URL = "/api/v1/auth/logout";

    public static boolean isAdminUrl(ServerHttpRequest request) {
        return request.getURI().getPath().contains(ADMIN);
    }

    public static boolean isLogOutUrl(ServerHttpRequest request) {
        return request.getURI().getPath().contains(LOGOUT_URL);
    }
}
