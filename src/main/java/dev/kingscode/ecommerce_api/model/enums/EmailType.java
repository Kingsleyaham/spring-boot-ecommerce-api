package dev.kingscode.ecommerce_api.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EmailType {
    USER_REGISTRATION("Verify Your Account", "user-verification"),
    PASSWORD_RESET("Reset Your Password", "password-reset"),
    ACCOUNT_LOCKED("Account Security Alert", "account-locked"),
    WELCOME_EMAIL("Welcome to Our Platform!", "welcome-email");

    private final String defaultSubject;
    private final String templateName;

}
