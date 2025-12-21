package com.cac.iam.service.plan.rules;

import com.cac.iam.model.Action;
import com.cac.iam.model.FileCategory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Configuration holder for plan ordering rules. The list is read from
 * application.properties so the execution order can be tuned without code changes.
 */
@Component
@ConfigurationProperties(prefix = "plan.ordering")
public class PlanOrderingProperties {

    private List<Rule> rules = new ArrayList<>();

    /**
     * Returns the configured ordering rules.
     *
     * @return list of rules
     */
    public List<Rule> getRules() {
        return rules;
    }

    /**
     * Sets the configured ordering rules.
     *
     * @param rules list of rules
     */
    public void setRules(List<Rule> rules) {
        this.rules = rules;
    }

    public static class Rule {
        private FileCategory category;
        private List<Action> actions = new ArrayList<>();

        /**
         * Returns the file category this rule applies to.
         *
         * @return category
         */
        public FileCategory getCategory() {
            return category;
        }

        /**
         * Sets the file category this rule applies to.
         *
         * @param category category value
         */
        public void setCategory(FileCategory category) {
            this.category = category;
        }

        /**
         * Returns the action ordering for this category.
         *
         * @return ordered actions
         */
        public List<Action> getActions() {
            return actions;
        }

        /**
         * Sets the action ordering for this category.
         *
         * @param actions ordered actions
         */
        public void setActions(List<Action> actions) {
            this.actions = actions;
        }
    }
}
