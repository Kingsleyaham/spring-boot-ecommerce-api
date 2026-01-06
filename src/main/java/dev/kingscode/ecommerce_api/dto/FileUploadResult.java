package dev.kingscode.ecommerce_api.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class FileUploadResult {
    String fileName;
    String fileKey;
    String url;
    long size;
    String contentType;
}
