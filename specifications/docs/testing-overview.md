# Testing Overview

The test suite favors fast unit tests around parsing, ordering, plan generation, apply dispatch, and state persistence.

## Running tests
- Full suite: `mvn -q test` (includes Jacoco report).  
- Targeted class: `mvn -q -Dtest=PlanServiceTest test`.  
- Skip tests during local exploration: `mvn -q -DskipTests spring-boot:run -- --plan`.

## Coverage highlights
- **Parsing and keying**: `Policy/Role/UserFileParsingStrategyTest` guard directory detection, payload deserialization, and key derivation fallbacks.  
- **Ordering**: `PlanOrderingRuleEngineTest` validates property-driven sequencing and the default IAM order.  
- **Plan creation**: `PlanServiceTest` exercises new/update/delete detection and object comparison.  
- **Plan IO**: `PlanWriterTest` and `PlanReaderTest` ensure master plan JSON round-trips into typed payloads.  
- **Apply path**: `PlanApplyServiceTest`, `AbstractPlanItemApplierTest`, and per-category applier tests verify dispatch and type safety.  
- **State persistence**: `StateFileServiceTest` and `CosmosStateRepositoryTest` cover upsert/delete flows and error handling.  
- **Utilities**: `PathUtilsTest`, `CommandLineFlagsTest`, and serializer tests keep helpers predictable.

## Adding tests
- New categories should ship with parsing tests, ordering coverage, applier tests, and state update checks.  
- Prefer mocks over live SDK calls; when touching Cosmos integration, limit emulator usage to explicit integration tests.  
- Update snapshots or fixtures under `changedfiles/` test resources if payload shapes change.
