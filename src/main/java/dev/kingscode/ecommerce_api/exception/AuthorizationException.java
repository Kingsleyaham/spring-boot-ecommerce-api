package dev.kingscode.ecommerce_api.exception;

import org.springframework.http.HttpStatus;

import dev.kingscode.ecommerce_api.dto.ApiException;

public class AuthorizationException extends ApiException {
    public AuthorizationException(String message) {
        super(message, ErrorCode.ACCESS_DENIED, HttpStatus.FORBIDDEN);
    }
}
