SCORE: 6/10
VERDICT: FAIL
VISUAL REVIEW: NOT APPLICABLE

BLOCKERS:

The plan is not implementation-ready because the required PRD deltas have not yet been made. PRD.md currently frames MVP interception around a supported distracting-app list and does not define arbitrary installed-app targets or browser website/domain rules. docs/SPRINT_26_CUSTOM_TARGETS_WEBSITE_BLOCKING.md correctly says Slice 26.0 must add explicit PRD deltas, but implementation must not begin until those deltas are actually written.

Browser URL detection is under-specified. The plan says Chrome-first support, unsupported-browser signaling, and no universal browser claim, but it does not define the browser support matrix, adapter proof contract, URL source, failure modes, stale-URL handling, or exact fallback behavior.

URL/path matching is not safe to approve as written. The plan still allows “supported URL patterns” and a “full URL/path rule type” without proving a reliable Android/browser API source for full current URLs. Sprint 26 should be limited to domain/host matching from a verified browser URL field unless a browser adapter can prove full URL visibility under explicit tested conditions.

The custom “any installed app” path lacks safety exclusions. The plan needs to exclude the Quality Alternative package itself, launchers, Android Settings and permission screens, emergency/phone flows, package installers, system UI, and other system-critical packages to avoid interception loops, degraded setup, or accidental hard-control behavior.

Privacy analytics are not sufficiently constrained. “Only user-authored rule ids/types and intervention outcomes” is still too ambiguous for website rules because a stable rule id tied to a domain can become browsing-history metadata when logged with timestamps. Remote analytics must not contain raw URLs, hosts, domains, page titles, URL-bar text, non-matching visits, stale URL candidates, or domain-derived hashes unless a stricter local-only design is documented.

Portable Profile behavior is incomplete for imported custom app and website rules. The plan says not to copy permission state or browsing history, but it does not specify that imported app packages must be resolved locally, unavailable apps must be marked inactive/unavailable, and imported website rules must not imply browser support on the new device until local permissions and browser adapters are verified.

PRD / SCOPE FIT:

The custom app picker is directionally compatible with the broader thesis only if it remains a replacement target selector rather than a generic blocker. The PRD’s core product decision is “replacement engine, not a blocker,” with Soft intervention, bounded alternatives, and clear override behavior. Sprint 26 mostly preserves that language, but the title, lane wording, and “blocking/full ban” terms create avoidable scope drift.

Arbitrary installed-app targets require a PRD update before implementation. PRD.md currently names a supported launch list: Instagram, X, YouTube, Facebook, Reddit, TikTok. Sprint 26 changes that to “any installed app,” which is a material expansion of FR1 and onboarding assumptions.

Website/domain targets also require a PRD update before implementation. The PRD does not currently define website interception as an MVP surface, and it explicitly excludes broad edge-case interception workflows, browser extensions, parental-control scope, and hard-block default behavior.

The PRD update should add website targets as “supported-browser website interventions,” not “universal URL blocking.” It should state that website rules operate only where Android/browser UI state exposes a verifiable current host and that unsupported browsers fall back to truthful status plus optional whole-browser app intervention.

Bedtime protection remains compatible only if it stays opt-in, preserves reading/meditation/bounded alternatives, hides Pause 15 min during Bedtime, and uses the existing visible emergency unlock wait rather than silent hard denial.

SLICE PLAN QUALITY:

The slice order is broadly coherent: contract review, custom app picker, unified matching, website rule model, browser detection, Bedtime integration, final E2E/release.

Slice 26.0 is correctly placed first, but it is currently a promise rather than an executed contract. The plan should not permit 26.1 implementation until Slice 26.0 produces the PRD deltas, support matrix, rule model, privacy boundary, and E2E evidence contract.

Slice 26.3 should not expose full URL/path rule UI before Slice 26.4 proves what Chrome can reliably provide. Either move the Chrome adapter proof before user-facing website rule UI, or limit Slice 26.3 to domain-only rules with path rules deferred.

Slice 26.1 and 26.2 should be treated as one demoable custom-app vertical path: select a non-standard safe app, persist it, trigger the same replacement intervention, use Soft/Firm/Bedtime semantics, and verify analytics.

Slice 26.4 needs more explicit acceptance criteria for non-matching domains, unreadable URL states, stale URL prevention, incognito/custom-tab/PWA behavior, and unsupported-browser fallback.

Slice 26.6’s 10/10 final review and APK release gate is appropriate, but the plan should state that release is blocked by any unresolved URL-support overclaim, privacy leak, or replacement-first regression.

ANDROID FEASIBILITY:

Custom installed-app selection is feasible at the Android package level if restricted to user-launchable, non-critical packages and integrated with the existing foreground-app/intervention architecture.

