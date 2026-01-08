package com.cac.iam.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

/**
 * Coordinates shutting down the Spring application and exiting the JVM.
 * A no-op variant is used in tests to avoid terminating the test JVM.
 */
@Component
public class ShutdownManager {

    private final ConfigurableApplicationContext context;
    private final boolean enabled;

    @Autowired
    public ShutdownManager(ConfigurableApplicationContext context) {
        this(context, true);
    }

    private ShutdownManager(ConfigurableApplicationContext context, boolean enabled) {
        this.context = context;
        this.enabled = enabled;
    }

    public void shutdown(int exitCode) {
        if (!enabled) {
            return;
        }
        int code = context != null
                ? SpringApplication.exit(context, () -> exitCode)
                : exitCode;
        System.exit(code);
    }

    public static ShutdownManager noOp() {
        return new ShutdownManager(null, false);
    }
}
