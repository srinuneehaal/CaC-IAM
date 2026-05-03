package com.cac.iam.service.apply;

import com.cac.iam.model.Action;
import com.cac.iam.model.FileCategory;
import com.cac.iam.model.PlanItem;

import java.util.List;
import java.util.Objects;

/**
 * Captures the outcome of an apply run, including one result per processed plan item.
 */
public final class PlanApplySummary {

    private final List<ItemResult> itemResults;

    public PlanApplySummary(List<ItemResult> itemResults) {
        this.itemResults = List.copyOf(Objects.requireNonNull(itemResults, "itemResults cannot be null"));
    }

    public static PlanApplySummary empty() {
        return new PlanApplySummary(List.of());
    }

    public List<ItemResult> getItemResults() {
        return itemResults;
    }

    public boolean isEmpty() {
        return itemResults.isEmpty();
    }

    public int totalCount() {
        return itemResults.size();
    }

    public int successCount() {
        return (int) itemResults.stream()
                .filter(ItemResult::isSuccess)
                .count();
    }

    public int failureCount() {
        return (int) itemResults.stream()
                .filter(ItemResult::isFailure)
                .count();
    }

    public boolean hasFailures() {
        return itemResults.stream().anyMatch(ItemResult::isFailure);
    }

    public List<ItemResult> getFailures() {
        return itemResults.stream()
                .filter(ItemResult::isFailure)
                .toList();
    }

    public enum Status {
        SUCCESS,
        FAILED
    }

    public record ItemResult(Action action,
                             FileCategory fileCategory,
                             String key,
                             String sourcePath,
                             Status status,
                             String message) {

        public ItemResult {
            status = Objects.requireNonNull(status, "status cannot be null");
            message = message == null ? "" : message;
        }

        public boolean isSuccess() {
            return status == Status.SUCCESS;
        }

        public boolean isFailure() {
            return status == Status.FAILED;
        }

        public static ItemResult success(PlanItem item, String message) {
            return from(item, Status.SUCCESS, message);
        }

        public static ItemResult failure(PlanItem item, String message) {
            return from(item, Status.FAILED, message);
        }

        private static ItemResult from(PlanItem item, Status status, String message) {
            return new ItemResult(
                    item != null ? item.getAction() : null,
                    item != null ? item.getFileCategory() : null,
                    item != null ? item.getKey() : null,
                    item != null ? item.getSourcePath() : null,
                    status,
                    message
            );
        }
    }
}
