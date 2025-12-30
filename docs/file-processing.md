# File Processing Catalogue

The IAM adaptor reads JSON under `changedfiles/` (or a configured alternative) and routes each path through a parsing strategy that derives the key and payload. The catalog below mirrors the CaC-Ex format so new contributors can trace how files become `PlanItem`s.

| Directory | Strategy class | Category | Model / payload | Notes |
| --- | --- | --- | --- | --- |
| `policies` | `PolicyFileParsingStrategy` | `FileCategory.POLICIES` | `PolicyCreationRequest` | Key from `code`, falls back to filename stem; must sit under the configured changed files dir. |
| `roles` | `RoleFileParsingStrategy` | `FileCategory.ROLES` | `RoleCreationRequest` | Key from `code`; payload used as-is for updates. |
| `users` | `UserFileParsingStrategy` | `FileCategory.USERS` | `CreateUserRequest` | Key prefers `login`, then `emailAddress`, then filename stem to keep deletes stable. |

## How processing ties into the plan
1. `FileParsingStrategyFactory` selects the first strategy whose `supports(Path)` is true (directory segment + `.json`).  
2. Strategies deserialize into SDK request models, derive a key, and wrap everything in `LoadedFile`.  
3. `PlanService` compares each payload to Cosmos state (`StateRepository.findPayload`) to emit `NEW`, `UPDATE`, or `DELETE` items with `cosmos://...` references for deletes.  
4. `PlanReader` rehydrates payloads from the master plan into the same types so the apply phase and state updates remain type-safe.

## Key naming and delete behavior
- Deletes are requested by including a missing path in `CHANGED_FILES`; `PlanService.detectDeletes` derives the key from the path via `PathUtils.baseName`.  
- Keep keys stable across files and state so Cosmos IDs, master plan entries, and API calls all line up.
