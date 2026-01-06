package dev.kingscode.ecommerce_api.exception;

import java.util.List;
import java.util.StringJoiner;

import dev.kingscode.ecommerce_api.dto.ApiException;

public class InvalidFileException extends ApiException {

    // Default allowed types if none is specified (The default here are for images)
    private static final List<String> DEFAULT_ALLOWED_TYPES = List.of("image/jpeg", "image/png", "image/webp");

    public InvalidFileException(String message) {
        super(message, ErrorCode.INVALID_INPUT);
    }

    public InvalidFileException(String message, ErrorCode errorCode) {
        super(message, errorCode);
    }

    public static InvalidFileException fileRequired() {
        return new InvalidFileException("File is required");
    }

    public static InvalidFileException fileTooLarge(long maxSizeMb) {
        return new InvalidFileException("File size must not exceed " + maxSizeMb + "MB", ErrorCode.FILE_TOO_LARGE);
    }

    public static InvalidFileException invalidType() {
        return invalidType(DEFAULT_ALLOWED_TYPES);
    }

    public static InvalidFileException invalidType(List<String> allowedTypes) {
        StringJoiner joiner = new StringJoiner(", ");
        allowedTypes.forEach(joiner::add);
        String message = "Invalid file type. Allowed types are: " + joiner.toString();

        return new InvalidFileException(message, ErrorCode.INVALID_FILE_TYPE); // Using a more specific error code
    }

}
