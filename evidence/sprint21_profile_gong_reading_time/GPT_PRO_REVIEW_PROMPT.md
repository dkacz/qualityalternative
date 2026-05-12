You are doing a fresh-from-scratch adversarial audit of Sprint 21 Profile Restore, Meditation Gong, and Reading Time.

GUIDING PRINCIPLES:
1. Use only the shipped bundle as the audit base.
2. If a claim cannot be proven from shipped files, label it `BUNDLE GAP`.
3. Do not inflate already-covered or irrelevant suggestions into blockers.
4. Prioritize user-visible correctness, Android lifecycle/storage behavior, visual evidence, and tests.
5. Treat package hygiene and stale artifacts as reviewable release risks.

PRIMARY DOCUMENTS TO READ FIRST:
- `evidence/sprint21_profile_gong_reading_time/README.md`
- `PRD.md`
- `sprint21_current_diff.patch`

SCOPE:
Audit only this Sprint 21 slice:
- Default portable profile backup and restore after a clean install/data reset, including Android Downloads/MediaStore collision behavior.
- Onboarding/settings UI entry points for restoring the default backup.
- Meditation completion sound replacing beep/tone behavior with a calm gong.
- EPUB/Markdown reading-time estimates no longer capped at 20 minutes while short session/link behavior remains bounded.
- Visual evidence and package hygiene.

KNOWN PRIOR BUG CLASSES TO ACTIVELY TEST AGAINST:
- Restore path silently reads a stale `quality-alternative-profile.json` while newer Android-collision files such as `quality-alternative-profile (1).json` exist.
- Fresh install/onboarding has no obvious restore path.
- Default backup works only in tests but not through the real Android app flow.
- Meditation uses `ToneGenerator` beep instead of a gong-like completion sound.
- Long EPUB/Markdown imports are mislabeled as 20 minutes.
- Link/session estimates accidentally become multi-hour because document timing was broadened too widely.
- Visual evidence is stale, duplicated, or not tied to the changed UI.

CHECKS REQUIRED:
1. Trace the implementation from app container default path through writer/reader, ViewModel restore, onboarding/settings buttons, and tests.
2. Verify that the Android MediaStore collision fix is plausible and covered by connected evidence.
3. Verify that reading-time changes affect extracted document text and portable document duration ranges, without expanding user-link validation unexpectedly.
4. Verify that meditation completion no longer uses the old beep/tone path.
5. Review screenshots for obvious layout/text problems and confirm they demonstrate the changed flows.
6. Review package hygiene: no obsolete failed screenshots or misleading stale bundle noise should be treated as current evidence.

OUTPUT FORMAT, EXACTLY:
SCORE: x/10
VERDICT: PASS / REVISE / BLOCK
VISUAL REVIEW: PASS / REVISE / BLOCK
BLOCKERS:
- bullet list or `None`
PROFILE RESTORE:
- concise assessment
MEDITATION GONG:
- concise assessment
READING TIME:
- concise assessment
TEST/EVIDENCE:
- concise assessment
BUNDLE GAPS:
- bullet list or `None`
PACKAGE HYGIENE:
- concise assessment
