SCORE: 9/10

VERDICT: FAIL

VISUAL REVIEW: PASS

BLOCKERS

Gate blocker for SCORE 10/10: the bundle still does not prove stale active-window/package-mismatch safety at the same evidentiary level as the positive Chrome live-service path. The minimum fix is to add package identity to the verified browser snapshot path, require the verified address-bar node or root to belong to com.android.chrome, and ship a connected negative test proving that a Chrome event cannot verify a host from a stale, unreadable, or non-Chrome active window.

R5 BLOCKER / GAP RECHECK

PARTIAL PASS.

The R6 bundle closes most R5 gaps. It ships a fresh connected Android run for the current source snapshot, a live Chrome AccessibilityService event-to-intervention proof, device/API/Chrome version evidence, schemed Unicode URL normalization coverage, raw git diff --check output, and package-hygiene evidence.

The remaining score gap is stale/unreadable negative coverage. The connected negative test proves unsupported browser package handling and unreadable null root handling. Unit/source evidence covers hidden address nodes, focused editable omnibox states, search text, IP text, and title/body false positives. However, direct connected proof is still missing for stale active-window/package mismatch, custom-tab/PWA/incognito-like unreadable surfaces, and rapid-navigation race behavior. Where the bundle claims those broader states are fully connected-proven, that claim is a BUNDLE GAP.

CHROME VERIFIED-HOST ADAPTER

PARTIAL PASS.

The adapter is Chrome-first and rejects unsupported browser packages. It reads only whitelisted Chrome address-bar resource names, rejects hidden nodes, rejects focused editable omnibox text, rejects non-host text through normalization, and does not cache or reuse prior hosts. The scan-budget increase to 512 nodes is supported by the source and by the live Chrome evidence.

The adapter still trusts the event browser package and the address-bar resource-name suffix, while the shipped snapshot model does not carry root or node package identity. That leaves a residual stale-window/package-mismatch proof gap: the bundle does not prove that a stale or non-Chrome node tree containing a matching url_bar-style identifier cannot be treated as Chrome-verified during a Chrome event. This is the decisive reason the slice does not reach 10/10.

WEBSITE RULE MATCHING

PASS.

Website matching is adequately proven for exact domains, wildcard domains, apex-include behavior, disabled rules, boundary-spoof rejection, Unicode-to-ASCII normalization, schemed Unicode URLs, scheme/path/port stripping, trailing-dot handling, and rejection of IP/local/ambiguous inputs. The resolver prioritizes exact rules over wildcard rules and keeps website suppression scoped by browser-package target key rather than by raw host.

STALE / UNREADABLE STATE SAFETY

PARTIAL PASS.

The bundle proves important safety properties: typed-but-not-loaded omnibox text is unreadable and does not trigger intervention; unsupported packages are rejected; null active roots are unreadable; hidden and focused editable address nodes are rejected in unit coverage; and the implementation does not store a prior verified host.

The missing proof is direct connected stale-state coverage. The bundle does not contain a connected test that simulates or demonstrates stale active-window mismatch, package-mismatched node trees, custom-tab/PWA/incognito unreadability, or rapid navigation from a previously matching host to a nonmatching host without a false intervention. These are BUNDLE GAP items for the R6 gap-closure claim.

SOFT / FIRM / OPEN ANYWAY BEHAVIOR

PASS.

The bundle proves website intervention UI behavior for soft and firm modes. Soft mode leaves “Open Chrome website” immediately available. Firm mode disables open-anyway during the five-second delay. View-model tests also prove website-specific suppression does not suppress the whole Chrome app target, while bedtime/firm unlock behavior remains governed by the existing intervention timing rules.

PRIVACY / ANALYTICS

PASS.

The evidence supports the privacy design. Website metadata is coarse and includes target type, browser package, support status, rule type, and apex setting rather than raw host or URL. Tests prove domain strings such as example, full URLs, paths, query tokens, and private replacement-link URLs are not emitted into the relevant analytics metadata. Intervention display uses “Chrome website” rather than exposing the matched domain.

TEST / EVIDENCE

PARTIAL PASS.

Unit and lint evidence is strong: 432 unit tests passed with 0 failures, 0 errors, and 0 skipped tests; lint completed with 0 errors and only warnings/hints. Connected evidence shows 4/4 Android tests passed on qaApi36(AVD) - 16. Device proof identifies sdk_gphone64_arm64, Android API 36, Android release 16, and Chrome 133.0.6943.137 with versionCode 694313732.

The live-service E2E evidence is materially stronger than R5: Chrome is launched to https://example.com/, the Quality Alternative AccessibilityService is enabled and bound, the live window dump contains “You reached for Chrome website,” and the live screenshot shows the website intervention. This proves the positive event-to-intervention path.

The evidence gap is negative connected coverage breadth. The R6 connected negative artifact is narrower than the stated R5 gap: it proves unsupported package and unreadable root, but not package-mismatched stale roots, custom-tab/PWA/incognito surfaces, or rapid-navigation stale-host races.

VISUAL REVIEW

PASS.

The visual evidence supports the shipped UI. The settings contact sheet shows website-rule creation, exact and wildcard modes, private/public IP rejection, include-main-domain behavior, pause/enable/edit/delete flows, and browser-support messaging. The Chrome E2E contact sheet shows nonmatching example.org without intervention, typed-but-not-loaded example.com without intervention, loaded example.com, and a live Chrome website intervention. The intervention contact sheet shows both soft and firm website intervention states, including the disabled firm-mode open-anyway countdown.

BUNDLE GAPS

BUNDLE GAP: no direct connected stale active-window/package-mismatch negative proof.

BUNDLE GAP: no direct connected custom-tab, PWA, or incognito unreadable-surface negative proof.

BUNDLE GAP: no direct connected rapid-navigation race proof showing that a prior matching host cannot trigger after navigating to a nonmatching or unreadable state.

BUNDLE GAP: the adapter snapshot source does not include root/node package identity, so the bundle does not prove that whitelisted address-bar resource names are package-authenticated rather than only event-package-authenticated.

PACKAGE HYGIENE

PASS.

The manifest, evidence files, source files, test files, connected logs, live-service artifacts, visual artifacts, unit/lint logs, and diff-check artifacts are present. Raw git diff --check status is PASS, and the raw output file is empty as expected for a clean diff check. The bundle does not rely on APK binaries for the review conclusion, and the post-evidence package structure is sufficiently organized for auditability.