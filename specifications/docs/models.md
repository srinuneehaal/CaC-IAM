# Domain model reference

Quick reference for core models used by the plan/apply flow.

## Action (enum)
- Values: `NEW`, `UPDATE`, `DELETE`.
- Drives plan item ordering and how state changes are applied.

## FileCategory (enum)
- Values: `POLICIES`, `ROLES`, `USERS`.
- Determines which parser and API client is used and how items are grouped.

## PlanItem
- Fields: `action` (`Action`), `fileCategory` (`FileCategory`), `key`, `sourcePath`, `payload`, `beforePayload`.
- Meaning: a single change request derived from a changed file and (optionally) existing state.
- Notes: `key` maps to the logical identity used in Cosmos; `beforePayload` holds the current state snapshot when present.

## MasterPlan
- Fields: `items` (`List<PlanItem>`).
- Meaning: ordered list of plan items produced by the plan phase and consumed by apply.
- Notes: ordering is handled by `PlanOrderingRuleEngine` based on config in `plan.ordering.rules`.

## LoadedFile
- Fields: `category` (`FileCategory`), `key`, `path` (`Path`), `payload` (deserialized body).
- Meaning: wraps a parsed file from `CHANGED_FILES` before it is turned into a `PlanItem`.
- Notes: equality/hashCode consider category, key, payload (path is ignored to dedupe logical items).

## StateDocument
- Fields: `id` (logical key), `typeOfItem` (partition key, serialized as `typeOfItem`), `data` (`JsonNode` payload).
- Meaning: representation of a persisted record in Cosmos.
- Notes: `typeOfItem` must match a `FileCategory` string; `data` holds the stored payload.

## StateSnapshot
- Fields: `policies` (`Map<String, PolicyCreationRequest>`), `roles` (`Map<String, RoleCreationRequest>`), `users` (`Map<String, CreateUserRequest>`).
- Meaning: in-memory view of current state pulled from Cosmos to compare with incoming files.
- Notes: maps are unmodifiable; null inputs become empty maps.
