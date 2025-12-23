package com.cac.iam.util;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class PathUtilsTest {

    @Test
    void baseNameStripsExtension() {
        assertThat(PathUtils.baseName(Path.of("a/b/c.txt"))).isEqualTo("c");
        assertThat(PathUtils.baseName(Path.of("a/b/c"))).isEqualTo("c");
        assertThat(PathUtils.baseName(null)).isEmpty();
    }

    @Test
    void fileNameReturnsEmptyForNull() {
        assertThat(PathUtils.fileName(null)).isEmpty();
        assertThat(PathUtils.fileName(Path.of("a/b.txt"))).isEqualTo("b.txt");
    }
}
