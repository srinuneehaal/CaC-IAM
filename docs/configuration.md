# Configuration

The application is driven by Spring Boot properties with environment overrides to make plan/apply runs portable between local dev and pipelines.

## Files and directories
- `cacex.paths.changed-files-dir`: Root directory containing candidate JSON changes (default `changedfiles`).  
- `cacex.paths.plan-dir`: Output directory for plan artifacts (default `plan`).  
- `cacex.paths.master-plan-file`: Master plan filename (default `masterplan.json`).  
- `cacex.paths.policies-dir-name`, `...roles-dir-name`, `...users-dir-name`: Directory names under the changed files root that map to parsing strategies.

Environment variables `PLAN_DIR`, `MASTER_PLAN_FILE`, and `CHANGED_FILES_DIR` override the above paths at runtime via `FileLocationProperties`.

## Plan ordering
`plan.ordering.rules[*]` controls the sequence of plan items. Defaults place policy creates/updates first, then roles, then users, followed by deletes in the same order. Adjust these rules if new dependencies are introduced.

## Cosmos DB
- `azure.cosmos.uri`, `azure.cosmos.key`, `azure.cosmos.database`, `azure.cosmos.container`, `azure.cosmos.partition-key` configure the Cosmos connection used by `CosmosStateRepository`.  
- Local defaults in `application-local.properties` point at the emulator (`https://localhost:8081`) with the default key.  
- `CosmosHealthIndicator` surfaces connectivity via `/actuator/health`.

## Reporting
- `MASTER_PLAN_REPORT_ENABLED` (env var) toggles the HTML report emitted by `MasterPlanHtmlReportGenerator`. Any of `false/off/no/0` disables it.  
- Logging for `com.cac.iam` defaults to `INFO` and can be raised per need.

## SDK integration
- Policy/role/user apply steps call into the Finbourne Access/Identity SDKs. The `PolicyApiService` and `RoleApiService` include placeholders for real API calls; wire credentials and uncomment calls before production use.  
- Keep SDK versions aligned with `pom.xml` (`access-sdk`, `identity-sdk`).
