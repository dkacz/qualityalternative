# Sprint 25 Final Release Validation

Status: ready for GitHub release as `v0.11.12-markdown-media-tables-alpha`.

## Review Gate

- GPT Pro R3: `SCORE: 10/10`, `VERDICT: PASS`, `VISUAL REVIEW: PASS`, blockers `None`.
- Review file: `evidence/sprint25_markdown_media_tables/pro_review_harvest_r3/GPT_PRO_REVIEW_R3.md`.
- Visual evidence: `evidence/sprint25_markdown_media_tables/screenshots-r3/contact_sheet_r3.png`.

## Final Build Gate

- PASS: `:app:testDebugUnitTest`
- PASS: `:app:lintDebug`
- PASS: `:app:assembleDebug`
- Final Gradle log: `docs/release-gate-logs/2026-05-31-sprint25-markdown-media-tables/final_gradle_build.log`

## APK

- Version: `versionCode=28`, `versionName=0.11.12-alpha`
- Artifact: `release_artifacts/quality-alternative-v0.11.12-markdown-media-tables-alpha-debug.apk`
- SHA-256: `827238fd2965f4863161b17e54bf27831d84b3ddb6c0203e6a1e85fa14b99959`
- Signature verification: `docs/release-gate-logs/2026-05-31-sprint25-markdown-media-tables/apk_signature.txt`
- Badging: `docs/release-gate-logs/2026-05-31-sprint25-markdown-media-tables/apk_badging.txt`

## Install And Launch Smoke

- PASS: emulator install via `adb install -r`.
- PASS: installed package reports `versionCode=28` and `versionName=0.11.12-alpha`.
- PASS: `am start -n com.qualityalternative.app/.MainActivity` focuses `MainActivity`.
- Launch smoke screenshot: `docs/release-gate-logs/2026-05-31-sprint25-markdown-media-tables/launch_smoke_loaded.png`.
