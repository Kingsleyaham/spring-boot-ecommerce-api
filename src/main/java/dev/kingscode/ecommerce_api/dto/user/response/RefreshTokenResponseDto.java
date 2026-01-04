package dev.kingscode.ecommerce_api.dto.user.response;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class RefreshTokenResponseDto {
    String accessToken;
}
