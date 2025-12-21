package com.cac.iam.service.apply.itemapply;

import com.cac.iam.model.FileCategory;
import com.cac.iam.service.apply.apiservice.PolicyApiService;
import com.finbourne.access.model.PolicyCreationRequest;
import org.springframework.stereotype.Component;

@Component
public class PolicyPlanItemApplier extends AbstractPlanItemApplier<PolicyCreationRequest> {

    public PolicyPlanItemApplier(PolicyApiService apiService) {
        super(PolicyCreationRequest.class, apiService);
    }

    @Override
    public FileCategory supportedCategory() {
        return FileCategory.POLICIES;
    }
}
