# Integration Test Plan

Run these steps to exercise the full plan → apply → state persistence loop with real configuration files.

## Prerequisites
- Java 21 and Maven installed.  
- Cosmos DB emulator running locally (defaults in `application-local.properties`) or valid Cosmos connection details in env vars.  
- Optional: real Access/Identity credentials if you intend to uncomment SDK calls in `PolicyApiService`/`RoleApiService`. By default, those services log actions instead of invoking the APIs.

## Scenario setup
1. Create sample payloads under the changed files root (defaults shown):  
   - `changedfiles/policies/policy-sample.json` with a `code` and `actions`.  
   - `changedfiles/roles/role-sample.json` with a `code` and role grants.  
   - `changedfiles/users/user-sample.json` with `login`/`emailAddress`.  
   Use the SDK model shapes from `docs/file-processing.md` as a guide.
2. Export `CHANGED_FILES` with the paths you want to include:
   ```powershell
   $env:CHANGED_FILES = "changedfiles/policies/policy-sample.json changedfiles/roles/role-sample.json changedfiles/users/user-sample.json"
   ```

## Execution
1. **Plan**: `mvn -q -DskipTests spring-boot:run -- --plan`  
   - Expect `plan/masterplan.json` (and `plan/masterplan.html` if enabled) to contain three items ordered policy → role → user.  
   - Verify actions (`NEW` vs `UPDATE`) by adjusting payloads and rerunning.
2. **Apply**: `mvn -q -DskipTests spring-boot:run -- --apply`  
   - Confirm logs show dispatch to the correct applier and API service per item.  
   - Inspect Cosmos documents for `typeOfItem = POLICIES | ROLES | USERS` with IDs matching your keys.
3. **Delete path**: Remove one of the files while keeping it in `CHANGED_FILES`; rerun `--plan` and ensure a `DELETE` item appears, then apply and confirm the document is removed.

## Success criteria
- Plan items align with payload keys and categories; ordering matches configuration.  
- Apply run completes without unhandled exceptions; Cosmos state reflects the plan.  
- Re-running `--plan` after apply yields zero differences for unchanged payloads.
