package com.sangyunpark.product.constant;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    CATEGORY_NOT_FOUND("CATEGORY_NOT_FOUND", HttpStatus.NOT_FOUND),
    CATEGORY_SELF_PARENT("CATEGORY_SELF_PARENT", HttpStatus.BAD_REQUEST),

    INTERNAL_SERVER_ERROR("INTERNAL_SERVER_ERROR", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_REQUEST("INVALID_REQUEST", HttpStatus.BAD_REQUEST),
    PRODUCT_NOT_FOUND("PRODUCT_NOT_FOUND", HttpStatus.NOT_FOUND);

    private final String code;
    private final HttpStatus status;

    ErrorCode(String code, HttpStatus status) {
        this.code = code;
        this.status = status;
    }
}