package org.sangyunpark.gateway.filter.matcher;

import org.sangyunpark.gateway.filter.vo.WhiteListVo;
import org.springframework.http.HttpMethod;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class WhitelistMatcher {

    private final List<WhiteListVo> WHITELIST = List.of(
            new WhiteListVo(HttpMethod.POST, "/api/v1/users"),
            new WhiteListVo(HttpMethod.POST, "/api/v1/auth/login")
    );

    public boolean isWhitelisted(ServerHttpRequest request) {
        final HttpMethod method = request.getMethod();
        final String path = request.getURI().getPath();

        return WHITELIST.stream()
                .anyMatch(entry -> entry.method().equals(method) && entry.path().equals(path));
    }
}
