package com.sangyunpark.user.application.mapper;

import com.sangyunpark.user.domain.dto.request.UserSignupRequestDto;
import com.sangyunpark.user.domain.entity.User;
import com.sangyunpark.user.domain.entity.UserAddress;
import com.sangyunpark.user.constant.enums.UserStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserMapper {

    private static final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public User toEntity(UserSignupRequestDto dto) {

        UserAddress userAddress = UserAddressMapper.toEntity(dto.shippingInfo());

        User user = User.builder()
                .email(dto.email())
                .username(dto.username())
                .password(passwordEncoder.encode(dto.password()))
                .registerType(dto.registerType())
                .phoneNumber(dto.phoneNumber())
                .userType(dto.userType())
                .userStatus(UserStatus.ACTIVE)
                .build();

        user.addUserAddress(userAddress);

        return user;
    }
}
