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
class AddressRequestDtoTest {

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
        AddressRequestDto dto = new AddressRequestDto("박상윤", "경기도 부천시", true);

        // when
        Set<ConstraintViolation<AddressRequestDto>> violations = validator.validate(dto);

        // then
        assertThat(violations).isEmpty();
    }

    @Test
    void 수령인_이름이_비어있으면_검증에_실패한다() {
        // given
        AddressRequestDto dto = new AddressRequestDto(" ", "경기도 부천시", true);

        // when
        Set<ConstraintViolation<AddressRequestDto>> violations = validator.validate(dto);

        // then
        assertViolation(dto,"receiverName", "수령인 이름은 필수입니다.");
    }

    @Test
    void 주소가_비어있으면_검증에_실패한다() {
        // given
        AddressRequestDto dto = new AddressRequestDto("박상윤", "", true);

        // when
        Set<ConstraintViolation<AddressRequestDto>> violations = validator.validate(dto);

        // then
        assertViolation(dto,"address", "주소는 필수입니다.");
    }

    private void assertViolation(AddressRequestDto dto, String field, String expectedMessage) {
        Set<ConstraintViolation<AddressRequestDto>> violations = validator.validate(dto);
        assertThat(violations)
                .hasSize(1)
                .anyMatch(v ->
                        v.getPropertyPath().toString().equals(field)
                                && v.getMessage().equals(expectedMessage)
                );
    }

}