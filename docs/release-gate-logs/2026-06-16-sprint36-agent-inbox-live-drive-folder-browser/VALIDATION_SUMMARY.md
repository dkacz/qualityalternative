# Sprint 36 Agent Inbox Live Drive Folder Browser Release Gate

Date: 2026-06-16

Release candidate:

- Version: `0.11.24-alpha`, `versionCode=40`
- Debug APK: `release_artifacts/quality-alternative-v0.11.24-agent-inbox-live-drive-folder-browser-alpha-debug.apk`
- Debug SHA-256: `96fc0011e3ce192897da4750d83497244fa97fcc4c924e9b49191199d6dddb54`
- Release unsigned APK: `release_artifacts/quality-alternative-v0.11.24-agent-inbox-live-drive-folder-browser-alpha-release-unsigned.apk`
- Release unsigned SHA-256: `0054437d86962e7abba49391dd7a514894f404ed0720d8e92e1a995a85ec66a9`

Release decision:

- GPT Pro R2 returned `SCORE: 10/10`, `VERDICT: PASS`, `BLOCKERS: None`.
- GPT Pro R2 response: `evidence/sprint36_agent_inbox_live_picker_e2e/gpt_pro_review_response_r2.md`.
- GPT Pro R2 URL: `https://chatgpt.com/c/6a318b65-c210-83eb-9af7-c86ab9c4afc4`.
- The reviewed live evidence proved a signed-in emulator, Google Play Services account authorization, real Drive folder browser selection, package scan, import, Library visibility, and reader rendering.

Validation:

- `testDebugUnitTest`, `lintDebug`, `assembleRelease`, and `assembleDebug`: PASS (`final_gradle_build.log`, `final_gradle_build.status.txt`).
- Debug APK badging: PASS (`apk_debug_badging.txt`) with `versionCode='40'`, `versionName='0.11.24-alpha'`.
- Release unsigned APK badging: PASS (`apk_release_unsigned_badging.txt`) with `versionCode='40'`, `versionName='0.11.24-alpha'`.
- Debug APK signature verification: PASS (`apk_debug_signature_verify_verbose.txt`).
- Emulator install: PASS (`adb_install_debug.status.txt`).
- Installed package readback: PASS (`dumpsys_package_after_install.txt`) with `versionCode=40`, `versionName=0.11.24-alpha`.
- Direct launch after install: PASS (`adb_direct_launch_after_install.txt`).

Live evidence carried from the Pro gate:

- Live evidence report: `evidence/sprint36_agent_inbox_live_picker_e2e/LIVE_E2E_REPORT.md`.
- Live screenshots: `evidence/sprint36_agent_inbox_live_picker_e2e/live_e2e/`.
- Final connected visual screenshots: `evidence/sprint36_agent_inbox_live_picker_e2e/visual_e2e/sprint35-agent-inbox-folder-selector-repair-1781630925520/`.
- Real Drive package: `QA-Agent-Inbox-Live-E2E-20260616-173729`, with package `codex-live-drive-e2e-package`.

Note:

- The GPT Pro live review was run on the same code before the final release version bump. The post-review code change is limited to Android version metadata (`versionCode=40`, `versionName=0.11.24-alpha`), followed by a fresh local build, install, version readback, and launch gate.
