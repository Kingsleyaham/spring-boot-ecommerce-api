package dev.kingscode.ecommerce_api.service.email;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import dev.kingscode.ecommerce_api.dto.email.EmailQueueMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailQueueService {
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    @Value("${app.mail.queue.name:email-queue}")
    private String emailQueueName;

    public void enqueue(EmailQueueMessage message) {
        try {
            String payload = objectMapper.writeValueAsString(message);
            redisTemplate.opsForList().rightPush(emailQueueName, payload);

            log.info("📩 Email queued for {}", message.getTo());
        } catch (Exception e) {
            log.error("Failed to enqueue email", e);
            throw new RuntimeException("Email queue failed", e);
        }
    }
}
