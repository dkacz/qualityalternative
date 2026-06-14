# Lane Status

Status timestamp: 2026-06-14

This file is the repo-level index for active and recently completed execution lanes. It should point to the canonical branch, review lane, validation artifacts, and next gate for each lane.

## Current Rule

- Use this document for cross-lane status.
- Use the branch-specific sprint docs for detailed implementation notes.
- Heartbeats should exist only while waiting for a current GPT Pro lane.
- Do not infer lane status from untracked local files alone. For example, `ios/` can appear untracked on non-iOS branches; the canonical iOS implementation source is the pushed iOS branch listed below.

## Sprint 28 Agent Inbox Drive Access Fix

Status: `in_progress`

- Branch: `codex/sprint28-agent-inbox-drive-access`
- Scope: fix the post-release Agent Inbox Drive access gap for packages uploaded later by rclone/external agents under the current `drive.file` model, and close the added Markdown image attachment gap for manual imports plus Agent Inbox Markdown packages.
- Canonical sprint plan: `docs/SPRINT_28_AGENT_INBOX_DRIVE_ACCESS.md`
- Current implementation state:
  - Sprint opened from Sprint 27 release branch after recording the `drive.file`/rclone diagnosis.
  - Default decision is Picker-first: keep `drive.file`, require explicit Google Picker folder selection for Agent Inbox, and only consider `drive.readonly` if a selected folder grant does not expose later-added package children.
  - Current validation/evidence summary: `evidence/sprint28_agent_inbox_drive_access/VALIDATION_SUMMARY.md`
  - Live rclone/Picker spike checklist: `evidence/sprint28_agent_inbox_drive_access/device_spike/RCLONE_PICKER_FOLDER_SPIKE.md`
  - `play-services-auth` is bumped to `21.6.0` because Google Play services release notes state this version adds `PICKER_ALLOW_FOLDER_SELECTION`.
  - App-side Picker-folder authorization is implemented with `PICKER_OAUTH_TRIGGER=true`, `PICKER_ALLOW_FOLDER_SELECTION=true`, consent prompt, and opt-out from previously granted scopes.
  - Agent Inbox scan now requires a selected folder id and no longer silently creates a separate app-owned inbox folder.
  - Selected-folder Drive 401/403/404 scan failures are treated as access-lost states that clear the local folder grant, show `Select folder`, and record privacy-safe failure analytics.
  - GPT Pro R1 returned `SCORE 7/10`, `VERDICT BLOCK`, `VISUAL REVIEW REVISE`; the blockers were legacy Sprint 27 app-created folder ids bypassing Picker and a connected-without-folder Settings state.
  - R1 fixes are implemented locally: Agent Inbox connection now requires durable `agent_inbox_drive_grant_mode=picker_folder`, legacy folder ids without that marker hydrate as disconnected, `saveAgentInboxDriveConnection(null)` clears connection state, and Settings copy/actions derive connected state from the Picker grant predicate.
  - Manual Markdown image attachment imports now allow image-only follow-up picker results to merge into the already selected Markdown file while preserving edited title, selected topics, and priority.
  - Agent Inbox Markdown packages can carry bounded safe sidecar images through review, Drive download, local Agent Inbox document storage, `UserDocumentDraft.imageAttachmentUris`, and reader rendering; EPUB package sidecars remain invalid.
  - Visual E2E coverage for disconnected select-folder, selected-folder, access-lost reconnect, Agent Inbox Markdown image reader, and dark selected-folder states passed in `VisualQaScreenshotTest#captureSprint28AgentInboxDriveAccessStates`.
  - Canonical Sprint 28 visual evidence: `evidence/sprint28_agent_inbox_drive_access/visual_e2e/contact_sheet_r2.png` and `evidence/sprint28_agent_inbox_drive_access/visual_e2e/sprint28-agent-inbox-drive-access-1781433607325/`.
- Validation:
  - Passed: full `testDebugUnitTest`.
  - Passed: targeted Markdown image/Agent Inbox tests for `DocumentImportCandidateFactoryTest`, `MainViewModelTest`, `AgentInboxReviewCandidateFactoryTest`, and `AgentInboxPackageImporterTest`.
  - Passed: targeted `testDebugUnitTest` for `GoogleDriveAuthorizationTest`, `MainViewModelTest`, and `AndroidGoogleDriveAgentInboxClientTest`.
  - Passed after R1 fixes: targeted rerun for `PreferencesSettingsRepositoryTest` and `MainViewModelTest`.
  - Passed: `compileDebugAndroidTestKotlin`.
  - Passed: focused connected visual E2E on `qaApi36(AVD) - 16` for `VisualQaScreenshotTest#captureSprint28AgentInboxDriveAccessStates`.
  - Passed: `lintDebug`, `processReleaseManifestForPackage`, and `assembleDebug`.
  - Passed: `git diff --check`.
- Next gate:
  - GPT Pro R2 returned `SCORE 7/10`, `VERDICT REVISE`, `VISUAL REVIEW PASS`; fix the three Markdown image sidecar findings, rerun validation, and send R3 before any release gate.

## Sprint 27 Agent Content Inbox

Status: `release_published`

