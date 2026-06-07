You are doing a fresh-from-scratch adversarial blocker-fix audit of Sprint 26 Slice 26.2 R2.

Use only the attached bundle as the audit base. If a claim cannot be proven from shipped files, label it `BUNDLE GAP`.

Read these files first, in order:

1. `PRD.md`
2. `docs/SPRINT_26_CUSTOM_TARGETS_WEBSITE_INTERVENTIONS.md`
3. `docs/LANE_STATUS.md`
4. `evidence/sprint26_custom_targets_website_interventions/GPT_PRO_SLICE26_2_REVIEW.md`
5. `evidence/sprint26_custom_targets_website_interventions/SLICE26_2_R2_EVIDENCE.md`
6. `evidence/sprint26_custom_targets_website_interventions/SLICE26_2_R2_REVIEW_BUNDLE_MANIFEST.md`
7. `evidence/sprint26_custom_targets_website_interventions/SLICE26_2_R2_DIFF.patch`

Review only this scope:

Sprint 26 Slice 26.2 Website Rule Model And Settings UI R2:

- user-authored website rules can be added in Settings,
- exact-domain and wildcard-subdomain rules normalize predictably,
- invalid, private, local, ambiguous, path-only, IP, and unsafe host inputs are rejected,
- wildcard rules can optionally include the apex domain only through a visible explicit choice,
- website rules can be paused, edited, canceled, and deleted,
- website rules persist through `PreferencesSettingsRepository`,
- website rules export/import through Portable Profile without leaking browser history, observed URLs, local support state, folders, or tokens,
- Settings communicates the browser support boundary honestly:
  - Chrome current-host verification/interception is deferred to Slice 26.3,
  - non-supported browsers remain whole-browser app targets for now,
  - this slice must NOT claim universal URL blocking.

Do not penalize Slice 26.2 for not implementing the Chrome verified-host adapter or live browser URL interception. Those are explicitly deferred to Slice 26.3. Do penalize any code, UI, or documentation that falsely implies URL blocking is already active for all browsers.

Actively recheck R1 blockers:

1. Public IP rejection
   - Verify public IPv4 literals such as `8.8.8.8` and `1.1.1.1` are rejected.
   - Verify malformed all-numeric hosts and IPv6 literals are rejected.
   - Verify Settings save, DataStore hydration, and Portable Profile import all use the fixed validation.

2. Explicit wildcard apex behavior
   - Verify typed `*.example.com` cannot silently create an apex-including rule.
   - Verify apex inclusion defaults off.
   - Verify the apex control is visible before save whenever the typed input is treated as wildcard.
   - Verify tests and visual evidence cover the typed wildcard path.

3. Build-reproducible bundle
   - Verify `gradle/libs.versions.toml`, wrapper files, Gradle files, and relevant source/tests are included.

4. Browser-support wording
   - Verify website-rule count says enabled/stored rather than active/enforced.
   - Verify UI/docs do not imply live website enforcement before Slice 26.3.

5. Evidence and package hygiene
   - Verify final R2 contact sheet has no keyboard/snackbar/transient overlays.
   - Verify public-IP rejection, typed wildcard visible apex toggle, subdomain-only save, visible apex save, cancel edit, and browser support matrix are visible.
   - Verify Android test metadata in the bundle is sanitized and does not contain absolute local macOS paths.

Scoring:

- Give `SCORE: 10/10` only if Slice 26.2 R2 is release-ready for this scope, R1 blockers are closed, visual review passes, browser-support claims are honest, and no material bundle gaps remain.
- If anything blocks release, use a score below 10 and list the tightest required fixes.

Required output format, with these exact markers:

SCORE: X/10

VERDICT: PASS or FAIL

VISUAL REVIEW: PASS or FAIL

BLOCKERS:
- If none, write `None`.

R1 BLOCKER RECHECK:
- Public IP rejection:
- Explicit wildcard apex behavior:
- Build-reproducible bundle:
- Browser-support wording:
- Evidence/package hygiene:

RISK / REQUIRED FIXES:
- If none, write `None`.

WEBSITE RULE MODEL:

SETTINGS UI:

PORTABLE PROFILE / PRIVACY:

BROWSER SUPPORT CLAIMS:

TEST / EVIDENCE:

BUNDLE GAPS:
- If none, write `None`.

PACKAGE HYGIENE:
