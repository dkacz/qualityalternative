# Sprint 26 - Custom Targets And Website Interventions

## Problem

The app started with a narrow standard distracting-app list. Real alpha use needs a broader but still replacement-first target model:

- users need to add personally distracting installed apps outside the standard suggestions,
- browser use can bypass app-level intervention when the habit is a website rather than a standalone app,
- website support must be honest about Android/browser limits,
- privacy must be stronger than ordinary browsing-history collection.

## Product Constraints

- `PRD.md` remains the source of truth.
- Soft intervention remains the default behavior.
- Firm mode and opt-in Bedtime protection keep their existing semantics.
- Reading, meditation, and bounded backup alternatives remain available during interventions.
- Sprint 26 adds website interventions only where Android/browser state exposes a verified current host through a tested adapter.
- Sprint 26 does not add universal URL blocking, parental control, browser extensions, VPN/DNS/proxy filtering, packet inspection, browsing-history collection, open-web crawling, AI categorization of visited sites, or default hard-block behavior.
- Remote analytics must not contain raw URLs, hosts, domains, paths, queries, page titles, URL-bar text, non-matching URL observations, browsing-history rows, or domain-derived hashes.

## PRD Mapping

- `FR1. Distracting App Selection`: extend from standard suggestions to eligible user-launchable installed apps.
- `FR1A. Supported Website Target Rules`: add exact-domain and wildcard-subdomain website rules for supported browsers.
- `FR2. Permission and Interception Setup`: make custom app and browser website reliability visible.
- `FR6. Intervention UI`: keep the same finite, calm intervention surface for custom app and website targets.
- `FR7. Core User Actions`: custom app and website interventions must respect Soft, Firm, and Bedtime behavior.
- `FR12. Analytics Instrumentation`: log target type, support status, mode, and outcome without recording browsing history.
- `FR13. Portable Profile`: export/import user-authored rules without copying permission state, browser support state, or private observations.
- `NFR1. Response Time`: preserve the two-second intervention target for detected app or verified-host website opens under normal conditions.
- `NFR3. Reliability Signaling`: never claim website protection is active for a browser where the current host cannot be verified.
- `NFR4. Local-First Portability And Privacy`: keep website matching local-first and user-authored.

## TargetRule Model

All intervention targets are represented as explicit user-controlled rules:

| Field | Purpose |
| --- | --- |
| `rule_id` | Local opaque id; remote analytics may use only a non-reversible event-local id. |
| `target_type` | `standard_app`, `custom_app`, `domain`, or `wildcard_domain`. |
| `enabled` | User-controlled active state. |
| `source` | `system_suggestion` or `user`. |
| `created_at` / `updated_at` | Local profile metadata. |
| `payload` | Type-specific payload stored locally and in Portable Profile when safe. |

Type-specific payloads:

- `standard_app`: canonical suggested target id and package id.
- `custom_app`: package id and last known user-visible label.
- `domain`: normalized exact host, stored only as explicit user-authored configuration.
- `wildcard_domain`: normalized base host and apex-inclusion flag.

Imported rules never import permission state, browser support state, protection-active state, raw URL observations, browsing history, or Android-only URI grants.

## Custom App Eligibility

The user-facing feature is "add any eligible app," not literally any package on the device.

Eligible custom targets:

- are user-launchable installed apps,
- are not already selected as standard suggested targets unless deduplicated,
- can be resolved by package id on the current device,
- can safely enter the existing replacement-first intervention flow.

Excluded custom targets:

- Quality Alternative itself,
- launchers and home apps,
- Android Settings and permission flows,
- accessibility settings,
- phone, emergency, dialer, call, and safety flows,
- package installers and app stores during install/update flows,
- System UI and other system-critical packages,
- any package the app cannot safely identify as user-launchable.

Excluded packages appear disabled with clear copy rather than disappearing silently.

## Website Matching Contract

Sprint 26 supports exact-domain and wildcard-subdomain interventions only.

Domain normalization:

- strip scheme, username/password, port, path, query, and fragment,
- lowercase the host,
- handle trailing dots consistently,
- handle IDNA/punycode consistently,
- reject empty, ambiguous, private, or invalid input,
- reject substring matching.

Matching semantics:

- `example.com` matches only `example.com`.
- `example.com` does not match `notexample.com` or `example.com.evil.example`.
- `*.example.com` matches subdomains of `example.com`.
- The UI must explicitly state whether `*.example.com` includes the apex `example.com`; the default is to include it only when the user enables "include main domain."

Full URL/path rules are deferred. They may be revisited only after a browser adapter proves reliable full URL visibility across navigation, reload, foreground/background, collapsed address bar, tab switch, custom tab, and PWA states.

## Browser Support Matrix Contract

Implementation must maintain a visible support matrix with at least these fields:

| Browser package | Adapter source | Support level | Can read host | Can read full path | Unsupported states | Test coverage | User-facing status | Fallback |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| `com.android.chrome` | Verified address-bar URL/host node only | Domain intervention candidate | Yes, when verified | No for Sprint 26 | Incognito if hidden, custom tabs unless proven, PWAs unless proven, collapsed/hidden/unreadable URL, stale window state | Required E2E | Supported for domain rules when host is readable | Whole-browser app target |
| Other browsers | Adapter absent until proven | Unavailable | No | No | All website-rule states | Status screenshot required | Website rules unavailable in this browser | Whole-browser app target |

Rules:

- Do not match from page title, body text, search snippets, notifications, autocomplete suggestions, visible page content, or arbitrary accessibility text.
- Fire a website intervention only from a current verified host observation for the active browser window.
- Never reuse the last observed URL/host when the current browser state is unreadable, stale, hidden, or unsupported.
- Unsupported browsers must show truthful degraded/unavailable status and offer whole-browser app intervention as the reliable fallback.

## Privacy And Analytics Contract

Allowed remote analytics fields:

- `target_type`,
- `rule_type`,
- `browser_support_status`,
- `intervention_mode`,
- `intervention_outcome`,
- coarse timing/session metrics,
- non-reversible event-local rule reference where needed.

Denied remote analytics/log fields:

- raw URL,
- host/domain,
- path/query/fragment,
- page title,
- URL-bar text,
- visible browser text,
- non-matching observations,
- browsing-history rows,
- domain-derived hashes,
- stable cross-device website identifiers.

Tests must inspect analytics payloads and logs to prove URL/domain/title strings do not leak.

## Scope

- Add custom eligible installed-app selection alongside standard distracting-app suggestions.
- Persist custom app targets as portable rules with package id and last known display label.
- Add exact-domain and wildcard-subdomain website rules in Settings.
- Add Chrome-first verified-host website intervention evidence.
- Add truthful unsupported-browser status and whole-browser app fallback.
- Use the existing intervention flow for app and website targets.
- Keep Bedtime emergency unlock opt-in and preserve alternatives.
- Add visual and automated evidence for all critical states.

## Out Of Scope

- Browser extensions.
- VPN, DNS, local proxy, or packet-inspection filtering.
- Open-web crawling or AI categorization of browsing.
- Parental-control or team/accountability features.
- Full URL/path rules.
- Full sub-surface rules such as Reels-only or Shorts-only.
- Capturing, exporting, or remotely logging browsing history.
- Silent denial of the original app outside the existing opt-in Bedtime emergency-unlock flow.

## Slices

### Slice 26.0 - Contract And Architecture Review

- Patch `PRD.md` to authorize eligible custom app targets and supported-browser domain interventions.
- Define the rule model, custom-app eligibility list, domain matching semantics, browser support matrix, privacy allowlist/denylist, and E2E evidence contract.
- Run GPT Pro plan review until `SCORE 10/10` and `VERDICT PASS`.

Acceptance:

- GPT Pro plan review returns `SCORE 10/10` and `VERDICT PASS`.
- `VISUAL REVIEW` is `NOT APPLICABLE` for plan-only review; visual review becomes mandatory once UI screenshots exist.
- Plan identifies current support as Chrome-first verified-domain support only.
- Plan states fallback behavior for unsupported browsers.
- Plan blocks implementation until PRD, scope, privacy, and evidence issues are resolved.

### Slice 26.1 - Custom App Target Vertical

- Let users add eligible installed apps outside the standard list.
- Separate standard suggestions from custom added apps.
- Support search, disabled/excluded states, package display, remove/edit, and persistence.
- Integrate selected custom app targets with the existing app-intervention flow.

Acceptance:

