# Sprint 16 Portable Profile Release Validation

Release candidate: `v0.9.0-portable-profile-alpha`

Branch: `codex/account-light-profile`

Commits included after `v0.8.1-reader-swipe-alpha`:

- `f504e85` Define Account Light profile contract
- `ce268f9` Implement Account Light settings export
- `615d262` Implement Portable Profile settings import
- `36cf7a6` Implement Portable Profile library state
- `4f9b9ec` Implement Portable Profile autosave destination
- `c106d05` Implement adaptive reader pagination
- `0d59821` Harden Sprint 16 final release gate
- `d179036` Align portable content id import contract
- `cba2f4a` Fix portable profile secondary-key import
- `06bb192` Complete Sprint 16 release gate fixes
- `8295636` Bump Android version for Sprint 16 release

## GPT Pro Gate

- Sprint 16 Final Release Gate R6: `SCORE: 10/10`, `VERDICT: PASS`, `VISUAL REVIEW: PASS`
- Harvested file: `PRO_REVIEW_OUTPUT_SPRINT16_FINAL_RELEASE_GATE_R6_20260505_155437/Sprint_16_R6_Review.md`

## Validation

- Full unit validation: `JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home ./gradlew testDebugUnitTest`
- Full Android validation: `JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home ./gradlew connectedDebugAndroidTest`
- Connected Android result: 97/97 tests passed on `qaApi36(AVD) - 16`
- Debug APK build: `JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home ./gradlew assembleDebug`

## APK

- Asset: `quality-alternative-v0.9.0-portable-profile-alpha-debug.apk`
- Package: `com.qualityalternative.app`
- APK versionCode: `14`
- APK versionName: `0.9.0-alpha`
- SHA-256: `9c25dbbfacecfac450dcdfac16ad0993cbd20ac25fc2ce06e689dc9ed1503c07`

## Signature And Install

- `apksigner verify --verbose --print-certs` passed.
- APK Signature Scheme v2: `true`
- Signer: Android Debug certificate
- Emulator install smoke: `adb install -r app/build/outputs/apk/debug/app-debug.apk` returned `Success`.
- Installed package reported `versionCode=14`, `versionName=0.9.0-alpha`.
