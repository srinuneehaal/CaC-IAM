package com.cac.iam.service.apply;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.finbourne.access.model.PolicyCreationRequest;
import com.finbourne.access.model.RoleCreationRequest;

public interface AccessApiService {

    void upsertPolicy(PolicyCreationRequest policy);

    void deletePolicy(String code);

    void upsertRole(RoleCreationRequest role);

    void deleteRole(String code);

    void upsertUser(ObjectNode userPayload);

    void deleteUser(String userKey);
}
