package com.cac.iam.service.apply.apiservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.finbourne.identity.model.CreateUserRequest;

@Service
public class UserApiService implements PlanItemActionService<CreateUserRequest> {

    private static final Logger log = LoggerFactory.getLogger(UserApiService.class);

    @Override
    public void create(String key, CreateUserRequest payload) {
        log.info("Create user {} with payload {}", key, payload);
    }

    @Override
    public void update(String key, CreateUserRequest payload) {
        log.info("Update user {} with payload {}", key, payload);
    }

    @Override
    public void delete(String key, CreateUserRequest payload) {
        log.info("Delete user {}", key);
    }
}
