You are doing a fresh-from-scratch adversarial audit of Sprint 26 Slice 26.5 R2 for the Android app Quality Alternative.

Read the FULL attached primary documents first:

1. `PRD.md`
2. `docs/SPRINT_26_CUSTOM_TARGETS_WEBSITE_INTERVENTIONS.md`
3. `docs/LANE_STATUS.md`
4. `evidence/sprint26_custom_targets_website_interventions/SLICE26_5_EVIDENCE.md`
5. `evidence/sprint26_custom_targets_website_interventions/GPT_PRO_SLICE26_5_REVIEW_SCRUBBED_FOR_R2.md`

Then deep-review only this named scope:

Sprint 26 Slice 26.5 R2 - Bedtime and supported website target integration, with R1 blocker recheck.

Use only the shipped bundle as the audit base. If a claim cannot be proven from shipped files, label it `BUNDLE GAP`. Do not trust stale derived prose over canonical source, test XML/logs, UI dumps, and screenshot files.

R1 result to recheck:

- `SCORE: 8/10`
- `VERDICT: FAIL`
- `VISUAL REVIEW: PASS`
- R1 blocker: the bundle did not fully prove that Bedtime website emergency unlock is reachable only through the verified-host website-intervention path, because the evidence used direct entrypoints and omitted `MainActivity.kt` / `AndroidManifest.xml`.
- R1 package gaps: missing source/evidence for intent boundary, analytics guard source, prior Slice 26.1 R4 artifacts, and unsanitized local lint paths.
- The original R1 harvest is preserved locally at `GPT_PRO_SLICE26_5_REVIEW.md`; this R2 bundle ships a scrubbed copy to avoid repeating the absolute-path leakage that R1 flagged.

Known prior bug classes to actively test against:

- Bedtime screenshot accidentally proving the normal selected app target instead of the Chrome website target.
- Website intervention overclaiming generic URL blocking rather than supported Chrome verified-host behavior.
- `Pause 15 min` remaining available during Bedtime.
- Emergency unlock suppression applying to the whole Chrome app target instead of only the website target key.
- Meditation/alternatives disappearing in Bedtime.
- Analytics metadata leaking raw host, URL, path, page title, or domain-derived identifiers.
- Screenshot/contact-sheet evidence mixing stale failed attempts with final canonical output.
- Forged or non-service-created website-intervention intents reaching Bedtime website UI.
- Bundle omissions preventing source audit of the exported activity / internal launch-token boundary.

Your job:

- Verify R1 blocker closure:
  - `MainActivity.kt` rejects system intervention intents unless they carry the per-process internal launch token.
  - `AndroidManifest.xml` does not expose another website-intervention surface.
  - `MainActivityTest#forgedWebsiteInterceptionIntentWithoutLaunchTokenIsIgnored` proves forged website intents do not show `Chrome website` or Bedtime website UI.
  - The external live-service evidence proves real Chrome + bound `QualityAlternativeAccessibilityService` + matching verified host reaches `Bedtime is protecting sleep from Chrome website`.
- Verify selected custom app Bedtime behavior remains covered and unchanged by prior Slice 26.1 evidence and current tests.
- Verify supported Chrome website/domain targets get Bedtime emergency unlock only when launched through the verified-host website intervention path.
- Verify Soft and Firm website behavior outside Bedtime remains unchanged.
- Verify Bedtime website UI visually shows `Chrome website`, no raw domain, meditation and quiet alternatives, a 60-second emergency unlock wait, and no `Pause 15 min`.
- Verify analytics/privacy, Portable Profile, and package hygiene constraints remain intact for this slice.

Required output format:

1. `SCORE:` x/10
2. `VERDICT:` PASS / FAIL
3. `VISUAL REVIEW:` PASS / FAIL / NOT APPLICABLE
4. `BLOCKERS:` numbered list, or `None`
5. `R1 BLOCKER RECHECK:` PASS/FAIL with evidence
6. `BEDTIME CUSTOM APPS:` PASS/FAIL with evidence
7. `BEDTIME WEBSITE TARGETS:` PASS/FAIL with evidence
8. `SOFT/FIRM PRESERVATION:` PASS/FAIL with evidence
9. `ALTERNATIVES / MEDITATION:` PASS/FAIL with evidence
10. `PRIVACY / ANALYTICS:` PASS/FAIL with evidence
11. `TEST / EVIDENCE:` PASS/FAIL with exact files checked
12. `VISUAL REVIEW DETAILS:` concrete observations from screenshots/contact sheet
13. `BUNDLE GAPS:` only if needed
14. `PACKAGE HYGIENE:` PASS/FAIL and what should be removed from future bundles if anything is stale or confusing

Scoring gate:

- Return `SCORE: 10/10`, `VERDICT: PASS`, and `VISUAL REVIEW: PASS` only if there are no release-blocking behavior, privacy, evidence, visual, or package-hygiene gaps for Slice 26.5 R2.
- If any screenshot proves the wrong target class, if Bedtime website cannot be proven through the verified-host path, if forged website intents can reach the Bedtime website UI, if `Pause 15 min` remains in the Bedtime website flow, if raw domains/URLs appear in remote analytics metadata, or if package hygiene prevents audit, return `FAIL`.
