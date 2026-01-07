package dev.kingscode.ecommerce_api.service.email;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import dev.kingscode.ecommerce_api.dto.email.EmailQueueMessage;
import dev.kingscode.ecommerce_api.dto.email.VerificationEmailDto;
import dev.kingscode.ecommerce_api.model.enums.EmailType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class VerificationEmailService {
    private final EmailTemplateVariablesBuilder templateVariablesBuilder;
    private final EmailQueueService emailQueueService;

    /**
     * Send user registration verification email
     */
    @Async
    public void sendUserVerificationEmail(String email, String username, String verificationToken, Instant expiryDate) {
        VerificationEmailDto emailDto = VerificationEmailDto.forUserRegistration(email, username, verificationToken,
                expiryDate);

        sendVerificationEmail(emailDto);
    }

    /**
     * Send password reset email
     */
    @Async
    public void sendPasswordResetEmail(String email, String username,
            String resetToken, Instant expiryDate) {
        VerificationEmailDto emailDTO = VerificationEmailDto.forPasswordReset(
                email, username, resetToken, expiryDate);

        sendVerificationEmail(emailDTO);
    }

    /**
     * Send welcome email after successful verification
     */
    @Async
    public void sendWelcomeEmail(String email, String username) {
        Map<String, Object> additionalData = new HashMap<>();
        additionalData.put("loginLink", "/login");
        additionalData.put("dashboardLink", "/dashboard");

        VerificationEmailDto emailDTO = VerificationEmailDto.builder()
                .recipientEmail(email)
                .recipientName(username)
                .emailType(EmailType.WELCOME_EMAIL)
                .additionalData(additionalData)
                .build();

        sendVerificationEmail(emailDTO);
    }

    /**
     * Resend verification email with new token
     */
    @Async
    public void resendVerificationEmail(String email, String username,
            String newToken, Instant newExpiry) {
        sendUserVerificationEmail(email, username, newToken, newExpiry);
        log.info("Verification email resent to: {}", email);
    }

    /**
     * Generic method to send any verification email
     */
    private void sendVerificationEmail(VerificationEmailDto emailDto) {
        try {
            Map<String, Object> variables = templateVariablesBuilder.buildTemplateVariables(emailDto);

            String subject = emailDto.getEmailType().getDefaultSubject();
            String templateName = emailDto.getEmailType().getTemplateName();

            EmailQueueMessage queueMessage = EmailQueueMessage.builder().to(emailDto.getRecipientEmail())
                    .subject(subject).templateName(templateName).variables(variables).retryCount(0)
                    .createdAt(Instant.now()).build();

            emailQueueService.enqueue(queueMessage);

            // mailService.sendHtmlEmail(emailDto.getRecipientEmail(), subject,
            // templateName, variables);

            log.info("{} email sent successfully to: {}", emailDto.getEmailType(), emailDto.getRecipientEmail());

        } catch (Exception e) {
            log.error("Failed to send {} email to: {}", emailDto.getEmailType(), emailDto.getRecipientEmail(), e);

            throw new RuntimeException("Failed to send verification email", e);
        }
    }

}
