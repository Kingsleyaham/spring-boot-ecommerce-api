package dev.kingscode.ecommerce_api.dto.user.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class VerifyEmailRequestDto {

    @NotBlank
    private String token;
}
