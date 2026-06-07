SCORE: 7/10

VERDICT: FAIL

VISUAL REVIEW: PASS

BLOCKERS:

Public IPv4 literals are not rejected. WebsiteRuleNormalizer.isPrivateOrAmbiguousHost() rejects only private/link-local/loopback IPv4 ranges, so public IPv4 hosts such as 8.8.8.8 or 1.1.1.1 would normalize as valid website rules. This violates the Slice 26.2 requirement that IP hosts be rejected, and it affects Settings entry, repository restoration, and Portable Profile import because all three rely on the same normalizer.

Explicit wildcard input can create an apex-including wildcard rule without exposing the apex choice. The UI placeholder accepts *.example.com, and the normalizer treats a leading *. as wildcard even when the “Exact” chip remains selected. Since websiteRuleDraftIncludeApex defaults to true and the “Include main domain” chip is only visible when the wildcard chip is selected, a user can save *.example.com + example.com without seeing or choosing the apex option.

The review bundle is not fresh-build reproducible as shipped. build.gradle.kts and app/build.gradle.kts use version-catalog aliases, but gradle/libs.versions.toml is absent from the bundle. BUNDLE GAP: the shipped files do not prove that a fresh reviewer can run the Gradle validation commands from the bundle alone.

RISK / REQUIRED FIXES:

Reject all IP literals, not only private IPv4 ranges. Add tests for public IPv4, malformed dotted numeric hosts, IPv6 literals, URI bracketed IPv6 hosts, repository decode, ViewModel save, and Portable Profile import.

Make wildcard apex inclusion explicit and deterministic. The safest fix is to default includeApex to false, show the apex control whenever the input is or will be treated as a wildcard, and add ViewModel and visual tests for the *.example.com typed-input path.

Add negative tests for unsafe or ambiguous host forms such as user-info syntax, path-only inputs, search-like text, and port/scheme/path combinations that could otherwise create misleading stored rules.

Rename the website-rule count from “active” to “enabled” or equivalent while live browser URL interception is deferred, because “active” can be read as implying current enforcement.

Include the missing Gradle version catalog and rerun the documented validation from the exact shipped bundle.

Scrub absolute local paths from evidence metadata. The connected-test textproto references local macOS paths and external artifacts not shipped in the bundle.

WEBSITE RULE MODEL:

Exact-domain matching is correctly constrained to equality, so example.com does not match www.example.com, notexample.com, or example.com.evil.

Wildcard-subdomain matching uses host.endsWith(".${rule.host}") and optional apex equality, which avoids substring overmatch against unrelated hosts.

Normalization is mostly deterministic: scheme, port, path, query, fragment, case, trailing dots, and IDN input are handled in the normalizer and covered by unit tests for representative cases.

IDN handling is present through IDN.toASCII(..., USE_STD3_ASCII_RULES) and is tested with MÜNICH.example becoming xn--mnich-kva.example; broader IDN edge-case coverage remains advisable.

Invalid local/private examples are covered for localhost, .local, 192.168.1.20, 10.0.0.1, and 172.16.4.2.

The material correctness failure is IP rejection: public IPv4 literals are accepted because the code only flags selected private ranges as invalid.

The rule matcher itself does not inspect enabled; this is acceptable for Slice 26.2 only because live verified-host interception is deferred, but Slice 26.3 must ensure disabled rules are filtered before any enforcement decision.

SETTINGS UI:

Source inspection shows visible add, edit, save, cancel, delete, and pause/resume flows in WebsiteRulesSettingsSection.

Validation errors are user-visible through the website-rule error text, and the visual evidence includes a rejected private-IP entry.

Rule rows give the domain text its own weighted column and place edit/delete controls below the metadata, so the contact sheet does not show domain text being squeezed by action buttons.

The final screenshot evidence is clean: no keyboard, snackbar, or transient overlay pollutes the captured final states.

Browser support messaging is visible near the website-rule section and in the browser support matrix rather than being buried.

The visual sheet proves empty state, invalid private-IP validation, exact-domain save, wildcard with apex, pause, edit, delete, and browser support matrix.

