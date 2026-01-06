package dev.kingscode.ecommerce_api.service.storage;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import dev.kingscode.ecommerce_api.exception.InvalidFileException;

@Service
public class FileValidationService {

    private static final long DEFAULT_MAX_DOC_SIZE_BYTES = 5 * 1024 * 1024; // 5MB
    private static final long DEFAULT_MAX_IMAGE_SIZE_BYTES = 2 * 1024 * 1024; // 2MB

    private static final List<String> DEFAULT_ALLOWED_IMAGE_TYPES = List.of(
            "image/jpeg",
            "image/png",
            "image/webp");

    private static final List<String> DEFAULT_ALLOWED_DOCUMENT_TYPES = List.of(
            // Standard Documents
            "application/pdf",
            "text/plain",

            // Microsoft Word
            "application/msword", // .doc
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document", // .docx

            // Microsoft Excel
            "application/vnd.ms-excel", // .xls
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", // .xlsx

            // Microsoft PowerPoint
            "application/vnd.ms-powerpoint", // .ppt
            "application/vnd.openxmlformats-officedocument.presentationml.presentation" // .pptx
    );

    public void validate(MultipartFile file, long maxSizeBytes, List<String> allowedTypes) {
        if (file == null || file.isEmpty()) {
            throw InvalidFileException.fileRequired();
        }

        if (file.getSize() > maxSizeBytes) {
            throw InvalidFileException.fileTooLarge(maxSizeBytes / (1024 * 1024));
        }

        String contentType = file.getContentType();
        if (contentType == null || !allowedTypes.contains(contentType)) {
            throw InvalidFileException.invalidType(allowedTypes);
        }
    }

    public void validateImage(MultipartFile file) {
        validate(file, DEFAULT_MAX_IMAGE_SIZE_BYTES, DEFAULT_ALLOWED_IMAGE_TYPES);
    }

    public void validatePdf(MultipartFile file) {
        validate(file, DEFAULT_MAX_DOC_SIZE_BYTES, List.of("application/pdf"));
    }

    public void validatePdf(MultipartFile file, long maxSizeBytes) {
        validate(file, maxSizeBytes, List.of("application/pdf"));
    }

    public void validateDocuments(MultipartFile file) {
        validate(file, DEFAULT_MAX_DOC_SIZE_BYTES, DEFAULT_ALLOWED_DOCUMENT_TYPES);
    }

    public void validateDocuments(MultipartFile file, long maxSizeBytes) {
        validate(file, maxSizeBytes, DEFAULT_ALLOWED_DOCUMENT_TYPES);
    }

}
