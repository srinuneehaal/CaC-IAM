package com.cac.iam.service.apply;

import com.cac.iam.config.FileLocationProperties;
import com.cac.iam.model.FileCategory;
import com.cac.iam.model.MasterPlan;
import com.cac.iam.model.PlanItem;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.finbourne.access.model.PolicyCreationRequest;
import com.finbourne.access.model.RoleCreationRequest;
import com.finbourne.identity.model.CreateUserRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Component
public class PlanReader {

    private static final Logger log = LoggerFactory.getLogger(PlanReader.class);

    private final FileLocationProperties fileLocationProperties;
    private final ObjectMapper objectMapper;

    public PlanReader(FileLocationProperties fileLocationProperties, ObjectMapper objectMapper) {
        this.fileLocationProperties = fileLocationProperties;
        this.objectMapper = objectMapper;
    }

    /**
     * Reads and converts the configured master plan file into a {@link MasterPlan}.
     *
     * @return populated master plan
     * @throws IllegalStateException if the plan file is missing or cannot be parsed
     */
    public MasterPlan read() {
        Path inputPath = fileLocationProperties.masterPlanPath();
        if (!Files.exists(inputPath)) {
            throw new IllegalStateException("Plan file not found at " + inputPath.toAbsolutePath());
        }
        try {
            JsonNode root = objectMapper.readTree(inputPath.toFile());
            MasterPlan plan = new MasterPlan();
            JsonNode itemsNode = root.get("items");
            if (itemsNode == null || !itemsNode.isArray()) {
                log.warn("Plan file {} contains no items array", inputPath.toAbsolutePath());
                return plan;
            }
            for (JsonNode itemNode : itemsNode) {
                PlanItem planItem = objectMapper.treeToValue(itemNode, PlanItem.class);
                planItem.setPayload(convertPayload(planItem.getFileCategory(), itemNode.get("payload")));
                plan.addItem(planItem);
            }
            return plan;
        } catch (IOException e) {
            throw new IllegalStateException("Unable to read plan file", e);
        }
    }

    private Object convertPayload(FileCategory category, JsonNode payloadNode) {
        if (payloadNode == null || payloadNode.isNull() || category == null) {
            return null;
        }
        return switch (category) {
            case POLICIES -> objectMapper.convertValue(payloadNode, PolicyCreationRequest.class);
            case ROLES -> objectMapper.convertValue(payloadNode, RoleCreationRequest.class);
            case USERS -> objectMapper.convertValue(payloadNode, CreateUserRequest.class);
        };
    }
}
