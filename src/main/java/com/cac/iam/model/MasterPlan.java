package com.cac.iam.model;

import java.util.ArrayList;
import java.util.List;

public class MasterPlan {

    private List<PlanItem> items = new ArrayList<>();

    /**
     * Returns plan items in the master plan.
     *
     * @return plan items
     */
    public List<PlanItem> getItems() {
        return items;
    }

    /**
     * Replaces the plan items in the master plan.
     *
     * @param items items to set
     */
    public void setItems(List<PlanItem> items) {
        this.items = items;
    }

    /**
     * Adds a single plan item to the master plan.
     *
     * @param item plan item to add
     */
    public void addItem(PlanItem item) {
        this.items.add(item);
    }
}
