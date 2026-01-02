package dev.kingscode.ecommerce_api.exception;

import dev.kingscode.ecommerce_api.dto.ApiException;

public class AuthenticationException extends ApiException {

    public AuthenticationException(String message) {
        super(message, ErrorCode.AUTHENTICATION_FAILED);
    }

}
