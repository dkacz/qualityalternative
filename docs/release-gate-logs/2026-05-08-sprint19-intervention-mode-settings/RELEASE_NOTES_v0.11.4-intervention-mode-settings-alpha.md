# Quality Alternative v0.11.4-intervention-mode-settings-alpha

This hotfix follows `v0.11.3-session-progress-meditation-alpha` and ships the Settings Mode repair before any AI note-assist work begins.

## What changed

- Settings now shows the real intervention mode instead of hard-coding Soft while the app behaves like Firm.
- Firm is the default mode, preserving the existing five-second Open anyway pause.
- Soft mode is now a real selectable mode: Open anyway is available immediately.
- The selected intervention mode is saved in app settings and included in portable profile export/import.
- Analytics now distinguish Soft and Firm paths and avoid Firm-only unlock events in Soft mode.

## Review and validation

- GPT Pro Slice 19.5B review: `SCORE: 10/10`, `VERDICT: PASS`, `VISUAL REVIEW: PASS`, blockers `None`.
- Visual E2E evidence covers Soft selected in Settings, Soft immediate Open anyway, and Firm wait state.
- AI note assistance remains intentionally excluded from this APK.

## Changelog versus `v0.11.3-session-progress-meditation-alpha`

- Adds truthful, persisted Soft/Firm intervention mode controls.
- Keeps the session-progress durability and meditation fallback from v0.11.3.
- Keeps Sprint 19 AI note assistance gated until after this APK release.
