package com.cac.iam.model;

import com.finbourne.access.model.PolicyCreationRequest;
import com.finbourne.access.model.RoleCreationRequest;
import com.finbourne.identity.model.CreateUserRequest;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StateSnapshotTest {

    @Test
    void snapshotIsUnmodifiable() {
        PolicyCreationRequest policy = new PolicyCreationRequest().code("p1");
        RoleCreationRequest role = new RoleCreationRequest().code("r1");
        CreateUserRequest user = new CreateUserRequest().login("u1");
        StateSnapshot snapshot = new StateSnapshot(
                Map.of("p1", policy),
                Map.of("r1", role),
                Map.of("u1", user));

        assertThat(snapshot.getPolicies()).containsEntry("p1", policy);
        assertThat(snapshot.getRoles()).containsEntry("r1", role);
        assertThat(snapshot.getUsers()).containsEntry("u1", user);

        assertThatThrownBy(() -> snapshot.getPolicies().put("x", policy))
                .isInstanceOf(UnsupportedOperationException.class);
    }
}
