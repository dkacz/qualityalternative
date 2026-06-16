# Sprint 35 Agent Inbox Folder Selector Repair

Status: local release gate passed, GPT Pro R2 pending.

Opened: 2026-06-16

Trigger: after `v0.11.22-agent-inbox-readonly-link-fallback-alpha`, real-device feedback showed that the Google file Picker route was still not a reliable folder grant for externally populated/rclone Agent Inbox package folders. The user explicitly asked to repair the feature, not remove it.

Implementation:

- Removed Agent Inbox production authorization/routing through Google file Picker.
- Restored Android `OpenDocumentTree` as the primary folder selector.
- Local/system document-tree folders scan without a Google access token.
- Google Drive-backed document-tree folders require explicit readonly Drive authorization and scan only the selected folder id through the Drive API path.
- Existing `readonly_folder` grants continue through readonly scan/import.
- Existing legacy `picker_folder` grants are preserved only as repair state and route to reconnect/readonly repair instead of `drive.file` scan/import.
- Package authoring instructions now tell agents to create complete, validated package folders before upload and not to assume machine-specific rclone remotes, Google accounts, Drive folder ids, local paths, or tokens.

Validation:

- `testDebugUnitTest`
- `lintDebug`
- `assembleRelease`
- `assembleDebug`
- Connected visual E2E on `emulator-5554` / `qaApi36(AVD) - 16`: `VisualQaScreenshotTest#captureSprint35AgentInboxFolderSelectorRepairStates`
- Debug APK installed and launched on the emulator.

Artifacts:

- Debug APK: `release_artifacts/quality-alternative-v0.11.23-agent-inbox-folder-selector-repair-alpha-debug.apk`
- Release unsigned APK: `release_artifacts/quality-alternative-v0.11.23-agent-inbox-folder-selector-repair-alpha-release-unsigned.apk`
- Release logs: `docs/release-gate-logs/2026-06-16-sprint35-agent-inbox-folder-selector-repair/`
- Visual evidence: `evidence/sprint35_agent_inbox_folder_selector_repair/visual_e2e/`
