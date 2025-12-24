package com.cac.iam.service.apply.apiservice;

import com.cac.iam.util.LoggerProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.finbourne.access.api.PoliciesApi;
import com.finbourne.access.model.PolicyCreationRequest;
import com.finbourne.access.model.PolicyUpdateRequest;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

import java.util.concurrent.Callable;

@Service
public class PolicyApiService implements PlanItemActionService<PolicyCreationRequest> {

    private final Logger log;
    private final PoliciesApi policiesApi;
    private final ObjectMapper objectMapper;

    @org.springframework.beans.factory.annotation.Autowired
    public PolicyApiService(LoggerProvider loggerProvider, ObjectMapper objectMapper) {
        this(loggerProvider, new PoliciesApi(), objectMapper);
    }

    PolicyApiService(LoggerProvider loggerProvider, PoliciesApi policiesApi, ObjectMapper objectMapper) {
        this.log = loggerProvider.getLogger(getClass());
        this.policiesApi = policiesApi;
        this.objectMapper = objectMapper;
    }

    @Override
    public void create(String key, PolicyCreationRequest payload) {
        execute(() -> {
            policiesApi.createPolicy(payload).execute();
            return null;
        }, "create policy", key);
    }

    @Override
    public void update(String key, PolicyCreationRequest payload) {
        PolicyUpdateRequest updateRequest = objectMapper.convertValue(payload, PolicyUpdateRequest.class);
        execute(() -> {
            policiesApi.updatePolicy(key, updateRequest).execute();
            return null;
        }, "update policy", key);
    }

    @Override
    public void delete(String key, PolicyCreationRequest payload) {
        execute(() -> {
            policiesApi.deletePolicy(key).execute();
            return null;
        }, "delete policy", key);
    }

    void execute(Callable<?> action, String verb, String key) {
        try {
            log.info("Policy API execute the call with fbn {} {}", verb, key);
           // action.call();
//        } catch (ApiException e) {
//            throw new RuntimeException("Access API failure while attempting to " + verb + " " + key + ": " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException("Unexpected failure while attempting to " + verb + " " + key + ": " + e.getMessage(), e);
        }
    }
}
