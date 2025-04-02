package com.sangyunpark.user.domain.dto.request;


import com.sangyunpark.user.constant.ValidationMessages;
import jakarta.validation.constraints.NotBlank;

public record AddressRequestDto(
        @NotBlank(message = ValidationMessages.RECEIVER_NAME_REQUIRED)
        String receiverName,

        @NotBlank(message = ValidationMessages.ADDRESS_REQUIRED)
        String address,

        boolean defaultAddress
) {
}