- Branch: `codex/sprint-agent-content-inbox`
- Scope: add a Google Drive-backed Agent Inbox so Codex/Claude-style agents can hand private Markdown or EPUB content to the app through a bounded package contract, with manifest priority requiring explicit user confirmation.
- Canonical sprint plan: `docs/SPRINT_27_AGENT_CONTENT_INBOX.md`
- Current implementation state:
  - Drive Agent Inbox scanning, review, import, duplicate detection, disconnect, settings UI, analytics, and Portable Profile metadata-only export/import are implemented locally.
  - Manifest priority is shown during review and is opt-in before it affects ranking.
  - Import accepts one reviewed private Markdown or EPUB content file through the existing document model; raw Drive content file names and raw document SHA values are not exported in Portable Profile.
  - Visual E2E covers Settings review, manifest priority opt-in, duplicate/rejected packages, imported reader content, EPUB rendering, and Portable Profile privacy copy.
  - R1 GPT Pro review returned `SCORE 6/10`, `VERDICT BLOCK`, `VISUAL REVIEW REVISE`; blockers were priority auto-acceptance, unbounded scan/review, missing disconnect UI, raw SHA export, weak duplicate/SHA visibility, and incomplete visual evidence.
  - R1 blockers were fixed with explicit priority opt-in, bounded scan and package review, disconnect UI, metadata-only Portable Profile export, duplicate/SHA mismatch review states, and expanded visual evidence.
  - R2 GPT Pro review returned `SCORE 6/10`, `VERDICT BLOCK`, `VISUAL REVIEW PASS`; blockers were missing cryptographic binding from reviewed content to import, raw Drive content file names leaking through `sourceLabel`, loose package cardinality, and unbounded manifest download.
  - R2 blockers were fixed with reviewed content SHA/size binding at import, neutral imported document source label, strict one manifest plus one content file contract, unsupported extra-file rejection, and a 64 KiB manifest cap before and after metadata download.
  - Validation passed after R2 fixes: `testDebugUnitTest`, `compileDebugAndroidTestKotlin`, and connected `VisualQaScreenshotTest#captureSprint27AgentInboxReviewScreens`.
  - R3 GPT Pro review returned `SCORE 6/10`, `VERDICT BLOCK`, `VISUAL REVIEW PASS`; blockers were same folder/name changed-byte overwrite risk, duplicate review from unverified manifest SHA, unbounded network reads when Drive metadata is missing/wrong, missing reject/remove path and rejected analytics, unqualified first-connect folder name search, and production fixture resolution/manifest risk.
  - R3 blockers were fixed with verified-SHA content-addressed Agent Inbox storage, duplicate status only from actual reviewed content SHA, bounded Drive client downloads with typed too-large handling, visible Remove action plus `AGENT_INBOX_CANDIDATE_REJECTED`, first-connect folder creation with persisted id, and BuildConfig/debug-manifest fixture gating.
  - R4 validation passed: 498 debug unit testcases, `compileDebugAndroidTestKotlin`, `lintDebug`, `processReleaseManifestForPackage`, `assembleDebug`, `git diff --check`, and connected `VisualQaScreenshotTest#captureSprint27AgentInboxReviewScreens`.
  - R4 GPT Pro review returned `SCORE 7/10`, `VERDICT REVISE`, `VISUAL REVIEW REVISE`; findings were non-size Drive download failures collapsing a scan, duplicate safety depending on an unhydrated in-memory document list, clipped priority-control labels in screenshots, and a connected-logcat bundle gap.
  - R5 fixes the R4 findings with package-level `DOWNLOAD_UNAVAILABLE`, a user-document readiness gate, repository/DAO duplicate lookup by verified fingerprint, full-width priority confirmation UI plus screenshot assertions, a fresh canonical visual run, and standalone connected logcat evidence.
  - R5 validation passed: 502 debug unit testcases, `compileDebugAndroidTestKotlin`, `lintDebug`, `processReleaseManifestForPackage`, `assembleDebug`, connected `VisualQaScreenshotTest#captureSprint27AgentInboxReviewScreens`, and `git diff --check`.
  - R5 GPT Pro review returned `SCORE 8/10`, `VERDICT BLOCK`, `VISUAL REVIEW PASS`; the remaining blocker was non-atomic same-fingerprint duplicate prevention plus duplicate import results leaving candidates visually ready.
  - R6 fixes the R5 blocker with `addDocumentIfFingerprintAbsent`, Room repository write mutex serialization, verified fingerprint fields on `UserDocumentDraft`, ViewModel import single-flight, duplicate-state updates after import-time duplicates, and same-scan same-SHA sibling duplicate marking after first import.
  - R6 validation passed: 505 debug unit testcases, `compileDebugAndroidTestKotlin`, `lintDebug`, `processReleaseManifestForPackage`, `assembleDebug`, connected `VisualQaScreenshotTest#captureSprint27AgentInboxReviewScreens`, and `git diff --check`.
  - R6 GPT Pro review returned `SCORE 8/10`, `VERDICT BLOCK`, `VISUAL REVIEW PASS`; the remaining blocker was import-time invalid/rejected/download-failure paths leaving rows visually READY with stale reviewed fingerprints.
  - R7 fixes the R6 blocker by converting import-time invalid/rejected/download-failure results into finite non-importable invalid candidates, clearing stale reviewed fingerprint/size, clearing accepted priority, adding `LOCAL_IMPORT_REJECTED` copy, and covering changed-after-review, oversize, download failure, and repository rejection with ViewModel tests.
  - R7 validation passed: 508 debug unit testcases, `compileDebugAndroidTestKotlin`, `lintDebug`, `processReleaseManifestForPackage`, `assembleDebug`, connected `VisualQaScreenshotTest#captureSprint27AgentInboxReviewScreens`, and `git diff --check`.
  - R7 GPT Pro review returned `SCORE 9/10`, `VERDICT REVISE`, `VISUAL REVIEW PASS`; the remaining finding was invisible local retention of private Agent Inbox files after post-write duplicate/rejected results.
  - R8 fixes the R7 finding with `AgentInboxDocumentStore.deleteDocument`, guarded File store deletion under the Agent Inbox root, importer cleanup on duplicate/rejected/exception after write, and tests for concurrent duplicate cleanup, repository rejection cleanup, and atomic-add exception cleanup.
  - R8 validation passed: 509 debug unit testcases, `compileDebugAndroidTestKotlin`, `lintDebug`, `processReleaseManifestForPackage`, `assembleDebug`, connected `VisualQaScreenshotTest#captureSprint27AgentInboxReviewScreens`, and `git diff --check`.
  - R8 GPT Pro review returned `SCORE 9/10`, `VERDICT REVISE`, `VISUAL REVIEW PASS`; the remaining finding was direct final-path writes in `FileAgentInboxDocumentStore.writeDocument`, which could leave a stale partial/mismatching final file after a write failure and block later valid import.
  - R9 fixes the R8 finding with scoped temp-file writes, SHA-256 verification before final promotion, atomic move with filesystem fallback, stale mismatching final-file replacement, temp cleanup in `finally`, and a regression test for stale-final replacement.
  - R9 validation passed: 510 debug unit testcases, `compileDebugAndroidTestKotlin`, `lintDebug`, `processReleaseManifestForPackage`, `assembleDebug`, connected `VisualQaScreenshotTest#captureSprint27AgentInboxReviewScreens`, and `git diff --check`.
  - R9 GPT Pro review returned `SCORE 9/10`, `VERDICT REVISE`, `VISUAL REVIEW REVISE`; the remaining finding was that screenshots `06`-`09` were generic user-document seed smoke evidence instead of proof that actual Agent Inbox import renders correctly in Library, intervention, Markdown reader, and EPUB reader.
  - R10 fixes the R9 visual finding by generating imported-content screenshots after `scanAgentInboxDrive` and `importAgentInboxCandidate` against a debug-gated fake Drive client, accepting Markdown priority before import, asserting verified fingerprint and neutral `Agent Inbox document` provenance, and asserting raw Drive content file names/package ids are not visible.
  - R10 validation passed: 510 debug unit testcases, `compileDebugAndroidTestKotlin`, `lintDebug`, `processReleaseManifestForPackage`, `assembleDebug`, connected `VisualQaScreenshotTest#captureSprint27AgentInboxReviewScreens`, and `git diff --check`.
  - R10 GPT Pro review returned `SCORE 10/10`, `VERDICT PASS`, `VISUAL REVIEW PASS`; no fresh findings, no bundle gaps, and package hygiene clean enough for release-gate audit.
- Current evidence:
  - Validation summary: `evidence/sprint27_agent_content_inbox/VALIDATION_SUMMARY.md`
  - Review bundle manifest: `evidence/sprint27_agent_content_inbox/REVIEW_BUNDLE_MANIFEST.md`
  - Visual contact sheet: `evidence/sprint27_agent_content_inbox/visual_e2e/sprint27_agent_inbox_contact_sheet.png`
  - Canonical screenshot run: `evidence/sprint27_agent_content_inbox/visual_e2e/sprint27-agent-content-inbox-1781272063934/`
  - GPT Pro R1 output: `evidence/sprint27_agent_content_inbox/pro_review_harvest/GPT_PRO_REVIEW_R1.md`
  - GPT Pro R2 output: `evidence/sprint27_agent_content_inbox/pro_review_harvest/GPT_PRO_REVIEW_R2.md`
  - GPT Pro R3 output: `evidence/sprint27_agent_content_inbox/pro_review_harvest/GPT_PRO_REVIEW_R3.md`
  - GPT Pro R4 lane: `https://chatgpt.com/c/6a2bdd0d-b5e8-83eb-972d-813bd00130f7`
  - GPT Pro R4 output: `evidence/sprint27_agent_content_inbox/pro_review_harvest/GPT_PRO_REVIEW_R4.md`
  - GPT Pro R5 lane: `https://chatgpt.com/c/6a2be5be-1d60-83eb-b0ba-01b633e2bcd1`
  - GPT Pro R5 output: `evidence/sprint27_agent_content_inbox/pro_review_harvest/GPT_PRO_REVIEW_R5.md`
  - GPT Pro R6 lane: `https://chatgpt.com/c/6a2bed8f-7c34-83ed-a148-9e749cf8a099`
  - GPT Pro R6 output: `evidence/sprint27_agent_content_inbox/pro_review_harvest/GPT_PRO_REVIEW_R6.md`
  - GPT Pro R7 lane: `https://chatgpt.com/c/6a2bf46d-43bc-83eb-8125-c1002b3ea3dd`
  - GPT Pro R7 output: `evidence/sprint27_agent_content_inbox/pro_review_harvest/GPT_PRO_REVIEW_R7.md`
  - GPT Pro R8 lane: `https://chatgpt.com/c/6a2bfb46-2550-83eb-a06a-889265bc2c57`
  - GPT Pro R8 output: `evidence/sprint27_agent_content_inbox/pro_review_harvest/GPT_PRO_REVIEW_R8.md`
  - GPT Pro R9 lane: `https://chatgpt.com/c/6a2c059d-9764-83ed-b808-4538ad6a3160`
  - GPT Pro R9 output: `evidence/sprint27_agent_content_inbox/pro_review_harvest/GPT_PRO_REVIEW_R9.md`
  - GPT Pro R10 lane: `https://chatgpt.com/c/6a2c0f4a-b3e0-83eb-97a2-4762aeb641b2`
  - GPT Pro R10 output: `evidence/sprint27_agent_content_inbox/pro_review_harvest/GPT_PRO_REVIEW_R10.md`
  - GPT Pro R10 bundle manifest: `evidence/sprint27_agent_content_inbox/REVIEW_BUNDLE_MANIFEST.md`
  - Final release gate passed after Android version bump to `versionCode=31`, `versionName=0.11.15-alpha`.
  - Final Gradle gate passed: `:app:testDebugUnitTest :app:lintDebug :app:assembleDebug`.
  - Final full connected Android gate passed on R2: 137 tests, 0 failures, 0 skipped.
  - APK badging/signature/install/launch evidence passed.
  - Release artifact: `release_artifacts/quality-alternative-v0.11.15-agent-content-inbox-alpha-debug.apk`
  - Release APK SHA-256: `10f2d54f7dc06c561afa32a83bcc7c5790c211f17cd320d469d93e6c957278f6`
  - Release gate summary: `docs/release-gate-logs/2026-06-12-sprint27-agent-content-inbox/VALIDATION_SUMMARY.md`
  - Release notes: `docs/release-gate-logs/2026-06-12-sprint27-agent-content-inbox/RELEASE_NOTES_v0.11.15-agent-content-inbox-alpha.md`
  - Release commit: `b173e3c` (`Complete Sprint 27 agent content inbox`).
  - Release tag: `v0.11.15-agent-content-inbox-alpha`.
  - Release URL: `https://github.com/dkacz/qualityalternative/releases/tag/v0.11.15-agent-content-inbox-alpha`.
  - Published assets: `quality-alternative-v0.11.15-agent-content-inbox-alpha-debug.apk` and `quality-alternative-v0.11.15-agent-content-inbox-alpha-debug.apk.sha256`.
  - Integration method: committed on `codex/sprint-agent-content-inbox`, tagged the release commit, pushed branch and tag to `origin`, and published the GitHub release from the committed release notes.
