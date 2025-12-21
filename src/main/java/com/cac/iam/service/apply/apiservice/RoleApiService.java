package com.cac.iam.service.apply.apiservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finbourne.access.ApiException;
import com.finbourne.access.api.RolesApi;
import com.finbourne.access.model.RoleCreationRequest;
import com.finbourne.access.model.RoleUpdateRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.Callable;

@Service
public class RoleApiService implements PlanItemActionService<RoleCreationRequest> {

    private static final Logger log = LoggerFactory.getLogger(RoleApiService.class);

    @Override
    public void create(String scope, String key, RoleCreationRequest payload) {
        log.info("Create role {} in scope {} with payload {}", key, scope, payload);

    }

    @Override
    public void update(String scope, String key, RoleCreationRequest payload) {
        log.info("Update role {} in scope {} with payload {}", key, scope, payload);

    }

    @Override
    public void delete(String scope, String key, RoleCreationRequest payload) {
        log.info("Delete role {} in scope {}", key, scope);

    }
}
