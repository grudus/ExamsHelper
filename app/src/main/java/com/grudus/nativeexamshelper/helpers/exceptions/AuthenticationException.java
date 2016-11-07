package com.grudus.nativeexamshelper.helpers.exceptions;


public class AuthenticationException extends RuntimeException {
    public AuthenticationException() {
    }

    public AuthenticationException(String detailMessage) {
        super(detailMessage);
    }

    public AuthenticationException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public AuthenticationException(Throwable throwable) {
        super(throwable);
    }
}
