package com.cac.iam.service.apply.itemapply;

import com.cac.iam.exception.InvalidPlanItemException;
import com.cac.iam.model.Action;
import com.cac.iam.model.FileCategory;
import com.cac.iam.model.PlanItem;
import com.cac.iam.service.apply.apiservice.PlanItemActionService;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class AbstractPlanItemApplierTest {

    @Test
    void delegatesActions() {
        @SuppressWarnings("unchecked")
        PlanItemActionService<String> apiService = mock(PlanItemActionService.class);
        PlanItem item = new PlanItem(Action.NEW, FileCategory.POLICIES, "k", "s", "payload");

        PlanItemApplier applier = new AbstractPlanItemApplier<>(String.class, apiService) {
            @Override
            public FileCategory supportedCategory() {
                return FileCategory.POLICIES;
            }
        };

        applier.apply(item);

        verify(apiService).create("k", "payload");
    }

    @Test
    void rejectsInvalidPayloadType() {
        @SuppressWarnings("unchecked")
        PlanItemActionService<String> apiService = mock(PlanItemActionService.class);
        PlanItem item = new PlanItem(Action.NEW, FileCategory.POLICIES, "k", "s", 42);

        PlanItemApplier applier = new AbstractPlanItemApplier<>(String.class, apiService) {
            @Override
            public FileCategory supportedCategory() {
                return FileCategory.POLICIES;
            }
        };

        assertThatThrownBy(() -> applier.apply(item)).isInstanceOf(InvalidPlanItemException.class);
    }

    @Test
    void rejectsNullPayloadForNew() {
        @SuppressWarnings("unchecked")
        PlanItemActionService<String> apiService = mock(PlanItemActionService.class);
        PlanItem item = new PlanItem(Action.NEW, FileCategory.POLICIES, "k", "s", null);

        PlanItemApplier applier = new AbstractPlanItemApplier<>(String.class, apiService) {
            @Override
            public FileCategory supportedCategory() {
                return FileCategory.POLICIES;
            }
        };

        assertThatThrownBy(() -> applier.apply(item)).isInstanceOf(InvalidPlanItemException.class);
    }
}
