package dev.kingscode.ecommerce_api.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;

@Getter
public enum ErrorCode {
    // 400 Bad Request
    VALIDATION_FAILED("VALIDATION_FAILED", "Validation failed", HttpStatus.BAD_REQUEST),
    INVALID_INPUT("INVALID_INPUT", "Invalid input format", HttpStatus.BAD_REQUEST),
    CONSTRAINT_VIOLATION("CONSTRAINT_VIOLATION", "Constraint violation", HttpStatus.BAD_REQUEST),
    TYPE_MISMATCH("TYPE_MISMATCH", "Type mismatch", HttpStatus.BAD_REQUEST),
    MISSING_PARAMETER("MISSING_PARAMETER", "Required parameter missing", HttpStatus.BAD_REQUEST),
    FILE_TOO_LARGE("FILE_TOO_LARGE", "File size exceeds limit", HttpStatus.PAYLOAD_TOO_LARGE),
    BIND_ERROR("BIND_ERROR", "Binding error", HttpStatus.BAD_REQUEST),
    ILLEGAL_ARGUMENT("ILLEGAL_ARGUMENT", "Illegal argument provided", HttpStatus.BAD_REQUEST),
    INVALID_PARAMETER("INVALID_PARAMETER", "Invalid parameter value", HttpStatus.BAD_REQUEST),
    INVALID_TOKEN(
            "INVALID_TOKEN",
            "Invalid or already used verification token",
            HttpStatus.BAD_REQUEST),

    TOKEN_EXPIRED(
            "TOKEN_EXPIRED",
            "Verification token has expired",
            HttpStatus.BAD_REQUEST),

    // 401/403 Authentication/Authorization
    UNAUTHENTICATED("UNAUTHENTICATED", "Authentication required", HttpStatus.UNAUTHORIZED),
    AUTHENTICATION_FAILED("AUTHENTICATION_FAILED", "Authentication failed", HttpStatus.UNAUTHORIZED),
    ACCESS_DENIED("ACCESS_DENIED", "Access denied", HttpStatus.FORBIDDEN),
    INSUFFICIENT_PERMISSIONS("INSUFFICIENT_PERMISSIONS", "Insufficient permissions", HttpStatus.FORBIDDEN),
    EMAIL_NOT_VERIFIED(
            "EMAIL_NOT_VERIFIED",
            "Email address has not been verified",
            HttpStatus.FORBIDDEN),

    // 404 Not Found
    RESOURCE_NOT_FOUND("RESOURCE_NOT_FOUND", "Resource not found", HttpStatus.NOT_FOUND),
    ENDPOINT_NOT_FOUND("ENDPOINT_NOT_FOUND", "Endpoint not found", HttpStatus.NOT_FOUND),

    // 405 Method Not Allowed
    METHOD_NOT_ALLOWED("METHOD_NOT_ALLOWED", "HTTP method not allowed", HttpStatus.METHOD_NOT_ALLOWED),

    // 409 Conflict
    RESOURCE_CONFLICT("RESOURCE_CONFLICT", "Resource conflict", HttpStatus.CONFLICT),
    DUPLICATE_RESOURCE("DUPLICATE_RESOURCE", "Duplicate resource", HttpStatus.CONFLICT),
    EMAIL_ALREADY_VERIFIED(
            "EMAIL_ALREADY_VERIFIED",
            "Email address is already verified",
            HttpStatus.CONFLICT),

    // 422 Unprocessable Entity
    BUSINESS_RULE_VIOLATION("BUSINESS_RULE_VIOLATION", "Business rule violation", HttpStatus.UNPROCESSABLE_ENTITY),
    INVALID_STATE("INVALID_STATE", "Invalid state", HttpStatus.UNPROCESSABLE_ENTITY),

    // 429 Rate Limit
    RATE_LIMIT_EXCEEDED("RATE_LIMIT_EXCEEDED", "Rate limit exceeded", HttpStatus.TOO_MANY_REQUESTS),

    // 500 Internal Server Error
    INTERNAL_SERVER_ERROR("INTERNAL_SERVER_ERROR", "Internal server error", HttpStatus.INTERNAL_SERVER_ERROR),
    SERVICE_UNAVAILABLE("SERVICE_UNAVAILABLE", "Service unavailable", HttpStatus.SERVICE_UNAVAILABLE),
    DATABASE_ERROR("DATABASE_ERROR", "Database error", HttpStatus.INTERNAL_SERVER_ERROR),

    // File / Media errors
    FILE_UPLOAD_FAILED("FILE_UPLOAD_FAILED", "File upload failed", HttpStatus.INTERNAL_SERVER_ERROR),
    FILE_DELETE_FAILED("FILE_DELETE_FAILED", "File deletion failed", HttpStatus.INTERNAL_SERVER_ERROR),
    FILE_NOT_FOUND("FILE_NOT_FOUND", "File not found", HttpStatus.NOT_FOUND),
    INVALID_FILE_TYPE("INVALID_FILE_TYPE", "Invalid file type", HttpStatus.BAD_REQUEST),
    INVALID_FILE_SIZE("INVALID_FILE_SIZE", "Invalid file size", HttpStatus.PAYLOAD_TOO_LARGE),

    // Default/Uncategorized
    UNCATEGORIZED_EXCEPTION("UNCATEGORIZED_EXCEPTION", "Uncategorized exception", HttpStatus.INTERNAL_SERVER_ERROR);

    private final String code;
    private final String description;
    private final HttpStatus defaultHttpStatus;

    ErrorCode(String code, String description, HttpStatus defaultHttpStatus) {
        this.code = code;
        this.description = description;
        this.defaultHttpStatus = defaultHttpStatus;
    }

    public static ErrorCode fromHttpStatus(HttpStatus httpStatus) {
        return switch (httpStatus.value()) {
            case 400 -> ErrorCode.INVALID_INPUT;
            case 401 -> ErrorCode.UNAUTHENTICATED;
            case 403 -> ErrorCode.ACCESS_DENIED;
            case 404 -> ErrorCode.RESOURCE_NOT_FOUND;
            case 405 -> ErrorCode.METHOD_NOT_ALLOWED;
            case 409 -> ErrorCode.RESOURCE_CONFLICT;
            case 422 -> ErrorCode.BUSINESS_RULE_VIOLATION;
            case 429 -> ErrorCode.RATE_LIMIT_EXCEEDED;
            case 500 -> ErrorCode.INTERNAL_SERVER_ERROR;
            case 503 -> ErrorCode.SERVICE_UNAVAILABLE;
            default -> ErrorCode.UNCATEGORIZED_EXCEPTION;
        };
    }

}
