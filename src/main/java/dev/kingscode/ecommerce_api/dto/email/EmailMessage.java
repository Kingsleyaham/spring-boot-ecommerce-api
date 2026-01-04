package dev.kingscode.ecommerce_api.dto.email;

public record EmailMessage(
        String to,
        String subject,
        String body) {
}