- Post-release diagnosis on 2026-06-14:
  - Agent Inbox packages uploaded by an external rclone daemon are not visible to the Android app under the current `drive.file` OAuth scope unless the app created those files or the user explicitly grants access through a Drive selection flow.
  - This differs from the user's Boox/rclone annotation pipeline, which runs outside the app with broader Drive access, and from the app's internal annotation sync, where the app creates/uploads the files itself and can later see them.
  - The observed app state `connected, no packages found` with externally uploaded packages is therefore expected under `drive.file`; the blocker is file authorization/ownership, not upload method.
  - Follow-up implementation decision: either request broader `drive.readonly` for Agent Inbox, or keep `drive.file` and add a Google Picker folder/file grant flow. The Picker route still needs validation for whether files added later to the granted folder remain visible.
- Next gate:
  - Choose the post-release Agent Inbox Drive access strategy before implementing the rclone-uploaded package fix.

## Sprint 26 Custom Targets And Website Interventions

Status: `release_published`

- Branch: `codex/sprint26-custom-targets-website-interventions`
- Scope: plan and implement support for replacement-first interventions on eligible arbitrary installed apps plus supported-browser website/domain rules, while avoiding universal URL blocking claims.
- Canonical sprint plan: `docs/SPRINT_26_CUSTOM_TARGETS_WEBSITE_INTERVENTIONS.md`
- GPT Pro plan review R1 lane: `https://chatgpt.com/c/6a25282f-e0b8-83ed-bc77-15b0fef88cad`
- GPT Pro plan review R1 output: `evidence/sprint26_custom_targets_website_interventions/GPT_PRO_PLAN_REVIEW.md`
- GPT Pro plan review R1 verdict: `SCORE 6/10`, `VERDICT FAIL`, `VISUAL REVIEW NOT APPLICABLE`
- GPT Pro plan review R2 lane: `https://chatgpt.com/c/6a252ba7-7c48-83eb-a9b5-a95bd1d499dd`
- GPT Pro plan review R2 output: `evidence/sprint26_custom_targets_website_interventions/GPT_PRO_PLAN_REVIEW_R2.md`
- GPT Pro plan review R2 verdict: `SCORE 10/10`, `VERDICT PASS`, `VISUAL REVIEW NOT APPLICABLE`
- Heartbeat automation: deleted after successful R2 harvest.
- Current plan review bundle: `evidence/sprint26_custom_targets_website_interventions/SPRINT26_PLAN_REVIEW_R2_BUNDLE_20260607.zip`
- Current plan review prompt: `evidence/sprint26_custom_targets_website_interventions/GPT_PRO_PLAN_REVIEW_PROMPT_R2.md`
- Current implementation state:
  - Slice 26.0 contract/plan gate is complete and committed.
  - Slice 26.1 custom app target vertical is implemented locally.
  - Android now enumerates launchable installed apps for custom target candidates and exposes unsafe packages as disabled rows with reasons.
  - R2 fixed the GPT Pro R1 blockers: default mode is Soft, DocumentsUI/file-picker packages are excluded, eligibility policy has direct unit coverage, and the review bundle includes Portable Profile, MainActivity, analytics, Gradle, raw logs, and expanded visual evidence.
  - R3 fixed the GPT Pro R2 blockers: the bundle included all `app/src`, Portable Profile all-missing app-target replace import stayed empty instead of selecting defaults, fresh `--rerun-tasks` unit reports included XML/test counts, and custom installed-app intervention was covered through `MainActivity.createSystemInterceptionIntent()`.
  - R4 fixes the GPT Pro R3 blockers: the review bundle now includes complete Gradle wrapper files and `app/proguard-rules.pro`; Settings can rebuild from an empty completed target set by allowing additions below the minimum while still blocking below-minimum removals; and the OEM safety boundary has direct regression coverage.
  - Settings separates standard suggestions from custom installed-app search/selection.
  - Selected eligible custom app packages hydrate into settings, Portable Profile import/export, and the AccessibilityService resolver's known target list.
  - Slice 26.2 implements the website rule model and Settings UI for exact-domain and wildcard-subdomain rules.
  - Website rules are normalized, reject local/private/public-IP/IPv6/all-numeric/ambiguous hosts, persist in DataStore, and round-trip through Portable Profile import/export without browser support state, URL observations, tokens, or local folders.
  - Settings exposes website rules as a separate target category with add, validation, pause, edit, delete, and a browser support matrix. Chrome current-host interception remains scoped to Slice 26.3.
  - Slice 26.2 R2 fixes the GPT Pro R1 blockers: all IP literals are rejected; typed `*.example.com` exposes the subdomain mode and apex toggle before save; apex inclusion defaults off; the website rule count says `enabled`; the review bundle includes `gradle/libs.versions.toml`; and Android test metadata is sanitized to remove absolute local paths.
  - Slice 26.3 implements Chrome-first verified-host website intervention through whitelisted address-bar accessibility nodes only.
  - Slice 26.3 keeps unsupported/unreadable browser states non-triggering, never reuses a prior host, and routes matching rules into the existing Soft/Firm intervention flow as `Chrome website`.
  - Slice 26.3 uses privacy-safe analytics metadata (`targetType`, browser support status, rule type) without raw URL, host/domain, path/query, page title, URL-bar text, non-match observations, browsing history, or domain-derived hashes.
  - Slice 26.3 R2 fixes the GPT Pro R1 blockers: Settings copy now marks Chrome domain rules as supported when readable; hidden/focused omnibox states are rejected; adapter depth covers the real Chrome toolbar; website Open Anyway suppression falls through to whole-browser Chrome app target evaluation; and real Chrome package/version plus URL-set evidence is recorded.
  - Slice 26.3 R3 fixes the GPT Pro R2 blocker by removing raw `externalUrl` from shared content analytics metadata and adding a website-domain regression that proves a replacement link URL with host, path, and query does not appear in website intervention, accept, or fallback-open analytics metadata.
  - Slice 26.3 R4 is a package/evidence completeness review after R3 returned PASS/PASS at 9/10; it adds full unit XML, standalone activity/analytics/repository/model source files, and a cleaned manifest for GPT Pro to re-score.
  - Slice 26.3 R5 ships all `app/src` source/test files plus full unit/lint evidence to close the R4 package-completeness gap.
  - Slice 26.3 R5 review returned `SCORE 9/10`, `VERDICT PASS`, `VISUAL REVIEW PASS`, with no release blockers but with evidence gaps for fresh connected evidence, live service E2E, negative unsupported/unreadable proof, device/API metadata, schemed Unicode URL coverage, and raw `git diff --check` output.
  - Slice 26.3 R6 closes those gaps with fresh connected 4/4 evidence, live Chrome AccessibilityService-to-intervention proof, connected unsupported/unreadable negative evidence, device/API/Chrome version metadata, schemed Unicode host normalization coverage, and raw diff-check output.
  - Slice 26.3 R6 review returned `SCORE 9/10`, `VERDICT FAIL`, `VISUAL REVIEW PASS`; GPT Pro's blocker was missing package-authenticated stale active-window/package-mismatch safety.
  - Slice 26.3 R7 adds package identity to browser snapshots, requires the root and address-bar node package to match `com.android.chrome`, and reruns unit, connected, visual, and live-service evidence after the change.
  - Slice 26.3 R7 review returned `SCORE 10/10`, `VERDICT PASS`, `VISUAL REVIEW PASS`; no blockers or release-blocking bundle gaps remain.
  - Slice 26.3 was committed as `8fdd20e` (`Complete Sprint 26 Chrome website interventions`).
  - Slice 26.4 is in progress: adding remote analytics privacy guards, log-scrub checks, and Portable Profile import/export hardening for custom app and website rules.
  - Slice 26.4 implementation/evidence was sent to GPT Pro at `https://chatgpt.com/c/6a25ae84-1310-83eb-a53a-1128d4a7edd1`; harvest heartbeat `harvest-sprint-26-slice26-4-gpt-pro-review` writes to `evidence/sprint26_custom_targets_website_interventions/GPT_PRO_SLICE26_4_REVIEW.md`.
  - Slice 26.4 heartbeat check at `2026-06-07T17:57:22Z`: exact-lane harvest found GPT Pro still thinking, so the heartbeat remains active and no review output has been harvested yet.
  - Slice 26.4 R1 review returned `SCORE 8/10`, `VERDICT FAIL`, `VISUAL REVIEW NOT APPLICABLE`; blockers were unknown `targetType` echoing to `targetClass`, missing host/IP/IDNA sanitizer cases, and metadata-only unsafe-field diagnostics.
  - Slice 26.4 R1 heartbeat was deleted after successful harvest.
  - Slice 26.4 R2 fixes the R1 blockers with fixed target-class mapping, known-only `targetType` metadata, expanded IP/host/IDNA/package/URL filtering, top-level unsafe-field diagnostics, and a remote-safe debug summary path.
  - Slice 26.4 R2 implementation/evidence was sent to GPT Pro at `https://chatgpt.com/c/6a25b565-7d28-83ed-bc8a-de6a19da9613`; harvest heartbeat `harvest-sprint-26-slice26-4-r2-gpt-pro-review` writes to `evidence/sprint26_custom_targets_website_interventions/GPT_PRO_SLICE26_4_R2_REVIEW.md`.
  - Slice 26.4 R2 heartbeat check at `2026-06-07T18:26:22Z`: exact-lane harvest found GPT Pro still thinking, so the heartbeat remains active and no R2 review output has been harvested yet.
  - Slice 26.4 R2 review returned `SCORE 9/10`, `VERDICT FAIL`, `VISUAL REVIEW NOT APPLICABLE`; remaining blocker was punycode/IDNA host-like values with punycode TLDs and trailing-dot IPv4 literals.
  - Slice 26.4 R2 heartbeat was deleted after successful harvest.
  - Slice 26.4 R3 fixes the R2 blocker by canonicalizing host candidates before IP checks and rejecting DNS-style multi-label values including punycode labels/TLDs, plus regression coverage through payload conversion, scrubber, and top-level diagnostics.
  - Slice 26.4 R3 implementation/evidence was sent to GPT Pro at `https://chatgpt.com/c/6a25bb13-7590-83ed-bbf4-8c84ac527bc0`; harvest heartbeat `harvest-sprint-26-slice26-4-r3-gpt-pro-review` writes to `evidence/sprint26_custom_targets_website_interventions/GPT_PRO_SLICE26_4_R3_REVIEW.md`.
  - Slice 26.4 R3 heartbeat check at `2026-06-07T18:50:22Z`: exact-lane harvest found GPT Pro still thinking, so the heartbeat remains active and no R3 review output has been harvested yet.
  - Slice 26.4 R3 review returned `SCORE 10/10`, `VERDICT PASS`, `VISUAL REVIEW NOT APPLICABLE`; no blockers, bundle gaps, or package hygiene issues remain.
  - Slice 26.4 R3 heartbeat was deleted after successful harvest.
  - Slice 26.4 was committed as `2ce73f8` (`Harden Sprint 26 analytics privacy boundary`).
  - Slice 26.5 implements Bedtime/custom-target/website integration evidence. Production behavior was already shared through `triggerIntervention`; this slice adds targeted regression coverage and visual proof for supported Chrome website Bedtime.
  - Slice 26.5 unit coverage proves website Bedtime uses the 60-second emergency unlock, keeps meditation/alternatives, blocks `delayFor15Minutes()`, keeps website unlock scoped to the website suppression key, and avoids raw host/URL analytics metadata.
  - Slice 26.5 visual evidence proves Soft website, Firm website wait, and Bedtime `Chrome website` emergency-unlock states; the Bedtime screen hides `Pause 15 min`, keeps meditation/quiet alternatives, shows no raw domain, and shows the 60-second breath wait.
  - Slice 26.5 validation passed: 443 debug unit tests, `lintDebug` with 0 errors, 1/1 connected screenshot E2E on `qaApi36`, `git diff --check`, and emulator shutdown proof.
  - Slice 26.5 implementation/evidence was sent to GPT Pro at `https://chatgpt.com/c/6a25c47f-21d0-83eb-ace6-66cb604c351e`; harvest heartbeat `harvest-sprint-26-slice26-5-gpt-pro-review` writes to `evidence/sprint26_custom_targets_website_interventions/GPT_PRO_SLICE26_5_REVIEW.md`.
  - Slice 26.5 R1 review returned `SCORE 8/10`, `VERDICT FAIL`, `VISUAL REVIEW PASS`; blocker was bundle/evidence completeness for verified-host launch provenance and missing source files (`MainActivity.kt`, `AndroidManifest.xml`, `AnalyticsPrivacyGuard.kt`) plus missing prior Slice 26.1 R4 artifacts.
  - Slice 26.5 R1 heartbeat was deleted after successful harvest.
  - Slice 26.5 R2 fixes the blocker with `MainActivityTest#forgedWebsiteInterceptionIntentWithoutLaunchTokenIsIgnored`, a repeatable all-day Bedtime website seed for external live-service E2E, and an external shell harness proving real Chrome -> bound `QualityAlternativeAccessibilityService` -> `Bedtime is protecting sleep from Chrome website`.
  - Slice 26.5 R2 evidence path: `evidence/sprint26_custom_targets_website_interventions/SLICE26_5_EVIDENCE.md`.
  - Slice 26.5 R2 visual contact sheet: `evidence/sprint26_custom_targets_website_interventions/visual_e2e_slice26_5_r2/sprint26_slice26_5_r2_bedtime_website_live_contact_sheet.png`.
  - Slice 26.5 R2 live-service evidence: `evidence/sprint26_custom_targets_website_interventions/live_service_e2e_slice26_5_r2/`.
  - Slice 26.5 R2 validation passed: 443 debug unit tests, `lintDebug` with 0 errors, connected tests 2/2 on `qaApi36`, external live-service E2E PASS, and `git diff --check` PASS.
  - Slice 26.5 R2 clean review bundle was prepared for GPT Pro after removing inherited local-path leakage from the R1 review context. The exact active review URL is recorded locally after bundle export; harvest heartbeat `harvest-sprint-26-slice26-5-r2-gpt-pro-review` writes to `evidence/sprint26_custom_targets_website_interventions/GPT_PRO_SLICE26_5_R2_REVIEW.md`.
  - Slice 26.5 R2 active GPT Pro review lane: `https://chatgpt.com/c/6a25ce86-3f54-83eb-8547-940e2d94ecf8`.
  - Slice 26.5 R2 superseded pre-harvest lanes: `https://chatgpt.com/c/6a25cdcc-13a8-83ed-90e2-960e68535cab`, `https://chatgpt.com/c/6a25ce25-d3c0-83eb-8d1e-23288dd931ee`.
  - Slice 26.5 R2 heartbeat check at `2026-06-07T20:10:22Z`: exact-lane harvest found GPT Pro still thinking with the stop button visible; no `SCORE`/`VERDICT` output was harvested and heartbeat `harvest-sprint-26-slice26-5-r2-gpt-pro-review` remains active.
  - Slice 26.5 R2 heartbeat check at `2026-06-07T20:20:22Z`: exact-lane harvest and fresh-tab retry still found GPT Pro thinking with the stop button visible; no `SCORE`/`VERDICT` output was harvested and heartbeat `harvest-sprint-26-slice26-5-r2-gpt-pro-review` remains active.
  - Slice 26.5 R2 review returned `SCORE 10/10`, `VERDICT PASS`, `VISUAL REVIEW PASS`; no blockers, bundle gaps, privacy issues, or package hygiene issues remain.
  - Slice 26.5 R2 heartbeat was deleted after successful harvest.
  - Slice 26.5 was committed as `ee0d001` (`Complete Sprint 26 bedtime target integration`).
  - Slice 26.6 release gate uses `v0.11.14-custom-targets-website-interventions-alpha`, `versionCode=30`, `versionName=0.11.14-alpha`.
  - Slice 26.6 final validation passed after the R2 gate repair: `:app:testDebugUnitTest :app:lintDebug :app:assembleDebug` PASS; targeted connected rerun PASS; full connected Android test PASS (`136` tests, `0` failures, `0` errors, `0` skipped); APK badging/signature/install/launch evidence PASS.
  - Slice 26.6 validation summary: `docs/release-gate-logs/2026-06-07-sprint26-custom-targets-website-interventions/VALIDATION_SUMMARY.md`.
  - Slice 26.6 release notes: `docs/release-gate-logs/2026-06-07-sprint26-custom-targets-website-interventions/RELEASE_NOTES_v0.11.14-custom-targets-website-interventions-alpha.md`.
  - Slice 26.6 review bundle: `SPRINT26_FINAL_RELEASE_REVIEW_BUNDLE_20260607.zip`.
  - Slice 26.6 final GPT Pro review lane: `https://chatgpt.com/c/6a25e184-a30c-83eb-b346-199c70ed88b5`.
  - Slice 26.6 final GPT Pro output path: `evidence/sprint26_custom_targets_website_interventions/GPT_PRO_FINAL_RELEASE_REVIEW.md`.
  - Slice 26.6 final GPT Pro heartbeat: deleted after successful R1 harvest.
  - Slice 26.6 final GPT Pro heartbeat check at `2026-06-07T21:34:23Z`: exact-lane harvest and fresh-tab retry found GPT Pro still thinking with the stop button visible; no `SCORE`/`VERDICT` output was harvested and the heartbeat remains active.
  - Slice 26.6 final GPT Pro R1 review returned `SCORE 8/10`, `VERDICT REVISE`, `VISUAL REVIEW PASS`; blockers were release-note privacy wording that overclaimed local analytics sanitization, and bundle-hygiene inconsistency around retaining the superseded failed connected log on disk while excluding it from the review ZIP.
  - Slice 26.6 R2 fixes the R1 blockers by narrowing the claim to remote/export analytics payloads, explicitly allowing device-local analytics rows to keep package-level fields needed for local behavior, aligning the validation summary and bundle manifest on the superseded failed connected run, adding verbose signature/status evidence, adding unit XML and full source/test trees to the R2 review bundle, and shipping scrubbed canonical logs in the bundle.
  - Slice 26.6 R2 review bundle: `SPRINT26_FINAL_RELEASE_R2_REVIEW_BUNDLE_20260607.zip`.
  - Slice 26.6 R2 GPT Pro lane: `https://chatgpt.com/c/6a25e7a6-74a4-83ed-a848-58c4e4eafa6e`.
  - Slice 26.6 R2 GPT Pro output path: `evidence/sprint26_custom_targets_website_interventions/GPT_PRO_FINAL_RELEASE_R2_REVIEW.md`.
  - Slice 26.6 R2 GPT Pro heartbeat: deleted after successful harvest.
  - Slice 26.6 R2 heartbeat check at `2026-06-07T22:00:23Z`: exact-lane harvest saw GPT Pro still thinking with the stop button visible; the helper's first refresh/fresh-tab attempt hit a target-closed browser error, and an immediate explicit fresh-tab retry still showed GPT Pro thinking. No `SCORE`/`VERDICT` output was harvested at that checkpoint.
  - Slice 26.6 R2 review returned `SCORE 10/10`, `VERDICT PASS`, `VISUAL REVIEW PASS`; blockers none, bundle gaps none, package hygiene PASS, release readiness PASS.
  - Slice 26.6 R2 review confirmed the R1 blocker recheck: release notes now correctly scope privacy to remote/export analytics payloads while permitting device-local package-level fields needed for local behavior, and the validation summary/manifest now consistently treat the superseded failed connected run as retained on disk only and excluded from the R2 review ZIP.
  - Release commit: `189ec67` (`Complete Sprint 26 final release gate`).
  - Release tag: `v0.11.14-custom-targets-website-interventions-alpha`.
  - Release URL: `https://github.com/dkacz/qualityalternative/releases/tag/v0.11.14-custom-targets-website-interventions-alpha`.
  - Published assets: `quality-alternative-v0.11.14-custom-targets-website-interventions-alpha-debug.apk` and `quality-alternative-v0.11.14-custom-targets-website-interventions-alpha-debug.apk.sha256`.
  - Integration method: committed on `codex/sprint26-custom-targets-website-interventions`, tagged the release commit, pushed branch and tag to `origin`, and published the GitHub release from the committed release notes.
  - Slice 26.1 GPT Pro R3 review returned `SCORE 8/10`, `VERDICT FAIL`, `VISUAL REVIEW PASS`.
  - Slice 26.1 GPT Pro R4 review returned `SCORE 10/10`, `VERDICT PASS`, `VISUAL REVIEW PASS`.
  - Slice 26.1 GPT Pro R4 heartbeat was deleted after successful harvest.
