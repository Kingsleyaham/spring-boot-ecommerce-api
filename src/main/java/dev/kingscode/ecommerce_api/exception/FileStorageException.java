package dev.kingscode.ecommerce_api.exception;

import dev.kingscode.ecommerce_api.dto.ApiException;

public class FileStorageException extends ApiException {

    private final String fileIdentifier;
    private final String operation;

    public FileStorageException(String message, ErrorCode errorCode, String fileidentifier, String operation) {
        super(message, errorCode);
        this.fileIdentifier = fileidentifier;
        this.operation = operation;
    }

    public FileStorageException(
            String message,
            ErrorCode errorCode,
            String fileIdentifier,
            String operation,
            Throwable cause) {

        super(message, errorCode, cause);
        this.fileIdentifier = fileIdentifier;
        this.operation = operation;
    }

    public static FileStorageException uploadFailed(
            String fileIdentifier,
            Throwable cause) {

        return new FileStorageException(
                "Failed to upload file",
                ErrorCode.FILE_UPLOAD_FAILED,
                fileIdentifier,
                "UPLOAD",
                cause);
    }

    public static FileStorageException deleteFailed(
            String fileIdentifier,
            Throwable cause) {

        return new FileStorageException(
                "Failed to delete file",
                ErrorCode.FILE_DELETE_FAILED,
                fileIdentifier,
                "DELETE",
                cause);
    }

    public static FileStorageException fileNotFound(
            String fileIdentifier) {

        return new FileStorageException(
                "File not found",
                ErrorCode.FILE_NOT_FOUND,
                fileIdentifier,
                "READ");
    }

}
