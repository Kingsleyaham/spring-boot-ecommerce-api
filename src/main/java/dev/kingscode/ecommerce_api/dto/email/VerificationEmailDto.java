package dev.kingscode.ecommerce_api.dto.email;

import java.time.Instant;
import java.util.Map;

import dev.kingscode.ecommerce_api.model.enums.EmailType;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class VerificationEmailDto {
    String recipientEmail;
    String recipientName;
    String verificationToken;
    Instant tokenExpiry;
    EmailType emailType;
    private Map<String, Object> additionalData;

    public static VerificationEmailDto forUserRegistration(String email, String username, String token,
            Instant expiry) {
        return VerificationEmailDto.builder().recipientEmail(email).recipientName(username).verificationToken(token)
                .tokenExpiry(expiry).emailType(EmailType.USER_REGISTRATION).build();
    }

    public static VerificationEmailDto forPasswordReset(String email, String username, String token, Instant expiry) {
        return VerificationEmailDto.builder().recipientEmail(email).recipientName(username).verificationToken(token)
                .tokenExpiry(expiry).emailType(EmailType.PASSWORD_RESET).build();
    }

}
