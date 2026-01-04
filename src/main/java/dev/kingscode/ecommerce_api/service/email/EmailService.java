package dev.kingscode.ecommerce_api.service.email;

import java.util.Map;

public interface EmailService {

    void sendSimpleEMail(String to, String subject, String text);

    void sendEmailWithAttachment(String to, String subject, String text, String attachmentPath);

    void sendHtmlEmail(String to, String subject, String templateName, Map<String, Object> variables);

    // public void sendSimpleMail();

}
