package com.cac.iam.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PlanApplyExceptionTest {

    @Test
    void constructsWithMessage() {
        PlanApplyException ex = new PlanApplyException("boom");

        assertThat(ex.getMessage()).isEqualTo("boom");
        assertThat(ex.getCause()).isNull();
    }

    @Test
    void constructsWithMessageAndCause() {
        Exception cause = new IllegalArgumentException("cause");
        PlanApplyException ex = new PlanApplyException("boom", cause);

        assertThat(ex.getMessage()).isEqualTo("boom");
        assertThat(ex.getCause()).isEqualTo(cause);
    }
}
