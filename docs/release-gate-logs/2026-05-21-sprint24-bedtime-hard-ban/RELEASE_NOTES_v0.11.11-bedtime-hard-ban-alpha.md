# Quality Alternative v0.11.11-bedtime-hard-ban-alpha

This release follows `v0.11.10-reader-footer-progress-alpha` and ships Sprint 24 Bedtime hard-ban protection.

## What Changed

- Added opt-in Bedtime protection in Settings with persisted start/end times.
- During active Bedtime, distracting-app interception now keeps reading, meditation, and bounded quiet alternatives available, hides `Pause 15 min`, and requires a visible 60-second emergency unlock before opening the original app.
- Normal Open Anyway quiet windows no longer bypass active Bedtime; only a completed Bedtime emergency unlock can quiet repeated opens during Bedtime.
- Stale interventions now convert to Bedtime enforcement across settings emissions, user actions, and pure clock transitions.
- Portable Profile export/import now preserves Bedtime settings without unknown-field warnings.
- Added visual E2E evidence for the Bedtime settings and calm hard-ban intervention screens.

## Review And Validation

- GPT Pro Sprint 24 R7 review: `SCORE: 10/10`, `VERDICT: PASS`, `VISUAL REVIEW: PASS`, fresh findings `None`.
- JVM regression coverage includes Bedtime runtime suppression, stale Open Anyway conversion, stale Pause refusal, foreground duplicate handling, pure clock boundary conversion, settings persistence, and Portable Profile export.
- Connected visual E2E verifies the Bedtime settings section, quiet alternatives, disabled `Breathe 60s` emergency action, and absence of `Pause 15 min`.
- APK evidence includes `versionCode=27`, `versionName=0.11.11-alpha`, signature verification, SHA-256 hash, emulator install, cold launch, and shutdown confirmation.

## Changelog Versus `v0.11.10-reader-footer-progress-alpha`

- Keeps the Sprint 23 reader footer progress-bar fix.
- Adds a user-controlled sleep-window hard ban that preserves alternatives instead of creating a total dead end.
- Tightens interception boundary behavior so normal unlocks, foreground duplicate de-noising, and clock transitions cannot silently bypass Bedtime.
- Adds release evidence for the Bedtime visual path and final installable debug APK.
