package com.cac.iam.service.apply.apiservice;

import com.cac.iam.exception.ApiServiceException;
import com.cac.iam.util.LoggerProvider;
import com.finbourne.identity.model.CreateUserRequest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserApiServiceTest {

    @Test
    void logsCallsWithoutException() {
        UserApiService service = new UserApiService(new LoggerProvider());
        CreateUserRequest payload = new CreateUserRequest();

        service.create("k", payload);
        service.update("k", payload);
        service.delete("k", payload);
    }

    @Test
    void executeWrapsUnexpectedExceptionsInApiServiceException() {
        UserApiService service = new UserApiService(new LoggerProvider());

        assertThatThrownBy(() -> service.execute(() -> {
            throw new IllegalStateException("boom");
        }, "create user", "k"))
                .isInstanceOf(ApiServiceException.class)
                .hasMessage("Unexpected failure while attempting to create user k: boom")
                .hasCauseInstanceOf(IllegalStateException.class);
    }
}
