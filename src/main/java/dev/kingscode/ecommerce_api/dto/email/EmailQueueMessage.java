package dev.kingscode.ecommerce_api.dto.email;

import java.io.Serializable;
import java.time.Instant;
import java.util.Map;

import dev.kingscode.ecommerce_api.model.enums.EmailType;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class EmailQueueMessage implements Serializable {
        String to;
        String subject;
        EmailType emailType;
        String text; // for simple email
        String templateName;
        Map<String, Object> variables;
        int retryCount;
        Instant createdAt;
        String attachmentPath;
}