package com.cac.iam.runner;

import com.cac.iam.service.PlanApplyService;
import com.cac.iam.util.LoggerProvider;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

class ApplyRunnerTest {

    @Test
    void runsApplyWhenFlagPresent() {
        PlanApplyService applyService = mock(PlanApplyService.class);
        ApplyRunner runner = new ApplyRunner(applyService, new LoggerProvider());

        runner.run("--apply");

        verify(applyService, times(1)).applyPlan();
    }

    @Test
    void skipsWhenFlagMissing() {
        PlanApplyService applyService = mock(PlanApplyService.class);
        ApplyRunner runner = new ApplyRunner(applyService, new LoggerProvider());

        runner.run();

        verifyNoInteractions(applyService);
    }

    @Test
    void warnsOnExtraArgsAndSwallowsErrors() {
        PlanApplyService applyService = mock(PlanApplyService.class);
        doThrow(new RuntimeException("boom")).when(applyService).applyPlan();
        ApplyRunner runner = new ApplyRunner(applyService, new LoggerProvider());

        runner.run("--apply", "extra");

        verify(applyService).applyPlan();
    }
}
