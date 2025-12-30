# Testing Steps

Quick references for validating CaC-IAM changes.

## Unit tests
- Run `mvn -q test` for the full suite.  
- Focus areas: parsing strategies, plan ordering, plan IO, appliers, `StateFileService`, and `CosmosStateRepository`.

## Plan/apply smoke
1. Set `CHANGED_FILES` to one policy, one role, and one user JSON path.  
2. Run `mvn -q -DskipTests spring-boot:run -- --plan`; inspect `plan/masterplan.json` (and `masterplan.html`).  
3. Run `mvn -q -DskipTests spring-boot:run -- --apply`; confirm logs show each category and state updates succeed.

## Regression checks
- Verify `plan.ordering.rules` still reflect desired dependencies.  
- Ensure HTML report generation remains functional when the plan schema changes.  
- Update diagrams and specs if you change directory names, key derivation, or API calls.
