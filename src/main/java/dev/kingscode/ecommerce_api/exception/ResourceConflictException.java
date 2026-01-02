package dev.kingscode.ecommerce_api.exception;

import dev.kingscode.ecommerce_api.dto.ApiException;

// @ResponseStatus(HttpStatus.CONFLICT)
public class ResourceConflictException extends ApiException {

    public ResourceConflictException(String message) {
        super(message, ErrorCode.RESOURCE_CONFLICT);
    }

    public ResourceConflictException(String resourceName, String conflictDetails) {
        super(String.format("%s conflict: %s", resourceName, conflictDetails), ErrorCode.RESOURCE_CONFLICT);
    }

}
