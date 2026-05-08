You are doing a fresh-from-scratch adversarial audit of one scoped Android hotfix.

Do not output interim status updates. Your first and only response must be the final audit in the required output format, including `SCORE:`, `VERDICT:`, and `VISUAL REVIEW:`.

GUIDING PRINCIPLES (respect these in your review):
1. Numbers in validation evidence are verified against shipped logs/screenshots, so do not question them without checking the shipped files.
2. Do not suggest weakening product claims unless you can name the concrete tester failure that the change would preempt.
3. Style suggestions cannot change product meaning.
4. Review the implementation as-is; do not reference hidden development history.
5. Screenshots, logs, PRD, sprint plan, and code must be consistent; flag mismatches specifically.
6. Your feedback is input, not instruction; duplicate or already-covered suggestions should not be inflated into fresh findings.

Read the full attached `evidence/sprint19_session_progress_hotfix/HOTFIX_EVIDENCE.md` first.
Then deep-review only this scope:

Sprint 19 Slice 19.5A R4 session progress durability and meditation backup hotfix.

Bundle rules:

- Use only the shipped bundle as the audit base.
- If a claim cannot be proven from shipped files, label it `BUNDLE GAP`.
- Do not review unrelated AI implementation, because AI is intentionally blocked and not included.
- Do not review unrelated Google Drive behavior, except to confirm this hotfix does not claim to change it.
- Treat `PRD.md` and `docs/SPRINT_19_AI_NOTE_ASSIST.md` as the product contract.
- Treat the connected test logs and screenshots as the visual E2E evidence for this hotfix.

Known prior bug classes to actively test against:

- Reader session progress saved too weakly: after reading, lock/reopen/app restart can return to the pre-session location.
- Backward reader navigation not persisted: only forward navigation updated durable progress.
- Lifecycle pause/stop not refreshing durable store when the visible source position is unchanged.
- Reader back/skip clears current content before the disposal save can capture the active reader state.
- Late disposal save after completion downgrades completed progress back to unfinished progress.
- In-flight race: unfinished lifecycle/disposal save passes ViewModel gate before completion is visible, then writes after completed progress.
- Duplicate progress analytics from lifecycle retries.
- Meditation disappears from intervention alternatives when reading items dominate the ranked inventory.
- Scope drift into AI note assistance before the regression APK/hotfix is reviewed.

Your job:

1. Verify the R1/R3 blockers are closed: reader back/skip persists before clearing active content, and late disposal/lifecycle writes cannot downgrade completed progress even when unfinished save and completion interleave.
2. Verify completion dominance is enforced at the final write boundary in `RoomReadingProgressRepository.saveProgress`, while intentional completed-content reactivation can still start a new reading cycle.
3. Verify the code durably refreshes source-anchored reader progress on forward moves, backward moves, lifecycle pause/stop, and reader disposal.
4. Verify same-visible-position lifecycle saves refresh durable storage without creating duplicate `READING_PROGRESS_SAVED` analytics.
5. Verify the connected E2E evidence proves Activity pause/stop/resume and close/reopen both return to the last viewed reader page after page forward/back navigation.
6. Verify meditation remains a visible finite backup alternative when reading is primary and meditation is eligible.
7. Verify the PRD/sprint docs accurately capture the new product rules and keep AI out of this hotfix.
8. Inspect package hygiene: identify stale, missing, redundant, or misleading files in this bundle, or say it is clean enough.

Output format:

SCORE: x/10
VERDICT: PASS / FAIL
VISUAL REVIEW: PASS / FAIL

FRESH FINDINGS:
- Numbered list with severity, exact claim, why it is vulnerable, file(s) checked, and the tightest fix.
- If no blockers remain, say `None`.

TRACE CHECKS:
- Cite exact files, tests, log names, screenshot names, and code paths used.

BLOCKERS:
- List release-blocking issues only.
- If none, say `None`.

SESSION PROGRESS:
- State whether forward, backward, lifecycle pause/stop, disposal, back/skip exit ordering, completion-downgrade prevention, same-position durable refresh, and duplicate analytics behavior are proven.

MEDITATION BACKUP:
- State whether meditation remains visible as a finite backup and whether the evidence proves it.

AI BOUNDARY:
- State whether this hotfix correctly avoids implementing AI before the release/hotfix gate.

BUNDLE GAPS:
- Only if needed.

PACKAGE HYGIENE:
- Say whether the bundle is clean enough for this scoped review, and name any files that should be removed or added for a future packet.
