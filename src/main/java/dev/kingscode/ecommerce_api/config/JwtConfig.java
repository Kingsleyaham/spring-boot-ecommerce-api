package dev.kingscode.ecommerce_api.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Validated
@Configuration
@ConfigurationProperties(prefix = "jwt")
public class JwtConfig {

    @NotBlank(message = "JWT secret key is required")
    private String secret;

    @Positive(message = "JWT access token expiration must be positive")
    private Long expiration = 3600000L; // 1 hour default

    @Positive(message = "JWT access token expiration must be positive")
    private Long refreshTokenExpiration = 604800000L; // 7 days default

}
