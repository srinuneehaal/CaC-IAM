package com.cac.iam.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UnsupportedFileCategoryExceptionTest {

    @Test
    void constructsWithMessage() {
        UnsupportedFileCategoryException ex = new UnsupportedFileCategoryException("unsupported");

        assertThat(ex.getMessage()).isEqualTo("unsupported");
    }
}
