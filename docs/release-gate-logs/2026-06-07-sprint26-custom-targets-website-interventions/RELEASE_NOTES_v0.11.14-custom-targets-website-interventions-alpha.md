# v0.11.14 Custom Targets Website Interventions Alpha

## Summary

This release finishes Sprint 26: Quality Alternative can now target eligible installed apps outside the original standard list, define supported Chrome website/domain rules, and carry those targets through soft, firm, and bedtime intervention flows without leaking private URL/package details into remote/export analytics payloads.

## Changes Since v0.11.13-code-review-hardening-alpha

- Added custom installed-app target selection in Settings, including search, eligibility filtering, self-exclusion, persistence, removal, and profile export/import support.
- Added website rule settings for Chrome verified-host domain interventions, including exact domain rules, wildcard subdomain rules with explicit apex toggle, pause/edit/delete controls, and browser support copy.
- Added Chrome verified-host intervention support that only intervenes when the visible Chrome host is readable, loaded, supported, and matches an enabled rule.
- Preserved safe behavior for stale, typed-but-not-loaded, unsupported, unreadable, synthetic, or package-mismatched browser states.
- Integrated custom app and website targets into bedtime behavior while preserving soft and firm mode semantics.
- Hardened the remote/export analytics boundary so URL, host, page title, package label, package id, and rule id are not emitted in remote-safe/exportable analytics payloads. Device-local analytics rows may still keep package-level fields needed for local intervention behavior.
- Updated Android release version to `versionCode=30`, `versionName=0.11.14-alpha`.

## Validation

- JVM/lint/build: PASS (`:app:testDebugUnitTest :app:lintDebug :app:assembleDebug`)
- Connected Android tests: PASS (`136` tests, `0` failures, `0` errors, `0` skipped)
- APK install on emulator: PASS
- APK signature verification: PASS
- Visual screenshot review: PASS

## APK

- Artifact: `release_artifacts/quality-alternative-v0.11.14-custom-targets-website-interventions-alpha-debug.apk`
- SHA-256: `0d863923fc39be5ef9032a13c1d312ed9ceca74ccb2130eb362e38b63bdf77bc`