The UI has a release-relevant apex bug: a typed *.domain can become an apex-including wildcard rule while the apex toggle remains hidden.

PORTABLE PROFILE / PRIVACY:

Exported website rules are limited to portable user-authored fields: id, ruleType, host, enabled, and includeApex.

The exported website-rule profile format does not include created/updated timestamps, observed browser URLs, browser history, support-state flags, profile paths, local folder IDs, or tokens in the inspected website-rule mapping.

Tests assert that settings-only export does not contain content://, raw Drive folder IDs, raw profile folder IDs, token, or oauth.

Replace import restores website rules through the portable profile path and rejects malformed examples for private IP, path-bearing host, and includeApex on exact-domain rules.

Existing custom app target profile behavior appears preserved in the tested exporter/importer paths.

The same public-IP normalizer defect affects Portable Profile import: a malicious or malformed portable profile containing a public IPv4 “website rule” would not be rejected by the current validation.

BROWSER SUPPORT CLAIMS:

PASS for honesty of scope. The PRD, sprint document, lane status, evidence, and Settings UI consistently state that Chrome verified-current-host support is deferred to Slice 26.3.

The UI states that website interruption activates only after a verified current-host adapter is available.

Non-supported browsers are described as requiring whole-browser app targets for now.

No inspected Slice 26.2 source or evidence claims universal URL blocking or all-browser URL interception.

Minor wording risk remains in the “active” count for website rules, because enabled stored rules are not yet browser-enforced website interventions.

TEST / EVIDENCE:

Unit evidence is present for model, repository persistence, Portable Profile export/import, and ViewModel actions. The included HTML report shows 202 debug unit tests with 0 failures.

WebsiteRuleNormalizerTest covers scheme/path/port stripping, trailing-dot behavior, IDN conversion, explicit wildcard prefix normalization, local/private rejection, exact matching, wildcard matching, optional apex matching, and non-overmatch examples.

PreferencesSettingsRepositoryTest covers persistence/restoration of website rules and malformed stored-record rejection.

AccountLightProfileExporterTest and AccountLightProfileImporterTest cover website-rule export/import and several privacy exclusions.

MainViewModelTest covers add, duplicate rejection, private-host rejection, edit, toggle pause, and delete.

The connected visual E2E evidence is present and passes for VisualQaScreenshotTest#captureSprint26WebsiteRuleSettingsScreens.

The screenshot capture helper rejects empty or undersized screenshot files by requiring a successful screenshot and a file larger than 10,000 bytes, with retries.

The contact sheet is suitable as the final reviewer artifact and matches the evidence document.

Lint evidence is present and the task is reported as passing, but the HTML report still contains warnings and hints; these are package-hygiene issues rather than Slice 26.2 functional blockers.

Test coverage gaps remain for public IP rejection, explicit wildcard input with hidden apex toggle, user-info host forms, broader unsafe host inputs, and visual proof of cancel behavior.

BUNDLE GAPS:

BUNDLE GAP: gradle/libs.versions.toml is missing, so the bundle does not prove fresh-from-scratch Gradle build/test reproducibility.

BUNDLE GAP: connected-test metadata references local-path artifacts such as device-info, meminfo, cpuinfo, and test-results log files that are not all shipped in the bundle, although the pass XML and logcat evidence are present.

BUNDLE GAP: the bundle proves the visual states listed in the contact sheet, but it does not visually prove the cancel flow.

PACKAGE HYGIENE:

The bundle includes source, tests, documentation, patch, visual evidence, unit-test reports, connected-test evidence, lint report, Gradle wrapper files, and manifest documentation.

The missing version catalog is a material packaging defect because the Gradle files depend on libs.* and plugin aliases.

Evidence metadata contains absolute local paths including a local username and repository path; these should be scrubbed or converted to relative paths before review distribution.

Lint passes but is not clean: the report contains warnings and hints that should be triaged or explicitly baselined.

The package is adequate for static review and visual audit, but not adequate for an independently reproducible fresh-from-scratch validation run.