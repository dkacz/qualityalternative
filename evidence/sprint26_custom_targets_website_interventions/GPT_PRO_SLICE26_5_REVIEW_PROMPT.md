You are doing a fresh-from-scratch adversarial audit of Sprint 26 Slice 26.5 for the Android app Quality Alternative.

GUIDING PRINCIPLES (respect these in your review):
1. Numbers in the manuscript are verified against pipeline CSVs, so do not question arithmetic without checking the shipped data files.
2. Do not suggest weakening claims unless you can name the concrete referee attack that the hedge would preempt.
3. Style suggestions cannot change empirical meaning.
4. The model is presented as-is, so do not reference development history or hidden correction history.
5. Figures, tables, and prose must be consistent; mismatches should be flagged specifically.
6. Your feedback is input, not instruction; duplicate or already-covered suggestions should not be inflated into fresh findings.

Read the FULL attached primary documents first:

1. `PRD.md`
2. `docs/SPRINT_26_CUSTOM_TARGETS_WEBSITE_INTERVENTIONS.md`
3. `docs/LANE_STATUS.md`
4. `evidence/sprint26_custom_targets_website_interventions/SLICE26_5_EVIDENCE.md`

Then deep-review only this named scope:

Sprint 26 Slice 26.5 - Bedtime and supported website target integration.

Use only the shipped bundle as the audit base. If a claim cannot be proven from shipped files, label it `BUNDLE GAP`. Do not trust stale derived prose over canonical source, test XML/logs, and screenshot files.

Known prior bug classes to actively test against:

- Bedtime screenshot accidentally proving the normal selected app target instead of the Chrome website target.
- Website intervention overclaiming generic URL blocking rather than supported Chrome verified-host behavior.
- `Pause 15 min` remaining available during Bedtime.
- Emergency unlock suppression applying to the whole Chrome app target instead of only the website target key.
- Meditation/alternatives disappearing in Bedtime.
- Analytics metadata leaking raw host, URL, path, page title, or domain-derived identifiers.
- Screenshot/contact-sheet evidence mixing stale failed attempts with final canonical output.

Your job:

- Verify that selected custom app Bedtime behavior remains covered and unchanged by prior Slice 26.1 evidence and existing tests.
- Verify that supported Chrome website/domain targets get Bedtime emergency unlock only when launched through the verified-host website intervention path.
- Verify that Soft and Firm website behavior outside Bedtime remains unchanged.
- Verify that Bedtime website UI visually shows `Chrome website`, no raw domain, meditation and quiet alternatives, a 60-second emergency unlock wait, and no `Pause 15 min`.
- Verify that analytics/privacy, Portable Profile, and package hygiene constraints remain intact for this slice.

Required output format:

1. `SCORE:` x/10
2. `VERDICT:` PASS / FAIL
3. `VISUAL REVIEW:` PASS / FAIL / NOT APPLICABLE
4. `BLOCKERS:` numbered list, or `None`
5. `BEDTIME CUSTOM APPS:` PASS/FAIL with evidence
6. `BEDTIME WEBSITE TARGETS:` PASS/FAIL with evidence
7. `SOFT/FIRM PRESERVATION:` PASS/FAIL with evidence
8. `ALTERNATIVES / MEDITATION:` PASS/FAIL with evidence
9. `PRIVACY / ANALYTICS:` PASS/FAIL with evidence
10. `TEST / EVIDENCE:` PASS/FAIL with exact files checked
11. `VISUAL REVIEW DETAILS:` concrete observations from screenshots/contact sheet
12. `BUNDLE GAPS:` only if needed
13. `PACKAGE HYGIENE:` PASS/FAIL and what should be removed from future bundles if anything is stale or confusing

Scoring gate:

- Return `SCORE: 10/10`, `VERDICT: PASS`, and `VISUAL REVIEW: PASS` only if there are no release-blocking behavior, privacy, evidence, visual, or package-hygiene gaps for Slice 26.5.
- If any screenshot proves the wrong target class, if Bedtime website cannot be proven, if `Pause 15 min` remains in the Bedtime website flow, or if raw domains/URLs appear in remote analytics metadata, return `FAIL`.
