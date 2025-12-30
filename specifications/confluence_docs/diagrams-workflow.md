# Diagrams & Workflow

Use these diagrams when explaining or reviewing CaC-IAM flows:

- `diagrams/plan-sequence.mmd`: CHANGED_FILES → PlanRunner → PlanService → ordering → writer/report.  
- `diagrams/apply-sequence.mmd`: ApplyRunner → PlanApplyService → appliers → API services → state updates.  
- `diagrams/class-overview.mmd`: Component relationships across plan/apply, repositories, and appliers.  
- Category-specific flows live under `spec/Policies|Roles|Users/*.mmd`.

## Tips
- Render Mermaid via VS Code preview or https://mermaid.live and export PNGs for Confluence attachments.  
- Keep arrows/actions aligned with the latest code whenever plan ordering, key derivation, or API integration changes.  
- Cross-link diagrams from `confluence_docs/categories/*.md` so readers can jump directly to visuals relevant to their category.
