package com.sangyunpark.auth.presentation.dto.response;

import com.sangyunpark.auth.domain.vo.Token;
import lombok.Builder;

@Builder
public record LoginResponseDto(
        Token token
) {
}
