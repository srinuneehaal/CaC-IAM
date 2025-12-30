# Confluence Export Notes

The `confluence_docs/` directory mirrors the Confluence space structure used for CaC-Ex. Use these guidelines when publishing:

- `confluence_docs/index.md` acts as the landing page and links to all category pages plus knowledge-share content.  
- `confluence_docs/application-pattern.md` summarizes the plan/apply patterns; update it alongside `spec/design-patterns.md` when behaviors change.  
- Category pages under `confluence_docs/categories/` (policy, role, user) should embed relevant diagrams from `diagrams/` and link to the matching specs under `spec/<Category>/`.  
- Knowledge-share pages live under `confluence_docs/knowledge-share/` and can be used as agendas or resource lists for onboarding sessions.  
- Attach PNG exports of Mermaid diagrams next to the Markdown when importing to Confluence to keep visuals in sync.
