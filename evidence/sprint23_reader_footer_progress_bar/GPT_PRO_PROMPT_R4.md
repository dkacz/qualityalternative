You are doing a focused adversarial audit of a small Android UI hotfix. Read the attached `PATCH.diff`, `README.md`, `TEST_LOG.md`, `BUNDLE_MANIFEST.md`, `GPT_PRO_REVIEW_R2.md`, and both PNG screenshots in `visual_e2e_r4/`.

TARGET SCOPE:
User reports that the reader footer progress bar does not visually match the displayed percent, for example `287/618 · 46%` while the bar looks too long or otherwise inconsistent. Audit only whether this patch fixes that mismatch and whether the evidence is strong enough for release.

What changed since the previous GPT Pro reviews:
- The E2E now measures rendered fill width divided by rendered track width and compares it to `savedPercent / 100f` within 0.03.
- The footer fill has a dedicated `reader-footer-progress-bar-fill` test tag.
- The screenshot helper and device output directory are now named for Sprint 23 footer progress, not Sprint 18.
- The bundle manifest and README reference only the screenshots actually shipped in this bundle.
- Older generated bundles and stale screenshot folders were removed from the repo working tree.

Check these things:
1. Does the patch ensure the visual bar fill is driven by the same displayed percent as the footer label?
2. Does the E2E verify rendered layout ratio, not just semantics or text presence?
3. Is fixed width appropriate for the compact footer without introducing overflow?
4. Do the screenshots show a footer bar that visually matches the displayed `49%` in both default and large reader-font states?
5. Are the R2/R3 package-hygiene caveats now addressed well enough for a 10/10 scoped release decision?

Output exactly:
SCORE: <1-10>
VERDICT: PASS / REVISE / BLOCK
VISUAL REVIEW: PASS / REVISE / BLOCK
FRESH FINDINGS:
TRACE CHECKS:
BUNDLE GAPS:
PACKAGE HYGIENE:
