# User Category

- **Files**: `changedfiles/.../users/*.json` (configurable).  
- **Parsing**: `UserFileParsingStrategy` loads `CreateUserRequest`, key from `login`, then `emailAddress`, else filename stem.  
- **Plan**: Compares against Cosmos `typeOfItem=USERS`, emits `NEW/UPDATE/DELETE`. Ordered after roles by default.  
- **Apply**: `UserPlanItemApplier` → `UserApiService` (logs) → `StateFileService` upsert/delete.  
- **References**: `spec/Users/users-plan-apply.md`, `spec/Users/users-plan.mmd`, `spec/Users/users-apply.mmd`.
