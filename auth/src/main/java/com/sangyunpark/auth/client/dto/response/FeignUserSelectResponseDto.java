package com.sangyunpark.auth.client.dto.response;

import com.sangyunpark.auth.constants.enums.RegisterType;
import com.sangyunpark.auth.constants.enums.UserStatus;
import com.sangyunpark.auth.constants.enums.UserType;
import lombok.Builder;

@Builder
public record FeignUserSelectResponseDto(
        Long id,
        String email,
        String password,
        String username,
        UserType userType,
        UserStatus userStatus,
        RegisterType registerType,
        String phoneNumber
) {
}
