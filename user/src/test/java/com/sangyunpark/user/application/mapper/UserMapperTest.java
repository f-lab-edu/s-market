package com.sangyunpark.user.application.mapper;

import com.sangyunpark.user.domain.dto.request.UserAddressRequestDto;
import com.sangyunpark.user.domain.dto.request.UserSignupRequestDto;
import com.sangyunpark.user.domain.entity.User;
import com.sangyunpark.user.domain.entity.UserAddress;
import com.sangyunpark.user.domain.vo.RegisterType;
import com.sangyunpark.user.domain.vo.UserType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserMapperTest {

    private final UserMapper userMapper = new UserMapper();

    @Test
    @DisplayName("UserSignupRequestDto를 User 엔티티로 변환한다")
    void UserSignupRequestDto에서_User로_변환_성공() {
        // given
        UserSignupRequestDto dto = new UserSignupRequestDto(
                "test@example.com",
                "상윤",
                "password123",
                "010-1234-5678",
                RegisterType.EMAIL,
                UserType.NORMAL,
                new UserAddressRequestDto("박상윤", "서울시 강남구", true)
        );

        // when
        User user = userMapper.toEntity(dto);

        // then
        assertThat(user.getEmail()).isEqualTo(dto.email());
        assertThat(user.getUsername()).isEqualTo(dto.username());
        assertThat(user.getPassword()).isNotEqualTo(dto.password());
        assertThat(user.getRegisterType()).isEqualTo(dto.registerType());
        assertThat(user.getPhoneNumber()).isEqualTo(dto.phoneNumber());
        assertThat(user.getUserType()).isEqualTo(dto.userType());
        assertThat(user.getUserStatus()).isNotNull();

        assertThat(user.getUserAddress()).hasSize(1);
        UserAddress address = user.getUserAddress().get(0);
        assertThat(address.getReceiverName()).isEqualTo(dto.shippingInfo().receiverName());
        assertThat(address.getAddress()).isEqualTo(dto.shippingInfo().address());
        assertThat(address.getDefaultAddress()).isEqualTo(dto.shippingInfo().defaultAddress());

        assertThat(address.getUser()).isEqualTo(user);
    }
}