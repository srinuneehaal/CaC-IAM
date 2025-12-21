package com.cac.iam.service;

import com.cac.iam.exception.MissingApplierException;
import com.cac.iam.exception.PlanApplyException;
import com.cac.iam.model.FileCategory;
import com.cac.iam.model.MasterPlan;
import com.cac.iam.model.PlanItem;
import com.cac.iam.service.apply.PlanReader;
import com.cac.iam.service.apply.itemapply.PlanItemApplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PlanApplyService {

    private static final Logger log = LoggerFactory.getLogger(PlanApplyService.class);

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
    public PlanApplyService(PlanReader planReader,
                            StateFileService stateFileService,
                            List<PlanItemApplier> discoveredAppliers) {
        this.planReader = planReader;
        this.stateFileService = stateFileService;
        this.appliers = discoveredAppliers.stream()
                .collect(Collectors.toMap(PlanItemApplier::supportedCategory, applier -> applier,
                        (a, b) -> a, () -> new EnumMap<>(FileCategory.class)));
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
            try {
                PlanItemApplier applier = resolveApplier(item.getFileCategory());
                applier.apply(item);
                stateFileService.applyStateChange(item);
            } catch (MissingApplierException e) {
                log.error("Skipping item {}: {}", item.getKey(), e.getMessage());
            } catch (PlanApplyException e) {
                log.error("Failed to apply item {}: {}", item.getKey(), e.getMessage(), e);
            } catch (Exception e) {
                log.error("Unexpected error applying item {}: {}", item.getKey(), e.getMessage(), e);
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
