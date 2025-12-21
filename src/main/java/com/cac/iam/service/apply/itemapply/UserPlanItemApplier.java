package com.cac.iam.service.apply.itemapply;

import com.cac.iam.model.FileCategory;
import com.cac.iam.service.apply.apiservice.UserApiService;
import com.finbourne.identity.model.CreateUserRequest;
import org.springframework.stereotype.Component;

@Component
public class UserPlanItemApplier extends AbstractPlanItemApplier<CreateUserRequest> {

    public UserPlanItemApplier(UserApiService apiService) {
        super(CreateUserRequest.class, apiService);
    }

    @Override
    public FileCategory supportedCategory() {
        return FileCategory.USERS;
    }
}
