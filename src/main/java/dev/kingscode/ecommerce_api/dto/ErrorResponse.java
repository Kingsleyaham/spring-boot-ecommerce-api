package dev.kingscode.ecommerce_api.dto;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import dev.kingscode.ecommerce_api.exception.ErrorCode;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC") Instant timestamp,
        int status, String error, String errorCode, String message,
        String path, List<FieldErrorDetail> fieldErrors, Map<String, Object> details) {

    // Constructor for ApiException
    public ErrorResponse(ApiException ex, String path) {
        this(
                Instant.now(),
                ex.getHttpStatus().value(),
                ex.getHttpStatus().getReasonPhrase(),
                ex.getErrorCode().getCode(),
                ex.getMessage(),
                path,
                null,
                null);
    }

    // Constructor for HttpStatus + ErrorCode
    public ErrorResponse(HttpStatus httpStatus, ErrorCode errorCode,
            String message, String path) {
        this(
                Instant.now(),
                httpStatus.value(),
                httpStatus.getReasonPhrase(),
                errorCode.getCode(),
                message,
                path,
                null,
                null);
    }

    // Constructor for validation errors
    public ErrorResponse(HttpStatus httpStatus, ErrorCode errorCode,
            String message, String path,
            List<FieldErrorDetail> fieldErrors) {
        this(
                Instant.now(),
                httpStatus.value(),
                httpStatus.getReasonPhrase(),
                errorCode.getCode(),
                message,
                path,
                fieldErrors,
                null);
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record FieldErrorDetail(
            String field,
            String message,
            Object rejectedValue) {
        public FieldErrorDetail(String field, String message) {
            this(field, message, null);
        }
    }
}