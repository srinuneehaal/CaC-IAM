package com.cac.iam.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MasterPlanTest {

    @Test
    void addAndGetItems() {
        MasterPlan plan = new MasterPlan();
        assertThat(plan.getItems()).isEmpty();

        PlanItem item = new PlanItem();
        plan.addItem(item);

        assertThat(plan.getItems()).containsExactly(item);

        plan.setItems(java.util.List.of(new PlanItem(Action.NEW, FileCategory.USERS, "k", "s", new Object())));
        assertThat(plan.getItems()).hasSize(1);
    }
}
