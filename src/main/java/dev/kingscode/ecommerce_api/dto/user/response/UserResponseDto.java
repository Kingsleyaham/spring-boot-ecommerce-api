package dev.kingscode.ecommerce_api.dto.user.response;

import java.time.Instant;
import java.util.UUID;

import dev.kingscode.ecommerce_api.model.enums.UserRole;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class UserResponseDto {
    UUID id;
    String email;
    String firstName;
    String lastName;
    String profileImage;

    boolean active;
    boolean emailVerified;
    UserRole role;

    Instant createdAt;
    Instant updatedAt;

}
