package com.sangyunpark.user.domain.dto.request;


import jakarta.validation.constraints.NotBlank;

public record AddressRequestDto(
        @NotBlank(message = "수령인 이름은 필수입니다.")
        String receiverName,

        @NotBlank(message = "주소는 필수입니다.")
        String address,

        boolean defaultAddress
) {
}
