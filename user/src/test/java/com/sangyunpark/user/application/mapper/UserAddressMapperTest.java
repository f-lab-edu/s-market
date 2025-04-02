package com.sangyunpark.user.application.mapper;

import com.sangyunpark.user.domain.dto.request.UserAddressRequestDto;
import com.sangyunpark.user.domain.entity.UserAddress;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserAddressMapperTest {

    @Test
    @DisplayName("UserAddressRequestDto를 UserAddress 엔티티로 정상 변환한다")
    void toEntity_변환성공() {
        // given
        UserAddressRequestDto dto = new UserAddressRequestDto(
                "박상윤",
                "경기도 부천시 소사구",
                true
        );

        // when
        UserAddress entity = UserAddressMapper.toEntity(dto);

        // then
        assertThat(entity.getReceiverName()).isEqualTo(dto.receiverName());
        assertThat(entity.getAddress()).isEqualTo(dto.address());
        assertThat(entity.getDefaultAddress()).isEqualTo(dto.defaultAddress());
    }
}