package com.cac.iam.service.apply;

import com.cac.iam.config.FileLocationProperties;
import com.cac.iam.model.MasterPlan;
import com.cac.iam.model.PlanItem;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.finbourne.access.model.PolicyCreationRequest;
import com.finbourne.access.model.RoleCreationRequest;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Component
public class PlanReader {

    private final FileLocationProperties fileLocationProperties;
    private final ObjectMapper objectMapper;

    public PlanReader(FileLocationProperties fileLocationProperties, ObjectMapper objectMapper) {
        this.fileLocationProperties = fileLocationProperties;
        this.objectMapper = objectMapper;
    }

    public MasterPlan read() {
        Path masterPlanPath = fileLocationProperties.masterPlanPath();
        if (!Files.exists(masterPlanPath)) {
            throw new IllegalStateException("Master plan not found at " + masterPlanPath.toAbsolutePath());
        }
        try {
            MasterPlan plan = objectMapper.readValue(masterPlanPath.toFile(), MasterPlan.class);
            rehydratePayloads(plan);
            return plan;
        } catch (IOException e) {
            throw new IllegalStateException("Unable to read master plan at " + masterPlanPath, e);
        }
    }

    private void rehydratePayloads(MasterPlan plan) {
        if (plan == null || plan.getItems() == null) {
            return;
        }
        for (PlanItem item : plan.getItems()) {
            if (item == null || item.getFileCategory() == null) {
                continue;
            }
            switch (item.getFileCategory()) {
                case POLICIES -> item.setPayload(convert(item.getPayload(), PolicyCreationRequest.class));
                case ROLES -> item.setPayload(convert(item.getPayload(), RoleCreationRequest.class));
                case USERS -> item.setPayload(convert(item.getPayload(), ObjectNode.class));
                default -> { }
            }
        }
    }

    private <T> T convert(Object payload, Class<T> type) {
        if (payload == null) {
            return null;
        }
        if (type.isInstance(payload)) {
            return type.cast(payload);
        }
        return objectMapper.convertValue(payload, type);
    }
}
