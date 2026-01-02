package dev.kingscode.ecommerce_api.dto;

import org.springframework.http.HttpStatus;

import dev.kingscode.ecommerce_api.exception.ErrorCode;
import lombok.Getter;

@Getter
public abstract class ApiException extends RuntimeException {
    private final ErrorCode errorCode;
    private final HttpStatus httpStatus;

    protected ApiException(String message, ErrorCode errorCode) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = errorCode.getDefaultHttpStatus();
    }

    protected ApiException(String message, ErrorCode errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.httpStatus = errorCode.getDefaultHttpStatus();
    }

    protected ApiException(String message, ErrorCode errorCode, HttpStatus customHttpStatus) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = customHttpStatus;
    }

}
