package com.cac.iam.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UnsupportedFilePathExceptionTest {

    @Test
    void constructsWithMessage() {
        UnsupportedFilePathException ex = new UnsupportedFilePathException("path");

        assertThat(ex.getMessage()).isEqualTo("path");
    }
}
