# GPT Pro Review R3 - Sprint 35 Agent Inbox Folder Selector Repair

You are reviewing the revised Android release slice after GPT Pro R2 returned:

- `SCORE: 9/10`
- `VERDICT: REVISE`
- `VISUAL REVIEW: PASS`

Use only the attached bundle as the audit base. If a claim cannot be proven from shipped files, label it `BUNDLE GAP`.

## R3 Scope

This R3 packet is focused on the single R2 blocker and release evidence freshness.

Please verify:

1. Legacy `picker_folder` grants remain restoreable only as repair state.
2. `picker_folder` is no longer included in operational `hasAgentInboxDriveFolderGrant`.
3. Direct ViewModel scan/import calls cannot operate under `picker_folder`.
4. Repository scan-success persistence cannot re-establish `picker_folder` as a working connection.
5. The visible repair UI is preserved: picker repair state still routes to Drive link reconnect and can still be disconnected.
6. The release gate was rerun after the R2 fix and current APK evidence still supports `versionCode=39`, `versionName=0.11.23-alpha`.
7. No new regression was introduced in the previously accepted R2 scope.

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

If no fresh findings or bundle gaps remain, say `None`.
