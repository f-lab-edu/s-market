package org.sangyunpark.gateway.application;

import lombok.RequiredArgsConstructor;
import org.sangyunpark.gateway.infrastructure.redis.TokenBlackListRepository;
import org.sangyunpark.gateway.jwt.TokenValidator;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final TokenValidator tokenValidator;
    private final TokenBlackListRepository tokenBlackListRepository;

    public Mono<Boolean> isValidToken(final String token) {
        if(!tokenValidator.validateToken(token)) {
            return Mono.just(false);
        }

        return tokenBlackListRepository.isBlackList(token)
                .map(isBlackList -> isBlackList);
    }
}
