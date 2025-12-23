package com.cac.iam.service.apply.itemapply;

import com.cac.iam.model.FileCategory;
import com.cac.iam.service.apply.apiservice.RoleApiService;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class RolePlanItemApplierTest {

    @Test
    void supportsRoles() {
        RolePlanItemApplier applier = new RolePlanItemApplier(mock(RoleApiService.class));
        assertThat(applier.supportedCategory()).isEqualTo(FileCategory.ROLES);
    }
}
