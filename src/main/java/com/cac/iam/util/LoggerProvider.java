package com.cac.iam.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Centralised logger provider to create class-specific loggers via dependency injection.
 */
@Component
public class LoggerProvider {

    public Logger getLogger(Class<?> type) {
        return LoggerFactory.getLogger(type);
    }
}
