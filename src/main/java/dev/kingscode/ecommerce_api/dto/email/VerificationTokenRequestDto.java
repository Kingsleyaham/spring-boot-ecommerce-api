package dev.kingscode.ecommerce_api.dto.email;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class VerificationTokenRequestDto {

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;

}
