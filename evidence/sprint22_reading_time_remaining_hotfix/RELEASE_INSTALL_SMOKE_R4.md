# Release Install Smoke R4

Artifact: `apk_r4/sprint22-reading-time-hotfix-release-debugsigned.apk`

Purpose: prove the release build can be signed, installed, and launched as an installable GitHub alpha APK.

Result:

- `apksigner verify`: PASS with APK Signature Scheme v3.
- `adb install -r`: `Success`.
- `adb shell am start -W -n com.qualityalternative.app/.MainActivity`: `Status: ok`, `LaunchState: COLD`.
- `adb shell pidof com.qualityalternative.app`: returned a running process id.
- Screenshot: `screenshots/release_install_smoke_r4.png`.

Note: the smoke command first attempted `adb uninstall com.qualityalternative.app || true` to clean state. That cleanup command returned `DELETE_FAILED_INTERNAL_ERROR`, but the subsequent `adb install -r` and launch both succeeded and are the pass/fail criteria for this smoke check.
