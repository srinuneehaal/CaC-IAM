# Users Plan + Apply

Users map to Identity profiles and are applied after policies/roles by default.

## Plan flow

```mermaid
flowchart TD
    Env[CHANGED_FILES list]
    Runner[PlanRunner (--plan)]
    Parse[UserFileParsingStrategy -> LoadedFile(USERS)]
    State[StateRepository.findPayload(USERS, key)]
    Compare[PlanService.payloadsEqual]
    New[PlanItem Action.NEW]
    Update[PlanItem Action.UPDATE]
    Delete[PlanItem Action.DELETE]

    Env --> Runner --> Parse --> Compare
    Parse --> State --> Compare
    Compare -->|no state| New
    Compare -->|payload differs| Update
    Runner -->|missing path| Delete
```

- Directory: `changedfiles/<...>/users/` (configurable).  
- Key preference: `login`, then `emailAddress`, then filename stem. This keeps delete detection stable even if emails change.  
- Ordering: users run after roles unless overridden.

## Apply flow

```mermaid
flowchart TD
    Plan[MasterPlan item (USERS)]
    Applier[UserPlanItemApplier]
    Api[UserApiService]
    StateSvc[StateFileService]
    Cosmos[CosmosStateRepository]

    Plan --> Applier --> Api
    Api --> StateSvc --> Cosmos
```

- Payload becomes `CreateUserRequest` via `PlanReader`.  
- `UserApiService` currently logs create/update/delete; swap in real identity calls when ready.  
- `StateFileService` upserts/deletes Cosmos documents partitioned by `typeOfItem=USERS`, ensuring `login`/`emailAddress` fields are populated.

## Data contracts
- Payload: `com.finbourne.identity.model.CreateUserRequest`.  
- Plan item key: preferred `login`, then `emailAddress`, else filename stem.  
- State document: `StateDocument` with `id=<key>`, `typeOfItem="USERS"`, `data` = serialized user request.

## Operational considerations
- Because keys may come from email, prefer immutable logins to avoid churn.  
- Keep CHANGED_FILES accurate; missing paths drive deletes.  
- Update `confluence_docs/categories/user.md` and related diagrams when the user lifecycle changes.
