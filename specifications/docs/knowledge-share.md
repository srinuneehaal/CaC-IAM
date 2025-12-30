# Knowledge Share Guide

Use this as a lightweight plan for onboarding teammates or running refresher sessions.

## Prep materials
- `docs/architecture.md` for the end-to-end flow.  
- `docs/file-processing.md` + `spec/*` for category specifics.  
- `diagrams/*.mmd` rendered as PNG or in Mermaid live editor for visuals.  
- `docs/integration-test-plan.md` to walk through a live demo.

## Suggested agenda (60–90 minutes)
1. **Overview (10 min)**: Walk through the high-level flow and major components.  
2. **Plan deep dive (15 min)**: Show how `CHANGED_FILES` drives `PlanService`, key derivation, and ordering.  
3. **Apply deep dive (15 min)**: Highlight applier mapping, API service boundaries, and Cosmos updates.  
4. **Hands-on (15–30 min)**: Modify a sample policy/role/user file, run `--plan`, inspect the HTML report, then `--apply`.  
5. **Ops/observability (5–10 min)**: Review configuration overrides, health checks, and report toggles.  
6. **Q&A (10 min)**: Capture follow-ups and doc updates.

## Follow-up actions
- Link the relevant `confluence_docs/*` pages to your team space.  
- Capture any new patterns under `spec/design-patterns.md` and update diagrams if flows changed.  
- Encourage new contributors to add or extend tests for the paths they touched.
