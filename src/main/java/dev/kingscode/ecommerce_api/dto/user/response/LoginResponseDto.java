package dev.kingscode.ecommerce_api.dto.user.response;

import java.util.UUID;

import dev.kingscode.ecommerce_api.model.enums.UserRole;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class LoginResponseDto {
    String accessToken;
    String tokenType;
    Boolean fullAuthenticated;
    String refreshToken;
    UserResponse user;

    @Value
    @Builder
    public static class UserResponse {
        UUID id;
        String email;
        String firstName;
        String lastName;
        UserRole role;
    }
}
