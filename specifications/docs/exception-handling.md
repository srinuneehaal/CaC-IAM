# Exception handling

How CaC-IAM treats failures across plan and apply, and what to follow when adding new logic.

## Exception types
- `PlanProcessingException`: wrapping comparison/plan-building failures (e.g., JSON tree comparison).
- `PlanApplyException`: base for apply failures; `InvalidPlanItemException` (missing/invalid payload/action) and `MissingApplierException` (no applier for category) extend it.
- `UnsupportedFilePathException` / `UnsupportedFileCategoryException`: thrown by parsing strategy resolution; treated as non-fatal skips.
- Generic `IllegalStateException`: thrown when plan IO fails (`PlanReader`, `PlanWriter`, `MasterPlanHtmlReportGenerator`).

## Where failures are handled
- **Plan path**:
  - `PlanRunner.run`: wraps `executePlan` and logs any uncaught errors.
  - `PlanService.loadChangedFiles` and `detectDeletes`: unsupported paths/categories are logged at warn and skipped; other parsing issues are logged at error but do not stop planning.
  - `PlanService.payloadsEqual`: wraps comparison errors in `PlanProcessingException`, which will bubble to the runner.
  - `PlanWriter` / `MasterPlanHtmlReportGenerator`: throw `IllegalStateException` on IO/template errors, causing plan generation to fail fast.
- **Apply path**:
  - `ApplyRunner.run`: logs any uncaught errors from `PlanApplyService.applyPlan`.
  - `PlanApplyService`: per-item handling; missing applier logs and skips the item; API/apply failures (`PlanApplyException` or other exceptions) are logged and the run continues; state update failures after a successful apply are logged and skipped.
  - `PlanReader`: throws `IllegalStateException` when the master plan is missing or unreadable; this halts apply.
  - `AbstractPlanItemApplier`: converts unexpected runtime errors into `PlanApplyException` to keep per-item error handling consistent.
- **Persistence**:
  - `CosmosStateRepository`: Cosmos exceptions are logged and converted to empty/ignored results so plan/apply can continue; 404s on delete/read are treated as benign.
  - `CosmosHealthIndicator`: reports detailed failure status codes via `/actuator/health` without throwing.

## Logging expectations
- Log with key context (`category`, `key`, `path`) so operators can identify the failing item.
- Avoid duplicate logs for the same failure; prefer to log where recovery/skip happens (e.g., in `PlanApplyService` or runners).
- Use warn for unsupported/expected skips; use error for actionable failures.

## Adding new code
- Decide fail-fast vs. skip: errors that invalidate the whole run should throw; item-scoped issues should throw a `PlanApplyException`/`PlanProcessingException` or subclass so existing catch blocks handle them.
- Keep exceptions unchecked and message-rich; include identifiers and cause.
- When integrating new external calls, wrap SDK exceptions in a `PlanApplyException` (apply) or `PlanProcessingException` (plan) with the key/action to avoid leaking SDK-specific types across layers.
- For new IO utilities, mirror `PlanWriter`/`PlanReader` by throwing `IllegalStateException` so runners can treat them as fatal.
