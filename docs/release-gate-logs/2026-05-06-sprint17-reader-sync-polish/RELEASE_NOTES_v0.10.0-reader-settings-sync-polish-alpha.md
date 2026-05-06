# Quality Alternative v0.10.0-reader-settings-sync-polish-alpha

This release closes the Sprint 17 real-device polish pass for Settings, Google Drive sync, reader pagination, and annotation editing.

## What Changed

- Replaced the oversized Reader text chooser with compact plus/minus controls and a live reading preview.
- Added separate interface text sizing so reader typography and app chrome can be tuned independently.
- Made Annotation sync and Portable Profile backup local-first by default, with clearer labels and destination status.
- Repaired Google Drive authorization and retry handling for annotation sync.
- Improved reader pagination fit across viewport sizes and reader text scales.
- Added source-anchored cross-page annotation range selection with compact arrow controls.
- Improved the annotation note editor so long quotes and long notes scroll inside the sheet while Save/Cancel remain reachable above the keyboard.

## Validation

- Unit tests: PASS
- Connected Android tests: 102/102 PASS on `qaApi36(AVD) - 16`
- APK build: PASS
- APK signature verification: PASS, Android Debug certificate, v2 signature
- Emulator install smoke: PASS

## APK

- File: `quality-alternative-v0.10.0-reader-settings-sync-polish-alpha-debug.apk`
- Package: `com.qualityalternative.app`
- Version code: `15`
- Version name: `0.10.0-alpha`
- SHA-256: `581c3dfd69b54add1e74438b88a5ee20fdea180672bdf790a0ebcc0401ebfde9`