- Current Slice 26.1 validation:
  - Validation summary: `evidence/sprint26_custom_targets_website_interventions/SLICE26_1_VALIDATION.md`
  - Visual contact sheet R2: `evidence/sprint26_custom_targets_website_interventions/screenshots-slice26_1_r2/CONTACT_SHEET.png`
  - Raw logs R2: `evidence/sprint26_custom_targets_website_interventions/logs-slice26_1_r2/`
  - GPT Pro Slice 26.1 lane: `https://chatgpt.com/c/6a2533e7-1718-83eb-a8a6-d55ddc6da463`
  - GPT Pro Slice 26.1 prompt: `evidence/sprint26_custom_targets_website_interventions/GPT_PRO_SLICE26_1_REVIEW_PROMPT.md`
  - GPT Pro Slice 26.1 bundle: `evidence/sprint26_custom_targets_website_interventions/SPRINT26_SLICE26_1_REVIEW_BUNDLE_20260607.zip`
  - GPT Pro Slice 26.1 R1 output: `evidence/sprint26_custom_targets_website_interventions/GPT_PRO_SLICE26_1_REVIEW.md`
  - GPT Pro Slice 26.1 R1 verdict: `SCORE 6/10`, `VERDICT FAIL`, `VISUAL REVIEW FAIL`
  - GPT Pro Slice 26.1 R2 lane: `https://chatgpt.com/c/6a253e58-239c-83ed-93df-5a24aad638fd`
  - GPT Pro Slice 26.1 R2 prompt: `evidence/sprint26_custom_targets_website_interventions/GPT_PRO_SLICE26_1_R2_REVIEW_PROMPT.md`
  - GPT Pro Slice 26.1 R2 bundle: `evidence/sprint26_custom_targets_website_interventions/SPRINT26_SLICE26_1_R2_REVIEW_BUNDLE_20260607.zip`
  - GPT Pro Slice 26.1 R2 output: `evidence/sprint26_custom_targets_website_interventions/GPT_PRO_SLICE26_1_R2_REVIEW.md`
  - GPT Pro Slice 26.1 R2 verdict: `SCORE 5/10`, `VERDICT FAIL`, `VISUAL REVIEW PASS`
  - GPT Pro Slice 26.1 R3 lane: `https://chatgpt.com/c/6a2546ce-9c3c-83eb-bb3e-732e636f8484`
  - GPT Pro Slice 26.1 R3 prompt: `evidence/sprint26_custom_targets_website_interventions/GPT_PRO_SLICE26_1_R3_REVIEW_PROMPT.md`
  - GPT Pro Slice 26.1 R3 bundle: `evidence/sprint26_custom_targets_website_interventions/SPRINT26_SLICE26_1_R3_REVIEW_BUNDLE_20260607.zip`
  - GPT Pro Slice 26.1 R3 output: `evidence/sprint26_custom_targets_website_interventions/GPT_PRO_SLICE26_1_R3_REVIEW.md`
  - GPT Pro Slice 26.1 R3 verdict: `SCORE 8/10`, `VERDICT FAIL`, `VISUAL REVIEW PASS`
  - GPT Pro Slice 26.1 R4 lane: `https://chatgpt.com/c/6a254f10-4c34-83eb-a943-148dde4a4efd`
  - GPT Pro Slice 26.1 R4 prompt: `evidence/sprint26_custom_targets_website_interventions/GPT_PRO_SLICE26_1_R4_REVIEW_PROMPT.md`
  - GPT Pro Slice 26.1 R4 bundle: `evidence/sprint26_custom_targets_website_interventions/SPRINT26_SLICE26_1_R4_REVIEW_BUNDLE_20260607.zip`
  - GPT Pro Slice 26.1 R4 output: `evidence/sprint26_custom_targets_website_interventions/GPT_PRO_SLICE26_1_R4_REVIEW.md`
  - GPT Pro Slice 26.1 R4 verdict: `SCORE 10/10`, `VERDICT PASS`, `VISUAL REVIEW PASS`
  - GPT Pro Slice 26.1 R4 heartbeat: deleted after successful harvest.
  - R4 validation summary: `evidence/sprint26_custom_targets_website_interventions/SLICE26_1_R4_VALIDATION.md`
  - Visual contact sheet R4: `evidence/sprint26_custom_targets_website_interventions/screenshots-slice26_1_r4/CONTACT_SHEET.png`
  - Raw logs R4: `evidence/sprint26_custom_targets_website_interventions/logs-slice26_1_r4/`
  - Passed targeted unit tests and lint for resolver, settings repository, ViewModel hydration, and Portable Profile import/export.
  - Passed emulator visual E2E: `VisualQaScreenshotTest#captureSprint26CustomTargetSettingsScreens`
  - Passed `git diff --check`.
