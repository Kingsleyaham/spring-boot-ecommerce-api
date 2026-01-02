package dev.kingscode.ecommerce_api.dto.user.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ResetPasswordRequestDto {
    @NotBlank
    private String token;

    @NotBlank
    @Size(min = 8, max = 64)
    private String newPassword;
}
