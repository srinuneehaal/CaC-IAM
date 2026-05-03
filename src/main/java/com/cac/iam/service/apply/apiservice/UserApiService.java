package com.cac.iam.service.apply.apiservice;

import com.cac.iam.exception.ApiServiceException;
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
        execute(() -> log.info("Create user {} with payload {}", key, payload), "create user", key);
    }

    @Override
    public void update(String key, CreateUserRequest payload) {
        execute(() -> log.info("Update user {} with payload {}", key, payload), "update user", key);
    }

    @Override
    public void delete(String key, CreateUserRequest payload) {
        execute(() -> log.info("Delete user {}", key), "delete user", key);
    }

    void execute(Runnable action, String verb, String key) {
        try {
            action.run();
        } catch (Exception e) {
            throw new ApiServiceException("Unexpected failure while attempting to " + verb + " " + key + ": " + e.getMessage(), e);
        }
    }
}
