package org.sangyunpark.gateway.application;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.sangyunpark.gateway.infrastructure.redis.TokenBlackListRepository;
import org.sangyunpark.gateway.jwt.TokenProvider;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final TokenProvider tokenProvider;
    private final TokenBlackListRepository tokenBlackListRepository;

    public Mono<Claims> extractClaims(final String token) {
        return tokenBlackListRepository.isBlackList(token)
                .flatMap(isBlackList -> {
                    if(isBlackList) {
                       return  Mono.error(new RuntimeException());
                    }
                    return Mono.fromCallable(() -> tokenProvider.parseClaims(token));
                });
    }
}