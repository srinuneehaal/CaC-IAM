# CaC-IAM Design & Implementation (Single Guide)

One-stop reference for how CaC-IAM is structured, how it runs, and how to extend or operate it.

## What the system does
Takes IAM JSON changes (policies, roles, users), generates an ordered master plan, and applies those items via mocked Access/Identity SDK calls while persisting state in Cosmos DB.

## End-to-end flow
- **Inputs**: `CHANGED_FILES` env var lists JSON paths (usually under `changedfiles/`).  
- **Plan (`--plan`)**: `PlanRunner` → `PlanService` → `PlanOrderingRuleEngine` → `PlanWriter` (JSON) and optional `MasterPlanHtmlReportGenerator` (HTML).  
- **Apply (`--apply`)**: `PlanApplyService` reads `plan/masterplan.json` via `PlanReader`, resolves a per-category applier, calls the API service, then updates Cosmos via `StateFileService`.  
- **State**: `CosmosStateRepository` implements `StateRepository`, partitioning documents by `typeOfItem` (`POLICIES`, `ROLES`, `USERS`).

## Core components (src/main/java)
- **Entrypoints**: `IamApplication`, `PlanRunner`, `ApplyRunner`.  
- **Plan pipeline**: `ChangedFilesProvider` (env parsing) → `FileParsingStrategyFactory` + strategies (Policy/Role/User) → `PlanService` (diff vs Cosmos, emit `PlanItem`s) → `PlanOrderingRuleEngine` (properties-driven order) → `PlanWriter` + `MasterPlanHtmlReportGenerator`.  
- **Apply pipeline**: `PlanReader` (rehydrate payloads) → `PlanApplyService` → `PlanItemApplier` per `FileCategory` (powered by `AbstractPlanItemApplier`) → `PlanItemActionService` (API call surface) → `StateFileService` (Cosmos upsert/delete).  
- **Persistence/Config**: `CosmosStateRepository`, `CosmosStateProperties`/`CosmosStateConfiguration`; `FileLocationProperties` for directory names/plan file; `PlanOrderingRuleEngine` bound to `plan.ordering.rules[*]`.

## Data model
- `FileCategory`: POLICIES, ROLES, USERS.  
- `PlanItem`: action, category, key, sourcePath, payload, beforePayload.  
- `MasterPlan`: list of `PlanItem`s.  
- `StateDocument`: `id`, `typeOfItem`, `data` (stored in Cosmos).  
- Payloads: `PolicyCreationRequest`, `RoleCreationRequest`, `CreateUserRequest`.

## Configuration
- **Paths**: `cacex.paths.changed-files-dir`, `plan-dir`, `master-plan-file`, `policies-dir-name`, `roles-dir-name`, `users-dir-name`; env overrides `CHANGED_FILES_DIR`, `PLAN_DIR`, `MASTER_PLAN_FILE`.  
- **Cosmos**: `azure.cosmos.uri`, `key`, `database`, `container`, `partition-key` (emulator defaults in `application-local.properties`).  
- **Reporting**: `MASTER_PLAN_REPORT_ENABLED` (`false/off/no/0` disables HTML).  
- **Logging**: `logging.level.com.cac.iam=INFO` default.

## How to run
1. Set `CHANGED_FILES` (space-separated paths).  
2. Plan: `mvn -q -DskipTests spring-boot:run -- --plan`; check `plan/masterplan.json` (and `plan/masterplan.html`).  
3. Apply: `mvn -q -DskipTests spring-boot:run -- --apply`; watch logs and Cosmos state.  
4. Re-run `--plan` to confirm no drift.

## Extending
- Add a category: update `FileCategory`; set dir in `FileLocationProperties`; add `FileParsingStrategy`; extend `PlanService` payload lookup; add `PlanItemApplier` + `PlanItemActionService`; configure ordering; write tests.  
- Make API live: swap logging in `PolicyApiService`/`RoleApiService`/`UserApiService` for real SDK calls and handle errors; document new env vars.  
- Adjust reporting: if plan schema changes, update `masterplan-report-template.html` and `MasterPlanHtmlReportGenerator`.

## Testing
- Unit: `mvn -q test` (parsers, ordering, plan write/read, appliers, state service, Cosmos repo).  
- Smoke: run `--plan` + `--apply` with sample files; verify ordering and Cosmos documents.  
- Key suites: `PlanServiceTest`, `PlanOrderingRuleEngineTest`, `PlanReaderTest`, `PlanApplyServiceTest`, `StateFileServiceTest`, `CosmosStateRepositoryTest`, `*FileParsingStrategyTest`, `AbstractPlanItemApplierTest`.

## Diagrams
- Plan sequence: `diagrams/plan-sequence.mmd`.  
- Apply sequence: `diagrams/apply-sequence.mmd`.  
- Category flows: `spec/Policies|Roles|Users/*.mmd`.

## Operational notes
- Missing paths in `CHANGED_FILES` are treated as deletes.  
- Unsupported paths are logged and skipped.  
- Cosmos deletes ignore 404s; other errors are logged.  
- HTML report is optional; disable in CI if unnecessary.

## More detail
For deeper dives, see `docs/architecture.md`, `docs/plan-lifecycle.md`, and category specs under `spec/`. For Confluence, use `confluence_docs/` and attach rendered diagram PNGs.
