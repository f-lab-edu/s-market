package com.sangyunpark.user.domain.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UserSelectByEmailRequestDto(
        @Email
        @NotBlank
        String email
) {
}
