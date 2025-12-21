package com.cac.iam.service.apply.itemapply;

import com.cac.iam.model.FileCategory;
import com.cac.iam.model.PlanItem;

public interface PlanItemApplier {

    /**
     * Returns the file category this applier supports.
     *
     * @return supported category
     */
    FileCategory supportedCategory();

    /**
     * Applies the given plan item using the underlying API.
     *
     * @param item plan item to apply
     */
    void apply(PlanItem item);
}
