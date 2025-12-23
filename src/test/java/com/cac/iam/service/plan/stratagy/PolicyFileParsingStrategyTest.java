package com.cac.iam.service.plan.stratagy;

import com.cac.iam.config.FileLocationProperties;
import com.cac.iam.model.FileCategory;
import com.cac.iam.model.LoadedFile;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.finbourne.access.model.PolicyCreationRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PolicyFileParsingStrategyTest {

    @Test
    void supportsAndParses(@TempDir Path tempDir) throws Exception {
        Path changedRoot = tempDir.resolve("changed");
        Path policiesDir = changedRoot.resolve("policies");
        Files.createDirectories(policiesDir);
        Path file = policiesDir.resolve("policy.json");
        Files.writeString(file, "{\"code\":\"P1\"}");

        FileLocationProperties props = new FileLocationProperties();
        props.setChangedFilesDir("changed");
        props.setPoliciesDirName("policies");

        PolicyFileParsingStrategy strategy = new PolicyFileParsingStrategy(props, new ObjectMapper());

        assertThat(strategy.supports(file)).isTrue();
        LoadedFile loaded = strategy.parse(file);
        assertThat(loaded.getCategory()).isEqualTo(FileCategory.POLICIES);
        assertThat(((PolicyCreationRequest) loaded.getPayload()).getCode()).isEqualTo("P1");
    }

    @Test
    void supportsRejectsInvalidPaths(@TempDir Path tempDir) {
        FileLocationProperties props = new FileLocationProperties();
        props.setChangedFilesDir("changed");
        props.setPoliciesDirName("policies");

        PolicyFileParsingStrategy strategy = new PolicyFileParsingStrategy(props, new ObjectMapper());

        assertThat(strategy.supports(tempDir.resolve("other/file.json"))).isFalse();
        assertThat(strategy.supports(null)).isFalse();
    }

    @Test
    void parseThrowsWhenMissing(@TempDir Path tempDir) {
        FileLocationProperties props = new FileLocationProperties();
        props.setChangedFilesDir("changed");
        props.setPoliciesDirName("policies");

        PolicyFileParsingStrategy strategy = new PolicyFileParsingStrategy(props, new ObjectMapper());

        assertThatThrownBy(() -> strategy.parse(tempDir.resolve("missing.json")))
                .isInstanceOf(IOException.class);
    }
}
