package com.cac.iam.service.apply.apiservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finbourne.access.ApiException;
import com.finbourne.access.api.PoliciesApi;
import com.finbourne.access.model.PolicyCreationRequest;
import com.finbourne.access.model.PolicyUpdateRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.Callable;

@Service
public class PolicyApiService implements PlanItemActionService<PolicyCreationRequest> {

    private static final Logger log = LoggerFactory.getLogger(PolicyApiService.class);


    @Override
    public void create(String scope, String key, PolicyCreationRequest payload) {
        log.info("Create policy {} in scope {} with payload {}", key, scope, payload);
    }

    @Override
    public void update(String scope, String key, PolicyCreationRequest payload) {
        log.info("Update policy {} in scope {} with payload {}", key, scope, payload);

    }

    @Override
    public void delete(String scope, String key, PolicyCreationRequest payload) {
        log.info("Delete policy {} in scope {}", key, scope);

    }
}
