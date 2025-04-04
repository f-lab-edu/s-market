package com.sangyunpark.user.constant.code;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    USER_DUPLICATE("user_duplicate",HttpStatus.CONFLICT),
    USER_NOT_FOUND("user_not_found", HttpStatus.NOT_FOUND),
    INVALID_REQUEST("invalid_request", HttpStatus.BAD_REQUEST),

    INTERNAL_SERVER_ERROR("internal_server_error", HttpStatus.INTERNAL_SERVER_ERROR);

    private final String code;
    private final HttpStatus status;

    ErrorCode(String code, HttpStatus status) {
        this.code = code;
        this.status = status;
    }
}
