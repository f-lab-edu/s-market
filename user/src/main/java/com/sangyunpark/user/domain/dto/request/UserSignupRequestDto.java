package com.sangyunpark.user.domain.dto.request;

import com.sangyunpark.user.constant.enums.RegisterType;
import com.sangyunpark.user.constant.enums.UserType;
import jakarta.validation.constraints.*;

public record UserSignupRequestDto(

        @Email
        @NotBlank
        String email,

        @NotBlank
        @Size(min = 2, max = 10)
        String username,

        @NotBlank
        @Size(min = 8, max = 20)
        String password,

        @NotBlank
        @Pattern(regexp = "^\\d{2,3}-\\d{3,4}-\\d{4}$")
        String phoneNumber,

        @NotNull
        RegisterType registerType,

        @NotNull
        UserType userType,

        @NotNull
        UserAddressRequestDto shippingInfo
) {

}
