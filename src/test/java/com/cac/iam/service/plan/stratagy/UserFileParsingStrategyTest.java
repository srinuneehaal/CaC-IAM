package com.cac.iam.service.plan.stratagy;

import com.cac.iam.config.FileLocationProperties;
import com.cac.iam.model.FileCategory;
import com.cac.iam.model.LoadedFile;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.finbourne.identity.model.CreateUserRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserFileParsingStrategyTest {

    @Test
    void supportsAndParses(@TempDir Path tempDir) throws Exception {
        Path changedRoot = tempDir.resolve("changed");
        Path dir = changedRoot.resolve("users");
        Files.createDirectories(dir);
        Path file = dir.resolve("user.json");
        Files.writeString(file, "{\"login\":\"u1\",\"emailAddress\":\"u1@example.com\"}");

        FileLocationProperties props = new FileLocationProperties();
        props.setChangedFilesDir("changed");
        props.setUsersDirName("users");

        UserFileParsingStrategy strategy = new UserFileParsingStrategy(props, new ObjectMapper());

        assertThat(strategy.supports(file)).isTrue();
        LoadedFile loaded = strategy.parse(file);
        assertThat(loaded.getCategory()).isEqualTo(FileCategory.USERS);
        assertThat(((CreateUserRequest) loaded.getPayload()).getLogin()).isEqualTo("u1");
        assertThat(loaded.getKey()).isEqualTo("u1");
    }

    @Test
    void supportsRejectsInvalidPaths(@TempDir Path tempDir) {
        FileLocationProperties props = new FileLocationProperties();
        props.setChangedFilesDir("changed");
        props.setUsersDirName("users");
        UserFileParsingStrategy strategy = new UserFileParsingStrategy(props, new ObjectMapper());

        assertThat(strategy.supports(tempDir.resolve("other.json"))).isFalse();
        assertThat(strategy.supports(null)).isFalse();
    }

    @Test
    void parseThrowsWhenMissing(@TempDir Path tempDir) {
        FileLocationProperties props = new FileLocationProperties();
        props.setChangedFilesDir("changed");
        props.setUsersDirName("users");
        UserFileParsingStrategy strategy = new UserFileParsingStrategy(props, new ObjectMapper());

        assertThatThrownBy(() -> strategy.parse(tempDir.resolve("missing.json")))
                .isInstanceOf(IOException.class);
    }
}
