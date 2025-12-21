package com.cac.iam.service.apply.itemapply;

import com.cac.iam.model.FileCategory;
import com.cac.iam.service.apply.apiservice.RoleApiService;
import com.finbourne.access.model.RoleCreationRequest;
import org.springframework.stereotype.Component;

@Component
public class RolePlanItemApplier extends AbstractPlanItemApplier<RoleCreationRequest> {

    public RolePlanItemApplier(RoleApiService apiService) {
        super(RoleCreationRequest.class, apiService);
    }

    @Override
    public FileCategory supportedCategory() {
        return FileCategory.ROLES;
    }
}
