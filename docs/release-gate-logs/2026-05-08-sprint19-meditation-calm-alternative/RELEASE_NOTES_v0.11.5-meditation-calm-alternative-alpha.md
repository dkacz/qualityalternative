# Quality Alternative v0.11.5-meditation-calm-alternative-alpha

This hotfix follows `v0.11.4-intervention-mode-settings-alpha` and ships the meditation intervention presentation fix before AI note-assist work continues.

## What changed

- Meditation is no longer shown as a normal row inside `Other options`.
- When reading is the primary recommendation, meditation now appears as a separate `Calm reset` panel.
- The calm panel has its own `Start` action, pause icon, quiet breathing copy, and 1m/3m/5m/10m duration controls.
- `Other options` remains reserved for normal reading, link, and file alternatives.
- The dedicated meditation `Start` action opens the meditation timer.

## Review and validation

- GPT Pro meditation UI review: `SCORE: 10/10`, `VERDICT: PASS`, `VISUAL REVIEW: PASS`, blockers `None`.
- Visual E2E evidence covers the separate green calm-reset panel and confirms meditation is not duplicated as an `Other options` row.
- AI note assistance remains intentionally excluded from this APK.

## Changelog versus `v0.11.4-intervention-mode-settings-alpha`

- Keeps the truthful Soft/Firm Settings Mode behavior from v0.11.4.
- Adds distinct meditation presentation as a calm non-reading alternative.
- Keeps Sprint 19 AI note assistance gated until after this APK release.
