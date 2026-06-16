# Agent Inbox Live Review Gate

This gate applies to every change that touches Agent Inbox Drive folder selection, Google Drive authorization, package scan, package validation, priority handling, import, or release APK delivery.

## Required Live Evidence

A release candidate must provide an evidence directory containing:

- APK metadata with path, SHA-256, versionCode, versionName, and install timestamps.
- Emulator or device state showing the connected device and a signed-in Google account.
- Real Google Drive folder state showing the externally uploaded Agent Inbox package folder, `manifest.json`, and content file.
- Full live screenshots from fresh app install or cleared app data through:
  - first launch or onboarding
  - Settings Agent Inbox before folder selection
  - real Google account authorization
  - in-app Drive folder browser showing the target folder
  - scan result showing the package title from `manifest.json`
  - import result with queue cleared
  - Library `Files` view showing the imported document
  - reader showing imported content
- UIAutomator XML or equivalent machine-readable screen dumps for the same milestones.
- Logcat health sentinels proving there is no app crash and no old failure text such as `Package is missing manifest`, `Package could not be saved`, `Agent Inbox package could not be imported`, or `Google Drive authorization hit a Google Play services error`.
- Automated test results for the changed logic and a connected visual test for the Agent Inbox Drive picker states.

## Reviewer Rules

A reviewer must return FAIL, regardless of code quality, if any of these are true:

- The folder picker is only a pasted Drive link and no folder browser or working folder picker evidence is shown.
- The evidence does not come from a signed-in emulator or real device.
- The package was created by the app itself instead of by an external agent or external Drive upload path.
- The evidence stops at folder selection or scan and does not show import into Library and reader rendering.
- The APK hash in evidence does not match the APK being proposed for release.
- Screenshots show clipped, hidden, or unusable primary controls such as `Choose folder`, `Open`, `Select`, `Scan now`, or `Import`.
- Logcat contains app crash signatures or the old Agent Inbox failure strings listed above.

## Acceptable PASS

A 10/10 PASS is only valid when the reviewer cites the exact evidence paths and confirms:

- real Google account authorization occurred
- the target Drive folder was visible and selected in the app
- a package with `manifest.json` was found
- the title, format, topics, and priority rendered correctly
- import cleared the queue
- the imported document appeared in Library
- the reader rendered the imported file content
- automated tests passed
- the final APK hash is recorded
