You are doing a fresh-from-scratch adversarial implementation audit of Sprint 26 Slice 26.1 R2.

GUIDING PRINCIPLES:
1. Use only the shipped bundle as the audit base.
2. If a claim cannot be proven from shipped files, label it `BUNDLE GAP`.
3. Do not inflate duplicate or already-covered suggestions into fresh findings.
4. Prefer concrete blockers over style feedback.
5. Visual evidence must be judged from the shipped screenshots/contact sheet, not from prose claims alone.
6. Package hygiene is part of the score: stale, partial, or misleading evidence should reduce the score.

Read these files first, in order:
1. `PRD.md`
2. `docs/SPRINT_26_CUSTOM_TARGETS_WEBSITE_INTERVENTIONS.md`
3. `evidence/sprint26_custom_targets_website_interventions/GPT_PRO_SLICE26_1_REVIEW.md`
4. `evidence/sprint26_custom_targets_website_interventions/SLICE26_1_VALIDATION.md`
5. `evidence/sprint26_custom_targets_website_interventions/SLICE26_1_R2_BUNDLE_MANIFEST.md`

Then deep-review only this scope:

Sprint 26 Slice 26.1 Custom App Target Vertical R2:
- eligible launchable installed apps can be selected as custom intervention targets,
- setup-critical/system-critical apps remain unavailable or disabled,
- standard suggestions remain separate from custom app search,
- selected custom packages persist and hydrate into Settings, Portable Profile, and AccessibilityService resolver behavior,
- selected custom targets can trigger the existing replacement-first intervention flow,
- website/domain blocking is NOT implemented yet and should remain deferred to Slice 26.2+.

Actively recheck these R1 blockers:

1. Default intervention mode
   - R1 failed because default mode was Firm and the visual showed "Open in 5s".
   - Verify R2 defaults to Soft in production, tests, and visual evidence.

2. DocumentsUI / file picker safety
   - R1 failed because DocumentsUI/Files appeared eligible.
   - Verify R2 excludes `com.android.documentsui`, `com.google.android.documentsui`, and `com.google.android.apps.docs`, with tests and disabled/setup-safe rationale.
   - The R2 visual emulator has these packages installed but no launcher activity; use `documentsui_package_check.log` plus policy tests for the audit.

3. Portable Profile auditability
   - R1 could not audit Portable Profile because implementation files were missing.
   - Verify R2 ships `AccountLightProfile.kt`, `SupportedCatalog.kt`, exporter/importer tests, and related hydration paths.

4. Full intervention E2E auditability
   - R1 could not audit the AccessibilityService to MainActivity to ViewModel path because files were missing.
   - Verify R2 ships `MainActivity.kt`, `QualityAlternativeAccessibilityService.kt`, `InterceptionTargetResolver.kt`, and relevant tests/visuals.

5. Visual evidence completeness
   - R1 visual evidence was missing persistence-after-restart, remove/unselect, unselected no-trigger, and Soft/Firm/Bedtime variants.
   - Verify `screenshots-slice26_1_r2/CONTACT_SHEET.png` and raw screenshots cover those states and have no obvious UI regressions.

6. Test and package evidence
   - Verify raw logs exist and support the validation claims.
   - Verify the bundle is clean enough to audit R2 without stale R1 artifacts being mistaken as current evidence.

Scoring:
- Give `SCORE: 10/10` only if Slice 26.1 is release-ready for this scope, R1 blockers are closed, visual review passes, and no material bundle gaps remain.
- If anything blocks release, use a score below 10 and list the tightest required fixes.
- Do not penalize Slice 26.1 for not implementing website/domain rules; those are explicitly deferred unless shared model changes break that future work.

Required output format, with these exact markers:

SCORE: X/10

VERDICT: PASS or FAIL

VISUAL REVIEW: PASS or FAIL

BLOCKERS:
- If none, write `None`.

R1 BLOCKER RECHECK:
- Default intervention mode:
- DocumentsUI / file picker safety:
- Portable Profile auditability:
- Full intervention E2E auditability:
- Visual evidence completeness:
- Test/package evidence:

CUSTOM APP TARGETS:

ELIGIBILITY / SAFETY:

INTERVENTION E2E:

PORTABLE PROFILE:

PRIVACY / ANALYTICS:

TEST / EVIDENCE:

BUNDLE GAPS:
- If none, write `None`.

PACKAGE HYGIENE:

REQUIRED FIXES:
- If none, write `None`.
