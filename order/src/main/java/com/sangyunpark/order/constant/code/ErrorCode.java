package com.sangyunpark.order.constant.code;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    PRODUCT_NOT_FOUND("PRODUCT_NOT_FOUND", HttpStatus.BAD_REQUEST),
    NOT_ENOUGH_STOCK("NOT_ENOUGH_STOCK", HttpStatus.BAD_REQUEST),
    ORDER_NOT_FOUND("ORDER_NOT_FOUND", HttpStatus.NOT_FOUND),
    INVALID_ORDER_STATUS("INVALID_ORDER_STATUS", HttpStatus.BAD_REQUEST),
    INTERNAL_SERVER_ERROR("INTERNAL_SERVER_ERROR", HttpStatus.INTERNAL_SERVER_ERROR);

    private final String code;
    private final HttpStatus status;

    ErrorCode(String code, HttpStatus status) {
        this.code = code;
        this.status = status;
    }
}
