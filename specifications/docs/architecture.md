# Architecture Overview

CaC-IAM turns IAM configuration JSON files into an ordered master plan and applies those items through mocked Finbourne Access/Identity SDK calls while mirroring state in Cosmos DB. The repository mirrors the CaC-Ex plan/apply pattern with IAM-specific categories (`POLICIES`, `ROLES`, `USERS`).

## High-level flow
1. **Discovery**: `ChangedFilesProvider` reads the `CHANGED_FILES` environment variable and normalizes the supplied paths (typically `changedfiles/...`).  
2. **Plan generation** (`--plan`): `PlanRunner` feeds those paths into `PlanService`, which delegates to `FileParsingStrategyFactory` to parse JSON into `LoadedFile` entries, compares payloads to Cosmos state via `StateRepository`, emits `PlanItem`s, orders them with `PlanOrderingRuleEngine`, and writes `plan/masterplan.json` through `PlanWriter`. When enabled, `MasterPlanHtmlReportGenerator` renders `plan/masterplan.html` beside the JSON.  
3. **Plan application** (`--apply`): `PlanApplyService` uses `PlanReader` to rehydrate payloads, resolves a `PlanItemApplier` per `FileCategory`, calls the matching `PlanItemActionService` (Policy/Role/User), and then updates Cosmos through `StateFileService` so subsequent plan runs diff against the new state.

## Component relationships
```
CHANGED_FILES -> PlanRunner
    PlanRunner -> ChangedFilesProvider
    PlanRunner -> PlanService -> FileParsingStrategyFactory -> FileParsingStrategy (policy/role/user)
                             \-> StateRepository (CosmosStateRepository)
                             \-> PlanOrderingRuleEngine
                             \-> PlanWriter & MasterPlanHtmlReportGenerator

-- Apply --
PlanApplyService -> PlanReader -> MasterPlan
PlanApplyService -> PlanItemApplier (per category) -> PlanItemActionService (API surface)
PlanApplyService -> StateFileService -> StateRepository
```

## Key directories and configuration
- `src/main/java/com/cac/iam/service/plan`: plan runner, file discovery, ordering, writer, HTML report.  
- `src/main/java/com/cac/iam/service/plan/stratagy`: parsing strategies for policies, roles, and users keyed off `cacex.paths.*` properties.  
- `src/main/java/com/cac/iam/service/apply`: plan reader, appliers, API services, and state updater.  
- `src/main/java/com/cac/iam/repository`: Cosmos-backed `StateRepository` abstraction.  
- `src/main/resources/application.properties`: directories, plan ordering, Cosmos connection, and the report toggle (`MASTER_PLAN_REPORT_ENABLED` defaults to on).

## Extending the architecture
To add another IAM category:
1. Add a `FileCategory` entry and directory name in `FileLocationProperties`.  
2. Implement a `FileParsingStrategy` that deserializes the JSON into the right SDK request and derives a stable key.  
3. Update `PlanService` to detect deletes (missing files) if needed and to load the correct state payload type.  
4. Provide a `PlanItemApplier` and `PlanItemActionService` pair, then wire it into `PlanApplyService` by discovery.  
5. Add plan ordering rules in properties, cover the new strategy, applier, and ordering engine with tests, and document the flow under `spec/` and `confluence_docs/`.

## Reporting and health
- `MasterPlanHtmlReportGenerator` writes an interactive HTML snapshot of the master plan; set `MASTER_PLAN_REPORT_ENABLED=false` to skip it.  
- `CosmosHealthIndicator` (Spring Boot actuator) surfaces readiness of the Cosmos container when `/actuator/health` is exposed.
