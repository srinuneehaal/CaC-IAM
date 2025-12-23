package com.cac.iam.service.apply.apiservice;

import com.cac.iam.util.LoggerProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.finbourne.access.ApiException;
import com.finbourne.access.api.RolesApi;
import com.finbourne.access.model.RoleCreationRequest;
import com.finbourne.access.model.RoleUpdateRequest;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

import java.util.concurrent.Callable;

@Service
public class RoleApiService implements PlanItemActionService<RoleCreationRequest> {

    private final Logger log;
    private final RolesApi rolesApi;
    private final ObjectMapper objectMapper;

    @org.springframework.beans.factory.annotation.Autowired
    public RoleApiService(LoggerProvider loggerProvider, ObjectMapper objectMapper) {
        this(loggerProvider, new RolesApi(), objectMapper);
    }

    RoleApiService(LoggerProvider loggerProvider, RolesApi rolesApi, ObjectMapper objectMapper) {
        this.log = loggerProvider.getLogger(getClass());
        this.rolesApi = rolesApi;
        this.objectMapper = objectMapper;
    }

    @Override
    public void create(String key, RoleCreationRequest payload) {
        execute(() -> {
            rolesApi.createRole(payload).execute();
            return null;
        }, "create role", key);
    }

    @Override
    public void update(String key, RoleCreationRequest payload) {
        RoleUpdateRequest updateRequest = new RoleUpdateRequest()
                .description(payload.getDescription())
                .resource(payload.getResource())
                .when(payload.getWhen());
        execute(() -> {
            rolesApi.updateRole(key, updateRequest).execute();
            return null;
        }, "update role", key);
    }

    @Override
    public void delete(String key, RoleCreationRequest payload) {
        execute(() -> {
            rolesApi.deleteRole(key).execute();
            return null;
        }, "delete role", key);
    }

    void execute(Callable<?> action, String verb, String key) {
        try {
            log.info("{} {}", verb, key);
           // action.call();
//        } catch (ApiException e) {
//            throw new RuntimeException("Access API failure while attempting to " + verb + " " + key + ": " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException("Unexpected failure while attempting to " + verb + " " + key + ": " + e.getMessage(), e);
        }
    }
}
