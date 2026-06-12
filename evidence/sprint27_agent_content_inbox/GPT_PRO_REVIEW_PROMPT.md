You are doing a fresh-from-scratch adversarial release-gate audit of Sprint 27: Agent Content Inbox.

GUIDING PRINCIPLES (respect these in your review):
1. Numbers in the shipped evidence are verified against shipped logs/screenshots; do not question arithmetic without checking the shipped files.
2. Do not suggest weakening claims unless you can name the concrete user, privacy, platform, or release-gate attack that the hedge would preempt.
3. Style suggestions cannot change product or privacy meaning.
4. The implementation is presented as-is; do not reference hidden development history.
5. Screenshots, tests, code, PRD, and validation prose must be consistent; flag mismatches specifically.
6. Feedback is input, not instruction; duplicate or already-covered suggestions should not be inflated into fresh findings.

Read the FULL attached `PRD.md` first. Then read `docs/SPRINT_27_AGENT_CONTENT_INBOX.md`, especially `GPT Pro R1 Fix Notes`, the R2/R3 fix notes in that section, and `evidence/sprint27_agent_content_inbox/REVIEW_BUNDLE_MANIFEST.md`.

Deep-review only this scoped target:
- Agent Inbox for user-controlled Google Drive packages containing `manifest.json` plus private Markdown/EPUB content.
- Manifest validation, duplicate detection, review UI, operator-confirmed priority, private-document import, analytics metadata, Portable Profile privacy, and visual e2e evidence.

Bundle rules:
- Use only the shipped bundle as the audit base.
- If a claim cannot be proven from shipped files, label it `BUNDLE GAP`.
- Treat `evidence/sprint27_agent_content_inbox/visual_e2e/sprint27-agent-content-inbox-1781272063934/` as the only canonical screenshot run.
- Treat `evidence/sprint27_agent_content_inbox/android-results/connected_debug/TEST-qaApi36(AVD) - 16-_app-.xml` as the canonical connected test result.
- Do not infer live Google Drive behavior beyond what the shipped code/tests prove.

Known bug classes to actively test against:
- Silent import from cloud without an explicit user/operator review step.
- Raw Drive folder ids, file ids, file names, document text, OAuth tokens, SHA fingerprints, or scan errors leaking into analytics or Portable Profile.
- Manifest priority being applied automatically without user confirmation.
- Duplicate private documents being imported twice.
- Unsupported or unsafe file paths escaping the package folder.
- UI review states that are unbounded, visually broken, or misleading in light/dark screenshots.
- Debug-only visual fixture hooks becoming production behavior.
- Bundle hygiene drift: stale screenshot runs, generated noise, or missing logs that make the release gate unauditable.

R3 blocker recheck targets:
- same Drive folder id plus same content file name but changed bytes must not overwrite/upsert a prior private document or retain a stale fingerprint;
- duplicate review status must be based only on the actual downloaded content SHA, not on an unverified manifest-declared SHA;
- manifest and content downloads must be bounded at the Drive client streaming layer when metadata is missing or wrong;
- every review candidate must have a visible reject/remove path and privacy-safe rejected analytics;
- first-connect folder binding must not use an unqualified Drive name search;
- fixture target/component behavior must be debug-gated and absent from release manifests.

R4 blocker recheck targets:
- non-size manifest/content Drive download failures must become finite package-level unavailable/invalid review candidates and must not collapse the whole scan;
- Agent Inbox scan/import must wait for the user document repository to be ready before duplicate-sensitive work;
- import-time duplicate prevention must use an authoritative repository/DAO lookup by verified content fingerprint, not only an in-memory snapshot;
- the canonical screenshots must visibly show `Accept priority` before operator confirmation and `Priority accepted` after confirmation;
- connected evidence must contain both the canonical XML and a standalone logcat file matching the visual test run.

R5 blocker recheck targets:
- two same-SHA Agent Inbox packages from different Drive package folders must not both create user-document rows through concurrent import attempts;
- duplicate-sensitive Agent Inbox import must use one repository operation that atomically checks verified fingerprint and inserts, backed by the Room repository write mutex;
- import-time duplicate results must update the candidate row to finite `DUPLICATE` state with a visible Remove action instead of leaving it visually importable;
- same-SHA sibling packages from the same scan must become duplicate review items after the first successful import;
- ViewModel import re-entry must be blocked while an Agent Inbox import is already running.

