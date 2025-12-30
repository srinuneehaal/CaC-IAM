# Policy Category

- **Files**: `changedfiles/.../policies/*.json` (configurable via `cacex.paths.policies-dir-name`).  
- **Parsing**: `PolicyFileParsingStrategy` loads `PolicyCreationRequest` and derives key from `code` (fallback: filename stem).  
- **Plan**: `PlanService` compares payloads to Cosmos state (`typeOfItem=POLICIES`) and emits `NEW/UPDATE/DELETE` items. Ordered before roles/users by default.  
- **Apply**: `PolicyPlanItemApplier` → `PolicyApiService` (logs by default) → `StateFileService` upsert/delete.  
- **References**: `spec/Policies/policies-plan-apply.md`, `spec/Policies/policies-plan.mmd`, `spec/Policies/policies-apply.mmd`.
