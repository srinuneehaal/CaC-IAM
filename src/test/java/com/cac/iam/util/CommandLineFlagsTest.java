package com.cac.iam.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CommandLineFlagsTest {

    @Test
    void detectsFlagIgnoringCaseAndWhitespace() {
        assertThat(CommandLineFlags.hasFlag(new String[] {"  --plan "}, "--PLAN")).isTrue();
        assertThat(CommandLineFlags.hasFlag(new String[] {"--other"}, "--plan")).isFalse();
        assertThat(CommandLineFlags.hasFlag(null, "--plan")).isFalse();
    }
}