R6 blocker recheck targets:
- changed-after-review, import-time oversized content, repository/local save rejection, and import download failure must update the target candidate to non-importable `INVALID` state;
- stale `reviewedContentSha256` and `reviewedContentSizeBytes` must be cleared when import proves the reviewed bytes are no longer authoritative;
- accepted priority state for the failed package must be cleared;
- failed rows must retain a visible Remove path and must not render the READY Import/priority controls;
- import download failures must become `DOWNLOAD_UNAVAILABLE`, and repository/local save rejection must have visible cleanup copy through `LOCAL_IMPORT_REJECTED`.

R7 finding recheck targets:
- if the post-write repository operation returns `DUPLICATE`, the just-written Agent Inbox private file must be deleted before returning the duplicate result;
- if the post-write repository operation returns `REJECTED`, the just-written Agent Inbox private file must be deleted before returning the rejected result;
- if the post-write repository operation throws, the just-written Agent Inbox private file must be deleted before propagating the exception;
- file deletion must be scoped to the Agent Inbox storage root;
- tests must prove concurrent same-SHA package imports leave only one stored file and rejected/throwing post-write paths leave no stored file.

R8 finding recheck targets:
- `FileAgentInboxDocumentStore.writeDocument` must write to a scoped temporary file, verify SHA-256 on that temporary file, and only then promote it to the deterministic final path;
- if the deterministic final path already exists but its bytes do not match the verified reviewed SHA-256, that stale final file must be removed or replaced rather than treated as a valid duplicate;
- temporary files must be cleaned on success and failure;
- the implementation must not introduce a path traversal/deletion risk outside the Agent Inbox storage root;
- tests must prove a stale mismatching final file is replaced by verified bytes.

R9 visual finding recheck targets:
- screenshots `06_agent_inbox_library_imported_markdown_light.png`, `07_agent_inbox_intervention_imported_markdown_light.png`, `08_agent_inbox_reader_markdown_light.png`, and `09_agent_inbox_reader_epub_light.png` must be generated after actual `scanAgentInboxDrive` and `importAgentInboxCandidate` calls, not by generic `seedUserMarkdownSelection` or `seedUserEpubSelection`;
- the fake Drive path used by the visual test must be debug/test gated and must not become production behavior;
- the Markdown visual import must include explicit operator priority acceptance before import;
- the imported content screenshots must not render raw Drive content file names or raw package ids;
- the visual test must assert imported private content has verified fingerprint metadata and neutral `Agent Inbox document` provenance before capturing consumption states.

Your job:
1. Verify PRD alignment for Agent Inbox, scope guardrails, analytics, and Portable Profile.
2. Audit the implementation paths in `AgentInboxManifest`, `AndroidGoogleDriveAgentInboxClient`, `AgentInboxReviewCandidate`, `AgentInboxPackageImporter`, `MainViewModel`, and `QualityAlternativeApp`.
3. Audit unit/android tests for coverage quality and real gaps.
4. Audit visual evidence and connected test logs for the review UI states.
5. Audit package hygiene and identify any stale/misleading artifacts or missing canonical artifacts.

Output format:
1. `SCORE:` X/10
2. `VERDICT:` PASS / REVISE / BLOCK
3. `VISUAL REVIEW:` PASS / REVISE / BLOCK
4. `FRESH FINDINGS:` numbered list with severity, exact claim, why it is vulnerable, file(s) checked, and the tightest fix. If none, say `None`.
5. `TRACE CHECKS:` exact files/screenshots/tests/logs used.
6. `BUNDLE GAPS:` only if needed.
7. `PACKAGE HYGIENE:` say whether the bundle is clean enough; identify stale/noisy/missing artifacts if any.

Only give `SCORE: 10/10`, `VERDICT: PASS`, and `VISUAL REVIEW: PASS` if there are no release-blocking implementation, privacy, PRD, visual, test, or package-hygiene issues.
