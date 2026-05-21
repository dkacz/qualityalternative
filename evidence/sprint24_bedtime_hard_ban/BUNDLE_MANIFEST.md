# Sprint 24 GPT Pro Bundle Manifest

## Included

- `PRD.md` - product contract updated for opt-in Bedtime protection.
- `app/src/main/java/com/qualityalternative/app/MainActivity.kt` - system-interception launch and target-exit mechanics.
- `app/src/main/java/com/qualityalternative/app/domain/model/ContentModels.kt` - content and meditation helper model used by the intervention UI.
- `app/src/main/java/com/qualityalternative/app/domain/model/MeditationReplacement.kt` - meditation replacement constants and helper item.
- `app/src/main/java/com/qualityalternative/app/domain/service/DefaultRecommendationEngine.kt` - recommendation/backup behavior.
- `app/src/main/java/com/qualityalternative/app/domain/model/UserModels.kt` - bedtime constants and settings fields.
- `app/src/main/java/com/qualityalternative/app/domain/model/InterventionModels.kt` - bedtime analytics events.
- `app/src/main/java/com/qualityalternative/app/domain/service/Contracts.kt` - settings repository contract.
- `app/src/main/java/com/qualityalternative/app/interception/InterceptionRuntimeGate.kt` - normal vs Bedtime emergency-unlock suppression behavior.
- `app/src/main/java/com/qualityalternative/app/interception/ForegroundAppDetectionPolicy.kt` - service de-duplication policy used before launching intervention.
- `app/src/main/java/com/qualityalternative/app/interception/QualityAlternativeAccessibilityService.kt` - active Bedtime check before honoring runtime suppression.
- `app/src/main/java/com/qualityalternative/app/data/PreferencesSettingsRepository.kt` - persisted bedtime settings.
- `app/src/main/java/com/qualityalternative/app/data/AccountLightProfile.kt` - Portable Profile bedtime export/import compatibility.
- `app/src/main/java/com/qualityalternative/app/ui/MainViewModel.kt` - bedtime active-window logic, unlock gating, analytics, and open-anyway behavior.
- `app/src/main/java/com/qualityalternative/app/ui/QualityAlternativeApp.kt` - settings UI and bedtime intervention UI.
- Relevant unit and Android tests under `app/src/test/...` and `app/src/androidTest/...`, including the GPT Pro R1-R7 suppression, boundary, duplicate-detection, and visual regressions.
- `evidence/sprint24_bedtime_hard_ban/` - README, prompt, diff, validation logs, E2E XML/logcat, and screenshots.

## Excluded

- Full Gradle build output, APKs, prior sprint review archives, and unrelated evidence directories.
- The bundle is scoped to the current Bedtime hard-ban slice rather than repository-wide archaeology.
