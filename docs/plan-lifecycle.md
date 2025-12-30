# Plan Lifecycle

This document traces how a plan run moves from `CHANGED_FILES` input to persisted plan artifacts.

## Triggering a plan run
- Invoke the application with `--plan` (e.g., `mvn -q -DskipTests spring-boot:run -- --plan`).  
- `ChangedFilesProvider` reads `CHANGED_FILES` (space-separated paths) and normalizes them so Windows/Unix separators both work.

## Building the master plan
1. `PlanRunner` passes the changed paths to `PlanService.buildPlan`.  
2. `PlanService` resolves a `FileParsingStrategy` for each path, producing `LoadedFile` entries that capture `FileCategory`, key, path, and payload. Unsupported paths are logged and skipped.  
3. For each category, `PlanService` loads the existing payload from Cosmos via `StateRepository.findPayload(...)` and compares it to the new payload using Jackson tree equality.  
4. Actions emitted:  
   - `NEW` when no state exists;  
   - `UPDATE` when payloads differ (previous payload stored on the `PlanItem`);  
   - `DELETE` when a path in `CHANGED_FILES` no longer exists on disk.  
5. `PlanOrderingRuleEngine` sorts the plan items using `plan.ordering.rules[*]` or its IAM defaults (policies → roles → users, new/update before deletes).

## Persisting outputs
- `PlanWriter` writes `plan/masterplan.json` to the directory configured by `cacex.paths.plan-dir`/`MASTER_PLAN_FILE`.  
- `MasterPlanHtmlReportGenerator` writes `plan/masterplan.html` unless `MASTER_PLAN_REPORT_ENABLED` is set to `false`, `off`, `no`, or `0`.  
- Both artifacts are created alongside each other so reviewers can choose JSON or HTML before running apply.

## Error handling and logging
- Missing files are treated as delete candidates; parsing or comparison errors are logged and skipped so a single malformed file does not halt the run.  
- `PlanProcessingException` wraps unexpected comparison failures (e.g., payloads that cannot be converted to trees).

## What to update when flows change
- Adjust `plan.ordering.rules` if new categories must run earlier/later.  
- Update `FileLocationProperties` and parsing strategies if directory names change.  
- Refresh the diagrams under `diagrams/plan-sequence.mmd` and the Confluence summary when plan inputs/outputs evolve.
