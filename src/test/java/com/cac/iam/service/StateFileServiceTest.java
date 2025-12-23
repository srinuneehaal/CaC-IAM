package com.cac.iam.service;

import com.cac.iam.exception.PlanApplyException;
import com.cac.iam.model.Action;
import com.cac.iam.model.FileCategory;
import com.cac.iam.model.PlanItem;
import com.cac.iam.repository.StateRepository;
import com.cac.iam.util.LoggerProvider;
import com.finbourne.access.model.PolicyCreationRequest;
import com.finbourne.access.model.RoleCreationRequest;
import com.finbourne.identity.model.CreateUserRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class StateFileServiceTest {

    @Mock
    private StateRepository stateRepository;

    private StateFileService service;

    @BeforeEach
    void setUp() {
        service = new StateFileService(stateRepository, new LoggerProvider());
    }

    @Test
    void appliesPolicyUpsertAndDelete() {
        PolicyCreationRequest payload = new PolicyCreationRequest();
        PlanItem upsert = new PlanItem(Action.NEW, FileCategory.POLICIES, "p1", "s", payload);
        service.applyStateChange(upsert);
        verify(stateRepository).upsert(FileCategory.POLICIES, "p1", payload);

        PlanItem delete = new PlanItem(Action.DELETE, FileCategory.POLICIES, "p1", "s", payload);
        service.applyStateChange(delete);
        verify(stateRepository).delete(FileCategory.POLICIES, "p1");
    }

    @Test
    void appliesRoleUpsertAndDelete() {
        RoleCreationRequest payload = new RoleCreationRequest();
        PlanItem upsert = new PlanItem(Action.NEW, FileCategory.ROLES, "r1", "s", payload);
        service.applyStateChange(upsert);
        verify(stateRepository).upsert(FileCategory.ROLES, "r1", payload);

        PlanItem delete = new PlanItem(Action.DELETE, FileCategory.ROLES, "r1", "s", payload);
        service.applyStateChange(delete);
        verify(stateRepository).delete(FileCategory.ROLES, "r1");
    }

    @Test
    void appliesUserUpsertAndDelete() {
        CreateUserRequest payload = new CreateUserRequest();
        PlanItem upsert = new PlanItem(Action.NEW, FileCategory.USERS, "u1", "s", payload);
        service.applyStateChange(upsert);
        verify(stateRepository).upsert(FileCategory.USERS, "u1", payload);

        PlanItem delete = new PlanItem(Action.DELETE, FileCategory.USERS, "u1", "s", payload);
        service.applyStateChange(delete);
        verify(stateRepository).delete(FileCategory.USERS, "u1");
    }

    @Test
    void throwsWhenKeyMissing() {
        PlanItem item = new PlanItem(Action.NEW, FileCategory.USERS, " ", "s", new CreateUserRequest());
        assertThatThrownBy(() -> service.applyStateChange(item))
                .isInstanceOf(PlanApplyException.class);
    }

    @Test
    void returnsWhenItemNull() {
        service.applyStateChange(null);
        verifyNoInteractions(stateRepository);
    }

    @Test
    void throwsWhenCategoryOrActionMissing() {
        PlanItem item = new PlanItem();
        item.setKey("k");
        assertThatThrownBy(() -> service.applyStateChange(item))
                .isInstanceOf(PlanApplyException.class);
    }

    @Test
    void throwsWhenPayloadNullOrWrongType() {
        PlanItem nullPayload = new PlanItem(Action.NEW, FileCategory.USERS, "k", "s", null);
        assertThatThrownBy(() -> service.applyStateChange(nullPayload))
                .isInstanceOf(PlanApplyException.class);

        PlanItem wrongPayload = new PlanItem(Action.NEW, FileCategory.USERS, "k", "s", new Object());
        assertThatThrownBy(() -> service.applyStateChange(wrongPayload))
                .isInstanceOf(PlanApplyException.class);
    }

    @Test
    void userDefaultsFilledWhenMissing() {
        CreateUserRequest payload = new CreateUserRequest();
        PlanItem item = new PlanItem(Action.NEW, FileCategory.USERS, "user@example.com", "s", payload);

        service.applyStateChange(item);

        verify(stateRepository).upsert(FileCategory.USERS, "user@example.com", payload);
    }
}
