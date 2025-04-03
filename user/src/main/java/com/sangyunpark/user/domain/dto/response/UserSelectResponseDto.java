package com.sangyunpark.user.domain.dto.response;

import com.sangyunpark.user.domain.vo.RegisterType;
import com.sangyunpark.user.domain.vo.UserStatus;
import com.sangyunpark.user.domain.vo.UserType;
import lombok.Builder;

@Builder
public record UserSelectResponseDto(
        Long id,
        String email,
        String username,
        UserType userType,
        UserStatus userStatus,
        RegisterType registerType,
        String phoneNumber
) {
}
