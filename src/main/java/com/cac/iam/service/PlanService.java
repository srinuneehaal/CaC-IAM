package com.cac.iam.service;

import com.cac.iam.exception.PlanProcessingException;
import com.cac.iam.exception.UnsupportedFileCategoryException;
import com.cac.iam.exception.UnsupportedFilePathException;
import com.cac.iam.model.*;
import com.cac.iam.repository.StateRepository;
import com.cac.iam.service.plan.rules.PlanOrderingRuleEngine;
import com.cac.iam.service.plan.stratagy.FileParsingStrategy;
import com.cac.iam.service.plan.stratagy.FileParsingStrategyFactory;
import com.cac.iam.util.LoggerProvider;
import com.cac.iam.util.PathUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

@Service
public class PlanService {

    private final Logger log;

    private final FileParsingStrategyFactory strategyFactory;
    private final PlanOrderingRuleEngine orderingRuleEngine;
    private final StateRepository stateRepository;
    private final ObjectMapper objectMapper;

    @org.springframework.beans.factory.annotation.Autowired
    public PlanService(FileParsingStrategyFactory strategyFactory,
                       PlanOrderingRuleEngine orderingRuleEngine,
                       StateRepository stateRepository,
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
        Map<FileCategory, Set<String>> requestedDeletes = detectDeletes(changedPaths);
        MasterPlan plan = new MasterPlan();
        processCategory(FileCategory.POLICIES, changedFiles.get(FileCategory.POLICIES),
                requestedDeletes.get(FileCategory.POLICIES), plan);
        processCategory(FileCategory.ROLES, changedFiles.get(FileCategory.ROLES),
                requestedDeletes.get(FileCategory.ROLES), plan);
        processCategory(FileCategory.USERS, changedFiles.get(FileCategory.USERS),
                requestedDeletes.get(FileCategory.USERS), plan);

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
                    log.info("Changed file missing on disk, skipping parse (possible delete): {}", path);
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
                                 Set<String> requestedDeletes,
                                 MasterPlan plan) {
        Map<String, LoadedFile> changedMap = changed == null ? Map.of() : changed;
        Set<String> deleteKeys = requestedDeletes == null ? Set.of() : requestedDeletes;

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

        // Add DELETE items only when explicitly requested via changed files
        for (String deleteKey : deleteKeys) {
            Object existingPayload = loadStatePayload(category, deleteKey);
            plan.addItem(new PlanItem(Action.DELETE, category, deleteKey,
                    cosmosStateReference(category, deleteKey), existingPayload));
        }
    }

    private Map<FileCategory, Set<String>> detectDeletes(List<Path> changedPaths) {
        Map<FileCategory, Set<String>> deletes = new EnumMap<>(FileCategory.class);
        if (changedPaths == null || changedPaths.isEmpty()) {
            return deletes;
        }
        for (Path path : changedPaths) {
            try {
                if (path == null || Files.exists(path)) {
                    continue;
                }
                FileParsingStrategy strategy = strategyFactory.resolve(path);
                FileCategory category = strategy.getCategory();
                if (category == null) {
                    log.warn("Skipping delete for {} because category could not be resolved", path);
                    continue;
                }
                String key = PathUtils.baseName(path);
                if (key == null || key.isBlank()) {
                    log.warn("Skipping delete for {} because no key could be derived", path);
                    continue;
                }
                deletes.computeIfAbsent(category, ignored -> new LinkedHashSet<>())
                        .add(key);
            } catch (UnsupportedFilePathException | UnsupportedFileCategoryException e) {
                log.warn("Ignoring unsupported file {}: {}", path, e.getMessage());
            } catch (Exception e) {
                log.error("Failed to process delete candidate {}: {}", path, e.getMessage(), e);
            }
        }
        return deletes;
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
