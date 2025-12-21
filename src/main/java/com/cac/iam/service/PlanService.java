package com.cac.iam.service;

import com.cac.iam.exception.PlanProcessingException;
import com.cac.iam.exception.UnsupportedFileCategoryException;
import com.cac.iam.exception.UnsupportedFilePathException;
import com.cac.iam.model.Action;
import com.cac.iam.model.FileCategory;
import com.cac.iam.model.LoadedFile;
import com.cac.iam.model.MasterPlan;
import com.cac.iam.model.PlanItem;
import com.cac.iam.model.StateSnapshot;
import com.cac.iam.repository.CosmosStateRepository;
import com.cac.iam.service.plan.rules.PlanOrderingRuleEngine;
import com.cac.iam.service.plan.stratagy.FileParsingStrategy;
import com.cac.iam.service.plan.stratagy.FileParsingStrategyFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger log = LoggerFactory.getLogger(PlanService.class);

    private final FileParsingStrategyFactory strategyFactory;
    private final PlanOrderingRuleEngine orderingRuleEngine;
    private final CosmosStateRepository stateRepository;
    private final ObjectMapper objectMapper;

    public PlanService(FileParsingStrategyFactory strategyFactory,
                       PlanOrderingRuleEngine orderingRuleEngine,
                       CosmosStateRepository stateRepository,
                       ObjectMapper objectMapper) {
        this.strategyFactory = strategyFactory;
        this.orderingRuleEngine = orderingRuleEngine;
        this.stateRepository = stateRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * Builds an ordered master plan for the given set of changed file paths.
     *
     * @param changedPaths paths under the configured changed files root
     * @return ordered master plan
     */
    public MasterPlan buildPlan(List<Path> changedPaths) {
        Map<FileCategory, Map<String, LoadedFile>> changedFiles = loadChangedFiles(changedPaths);
        StateSnapshot stateSnapshot = stateRepository.loadSnapshot();

        MasterPlan plan = new MasterPlan();
        processCategory(FileCategory.POLICIES, changedFiles.get(FileCategory.POLICIES), stateSnapshot.getPolicies(), plan);
        processCategory(FileCategory.ROLES, changedFiles.get(FileCategory.ROLES), stateSnapshot.getRoles(), plan);
        processCategory(FileCategory.USERS, changedFiles.get(FileCategory.USERS), stateSnapshot.getUsers(), plan);

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
                                 Map<String, ?> state,
                                 MasterPlan plan) {
        Map<String, LoadedFile> changedMap = changed == null ? Map.of() : changed;
        Map<String, ?> stateMap = state == null ? Map.of() : state;

        for (LoadedFile file : changedMap.values()) {
            Object statePayload = stateMap.get(file.getKey());
            if (statePayload == null) {
                plan.addItem(new PlanItem(Action.NEW, category, "", file.getKey(),
                        file.getPath().toString(), file.getPayload()));
            } else if (!payloadsEqual(file.getPayload(), statePayload)) {
                PlanItem update = new PlanItem(Action.UPDATE, category, "", file.getKey(),
                        file.getPath().toString(), file.getPayload());
                update.setBeforePayload(statePayload);
                plan.addItem(update);
            }
        }

        for (Map.Entry<String, ?> entry : stateMap.entrySet()) {
            if (!changedMap.containsKey(entry.getKey())) {
                plan.addItem(new PlanItem(Action.DELETE, category, "", entry.getKey(),
                        cosmosStateReference(category, entry.getKey()), entry.getValue()));
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

    private String cosmosStateReference(FileCategory category, String key) {
        return "cosmos://" + category.name() + "/" + key;
    }
}
