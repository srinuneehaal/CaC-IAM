# Application Pattern (CaC-IAM)

The IAM adaptor follows the same plan/apply rhythm as CaC-Ex with IAM-specific categories.

- **Plan**: `ChangedFilesProvider` reads `CHANGED_FILES`, `PlanService` parses policy/role/user JSON via `FileParsingStrategyFactory`, compares payloads to Cosmos state, emits `PlanItem`s, orders them with `PlanOrderingRuleEngine`, and writes `plan/masterplan.json` plus an optional HTML report.  
- **Apply**: `PlanApplyService` reads the master plan, maps each item to a `PlanItemApplier` (policy/role/user), delegates to the category-specific `PlanItemActionService`, and then updates Cosmos via `StateFileService`. Missing appliers surface as `MissingApplierException` but do not stop the run.  
- **Ordering**: Driven by `plan.ordering.rules[*]` (default: policies → roles → users; new/update before deletes). Adjust the property when dependencies change.  
- **Persistence**: `StateRepository` abstracts Cosmos; documents are partitioned by `typeOfItem` (`POLICIES`, `ROLES`, `USERS`) with IDs matching the logical key. Delete detection relies on missing files in `CHANGED_FILES`.  
- **Reporting/ops**: `MASTER_PLAN_REPORT_ENABLED` toggles the HTML report; `/actuator/health` surfaces Cosmos connectivity via `CosmosHealthIndicator`.
