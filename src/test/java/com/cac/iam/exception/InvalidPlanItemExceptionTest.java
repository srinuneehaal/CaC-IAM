package com.cac.iam.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class InvalidPlanItemExceptionTest {

    @Test
    void constructsWithMessage() {
        InvalidPlanItemException ex = new InvalidPlanItemException("bad");

        assertThat(ex.getMessage()).isEqualTo("bad");
    }
}
