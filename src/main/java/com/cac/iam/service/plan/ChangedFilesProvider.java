package com.cac.iam.service.plan;

import com.cac.iam.config.EnvironmentLookup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

@Component
public class ChangedFilesProvider {

    private static final String ENV_KEY = "CHANGED_FILES";

    private final EnvironmentLookup envLookup;

    /**
     * Creates a provider that reads from real environment variables.
     */
    @Autowired
    public ChangedFilesProvider() {
        this(System::getenv);
    }

    /**
     * Creates a provider with a custom environment lookup (primarily for testing).
     *
     * @param envLookup lookup that returns environment values
     */
    ChangedFilesProvider(EnvironmentLookup envLookup) {
        this.envLookup = envLookup;
    }

    /**
     * Returns normalized paths supplied via the CHANGED_FILES environment variable.
     *
     * @return list of changed file paths, or empty when none
     */
    public List<Path> getChangedPaths() {
        String envValue = envLookup.lookup(ENV_KEY);
        if (envValue == null || envValue.trim().isEmpty()) {
            return List.of();
        }

        return Arrays.stream(envValue.trim().split("\\s+"))
                .map(this::normalizePath)
                .toList();
    }

    private Path normalizePath(String rawPath) {
        // Support both forward and backward slashes from env values.
        String sanitized = rawPath.replace("\"", "").trim();
        return Paths.get(sanitized);
    }
}
