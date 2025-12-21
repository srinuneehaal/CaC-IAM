package com.cac.iam.service.apply.impl;

import com.cac.iam.model.Action;
import com.cac.iam.model.FileCategory;
import com.cac.iam.model.PlanItem;
import com.cac.iam.repository.CosmosStateRepository;
import com.cac.iam.service.apply.AccessApiService;
import com.cac.iam.service.apply.PlanItemApplier;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class UserPlanItemApplier implements PlanItemApplier {

    private static final Logger log = LoggerFactory.getLogger(UserPlanItemApplier.class);

    private final AccessApiService accessApiService;
    private final CosmosStateRepository stateRepository;

    public UserPlanItemApplier(AccessApiService accessApiService, CosmosStateRepository stateRepository) {
        this.accessApiService = accessApiService;
        this.stateRepository = stateRepository;
    }

    @Override
    public boolean supports(FileCategory category) {
        return FileCategory.USERS == category;
    }

    @Override
    public void apply(PlanItem item) {
        ObjectNode payload = cast(item.getPayload());
        String key = resolveKey(payload, item);
        Action action = item.getAction();
        switch (action) {
            case NEW, UPDATE -> {
                accessApiService.upsertUser(payload);
                stateRepository.upsert(FileCategory.USERS, key, payload);
            }
            case DELETE -> {
                accessApiService.deleteUser(key);
                stateRepository.delete(FileCategory.USERS, key);
            }
            default -> log.warn("Unsupported action {} for user {}", action, key);
        }
    }

    private ObjectNode cast(Object payload) {
        if (payload instanceof ObjectNode node) {
            return node;
        }
        throw new IllegalArgumentException("User payload missing or wrong type");
    }

    private String resolveKey(ObjectNode payload, PlanItem item) {
        if (payload == null) {
            return item.getKey();
        }
        if (payload.hasNonNull("login") && StringUtils.hasText(payload.get("login").asText())) {
            return payload.get("login").asText();
        }
        if (payload.hasNonNull("emailAddress") && StringUtils.hasText(payload.get("emailAddress").asText())) {
            return payload.get("emailAddress").asText();
        }
        return item.getKey();
    }
}
