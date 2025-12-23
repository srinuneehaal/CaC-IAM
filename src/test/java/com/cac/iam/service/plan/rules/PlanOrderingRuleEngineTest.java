package com.cac.iam.service.plan.rules;

import com.cac.iam.model.Action;
import com.cac.iam.model.FileCategory;
import com.cac.iam.model.MasterPlan;
import com.cac.iam.model.PlanItem;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PlanOrderingRuleEngineTest {

    @Test
    void appliesDefaultOrdering() {
        PlanOrderingRuleEngine engine = new PlanOrderingRuleEngine(new PlanOrderingProperties());
        MasterPlan plan = new MasterPlan();
        plan.setItems(List.of(
                new PlanItem(Action.DELETE, FileCategory.POLICIES, "k3", "s3", null),
                new PlanItem(Action.NEW, FileCategory.ROLES, "k2", "s2", null),
                new PlanItem(Action.NEW, FileCategory.POLICIES, "k1", "s1", null)
        ));

        MasterPlan ordered = engine.applyOrdering(plan);

        assertThat(ordered.getItems()).extracting(PlanItem::getKey).containsExactly("k1", "k2", "k3");
    }

    @Test
    void returnsPlanUnchangedWhenEmpty() {
        PlanOrderingRuleEngine engine = new PlanOrderingRuleEngine(new PlanOrderingProperties());
        MasterPlan plan = new MasterPlan();

        assertThat(engine.applyOrdering(plan)).isSameAs(plan);
    }
}
