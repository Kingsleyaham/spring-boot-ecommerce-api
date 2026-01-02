package dev.kingscode.ecommerce_api.exception;

import dev.kingscode.ecommerce_api.dto.ApiException;

// @ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends ApiException {
    public ResourceNotFoundException(String message) {
        super(message, ErrorCode.RESOURCE_NOT_FOUND);
    }

    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s not found with %s: '%s'", resourceName, fieldName, fieldValue),
                ErrorCode.RESOURCE_NOT_FOUND);
    }
}
