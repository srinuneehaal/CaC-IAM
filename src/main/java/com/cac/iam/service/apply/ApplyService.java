package com.cac.iam.service.apply;

import com.cac.iam.model.FileCategory;
import com.cac.iam.model.MasterPlan;
import com.cac.iam.model.PlanItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class ApplyService {

    private static final Logger log = LoggerFactory.getLogger(ApplyService.class);

    private final PlanReader planReader;
    private final Map<FileCategory, PlanItemApplier> appliers;

    public ApplyService(PlanReader planReader, List<PlanItemApplier> discoveredAppliers) {
        this.planReader = planReader;
        this.appliers = discoveredAppliers.stream()
                .collect(Collectors.toMap(applier -> findCategory(applier),
                        applier -> applier, (a, b) -> a));
    }

    public void applyMasterPlan() {
        MasterPlan masterPlan = planReader.read();
        if (masterPlan == null || masterPlan.getItems() == null || masterPlan.getItems().isEmpty()) {
            log.warn("No plan items found; nothing to apply.");
            return;
        }
        log.info("Applying {} plan item(s)", masterPlan.getItems().size());
        for (PlanItem item : masterPlan.getItems()) {
            if (item == null || item.getFileCategory() == null) {
                log.warn("Skipping empty plan item {}", item);
                continue;
            }
            PlanItemApplier applier = appliers.get(item.getFileCategory());
            if (applier == null) {
                log.warn("No applier registered for category {}; skipping item {}", item.getFileCategory(), item.getKey());
                continue;
            }
            try {
                applier.apply(item);
            } catch (Exception e) {
                log.error("Failed to apply item {} {}: {}", item.getFileCategory(), item.getKey(), e.getMessage(), e);
            }
        }
    }

    private com.cac.iam.model.FileCategory findCategory(PlanItemApplier applier) {
        for (com.cac.iam.model.FileCategory category : com.cac.iam.model.FileCategory.values()) {
            if (applier.supports(category)) {
                return category;
            }
        }
        throw new IllegalStateException("Applier " + applier.getClass().getSimpleName() + " does not declare a category");
    }
}
