# GPT Pro Review R2 - Sprint 35 Agent Inbox Folder Selector Repair

You are reviewing a revised Android release slice after GPT Pro R1 returned `SCORE: 8/10`, `VERDICT: REVISE`, `VISUAL REVIEW: REVISE`.

Use only the attached bundle as the audit base. If a claim cannot be proven from shipped files, label it `BUNDLE GAP`. Do not infer unshipped source, build results, or live Google Drive behavior.

## Context

The user reported that the prior Agent Inbox Google file Picker route was not a real folder picker for externally populated/rclone package folders. The requested fix is not to remove Agent Inbox, but to repair folder selection so agents can push package folders and the app can import them.

## Implementation Claim To Audit

Sprint 35 should satisfy these claims:

1. Agent Inbox no longer uses Google file Picker as a production folder-selection route.
2. Android `OpenDocumentTree` is the primary folder selector.
3. Local/system document-tree folders scan/import without a Google access token.
4. Google Drive-backed document-tree selections require explicit readonly Drive authorization and scan/import only the selected/extracted folder id.
5. Existing `readonly_folder` grants continue through readonly scan/import.
6. Existing legacy `picker_folder` grants are treated as repair state and cannot continue through `drive.file`.
7. Annotation sync still uses `drive.file`; Agent Inbox Drive scan/import uses `drive.readonly`.
8. Package authoring instructions are portable for Codex/Claude-style agents and do not assume this user's local rclone remote, account, folder id, paths, or tokens.
9. Release evidence supports the APK claim for `versionCode=39`, `versionName=0.11.23-alpha`, including forced Gradle gate, unit XML, APK metadata/hash, connected visual E2E, install, and launch evidence.

## R1 Findings To Recheck

R1 blockers should now be fixed:

- Production dependency wiring is now shipped in `AppContainer.kt`.
- Scope constants are now shipped in `ReadingAnnotationDriveSync.kt`.
- App-side package model/validation sources are now shipped.
- Visual evidence is Sprint35-named and the access-lost fixture clears the stale folder draft like production recovery.
- Unit XML, APK badging, install, package dump, and direct launch evidence are now shipped.
- The bundle no longer relies on full noisy `docs/LANE_STATUS.md` or prior Sprint 34 review output; it ships a scoped Sprint 35 status excerpt instead.

## Review Tasks

1. Inventory the bundle and confirm it is scoped enough for this review.
2. Trace the production folder selection and authorization paths from UI to ViewModel to Drive/document-tree clients.
3. Verify legacy picker grants cannot continue under `drive.file`.
4. Verify Google Drive scans are bounded to the selected folder id, not whole-Drive discovery/search.
5. Verify package authoring instructions and validator contract are portable and not user-machine-specific.
6. Verify test and release evidence, including visual evidence and APK metadata.
7. Report any fresh bugs, behavioral regressions, missing tests, evidence gaps, or package hygiene issues.

## Output Format

Return exactly these sections:

SCORE: `<0-10>/10`

VERDICT: `PASS` or `REVISE`

VISUAL REVIEW: `PASS`, `REVISE`, or `NOT APPLICABLE`

FRESH FINDINGS:

TRACE CHECKS:

BUNDLE GAPS:

PACKAGE HYGIENE:

RELEASE READINESS:

Use concrete file paths from the bundle. If no fresh findings or bundle gaps remain, say `None`.
