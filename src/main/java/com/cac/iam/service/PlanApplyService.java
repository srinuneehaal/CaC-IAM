package com.cac.iam.service;

import com.cac.iam.exception.MissingApplierException;
import com.cac.iam.exception.PlanApplyException;
import com.cac.iam.model.FileCategory;
import com.cac.iam.model.MasterPlan;
import com.cac.iam.model.PlanItem;
import com.cac.iam.service.apply.PlanReader;
import com.cac.iam.service.apply.itemapply.PlanItemApplier;
import com.cac.iam.util.LoggerProvider;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PlanApplyService {

    private final Logger log;

    private final PlanReader planReader;
    private final Map<FileCategory, PlanItemApplier> appliers;
    private final StateFileService stateFileService;

    /**
     * Creates a plan apply service with discovered appliers and state updates.
     *
     * @param planReader         reader for the master plan
     * @param stateFileService   state file updater
     * @param discoveredAppliers appliers keyed by category
     */
    @org.springframework.beans.factory.annotation.Autowired
    public PlanApplyService(PlanReader planReader,
                            StateFileService stateFileService,
                            List<PlanItemApplier> discoveredAppliers,
                            LoggerProvider loggerProvider) {
        this.planReader = planReader;
        this.stateFileService = stateFileService;
        this.appliers = discoveredAppliers.stream()
                .collect(Collectors.toMap(PlanItemApplier::supportedCategory, applier -> applier,
                        (a, b) -> a, () -> new EnumMap<>(FileCategory.class)));
        this.log = loggerProvider.getLogger(getClass());
    }

    /**
     * Reads the master plan and applies each plan item using the matching applier and state updater.
     */
    public void applyPlan() {
        MasterPlan masterPlan = readMasterPlan();
        if (masterPlan.getItems().isEmpty()) {
            log.warn("No plan items found; nothing to apply.");
            return;
        }
        log.info("Applying {} plan item(s) from master plan", masterPlan.getItems().size());
        for (PlanItem item : masterPlan.getItems()) {
            PlanItemApplier applier;
            try {
                applier = resolveApplier(item.getFileCategory());
            } catch (MissingApplierException e) {
                log.error("Skipping item {}: {}", item.getKey(), e.getMessage());
                continue;
            }

            boolean apiApplied = false;
            try {
                applier.apply(item);
                apiApplied = true;
            } catch (PlanApplyException e) {
                log.error("Failed to apply item {}: {}", item.getKey(), e.getMessage(), e);
            } catch (Exception e) {
                log.error("Unexpected error applying item {}: {}", item.getKey(), e.getMessage(), e);
            }

            if (!apiApplied) {
                continue;
            }

            try {
                stateFileService.applyStateChange(item);
            } catch (PlanApplyException e) {
                log.error("State update failed for item {} after successful apply: {}", item.getKey(), e.getMessage(), e);
            } catch (Exception e) {
                log.error("Unexpected state update error for item {} after successful apply: {}", item.getKey(), e.getMessage(), e);
            }
        }
    }

    private MasterPlan readMasterPlan() {
        try {
            return planReader.read();
        } catch (Exception e) {
            throw new PlanApplyException("Failed to read master plan", e);
        }
    }

    private PlanItemApplier resolveApplier(FileCategory category) {
        PlanItemApplier applier = appliers.get(category);
        if (applier == null) {
            throw new MissingApplierException("No applier found for category " + category);
        }
        return applier;
    }
}
