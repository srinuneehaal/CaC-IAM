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
    void returnsNullWhenPlanIsNull() {
        PlanOrderingRuleEngine engine = new PlanOrderingRuleEngine(new PlanOrderingProperties());

        assertThat(engine.applyOrdering(null)).isNull();
    }

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

    @Test
    void usesConfiguredRulesAndDefaultActionsWhenEmpty() {
        PlanOrderingProperties properties = new PlanOrderingProperties();
        PlanOrderingProperties.Rule usersRule = new PlanOrderingProperties.Rule();
        usersRule.setCategory(FileCategory.USERS);
        usersRule.setActions(List.of(Action.DELETE, Action.NEW)); // custom order
        PlanOrderingProperties.Rule policiesRule = new PlanOrderingProperties.Rule();
        policiesRule.setCategory(FileCategory.POLICIES); // empty actions -> defaults
        properties.setRules(List.of(usersRule, policiesRule));

        PlanOrderingRuleEngine engine = new PlanOrderingRuleEngine(properties);
        MasterPlan plan = new MasterPlan();
        PlanItem userNew = new PlanItem(Action.NEW, FileCategory.USERS, "u-new", "u-new.json", null);
        PlanItem userDelete = new PlanItem(Action.DELETE, FileCategory.USERS, "u-del", "u-del.json", null);
        PlanItem policyUpdate = new PlanItem(Action.UPDATE, FileCategory.POLICIES, "p-upd", "p-upd.json", null);
        plan.setItems(List.of(userNew, userDelete, policyUpdate));

        MasterPlan ordered = engine.applyOrdering(plan);

        assertThat(ordered.getItems()).extracting(PlanItem::getKey)
                .containsExactly("u-del", "u-new", "p-upd");
    }

    @Test
    void fallsBackWhenRuleDoesNotMatchAndWhenActionMissing() {
        PlanOrderingProperties properties = new PlanOrderingProperties();
        PlanOrderingProperties.Rule policiesRule = new PlanOrderingProperties.Rule();
        policiesRule.setCategory(FileCategory.POLICIES);
        properties.setRules(List.of(policiesRule));

        PlanOrderingRuleEngine engine = new PlanOrderingRuleEngine(properties);
        MasterPlan plan = new MasterPlan();
        PlanItem unmatchedCategory = new PlanItem(Action.NEW, FileCategory.ROLES, "role-item", "roles.json", null);
        PlanItem missingAction = new PlanItem();
        missingAction.setFileCategory(FileCategory.ROLES);
        missingAction.setKey("missing-action");
        plan.setItems(List.of(missingAction, unmatchedCategory));

        MasterPlan ordered = engine.applyOrdering(plan);

        assertThat(ordered.getItems()).extracting(PlanItem::getKey)
                .containsExactly("role-item", "missing-action");
    }

    @Test
    void usesActionIndexFallbackWhenActionNotConfigured() {
        PlanOrderingProperties properties = new PlanOrderingProperties();
        PlanOrderingProperties.Rule usersRule = new PlanOrderingProperties.Rule();
        usersRule.setCategory(FileCategory.USERS);
        usersRule.setActions(List.of(Action.DELETE));
        properties.setRules(List.of(usersRule));

        PlanOrderingRuleEngine engine = new PlanOrderingRuleEngine(properties);
        PlanItem deleteItem = new PlanItem(Action.DELETE, FileCategory.USERS, "delete", "delete.json", null);
        PlanItem updateItem = new PlanItem(Action.UPDATE, FileCategory.USERS, "update", "update.json", null);
        MasterPlan plan = new MasterPlan();
        plan.setItems(List.of(updateItem, deleteItem));

        MasterPlan ordered = engine.applyOrdering(plan);

        assertThat(ordered.getItems()).extracting(PlanItem::getKey)
                .containsExactly("delete", "update");
    }
}
