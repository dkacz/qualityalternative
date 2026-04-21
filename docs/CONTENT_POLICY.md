# Content Policy

Status: `active product decision`

This document defines how Quality Alternative can source, store, render, and recommend replacement content. It is a product and implementation policy, not legal advice. Before a public paid launch, the policy should be reviewed by an IP lawyer in the target launch jurisdictions.

This policy is enforced at content selection, triage, metadata review, and inventory audit time. The app should not contain hidden runtime copyright blockers that override the configured reading flow for a user; runtime should follow explicit item metadata and log enough context for audit.

## Core Principle

The product should separate content rights from recommendation value.

Users should pay, if they ever pay, for curation, timing, personalization, private-library workflow, offline/readability features, licensed collections, and the behavior loop around impulse replacement. The product should not imply that users are paying for exclusive access to public-domain or freely available third-party works.

## Content Classes

### `renderable`

The app may show the full work inside the Quality Alternative reader when the product has a clear basis to reproduce and display the content.

Allowed renderable sources:

- First-party content written by the product team or commissioned under a written work-for-hire/license agreement.
- Partner/licensed content with explicit in-app reproduction rights.
- Verified public-domain works, with jurisdiction checked before inclusion.
- CC0 works.
- Public Domain Mark works only when the status appears globally safe, not merely public domain in one jurisdiction.
- Creative Commons works that allow commercial reuse, usually `CC BY` or `CC BY-SA`, with complete attribution.

Constraints:

- `CC BY-SA` requires care around adaptations because derivative works may need the same or a compatible license.
- `CC BY-ND` should generally remain unmodified. Do not translate, summarize, excerpt heavily, or adapt it unless legal review confirms the use.
- `CC-NC` content should not be used as product-rendered inventory for a startup that may monetize the app, unless there is separate permission.
- Every renderable item must carry rights metadata before pilot distribution.

### `link_only`

The app may recommend the work and send the user to the original source, but should not reproduce the full text in the Quality Alternative reader.

Default link-only sources:

- Substack posts without an explicit reuse license.
- Independent blogs without an explicit reuse license.
- Magazine or newspaper articles.
- Public web essays where the author has made the page readable but has not granted reuse rights.
- Any source behind a paywall, login, email gate, or subscriber-only restriction.

Allowed product behavior:

- Store title, author/publication name, canonical URL, topic tags, estimated duration, and a short product-written recommendation note.
- Open the canonical URL through a browser/custom tab or clearly labeled external handoff.
- Let the user add the link to their private local inventory.

Disallowed product behavior without permission:

- Scraping, caching, or rehosting the full text.
- Reader-mode extraction that makes the external page look like an in-app work.
- Circumventing paywalls, subscriber gates, robots restrictions, or technical access controls.
- Presenting a generated summary as a substitute for the source when the summary depends on scraping the full work.

### `user_private`

The app may display user-provided files or links only to the user who added them.

Examples:

- User-added links.
- User-uploaded PDFs.
- Future user-uploaded EPUBs or reading files.

Constraints:

- The product should treat this as private workflow tooling, not shared content distribution.
- Terms should state that the user is responsible for having the right to store and view uploaded material.
- User-private content should not be made available to other users, used as shared inventory, or repackaged into the editorial library without separate permission.

## Public Domain Rules

Public domain is jurisdiction-specific and should not be inferred from age alone.

Operational defaults:

- For EU/Poland-facing use, assume author-life-plus-70-years as the main baseline for literary and artistic works.
- For U.S. works, publication year matters. As of 2026, U.S. works first published before 1931 are generally public domain under the U.S. Copyright Office's public-domain timing.
- Translations, introductions, annotations, illustrations, editions, and recordings can have separate rights even when the underlying work is public domain.
- Do not use modern editions from publishers as renderable assets unless the edition itself is cleared.

Required metadata for public-domain inventory:

- author
- original title
- publication year, when known
- author death year, when relevant
- source URL
- jurisdiction note
- edition/source note
- rights status
- date reviewed

## Creative Commons Rules

Operational defaults:

- `CC0`: safe default for renderable inventory, with attribution still preferred as an ethical product norm.
- `CC BY`: acceptable if attribution is complete and durable.
- `CC BY-SA`: acceptable only if we are comfortable with share-alike obligations for adaptations.
- `CC BY-ND`: link-only by default unless the work is shown unmodified and legal review approves in-app rendering.
- `CC BY-NC`, `CC BY-NC-SA`, `CC BY-NC-ND`: do not use as renderable shared inventory for any product path that may monetize the app.

Required attribution fields:

- creator
- work title
- source URL
- license name
- license URL
- modification note

## Premium Packaging Position

Free tier may include:

- core interception
- basic renderable starter content
- limited user-private inventory
- link-only recommendations to public web sources

Premium may include:

- broader curated library where rights are cleared or public-domain/CC status is documented
- higher or unlimited user-private content capacity
- better personalization and offline/readability workflow
- partner/licensed collections
- advanced intervention modes after soft intervention value is proven

Premium should not claim exclusive ownership of public-domain works or charge for public-domain access as such. The paid value is the quality replacement system around the content.

## Implementation Requirements

Every `ContentItem` that is not purely user-private should eventually carry:

- `rightsClass`: `RENDERABLE`, `LINK_ONLY`, or `USER_PRIVATE`
- `licenseName`
- `licenseUrl`
- `sourceUrl`
- `attribution`
- `rightsReviewedAt`
- `renderMode`: `IN_APP_READER`, `EXTERNAL_HANDOFF`, or `USER_PRIVATE_READER`

Before external pilot:

- Existing editorial starter-pack metadata must not imply affiliation with real publications unless the content is actually licensed from or published by them.
- Synthetic/editorial placeholder works should be labeled as Quality Alternative editorial material.
- Link-only sources must stay in handoff mode.
- Public-domain or CC starter packs must include an attribution ledger.

## Sources Checked

- Creative Commons license explanations: https://creativecommons.org/cc-licenses/
- Creative Commons CC0: https://creativecommons.org/public-domain/cc0/
- Creative Commons Public Domain Mark: https://creativecommons.org/public-domain/pdm/
- U.S. Copyright Office copyright duration guidance: https://www.copyright.gov/what-is-copyright/
- U.S. Copyright Office Circular 15A: https://www.copyright.gov/circs/circ15a.pdf
- EU Directive 2006/116/EC via WIPO Lex: https://www.wipo.int/wipolex/en/legislation/details/6394
- CJEU GS Media hyperlinking press release: https://curia.europa.eu/jcms/upload/docs/application/pdf/2016-09/cp160092en.pdf
- Substack fair use guidance: https://support.substack.com/hc/en-us/articles/8416262138644-What-is-fair-use
