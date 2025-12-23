package com.cac.iam.service.apply.itemapply;

import com.cac.iam.model.FileCategory;
import com.cac.iam.service.apply.apiservice.UserApiService;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class UserPlanItemApplierTest {

    @Test
    void supportsUsers() {
        UserPlanItemApplier applier = new UserPlanItemApplier(mock(UserApiService.class));
        assertThat(applier.supportedCategory()).isEqualTo(FileCategory.USERS);
    }
}
