package com.cac.iam.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FileCategoryTest {

    @Test
    void containsExpectedCategories() {
        assertThat(FileCategory.values()).containsExactly(FileCategory.POLICIES, FileCategory.ROLES, FileCategory.USERS);
        assertThat(FileCategory.valueOf("USERS")).isEqualTo(FileCategory.USERS);
    }
}
