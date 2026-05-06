# Sprint 17 Reader Settings Sync Polish Release Validation

Release candidate: `v0.10.0-reader-settings-sync-polish-alpha`

Previous release: `v0.9.0-portable-profile-alpha`

Branch: `codex/sprint17-reader-sync-polish`

## GPT Pro Gates

- Slice 17.0 R2: `SCORE: 10/10`, `VERDICT: PASS`, `VISUAL REVIEW: PASS`
- Slice 17.1 R2: `SCORE: 10/10`, `VERDICT: PASS`, `VISUAL REVIEW: PASS`
- Slice 17.2 R4: `SCORE: 10/10`, `VERDICT: PASS`, `VISUAL REVIEW: PASS`
- Slice 17.3 R3: `SCORE: 10/10`, `VERDICT: PASS`, `VISUAL REVIEW: PASS`
- Slice 17.4 R20: `SCORE: 10/10`, `VERDICT: PASS`, `VISUAL REVIEW: PASS`
- Slice 17.5 R2: `SCORE: 10/10`, `VERDICT: PASS`, `VISUAL REVIEW: PASS`
- Slice 17.6 R2: `SCORE: 10/10`, `VERDICT: PASS`, `VISUAL REVIEW: PASS`

## Validation

- Full unit validation: `JAVA_HOME=/opt/homebrew/Cellar/openjdk@17/17.0.18 ./gradlew testDebugUnitTest`
- Full Android validation: `JAVA_HOME=/opt/homebrew/Cellar/openjdk@17/17.0.18 ./gradlew connectedDebugAndroidTest`
- Connected Android result: 102/102 tests passed on `qaApi36(AVD) - 16`
- Debug APK build: `JAVA_HOME=/opt/homebrew/Cellar/openjdk@17/17.0.18 ./gradlew assembleDebug`

## APK

- Asset: `quality-alternative-v0.10.0-reader-settings-sync-polish-alpha-debug.apk`
- Package: `com.qualityalternative.app`
- APK `versionCode`: `15`
- APK `versionName`: `0.10.0-alpha`
- SHA-256: `581c3dfd69b54add1e74438b88a5ee20fdea180672bdf790a0ebcc0401ebfde9`

## Signature And Install

- `apksigner verify --verbose --print-certs` passed.
- APK Signature Scheme v2: `true`
- Signer: Android Debug certificate
- Emulator install smoke: `adb install -r app/build/outputs/apk/debug/app-debug.apk` returned `Success`.
- Installed package reported `versionCode=15`, `versionName=0.10.0-alpha`.

## Changelog Versus `v0.9.0-portable-profile-alpha`

- Settings now uses compact numeric reading text controls with live preview and separate interface text scaling.
- Annotation sync and Portable Profile backup now have clear local-first defaults and clearer Settings information architecture.
- Google Drive authorization was repaired so the connected Drive state can be reached and annotation sync can write through the Drive path.
- Reader pagination now adapts to real viewport dimensions, safe insets, reader text size, and reader chrome to reduce avoidable blank bottom space.
- Annotation start/end controls are source-anchored across pages and compact enough to stay inside the annotation header.
- Long selected quotes and long notes now stay within a bounded annotation sheet, with internal scrolling and visible Save/Cancel above the keyboard.