- A safe non-standard installed app can be selected, saved, removed, and restored after app restart.
- Excluded packages are disabled with clear copy.
- Standard suggestions remain intact and deduplicate against custom targets.
- Opening a selected custom app triggers the existing intervention.
- Opening an unselected app does not trigger the intervention.
- Soft, Firm, Pause 15 min, unlock windows, and Bedtime behavior match existing app-intervention rules.
- Portable Profile includes custom app target rules, resolves them locally on import, and marks missing packages unavailable.
- Tests cover package eligibility, persistence, import merge/replace, deduplication, matching priority, quiet windows, and mode behavior.
- Visual evidence captures picker search, excluded package state, selected custom app Settings state, persistence after restart, intervention from custom app, and unselected app non-trigger evidence.

### Slice 26.2 - Website Rule Model And Settings UI

- Add website rules in Settings as a distinct section from app targets.
- Support exact-domain and wildcard-subdomain rules only.
- Validate, normalize, enable/disable, edit, and delete rules.
- Add browser support matrix/status UI before any website rule can claim protection.

Acceptance:

- Users can add, edit, disable, and delete domain rules.
- Invalid and ambiguous rules show clear inline validation.
- Domain normalization and wildcard semantics are visible enough for users to understand.
- Rules are stored locally and exported in Portable Profile without browser support state.
- Path/full URL rule UI is absent or explicitly marked deferred/unavailable.
- Visual evidence captures add/edit/delete/disable states, invalid input, wildcard apex toggle, and browser support status.
- Tests cover normalization, IDNA/punycode handling, trailing dots, substring rejection, wildcard semantics, persistence, and Portable Profile import.

### Slice 26.3 - Chrome Verified-Host Website Intervention

- Implement Chrome-first website matching through a verified address-bar URL/host source only.
- Trigger the existing intervention when Chrome exposes a current verified host matching an enabled rule.
- Add stale/unreadable state protection.
- Add unsupported-browser fallback behavior.

Implementation status:

- Implemented locally on branch `codex/sprint26-custom-targets-website-interventions`.
- Chrome support is limited to package `com.android.chrome` and whitelisted address-bar accessibility node ids.
- The AccessibilityService listens for window state/content changes, not text-entry changes, to avoid firing from a typed-but-not-loaded URL.
- Website interventions use `Chrome website` as the visible target and keep a separate non-domain suppression key for Open Anyway.
- Analytics metadata is limited to target type, browser package, support status, and rule type; raw URL, host/domain, path/query, page title, URL-bar text, and domain-derived hashes remain denied.
- R2 fixed GPT Pro R1 blockers by updating Chrome support copy, adding hidden/focused adapter state, increasing real Chrome toolbar scan depth, proving website-suppression fallback, and adding real Chrome package/version evidence.
- R3 fixes the GPT Pro R2 privacy blocker by removing replacement `externalUrl` from shared content analytics metadata and adding a website-domain regression that proves a replacement link with host, path, and query does not leak into intervention, accept, or fallback-open analytics.
- R4 packages the R3 PASS/PASS implementation with full unit XML and standalone activity, analytics, repository, and model source files for final GPT Pro scoring.
- R5 shipped all `app/src` source/test files plus full unit/lint evidence and returned `SCORE 9/10`, `VERDICT PASS`, `VISUAL REVIEW PASS`, with no release blockers but with evidence gaps.
- R6 closes the R5 evidence gaps with schemed Unicode host normalization, larger Chrome accessibility scan budget, fresh connected 4/4 test evidence, live AccessibilityService-to-intervention proof, connected unsupported/unreadable negative evidence, device/API/Chrome version proof, and raw `git diff --check` output.
- R7 fixes the GPT Pro R6 package-mismatch blocker by adding package identity to browser snapshots, requiring root and address-bar node package authentication, and rerunning unit, connected, visual, and live-service evidence after the change.
- Evidence: `evidence/sprint26_custom_targets_website_interventions/SLICE26_3_R7_EVIDENCE.md`.
- GPT Pro R7 review: `SCORE 10/10`, `VERDICT PASS`, `VISUAL REVIEW PASS`; no blockers or release-blocking bundle gaps remain.

Acceptance:

- Chrome E2E shows a matching domain rule triggering an intervention.
- Chrome E2E shows a non-matching domain not triggering an intervention.
- Negative tests prove `notreddit.com` and `reddit.com.evil.example` do not match `reddit.com`.
- Unreadable, hidden, stale, custom-tab, PWA, incognito, or unsupported states do not trigger from a prior URL.
- Unsupported browsers show website rules unavailable/degraded status and offer whole-browser app intervention.
- Website interventions preserve Soft, Firm, Pause 15 min, unlock windows, and Bedtime behavior.
- No full browsing history is persisted.
- Visual evidence captures Chrome supported intervention, Chrome non-match, unreadable/stale fallback, unsupported-browser status, and whole-browser fallback.
- Test evidence records emulator/device model, Android API level, Chrome package/version, browser support status, test URL set, and raw test output path.

### Slice 26.4 - Privacy, Analytics, And Portable Profile Hardening

- Enforce analytics allowlist and denylist for website and custom app targets.
- Add log-scrubbing checks.
- Complete Portable Profile behavior for imported app and website rules.
- Implementation started after Slice 26.3 commit `8fdd20e`.
- Add `AnalyticsPrivacyGuard` as the remote/export analytics boundary: local analytics may keep package data needed for device-local behavior, but exported/remote payloads must collapse targets to classes and drop packages, URLs, hosts, page titles, URL-bar text, paths, queries, and website rule ids.
- Extend Portable Profile tests so website rules export without Chrome/browser support state and missing custom app warnings do not reveal private package names.
- Evidence bundle: `evidence/sprint26_custom_targets_website_interventions/SPRINT26_SLICE26_4_REVIEW_BUNDLE_20260607.zip`.
- GPT Pro review lane: `https://chatgpt.com/c/6a25ae84-1310-83eb-a53a-1128d4a7edd1`; heartbeat `harvest-sprint-26-slice26-4-gpt-pro-review`.
- GPT Pro R1 review: `SCORE 8/10`, `VERDICT FAIL`, `VISUAL REVIEW NOT APPLICABLE`.
- R2 fixes R1 blockers: unknown `targetType` cannot echo into `targetClass`, IP/port/trailing-dot/Unicode host variants are rejected, top-level remote fields are included in unsafe-field diagnostics, and remote-safe debug summaries use the scrubber.
- R2 evidence: `evidence/sprint26_custom_targets_website_interventions/SLICE26_4_R2_EVIDENCE.md`.
- GPT Pro R2 review lane: `https://chatgpt.com/c/6a25b565-7d28-83ed-bc8a-de6a19da9613`; heartbeat `harvest-sprint-26-slice26-4-r2-gpt-pro-review`.
- GPT Pro R2 review: `SCORE 9/10`, `VERDICT FAIL`, `VISUAL REVIEW NOT APPLICABLE`.
- R3 fixes R2 blocker: punycode/IDNA host-like values with punycode TLDs and trailing-dot IPv4 literals are rejected through payload conversion, scrubber, and top-level diagnostics.
- R3 evidence: `evidence/sprint26_custom_targets_website_interventions/SLICE26_4_R3_EVIDENCE.md`.
- GPT Pro R3 review lane: `https://chatgpt.com/c/6a25bb13-7590-83ed-bbf4-8c84ac527bc0`; heartbeat `harvest-sprint-26-slice26-4-r3-gpt-pro-review`.
- GPT Pro R3 review: `SCORE 10/10`, `VERDICT PASS`, `VISUAL REVIEW NOT APPLICABLE`; no blockers, bundle gaps, or package hygiene issues remain.

Acceptance:

- Remote analytics payloads contain no raw URL, host/domain, path/query, page title, URL-bar text, non-match observations, browsing-history rows, or domain-derived hashes.
- Website rule event ids are non-reversible and not stable cross-device identifiers.
- Debug logs, crash breadcrumbs, screenshot labels, and review artifacts avoid raw URL/domain/title strings unless deliberately included as user-authored setup evidence in local test docs.
- Custom app analytics do not leak unnecessary package metadata; if package id is needed locally, remote analytics use target type and rule class instead.
- Profile import re-resolves custom app packages and website browser support locally.
- Tests inspect analytics payloads and logs for absence of forbidden strings.

### Slice 26.5 - Bedtime And Supported Website Target Integration

