package com.cac.iam.runner;

import com.cac.iam.model.MasterPlan;
import com.cac.iam.service.PlanService;
import com.cac.iam.service.plan.ChangedFilesProvider;
import com.cac.iam.service.plan.MasterPlanHtmlReportGenerator;
import com.cac.iam.service.plan.PlanWriter;
import com.cac.iam.util.LoggerProvider;
import com.cac.iam.util.ShutdownManager;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;

import static org.mockito.Mockito.*;

class PlanRunnerTest {

    @Test
    void executesPlanWhenFlagPresent() {
        ChangedFilesProvider changedFilesProvider = mock(ChangedFilesProvider.class);
        PlanService planService = mock(PlanService.class);
        PlanWriter planWriter = mock(PlanWriter.class);
        MasterPlanHtmlReportGenerator reportGenerator = mock(MasterPlanHtmlReportGenerator.class);
        MasterPlan plan = new MasterPlan();

        when(changedFilesProvider.getChangedPaths()).thenReturn(List.of(Path.of("a")));
        when(planService.buildPlan(anyList())).thenReturn(plan);
        when(planWriter.write(plan)).thenReturn(Path.of("plan/masterplan.json"));
        when(reportGenerator.generateReport(plan)).thenReturn(Path.of("plan/masterplan.html"));

        ShutdownManager shutdownManager = mock(ShutdownManager.class);
        PlanRunner runner = new PlanRunner(changedFilesProvider, planService, planWriter, reportGenerator, new LoggerProvider(), shutdownManager);

        runner.run("--plan");

        verify(planService, times(1)).buildPlan(anyList());
        verify(planWriter, times(1)).write(plan);
        verify(reportGenerator, times(1)).generateReport(plan);
        verify(shutdownManager).shutdown(0);
    }

    @Test
    void skipsWhenNoFlag() {
        ChangedFilesProvider changedFilesProvider = mock(ChangedFilesProvider.class);
        PlanService planService = mock(PlanService.class);
        PlanWriter planWriter = mock(PlanWriter.class);
        MasterPlanHtmlReportGenerator reportGenerator = mock(MasterPlanHtmlReportGenerator.class);

        ShutdownManager shutdownManager = mock(ShutdownManager.class);
        PlanRunner runner = new PlanRunner(changedFilesProvider, planService, planWriter, reportGenerator, new LoggerProvider(), shutdownManager);

        runner.run();

        verifyNoInteractions(planService, planWriter, reportGenerator);
        verify(shutdownManager, never()).shutdown(anyInt());
    }

    @Test
    void handlesEmptyChangedFiles() {
        ChangedFilesProvider changedFilesProvider = mock(ChangedFilesProvider.class);
        when(changedFilesProvider.getChangedPaths()).thenReturn(List.of());
        ShutdownManager shutdownManager = mock(ShutdownManager.class);
        PlanRunner runner = new PlanRunner(changedFilesProvider, mock(PlanService.class),
                mock(PlanWriter.class), mock(MasterPlanHtmlReportGenerator.class), new LoggerProvider(), shutdownManager);

        runner.run("--plan", "extra");

        verify(changedFilesProvider).getChangedPaths();
        verify(shutdownManager).shutdown(0);
    }

    @Test
    void respectsReportToggle() {
        ChangedFilesProvider changedFilesProvider = mock(ChangedFilesProvider.class);
        when(changedFilesProvider.getChangedPaths()).thenReturn(List.of(Path.of("a")));
        PlanService planService = mock(PlanService.class);
        PlanWriter planWriter = mock(PlanWriter.class);
        MasterPlan plan = new MasterPlan();
        when(planService.buildPlan(anyList())).thenReturn(plan);
        when(planWriter.write(plan)).thenReturn(Path.of("plan/masterplan.json"));

        MasterPlanHtmlReportGenerator reportGenerator = mock(MasterPlanHtmlReportGenerator.class);
        ShutdownManager shutdownManager = mock(ShutdownManager.class);
        PlanRunner runner = new PlanRunner(changedFilesProvider, planService, planWriter, reportGenerator, new LoggerProvider(), shutdownManager);
        runner.setEnvLookup(key -> "false");

        runner.run("--plan");

        verify(reportGenerator, never()).generateReport(plan);
        verify(shutdownManager).shutdown(0);
    }

    @Test
    void swallowsExecutionFailures() {
        ChangedFilesProvider changedFilesProvider = mock(ChangedFilesProvider.class);
        when(changedFilesProvider.getChangedPaths()).thenReturn(List.of(Path.of("a")));
        PlanService planService = mock(PlanService.class);
        when(planService.buildPlan(anyList())).thenThrow(new RuntimeException("bad"));

        ShutdownManager shutdownManager = mock(ShutdownManager.class);
        PlanRunner runner = new PlanRunner(changedFilesProvider, planService, mock(PlanWriter.class),
                mock(MasterPlanHtmlReportGenerator.class), new LoggerProvider(), shutdownManager);

        runner.run("--plan");

        verify(planService).buildPlan(anyList());
        verify(shutdownManager).shutdown(1);
    }
}
