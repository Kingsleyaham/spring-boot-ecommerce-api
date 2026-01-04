package dev.kingscode.ecommerce_api.service.email;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import dev.kingscode.ecommerce_api.dto.email.VerificationEmailDto;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class EmailTemplateVariablesBuilder {
    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    @Value("${app.client-base-url:http://localhost:3000}")
    private String frontendBaseUrl;

    @Value("${app.company.name:KingsCode E-Commerce}")
    private String companyName;

    public Map<String, Object> buildTemplateVariables(VerificationEmailDto emailDto) {
        Map<String, Object> variables = new HashMap<>();

        // common variables for all emails
        variables.put("recipientName", emailDto.getRecipientName());
        variables.put("currentYear", java.time.Year.now().getValue());
        variables.put("companyName", companyName);
        if (emailDto.getVerificationToken() != null) {
            variables.put("verificationToken", emailDto.getVerificationToken());
        }

        if (emailDto.getTokenExpiry() != null) {
            variables.put("tokenExpiry", emailDto.getTokenExpiry());
            variables.put("expiryInHours", "24"); // Default, can be customized
        }

        if (emailDto.getAdditionalData() != null) {
            variables.putAll(emailDto.getAdditionalData());
        }

        // Add type-specific variables
        addTypeSpecificVariables(emailDto, variables);

        return variables;

    }

    private void addTypeSpecificVariables(VerificationEmailDto emailDto, Map<String, Object> variables) {

        switch (emailDto.getEmailType()) {
            case USER_REGISTRATION -> {
                variables.put("verificationLink",
                        String.format("%s/api/v1/auth/verify-email?token=%s",
                                baseUrl, emailDto.getVerificationToken()));
                variables.put("action",
                        "complete your registration");
                variables.put("buttonText", "Verify Email");
                variables
                        .put("expiryInHours", "24");
            }

            case PASSWORD_RESET -> {
                variables.put("action", "reset your password");
                variables.put("buttonText", "Reset Password");
                variables.put("expiryInMinutes", "15"); // 15min for password reset
            }

            case ACCOUNT_LOCKED -> {
                variables.put("unlockLink",
                        String.format("%s/api/v1/auth/unlock-account?token=%s",
                                baseUrl, emailDto.getVerificationToken()));
                variables.put("action", "unlock your account");
                variables.put("buttonText", "Unlock Account");
            }

            case WELCOME_EMAIL -> {
                variables.put("dashboardLink", frontendBaseUrl + "/dashboard");
                variables.put("supportEmail", "support@kingscode.com");
                variables.put("action", "explore your new account");
            }
            default -> {
                log.warn("No specific variables defined for email type: {}", emailDto.getEmailType());
            }

        }
    }

}
