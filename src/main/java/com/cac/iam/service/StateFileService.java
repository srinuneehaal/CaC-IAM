package com.cac.iam.service;

import com.cac.iam.exception.PlanApplyException;
import com.cac.iam.model.Action;
import com.cac.iam.model.FileCategory;
import com.cac.iam.model.PlanItem;
import com.cac.iam.repository.CosmosStateRepository;
import com.finbourne.access.model.PolicyCreationRequest;
import com.finbourne.access.model.RoleCreationRequest;
import com.finbourne.identity.model.CreateUserRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class StateFileService {

    private static final Logger log = LoggerFactory.getLogger(StateFileService.class);

    private final CosmosStateRepository stateRepository;

    public StateFileService(CosmosStateRepository stateRepository) {
        this.stateRepository = stateRepository;
    }

    /**
     * Applies a state change for the given plan item by upserting or deleting the record in Cosmos DB.
     *
     * @param item plan item to persist
     */
    public void applyStateChange(PlanItem item) {
        if (item == null) {
            return;
        }
        if (item.getFileCategory() == null || item.getAction() == null) {
            throw new PlanApplyException("Plan item missing category or action; cannot update state file");
        }
        String key = item.getKey();
        if (!StringUtils.hasText(key)) {
            throw new PlanApplyException("Plan item missing key; cannot update state file");
        }
        FileCategory category = item.getFileCategory();
        switch (category) {
            case POLICIES -> applyPolicy(item, key);
            case ROLES -> applyRole(item, key);
            case USERS -> applyUser(item, key);
            default -> log.warn("No state update defined for category {}", category);
        }
    }

    private void applyPolicy(PlanItem item, String key) {
        if (item.getAction() == Action.DELETE) {
            stateRepository.delete(FileCategory.POLICIES, key);
            return;
        }
        PolicyCreationRequest payload = castPayload(item, PolicyCreationRequest.class);
        if (!StringUtils.hasText(payload.getCode())) {
            payload.setCode(key);
        }
        stateRepository.upsert(FileCategory.POLICIES, key, payload, item.getScope());
    }

    private void applyRole(PlanItem item, String key) {
        if (item.getAction() == Action.DELETE) {
            stateRepository.delete(FileCategory.ROLES, key);
            return;
        }
        RoleCreationRequest payload = castPayload(item, RoleCreationRequest.class);
        if (!StringUtils.hasText(payload.getCode())) {
            payload.setCode(key);
        }
        stateRepository.upsert(FileCategory.ROLES, key, payload, item.getScope());
    }

    private void applyUser(PlanItem item, String key) {
        if (item.getAction() == Action.DELETE) {
            stateRepository.delete(FileCategory.USERS, key);
            return;
        }
        CreateUserRequest payload = castPayload(item, CreateUserRequest.class);
        if (!StringUtils.hasText(payload.getLogin())) {
            payload.setLogin(key);
        }
        if (!StringUtils.hasText(payload.getEmailAddress())) {
            payload.setEmailAddress(key);
        }
        stateRepository.upsert(FileCategory.USERS, key, payload, item.getScope());
    }

    private <T> T castPayload(PlanItem item, Class<T> type) {
        Object payload = item.getPayload();
        if (payload == null) {
            throw new PlanApplyException("Plan item " + item.getKey() + " missing payload for " + item.getAction());
        }
        if (!type.isInstance(payload)) {
            throw new PlanApplyException("Plan item " + item.getKey() + " expected payload of type "
                    + type.getSimpleName() + " but found " + payload.getClass().getSimpleName());
        }
        return type.cast(payload);
    }
}
