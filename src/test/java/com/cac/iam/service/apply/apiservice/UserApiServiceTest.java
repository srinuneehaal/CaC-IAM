package com.cac.iam.service.apply.apiservice;

import com.cac.iam.util.LoggerProvider;
import com.finbourne.identity.model.CreateUserRequest;
import org.junit.jupiter.api.Test;

class UserApiServiceTest {

    @Test
    void logsCallsWithoutException() {
        UserApiService service = new UserApiService(new LoggerProvider());
        CreateUserRequest payload = new CreateUserRequest();

        service.create("k", payload);
        service.update("k", payload);
        service.delete("k", payload);
    }
}
