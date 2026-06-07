You are doing a fresh-from-scratch adversarial implementation audit of Sprint 26 Slice 26.1 R4.

Use only the attached bundle as the audit base. If a claim cannot be proven from shipped files, label it `BUNDLE GAP`. Do not penalize Slice 26.1 for not implementing website/domain rules; those are explicitly deferred to Slice 26.2+.

Read these files first, in order:

1. `PRD.md`
2. `docs/SPRINT_26_CUSTOM_TARGETS_WEBSITE_INTERVENTIONS.md`
3. `evidence/sprint26_custom_targets_website_interventions/GPT_PRO_SLICE26_1_REVIEW.md`
4. `evidence/sprint26_custom_targets_website_interventions/GPT_PRO_SLICE26_1_R2_REVIEW.md`
5. `evidence/sprint26_custom_targets_website_interventions/GPT_PRO_SLICE26_1_R3_REVIEW.md`
6. `evidence/sprint26_custom_targets_website_interventions/SLICE26_1_R4_VALIDATION.md`
7. `evidence/sprint26_custom_targets_website_interventions/SLICE26_1_R4_BUNDLE_MANIFEST.md`

Then deep-review only this scope:

Sprint 26 Slice 26.1 Custom App Target Vertical R4:

- eligible launchable installed apps can be selected as custom intervention targets,
- setup-critical/system-critical apps remain unavailable or disabled,
- standard suggestions remain separate from custom app search,
- selected custom packages persist and hydrate into Settings, Portable Profile, and AccessibilityService resolver behavior,
- selected custom targets can trigger the existing replacement-first intervention flow through the system-interception intent path,
- website/domain blocking is NOT implemented yet and should remain deferred to Slice 26.2+.

Actively recheck R3 blockers:

1. Build-complete bundle
   - R3 failed because the bundle omitted `gradle/wrapper/gradle-wrapper.jar`, `gradle/wrapper/gradle-wrapper.properties`, and `app/proguard-rules.pro`.
   - Verify R4 includes those files plus all `app/src` production/test/androidTest source.

2. Completed-profile empty Settings recovery
   - R3 found that all-missing Portable Profile replace import correctly stayed empty, but Settings could not recover because `toggleSettingsApp()` rejected any add while selection size remained below three.
   - Verify R4 allows incremental additions from an empty completed Settings state while still blocking removals that would drop below the minimum.
   - Verify regression coverage proves standard and custom eligible targets can be selected and persisted after all-missing replace import.

3. Setup-critical/OEM safety boundary
   - Verify known safety/permission/phone package families are excluded or disabled.
   - Verify R4 does not claim exhaustive future-OEM coverage beyond shipped static package families plus launcher/home discovery.
   - Verify the policy test covers known OEM safety apps without blanket-blocking ordinary OEM launchable apps.

4. R1/R2 blockers remain closed
   - Default intervention mode is Soft.
   - DocumentsUI/file-picker packages are excluded.
   - Portable Profile all-missing import does not hydrate unrelated defaults.
   - Visual evidence still covers persistence, remove/unselect, unselected no-trigger, Soft, Firm, and Bedtime.
   - Soft visual evidence is reached through `MainActivity.createSystemInterceptionIntent()` for a selected custom package, not a direct ViewModel shortcut.

Scoring:

- Give `SCORE: 10/10` only if Slice 26.1 is release-ready for this scope, R1/R2/R3 blockers are closed, visual review passes, and no material bundle gaps remain.
- If anything blocks release, use a score below 10 and list the tightest required fixes.

Required output format, with these exact markers:

SCORE: X/10

VERDICT: PASS or FAIL

VISUAL REVIEW: PASS or FAIL

BLOCKERS:
- If none, write `None`.

R3 BLOCKER RECHECK:
- Build-complete bundle:
- Completed-profile empty Settings recovery:
- Setup-critical/OEM safety boundary:

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