- Next gate:
  - None for Sprint 26; release is published.

### Current Slice 26.2 Validation

- Validation summary: `evidence/sprint26_custom_targets_website_interventions/SLICE26_2_EVIDENCE.md`
- Visual screenshot directory: `evidence/sprint26_custom_targets_website_interventions/visual_e2e/sprint26-custom-targets-1780833001182/`
- Visual contact sheet: `evidence/sprint26_custom_targets_website_interventions/visual_e2e/sprint26_slice26_2_website_rules_contact_sheet.png`
- Passed targeted unit tests:
  - `WebsiteRuleNormalizerTest`
  - `PreferencesSettingsRepositoryTest`
  - `AccountLightProfileExporterTest`
  - `AccountLightProfileImporterTest`
  - `MainViewModelTest`
- Passed emulator visual E2E: `VisualQaScreenshotTest#captureSprint26WebsiteRuleSettingsScreens`
- Passed `:app:lintDebug`
- GPT Pro Slice 26.2 lane: `https://chatgpt.com/c/6a255d01-cb24-83eb-b76b-33fcd656b7e7`
- GPT Pro Slice 26.2 prompt: `evidence/sprint26_custom_targets_website_interventions/GPT_PRO_SLICE26_2_REVIEW_PROMPT.md`
- GPT Pro Slice 26.2 bundle: `evidence/sprint26_custom_targets_website_interventions/SPRINT26_SLICE26_2_REVIEW_BUNDLE_20260607.zip`
- GPT Pro Slice 26.2 R1 output: `evidence/sprint26_custom_targets_website_interventions/GPT_PRO_SLICE26_2_REVIEW.md`
- GPT Pro Slice 26.2 R1 verdict: `SCORE 7/10`, `VERDICT FAIL`, `VISUAL REVIEW PASS`
- GPT Pro Slice 26.2 R1 heartbeat: deleted after successful harvest.
- GPT Pro Slice 26.2 R2 evidence: `evidence/sprint26_custom_targets_website_interventions/SLICE26_2_R2_EVIDENCE.md`
- GPT Pro Slice 26.2 R2 prompt: `evidence/sprint26_custom_targets_website_interventions/GPT_PRO_SLICE26_2_R2_REVIEW_PROMPT.md`
- GPT Pro Slice 26.2 R2 bundle: `evidence/sprint26_custom_targets_website_interventions/SPRINT26_SLICE26_2_R2_REVIEW_BUNDLE_20260607.zip`
- GPT Pro Slice 26.2 R2 lane: `https://chatgpt.com/c/6a25665c-17d8-83eb-823f-46295216bbb1`
- GPT Pro Slice 26.2 R2 output: `evidence/sprint26_custom_targets_website_interventions/GPT_PRO_SLICE26_2_R2_REVIEW.md`
- GPT Pro Slice 26.2 R2 verdict: `SCORE 10/10`, `VERDICT PASS`, `VISUAL REVIEW PASS`
- GPT Pro Slice 26.2 R2 heartbeat: deleted after successful harvest.
- GPT Pro Slice 26.2 R2 visual screenshot directory: `evidence/sprint26_custom_targets_website_interventions/visual_e2e_r2/sprint26-custom-targets-1780835556853/`
- GPT Pro Slice 26.2 R2 visual contact sheet: `evidence/sprint26_custom_targets_website_interventions/visual_e2e_r2/sprint26_slice26_2_r2_website_rules_contact_sheet.png`
- GPT Pro Slice 26.2 R2 bundle manifest: `evidence/sprint26_custom_targets_website_interventions/SLICE26_2_R2_REVIEW_BUNDLE_MANIFEST.md`

