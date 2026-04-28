# Quality Alternative v0.6.1-intervention-progress-alpha

Internal Android alpha for GitHub testers.

This release ships the GPT Pro-reviewed intervention continuation metadata slice on top of `v0.6.0-content-management-alpha`.

## What changed since v0.6.0-content-management-alpha

The previous release added content management, batch import, auto reading-time estimates, priority-at-add, saved progress, continue reading, and unfinished-first recommendations.

This release tightens the intervention itself:

- Continuation recommendations now show progress directly in the intervention, for example `42% read · 5 min left`.
- The primary recommendation card shows percent already read and remaining time when it is continuing unfinished content.
- Other options show the same continuation metadata for unfinished backup choices.
- The remaining-time label intentionally avoids a tilde; the UI uses `5 min left`, not `~5 min left`.
- Other options are now a bounded scrollable list instead of being capped at two visible choices.
- Recommendation selection can return up to six finite backup options while preserving the one-primary recommendation model.
- Backup rows expose the continuation label through accessibility semantics for stronger Android E2E assertions.
- `PRD.md` now matches the bounded backup-list behavior and the continuation progress metadata requirement.

## Validation

- GPT Pro R3 visual/code review: `SCORE: 10/10`, `VERDICT: PASS`.
- Unit/compile validation: `testDebugUnitTest compileDebugAndroidTestKotlin` passed.
- Targeted Android E2E: `MainActivityTest#systemInterventionShowsContinueProgressRemainingTimeAndScrollableOtherOptions` passed.
- Full connected Android suite: 65 tests, 0 failures, 0 errors, 0 skipped.
- Recommendation engine unit suite: 14 tests, 0 failures, 0 errors, 0 skipped.
- Visual evidence: light primary intervention, scrolled Other options, and dark mode screenshots were inspected by GPT Pro and passed.
- Release APK install smoke: `adb install -r` passed on `qaApi36`.

## APK Assets

- Installable alpha APK: `quality-alternative-v0.6.1-intervention-progress-alpha-debug.apk`
- APK versionCode: 8
- APK versionName: `0.6.1-alpha`

## Evidence

- Final GPT Pro audit: `docs/visual-qa/2026-04-28-intervention-continue-progress/test-evidence/gpt_pro_r3_10_10_pass.md`
- Visual screenshots: `docs/visual-qa/2026-04-28-intervention-continue-progress/screenshots/`
- Android connected test XML: `docs/visual-qa/2026-04-28-intervention-continue-progress/test-evidence/connected-debug-TEST-qaApi36.xml`
- Recommendation engine test XML: `docs/visual-qa/2026-04-28-intervention-continue-progress/test-evidence/unit-DefaultRecommendationEngineTest.xml`
- Release build validation: `docs/visual-qa/2026-04-28-intervention-continue-progress/test-evidence/release_build_validation_20260428.md`