“Any installed app” is too broad as product and engineering language. The implementable version is “eligible user-launchable installed apps,” with explicit exclusions and clear disabled-state copy for excluded packages.

Chrome-first website detection through Android accessibility may be possible for narrow cases, but it is not a stable universal URL API. The plan must define that domain matching may only use a verified URL/address-bar accessibility node or another documented, testable browser adapter source.

The plan must prohibit matching based on page title, body text, search snippets, accessibility text from web content, notifications, autocomplete suggestions, or any scraped visible page text.

The plan must define stale-state handling: if the adapter cannot read a current verified URL for the active browser window, it must not trigger a website intervention from the last observed URL.

Unsupported browsers should show “website rules unavailable for this browser” or equivalent truthful status and offer only whole-browser app intervention as the reliable fallback. They must not silently claim domain protection.

Domain matching needs precise normalization and tests: lowercasing, scheme stripping, path/query/fragment stripping for domain rules, trailing-dot handling, IDNA/punycode handling, exact-host semantics, wildcard-subdomain semantics, and rejection of substring matches such as notreddit.com or reddit.com.evil.example.

Path rules should be removed from Sprint 26 or placed behind an adapter capability flag that is false by default. Chrome accessibility-based detection should not be marketed as full path blocking unless the adapter can repeatedly prove full canonical URL visibility across navigation, reload, background/foreground, address-bar collapsed state, and tab changes.

PRIVACY / ANALYTICS:

The stated privacy direction is correct: no browsing-history collection, no visited URLs off-device, no open-web crawling, no AI browsing categorization, and no browser extension/VPN/DNS/proxy scope.

The analytics contract must be tightened. Acceptable remote fields should be limited to values such as target_type=website, rule_type=domain|wildcard_domain, browser_support_status, intervention_outcome, mode, timestamps, and coarse session metrics. Raw domain, full URL, path, query, page title, visible browser text, and non-matching URL observations must be excluded.

User-authored website rules may be stored locally and exported only as explicit user profile data, but raw analytics event logs must not be exported in Portable Profile, consistent with FR13.

The implementation plan needs tests or log-scrubbing checks proving that URL candidates, page titles, domains, and non-match visits do not appear in analytics payloads, debug logs, crash breadcrumbs, screenshot labels, or review artifacts.

Custom app analytics also need a privacy decision. The plan says analytics should distinguish standard and custom app targets without leaking unnecessary package metadata, but it does not define whether remote analytics may store package IDs for custom apps. That must be settled in the plan.

Portable Profile import must not transfer browser compatibility state, permission state, Drive/auth state, raw Android URI state, or any indication that website rules are active on the new device before local verification.

TEST AND VISUAL EVIDENCE PLAN:

VISUAL REVIEW is not applicable for this plan review because the manifest states that screenshots and APK artifacts are excluded and do not exist yet for Sprint 26.

The future evidence plan is directionally acceptable but not strong enough yet. It lists screenshot contact sheets, automated output, emulator/device notes, and package hygiene notes, but it does not specify the exact critical screenshots, E2E scripts, browser versions, or privacy assertions.

Required custom-app evidence: app picker search, selected custom app state, removal/edit state, Settings persistence after restart, intervention triggered by a safe non-standard app, unselected app not triggering, and Soft/Firm/Bedtime variants.

Required website evidence: domain-rule add/edit/delete/disable validation, unsupported-browser status, Chrome matching-domain intervention, Chrome non-matching-domain non-intervention test output, unreadable URL fallback, and browser support matrix UI/copy.

Required automated tests: rule normalization, wildcard matching, substring rejection, stale URL rejection, local-only rule persistence, Portable Profile merge/replace behavior, package deduplication, excluded package filtering, unlock-window behavior, Pause 15 min behavior, Firm wait behavior, Bedtime emergency unlock behavior, and analytics payload privacy.

Required E2E metadata: emulator/device model, Android API level, Chrome package name/version, browser support status, test URL set, whether incognito/custom tabs/PWAs are supported or explicitly unsupported, and raw test output path.

Required negative evidence: no trigger for notreddit.com when reddit.com is configured, no trigger for reddit.com.evil.example, no trigger from stale previously observed URLs, and no analytics/debug artifact containing raw URL strings.

BUNDLE GAPS:

BUNDLE GAP: App source code is excluded by design, so the current interception architecture, accessibility service implementation, overlay behavior, analytics implementation, and Portable Profile implementation cannot be verified from this bundle.

BUNDLE GAP: The current Sprint 26 plan says Slice 26.0 will define the browser support matrix, privacy boundary, rule model, and E2E evidence contract, but those artifacts are not present in the shipped files.

BUNDLE GAP: No PRD patch or decision document for arbitrary custom apps or website/domain interventions is included.

