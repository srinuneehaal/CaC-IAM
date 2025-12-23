package com.cac.iam.service.plan;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ChangedFilesProviderTest {

    @Test
    void returnsEmptyWhenEnvMissing() {
        ChangedFilesProvider provider = new ChangedFilesProvider(key -> null);
        assertThat(provider.getChangedPaths()).isEmpty();
    }

    @Test
    void parsesWhitespaceSeparatedPaths() {
        ChangedFilesProvider provider = new ChangedFilesProvider(key -> "a/b.json c\\d.json");
        List<Path> paths = provider.getChangedPaths();
        assertThat(paths).hasSize(2);
        assertThat(paths.get(0).toString()).contains("a\\b.json").isNotBlank();
    }
}
