package com.sangyunpark.user.domain.dto.request;


import jakarta.validation.constraints.NotBlank;

public record UserAddressRequestDto(
        @NotBlank
        String receiverName,

        @NotBlank
        String address,

        boolean defaultAddress
) {
}
