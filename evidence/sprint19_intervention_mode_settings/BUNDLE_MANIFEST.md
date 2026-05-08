# Sprint 19 Slice 19.5B Review Bundle Manifest

Scope: intervention mode settings repair.

This bundle is intentionally selective. It contains the implementation diff, the touched source and test files, local verification logs, and screenshots from the connected Android test. It excludes unrelated prior Sprint 17/18/19 review bundles, APKs, Gradle build directories, and stale ChatGPT harvest outputs so GPT Pro can audit the current fix without confusing generated noise.

Primary evidence:
- EVIDENCE.md
- sprint19_intervention_mode_settings.diff
- logs/unit.log
- logs/connected_intervention_mode.log
- screenshots/13_intervention_mode_soft_selected.png
- screenshots/14_soft_mode_open_anyway_immediate.png
- screenshots/15_firm_mode_open_anyway_wait.png

Source files:
- app/src/main/java/com/qualityalternative/app/domain/model/UserModels.kt
- app/src/main/java/com/qualityalternative/app/domain/service/Contracts.kt
- app/src/main/java/com/qualityalternative/app/data/PreferencesSettingsRepository.kt
- app/src/main/java/com/qualityalternative/app/data/AccountLightProfile.kt
- app/src/main/java/com/qualityalternative/app/ui/MainViewModel.kt
- app/src/main/java/com/qualityalternative/app/ui/QualityAlternativeApp.kt

Test files:
- app/src/test/java/com/qualityalternative/app/data/PreferencesSettingsRepositoryTest.kt
- app/src/test/java/com/qualityalternative/app/data/AccountLightProfileExporterTest.kt
- app/src/test/java/com/qualityalternative/app/ui/MainViewModelTest.kt
- app/src/androidTest/java/com/qualityalternative/app/data/RoomAnalyticsTrackerTest.kt
- app/src/androidTest/java/com/qualityalternative/app/MainActivityTest.kt

Docs:
- PRD.md
- docs/SPRINT_19_AI_NOTE_ASSIST.md
