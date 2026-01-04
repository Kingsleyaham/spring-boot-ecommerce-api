package dev.kingscode.ecommerce_api.service.email;

import java.io.File;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Validated
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${app.mail.from}")
    private String from;

    @Override
    public void sendSimpleEMail(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        message.setFrom(from);

        mailSender.send(message);
        log.info("{} sent to: {}", subject, to);
    }

    @Override
    public void sendHtmlEmail(@NonNull String to, @NonNull String subject,
            @NonNull String templateName,
            Map<String, Object> variables) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            Context context = new Context();
            context.setVariables(variables);
            String htmlContent = templateEngine.process(templateName, context);

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            helper.setFrom(from);

            mailSender.send(message);

            log.info("{} email sent to {}", subject, to);

        } catch (MessagingException e) {
            log.error("Failed to send {} to: {}", subject, to, e);
            throw new RuntimeException("Failed to send email", e);
        }
    }

    @Override
    public void sendEmailWithAttachment(@NonNull String to,
            @NonNull String subject, @NonNull String text, @NonNull String attachmentPath) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(text);
            helper.setFrom(from);

            // Add attachment
            File file = new ClassPathResource(attachmentPath).getFile();
            helper.addAttachment(file.getName(), file);

            mailSender.send(message);
            log.info("Email with attachment '{}' sent to: {}", subject, to);

        } catch (Exception e) {
            log.error("Failed to send HTML email to: {}", to, e);
            throw new RuntimeException("Failed to send email", e);
        }
    }

}
