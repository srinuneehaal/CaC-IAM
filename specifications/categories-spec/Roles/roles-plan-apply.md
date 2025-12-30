# Roles Plan + Apply

Roles bundle policies for specific user groups. They are planned after policies so references are available.

## Plan flow

```mermaid
flowchart TD
    Env[CHANGED_FILES list]
    Runner[PlanRunner plan]
    Parse[RoleFileParsingStrategy to LoadedFile ROLES]
    State[StateRepository findPayload ROLES]
    Compare[PlanService.payloadsEqual]
    New[PlanItem NEW]
    Update[PlanItem UPDATE]
    Delete[PlanItem DELETE]

    Env --> Runner --> Parse --> Compare
    Parse --> State --> Compare
    Compare -->|no state| New
    Compare -->|payload differs| Update
    Runner -->|missing path| Delete
```

- Directory: `changedfiles/<...>/roles/` (configurable).  
- Key: `RoleCreationRequest.code`, falling back to filename stem for deletes.  
- Ordering: roles are processed after policies and before users unless overridden in `plan.ordering.rules`.

## Apply flow

```mermaid
flowchart TD
    Plan[MasterPlan_ROLES]
    Applier[RolePlanItemApplier]
    Api[RoleApiService]
    StateSvc[StateFileService]
    Cosmos[CosmosStateRepository]

    Plan --> Applier --> Api
    Api --> StateSvc --> Cosmos
```

- Payload is rehydrated into `RoleCreationRequest` by `PlanReader`.  
- `RoleApiService` constructs `RoleUpdateRequest` for updates; SDK calls are currently commented and can be enabled with credentials.  
- `StateFileService` upserts/deletes Cosmos documents partitioned by `typeOfItem=ROLES`.

## Data contracts
- Payload: `com.finbourne.access.model.RoleCreationRequest`.  
- Plan item key: role `code`.  
- State document: `StateDocument` with `id=<code>`, `typeOfItem="ROLES"`, `data` = serialized role request.

## Operational considerations
- Keep policy references valid; invalid references will surface during downstream API execution when SDK calls are enabled.  
- Use delete requests (missing file in `CHANGED_FILES`) to retire obsolete roles cleanly.  
- Update `confluence_docs/categories/role.md` and diagrams when role flows change.
