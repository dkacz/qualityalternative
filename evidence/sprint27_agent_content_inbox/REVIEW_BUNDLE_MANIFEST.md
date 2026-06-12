# Sprint 27 Agent Content Inbox Review Bundle Manifest

Primary document: `PRD.md`

Review scope:
- PRD alignment for FR3B Agent Inbox, FR13 Portable Profile, analytics, and privacy.
- Google Drive folder/package scan semantics and `drive.file`-style user-controlled access.
- Manifest validation and operator-confirmed priority import.
- Private Markdown/EPUB import through existing user-document model.
- Analytics metadata and Portable Profile exclusion of raw Drive ids, file names, tokens, and document text.
- Settings UI boundedness and visual e2e evidence.
- R3 blocker recheck: content-addressed import storage, actual-content duplicate detection, bounded Drive downloads, reject action/analytics, first-connect folder creation, and release-safe fixture gating.
- R4 blocker recheck: package-level unavailable handling for non-size Drive download failures, user-document readiness/authoritative duplicate lookup, readable operator priority confirmation screenshots, and connected logcat evidence.
- R5 blocker recheck: atomic same-fingerprint Agent Inbox import, same-scan same-SHA duplicate state, import-time duplicate state, and ViewModel single-flight guard.
- R6 blocker recheck: import-time invalid/rejected/download-failure state must become finite non-importable invalid review rows with stale reviewed fingerprints and accepted priority cleared.
- R7 finding recheck: post-write duplicate/rejected/exception import results must delete the just-written Agent Inbox private file so no invisible orphaned local bytes remain.
- R8 finding recheck: Agent Inbox document storage must not write directly into the deterministic final path before verifying bytes; stale or partial final files must not block a later valid import or leave unverified bytes behind.
- R9 visual finding recheck: screenshots `06` through `09` must prove actual Agent Inbox scan/import-to-consumption rendering, not generic user-document seeding.

Included files:
- `PRD.md`
- `docs/SPRINT_27_AGENT_CONTENT_INBOX.md`
- `evidence/sprint27_agent_content_inbox/VALIDATION_SUMMARY.md`
- `evidence/sprint27_agent_content_inbox/sprint27_tracked_diff.patch`
- `evidence/sprint27_agent_content_inbox/git_status_short.txt`
- `evidence/sprint27_agent_content_inbox/pro_review_harvest/GPT_PRO_REVIEW_R1.md`
- `evidence/sprint27_agent_content_inbox/pro_review_harvest/GPT_PRO_REVIEW_R2.md`
- `evidence/sprint27_agent_content_inbox/pro_review_harvest/GPT_PRO_REVIEW_R3.md`
- `evidence/sprint27_agent_content_inbox/pro_review_harvest/GPT_PRO_REVIEW_R4.md`
- `evidence/sprint27_agent_content_inbox/pro_review_harvest/GPT_PRO_REVIEW_R5.md`
- `evidence/sprint27_agent_content_inbox/pro_review_harvest/GPT_PRO_REVIEW_R6.md`
- `evidence/sprint27_agent_content_inbox/pro_review_harvest/GPT_PRO_REVIEW_R7.md`
- `evidence/sprint27_agent_content_inbox/pro_review_harvest/GPT_PRO_REVIEW_R8.md`
- `evidence/sprint27_agent_content_inbox/pro_review_harvest/GPT_PRO_REVIEW_R9.md`
- Agent Inbox production source files under `app/src/main/java/com/qualityalternative/app/data/`, `domain/service/`, `domain/model/`, and `ui/`
- `app/src/main/java/com/qualityalternative/app/domain/service/ReadingAnnotationDriveSync.kt` for the exact `drive.file` scope constant reused by Agent Inbox authorization.
- `app/src/main/java/com/qualityalternative/app/interception/FixtureTargetRegistry.kt`
- `app/src/main/java/com/qualityalternative/app/interception/InterceptionTargetResolver.kt`
- `app/src/main/AndroidManifest.xml`
- `app/src/debug/AndroidManifest.xml`
- Agent Inbox unit tests and updated privacy/settings/ViewModel tests under `app/src/test/java/...`
- `app/src/androidTest/java/com/qualityalternative/app/VisualQaScreenshotTest.kt`
- Final visual screenshots/contact sheet under `evidence/sprint27_agent_content_inbox/visual_e2e/`
- Connected test XML/logcat under `evidence/sprint27_agent_content_inbox/android-results/connected_debug/`
- Unit test XML results under `evidence/sprint27_agent_content_inbox/unit-results/testDebugUnitTest/`
- Lint report under `evidence/sprint27_agent_content_inbox/lint/`
- Release/debug manifest evidence under `evidence/sprint27_agent_content_inbox/manifests/`
- Debug APK candidate was included in the local GPT Pro review bundle; committed evidence keeps the checksum and manifest check under `evidence/sprint27_agent_content_inbox/apk/`

Excluded:
- Prior sprint evidence directories, stale review bundles, old APKs, and unrelated release logs.
- Full Gradle build directories except the copied result, manifest, lint, and APK files listed above.
- Live Google OAuth credentials, tokens, browser profiles, and local Android emulator state.

Bundle hygiene expectation:
- Treat `sprint27-agent-content-inbox-1781272063934` as the only canonical screenshot run.
- Treat `connected_debug/TEST-qaApi36(AVD) - 16-_app-.xml` as the canonical connected test result.
- Treat `connected_debug/logcat-VisualQaScreenshotTest-captureSprint27AgentInboxReviewScreens.txt` as the standalone connected logcat evidence copy; the original nested logcat is also present under the device-specific connected output directory.
- Treat the locally generated R10 review APK with SHA-256 `9a51ec2a435c8cb8e8a0cdaa8e74212551127a04e57dda08d81115f59d3bf4e8` as review-only; committed evidence keeps the checksum/manifest check, and the final version-bumped APK is published from `release_artifacts/`.
