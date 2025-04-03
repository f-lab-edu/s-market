package com.sangyunpark.user.constant;

public enum ExceptionMessages {

    EXCEPTION_NOT_FOUND_USER("존재하지 않는 회원입니다.");

    private final String message;

    ExceptionMessages(String message) {
        this.message = message;
    }

    public String message() {
        return message;
    }
}
