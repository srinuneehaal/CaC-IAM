package com.cac.iam.model;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.finbourne.access.model.PolicyCreationRequest;
import com.finbourne.access.model.RoleCreationRequest;

import java.util.Collections;
import java.util.Map;

public class StateSnapshot {

    private final Map<String, PolicyCreationRequest> policies;
    private final Map<String, RoleCreationRequest> roles;
    private final Map<String, ObjectNode> users;

    public StateSnapshot(Map<String, PolicyCreationRequest> policies,
                         Map<String, RoleCreationRequest> roles,
                         Map<String, ObjectNode> users) {
        this.policies = policies == null ? Collections.emptyMap() : Collections.unmodifiableMap(policies);
        this.roles = roles == null ? Collections.emptyMap() : Collections.unmodifiableMap(roles);
        this.users = users == null ? Collections.emptyMap() : Collections.unmodifiableMap(users);
    }

    public Map<String, PolicyCreationRequest> getPolicies() {
        return policies;
    }

    public Map<String, RoleCreationRequest> getRoles() {
        return roles;
    }

    public Map<String, ObjectNode> getUsers() {
        return users;
    }
}
