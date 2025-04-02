package com.sangyunpark.user.domain.dto.request;

import com.sangyunpark.user.domain.vo.RegisterType;
import jakarta.validation.constraints.*;

public record UserSignupRequestDto(

        @Email(message = "이메일 형식이 올바르지 않습니다.")
        @NotBlank(message = "이메일은 필수로 입력해야 합니다.")
        String email,

        @NotBlank(message = "사용자 이름은 필수로 작성해야 합니다.")
        @Size(min = 2, max = 10, message = "이름은 2자 이상 10자 이하로 입력해야 합니다.")
        String username,

        @NotBlank(message = "비밀번호는 필수로 입력해야 합니다.")
        @Size(min = 8, max = 20, message = "비밀번호는 8자 이상 20자 이하여야 합니다.")
        String password,

        @NotBlank(message = "전화번호는 필수입니다.")
        @Pattern(regexp = "^010-?\\d{4}-?\\d{4}$", message = "전화번호 형식이 올바르지 않습니다.")
        String phoneNumber,

        @NotNull(message = "가입 유형은 필수입니다.")
        RegisterType registerType,

        @NotNull(message = "주소 정보는 필수입니다.")
        AddressRequestDto address
) {
}
