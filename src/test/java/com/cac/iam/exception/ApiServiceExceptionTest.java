package com.cac.iam.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ApiServiceExceptionTest {

    @Test
    void constructsWithMessage() {
        ApiServiceException ex = new ApiServiceException("boom");

        assertThat(ex.getMessage()).isEqualTo("boom");
        assertThat(ex.getCause()).isNull();
    }

    @Test
    void constructsWithMessageAndCause() {
        Exception cause = new IllegalArgumentException("cause");
        ApiServiceException ex = new ApiServiceException("boom", cause);

        assertThat(ex.getMessage()).isEqualTo("boom");
        assertThat(ex.getCause()).isEqualTo(cause);
    }
}
