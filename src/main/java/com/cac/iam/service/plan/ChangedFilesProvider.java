package com.cac.iam.service.plan;

import com.cac.iam.config.EnvironmentLookup;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class ChangedFilesProvider {

    private static final String ENV_KEY = "CHANGED_FILES";
    // Capture path segments up to and including ".json", allowing embedded spaces.
    private static final Pattern JSON_PATH_PATTERN = Pattern.compile("(?i)\\s*([^\\s].*?\\.json)");

    private final EnvironmentLookup envLookup;

    /**
     * Creates a provider that reads from real environment variables.
     */
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

        List<String> tokens = tokenizeByJson(envValue);
        if (tokens.isEmpty()) {
            return List.of();
        }

        return tokens.stream()
                .map(this::normalizePath)
                .toList();
    }

    private Path normalizePath(String rawPath) {
        // Support both forward and backward slashes from env values.
        String sanitized = rawPath.replace("\"", "").trim();
        return Paths.get(sanitized);
    }

    /**
     * Splits environment values into path tokens by finding each occurrence ending with ".json".
     */
    private List<String> tokenizeByJson(String envValue) {
        Matcher matcher = JSON_PATH_PATTERN.matcher(envValue);
        return matcher.results()
                .map(match -> match.group(1).trim())
                .filter(token -> !token.isBlank())
                .toList();
    }
}
