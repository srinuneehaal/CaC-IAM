package com.cac.iam.service.apply;

import com.cac.iam.model.FileCategory;
import com.cac.iam.model.PlanItem;

public interface PlanItemApplier {

    boolean supports(FileCategory category);

    void apply(PlanItem item);
}
