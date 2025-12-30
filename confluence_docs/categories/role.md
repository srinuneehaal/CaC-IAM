# Role Category

- **Files**: `changedfiles/.../roles/*.json` (configurable).  
- **Parsing**: `RoleFileParsingStrategy` loads `RoleCreationRequest`, key from `code` (fallback: filename stem).  
- **Plan**: Compares against Cosmos `typeOfItem=ROLES`, emits `NEW/UPDATE/DELETE`. Ordered after policies to respect dependencies.  
- **Apply**: `RolePlanItemApplier` → `RoleApiService` (logs by default) → `StateFileService` upsert/delete.  
- **References**: `spec/Roles/roles-plan-apply.md`, `spec/Roles/roles-plan.mmd`, `spec/Roles/roles-apply.mmd`.
