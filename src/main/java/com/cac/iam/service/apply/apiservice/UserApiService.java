package com.cac.iam.service.apply.apiservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.finbourne.identity.model.CreateUserRequest;

@Service
public class UserApiService implements PlanItemActionService<CreateUserRequest> {

    private static final Logger log = LoggerFactory.getLogger(UserApiService.class);

    @Override
    public void create(String scope, String key, CreateUserRequest payload) {
        log.info("Create user {} in scope {} with payload {}", key, scope, payload);
    }

    @Override
    public void update(String scope, String key, CreateUserRequest payload) {
        log.info("Update user {} in scope {} with payload {}", key, scope, payload);
    }

    @Override
    public void delete(String scope, String key, CreateUserRequest payload) {
        log.info("Delete user {} in scope {}", key, scope);
    }
}
