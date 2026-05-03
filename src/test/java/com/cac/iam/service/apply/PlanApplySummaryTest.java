package com.cac.iam.service.apply;

import com.cac.iam.model.Action;
import com.cac.iam.model.FileCategory;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PlanApplySummaryTest {

    @Test
    void tracksSuccessAndFailureCounts() {
        PlanApplySummary summary = new PlanApplySummary(List.of(
                new PlanApplySummary.ItemResult(Action.NEW, FileCategory.POLICIES, "p1", "policies/p1.json",
                        PlanApplySummary.Status.SUCCESS, "Applied successfully"),
                new PlanApplySummary.ItemResult(Action.UPDATE, FileCategory.ROLES, "r1", "roles/r1.json",
                        PlanApplySummary.Status.FAILED, "State update failed")
        ));

        assertThat(summary.totalCount()).isEqualTo(2);
        assertThat(summary.successCount()).isEqualTo(1);
        assertThat(summary.failureCount()).isEqualTo(1);
        assertThat(summary.hasFailures()).isTrue();
        assertThat(summary.getFailures()).singleElement()
                .extracting(PlanApplySummary.ItemResult::key)
                .isEqualTo("r1");
    }

    @Test
    void emptySummaryHasNoFailures() {
        PlanApplySummary summary = PlanApplySummary.empty();

        assertThat(summary.isEmpty()).isTrue();
        assertThat(summary.totalCount()).isZero();
        assertThat(summary.successCount()).isZero();
        assertThat(summary.failureCount()).isZero();
        assertThat(summary.hasFailures()).isFalse();
        assertThat(summary.getFailures()).isEmpty();
    }
}
