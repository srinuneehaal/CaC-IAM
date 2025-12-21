package com.cac.iam.repository;

import com.azure.cosmos.CosmosContainer;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.util.CosmosPagedIterable;
import com.cac.iam.model.FileCategory;
import com.cac.iam.model.StateDocument;
import com.cac.iam.model.StateSnapshot;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.finbourne.access.model.PolicyCreationRequest;
import com.finbourne.access.model.RoleCreationRequest;
import com.finbourne.identity.model.CreateUserRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.LinkedHashMap;
import java.util.Map;

@Repository
public class CosmosStateRepository {

    private static final Logger log = LoggerFactory.getLogger(CosmosStateRepository.class);

    private final CosmosContainer container;
    private final ObjectMapper objectMapper;

    public CosmosStateRepository(CosmosContainer container, ObjectMapper objectMapper) {
        this.container = container;
        this.objectMapper = objectMapper;
    }

    /**
     * Loads the latest snapshot of roles and policies stored in Cosmos DB.
     *
     * @return state snapshot (empty maps when Cosmos cannot be reached)
     */
    public StateSnapshot loadSnapshot() {
        Map<String, PolicyCreationRequest> policies =
                loadByCategory(FileCategory.POLICIES, PolicyCreationRequest.class);
        Map<String, RoleCreationRequest> roles =
                loadByCategory(FileCategory.ROLES, RoleCreationRequest.class);
        Map<String, CreateUserRequest> users = loadByCategory(FileCategory.USERS, CreateUserRequest.class);
        return new StateSnapshot(policies, roles, users);
    }

    /**
     * Upserts the supplied payload into the cosmos state container using the category as the partition key.
     */
    public void upsert(FileCategory category, String id, Object payload) {
        upsert(category, id, payload, null);
    }

    /**
     * Upserts the supplied payload into the cosmos state container using the category as the partition key, recording
     * the supplied scope when present.
     */
    public void upsert(FileCategory category, String id, Object payload, String scope) {
        if (!StringUtils.hasText(id)) {
            log.warn("Cannot upsert {} with blank id", category);
            return;
        }
        try {
            StateDocument document = new StateDocument();
            document.setId(id);
            document.setTypeOfItem(category.name());
            document.setScope(scope == null ? "" : scope);
            document.setData(objectMapper.valueToTree(payload));
            container.upsertItem(document, new PartitionKey(category.name()), null);
        } catch (Exception e) {
            log.error("Failed to upsert state for {} id {}: {}", category, id, e.getMessage(), e);
        }
    }

    public void delete(FileCategory category, String id) {
        if (!StringUtils.hasText(id)) {
            log.warn("Cannot delete {} with blank id", category);
            return;
        }
        try {
            container.deleteItem(id, new PartitionKey(category.name()), null);
        } catch (CosmosException e) {
            if (e.getStatusCode() == 404) {
                log.info("State document {} for {} already absent", id, category);
            } else {
                log.error("Failed to delete state for {} id {}: {}", category, id, e.getMessage(), e);
            }
        } catch (Exception e) {
            log.error("Unexpected error deleting state for {} id {}: {}", category, id, e.getMessage(), e);
        }
    }

    /**
     * Reads all documents for a category and converts them into typed payloads keyed by code.
     */
    public <T> Map<String, T> loadByCategory(FileCategory category, Class<T> payloadType) {
        Map<String, T> results = new LinkedHashMap<>();
        try {
            CosmosPagedIterable<StateDocument> documents =
                    container.readAllItems(new PartitionKey(category.name()), StateDocument.class);
            for (StateDocument document : documents) {
                T payload = toPayload(document, payloadType);
                String key = extractKey(payload, document);
                if (payload != null && StringUtils.hasText(key)) {
                    results.put(key, payload);
                }
            }
        } catch (CosmosException e) {
            log.error("Failed to load state for category {} from Cosmos: {}", category, e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error loading state for category {}: {}", category, e.getMessage(), e);
        }
        return results;
    }

    private <T> T toPayload(StateDocument document, Class<T> payloadType) {
        JsonNode data = document == null ? null : document.getData();
        if (data == null) {
            return null;
        }
        try {
            return objectMapper.treeToValue(data, payloadType);
        } catch (Exception e) {
            log.warn("Unable to parse document {} as {}: {}", document.getId(), payloadType.getSimpleName(), e.getMessage());
            return null;
        }
    }

    private <T> String extractKey(T payload, StateDocument document) {
        if (payload instanceof PolicyCreationRequest policy && StringUtils.hasText(policy.getCode())) {
            return policy.getCode();
        }
        if (payload instanceof RoleCreationRequest role && StringUtils.hasText(role.getCode())) {
            return role.getCode();
        }
        if (payload instanceof CreateUserRequest user) {
            if (StringUtils.hasText(user.getLogin())) {
                return user.getLogin();
            }
            if (StringUtils.hasText(user.getEmailAddress())) {
                return user.getEmailAddress();
            }
        }
        if (document != null && StringUtils.hasText(document.getId())) {
            return document.getId();
        }
        return null;
    }

    private String textValue(JsonNode node) {
        return node != null && node.isTextual() ? node.asText() : null;
    }
}
