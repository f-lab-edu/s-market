package org.sangyunpark.gateway.filter.vo;

import org.springframework.http.HttpMethod;

public record WhiteListVo(HttpMethod method, String path) {
}
