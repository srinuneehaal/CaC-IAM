# Test Plan Suite

Use this checklist to validate changes before merging or promoting builds.

## Unit suite (required)
- Parsing strategies: verify detection of `policies/`, `roles/`, `users/` paths, key derivation fallbacks, and JSON deserialization.  
- PlanService: covers new/update/delete emission, delete detection for missing paths, and ordering handoff.  
- PlanReader/Writer: confirm master plan round-trips payloads into SDK models.  
- Apply path: applier selection (`MissingApplierException` cases), action dispatch, and error handling.  
- State updates: Cosmos upsert/delete logic, partition keys, and payload casting/validation in `StateFileService`.

## Integration smoke (recommended)
1. Create sample payloads under `changedfiles/policies`, `changedfiles/roles`, `changedfiles/users`.  
2. Set `CHANGED_FILES` to those paths and run `--plan`; confirm `plan/masterplan.json` lists expected actions.  
3. Run `--apply`; ensure state writes succeed (Cosmos emulator or mocks) and logs show API dispatches.  
4. Re-run `--plan` to verify no unexpected diffs remain.

## Regression checks
- HTML report generation remains functional when the master plan schema changes.  
- Plan ordering updates still respect dependencies (policies before roles/users by default).  
- New environment variables or property defaults are reflected in `docs/configuration.md` and `confluence_docs/application-pattern.md`.
