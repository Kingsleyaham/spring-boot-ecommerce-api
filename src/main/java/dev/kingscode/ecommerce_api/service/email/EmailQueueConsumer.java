package dev.kingscode.ecommerce_api.service.email;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import dev.kingscode.ecommerce_api.dto.email.EmailQueueMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailQueueConsumer {
    @Value("${app.mail.queue.name:email-queue}")
    private String queueName;

    @Value("${app.email.queue.max-retries:3}")
    private int maxRetries;

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;
    private final EmailService emailService;

    @Scheduled(fixedDelay = 5000) // every 5 seconds
    public void processQueue() {
        String payload = redisTemplate.opsForList().leftPop(queueName);

        if (payload == null) {
            return;
        }

        try {
            EmailQueueMessage message = objectMapper.readValue(payload, EmailQueueMessage.class);

            emailService.sendHtmlEmail(message.getTo(), message.getSubject(), message.getTemplateName(),
                    message.getVariables());

            log.info("✅ Email sent to {}", message.getTo());

        } catch (Exception e) {
            handleFailure(payload, e);
        }
    }

    private void handleFailure(String payload, Exception e) {
        try {

            EmailQueueMessage message = objectMapper.readValue(payload, EmailQueueMessage.class);

            if (message.getRetryCount() < maxRetries) {
                EmailQueueMessage updatedMessage = message.toBuilder()
                        .retryCount(message.getRetryCount() + 1)
                        .build();

                // Enqueue the new, updated message
                redisTemplate.opsForList()
                        .rightPush(queueName, objectMapper.writeValueAsString(updatedMessage));

                log.warn("🔁 Retrying email to {} (attempt {})",
                        updatedMessage.getTo(), updatedMessage.getRetryCount());
            } else {
                log.error("❌ Email permanently failed: {}", message.getTo(), e);
            }

        } catch (Exception ex) {
            log.error("Failed to process email failure", ex);
        }
    }
}
