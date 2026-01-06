package dev.kingscode.ecommerce_api.service.storage;

import java.util.Map;

import org.springframework.web.multipart.MultipartFile;

import dev.kingscode.ecommerce_api.dto.FileUploadResult;
import dev.kingscode.ecommerce_api.exception.FileStorageException;

/**
 * Contract for file storage services.
 * Implementations can be S3, Cloudinary, Azure Blob, Local Storage, etc.
 */
public interface FileStorageService {

    /**
     * Upload a file to storage
     * 
     * @param file     The file to upload
     * @param folder   The folder/path to store the file in
     * @param metadata Additional metadata for the file
     * @return FileUploadResult containing upload details
     * @throws FileStorageException if upload fails
     */
    FileUploadResult uploadFile(MultipartFile file, String folder, Map<String, String> metadata);

    /**
     * Upload a file to storage with default metadata
     * 
     * @param file   The file to upload
     * @param folder The folder/path to store the file in
     * @return FileUploadResult containing upload details
     * @throws FileStorageException if upload fails
     */
    default FileUploadResult uploadFile(MultipartFile file, String folder) {
        return uploadFile(file, folder, Map.of());
    }
}
