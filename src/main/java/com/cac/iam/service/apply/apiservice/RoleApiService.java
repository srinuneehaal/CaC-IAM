package com.cac.iam.service.apply.apiservice;

import com.cac.iam.exception.ApiServiceException;
import com.cac.iam.util.LoggerProvider;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.finbourne.access.ApiException;
import com.finbourne.access.api.RolesApi;
import com.finbourne.access.model.RoleCreationRequest;
import com.finbourne.access.model.RoleUpdateRequest;
import com.finbourne.identity.model.CreateRoleRequest;
import com.finbourne.identity.model.UpdateRoleRequest;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

import java.util.concurrent.Callable;

@Service
public class RoleApiService implements PlanItemActionService<RoleCreationRequest> {

    private final Logger log;
    private final RolesApi accessRolesApi;
    private final com.finbourne.identity.api.RolesApi identityRolesApi;
    private final ObjectMapper objectMapper;

    @org.springframework.beans.factory.annotation.Autowired
    public RoleApiService(LoggerProvider loggerProvider, ObjectMapper objectMapper) {
        this(loggerProvider, new RolesApi(), new com.finbourne.identity.api.RolesApi(), objectMapper);
    }

    RoleApiService(LoggerProvider loggerProvider,
                   RolesApi accessRolesApi,
                   com.finbourne.identity.api.RolesApi identityRolesApi,
                   ObjectMapper objectMapper) {
        this.log = loggerProvider.getLogger(getClass());
        this.accessRolesApi = accessRolesApi;
        this.identityRolesApi = identityRolesApi;
        this.objectMapper = objectMapper;
    }

    @Override
    public void create(String key, RoleCreationRequest payload) {
        execute(() -> {
            Object accessResponse = accessRolesApi.createRole(payload).executeWithHttpInfo();
            Object identityResponse = identityRolesApi.createRole(toIdentityCreateRequest(key, payload)).executeWithHttpInfo();
            return new ApiCallResults(accessResponse, identityResponse);
        }, "create role", key);
    }

    @Override
    public void update(String key, RoleCreationRequest payload) {
        RoleUpdateRequest updateRequest = new RoleUpdateRequest()
                .description(payload.getDescription())
                .resource(payload.getResource())
                .when(payload.getWhen());
        execute(() -> {
            Object accessResponse = accessRolesApi.updateRole(key, updateRequest).executeWithHttpInfo();
            Object identityResponse = identityRolesApi.updateRole(key)
                    .updateRoleRequest(toIdentityUpdateRequest(payload))
                    .executeWithHttpInfo();
            return new ApiCallResults(accessResponse, identityResponse);
        }, "update role", key);
    }

    @Override
    public void delete(String key, RoleCreationRequest payload) {
        execute(() -> {
            Object accessResponse = accessRolesApi.deleteRole(key).executeWithHttpInfo();
            Object identityResponse = identityRolesApi.deleteRole(key).executeWithHttpInfo();
            return new ApiCallResults(accessResponse, identityResponse);
        }, "delete role", key);
    }

    void execute(Callable<ApiCallResults> action, String verb, String key) {
        try {
            log.info("Role API execute the call with fbn {} {}", verb, key);
            ApiCallResults results = action.call();
            logResponses(verb, key, results);
        } catch (ApiException | com.finbourne.identity.ApiException e) {
            throw new ApiServiceException("Access API failure while attempting to " + verb + " " + key + ": " + e.getMessage(), e);
        } catch (Exception e) {
            throw new ApiServiceException("Unexpected failure while attempting to " + verb + " " + key + ": " + e.getMessage(), e);
        }
    }

    private CreateRoleRequest toIdentityCreateRequest(String key, RoleCreationRequest payload) {
        String name = key != null && !key.isBlank() ? key : payload.getCode();
        return new CreateRoleRequest()
                .name(name)
                .description(payload.getDescription());
    }

    private UpdateRoleRequest toIdentityUpdateRequest(RoleCreationRequest payload) {
        return new UpdateRoleRequest().description(payload.getDescription());
    }

    private void logResponses(String verb, String key, ApiCallResults results) {
        if (results == null) {
            log.debug("No responses captured for {} {}", verb, key);
            return;
        }
        log.debug("Access response for {} {}: {}", verb, key, toJson(results.accessResponse()));
        log.debug("Identity response for {} {}: {}", verb, key, toJson(results.identityResponse()));
    }

    private String toJson(Object value) {
        if (value == null) {
            return "null";
        }
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            return value.toString();
        }
    }

    record ApiCallResults(Object accessResponse, Object identityResponse) {
    }
}
