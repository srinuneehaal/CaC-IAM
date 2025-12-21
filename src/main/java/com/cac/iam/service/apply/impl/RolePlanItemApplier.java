package com.cac.iam.service.apply.impl;

import com.cac.iam.model.Action;
import com.cac.iam.model.FileCategory;
import com.cac.iam.model.PlanItem;
import com.cac.iam.repository.CosmosStateRepository;
import com.cac.iam.service.apply.AccessApiService;
import com.cac.iam.service.apply.PlanItemApplier;
import com.finbourne.access.model.RoleCreationRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class RolePlanItemApplier implements PlanItemApplier {

    private static final Logger log = LoggerFactory.getLogger(RolePlanItemApplier.class);

    private final AccessApiService accessApiService;
    private final CosmosStateRepository stateRepository;

    public RolePlanItemApplier(AccessApiService accessApiService, CosmosStateRepository stateRepository) {
        this.accessApiService = accessApiService;
        this.stateRepository = stateRepository;
    }

    @Override
    public boolean supports(FileCategory category) {
        return FileCategory.ROLES == category;
    }

    @Override
    public void apply(PlanItem item) {
        RoleCreationRequest payload = cast(item.getPayload());
        String code = payload.getCode() != null ? payload.getCode() : item.getKey();
        Action action = item.getAction();
        switch (action) {
            case NEW, UPDATE -> {
                accessApiService.upsertRole(payload);
                stateRepository.upsert(FileCategory.ROLES, code, payload);
            }
            case DELETE -> {
                accessApiService.deleteRole(code);
                stateRepository.delete(FileCategory.ROLES, code);
            }
            default -> log.warn("Unsupported action {} for role {}", action, code);
        }
    }

    private RoleCreationRequest cast(Object payload) {
        if (payload instanceof RoleCreationRequest role) {
            return role;
        }
        throw new IllegalArgumentException("Role payload missing or wrong type");
    }
}