BUNDLE GAP: No screenshots, visual contact sheets, instrumentation logs, APKs, or browser E2E evidence exist for Sprint 26; the manifest explicitly excludes them because implementation has not begun.

BUNDLE GAP: Prior lane status claims, including Sprint 25’s 10/10 review and validation results, are reported in docs/LANE_STATUS.md but cannot be independently verified from the shipped Sprint 26 plan bundle.

BUNDLE GAP: The bundle does not include current Android manifest permissions, Play policy declarations, accessibility disclosure copy, or onboarding copy, so permission-risk mitigation cannot be verified.

PACKAGE HYGIENE:

The review bundle is mostly clean: it contains five files, matches the manifest’s included-file list, and excludes source code, stale generated review bundles, APKs, and screenshots as declared.

Minor hygiene issue: docs/LANE_STATUS.md has a status timestamp of 2026-05-31 while the Sprint 26 documents in the same bundle are dated 2026-06-07, creating stale-status ambiguity.

Scope-language hygiene issue: the sprint title, branch name, and lane wording use “blocking/url-blocking,” while the PRD requires replacement-first intervention rather than blocker positioning. Rename the sprint and user-facing language toward “custom targets and website interventions” or equivalent.

Minor manifest issue: BUNDLE_MANIFEST.md does not list REQUIRED PLAN CHANGES BEFORE IMPLEMENTATION in the expected review output, although this review prompt requires that section.

REQUIRED PLAN CHANGES BEFORE IMPLEMENTATION:

Patch PRD.md before code changes to explicitly authorize two new target classes: eligible custom installed-app targets and supported-browser website/domain intervention rules.

State in the PRD delta that Sprint 26 does not add universal URL blocking, parental control, browser extensions, VPN/DNS/proxy filtering, browsing-history collection, open-web crawling, or default hard-block behavior.

Rename sprint wording away from “website blocking” and “full ban” except where describing opt-in Bedtime emergency-unlock behavior.

Add a TargetRule model to the sprint plan with explicit fields for target_type, rule_id, enabled, source=user, created_at, updated_at, and type-specific payloads for standard app, custom app, exact domain, and wildcard domain.

Add custom-app eligibility rules: include only user-launchable non-critical packages; exclude the app itself, launchers, Settings/permission flows, phone/emergency packages, package installers, system UI, accessibility settings, and other system-critical packages.

Define Portable Profile behavior for custom apps: export package id and user-visible label as user-authored configuration; on import, resolve locally, deduplicate against standard targets, mark missing packages unavailable, and never import permission/protection-active state.

Limit Sprint 26 website matching to exact-domain and wildcard-subdomain rules unless a later adapter proves full URL/path support. Remove path rules from the user-facing plan or mark them deferred.

Define exact domain semantics: no substring matching; example.com must not match notexample.com or example.com.evil.tld; wildcard rules must specify whether *.example.com includes or excludes the apex domain.

Add URL normalization requirements: strip scheme/path/query/fragment for domain rules, lowercase host, handle trailing dots, handle IDNA/punycode consistently, reject ambiguous invalid input, and never store visited URLs as match candidates.

Add a browser support matrix with at least these columns: browser package, adapter source, support level, can read host, can read full path, unsupported states, test coverage, user-facing status copy, and fallback.

Define Chrome support as verified-domain support only when the current host is read from a verified URL/address-bar source. Mark incognito, custom tabs, PWAs, collapsed/hidden URL states, and non-Chrome browsers unsupported unless explicitly proven.

Add stale URL prevention: website intervention may fire only from a current verified URL observation for the active browser window; unreadable URL state must not reuse a previous URL.

Add unsupported-browser fallback behavior: show truthful degraded/unavailable status and offer whole-browser app intervention as the reliable fallback, preserving Soft/Firm/Bedtime semantics.

Replace analytics language with a concrete allowlist and denylist. Deny raw URL, host, domain, path, query, page title, URL-bar text, non-match observations, browsing-history rows, and domain-derived hashes in remote analytics.

Specify that website rule ids used in remote analytics must not be reversible to domains and must not be stable cross-device identifiers unless the privacy model is explicitly approved.

Add privacy tests that inspect analytics payloads and logs for absence of raw URL/domain/title strings.

Add visual/E2E acceptance details for each slice, including exact screenshot states, emulator/API level, Chrome version, supported and unsupported browser status, matching/non-matching domain tests, and Bedtime website/custom-app evidence.

Change Slice 26.0 acceptance from requiring VISUAL REVIEW PASS to VISUAL REVIEW NOT APPLICABLE for plan-only review, then require VISUAL REVIEW PASS only after UI screenshots exist.

Update docs/LANE_STATUS.md timestamp to match the Sprint 26 bundle date before the next review bundle is produced.