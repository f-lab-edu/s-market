package com.sangyunpark.user.domain.dto.response;

import com.sangyunpark.user.constant.enums.RegisterType;
import com.sangyunpark.user.constant.enums.UserStatus;
import com.sangyunpark.user.constant.enums.UserType;
import lombok.Builder;

@Builder
public record UserSelectResponseDto(
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
