# Sprint 9 Android Validation Results

Date: 2026-04-26

Scope: post-integration Android validation for Sprint 9 content expansion after fifth GPT Pro visual review remediation.

## Current App Inventory

- Total starter packs in `app/src/main/assets/editorial/starter_packs.json`: 10
- Total editorial items in the Android asset manifest: 145
- Sprint 9 packs integrated into the Android asset manifest: 5
- Sprint 9 editorial items integrated into the Android asset manifest: 100
- Sprint 9 renderable in-app reader items: 42
- Sprint 9 link-only external handoff items: 58
- Missing Sprint 9 renderable body assets: 0
- Sprint 9 render-mode contract mismatches: 0
- Project Gutenberg boilerplate hits in Sprint 9 renderable body assets: 0
- Sprint 9 renderable word-count range: 513 to 1709 words
- Corrected Fabre fly body word count: 752 words
- Sprint 9 `rightsReviewedAt` date: 2026-04-26
- Sprint 9 final release approval rows in `docs/content-sourcing/final_release_approval_20260426.csv`: 100
- Sprint 9 approved in-app reader rows: 42
- Sprint 9 approved link-only handoff rows: 58

## Sprint 9 Pack Counts

- `attention_practical_agency_v1`: 24 items; 9 renderable, 15 link-only
- `embodied_calm_v1`: 20 items; 8 renderable, 12 link-only
- `wonder_science_v1`: 26 items; 11 renderable, 15 link-only
- `long_view_history_v1`: 22 items; 8 renderable, 14 link-only
- `creativity_play_v1`: 8 items; 6 renderable, 2 link-only

## Automated Test Evidence

- `:app:testDebugUnitTest`: passed, 169 tests, 0 failures, 0 errors, 0 skipped.
- `:app:connectedDebugAndroidTest`: passed, 54 tests, 0 failures, 0 errors, 0 skipped on `qaApi36`.
- Direct Android instrumentation runner for screenshot capture: `OK (1 test)`.

The direct runner was used after installing the debug and androidTest APKs so the generated screenshot files could be pulled from the app sandbox before Gradle cleanup.

Final raw command logs are included in `test-evidence/`; generated Gradle XML result files were checked locally and included in the GPT Pro review bundle.

## Screenshot Evidence

- `01_library_sprint9_light.png`: Sprint 9 library inventory with new renderable items.
- `02_intervention_sprint9_renderable_light.png`: intervention with Sprint 9 renderable option visible.
- `03_reader_sprint9_renderable_light.png`: in-app reader for a Sprint 9 renderable Markdown body.
- `03b_reader_sprint9_darwin_light.png`: in-app reader for the corrected Darwin body opening.
- `03c_reader_sprint9_figuier_light.png`: in-app reader for the corrected Figuier body opening.
- `03d_reader_sprint9_fabre_fly_light.png`: in-app reader for the corrected Fabre fly body opening.
- `04_intervention_sprint9_link_only_light.png`: intervention with Sprint 9 link-only option visible.
- `05_external_handoff_sprint9_light.png`: external handoff state for a Sprint 9 link-only item.
- `06_intervention_sprint9_dark.png`: dark-mode intervention for a Sprint 9 history item.
- `07_reader_sprint9_dark.png`: dark-mode in-app reader for a Sprint 9 renderable Markdown body.

## Local Visual Inspection Status

Codex local inspection found no obvious overlap, clipping, unreadable body text, or broken external handoff presentation in the captured Sprint 9 light/dark screenshots after the fifth GPT Pro visual remediation. The visible Sprint 9 intervention copy no longer shows internal release or implementation terms such as "candidate", "Sprint 9", "link-only", "in-app", or "rehosting". The added Darwin and Figuier reader screenshots begin with substantive reader text and do not show the prior bibliographic footnote, title-page, or book-purpose framing. The added Fabre fly reader screenshot begins with fly-specific body text and no longer shows the prior open-air-laboratory mismatch.

GPT Pro post-integration visual review and the first four fix follow-ups returned `BLOCK`. The fifth remediation replaced the Fabre fly body with a fly-specific Project Gutenberg excerpt, added a narrow subject-alignment guard for the fragile Fabre row, added a targeted Fabre reader screenshot, refreshed screenshots, and reran unit, connected, and direct screenshot validation. The final GPT Pro visual verdict is `PASS`, harvested at `PRO_REVIEW_OUTPUT_SPRINT9_ANDROID_VISUAL_FIX5_20260426_181457/Sprint_9_Android_Audit.md`.