### Current Slice 26.3 R2 Validation

- Evidence summary: `evidence/sprint26_custom_targets_website_interventions/SLICE26_3_EVIDENCE.md`
- R2 evidence summary: `evidence/sprint26_custom_targets_website_interventions/SLICE26_3_R2_EVIDENCE.md`
- Review bundle manifest: `evidence/sprint26_custom_targets_website_interventions/SLICE26_3_REVIEW_BUNDLE_MANIFEST.md`
- R2 review bundle manifest: `evidence/sprint26_custom_targets_website_interventions/SLICE26_3_R2_REVIEW_BUNDLE_MANIFEST.md`
- R2 diff: `evidence/sprint26_custom_targets_website_interventions/SLICE26_3_R2_DIFF.patch`
- GPT Pro Slice 26.3 lane: `https://chatgpt.com/c/6a257187-c76c-83eb-ab7a-c3b3e873fa85`
- GPT Pro Slice 26.3 prompt: `evidence/sprint26_custom_targets_website_interventions/GPT_PRO_SLICE26_3_REVIEW_PROMPT.md`
- GPT Pro Slice 26.3 R1 output: `evidence/sprint26_custom_targets_website_interventions/GPT_PRO_SLICE26_3_REVIEW.md`
- GPT Pro Slice 26.3 R1 verdict: `SCORE 7/10`, `VERDICT FAIL`, `VISUAL REVIEW PASS`
- GPT Pro Slice 26.3 R2 prompt: `evidence/sprint26_custom_targets_website_interventions/GPT_PRO_SLICE26_3_R2_REVIEW_PROMPT.md`
- GPT Pro Slice 26.3 R2 bundle: `SPRINT26_SLICE26_3_R2_REVIEW_BUNDLE_20260607.zip`
- GPT Pro Slice 26.3 R2 lane: `https://chatgpt.com/c/6a257f25-eefc-83ed-b502-a7c70b89ed71`
- GPT Pro Slice 26.3 R2 output: `evidence/sprint26_custom_targets_website_interventions/GPT_PRO_SLICE26_3_R2_REVIEW.md`
- GPT Pro Slice 26.3 R2 verdict: `SCORE 8/10`, `VERDICT FAIL`, `VISUAL REVIEW PASS`
- GPT Pro Slice 26.3 R2 blocker: website-domain analytics could still include replacement `externalUrl` values through shared content metadata.
- GPT Pro Slice 26.3 R2 heartbeat: deleted after successful harvest.
- R3 evidence summary: `evidence/sprint26_custom_targets_website_interventions/SLICE26_3_R3_EVIDENCE.md`
- R3 diff: `evidence/sprint26_custom_targets_website_interventions/SLICE26_3_R3_DIFF.patch`
- R3 raw test evidence: `evidence/sprint26_custom_targets_website_interventions/logs-slice26_3_r3/`
- GPT Pro Slice 26.3 R3 prompt: `evidence/sprint26_custom_targets_website_interventions/GPT_PRO_SLICE26_3_R3_REVIEW_PROMPT.md`
- GPT Pro Slice 26.3 R3 bundle: `SPRINT26_SLICE26_3_R3_REVIEW_BUNDLE_20260607.zip`
- GPT Pro Slice 26.3 R3 lane: `https://chatgpt.com/c/6a258670-aa7c-83eb-a4ec-a37abb91c04f`
- GPT Pro Slice 26.3 R3 output: `evidence/sprint26_custom_targets_website_interventions/GPT_PRO_SLICE26_3_R3_REVIEW.md`
- GPT Pro Slice 26.3 R3 verdict: `SCORE 9/10`, `VERDICT PASS`, `VISUAL REVIEW PASS`, blockers none.
- GPT Pro Slice 26.3 R3 heartbeat: deleted after successful harvest.
- R4 evidence summary: `evidence/sprint26_custom_targets_website_interventions/SLICE26_3_R4_EVIDENCE.md`
- R4 diff: `evidence/sprint26_custom_targets_website_interventions/SLICE26_3_R4_DIFF.patch`
- R4 raw test evidence: `evidence/sprint26_custom_targets_website_interventions/logs-slice26_3_r4/`
- GPT Pro Slice 26.3 R4 prompt: `evidence/sprint26_custom_targets_website_interventions/GPT_PRO_SLICE26_3_R4_REVIEW_PROMPT.md`
- GPT Pro Slice 26.3 R4 bundle: `SPRINT26_SLICE26_3_R4_REVIEW_BUNDLE_20260607.zip`
- GPT Pro Slice 26.3 R4 lane: `https://chatgpt.com/c/6a258c5f-0fd8-83eb-b96b-434cb0c95945`
- GPT Pro Slice 26.3 R4 output: `evidence/sprint26_custom_targets_website_interventions/GPT_PRO_SLICE26_3_R4_REVIEW.md`
- GPT Pro Slice 26.3 R4 verdict: `SCORE 9/10`, `VERDICT PASS`, `VISUAL REVIEW PASS`, blockers none.
- GPT Pro Slice 26.3 R4 heartbeat: deleted after successful harvest.
- R5 evidence summary: `evidence/sprint26_custom_targets_website_interventions/SLICE26_3_R5_EVIDENCE.md`
- R5 review bundle manifest: `evidence/sprint26_custom_targets_website_interventions/SLICE26_3_R5_REVIEW_BUNDLE_MANIFEST.md`
- GPT Pro Slice 26.3 R5 prompt: `evidence/sprint26_custom_targets_website_interventions/GPT_PRO_SLICE26_3_R5_REVIEW_PROMPT.md`
- GPT Pro Slice 26.3 R5 bundle: `SPRINT26_SLICE26_3_R5_REVIEW_BUNDLE_20260607.zip`
- GPT Pro Slice 26.3 R5 lane: `https://chatgpt.com/c/6a259198-0f20-83eb-849d-4188836260c4`
- GPT Pro Slice 26.3 R5 review output target: `evidence/sprint26_custom_targets_website_interventions/GPT_PRO_SLICE26_3_R5_REVIEW.md`
- GPT Pro Slice 26.3 R5 verdict: `SCORE 9/10`, `VERDICT PASS`, `VISUAL REVIEW PASS`, blockers none.
- GPT Pro Slice 26.3 R5 heartbeat: deleted after successful harvest.
- R6 evidence summary: `evidence/sprint26_custom_targets_website_interventions/SLICE26_3_R6_EVIDENCE.md`
- R6 review bundle manifest: `evidence/sprint26_custom_targets_website_interventions/SLICE26_3_R6_REVIEW_BUNDLE_MANIFEST.md`
- R6 diff: `evidence/sprint26_custom_targets_website_interventions/SLICE26_3_R6_DIFF.patch`
- GPT Pro Slice 26.3 R6 prompt: `evidence/sprint26_custom_targets_website_interventions/GPT_PRO_SLICE26_3_R6_REVIEW_PROMPT.md`
- GPT Pro Slice 26.3 R6 bundle: `SPRINT26_SLICE26_3_R6_REVIEW_BUNDLE_20260607.zip`
- GPT Pro Slice 26.3 R6 lane: `https://chatgpt.com/c/6a259ebf-7448-83eb-8027-d21674b45366`
- GPT Pro Slice 26.3 R6 output target: `evidence/sprint26_custom_targets_website_interventions/GPT_PRO_SLICE26_3_R6_REVIEW.md`
- GPT Pro Slice 26.3 R6 verdict: `SCORE 9/10`, `VERDICT FAIL`, `VISUAL REVIEW PASS`.
- GPT Pro Slice 26.3 R6 heartbeat: deleted after successful harvest.
- GPT Pro Slice 26.3 R6 heartbeat check `2026-06-07T16:49:22Z`: exact-lane harvest still showed ChatGPT thinking; final R6 harvest completed at the next heartbeat and the heartbeat was deleted.
- R7 evidence summary: `evidence/sprint26_custom_targets_website_interventions/SLICE26_3_R7_EVIDENCE.md`
- R7 review bundle manifest: `evidence/sprint26_custom_targets_website_interventions/SLICE26_3_R7_REVIEW_BUNDLE_MANIFEST.md`
- R7 diff: `evidence/sprint26_custom_targets_website_interventions/SLICE26_3_R7_DIFF.patch`
- GPT Pro Slice 26.3 R7 prompt: `evidence/sprint26_custom_targets_website_interventions/GPT_PRO_SLICE26_3_R7_REVIEW_PROMPT.md`
- GPT Pro Slice 26.3 R7 bundle: `SPRINT26_SLICE26_3_R7_REVIEW_BUNDLE_20260607.zip`
- GPT Pro Slice 26.3 R7 lane: `https://chatgpt.com/c/6a25a63b-ce44-83eb-8cdc-764669a47268`
- GPT Pro Slice 26.3 R7 output: `evidence/sprint26_custom_targets_website_interventions/GPT_PRO_SLICE26_3_R7_REVIEW.md`
- GPT Pro Slice 26.3 R7 verdict: `SCORE 10/10`, `VERDICT PASS`, `VISUAL REVIEW PASS`
- GPT Pro Slice 26.3 R7 heartbeat: deleted after successful harvest.
- GPT Pro Slice 26.3 R7 heartbeat check `2026-06-07T17:21:22Z`: exact-lane harvest still showed ChatGPT thinking; no completed `SCORE`/`VERDICT` response harvested yet, so the heartbeat remained active at that checkpoint.
- GPT Pro Slice 26.3 R7 harvest `2026-06-07T17:31:22Z`: exact-lane harvest completed from `https://chatgpt.com/c/6a25a63b-ce44-83eb-8cdc-764669a47268`; output preserved at `evidence/sprint26_custom_targets_website_interventions/GPT_PRO_SLICE26_3_R7_REVIEW.md`; heartbeat deleted.
- Visual screenshot directory: `evidence/sprint26_custom_targets_website_interventions/visual_e2e_slice26_3/sprint26-custom-targets-1780838366859/`
- Visual contact sheet: `evidence/sprint26_custom_targets_website_interventions/visual_e2e_slice26_3/sprint26_slice26_3_chrome_website_intervention_contact_sheet.png`
- R2 visual evidence: `evidence/sprint26_custom_targets_website_interventions/visual_e2e_slice26_3_r2/sprint26_slice26_3_r2_chrome_verified_host_contact_sheet.png`
- R2 Chrome evidence: `evidence/sprint26_custom_targets_website_interventions/chrome_verified_host_e2e_r2_latest/`
- R6 visual evidence: `evidence/sprint26_custom_targets_website_interventions/visual_e2e_slice26_3_r6/`
- R6 Chrome/live-service evidence: `evidence/sprint26_custom_targets_website_interventions/chrome_verified_host_e2e_r6_latest/` and `evidence/sprint26_custom_targets_website_interventions/live_service_e2e_r6/`
- R6 raw test evidence: `evidence/sprint26_custom_targets_website_interventions/logs-slice26_3_r6/`
- Raw test evidence: `evidence/sprint26_custom_targets_website_interventions/logs-slice26_3_r2/`
- Passed targeted unit tests:
  - `VerifiedBrowserHostAdapterTest`
  - `WebsiteInterceptionResolverTest`
  - `AccessibilityInterceptionPlannerTest`
  - `MainViewModelTest.requestSystemWebsiteInterception_opensInterventionWithoutSelectedBrowserAndKeepsDomainPrivate`
  - `MainViewModelTest.websiteOpenAnywaySuppressesWebsiteKeyWithoutSuppressingWholeBrowserTarget`
