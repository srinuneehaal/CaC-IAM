package com.cac.iam.service.apply.apiservice;

import com.cac.iam.exception.ApiServiceException;
import com.cac.iam.util.LoggerProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.finbourne.access.ApiException;
import com.finbourne.access.api.RolesApi;
import com.finbourne.access.model.RoleCreationRequest;
import com.finbourne.access.model.RoleResourceRequest;
import com.finbourne.access.model.WhenSpec;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class RoleApiServiceTest {

    private final LoggerProvider loggerProvider = new LoggerProvider();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void createInvokesAccessAndIdentityApis() throws Exception {
        RolesApi accessApi = mock(RolesApi.class, RETURNS_DEEP_STUBS);
        com.finbourne.identity.api.RolesApi identityApi = mock(com.finbourne.identity.api.RolesApi.class, RETURNS_DEEP_STUBS);
        RoleApiService service = new RoleApiService(loggerProvider, accessApi, identityApi, objectMapper);

        RoleCreationRequest payload = new RoleCreationRequest()
                .code("role-1")
                .description("desc");

        service.create("role-1", payload);

        verify(accessApi.createRole(payload)).executeWithHttpInfo();
        verify(identityApi.createRole(any(com.finbourne.identity.model.CreateRoleRequest.class))).executeWithHttpInfo();
    }

    @Test
    void updateInvokesAccessAndIdentityApis() throws Exception {
        RolesApi accessApi = mock(RolesApi.class, RETURNS_DEEP_STUBS);
        com.finbourne.identity.api.RolesApi identityApi = mock(com.finbourne.identity.api.RolesApi.class, RETURNS_DEEP_STUBS);
        RoleApiService service = new RoleApiService(loggerProvider, accessApi, identityApi, objectMapper);

        RoleCreationRequest payload = new RoleCreationRequest()
                .description("new-desc")
                .resource(new RoleResourceRequest())
                .when(new WhenSpec());

        service.update("role-2", payload);

        verify(accessApi.updateRole(eq("role-2"), any())).executeWithHttpInfo();
        verify(identityApi.updateRole(eq("role-2")).updateRoleRequest(any(com.finbourne.identity.model.UpdateRoleRequest.class))).executeWithHttpInfo();
    }

    @Test
    void deleteInvokesAccessAndIdentityApis() throws Exception {
        RolesApi accessApi = mock(RolesApi.class, RETURNS_DEEP_STUBS);
        com.finbourne.identity.api.RolesApi identityApi = mock(com.finbourne.identity.api.RolesApi.class, RETURNS_DEEP_STUBS);
        RoleApiService service = new RoleApiService(loggerProvider, accessApi, identityApi, objectMapper);

        service.delete("role-3", new RoleCreationRequest());

        verify(accessApi.deleteRole("role-3")).executeWithHttpInfo();
        verify(identityApi.deleteRole("role-3")).executeWithHttpInfo();
    }

    @Test
    void executeWrapsSdkExceptionsInApiServiceException() {
        RoleApiService service = new RoleApiService(
                loggerProvider,
                mock(RolesApi.class, RETURNS_DEEP_STUBS),
                mock(com.finbourne.identity.api.RolesApi.class, RETURNS_DEEP_STUBS),
                objectMapper
        );

        assertThatThrownBy(() -> service.execute(() -> {
            throw new ApiException("boom");
        }, "create role", "role-4"))
                .isInstanceOf(ApiServiceException.class)
                .hasMessageContaining("Access API failure while attempting to create role role-4:")
                .hasMessageContaining("boom")
                .hasCauseInstanceOf(ApiException.class);
    }
}
