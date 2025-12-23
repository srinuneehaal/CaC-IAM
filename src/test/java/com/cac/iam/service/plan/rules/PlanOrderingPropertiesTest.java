package com.cac.iam.service.plan.rules;

import com.cac.iam.model.Action;
import com.cac.iam.model.FileCategory;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PlanOrderingPropertiesTest {

    @Test
    void gettersAndSettersWork() {
        PlanOrderingProperties props = new PlanOrderingProperties();
        PlanOrderingProperties.Rule rule = new PlanOrderingProperties.Rule();
        rule.setCategory(FileCategory.POLICIES);
        rule.setActions(List.of(Action.NEW));
        props.setRules(List.of(rule));

        assertThat(props.getRules()).hasSize(1);
        PlanOrderingProperties.Rule saved = props.getRules().getFirst();
        assertThat(saved.getCategory()).isEqualTo(FileCategory.POLICIES);
        assertThat(saved.getActions()).containsExactly(Action.NEW);
    }
}
