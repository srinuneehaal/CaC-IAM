# Policies Plan + Apply

Policies capture coarse IAM permissions and are planned/applied before dependent roles. This doc mirrors the CaC-Ex spec layout for easy navigation.

## Plan flow

```mermaid
flowchart TD
    Env[CHANGED_FILES list]
    Runner[PlanRunner (--plan)]
    Parse[PolicyFileParsingStrategy -> LoadedFile(POLICIES)]
    State[StateRepository.findPayload(POLICIES, key)]
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

- Paths must live under `changedfiles/<...>/policies/` (configurable via `cacex.paths.*`).  
- Keys come from `PolicyCreationRequest.code`, falling back to the filename stem so delete detection remains stable.  
- Ordering rules place policy creates/updates before roles/users; deletes run last by default.

## Apply flow

```mermaid
flowchart TD
    Plan[MasterPlan item (POLICIES)]
    Applier[PolicyPlanItemApplier]
    Api[PolicyApiService]
    StateSvc[StateFileService]
    Cosmos[CosmosStateRepository]

    Plan --> Applier --> Api
    Api --> StateSvc --> Cosmos
```

- `PlanReader` rehydrates the payload into `PolicyCreationRequest`.  
- `PolicyApiService` currently logs calls; wire credentials and uncomment SDK calls to execute against Finbourne Access.  
- `StateFileService` upserts or deletes Cosmos documents partitioned by `typeOfItem=POLICIES`.

## Data contracts
- Payload: `com.finbourne.access.model.PolicyCreationRequest`.  
- Plan item key: policy `code`.  
- State document: `StateDocument` with `id=<code>`, `typeOfItem="POLICIES"`, `data` = serialized policy request.

## Operational considerations
- Keep policy codes immutable to avoid duplicate documents; rename by issuing a delete + new pair.  
- If policies reference other resources, retain the default ordering or adjust `plan.ordering.rules` so dependencies resolve correctly.  
- Update `diagrams/plan-sequence.mmd` and `confluence_docs/categories/policy.md` if the flow changes.
