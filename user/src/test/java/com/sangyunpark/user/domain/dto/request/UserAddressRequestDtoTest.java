package com.sangyunpark.user.domain.dto.request;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;


@SuppressWarnings("NonAsciiCharacters")
class UserAddressRequestDtoTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        try(ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    @Test
    void 모든값이_정상일때_검증에_성공한다() {
        // given
        UserAddressRequestDto dto = new UserAddressRequestDto("박상윤", "경기도 부천시", true);

        // when
        Set<ConstraintViolation<UserAddressRequestDto>> violations = validator.validate(dto);

        // then
        assertThat(violations).isEmpty();
    }

    @Test
    void 수령인_이름이_비어있으면_검증에_실패한다() {
        // given
        UserAddressRequestDto dto = new UserAddressRequestDto(" ", "경기도 부천시", true);

        // when, then
        assertViolation(dto,"receiverName");
    }

    @Test
    void 주소가_비어있으면_검증에_실패한다() {
        // given
        UserAddressRequestDto dto = new UserAddressRequestDto("박상윤", "", true);

        // when, then
        assertViolation(dto,"address");
    }

    private void assertViolation(UserAddressRequestDto dto, String field) {
        Set<ConstraintViolation<UserAddressRequestDto>> violations = validator.validate(dto);
        assertThat(violations)
                .hasSize(1)
                .anyMatch(v ->
                        v.getPropertyPath().toString().equals(field)
                );
    }

}