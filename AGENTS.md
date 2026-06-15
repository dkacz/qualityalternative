# Repository Instructions

## Workspace Ownership And Cleanliness

- Treat every generated review bundle, GPT Pro output, validation log, APK artifact, screenshot, and temporary sprint artifact in this repository as Codex-owned unless the user explicitly marks it otherwise.
- Keep the repository clean. Do not leave stale generated artifacts, duplicate review bundles, or obsolete outputs scattered in the repo root.
- When using GPT Pro slice reviews, preserve only the current auditable trail needed for the sprint and move or delete superseded/generated noise before claiming release readiness.
- Codex is responsible for the repository state it creates in this project.

This repository defines an Android-first MVP for "quality replacement at the moment of impulse." Future work should protect the product thesis and avoid accidental scope drift.

## Source of Truth

- `PRD.md` is the primary execution document.
- `PRODUCT_MEMO.md` provides strategic context, not implementation authority.
- If code, tests, or implementation plans conflict with `PRD.md`, follow `PRD.md` unless the user explicitly changes it.

## Scope Guardrails

- Do not expand MVP beyond the current PRD without an explicit PRD update.
- Do not add iOS parity work unless explicitly requested.
- Do not default to hard-block behavior. Soft intervention is the default product shape.
- Do not turn the replacement flow into a browsing feed, infinite list, or discovery-heavy experience.
- Do not introduce heavyweight AI retrieval or open-web crawling as a prerequisite for MVP.

## Implementation Style

- Build in small vertical slices that can be demoed end to end.
- Before implementing a slice, map the work to the relevant `FR` and `NFR` items in `PRD.md`.
- Prefer simple, explainable ranking logic first. Complexity should follow observed product value, not precede it.
- Treat analytics instrumentation as part of the feature, not a follow-up task.

## Agent Inbox Package Authoring

- When creating private Markdown or EPUB packages for Agent Inbox, follow `docs/AGENT_INBOX_PACKAGE_AUTHORING.md`.
- Validate each package with `python3 tools/validate_agent_inbox_package.py <package-folder>` before uploading or syncing it into the user's selected Agent Inbox folder.
- Do not hard-code the user's local paths, rclone remote names, Google account, Drive folder ids, access tokens, or machine-specific assumptions into package content or instructions.
- Keep generated private content packages out of git unless the user explicitly asks to commit an example fixture.

## Testing Strategy

- Use acceptance criteria from `PRD.md` as the baseline test contract.
- Prefer targeted TDD for:
  - ranking logic
  - state transitions
  - permission gating
  - analytics event mapping
- Use end-to-end tests sparingly for the critical user path only.
- A slice is not done until the changed logic has right-sized tests.

## Definition of Done

A work slice is only done when all of the following are true:

- the primary user flow works manually
- the most important changed logic has automated tests
- analytics events for the slice are implemented
- no out-of-scope behavior was added unintentionally
- documentation is updated if the slice changed requirements or assumptions

## Git Workflow

- Keep `main` as the stable branch.
- Create a fresh branch for each meaningful implementation slice.
- Do not commit directly to `main`.
- Keep commits focused and easy to review.
- Record important architecture or scope decisions in `PRD.md` or a dedicated decision document if one is later added.
