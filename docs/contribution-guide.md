# Contribution Guide

This project mirrors the CaC-Ex plan/apply layout. Keep changes small, well-tested, and documented so new IAM categories or behaviors stay predictable.

## Workflow
- Prefer feature branches; keep commits focused and avoid amending others' work.  
- Run `mvn -q test` before pushing. For plan/apply behavior changes, also run `--plan` and `--apply` locally with representative `CHANGED_FILES`.

## Code changes
- When adding a category, update `FileCategory`, `FileLocationProperties`, parsing strategies, `PlanService` state lookup, `PlanItemApplier`, and ordering rules.  
- Keep keys stable across parsing, plan, apply, and state persistence to avoid duplicate or orphaned documents.  
- Avoid breaking the HTML report; if the master plan schema changes, update `MasterPlanHtmlReportGenerator` and the template.

## Tests
- Add/adjust unit tests that cover parsing (`*FileParsingStrategyTest`), ordering (`PlanOrderingRuleEngineTest`), plan generation (`PlanServiceTest`), plan reading (`PlanReaderTest`), appliers/API services, and state updates (`StateFileServiceTest`, `CosmosStateRepositoryTest`).  
- Use the Cosmos emulator or mocks for repository tests; avoid hitting live services in unit tests.

## Documentation
- Mirror updates in `docs/`, `spec/`, and `confluence_docs/`. Include new diagrams under `diagrams/` when flows change.  
- Note any new env vars or property defaults in `docs/configuration.md`.  
- If API call behavior changes (e.g., uncommented SDK calls), document the operational impact under `spec/<Category>/`.
