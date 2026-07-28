package com.samuelgularte.financeflow.budgeting.infrastructure.i18n;

import org.springframework.context.MessageSource;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.stereotype.Component;

@Component
public class Messages {

    private final MessageSourceAccessor accessor;

    public Messages(MessageSource messageSource) {
        this.accessor = new MessageSourceAccessor(messageSource);
    }

    public String get(String key) {
        return accessor.getMessage(key);
    }
}
