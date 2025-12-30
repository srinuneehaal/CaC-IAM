# Item Workflow (Add/Update/Delete)

Use this checklist to add or change IAM assets and drive them through the plan/apply cycle.

## 1) Prepare a JSON change
1. Pick the directory under the changed files root (`changedfiles/` by default): `policies/`, `roles/`, or `users/`.  
2. Copy an existing JSON example or craft a new one that matches the SDK model for that category (`PolicyCreationRequest`, `RoleCreationRequest`, `CreateUserRequest`).  
3. Ensure the payload includes a stable key (`code` for policies/roles, `login` or `emailAddress` for users). The filename stem is used as a fallback key.

## 2) Populate `CHANGED_FILES`
- Export a space-separated list of files you want included:
  ```powershell
  $env:CHANGED_FILES = "changedfiles/policies/policy-traders.json changedfiles/roles/role-ops.json"
  ```
- Missing paths in the list are treated as delete requests; keep paths accurate to avoid accidental deletes.

## 3) Generate the plan
1. Run `mvn -q -DskipTests spring-boot:run -- --plan`.  
2. Inspect `plan/masterplan.json` to confirm each `PlanItem` shows the right `fileCategory`, `key`, and `action`.  
3. Open `plan/masterplan.html` (unless disabled) for a quick KPI/table view. Re-run the plan after tweaks until it looks correct.

## 4) Apply
1. Run `mvn -q -DskipTests spring-boot:run -- --apply`.  
2. The apply phase rehydrates payloads, dispatches to the appropriate API service, and persists state to Cosmos. Errors are logged per item; the run continues for other entries.

## 5) Verify and iterate
- Re-run `--plan` to confirm no unexpected changes remain after state persistence.  
- Check Cosmos (emulator or live) to see upserted documents keyed by category and logical key.  
- Update docs under `spec/` and `confluence_docs/` if you introduced new patterns or directories.
