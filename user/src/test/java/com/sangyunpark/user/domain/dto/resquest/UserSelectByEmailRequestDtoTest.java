package com.sangyunpark.user.domain.dto.resquest;

import com.sangyunpark.user.constant.ValidationMessages;
import com.sangyunpark.user.domain.dto.resquest.UserSelectByEmailRequestDto;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class UserSelectByEmailRequestDtoTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("올바른 이메일이면 검증을 통과한다")
    void 유효한_이메일_검증_성공() {
        // given
        UserSelectByEmailRequestDto dto = new UserSelectByEmailRequestDto("test@example.com");

        // when
        Set<ConstraintViolation<UserSelectByEmailRequestDto>> violations = validator.validate(dto);

        // then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("이메일이 빈 값이면 검증에 실패한다")
    void 이메일_빈값_검증_실패() {
        // given
        UserSelectByEmailRequestDto dto = new UserSelectByEmailRequestDto("");

        // when
        Set<ConstraintViolation<UserSelectByEmailRequestDto>> violations = validator.validate(dto);

        // then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo(ValidationMessages.EMAIL_REQUIRED);
    }

    @Test
    @DisplayName("이메일 형식이 아니면 검증에 실패한다")
    void 이메일_형식_검증_실패() {
        // given
        UserSelectByEmailRequestDto dto = new UserSelectByEmailRequestDto("invalid-email");

        // when
        Set<ConstraintViolation<UserSelectByEmailRequestDto>> violations = validator.validate(dto);

        // then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo(ValidationMessages.EMAIL_INVALID);
    }
}