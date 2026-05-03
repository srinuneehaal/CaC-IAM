package com.cac.iam.runner;

import com.cac.iam.service.PlanApplyService;
import com.cac.iam.service.apply.PlanApplySummary;
import com.cac.iam.util.LoggerProvider;
import com.cac.iam.util.ShutdownManager;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

class ApplyRunnerTest {

    @Test
    void runsApplyWhenFlagPresent() {
        PlanApplyService applyService = mock(PlanApplyService.class);
        when(applyService.applyPlan()).thenReturn(PlanApplySummary.empty());
        ShutdownManager shutdownManager = mock(ShutdownManager.class);
        ApplyRunner runner = new ApplyRunner(applyService, new LoggerProvider(), shutdownManager);

        runner.run("--apply");

        verify(applyService, times(1)).applyPlan();
        verify(shutdownManager).shutdown(0);
    }

    @Test
    void skipsWhenFlagMissing() {
        PlanApplyService applyService = mock(PlanApplyService.class);
        ShutdownManager shutdownManager = mock(ShutdownManager.class);
        ApplyRunner runner = new ApplyRunner(applyService, new LoggerProvider(), shutdownManager);

        runner.run();

        verifyNoInteractions(applyService);
        verify(shutdownManager, never()).shutdown(anyInt());
    }

    @Test
    void warnsOnExtraArgsAndSwallowsErrors() {
        PlanApplyService applyService = mock(PlanApplyService.class);
        doThrow(new RuntimeException("boom")).when(applyService).applyPlan();
        ShutdownManager shutdownManager = mock(ShutdownManager.class);
        ApplyRunner runner = new ApplyRunner(applyService, new LoggerProvider(), shutdownManager);

        runner.run("--apply", "extra");

        verify(applyService).applyPlan();
        verify(shutdownManager).shutdown(1);
    }

    @Test
    void exitsNonZeroWhenAnyItemFails() {
        PlanApplyService applyService = mock(PlanApplyService.class);
        when(applyService.applyPlan()).thenReturn(new PlanApplySummary(List.of(
                new PlanApplySummary.ItemResult(null, null, "k1", "p",
                        PlanApplySummary.Status.FAILED, "Apply failed")
        )));
        ShutdownManager shutdownManager = mock(ShutdownManager.class);
        ApplyRunner runner = new ApplyRunner(applyService, new LoggerProvider(), shutdownManager);

        runner.run("--apply");

        verify(shutdownManager).shutdown(1);
    }
}
