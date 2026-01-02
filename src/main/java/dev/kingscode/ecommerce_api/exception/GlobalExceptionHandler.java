package dev.kingscode.ecommerce_api.exception;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.NoHandlerFoundException;

import dev.kingscode.ecommerce_api.dto.ApiException;
import dev.kingscode.ecommerce_api.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final HttpServletRequest httpServletRequest;

    @SuppressWarnings("null")
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErrorResponse> handleApiException(ApiException ex, WebRequest request) {

        log.warn("API Exception [{}]: {}", ex.getErrorCode().getCode(), ex.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(ex, getRequestPath(request));

        return ResponseEntity.status(ex.getHttpStatus()).body(errorResponse);
    }

    // Handle Spring validation errors
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(
            MethodArgumentNotValidException ex, WebRequest request) {

        List<ErrorResponse.FieldErrorDetail> fieldErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(this::mapToFieldErrorDetail)
                .collect(Collectors.toList());

        log.warn("Validation failed: {}", fieldErrors);

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST,
                ErrorCode.VALIDATION_FAILED,
                "Validation failed for one or more fields",
                getRequestPath(request),
                fieldErrors);

        return ResponseEntity.badRequest().body(errorResponse);
    }

    // Handle constraint violations (@PathVariable, @RequestParam)
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(
            ConstraintViolationException ex, WebRequest request) {

        List<ErrorResponse.FieldErrorDetail> fieldErrors = ex.getConstraintViolations()
                .stream()
                .map(this::mapToFieldErrorDetail)
                .collect(Collectors.toList());

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST,
                ErrorCode.CONSTRAINT_VIOLATION,
                "Constraint violation in request parameters",
                getRequestPath(request),
                fieldErrors);

        return ResponseEntity.badRequest().body(errorResponse);
    }

    // Handle 404 - Route not found
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoHandlerFound(
            NoHandlerFoundException ex, WebRequest request) {

        log.warn("No handler found for {} {}", ex.getHttpMethod(), ex.getRequestURL());

        String message = String.format("No endpoint found for %s %s",
                ex.getHttpMethod(), ex.getRequestURL());

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.NOT_FOUND,
                ErrorCode.ENDPOINT_NOT_FOUND,
                message,
                getRequestPath(request));

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    // Handle method not allowed
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotSupported(
            HttpRequestMethodNotSupportedException ex, WebRequest request) {

        String supportedMethods = ex.getSupportedMethods() != null
                ? String.join(", ", ex.getSupportedMethods())
                : "";

        String message = String.format("Method %s is not supported. Supported methods: %s",
                ex.getMethod(), supportedMethods);

        Map<String, Object> details = new HashMap<>();
        details.put("supportedMethods", supportedMethods);

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.METHOD_NOT_ALLOWED,
                ErrorCode.METHOD_NOT_ALLOWED,
                message,
                getRequestPath(request));

        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(errorResponse);
    }

    // Handle authentication/authorization errors
    @ExceptionHandler({ AuthenticationException.class, BadCredentialsException.class })
    public ResponseEntity<ErrorResponse> handleAuthenticationException(
            Exception ex, WebRequest request) {

        log.warn("Authentication failed: {}", ex.getMessage());
        String errorMessage = ex.getMessage() != null ? ex.getMessage() : "Authentication failed";

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.UNAUTHORIZED,
                ErrorCode.AUTHENTICATION_FAILED,
                errorMessage,
                getRequestPath(request));

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(
            AccessDeniedException ex, WebRequest request) {

        log.warn("Access denied: {}", ex.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.FORBIDDEN,
                ErrorCode.ACCESS_DENIED,
                "You don't have permission to access this resource",
                getRequestPath(request));

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
    }

    // Handle other Spring exceptions
    @ExceptionHandler({
            HttpMessageNotReadableException.class,
            MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class,
            BindException.class
    })
    public ResponseEntity<ErrorResponse> handleBadRequestExceptions(
            Exception ex, WebRequest request) {

        log.warn("Bad request: {}", ex.getMessage());

        ErrorCode errorCode = determineErrorCode(ex);
        String message = determineErrorMessage(ex);

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST,
                errorCode,
                message,
                getRequestPath(request));

        return ResponseEntity.badRequest().body(errorResponse);
    }

    // Handle file upload size limit
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleMaxUploadSizeExceeded(
            MaxUploadSizeExceededException ex, WebRequest request) {

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.PAYLOAD_TOO_LARGE,
                ErrorCode.FILE_TOO_LARGE,
                "File size exceeds maximum limit",
                getRequestPath(request));

        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(errorResponse);
    }

    // Handle Illegal argument exception
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(
            IllegalArgumentException ex, WebRequest request) {

        log.warn("Illegal argument: {}", ex.getMessage());

        // Determine if it's a parameter validation issue or general illegal argument
        ErrorCode errorCode = determineIllegalArgumentErrorCode(ex);
        String message = ex.getMessage() != null
                ? ex.getMessage()
                : "Invalid argument provided";

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST,
                errorCode,
                message,
                getRequestPath(request));

        return ResponseEntity.badRequest().body(errorResponse);
    }

    // Catch-all for any unhandled exceptions
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAllUncaughtException(
            Exception ex, WebRequest request) {

        log.error("Unhandled exception occurred", ex);

        Map<String, Object> details = null;
        if (isDevelopmentEnvironment()) {
            details = new HashMap<>();
            details.put("exception", ex.getClass().getName());
            details.put("message", ex.getMessage());
            details.put("cause", ex.getCause() != null ? ex.getCause().toString() : null);
            // Don't include stack trace in response, just log it
        }

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                ErrorCode.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred",
                getRequestPath(request));

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    // Helper methods
    private String getRequestPath(WebRequest request) {
        if (request instanceof ServletWebRequest) {
            return ((ServletWebRequest) request).getRequest().getRequestURI();
        }
        return httpServletRequest.getRequestURI();
    }

    private boolean isDevelopmentEnvironment() {
        String profile = System.getenv("SPRING_PROFILES_ACTIVE");
        return profile != null && (profile.contains("dev") || profile.contains("local"));
    }

    private ErrorResponse.FieldErrorDetail mapToFieldErrorDetail(FieldError fieldError) {
        return new ErrorResponse.FieldErrorDetail(
                fieldError.getField(),
                fieldError.getDefaultMessage(),
                fieldError.getRejectedValue());
    }

    private ErrorResponse.FieldErrorDetail mapToFieldErrorDetail(ConstraintViolation<?> violation) {
        String field = violation.getPropertyPath().toString();
        // Extract field name from property path (e.g., "createUser.user.email" ->
        // "email")
        if (field.contains(".")) {
            field = field.substring(field.lastIndexOf('.') + 1);
        }

        return new ErrorResponse.FieldErrorDetail(
                field,
                violation.getMessage(),
                violation.getInvalidValue());
    }

    private ErrorCode determineErrorCode(Exception ex) {
        if (ex instanceof HttpMessageNotReadableException) {
            return ErrorCode.INVALID_INPUT;
        } else if (ex instanceof MethodArgumentTypeMismatchException) {
            return ErrorCode.TYPE_MISMATCH;
        } else if (ex instanceof MissingServletRequestParameterException) {
            return ErrorCode.MISSING_PARAMETER;
        } else if (ex instanceof BindException) {
            return ErrorCode.BIND_ERROR;
        }
        return ErrorCode.INVALID_INPUT;
    }

    @SuppressWarnings("null")
    private String determineErrorMessage(Exception ex) {
        if (ex instanceof HttpMessageNotReadableException) {
            return "Invalid request format";
        } else if (ex instanceof MethodArgumentTypeMismatchException typeMismatchEx) {
            return String.format("Parameter '%s' should be of type %s",
                    typeMismatchEx.getName(),
                    typeMismatchEx.getRequiredType() != null
                            ? typeMismatchEx.getRequiredType().getSimpleName()
                            : "unknown");
        } else if (ex instanceof MissingServletRequestParameterException missingParamEx) {
            return String.format("Required parameter '%s' is missing",
                    missingParamEx.getParameterName());
        }
        return "Invalid request";
    }

    private ErrorCode determineIllegalArgumentErrorCode(IllegalArgumentException ex) {
        String message = ex.getMessage() != null ? ex.getMessage().toLowerCase() : "";

        // Check common patterns to determine specific error code
        if (message.contains("must not be null") ||
                message.contains("cannot be null") ||
                message.contains("null")) {
            return ErrorCode.INVALID_INPUT;
        } else if (message.contains("invalid") ||
                message.contains("not valid") ||
                message.contains("not supported")) {
            return ErrorCode.INVALID_PARAMETER;
        } else if (message.contains("empty") ||
                message.contains("blank")) {
            return ErrorCode.INVALID_INPUT;
        } else {
            return ErrorCode.ILLEGAL_ARGUMENT;
        }
    }

}
