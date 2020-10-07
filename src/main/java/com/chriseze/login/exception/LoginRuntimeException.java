package com.chriseze.login.exception;

public class LoginRuntimeException extends RuntimeException {
    private static final long serialVersionUID = 56718756104107988L;

    public LoginRuntimeException() {
        super();
    }

    public LoginRuntimeException(String message) {
        super(message);
    }

    public LoginRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

    public LoginRuntimeException(Throwable cause) {
        super(cause);
    }

    protected LoginRuntimeException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
