package dev.kingscode.ecommerce_api.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;
import lombok.Setter;

@Configuration
@ConfigurationProperties(prefix = "aws.s3")
@Getter
@Setter
public class AwsS3Properties {
    private String bucketName;
    private String region;
    private String publicUrl;
}
