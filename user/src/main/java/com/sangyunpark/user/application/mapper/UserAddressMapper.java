package com.sangyunpark.user.application.mapper;

import com.sangyunpark.user.domain.dto.request.UserAddressRequestDto;
import com.sangyunpark.user.domain.entity.UserAddress;

public class UserAddressMapper {

    public static UserAddress toEntity(final UserAddressRequestDto dto) {
        return UserAddress.builder()
                .address(dto.address())
                .defaultAddress(dto.defaultAddress())
                .receiverName(dto.receiverName())
                .build();
    }
}
