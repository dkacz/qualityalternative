# v0.11.25 Agent Inbox Autoimport Options Alpha

This internal alpha adds configurable Agent Inbox autoimport behavior and a copyable package-authoring prompt for Codex, Claude Code, or another local agent.

## Changes

- Added Agent Inbox import options for priority handling:
  - Ask me
  - Ignore manifest priority
  - Auto-accept high priority
- Added category handling options:
  - Use manifest topics
  - Import without a specific category, represented internally as `OTHER`
- Added a copyable agent prompt that reflects the selected priority/category modes and avoids machine-specific Drive paths, account names, tokens, and rclone remotes.
- Snapshotted priority/category import policy at import dispatch so in-flight single imports and Import all batches cannot drift if settings change mid-import.
- Updated package authoring docs and the Agent Inbox live review gate so future agents must validate packages and provide live Drive evidence for this feature class.

## Release Gate

- GPT Pro R2: `VERDICT: PASS`, `SCORE: 10/10`
- Live signed-in emulator evidence: passed
- Real Google Drive folder evidence: passed
- Externally created package validation: passed
- Two-package Import all evidence: passed
- Autoimport-on-start evidence: passed
- Library `Files` visibility and reader rendering: passed
- App crash/error sentinel: passed

## APK Assets

- Installable debug APK: `quality-alternative-v0.11.25-agent-inbox-autoimport-options-alpha-debug.apk`
- Installable debug APK SHA-256: `3d9b76ef581d737251370058a31c23912c932771a52efa663e45f30590cff501`
- Unsigned release APK: `quality-alternative-v0.11.25-agent-inbox-autoimport-options-alpha-release-unsigned.apk`
- Unsigned release APK SHA-256: `57eee1021adb0a5d7faebf4969b076a97f7af1ca4bdd2b9f034ecdc554cbf1ce`

## Evidence

- Validation summary: `docs/release-gate-logs/2026-06-18-sprint39-agent-inbox-autoimport-options/VALIDATION_SUMMARY.md`
- Live E2E report: `evidence/sprint39_agent_inbox_autoimport_options_prompt/LIVE_E2E_REPORT_R2.md`
- GPT Pro output: `evidence/sprint39_agent_inbox_autoimport_options_prompt/review_pro_r2_response.md`
