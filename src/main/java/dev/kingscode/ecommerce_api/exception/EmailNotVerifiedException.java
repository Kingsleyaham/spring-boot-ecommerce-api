package dev.kingscode.ecommerce_api.exception;

import lombok.Getter;

@Getter
public class EmailNotVerifiedException extends AuthenticationException {
    private final String email;
    private final ErrorCode errorCode;

    public EmailNotVerifiedException(String message, ErrorCode errorCode, String email) {
        super(message);
        this.errorCode = errorCode;
        this.email = email;
    }

    public EmailNotVerifiedException(String message, ErrorCode errorCode, String email, Throwable cause) {
        super(message);
        this.errorCode = errorCode;
        this.email = email;
    }
}