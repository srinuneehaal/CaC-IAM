package com.cac.iam.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PlanItemTest {

    @Test
    void gettersAndSettersWork() {
        Object payload = new Object();
        Object before = new Object();
        PlanItem item = new PlanItem(Action.NEW, FileCategory.POLICIES, "key1", "/src", payload);
        item.setBeforePayload(before);

        assertThat(item.getAction()).isEqualTo(Action.NEW);
        assertThat(item.getFileCategory()).isEqualTo(FileCategory.POLICIES);
        assertThat(item.getKey()).isEqualTo("key1");
        assertThat(item.getSourcePath()).isEqualTo("/src");
        assertThat(item.getPayload()).isEqualTo(payload);
        assertThat(item.getBeforePayload()).isEqualTo(before);

        item.setAction(Action.DELETE);
        item.setFileCategory(FileCategory.USERS);
        item.setKey("k2");
        item.setSourcePath("/other");
        item.setPayload("newPayload");

        assertThat(item.getAction()).isEqualTo(Action.DELETE);
        assertThat(item.getFileCategory()).isEqualTo(FileCategory.USERS);
        assertThat(item.getKey()).isEqualTo("k2");
        assertThat(item.getSourcePath()).isEqualTo("/other");
        assertThat(item.getPayload()).isEqualTo("newPayload");
    }
}
