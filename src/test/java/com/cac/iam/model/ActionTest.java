package com.cac.iam.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ActionTest {

    @Test
    void containsAllValues() {
        assertThat(Action.values()).containsExactly(Action.NEW, Action.UPDATE, Action.DELETE);
        assertThat(Action.valueOf("NEW")).isEqualTo(Action.NEW);
    }
}