- Passed real Chrome adapter harness: `AccessibilityInterceptionTest#chromeVerifiedHostAdapterHarnessAcceptsOnlyLoadedMatchingHost` on `qaApi36(AVD) - 16`.
- Passed emulator visual E2E:
  - `VisualQaScreenshotTest#captureSprint26WebsiteRuleSettingsScreens`
  - `VisualQaScreenshotTest#captureSprint26ChromeWebsiteInterventionScreens`
- Passed `:app:lintDebug`.
- R3 passed targeted unit regression for website-domain analytics URL privacy.
- R3 passed `:app:lintDebug`.
- R3 passed `git diff --check`.
- R4 passed full `:app:testDebugUnitTest`.
- R4 passed `:app:lintDebug`.
- R6 passed full `:app:testDebugUnitTest` with 432 tests and no failures/errors/skips.
- R6 passed `:app:lintDebug` with 0 errors.
- R6 passed fresh connected Android tests 4/4, including Chrome loaded-host harness, unsupported/unreadable negative states, website Settings visuals, and website intervention visuals.
- R6 passed external live-service Chrome E2E and `git diff --check`.
- R7 passed full `:app:testDebugUnitTest` with 433 tests and no failures/errors/skips.
- R7 passed `:app:lintDebug` with 0 errors.
- R7 passed fresh connected Android tests 4/4, including package-mismatched stale snapshot negative evidence.
- R7 passed external live-service Chrome E2E after the package-authentication change and `git diff --check`.

