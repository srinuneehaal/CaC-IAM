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
    private final ExitStrategy exitStrategy;

    @Autowired
    public ShutdownManager(ConfigurableApplicationContext context) {
        this(context, true, System::exit);
    }

    ShutdownManager(ConfigurableApplicationContext context, boolean enabled, ExitStrategy exitStrategy) {
        this.context = context;
        this.enabled = enabled;
        this.exitStrategy = exitStrategy;
    }

    public void shutdown(int exitCode) {
        if (!enabled) {
            return;
        }
        int code = computeExitCode(context, exitCode);
        exitStrategy.exit(code);
    }

    protected int computeExitCode(ConfigurableApplicationContext ctx, int requestedExitCode) {
        return ctx != null
                ? SpringApplication.exit(ctx, () -> requestedExitCode)
                : requestedExitCode;
    }

    public static ShutdownManager noOp() {
        return new ShutdownManager(null, false, code -> { });
    }

    @FunctionalInterface
    interface ExitStrategy {
        void exit(int code);
    }
}
