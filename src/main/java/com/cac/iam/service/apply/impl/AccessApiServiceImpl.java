package com.cac.iam.service.apply.impl;

import com.cac.iam.service.apply.AccessApiService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.finbourne.access.model.PolicyCreationRequest;
import com.finbourne.access.model.RoleCreationRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Thin abstraction over the Finbourne Access SDK. For now we log intent so the
 * application can run locally without credentials; swap the bodies to real SDK
 * calls when wiring Access configuration.
 */
@Service
public class AccessApiServiceImpl implements AccessApiService {

    private static final Logger log = LoggerFactory.getLogger(AccessApiServiceImpl.class);

    private final ObjectMapper objectMapper;

    public AccessApiServiceImpl(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void upsertPolicy(PolicyCreationRequest policy) {
        log.info("Applying policy upsert for code={} payload={}", policy.getCode(), safeJson(policy));
        // TODO: integrate with com.finbourne.access.api.PoliciesApi when Access credentials/config are available.
    }

    @Override
    public void deletePolicy(String code) {
        log.info("Applying policy delete for code={}", code);
        // TODO: integrate with PoliciesApi.deletePolicy(...) when available.
    }

    @Override
    public void upsertRole(RoleCreationRequest role) {
        log.info("Applying role upsert for code={} payload={}", role.getCode(), safeJson(role));
        // TODO: integrate with com.finbourne.access.api.RolesApi.
    }

    @Override
    public void deleteRole(String code) {
        log.info("Applying role delete for code={}", code);
        // TODO: integrate with RolesApi.deleteRole(...) when available.
    }

    @Override
    public void upsertUser(ObjectNode userPayload) {
        log.info("Applying user upsert for key={} payload={}", key(userPayload), userPayload);
        // TODO: integrate with appropriate Access user API when available.
    }

    @Override
    public void deleteUser(String userKey) {
        log.info("Applying user delete for key={}", userKey);
        // TODO: integrate with Access user delete API.
    }

    private String safeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            return "<unable to serialize>";
        }
    }

    private String key(ObjectNode node) {
        if (node == null) {
            return "";
        }
        if (node.hasNonNull("login")) {
            return node.get("login").asText();
        }
        if (node.hasNonNull("emailAddress")) {
            return node.get("emailAddress").asText();
        }
        return "";
    }
}
