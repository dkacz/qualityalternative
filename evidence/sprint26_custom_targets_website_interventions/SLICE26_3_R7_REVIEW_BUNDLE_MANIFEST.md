# Sprint 26 Slice 26.3 R7 Review Bundle Manifest

Bundle: `SPRINT26_SLICE26_3_R7_REVIEW_BUNDLE_20260607.zip`

Purpose: GPT Pro 10/10 gate review after R6 returned `SCORE 9/10`, `VERDICT FAIL`, `VISUAL REVIEW PASS`, with one blocker: stale active-window/package-mismatch safety was not proven.

## Included Planning / Status Files

- `PRD.md`
- `docs/SPRINT_26_CUSTOM_TARGETS_WEBSITE_INTERVENTIONS.md`
- `docs/LANE_STATUS.md`

## Included Review / Evidence Files

- `evidence/sprint26_custom_targets_website_interventions/GPT_PRO_SLICE26_3_R6_REVIEW.md`
- `evidence/sprint26_custom_targets_website_interventions/GPT_PRO_SLICE26_3_R7_REVIEW_PROMPT.md`
- `evidence/sprint26_custom_targets_website_interventions/SLICE26_3_R7_EVIDENCE.md`
- `evidence/sprint26_custom_targets_website_interventions/SLICE26_3_R7_REVIEW_BUNDLE_MANIFEST.md`
- `evidence/sprint26_custom_targets_website_interventions/SLICE26_3_R7_DIFF.patch`
- `evidence/sprint26_custom_targets_website_interventions/logs-slice26_3_r7/`
- `evidence/sprint26_custom_targets_website_interventions/logs-slice26_3_r7_unit_lint_console.txt`
- `evidence/sprint26_custom_targets_website_interventions/chrome_verified_host_e2e_r7_latest/`
- `evidence/sprint26_custom_targets_website_interventions/visual_e2e_slice26_3_r7/`
- `evidence/sprint26_custom_targets_website_interventions/live_service_e2e_r7/`

## Included Source / Tests

- Full `app/src/` tree, including:
  - production source,
  - unit tests,
  - android tests,
  - resources,
  - manifest.

## Included Build Context

- `settings.gradle.kts`
- `build.gradle.kts`
- `gradle/libs.versions.toml`
- `gradlew`
- `gradlew.bat`
- `gradle/wrapper/gradle-wrapper.properties`
- `gradle/wrapper/gradle-wrapper.jar`
- `app/build.gradle.kts`
- `app/proguard-rules.pro`

## Deliberate Exclusions

- APKs and release artifacts are excluded from this slice review.
- Gradle build output under `app/build/` is excluded except copied unit, lint, connected, and visual evidence in `logs-slice26_3_r7/`.
- Older Slice 26.1 / 26.2 / 26.3 review bundles and superseded logs are excluded; accepted results and current status are documented in `docs/LANE_STATUS.md`.
- R6 review output is included because R7 directly fixes its blocker.
