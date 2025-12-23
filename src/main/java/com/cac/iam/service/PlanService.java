package com.cac.iam.service;

import com.cac.iam.exception.PlanProcessingException;
import com.cac.iam.exception.UnsupportedFileCategoryException;
import com.cac.iam.exception.UnsupportedFilePathException;
import com.cac.iam.model.Action;
import com.cac.iam.model.FileCategory;
import com.cac.iam.model.LoadedFile;
import com.cac.iam.model.MasterPlan;
import com.cac.iam.model.PlanItem;
import com.cac.iam.repository.CosmosStateRepository;
import com.cac.iam.service.plan.rules.PlanOrderingRuleEngine;
import com.cac.iam.service.plan.stratagy.FileParsingStrategy;
import com.cac.iam.service.plan.stratagy.FileParsingStrategyFactory;
import com.cac.iam.util.LoggerProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class PlanService {

    private final Logger log;

    private final FileParsingStrategyFactory strategyFactory;
    private final PlanOrderingRuleEngine orderingRuleEngine;
    private final CosmosStateRepository stateRepository;
    private final ObjectMapper objectMapper;

    @org.springframework.beans.factory.annotation.Autowired
    public PlanService(FileParsingStrategyFactory strategyFactory,
                       PlanOrderingRuleEngine orderingRuleEngine,
                       CosmosStateRepository stateRepository,
                       ObjectMapper objectMapper,
                       LoggerProvider loggerProvider) {
        this.strategyFactory = strategyFactory;
        this.orderingRuleEngine = orderingRuleEngine;
        this.stateRepository = stateRepository;
        this.objectMapper = objectMapper;
        this.log = loggerProvider.getLogger(getClass());
    }

    /**
     * Builds an ordered master plan for the given set of changed file paths.
     *
     * @param changedPaths paths under the configured changed files root
     * @return ordered master plan
     */
    public MasterPlan buildPlan(List<Path> changedPaths) {
        Map<FileCategory, Map<String, LoadedFile>> changedFiles = loadChangedFiles(changedPaths);
        MasterPlan plan = new MasterPlan();
        processCategory(FileCategory.POLICIES, changedFiles.get(FileCategory.POLICIES), plan);
        processCategory(FileCategory.ROLES, changedFiles.get(FileCategory.ROLES), plan);
        processCategory(FileCategory.USERS, changedFiles.get(FileCategory.USERS), plan);

        return orderingRuleEngine.applyOrdering(plan);
    }

    private Map<FileCategory, Map<String, LoadedFile>> loadChangedFiles(List<Path> changedPaths) {
        Map<FileCategory, Map<String, LoadedFile>> result = new EnumMap<>(FileCategory.class);
        if (changedPaths == null || changedPaths.isEmpty()) {
            return result;
        }
        for (Path path : changedPaths) {
            try {
                if (path == null || !Files.exists(path)) {
                    log.warn("Skipping missing changed file: {}", path);
                    continue;
                }
                FileParsingStrategy strategy = strategyFactory.resolve(path);
                LoadedFile loaded = strategy.parse(path);
                result.computeIfAbsent(loaded.getCategory(), ignored -> new LinkedHashMap<>())
                        .put(loaded.getKey(), loaded);
            } catch (UnsupportedFilePathException | UnsupportedFileCategoryException e) {
                log.warn("Ignoring unsupported file {}: {}", path, e.getMessage());
            } catch (Exception e) {
                log.error("Failed to parse changed file {}: {}", path, e.getMessage(), e);
            }
        }
        return result;
    }

    private void processCategory(FileCategory category,
                                 Map<String, LoadedFile> changed,
                                 MasterPlan plan) {
        Map<String, LoadedFile> changedMap = changed == null ? Map.of() : changed;

        // Add NEW/UPDATE items by lazily fetching existing state per key
        for (LoadedFile file : changedMap.values()) {
            Object statePayload = loadStatePayload(category, file.getKey());
            if (statePayload == null) {
                plan.addItem(new PlanItem(Action.NEW, category, file.getKey(),
                        file.getPath().toString(), file.getPayload()));
            } else if (!payloadsEqual(file.getPayload(), statePayload)) {
                PlanItem update = new PlanItem(Action.UPDATE, category, file.getKey(),
                        file.getPath().toString(), file.getPayload());
                update.setBeforePayload(statePayload);
                plan.addItem(update);
            }
        }

        // Add DELETE items by listing keys then lazily pulling payloads only when needed
        for (String existingKey : stateRepository.listKeys(category)) {
            if (!changedMap.containsKey(existingKey)) {
                Object existingPayload = loadStatePayload(category, existingKey);
                plan.addItem(new PlanItem(Action.DELETE, category, existingKey,
                        cosmosStateReference(category, existingKey), existingPayload));
            }
        }
    }

    private boolean payloadsEqual(Object left, Object right) {
        if (Objects.equals(left, right)) {
            return true;
        }
        if (left == null || right == null) {
            return false;
        }
        try {
            return objectMapper.valueToTree(left).equals(objectMapper.valueToTree(right));
        } catch (IllegalArgumentException e) {
            throw new PlanProcessingException("Failed to compare payloads: " + e.getMessage(), e);
        }
    }

    private Object loadStatePayload(FileCategory category, String key) {
        Class<?> payloadType = switch (category) {
            case POLICIES -> com.finbourne.access.model.PolicyCreationRequest.class;
            case ROLES -> com.finbourne.access.model.RoleCreationRequest.class;
            case USERS -> com.finbourne.identity.model.CreateUserRequest.class;
        };
        return stateRepository.findPayload(category, key, payloadType).orElse(null);
    }

    private String cosmosStateReference(FileCategory category, String key) {
        return "cosmos://" + category.name() + "/" + key;
    }
}
