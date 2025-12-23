package com.cac.iam.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PlanProcessingExceptionTest {

    @Test
    void constructsWithMessage() {
        PlanProcessingException ex = new PlanProcessingException("fail");

        assertThat(ex.getMessage()).isEqualTo("fail");
        assertThat(ex.getCause()).isNull();
    }

    @Test
    void constructsWithMessageAndCause() {
        Exception cause = new IllegalStateException("root");
        PlanProcessingException ex = new PlanProcessingException("fail", cause);

        assertThat(ex.getMessage()).isEqualTo("fail");
        assertThat(ex.getCause()).isEqualTo(cause);
    }
}
