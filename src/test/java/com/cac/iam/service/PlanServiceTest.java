package com.cac.iam.service;

import com.cac.iam.model.*;
import com.cac.iam.repository.StateRepository;
import com.cac.iam.service.plan.rules.PlanOrderingRuleEngine;
import com.cac.iam.service.plan.stratagy.FileParsingStrategy;
import com.cac.iam.service.plan.stratagy.FileParsingStrategyFactory;
import com.cac.iam.util.LoggerProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.finbourne.access.model.PolicyCreationRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlanServiceTest {

    @Mock
    private FileParsingStrategyFactory strategyFactory;
    @Mock
    private FileParsingStrategy strategy;
    @Mock
    private PlanOrderingRuleEngine orderingRuleEngine;
    @Mock
    private StateRepository stateRepository;

    private PlanService planService;

    @Test
    void buildPlanCreatesNewUpdateAndDeleteItems(@TempDir Path tempDir) throws IOException {
        Path changed = Files.createTempFile(tempDir, "policy-", ".json");
        PolicyCreationRequest changedPayload = new PolicyCreationRequest().code("p1").description("new");
        LoadedFile loadedFile = new LoadedFile(FileCategory.POLICIES, "p1", changed, changedPayload);
        Path toDelete = tempDir.resolve("stale.json");

        lenient().when(strategyFactory.resolve(org.mockito.ArgumentMatchers.<Path>any())).thenReturn(strategy);
        lenient().when(strategy.parse(any())).thenReturn(loadedFile);
        when(strategy.getCategory()).thenReturn(FileCategory.POLICIES);
        PolicyCreationRequest existingPayload = new PolicyCreationRequest().code("p1").description("old");
        when(stateRepository.findPayload(FileCategory.POLICIES, "p1", PolicyCreationRequest.class))
                .thenReturn(java.util.Optional.of(existingPayload));
        when(stateRepository.findPayload(FileCategory.POLICIES, "stale", PolicyCreationRequest.class))
                .thenReturn(java.util.Optional.of(new PolicyCreationRequest().code("stale")));
        when(orderingRuleEngine.applyOrdering(any())).thenAnswer(invocation -> invocation.getArgument(0));

        planService = new PlanService(strategyFactory, orderingRuleEngine, stateRepository,
                new ObjectMapper(), new LoggerProvider());

        MasterPlan plan = planService.buildPlan(List.of(changed, toDelete));

        assertThat(plan.getItems()).hasSize(2);
        PlanItem update = plan.getItems().stream()
                .filter(i -> i.getAction() == Action.UPDATE).findFirst().orElseThrow();
        assertThat(update.getKey()).isEqualTo("p1");
        assertThat(update.getBeforePayload()).isEqualTo(existingPayload);

        PlanItem delete = plan.getItems().stream()
                .filter(i -> i.getAction() == Action.DELETE).findFirst().orElseThrow();
        assertThat(delete.getKey()).isEqualTo("stale");
        assertThat(delete.getSourcePath()).contains("cosmos://POLICIES/");
    }

    @Test
    void buildPlanAddsNewWhenNotInState(@TempDir Path tempDir) throws IOException {
        Path changed = Files.createTempFile(tempDir, "policy-", ".json");
        PolicyCreationRequest payload = new PolicyCreationRequest().code("np");
        LoadedFile loadedFile = new LoadedFile(FileCategory.POLICIES, "np", changed, payload);

        when(strategyFactory.resolve(org.mockito.ArgumentMatchers.<Path>any())).thenReturn(strategy);
        when(strategy.parse(any())).thenReturn(loadedFile);
        when(orderingRuleEngine.applyOrdering(any())).thenAnswer(invocation -> invocation.getArgument(0));

        planService = new PlanService(strategyFactory, orderingRuleEngine, stateRepository,
                new ObjectMapper(), new LoggerProvider());

        MasterPlan plan = planService.buildPlan(List.of(changed));

        assertThat(plan.getItems()).hasSize(1);
        PlanItem newItem = plan.getItems().getFirst();
        assertThat(newItem.getAction()).isEqualTo(Action.NEW);
        assertThat(newItem.getKey()).isEqualTo("np");
    }

    @Test
    void buildPlanSkipsMissingPathsAndUnsupported(@TempDir Path tempDir) {
        Path missing = tempDir.resolve("missing.json");
        when(orderingRuleEngine.applyOrdering(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(strategyFactory.resolve(missing)).thenThrow(new com.cac.iam.exception.UnsupportedFilePathException("bad"));

        planService = new PlanService(strategyFactory, orderingRuleEngine, stateRepository,
                new ObjectMapper(), new LoggerProvider());

        MasterPlan plan = planService.buildPlan(List.of(missing));

        assertThat(plan.getItems()).isEmpty();
    }

    @Test
    void buildPlanHandlesUnsupportedStrategy(@TempDir Path tempDir) throws IOException {
        Path path = Files.createTempFile(tempDir, "bad-", ".txt");
        when(orderingRuleEngine.applyOrdering(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(strategyFactory.resolve(org.mockito.ArgumentMatchers.<Path>any())).thenThrow(new com.cac.iam.exception.UnsupportedFilePathException("bad"));

        planService = new PlanService(strategyFactory, orderingRuleEngine, stateRepository,
                new ObjectMapper(), new LoggerProvider());

        MasterPlan plan = planService.buildPlan(List.of(path));

        assertThat(plan.getItems()).isEmpty();
    }

    @Test
    void buildPlanWrapsPayloadComparisonError(@TempDir Path tempDir) throws IOException {
        Path changed = Files.createTempFile(tempDir, "policy-", ".json");
        LoadedFile loadedFile = new LoadedFile(FileCategory.POLICIES, "p1", changed, new PolicyCreationRequest());
        when(strategyFactory.resolve(org.mockito.ArgumentMatchers.<Path>any())).thenReturn(strategy);
        when(strategy.parse(any())).thenReturn(loadedFile);
        lenient().when(stateRepository.findPayload(any(), any(), any())).thenReturn(java.util.Optional.of(new Object()));
        com.fasterxml.jackson.databind.ObjectMapper failingMapper = mock(com.fasterxml.jackson.databind.ObjectMapper.class);
        when(failingMapper.valueToTree(any())).thenThrow(new IllegalArgumentException("bad"));
        lenient().when(orderingRuleEngine.applyOrdering(any())).thenAnswer(invocation -> invocation.getArgument(0));

        planService = new PlanService(strategyFactory, orderingRuleEngine, stateRepository,
                failingMapper, new LoggerProvider());

        assertThatThrownBy(() -> planService.buildPlan(List.of(changed)))
                .isInstanceOf(com.cac.iam.exception.PlanProcessingException.class);
    }
}