## Sprint 25 Markdown Media And Tables

Status: `gpt_pro_10_10_pass_release_prep`

- Branch: `codex/sprint25-md-image-embeds`
- Scope: Markdown reader import/rendering fixes for embedded images and Markdown tables.
- Current implementation state:
  - Added `ReaderDocumentImage` and image metadata on reader document blocks.
  - Added `MarkdownReaderDocumentParser` for standalone Markdown images, inline-image alt text, `data:image/...` payloads, relative `file://` image resolution, and picked image attachment URI maps.
  - Added Markdown image attachment URI persistence via `user_documents.imageAttachmentUrisJson` and migration `14 -> 15`.
  - Updated the Android document picker to allow image files and attach picked images to Markdown documents instead of saving image files as separate library items.
  - Updated reader UI to render image blocks from `content://`, `file://`, `android.resource://`, and `data:image/...`; placeholders appear when an image source cannot be opened.
  - Added pipe Markdown table parsing with header/body rows and alignment metadata.
  - Updated reader UI to render Markdown tables as structured rows and columns with header styling, cell alignment, and horizontal scrolling for wide tables.
  - Updated reader pagination, progress snapshots, and reading-time estimates so image payloads and Markdown table delimiter syntax do not distort page fit or time estimates.
  - Updated reader gesture handling so horizontally scrolling wide Markdown tables do not advance or complete reader pages.
  - Narrowed the table gesture guard after GPT Pro R2 so ordinary text taps/swipes still advance reader pages.
  - Updated table pagination measurement so wrapped cell text contributes to page fit and oversized tables split by visual row weight.
- Validation status:
  - Unit/instrumented tests have been added for parser, reading-time estimate, Room migration, document attachment persistence, reader image block pagination, reader table pagination, and Sprint 25 visual evidence.
  - Passed with Homebrew JDK 17: `./gradlew :app:testDebugUnitTest :app:lintDebug`.
  - Passed on emulator `qaApi36(AVD) - 16`: `VisualQaScreenshotTest#captureSprint25MarkdownMediaAndTableScreens`.
  - Passed on emulator `qaApi36(AVD) - 16`: `VisualQaScreenshotTest#captureSprint25WideMarkdownTableHorizontalScrollDoesNotAdvanceReaderPage`.
  - Passed on emulator `qaApi36(AVD) - 16`: `VisualQaScreenshotTest#captureSprint25OrdinaryTextNavigationStillWorksAfterTableGestureGuard`.
  - Passed on emulator `qaApi36(AVD) - 16`: `RoomUserDocumentRepositoryTest`.
  - Passed on emulator `qaApi36(AVD) - 16`: `QualityAlternativeDatabaseMigrationInstrumentedTest`.
  - Visual evidence: `evidence/sprint25_markdown_media_tables/screenshots-r3/contact_sheet_r3.png`.
  - R3 Android results: `evidence/sprint25_markdown_media_tables/android-results-r3/`.
  - Passed: `git diff --check`.
  - GPT Pro R3: `SCORE 10/10`, `VERDICT PASS`, `VISUAL REVIEW PASS`.
- Next gate:
  - Bump Android version, run final tests, build and verify the debug APK, then publish the GitHub release.

## Android Meditation / Content Priority Fix

Status: `complete_10_10_pass`

- Branch: `codex/android-meditation-priority-controls`
- Remote: `origin/codex/android-meditation-priority-controls`
- Latest implementation commit: `b37cdb8` (`Persist replacement history durations`)
- PR draft URL: `https://github.com/dkacz/qualityalternative/pull/new/codex/android-meditation-priority-controls`
- Current GPT Pro follow-up lane: `https://chatgpt.com/c/69eb34ec-0704-8388-b485-e94dc4080e4e`
- Heartbeat automation: `android-meditation-priority-pro-review`
- Current review prompt: `PRO_REVIEW_PROMPT_20260424_110657.md`
- Current review bundle: `QUALITY_ALTERNATIVE_REVIEW_BUNDLE_20260424_110657.zip`
- Final review output: `PRO_REVIEW_OUTPUT_20260424_110657_CLEAN/Android_Meditation_Review.md`
- Final verdict: `10/10 PASS`
- Prior review outputs: `PRO_REVIEW_OUTPUT_20260424_093850/`, `PRO_REVIEW_OUTPUT_20260424_103230/`
- Superseded mixed/accidental lane no longer tracked: `https://chatgpt.com/c/69eb335c-dc40-8391-84c3-383aefd24c0c`
- Visual QA contact sheet: `output/android-meditation-priority-visual-20260424/contact_sheet_meditation_priority.png`

Completed validation:

- `testDebugUnitTest`
- `lintDebug`
- `connectedDebugAndroidTest` on Android emulator, 50/50 passing
- `git diff --check`
- JSON validation for `app/src/main/assets/editorial/starter_packs.json`
- `VisualQaScreenshotTest#captureSprint10ReaderProgressStreakAndMeditationScreens`
- Manual visual review of the contact sheet

Next gate:

- Eligible for merge/release decision.
- Heartbeat `android-meditation-priority-pro-review` should be deleted after this PASS is reported.

## iOS Sprint 11 Discovery

Status: `complete_10_10_pass`

- Branch/source: `main` and `codex/sprint11-ios-discovery`
- Latest relevant commit on main: `bae1778` (`Remove iOS reopen gate overlap`)
- Canonical docs:
  - `docs/IOS_DISCOVERY.md`
  - `docs/SPRINT_11_IOS_DISCOVERY.md`
- Final GPT Pro output: `PRO_REVIEW_OUTPUT_20260423_233635_RETRY/iOS_Sprint_11_Review.md`
- Final verdict: `10/10 PASS`
- Recommendation: run a bounded `build_full_ios_spike`, not a promise of Android/iOS parity.

Important decision:

- iOS should be pursued as a Screen Time / FamilyControls / DeviceActivity / ManagedSettings feasibility spike.
- Exact Android overlay parity is not a public-API target on iOS.
- Android remains the MVP launch platform unless the PRD changes.

## iOS Sprint 12 Implementation Spike

Status: `slice_12_1_implemented_pushed_not_currently_under_review`

- Branch: `codex/sprint12-ios-implementation`
- Remote: `origin/codex/sprint12-ios-implementation`
- Commit: `571aef4` (`Add iOS visual parity skeleton`)
- Canonical branch doc: `docs/SPRINT_12_IOS_IMPLEMENTATION.md` on `codex/sprint12-ios-implementation`
- Visual QA artifacts on that branch: `docs/visual-qa/sprint12-ios-slice12-1/`
- Local reference bundle: `output/ios-android-visual-parity-reference-20260424_083016/IOS_ANDROID_VISUAL_PARITY_REFERENCE_20260424_083016.zip`

Implemented in Slice 12.1:

- Native SwiftUI project under `ios/`
- Visual parity skeleton for home, library, intervention, reader, link handoff, meditation, progress, and settings
- Shared visual language: parchment/dark palette, rounded cards, serif display typography, finite replacement shape
- iOS unit tests and UI screenshot tests
- Simulator visual QA screenshots

Known state:

- This is a visual/flow skeleton, not a complete iOS release implementation.
- Screen Time APIs, app shielding, FamilyControls authorization, DeviceActivity monitoring, real persistence, app selection, and signing/release packaging are not complete.
- The lane is not currently waiting on an active GPT Pro heartbeat.

Next gate:

- Resume this branch in a clean iOS worktree.
- Launch GPT Pro review for Slice 12.1 with the branch doc, source, tests, and visual QA screenshots.
- Iterate until `10/10 PASS`.
- Only then proceed to Slice 12.2.
