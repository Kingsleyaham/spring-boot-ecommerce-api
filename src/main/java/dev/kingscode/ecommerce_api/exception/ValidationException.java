package dev.kingscode.ecommerce_api.exception;

import dev.kingscode.ecommerce_api.dto.ApiException;

public class ValidationException extends ApiException {

    protected ValidationException(String message) {
        super(message, ErrorCode.VALIDATION_FAILED);
    }

}
