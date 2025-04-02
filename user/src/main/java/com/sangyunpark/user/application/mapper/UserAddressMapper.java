package com.sangyunpark.user.application.mapper;

import com.sangyunpark.user.domain.dto.request.UserAddressRequestDto;
import com.sangyunpark.user.domain.entity.UserAddress;
import org.springframework.stereotype.Component;

@Component
public class UserAddressMapper {

    public static UserAddress toEntity(UserAddressRequestDto dto) {
        return UserAddress.builder()
                .address(dto.address())
                .defaultAddress(dto.defaultAddress())
                .receiverName(dto.receiverName())
                .build();
    }
}
