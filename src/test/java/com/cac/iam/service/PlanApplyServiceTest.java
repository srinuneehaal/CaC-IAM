package com.cac.iam.service;

import com.cac.iam.exception.PlanApplyException;
import com.cac.iam.model.Action;
import com.cac.iam.model.FileCategory;
import com.cac.iam.model.MasterPlan;
import com.cac.iam.model.PlanItem;
import com.cac.iam.service.apply.PlanReader;
import com.cac.iam.service.apply.itemapply.PlanItemApplier;
import com.cac.iam.util.LoggerProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlanApplyServiceTest {

    @Mock
    private PlanReader planReader;
    @Mock
    private StateFileService stateFileService;
    @Mock
    private PlanItemApplier applier;

    private PlanApplyService service;

    @BeforeEach
    void setUp() {
        when(applier.supportedCategory()).thenReturn(FileCategory.POLICIES);
        service = new PlanApplyService(
                planReader,
                stateFileService,
                List.of(applier),
                new LoggerProvider());
    }

    @Test
    void applyPlan_updatesStateAfterSuccessfulApply() {
        MasterPlan plan = new MasterPlan();
        plan.addItem(new PlanItem(Action.NEW, FileCategory.POLICIES, "k1", "p", new Object()));
        when(planReader.read()).thenReturn(plan);

        service.applyPlan();

        verify(applier, times(1)).apply(any());
        verify(stateFileService, times(1)).applyStateChange(any());
    }

    @Test
    void applyPlan_skipsStateWhenApplyFails() {
        MasterPlan plan = new MasterPlan();
        plan.addItem(new PlanItem(Action.NEW, FileCategory.POLICIES, "k1", "p", new Object()));
        when(planReader.read()).thenReturn(plan);
        doThrow(new PlanApplyException("boom")).when(applier).apply(any());

        service.applyPlan();

        verify(stateFileService, times(0)).applyStateChange(any());
    }

    @Test
    void applyPlan_skipsWhenApplierMissing() {
        PlanApplyService missingService = new PlanApplyService(planReader, stateFileService, List.of(), new LoggerProvider());
        MasterPlan plan = new MasterPlan();
        plan.addItem(new PlanItem(Action.NEW, FileCategory.ROLES, "r1", "p", new Object()));
        when(planReader.read()).thenReturn(plan);

        missingService.applyPlan();

        verifyNoInteractions(stateFileService);
    }

    @Test
    void applyPlan_noItemsNoOps() {
        when(planReader.read()).thenReturn(new MasterPlan());

        service.applyPlan();

        verify(applier, never()).apply(any());
        verifyNoInteractions(stateFileService);
    }

    @Test
    void applyPlan_handlesReaderFailure() {
        when(planReader.read()).thenThrow(new RuntimeException("read failed"));

        assertThatThrownBy(() -> service.applyPlan())
                .isInstanceOf(PlanApplyException.class);
    }

    @Test
    void applyPlan_handlesUnexpectedApplyException() {
        MasterPlan plan = new MasterPlan();
        plan.addItem(new PlanItem(Action.NEW, FileCategory.POLICIES, "k1", "p", new Object()));
        when(planReader.read()).thenReturn(plan);
        doThrow(new RuntimeException("explode")).when(applier).apply(any());

        service.applyPlan();

        verify(stateFileService, never()).applyStateChange(any());
    }

    @Test
    void applyPlan_logsStateUpdateFailures() {
        MasterPlan plan = new MasterPlan();
        plan.addItem(new PlanItem(Action.NEW, FileCategory.POLICIES, "k1", "p", new Object()));
        when(planReader.read()).thenReturn(plan);
        doThrow(new PlanApplyException("fail")).when(stateFileService).applyStateChange(any());

        service.applyPlan();

        verify(applier, times(1)).apply(any());
    }

    @Test
    void applyPlan_logsUnexpectedStateUpdateFailures() {
        MasterPlan plan = new MasterPlan();
        plan.addItem(new PlanItem(Action.NEW, FileCategory.POLICIES, "k1", "p", new Object()));
        when(planReader.read()).thenReturn(plan);
        doThrow(new RuntimeException("boom")).when(stateFileService).applyStateChange(any());

        service.applyPlan();

        verify(applier, times(1)).apply(any());
    }
}
