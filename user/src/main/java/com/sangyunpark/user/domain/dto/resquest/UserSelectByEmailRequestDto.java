package com.sangyunpark.user.domain.dto.resquest;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import static com.sangyunpark.user.constant.ValidationMessages.EMAIL_INVALID;
import static com.sangyunpark.user.constant.ValidationMessages.EMAIL_REQUIRED;

public record UserSelectByEmailRequestDto(
        @Email(message = EMAIL_INVALID)
        @NotBlank(message = EMAIL_REQUIRED)
        String email
) {
}
