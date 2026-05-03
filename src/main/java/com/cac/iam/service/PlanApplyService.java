package com.cac.iam.service;

import com.cac.iam.exception.MissingApplierException;
import com.cac.iam.exception.PlanApplyException;
import com.cac.iam.model.FileCategory;
import com.cac.iam.model.MasterPlan;
import com.cac.iam.model.PlanItem;
import com.cac.iam.service.apply.PlanReader;
import com.cac.iam.service.apply.PlanApplySummary;
import com.cac.iam.service.apply.itemapply.PlanItemApplier;
import com.cac.iam.util.LoggerProvider;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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
    public PlanApplySummary applyPlan() {
        MasterPlan masterPlan = readMasterPlan();
        if (masterPlan.getItems().isEmpty()) {
            log.warn("No plan items found; nothing to apply.");
            return PlanApplySummary.empty();
        }
        int totalItems = masterPlan.getItems().size();
        List<PlanApplySummary.ItemResult> results = new ArrayList<>(totalItems);
        log.info("Applying {} plan item(s) from master plan", totalItems);
        for (int index = 0; index < totalItems; index++) {
            PlanItem item = masterPlan.getItems().get(index);
            results.add(applyItem(item, index + 1, totalItems));
        }

        PlanApplySummary summary = new PlanApplySummary(results);
        logSummary(summary);
        return summary;
    }

    private PlanApplySummary.ItemResult applyItem(PlanItem item, int position, int totalItems) {
        log.info("Applying item [{}/{}]: action={}, category={}, key={}", position, totalItems,
                item.getAction(), item.getFileCategory(), item.getKey());

        PlanItemApplier applier;
        try {
            applier = resolveApplier(item.getFileCategory());
        } catch (MissingApplierException e) {
            log.error("Skipping item {}: {}", item.getKey(), e.getMessage());
            return PlanApplySummary.ItemResult.failure(item, e.getMessage());
        }

        try {
            applier.apply(item);
        } catch (PlanApplyException e) {
            log.error("Failed to apply item {}: {}", item.getKey(), e.getMessage(), e);
            return PlanApplySummary.ItemResult.failure(item, formatFailureMessage("Apply failed", e));
        } catch (Exception e) {
            log.error("Unexpected error applying item {}: {}", item.getKey(), e.getMessage(), e);
            return PlanApplySummary.ItemResult.failure(item, formatFailureMessage("Unexpected apply error", e));
        }

        try {
            stateFileService.applyStateChange(item);
            return PlanApplySummary.ItemResult.success(item, "Applied and state updated successfully");
        } catch (PlanApplyException e) {
            log.error("State update failed for item {} after successful apply: {}", item.getKey(), e.getMessage(), e);
            return PlanApplySummary.ItemResult.failure(item,
                    formatFailureMessage("State update failed after successful apply", e));
        } catch (Exception e) {
            log.error("Unexpected state update error for item {} after successful apply: {}", item.getKey(), e.getMessage(), e);
            return PlanApplySummary.ItemResult.failure(item,
                    formatFailureMessage("Unexpected state update error after successful apply", e));
        }
    }

    private void logSummary(PlanApplySummary summary) {
        for (PlanApplySummary.ItemResult result : summary.getItemResults()) {
            if (result.isSuccess()) {
                log.info("Apply item result: status={}, action={}, category={}, key={}, message={}",
                        result.status(), result.action(), result.fileCategory(), result.key(), result.message());
            } else {
                log.error("Apply item result: status={}, action={}, category={}, key={}, message={}",
                        result.status(), result.action(), result.fileCategory(), result.key(), result.message());
            }
        }

        if (summary.hasFailures()) {
            log.error("Apply completed with failures: succeeded={}, failed={}, total={}",
                    summary.successCount(), summary.failureCount(), summary.totalCount());
            return;
        }

        log.info("Apply completed successfully: succeeded={}, failed=0, total={}",
                summary.successCount(), summary.totalCount());
    }

    private String formatFailureMessage(String prefix, Exception exception) {
        String detail = exception.getMessage();
        if (detail == null || detail.isBlank()) {
            return prefix;
        }
        return prefix + ": " + detail;
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
