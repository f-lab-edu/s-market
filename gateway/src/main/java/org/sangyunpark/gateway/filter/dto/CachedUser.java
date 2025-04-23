package org.sangyunpark.gateway.filter.dto;

public record CachedUser(
        String email,
        String userType,
        String userStatus
) {
}
