package dev.kingscode.ecommerce_api.util;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.UUID;

import org.springframework.stereotype.Component;

@Component
public class TokenGenerator {
    private final SecureRandom random = new SecureRandom();

    public String generateToken() {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);

        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    public String generateRandomString() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    public String generateDigitOtp() {
        return generateDigitOtp(6);
    }

    public String generateDigitOtp(int digits) {
        if (digits < 1) {
            throw new IllegalArgumentException("digits must be >= 1");
        }
        if (digits > 9) {
            // int would overflow for 10+ digits; use long/BigInteger if you ever need more
            throw new IllegalArgumentException("digits must be <= 9");
        }

        int bound = (int) Math.pow(10, digits); // e.g. 6 -> 1_000_000
        int otp = random.nextInt(bound); // 0..(10^digits - 1)
        return String.format("%0" + digits + "d", otp); // zero-pad to required length
    }
}
