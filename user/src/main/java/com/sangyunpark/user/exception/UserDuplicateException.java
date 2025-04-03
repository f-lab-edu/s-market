package com.sangyunpark.user.exception;

public class UserDuplicateException extends RuntimeException {
    public UserDuplicateException(String message)  {
        super(message);
    }
}
