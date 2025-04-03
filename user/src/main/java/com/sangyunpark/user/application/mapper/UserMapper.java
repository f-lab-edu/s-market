package com.sangyunpark.user.application.mapper;

import com.sangyunpark.user.domain.dto.response.UserSelectResponseDto;
import com.sangyunpark.user.domain.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserSelectResponseDto toUserSelectResponseDto(User user) {
        return UserSelectResponseDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .username(user.getUsername())
                .userType(user.getUserType())
                .userStatus(user.getUserStatus())
                .registerType(user.getRegisterType())
                .phoneNumber(user.getPhoneNumber())
                .build();
    }
}
