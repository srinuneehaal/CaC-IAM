package com.cac.iam.service.apply.impl;

import com.cac.iam.model.Action;
import com.cac.iam.model.FileCategory;
import com.cac.iam.model.PlanItem;
import com.cac.iam.repository.CosmosStateRepository;
import com.cac.iam.service.apply.AccessApiService;
import com.cac.iam.service.apply.PlanItemApplier;
import com.finbourne.access.model.PolicyCreationRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class PolicyPlanItemApplier implements PlanItemApplier {

    private static final Logger log = LoggerFactory.getLogger(PolicyPlanItemApplier.class);

    private final AccessApiService accessApiService;
    private final CosmosStateRepository stateRepository;

    public PolicyPlanItemApplier(AccessApiService accessApiService, CosmosStateRepository stateRepository) {
        this.accessApiService = accessApiService;
        this.stateRepository = stateRepository;
    }

    @Override
    public boolean supports(FileCategory category) {
        return FileCategory.POLICIES == category;
    }

    @Override
    public void apply(PlanItem item) {
        PolicyCreationRequest payload = cast(item.getPayload());
        String code = payload.getCode() != null ? payload.getCode() : item.getKey();
        Action action = item.getAction();
        switch (action) {
            case NEW, UPDATE -> {
                accessApiService.upsertPolicy(payload);
                stateRepository.upsert(FileCategory.POLICIES, code, payload);
            }
            case DELETE -> {
                accessApiService.deletePolicy(code);
                stateRepository.delete(FileCategory.POLICIES, code);
            }
            default -> log.warn("Unsupported action {} for policy {}", action, code);
        }
    }

    private PolicyCreationRequest cast(Object payload) {
        if (payload instanceof PolicyCreationRequest policy) {
            return policy;
        }
        throw new IllegalArgumentException("Policy payload missing or wrong type");
    }
}
