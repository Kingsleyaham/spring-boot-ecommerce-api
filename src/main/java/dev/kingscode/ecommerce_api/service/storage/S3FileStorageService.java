package dev.kingscode.ecommerce_api.service.storage;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import dev.kingscode.ecommerce_api.config.AwsS3Properties;
import dev.kingscode.ecommerce_api.dto.FileUploadResult;
import dev.kingscode.ecommerce_api.exception.ErrorCode;
import dev.kingscode.ecommerce_api.exception.FileStorageException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3FileStorageService implements FileStorageService {

    private final S3Client s3Client;
    private final AwsS3Properties properties;

    @Override
    public FileUploadResult uploadFile(MultipartFile file, String folder, Map<String, String> metadata) {

        String originalFilename = file.getOriginalFilename();
        String contentType = file.getContentType();
        String fileKey = buildFileKey(folder, originalFilename);

        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder().bucket(properties.getBucketName())
                    .key(fileKey).contentType(contentType).metadata(metadata).build();

            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(file.getBytes()));

            String fileUrl = buildPublicUrl(fileKey);

            return FileUploadResult.builder().fileName(originalFilename).fileKey(fileKey).url(fileUrl)
                    .size(file.getSize()).contentType(contentType).build();

        } catch (IOException e) {
            log.error("Failed to read file bytes for upload", e);
            throw FileStorageException.uploadFailed(fileKey, e);
        } catch (S3Exception e) {
            log.error("S3 upload failed: {}", e.awsErrorDetails().errorMessage(), e);
            throw new FileStorageException("Failed to upload file to S3", ErrorCode.SERVICE_UNAVAILABLE, fileKey,
                    "UPLOAD");
        }

    }

    private String buildFileKey(String folder, String originalFilename) {
        String safeFolder = folder.endsWith("/") ? folder : folder + "/";
        String extension = extractExtension(originalFilename);
        return safeFolder + UUID.randomUUID() + extension;
    }

    private String extractExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf('.'));
    }

    private String buildPublicUrl(String fileKey) {
        return String.format(properties.getPublicUrl(), properties.getBucketName()) + "/" + fileKey;
    }

}
