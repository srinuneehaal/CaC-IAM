package com.cac.iam.service.plan.rules;

import com.cac.iam.model.Action;
import com.cac.iam.model.FileCategory;
import com.cac.iam.model.MasterPlan;
import com.cac.iam.model.PlanItem;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Rule engine that orders plan items using a configurable list of category/action rules.
 * Rules are read from {@link PlanOrderingProperties}, so the execution order can be tweaked
 * through application.properties without touching code. A stable tie-breaker preserves the
 * original discovery order when no explicit preference is provided.
 */
@Service
public class PlanOrderingRuleEngine {

    private static final List<Action> DEFAULT_ACTION_ORDER = List.of(Action.NEW, Action.UPDATE, Action.DELETE);

    private final PlanOrderingProperties properties;

    /**
     * Creates an ordering engine configured with the supplied properties.
     *
     * @param properties ordering configuration
     */
    public PlanOrderingRuleEngine(PlanOrderingProperties properties) {
        this.properties = properties;
    }

    /**
     * Applies ordering rules to the supplied master plan, returning the same instance with sorted items.
     *
     * @param plan master plan to order
     * @return the ordered plan (or the original plan when null/empty)
     */
    public MasterPlan applyOrdering(MasterPlan plan) {
        if (plan == null || plan.getItems() == null || plan.getItems().isEmpty()) {
            return plan;
        }

        List<PlanOrderingRule> rules = resolveRules();
        List<PlanItem> sorted = new ArrayList<>(plan.getItems());
        Map<PlanItem, Integer> originalOrder = indexByIdentity(sorted);
        sorted.sort((left, right) -> buildOrderKey(left, rules, originalOrder)
                .compareTo(buildOrderKey(right, rules, originalOrder)));
        plan.setItems(sorted);
        return plan;
    }

    private List<PlanOrderingRule> resolveRules() {
        List<PlanOrderingRule> configured = properties.getRules().stream()
                .filter(rule -> rule.getCategory() != null)
                .map(this::toRule)
                .toList();
        if (!configured.isEmpty()) {
            return configured;
        }
        return defaultRules();
    }

    private PlanOrderingRule toRule(PlanOrderingProperties.Rule source) {
        List<Action> actions = source.getActions() == null || source.getActions().isEmpty()
                ? DEFAULT_ACTION_ORDER
                : List.copyOf(source.getActions());
        return new PlanOrderingRule(source.getCategory(), actions);
    }

    private OrderKey buildOrderKey(PlanItem item,
                                   List<PlanOrderingRule> rules,
                                   Map<PlanItem, Integer> originalOrder) {
        for (int i = 0; i < rules.size(); i++) {
            PlanOrderingRule rule = rules.get(i);
            if (rule.matches(item)) {
                return new OrderKey(
                        i,
                        rule.actionIndex(item.getAction()),
                        originalOrder.getOrDefault(item, Integer.MAX_VALUE)
                );
            }
        }
        Action action = item != null ? item.getAction() : null;
        int fallbackActionIndex = action == null ? -1 : DEFAULT_ACTION_ORDER.indexOf(action);
        if (fallbackActionIndex < 0) {
            fallbackActionIndex = DEFAULT_ACTION_ORDER.size();
        }
        return new OrderKey(
                rules.size(),
                fallbackActionIndex,
                originalOrder.getOrDefault(item, Integer.MAX_VALUE)
        );
    }

    private Map<PlanItem, Integer> indexByIdentity(List<PlanItem> items) {
        Map<PlanItem, Integer> order = new IdentityHashMap<>();
        for (int i = 0; i < items.size(); i++) {
            order.put(items.get(i), i);
        }
        return order;
    }

    private List<PlanOrderingRule> defaultRules() {
        List<PlanOrderingRule> defaults = new ArrayList<>();
        // policies first so roles can reference them
        defaults.add(new PlanOrderingRule(FileCategory.POLICIES, List.of(Action.NEW, Action.UPDATE)));
        defaults.add(new PlanOrderingRule(FileCategory.ROLES, List.of(Action.NEW, Action.UPDATE)));
        defaults.add(new PlanOrderingRule(FileCategory.USERS, List.of(Action.NEW, Action.UPDATE)));
        defaults.add(new PlanOrderingRule(FileCategory.POLICIES, List.of(Action.DELETE)));
        defaults.add(new PlanOrderingRule(FileCategory.ROLES, List.of(Action.DELETE)));
        defaults.add(new PlanOrderingRule(FileCategory.USERS, List.of(Action.DELETE)));
        return defaults;
    }

    private record OrderKey(int groupOrder, int actionOrder, int originalOrder)
            implements Comparable<OrderKey> {

        @Override
        public int compareTo(OrderKey other) {
            int result = Integer.compare(groupOrder, other.groupOrder);
            if (result != 0) {
                return result;
            }
            result = Integer.compare(actionOrder, other.actionOrder);
            if (result != 0) {
                return result;
            }
            return Integer.compare(originalOrder, other.originalOrder);
        }
    }

    private static final class PlanOrderingRule {
        private final FileCategory category;
        private final List<Action> actions;

        PlanOrderingRule(FileCategory category, List<Action> actions) {
            this.category = category;
            this.actions = actions == null || actions.isEmpty() ? DEFAULT_ACTION_ORDER : actions;
        }

        boolean matches(PlanItem item) {
            if (item == null || item.getFileCategory() == null || item.getAction() == null) {
                return false;
            }
            if (!Objects.equals(category, item.getFileCategory())) {
                return false;
            }
            return actions.isEmpty() || actions.contains(item.getAction());
        }

        int actionIndex(Action action) {
            int index = actions.indexOf(action);
            return index >= 0 ? index : actions.size();
        }
    }
}
