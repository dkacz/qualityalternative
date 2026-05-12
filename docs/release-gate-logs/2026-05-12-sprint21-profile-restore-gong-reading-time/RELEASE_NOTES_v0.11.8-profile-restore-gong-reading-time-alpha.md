# Quality Alternative v0.11.8-profile-restore-gong-reading-time-alpha

This release follows `v0.11.7-epub-loading-performance-alpha` and ships the Sprint 21 profile restore, meditation gong, and reading-time polish.

## What Changed

- Default portable profile backup now uses a clear shared path: `Downloads/Quality Alternative/quality-alternative-profile.json`.
- Fresh onboarding can restore the default profile backup after app data is cleared or the app is reinstalled.
- Settings `Restore default backup` is now safe: it opens a preview first, preserves local settings until confirmation, and requires an explicit Replace confirmation before applying imported profile data.
- Default profile restore now handles Android MediaStore collision names and chooses the newest matching backup.
- Meditation completion now uses an in-app generated gong instead of a short system beep.
- EPUB/Markdown import reading-time estimates now use extracted text and support multi-hour books with a defensive 720-minute cap, while links and session defaults stay short.

## Review And Validation

- GPT Pro Sprint 21 R6 review: `SCORE: 10/10`, `VERDICT: PASS`, `VISUAL REVIEW: PASS`, blockers `none`.
- Unit evidence covers profile restore preview safety, extracted-text reading-time estimates, and import candidate estimates.
- Connected visual evidence covers onboarding restore entry, Settings preview/confirmation/success, meditation calm alternative plus completed gong state, and long-document import preview.
- Package hygiene keeps the canonical named review trail and current R6 bundle without raw harvest duplicates.

## Changelog Versus `v0.11.7-epub-loading-performance-alpha`

- Keeps the Sprint 20 EPUB loading performance fixes from v0.11.7.
- Adds reinstall-oriented profile recovery from the default shared Downloads backup.
- Makes Settings default profile restore non-destructive until explicit confirmation.
- Replaces meditation completion beeps with a generated gong.
- Corrects long EPUB/Markdown reading-time labels so large books are no longer shown as short sessions.
