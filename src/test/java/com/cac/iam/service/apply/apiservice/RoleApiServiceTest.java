package com.cac.iam.service.apply.apiservice;

import com.cac.iam.util.LoggerProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.finbourne.access.ApiException;
import com.finbourne.access.api.RolesApi;
import com.finbourne.access.model.RoleCreationRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoleApiServiceTest {

    @Mock
    private RolesApi rolesApi;

    @Mock
    private RolesApi.APIcreateRoleRequest createRequest;

    @Mock
    private RolesApi.APIupdateRoleRequest updateRequest;

    @Mock
    private RolesApi.APIdeleteRoleRequest deleteRequest;

    private RoleApiService service;

    @BeforeEach
    void setUp() {
        service = new RoleApiService(new LoggerProvider(), rolesApi, new ObjectMapper());
    }

    @Test
    void delegatesCreateUpdateDelete() throws Exception {
        RoleCreationRequest payload = new RoleCreationRequest().description("d");
        when(rolesApi.createRole(payload)).thenReturn(createRequest);
        when(rolesApi.updateRole(eq("r1"), any())).thenReturn(updateRequest);
        when(rolesApi.deleteRole("r1")).thenReturn(deleteRequest);

        service.create("r1", payload);
        service.update("r1", payload);
        service.delete("r1", payload);

        verify(createRequest).execute();
        verify(updateRequest).execute();
        verify(deleteRequest).execute();
    }

    @Test
    void wrapsApiException() throws Exception {
        assertThatThrownBy(() -> service.execute(() -> { throw new ApiException("bad"); }, "delete role", "r1"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Access API failure");
    }
}
