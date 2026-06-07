# Lane Status

Status timestamp: 2026-06-07

This file is the repo-level index for active and recently completed execution lanes. It should point to the canonical branch, review lane, validation artifacts, and next gate for each lane.

## Current Rule

- Use this document for cross-lane status.
- Use the branch-specific sprint docs for detailed implementation notes.
- Heartbeats should exist only while waiting for a current GPT Pro lane.
- Do not infer lane status from untracked local files alone. For example, `ios/` can appear untracked on non-iOS branches; the canonical iOS implementation source is the pushed iOS branch listed below.

## Sprint 26 Custom Targets And Website Interventions

Status: `slice26_3_complete_10_10_pass_next_slice26_4`

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
  - Commit Slice 26.3 implementation/evidence.
  - Continue to Slice 26.4 privacy, analytics, and Portable Profile hardening.

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
