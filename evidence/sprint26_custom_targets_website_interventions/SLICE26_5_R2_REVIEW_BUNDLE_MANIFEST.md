# Sprint 26 Slice 26.5 R2 GPT Pro Review Bundle Manifest

Bundle target: Sprint 26 Slice 26.5 R2 - Bedtime And Supported Website Target Integration.

Primary documents:

- `PRD.md`
- `docs/SPRINT_26_CUSTOM_TARGETS_WEBSITE_INTERVENTIONS.md`
- `docs/LANE_STATUS.md`
- `evidence/sprint26_custom_targets_website_interventions/SLICE26_5_EVIDENCE.md`
- `evidence/sprint26_custom_targets_website_interventions/GPT_PRO_SLICE26_5_REVIEW_SCRUBBED_FOR_R2.md`

Prior review context:

- `evidence/sprint26_custom_targets_website_interventions/GPT_PRO_PLAN_REVIEW_R2.md`
- `evidence/sprint26_custom_targets_website_interventions/GPT_PRO_SLICE26_5_REVIEW.md` is preserved locally as the original R1 harvest; the R2 external bundle uses the scrubbed copy above.
- `evidence/sprint26_custom_targets_website_interventions/GPT_PRO_SLICE26_1_R4_REVIEW.md`
- `evidence/sprint26_custom_targets_website_interventions/GPT_PRO_SLICE26_3_R7_REVIEW.md`
- `evidence/sprint26_custom_targets_website_interventions/GPT_PRO_SLICE26_4_R3_REVIEW.md`

Prior Slice 26.1 R4 custom-app Bedtime artifacts included for reinspection:

- `evidence/sprint26_custom_targets_website_interventions/SLICE26_1_R4_VALIDATION.md`
- `evidence/sprint26_custom_targets_website_interventions/screenshots-slice26_1_r4/CONTACT_SHEET.png`
- `evidence/sprint26_custom_targets_website_interventions/logs-slice26_1_r4/`

Source and tests:

- `app/src/main/AndroidManifest.xml`
- `app/src/main/java/com/qualityalternative/app/MainActivity.kt`
- `app/src/main/java/com/qualityalternative/app/ui/MainViewModel.kt`
- `app/src/main/java/com/qualityalternative/app/ui/QualityAlternativeApp.kt`
- `app/src/main/java/com/qualityalternative/app/interception/QualityAlternativeAccessibilityService.kt`
- `app/src/main/java/com/qualityalternative/app/interception/InterceptionRuntimeGate.kt`
- `app/src/main/java/com/qualityalternative/app/interception/WebsiteInterceptionResolver.kt`
- `app/src/main/java/com/qualityalternative/app/interception/VerifiedBrowserHostAdapter.kt`
- `app/src/main/java/com/qualityalternative/app/domain/model/AnalyticsPrivacyGuard.kt`
- `app/src/main/java/com/qualityalternative/app/domain/model/UserModels.kt`
- `app/src/test/java/com/qualityalternative/app/ui/MainViewModelTest.kt`
- `app/src/test/java/com/qualityalternative/app/interception/InterceptionRuntimeGateTest.kt`
- `app/src/test/java/com/qualityalternative/app/interception/ForegroundAppDetectionPolicyTest.kt`
- `app/src/test/java/com/qualityalternative/app/interception/WebsiteInterceptionResolverTest.kt`
- `app/src/test/java/com/qualityalternative/app/domain/model/AnalyticsPrivacyGuardTest.kt`
- `app/src/androidTest/java/com/qualityalternative/app/MainActivityTest.kt`
- `app/src/androidTest/java/com/qualityalternative/app/AccessibilityInterceptionTest.kt`
- `app/src/androidTest/java/com/qualityalternative/app/VisualQaScreenshotTest.kt`

Validation artifacts:

- `evidence/sprint26_custom_targets_website_interventions/SLICE26_5_R2_DIFF.patch`
- `evidence/sprint26_custom_targets_website_interventions/logs-slice26_5_r2/unit_lint_console.scrubbed.txt`
- `evidence/sprint26_custom_targets_website_interventions/logs-slice26_5_r2/unit_xml/`
- `evidence/sprint26_custom_targets_website_interventions/logs-slice26_5_r2/lint-results-debug.scrubbed.txt`
- `evidence/sprint26_custom_targets_website_interventions/logs-slice26_5_r2/lint-results-debug.scrubbed.xml`
- `evidence/sprint26_custom_targets_website_interventions/logs-slice26_5_r2/connected_debug/`
- `evidence/sprint26_custom_targets_website_interventions/logs-slice26_5_r2/git_diff_check.txt`
- `evidence/sprint26_custom_targets_website_interventions/logs-slice26_5_r2/git_diff_check.status.txt`
- `evidence/sprint26_custom_targets_website_interventions/logs-slice26_5_r2/adb_devices_after_emulator_shutdown.txt`
- `evidence/sprint26_custom_targets_website_interventions/live_service_e2e_slice26_5_r2/`

Visual artifacts:

- `evidence/sprint26_custom_targets_website_interventions/visual_e2e_slice26_5_r2/sprint26_slice26_5_r2_bedtime_website_live_contact_sheet.png`
- `evidence/sprint26_custom_targets_website_interventions/visual_e2e_slice26_5_r2/sprint26-custom-targets-1780861651054/23_website_chrome_verified_host_soft_intervention_light.png`
- `evidence/sprint26_custom_targets_website_interventions/visual_e2e_slice26_5_r2/sprint26-custom-targets-1780861651054/24_website_chrome_verified_host_firm_wait_light.png`
- `evidence/sprint26_custom_targets_website_interventions/visual_e2e_slice26_5_r2/sprint26-custom-targets-1780861651054/25_website_chrome_bedtime_emergency_unlock_light.png`
- `evidence/sprint26_custom_targets_website_interventions/live_service_e2e_slice26_5_r2/live_chrome_bedtime_service_intervention.png`
- `evidence/sprint26_custom_targets_website_interventions/live_service_e2e_slice26_5_r2/window_live.xml`

Package hygiene notes:

- R2 includes the source files GPT Pro named as missing in R1.
- R2 includes prior Slice 26.1 R4 custom-app Bedtime artifacts referenced by current acceptance.
- R2 includes scrubbed lint text/XML artifacts and a scrubbed R1 review copy in the review bundle to avoid leaking absolute local paths.
- R2 excludes the Chrome shell launch log and empty binary result directory; the live-service proof remains auditable through the seed log, accessibility service dump, UI dump, result file, and screenshot.
- R2 excludes stale failed visual attempts and the failed in-instrumentation live-service attempt from the canonical bundle.
