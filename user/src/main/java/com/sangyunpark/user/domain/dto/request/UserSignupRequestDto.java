package com.sangyunpark.user.domain.dto.request;

import com.sangyunpark.user.domain.vo.RegisterType;
import jakarta.validation.constraints.*;

import static com.sangyunpark.user.constant.ValidationMessages.*;

public record UserSignupRequestDto(

        @Email(message = EMAIL_INVALID)
        @NotBlank(message = EMAIL_REQUIRED)
        String email,

        @NotBlank(message = USERNAME_REQUIRED)
        @Size(min = 2, max = 10, message = USERNAME_LENGTH)
        String username,

        @NotBlank(message = PASSWORD_REQUIRED)
        @Size(min = 8, max = 20, message = PASSWORD_LENGTH)
        String password,

        @NotBlank(message = PHONE_REQUIRED)
        @Pattern(regexp = PHONE_REGEX, message = PHONE_INVALID)
        String phoneNumber,

        @NotNull(message = REGISTER_TYPE_REQUIRED)
        RegisterType registerType,

        @NotNull(message = SHIPPING_INFO_REQUIRED)
        AddressRequestDto shippingInfo
) {
}
