package com.sangyunpark.user.constant.message;

public enum ExceptionMessages {

    EXCEPTION_USER_DUPLICATE("이미 존재하는 회원입니다.");

    private final String message;

    ExceptionMessages(String message) {
        this.message = message;
    }

    public String message() {
        return message;
    }
}