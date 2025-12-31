# Cosmos DB state store

Reference for how CaC-IAM uses Azure Cosmos DB to persist state across plan/apply runs.

## Responsibilities
- Persist the latest policy, role, and user payloads so `PlanService` can diff incoming JSON against stored state.
- Provide point lookups (`findPayload`) and key listings (`listKeys`) for delete detection and ordering.
- Maintain idempotent upserts/deletes during apply so subsequent plan runs see the new baseline.

## Data model
- Single container (default `policies_roles`) with partition key `/typeOfItem`; values are `POLICIES`, `ROLES`, `USERS`.
- Document shape:
  ```json
  {
    "id": "policy-code-or-login",
    "typeOfItem": "POLICIES",
    "data": { "full": "Finbourne SDK request body" }
  }
  ```
- Keys per category: policy `code`, role `code`, user `login` (or `emailAddress` as fallback). Keys are echoed into `id` when missing.

## Access patterns
- **Reads**: `CosmosStateRepository.loadSnapshot` performs `readAllItems` per partition; `findPayload` uses `readItem` and treats 404 as empty; `listKeys` runs `SELECT c.id ... WHERE c.typeOfItem=@typeOfItem`.
- **Writes**: `StateFileService` calls `upsertItem` with the category as the partition key; deletes swallow 404s but log other errors.
- **Diffing**: `PlanService` compares parsed files to `findPayload` results; deletes include a `cosmos://<CATEGORY>/<id>` reference in the plan.

## Configuration and connectivity
- Properties: `azure.cosmos.uri`, `azure.cosmos.key`, `azure.cosmos.database`, `azure.cosmos.container`, `azure.cosmos.partition-key` (env overrides: `AZURE_COSMOS_*`).
- Defaults in `application-local.properties` target the Cosmos emulator at `https://localhost:8081` with the emulator key.
- `CosmosStateConfiguration` builds the client with eventual consistency and `contentResponseOnWriteEnabled(true)`, then retrieves the configured database/container (no auto-create).

## Health and operations
- `/actuator/health` is backed by `CosmosHealthIndicator`, which runs `SELECT TOP 1 * FROM c`; status code is included on failures.
- Local usage: start the Cosmos emulator, then run with `spring-boot:run -- --plan/--apply --spring.profiles.active=local` or set `AZURE_COSMOS_*` env vars for a live account.
- Partition key alignment is critical; if you change `azure.cosmos.partition-key`, update the container and ensure `StateDocument.typeOfItem` still matches.

## Testing
- Unit coverage: `CosmosStateRepositoryTest`, `CosmosStateConfigurationTest`, `CosmosStatePropertiesTest`, `CosmosHealthIndicatorTest`.
- Integration guidance in `integration-test-plan.md`: prefer the emulator or mocks; avoid live Cosmos in CI.
