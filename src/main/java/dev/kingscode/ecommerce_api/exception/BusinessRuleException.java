package dev.kingscode.ecommerce_api.exception;

import dev.kingscode.ecommerce_api.dto.ApiException;

public class BusinessRuleException extends ApiException {

    protected BusinessRuleException(String message) {
        super(message, ErrorCode.BUSINESS_RULE_VIOLATION);
    }

}
