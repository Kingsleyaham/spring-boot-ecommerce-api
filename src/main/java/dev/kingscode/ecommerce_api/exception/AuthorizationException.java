package dev.kingscode.ecommerce_api.exception;

import dev.kingscode.ecommerce_api.dto.ApiException;

public class AuthorizationException extends ApiException {
    public AuthorizationException(String message) {
        super(message, ErrorCode.ACCESS_DENIED);
    }

    public AuthorizationException(String message, ErrorCode errorCode) {
        super(message, errorCode);
    }
}
