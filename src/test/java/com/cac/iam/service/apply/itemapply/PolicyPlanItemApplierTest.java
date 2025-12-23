package com.cac.iam.service.apply.itemapply;

import com.cac.iam.model.FileCategory;
import com.cac.iam.service.apply.apiservice.PolicyApiService;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class PolicyPlanItemApplierTest {

    @Test
    void supportsPolicies() {
        PolicyPlanItemApplier applier = new PolicyPlanItemApplier(mock(PolicyApiService.class));
        assertThat(applier.supportedCategory()).isEqualTo(FileCategory.POLICIES);
    }
}
