package com.sangyunpark.auth.domain.vo;

public record Token(
        String accessToken,
        String refreshToken
) {
}
