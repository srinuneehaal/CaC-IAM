package com.cac.iam.service.apply.apiservice;

import com.cac.iam.util.LoggerProvider;
import com.finbourne.identity.model.CreateUserRequest;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

@Service
public class UserApiService implements PlanItemActionService<CreateUserRequest> {

    private final Logger log;

    @org.springframework.beans.factory.annotation.Autowired
    public UserApiService(LoggerProvider loggerProvider) {
        this.log = loggerProvider.getLogger(getClass());
    }

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
