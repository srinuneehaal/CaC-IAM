package com.cac.iam.service.apply.apiservice;

import com.cac.iam.config.JacksonConfiguration;
import com.cac.iam.util.LoggerProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.finbourne.access.ApiException;
import com.finbourne.access.api.PoliciesApi;
import com.finbourne.access.model.PolicyCreationRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PolicyApiServiceTest {

    @Mock
    private PoliciesApi policiesApi;

    @Mock
    private PoliciesApi.APIcreatePolicyRequest createRequest;

    @Mock
    private PoliciesApi.APIupdatePolicyRequest updateRequest;

    @Mock
    private PoliciesApi.APIdeletePolicyRequest deleteRequest;

    private PolicyApiService service;

    @BeforeEach
    void setUp() {
        ObjectMapper mapper = JacksonConfiguration.createObjectMapper();
        service = new PolicyApiService(new LoggerProvider(), policiesApi, mapper);
    }

    @Test
    void delegatesCreateUpdateDelete() throws Exception {
        PolicyCreationRequest payload = new PolicyCreationRequest().code("c1");
        when(policiesApi.createPolicy(payload)).thenReturn(createRequest);
        when(policiesApi.updatePolicy(eq("c1"), any())).thenReturn(updateRequest);
        when(policiesApi.deletePolicy("c1")).thenReturn(deleteRequest);

        service.create("c1", payload);
        service.update("c1", payload);
        service.delete("c1", payload);

        verify(createRequest).execute();
        verify(updateRequest).execute();
        verify(deleteRequest).execute();
    }

    @Test
    void wrapsApiException() throws Exception {
        assertThatThrownBy(() -> service.execute(() -> { throw new ApiException("bad"); }, "create policy", "k"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Access API failure");
    }
}
