package com.cac.iam.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MissingApplierExceptionTest {

    @Test
    void constructsWithMessage() {
        MissingApplierException ex = new MissingApplierException("missing");

        assertThat(ex.getMessage()).isEqualTo("missing");
    }
}
