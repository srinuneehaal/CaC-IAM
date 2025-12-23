package com.cac.iam.service.plan.stratagy;

import com.cac.iam.config.FileLocationProperties;
import com.cac.iam.model.FileCategory;
import com.cac.iam.model.LoadedFile;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.finbourne.access.model.RoleCreationRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RoleFileParsingStrategyTest {

    @Test
    void supportsAndParses(@TempDir Path tempDir) throws Exception {
        Path changedRoot = tempDir.resolve("changed");
        Path dir = changedRoot.resolve("roles");
        Files.createDirectories(dir);
        Path file = dir.resolve("role.json");
        Files.writeString(file, "{\"code\":\"R1\"}");

        FileLocationProperties props = new FileLocationProperties();
        props.setChangedFilesDir("changed");
        props.setRolesDirName("roles");

        RoleFileParsingStrategy strategy = new RoleFileParsingStrategy(props, new ObjectMapper());

        assertThat(strategy.supports(file)).isTrue();
        LoadedFile loaded = strategy.parse(file);
        assertThat(loaded.getCategory()).isEqualTo(FileCategory.ROLES);
        assertThat(((RoleCreationRequest) loaded.getPayload()).getCode()).isEqualTo("R1");
        assertThat(loaded.getKey()).isEqualTo("R1");
    }

    @Test
    void fallsBackToBaseNameWhenCodeMissing(@TempDir Path tempDir) throws Exception {
        Path changedRoot = tempDir.resolve("changed");
        Path dir = changedRoot.resolve("roles");
        Files.createDirectories(dir);
        Path file = dir.resolve("role.json");
        Files.writeString(file, "{}");

        FileLocationProperties props = new FileLocationProperties();
        props.setChangedFilesDir("changed");
        props.setRolesDirName("roles");

        RoleFileParsingStrategy strategy = new RoleFileParsingStrategy(props, new ObjectMapper());
        LoadedFile loaded = strategy.parse(file);

        assertThat(loaded.getKey()).isEqualTo("role");
    }

    @Test
    void supportsRejectsInvalidPaths(@TempDir Path tempDir) {
        FileLocationProperties props = new FileLocationProperties();
        props.setChangedFilesDir("changed");
        props.setRolesDirName("roles");

        RoleFileParsingStrategy strategy = new RoleFileParsingStrategy(props, new ObjectMapper());
        assertThat(strategy.supports(tempDir.resolve("wrong.json"))).isFalse();
        assertThat(strategy.supports(null)).isFalse();
    }

    @Test
    void parseThrowsWhenMissing(@TempDir Path tempDir) {
        FileLocationProperties props = new FileLocationProperties();
        props.setChangedFilesDir("changed");
        props.setRolesDirName("roles");

        RoleFileParsingStrategy strategy = new RoleFileParsingStrategy(props, new ObjectMapper());

        assertThatThrownBy(() -> strategy.parse(tempDir.resolve("missing.json")))
                .isInstanceOf(IOException.class);
    }
}
