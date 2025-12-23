package com.cac.iam.model;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

class LoadedFileTest {

    @Test
    void equalityAndHashCodeIncludeCategoryKeyPayload() {
        Path path = Path.of("a/b.json");
        Object payload = java.util.Map.of("x", 1);
        LoadedFile f1 = new LoadedFile(FileCategory.ROLES, "k", path, payload);
        LoadedFile f2 = new LoadedFile(FileCategory.ROLES, "k", path, payload);

        assertThat(f1).isEqualTo(f2);
        assertThat(f1.hashCode()).isEqualTo(f2.hashCode());
        assertThat(f1.toString()).contains("ROLES").contains("k");
    }
}
