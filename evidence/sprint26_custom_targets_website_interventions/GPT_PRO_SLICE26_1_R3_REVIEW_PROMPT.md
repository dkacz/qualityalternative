You are doing a fresh-from-scratch adversarial implementation audit of Sprint 26 Slice 26.1 R3.

Use only the attached bundle as the audit base. If a claim cannot be proven from shipped files, label it `BUNDLE GAP`. Do not penalize Slice 26.1 for not implementing website/domain rules; those are explicitly deferred to Slice 26.2+.

Read these files first, in order:
1. `PRD.md`
2. `docs/SPRINT_26_CUSTOM_TARGETS_WEBSITE_INTERVENTIONS.md`
3. `evidence/sprint26_custom_targets_website_interventions/GPT_PRO_SLICE26_1_REVIEW.md`
4. `evidence/sprint26_custom_targets_website_interventions/GPT_PRO_SLICE26_1_R2_REVIEW.md`
5. `evidence/sprint26_custom_targets_website_interventions/SLICE26_1_R3_VALIDATION.md`
6. `evidence/sprint26_custom_targets_website_interventions/SLICE26_1_R3_BUNDLE_MANIFEST.md`

Then deep-review only this scope:

Sprint 26 Slice 26.1 Custom App Target Vertical R3:
- eligible launchable installed apps can be selected as custom intervention targets,
- setup-critical/system-critical apps remain unavailable or disabled,
- standard suggestions remain separate from custom app search,
- selected custom packages persist and hydrate into Settings, Portable Profile, and AccessibilityService resolver behavior,
- selected custom targets can trigger the existing replacement-first intervention flow through the system-interception intent path,
- website/domain blocking is NOT implemented yet and should remain deferred to Slice 26.2+.

Actively recheck R2 blockers:

1. Build-complete bundle
   - R2 failed because the bundle omitted source referenced by included source/tests.
   - Verify R3 includes all `app/src` production/test/androidTest source plus Gradle/config and is no longer a partial implementation bundle.

2. Portable Profile all-missing import bug
   - R2 found that all-missing imported app targets could hydrate unrelated default supported apps into Settings/UI.
   - Verify R3 fixes this in production and tests. Completed-profile replace import with empty post-filter selected packages must remain empty in repository, Settings/UI hydration, and resolver-visible target list.

3. Fresh test evidence
   - R2 rejected mostly UP-TO-DATE logs.
   - Verify R3 includes `--rerun-tasks` unit logs, XML reports, and summaries with test class counts.

4. System-interception intent path
   - R2 rejected direct ViewModel-only visual evidence.
   - Verify R3 includes a connected test for `MainActivity.createSystemInterceptionIntent()` with a selected custom installed app and that the R3 Soft visual screenshot is reached through that intent path.

5. R1 blockers remain closed
   - Default intervention mode is Soft.
   - DocumentsUI/file-picker packages are excluded.
   - Visual evidence still covers persistence, remove/unselect, unselected no-trigger, Soft, Firm, and Bedtime.

Scoring:
- Give `SCORE: 10/10` only if Slice 26.1 is release-ready for this scope, R1/R2 blockers are closed, visual review passes, and no material bundle gaps remain.
- If anything blocks release, use a score below 10 and list the tightest required fixes.

Required output format, with these exact markers:

SCORE: X/10

VERDICT: PASS or FAIL

VISUAL REVIEW: PASS or FAIL

BLOCKERS:
- If none, write `None`.

R2 BLOCKER RECHECK:
- Build-complete bundle:
- Portable Profile all-missing import:
- Fresh test evidence:
- System-interception intent path:

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
