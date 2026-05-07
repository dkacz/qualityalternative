# Sprint 18 Reader Progress Hotfix Release Validation

Release candidate: `v0.11.1-reader-progress-hotfix-alpha`

Previous release: `v0.11.0-gdrive-reader-fit-alpha`

Branch: `codex/sprint18-gdrive-selection-regression-fixes`

## GPT Pro Gate

- Sprint 18 progress hotfix R3: `SCORE: 10/10`, `VERDICT: PASS`, `VISUAL REVIEW: PASS`
- R3 confirmed source-anchored Reader progress, stable percent after font repagination, no technical annotation selection copy in the popup, sufficient connected visual evidence, and clean package hygiene.

## Validation

- Unit validation:
  `JAVA_HOME=/opt/homebrew/opt/openjdk@17 ./gradlew :app:testDebugUnitTest --rerun-tasks`
- Release build:
  `JAVA_HOME=/opt/homebrew/opt/openjdk@17 ./gradlew :app:testDebugUnitTest :app:assembleDebug`
- Targeted connected Android evidence from the hotfix review: 2/2 tests passed on `qaApi36(AVD) - 16`
- Debug APK build: PASS

## APK

- Asset: `quality-alternative-v0.11.1-reader-progress-hotfix-alpha-debug.apk`
- Package: `com.qualityalternative.app`
- APK `versionCode`: `17`
- APK `versionName`: `0.11.1-alpha`
- SHA-256: `5ec7fa54fdefc2aaa15c87fbaf8b57546d04d1a371f955a4ab541a50c5062b26`

## Signature And Install

- `apksigner verify --verbose --print-certs` passed.
- APK Signature Scheme v2: `true`
- Signer: Android Debug certificate
- Emulator install smoke: `adb install -r release_artifacts/quality-alternative-v0.11.1-reader-progress-hotfix-alpha-debug.apk` returned `Success`.
- Installed package reported `versionCode=17`, `versionName=0.11.1-alpha`.
- Emulator was shut down immediately after install/version smoke to free RAM.

## Changelog Versus `v0.11.0-gdrive-reader-fit-alpha`

- Reader progress percent is now source anchored, so changing Reader text size does not move the saved percent when pagination changes.
- The progress hotfix preserves source paragraph and text offset through save, restore, Account Light export, and Account Light import paths already reviewed in R3.
- The annotation popup no longer shows the unwanted technical `Selection block ... steps ...` copy.
- Visual evidence covers default-font saved progress, large-font restored progress, compact annotation controls, long quote scrolling, and reopened cross-page quote state.