- Ensure custom app and website targets respect opt-in Bedtime emergency-unlock behavior.
- Preserve reading, meditation, and bounded alternatives.
- Keep unavailable browser states truthful during Bedtime.
- Implementation/evidence: `evidence/sprint26_custom_targets_website_interventions/SLICE26_5_EVIDENCE.md`.
- R1 GPT Pro review lane: `https://chatgpt.com/c/6a25c47f-21d0-83eb-ace6-66cb604c351e`.
- R1 GPT Pro review: `SCORE 8/10`, `VERDICT FAIL`, `VISUAL REVIEW PASS`.
- R1 blocker: package/evidence gap for launch provenance; the bundle did not include enough source and live-service proof to disprove forged/non-verified website intents.
- R2 fixes the R1 blocker with:
  - `MainActivityTest#forgedWebsiteInterceptionIntentWithoutLaunchTokenIsIgnored`,
  - external live Chrome AccessibilityService Bedtime website E2E evidence,
  - `MainActivity.kt`, `AndroidManifest.xml`, `AnalyticsPrivacyGuard.kt`, and prior Slice 26.1 R4 artifacts in the review bundle,
  - scrubbed lint artifacts for package hygiene.
- R2 visual evidence: `evidence/sprint26_custom_targets_website_interventions/visual_e2e_slice26_5_r2/sprint26_slice26_5_r2_bedtime_website_live_contact_sheet.png`.
- R2 validation: 443 debug unit tests passed, lint passed with 0 errors, connected tests passed 2/2 on `qaApi36`, external live-service E2E passed, and `git diff --check` passed.
- R2 GPT Pro review lane: created after bundle export; the exact active URL is tracked in `docs/LANE_STATUS.md` after send.
- R2 active GPT Pro lane after send: `https://chatgpt.com/c/6a25ce86-3f54-83eb-8547-940e2d94ecf8`.
- R2 GPT Pro review: `SCORE 10/10`, `VERDICT PASS`, `VISUAL REVIEW PASS`; no blockers, bundle gaps, privacy issues, or package hygiene issues remain.

Acceptance:

- Bedtime applies to selected custom app targets with existing emergency unlock behavior.
- Bedtime applies to supported Chrome domain targets only when the active host is verified.
- Alternatives remain visible and usable.
- Pause 15 min is hidden during Bedtime.
- Soft/Firm behavior outside Bedtime remains unchanged.
- Visual evidence captures Bedtime custom app flow, Bedtime website flow, alternatives, and emergency unlock wait.

### Slice 26.6 - Final E2E, Evidence, And Release

- Run targeted unit tests, lint, instrumentation tests, and visual screenshot tests.
- Run GPT Pro final review with bundle hygiene and visual evidence.
- Iterate until `SCORE 10/10`, `VERDICT PASS`, and `VISUAL REVIEW PASS`.
- Build, verify, and release the APK.

Acceptance:

- Final review has no blockers, unresolved bundle gaps, package hygiene issues, privacy concerns, or visual concerns.
- Release is blocked by any URL-support overclaim, privacy leak, or replacement-first regression.
- Installable debug APK is built and signature/install are verified.
- GitHub release changelog compares against the previous release.

## Evidence Contract

Each implementation slice must preserve:

- prompt file,
- review bundle or manifest,
- harvested GPT Pro response,
- screenshot contact sheet for visual UI changes,
- automated test output,
- emulator/device model and Android API level,
- browser package/version for website evidence,
- explicit supported/unsupported browser status,
- package hygiene notes.

Required custom-app screenshots:

- picker search,
- excluded package disabled state,
- selected custom app Settings state,
- persistence after restart,
- intervention triggered by safe non-standard app,
- unselected app non-trigger evidence,
- Soft/Firm/Bedtime variants.

Required website screenshots:

- domain-rule add/edit/delete/disable validation,
- wildcard apex toggle,
- browser support matrix/status,
- Chrome matching-domain intervention,
- Chrome non-matching-domain non-intervention evidence,
- unreadable/stale URL fallback,
- unsupported-browser status,
- whole-browser app fallback,
- Bedtime supported-website flow.

Required negative evidence:

- no trigger for `notreddit.com` when `reddit.com` is configured,
- no trigger for `reddit.com.evil.example`,
- no trigger from stale previously observed hosts,
- no analytics/debug artifact containing forbidden URL/domain/title strings.

## Open Risks For GPT Pro Review

- Android accessibility may not expose a reliable current host for every Chrome state.
- Browser adapters can become fragile across browser versions.
- Custom app selection can drift toward generic blocking if the intervention defaults are weakened.
- Portable Profile must preserve user-authored rules without implying permission or browser-support transfer.
- Analytics must remain useful without recording private browsing history.
