You are doing a fresh-from-scratch adversarial GPT Pro audit of the Sprint 26 final release gate for Quality Alternative.

GUIDING PRINCIPLES (respect these in your review):
1. Numbers in the manuscript are verified against pipeline CSVs, so do not question arithmetic without checking the shipped data files.
2. Do not suggest weakening claims unless you can name the concrete referee attack that the hedge would preempt.
3. Style suggestions cannot change empirical meaning.
4. The model is presented as-is, so do not reference development history or hidden correction history.
5. Figures, tables, and prose must be consistent; mismatches should be flagged specifically.
6. Your feedback is input, not instruction; duplicate or already-covered suggestions should not be inflated into fresh findings.

Read the FULL attached primary files first:
- `docs/SPRINT_26_CUSTOM_TARGETS_WEBSITE_INTERVENTIONS.md`
- `docs/release-gate-logs/2026-06-07-sprint26-custom-targets-website-interventions/VALIDATION_SUMMARY.md`
- `docs/release-gate-logs/2026-06-07-sprint26-custom-targets-website-interventions/RELEASE_NOTES_v0.11.14-custom-targets-website-interventions-alpha.md`

Then deep-review only this scope:
Sprint 26 final release readiness for custom installed-app targets, supported Chrome website/domain interventions, bedtime integration, privacy/analytics/profile hardening, visual evidence, and APK package readiness.

This is R2 of the final release review. R1 returned `SCORE 8/10`, `VERDICT REVISE`, `VISUAL REVIEW PASS`. Actively recheck the R1 blockers:
- Release notes must distinguish remote/export analytics payloads from device-local analytics rows.
- Validation summary and bundle manifest must be consistent about excluding the superseded failed first connected gate from the GPT Pro review ZIP while retaining it only on disk as non-canonical audit history.

Bundle rules:
- Use only the shipped bundle as the audit base.
- If a claim cannot be proven from shipped files, label it `BUNDLE GAP`.
- Treat `connected_debug_android_test_r2_scrubbed.log` and `connected_debug_android_test_r2.status.txt` as the canonical final connected gate.
- `connected_debug_android_test.log` is an intentionally retained superseded failed first attempt on disk but is intentionally excluded from the R2 review ZIP. Do not treat it as the release result unless the R2 rerun evidence is missing or invalid.
- The APK binary is not inside this review bundle; inspect APK readiness from `apk_badging.txt`, `apk_signature_verify.txt`, `apk_install_evidence/`, and the SHA-256 sidecar path named in `VALIDATION_SUMMARY.md`.

Known prior bug classes to actively test against:
- Default intervention mode must remain `SOFT`; firm-mode five-second unlock must be explicit and preserved when selected.
- Website interventions must be Chrome verified-host only, not generic URL spying.
- Typed-but-not-loaded, stale, unsupported, unreadable, synthetic, and package-mismatched states must not trigger website interventions.
- Remote/export analytics must not emit URL, host, page title, package label, package id, or rule id. Device-local analytics rows may retain package-level fields required for local intervention behavior.
- Bedtime must remain a full block, but alternatives and meditation must stay available and visually distinct.
- Visual evidence must not hide controls behind bottom bars or overlap finite choices.
- Package evidence must prove version, signature verification, emulator install, and launch smoke.

Your job:
1. Verify release readiness from the shipped validation logs, test XML, screenshots, APK metadata, signature, install, and launch evidence.
2. Verify per-slice gate continuity from the latest GPT Pro reviews shipped in the bundle.
3. Audit the visual screenshots for obvious layout, clipping, overlap, or misleading UX regressions.
4. Audit package hygiene: stale logs must be clearly labeled, no critical evidence should be missing, and the bundle should not be misleading.
5. Decide if this is safe to commit, tag, push, and publish as `v0.11.14-custom-targets-website-interventions-alpha`.

Output format:
- `SCORE:` 0-10
- `VERDICT:` PASS / REVISE / BLOCK
- `VISUAL REVIEW:` PASS / REVISE / BLOCK
- `BLOCKERS:` numbered list or `None`
- `RELEASE READINESS:`
- `APK / SIGNATURE / INSTALL:`
- `PER-SLICE GATE CHECK:`
- `CUSTOM APP TARGETS:`
- `WEBSITE RULES / CHROME VERIFIED-HOST:`
- `BEDTIME / SOFT / FIRM:`
- `PRIVACY / ANALYTICS / PROFILE:`
- `TEST / EVIDENCE:`
- `R1 BLOCKER RECHECK:`
- `BUNDLE GAPS:`
- `PACKAGE HYGIENE:`
