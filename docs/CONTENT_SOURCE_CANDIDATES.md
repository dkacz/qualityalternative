# Content Source Candidates

Status: `Sprint 7.1 working inventory`

This document is the first source-discovery pass for building a real English-language replacement library without writing first-party essays. It is not a final rights clearance ledger. It is a candidate map that says what looks promising, what should stay as link-only, and what should be rejected for now.

Copyright and licensing checks happen during content selection, triage, and inventory audit. The app runtime should follow explicit item metadata and should not add hidden copyright blocking.

## Source Classes

- `renderable_candidate`: plausible source for in-app reader content after item-level rights, edition, attribution, and jurisdiction checks.
- `link_only_candidate`: worthwhile recommendation source, but the app should open the canonical external page rather than reproduce or reader-mode the work.
- `reject_for_now`: not worth adding in Sprint 7 because rights, product fit, source quality, or implementation complexity is too uncertain.

## Working Selection Principles

- Prefer short, modular pieces that can plausibly replace a 5-12 minute social impulse.
- Prefer sources with stable URLs, explicit reuse terms, and attribution requirements we can satisfy.
- Prefer timeless topics: attention, self-command, practical philosophy, science literacy, history of ideas, environment, and human behavior.
- Do not use paywalled, subscriber-only, NonCommercial, unclear-license, scraped, or reader-mode extracted content as shared renderable inventory.
- Do not pad the library. A smaller pack of strong items is better than a broad but weak catalog.

## Candidate Inventory

| # | Candidate | Source family | Class | Product fit | Next triage action |
|---|---|---|---|---|---|
| 1 | Marcus Aurelius, `Meditations`, George Long translation | [Standard Ebooks](https://standardebooks.org/ebooks) / Project Gutenberg | `renderable_candidate` | Short reflective passages work well as anti-scroll resets. | Verify author and translator status for EU/Poland, choose edition, create excerpt-level duration estimates. |
| 2 | Epictetus, `Enchiridion`, older English translation | Standard Ebooks / Project Gutenberg | `renderable_candidate` | Direct practical self-command theme; easy to split into short entries. | Identify specific translation and translator death year before rendering. |
| 3 | Seneca, selected letters from `Letters from a Stoic` older translations | Project Gutenberg / Wikisource | `renderable_candidate` | Strong impulse-control fit, but translation choice matters. | Find a public-domain English translation safe in target jurisdictions. |
| 4 | John Stuart Mill, `On Liberty` selected chapters | Standard Ebooks / Project Gutenberg | `renderable_candidate` | Good for autonomy and intentional choice, but denser than ideal. | Select short sections only; estimate 7-12 minute cuts. |
| 5 | John Stuart Mill, `The Subjection of Women` selections | Standard Ebooks / Project Gutenberg | `renderable_candidate` | High-quality social thought, useful for deeper downtime. | Verify edition and choose concise passages with clear standalone value. |
| 6 | Ralph Waldo Emerson, `Self-Reliance` | Standard Ebooks / Project Gutenberg | `renderable_candidate` | Strong founder-user appeal around agency and attention. | Verify source text and cut into readable modules. |
| 7 | Ralph Waldo Emerson, `Nature` selections | Standard Ebooks / Project Gutenberg | `renderable_candidate` | Good contemplative alternative to social feeds. | Triage for accessible excerpts rather than full essay. |
| 8 | Henry David Thoreau, `Walking` | Standard Ebooks / Project Gutenberg | `renderable_candidate` | Excellent "put the phone down" thematic match. | Verify edition and split into short standalone readings. |
| 9 | Henry David Thoreau, `Life Without Principle` | Standard Ebooks / Project Gutenberg | `renderable_candidate` | Direct fit for reclaiming attention and values. | Check source text and cut down to non-sermonic excerpts. |
| 10 | Henry David Thoreau, `Walden` selections | Standard Ebooks / Project Gutenberg | `renderable_candidate` | Strong but can be too slow; works as curated excerpts. | Choose only high-signal passages for 5-10 minute sessions. |
| 11 | William James, `Habit` chapter from `The Principles of Psychology` | Project Gutenberg / Wikisource | `renderable_candidate` | Directly maps to behavior loops and habit interruption. | Verify source edition and produce a short modern-title wrapper note written by QA. |
| 12 | William James, `Pragmatism` lecture selections | Standard Ebooks / Project Gutenberg | `renderable_candidate` | Useful for action-oriented reflective reading. | Prefer only accessible sections; avoid overly academic slices. |
| 13 | William James, `The Varieties of Religious Experience` selections | Standard Ebooks / Project Gutenberg | `renderable_candidate` | Deep, meaningful, but long; could support premium library later. | Defer full inclusion unless a short excerpt is clearly compelling. |
| 14 | Charles Darwin, `The Voyage of the Beagle` selections | Standard Ebooks / Project Gutenberg | `renderable_candidate` | Observational science and wonder; good antidote to feeds. | Select vivid field-notes style passages with natural breaks. |
| 15 | Charles Darwin, `On the Origin of Species` selections | Standard Ebooks / Project Gutenberg | `renderable_candidate` | Historically important, but dense for impulse replacement. | Treat as later science/history pack unless excerpts test well. |
| 16 | Mary Austin, `The Land of Little Rain` | Standard Ebooks / Project Gutenberg | `renderable_candidate` | Lyrical nature writing with short, vivid chapters. | Verify source and select one or two place-based pieces. |
| 17 | Booker T. Washington, `Up From Slavery` selections | Standard Ebooks / Project Gutenberg | `renderable_candidate` | Practical resilience and biography, good for purposeful downtime. | Select self-contained passages; avoid making biography feel like homework. |
| 18 | Henry Adams, `The Education of Henry Adams` selections | Standard Ebooks / Project Gutenberg | `renderable_candidate` | Strong history-of-ideas value, but less immediate. | Keep as later library candidate, not first pack default. |
| 19 | Francis Bacon, selected essays | Project Gutenberg / Wikisource | `renderable_candidate` | Very short, aphoristic, and suitable for finite recommendations. | Verify edition and choose readable titles like study, friendship, or habit. |
| 20 | Michel de Montaigne, selected essays in older English translation | Project Gutenberg / Wikisource | `renderable_candidate` | Excellent reflective material, but translation rights must be precise. | Identify translator and edition; likely later pack after verification. |
| 21 | Leo Tolstoy, `A Confession`, older English translation | Project Gutenberg / Standard Ebooks if available | `renderable_candidate` | Meaning-heavy alternative, but emotionally intense. | Verify translator and use only if tone fits calm intervention. |
| 22 | Laozi, `Tao Te Ching`, older English translation | Project Gutenberg / Wikisource | `renderable_candidate` | Short contemplative passages fit the product beautifully. | Translation rights are the whole issue; verify before considering. |
| 23 | `The Dhammapada`, older English translation | Project Gutenberg / Wikisource | `renderable_candidate` | Short passages about craving and attention are highly relevant. | Verify translation and add context carefully to avoid sloppy spiritual packaging. |
| 24 | Our World in Data, `Energy` explainers | [Our World in Data](https://ourworldindata.org/) | `renderable_candidate` | Clear modern knowledge snack; strong for science-curious users. | Select specific CC BY pages or excerpts; preserve attribution, source URL, license URL, and modification note. |
| 25 | Our World in Data, `Climate Change` explainers | Our World in Data | `renderable_candidate` | High-value alternative to outrage feeds if kept concise. | Select specific CC BY pages or excerpts; do not create QA-authored summaries as replacement content. |
| 26 | Our World in Data, `Life Expectancy` / health progress explainers | Our World in Data | `renderable_candidate` | Positive perspective reset; likely strong replacement content. | Select specific CC BY pages or excerpts; avoid chart-dependent material unless third-party data terms are clear. |
| 27 | Our World in Data, `Mental Health` explainers | Our World in Data | `renderable_candidate` | Relevant to smartphone self-regulation, but sensitive. | Avoid medical advice framing; keep attribution and source link prominent. |
| 28 | Our World in Data, `Demographic Change` explainers | Our World in Data | `renderable_candidate` | Big-picture learning in short sessions. | Pick only accessible pieces and avoid chart dependency. |
| 29 | Our World in Data, `Artificial Intelligence` explainers | Our World in Data | `renderable_candidate` | Timely, educational, strong for tech testers. | Verify license on selected pages and avoid fast-aging claims if offline. |
| 30 | NASA Earth Observatory, carbon cycle features | [NASA Earth Observatory](https://earthobservatory.nasa.gov/) | `renderable_candidate` | Visual science and Earth systems can replace scroll with wonder. | Check each feature for copyright exceptions and third-party imagery before rendering. |
| 31 | NASA Earth Observatory, remote sensing explainers | NASA Earth Observatory | `renderable_candidate` | Practical science literacy, visually rich. | Use only items with clear reuse terms; decide if image rights are included. |
| 32 | NOAA Climate.gov explainers | [NOAA Climate.gov](https://www.climate.gov/) | `renderable_candidate` | Strong for climate/science pack, but asset provenance varies. | Check item-level credits; avoid third-party copyrighted assets. |
| 33 | OpenStax psychology or biology chapter excerpts | [OpenStax](https://openstax.org/) | `renderable_candidate` | Rights are promising, but textbook tone may be weak in impulse moment. | Test only if a short chapter section can be made compelling without heavy adaptation. |
| 34 | SAPIENS anthropology articles | [SAPIENS](https://www.sapiens.org/) | `link_only_candidate` | High-quality human sciences; excellent "learn something real" handoff. | Keep external unless article-level license and no-derivatives constraints are cleared for exact republication. |
| 35 | The Conversation explainers | [The Conversation](https://theconversation.com/) | `link_only_candidate` | Accessible academic journalism, broad topic coverage. | Treat as external handoff by default because article terms are specific and often no-derivatives. |
| 36 | Stanford Encyclopedia of Philosophy entries | [Stanford Encyclopedia of Philosophy](https://plato.stanford.edu/) | `link_only_candidate` | Authoritative but long; useful as deep links, not in-app replacements. | Recommend selected entries externally; do not reproduce full text. |
| 37 | Internet Encyclopedia of Philosophy entries | [IEP](https://iep.utm.edu/) | `link_only_candidate` | Useful for philosophy-curious users, but still encyclopedia-like. | External handoff only unless explicit reuse permission is obtained. |
| 38 | Aeon / Psyche essays | [Aeon](https://aeon.co/) | `link_only_candidate` | Strong product fit for long-form reflective reading. | External handoff only unless using formal republishing path with exact terms. |
| 39 | Nautilus essays | [Nautilus](https://nautil.us/) | `link_only_candidate` | High-quality science/culture reading. | External handoff only; do not cache or reader-mode. |
| 40 | Quanta Magazine explainers | [Quanta Magazine](https://www.quantamagazine.org/) | `link_only_candidate` | Excellent science reading, often beautiful and evergreen. | External handoff only unless explicit license/permission exists. |
| 41 | Long Now essays and talks | [Long Now](https://longnow.org/) | `link_only_candidate` | Strong antidote to short-term social feeds. | External handoff only; verify terms before any deeper integration. |
| 42 | Substack posts from ambitious writers | Substack | `link_only_candidate` | Potentially excellent personalized recommendations. | User-added/private or external handoff only unless author grants permission. |
| 43 | Modern copyrighted books or book chapters without license | Publishers / book previews | `reject_for_now` | Often high quality, but not safe shared inventory. | Do not add until licensed, public-domain, or user-private. |
| 44 | NonCommercial Creative Commons essays | Various | `reject_for_now` | Some may be good, but conflicts with future monetization. | Exclude from shared renderable inventory unless separate permission is granted. |
| 45 | Paywalled or subscriber-only articles | Various | `reject_for_now` | Bad user experience and rights risk. | Do not recommend as shared default inventory; allow only user-private manual links later if appropriate. |
| 46 | Reader-mode extracted web articles | Various | `reject_for_now` | Would make the product behave like a scraper/read-later clone. | Explicitly avoid for shared inventory. |

## Strongest Sprint 7 Pack Direction

The strongest first pack is likely a mixed `renderable_candidate` pack built from public-domain classics and one modern open source with explicit reuse terms:

- Attention and self-command: Epictetus, Marcus Aurelius, William James, Thoreau.
- Wonder and science literacy: Mary Austin, Darwin, NASA Earth Observatory, Our World in Data.
- Agency and values: Emerson, Mill, Bacon.

This gives the product a distinctive taste without pretending to own the works. It also avoids the trap of becoming a generic web recommender.

## Recommended 7.2 Shortlist

Start with 10 items and cut aggressively if triage is uncertain:

- Marcus Aurelius, `Meditations`, selected short passage.
- Epictetus, `Enchiridion`, selected short passage.
- William James, `Habit`, one short section.
- Thoreau, `Walking`, one short section.
- Thoreau, `Life Without Principle`, one short section.
- Emerson, `Self-Reliance`, one short section.
- Mary Austin, `The Land of Little Rain`, one short section.
- Francis Bacon, one selected essay.
- Our World in Data, one chart-light progress explainer.
- NASA Earth Observatory, one item-level-cleared Earth systems explainer.

If the public-domain translation/edition checks take longer than expected, the fallback for 7.2 should be a `link_only` pack with article-shaped sources such as SAPIENS, The Conversation, Aeon/Psyche, Quanta, Nautilus, and Long Now. Stanford Encyclopedia and Internet Encyclopedia of Philosophy should remain later deep-reference sources unless Slice 7.2 identifies a specific bounded section with a credible 5-12 minute reading time. The fallback should still preserve the one-primary-plus-two-backups intervention shape.

## Source Notes Checked

- [Standard Ebooks collections policy](https://standardebooks.org/contribute/collections-policy): Standard Ebooks works on U.S. public-domain books, so EU/Poland and translator checks remain our responsibility before rendering.
- [Project Gutenberg license](https://www.gutenberg.org/policy/license.html): book text may be unrestricted by U.S. copyright law, while Project Gutenberg trademark/license material needs care.
- [Our World in Data reuse guidance](https://ourworldindata.org/how-to-use-our-world-in-data-visualizations-in-presentations/): OWID charts/content are generally reusable with attribution when marked as OWID/CC BY, but underlying third-party data can have separate terms.
- [NASA Earth Observatory image use policy](https://earthobservatory.nasa.gov/image-use-policy): many materials are reusable, including commercially, except where copyright is indicated.
- [NOAA Climate.gov FAQs](https://www.climate.gov/faqs): third-party assets require direct permission from their source.
- [OpenStax licensing](https://help.openstax.org/s/article/Openstax-textbook-licensing-and-customization): the library uses Creative Commons licenses, but some books are CC BY while others are CC BY-NC-SA, so item-level checks are required before renderable use.
- [The Conversation editorial/republication guidance](https://cdn.theconversation.com/static_files/files/1976/Global_Editorial_Guidelines.pdf): The Conversation emphasizes Creative Commons republication, but article-level terms and no-derivatives constraints make link-only the safer default.
- [SAPIENS republishing guidelines](https://www.sapiens.org/republish/): many articles use CC BY-ND 4.0, which should remain link-only by default unless exact unmodified republication is deliberately reviewed.
- [Aeon terms of use](https://aeon.co/terms-of-use): Aeon content is protected except where republishing terms permit use.
- [Quanta terms and conditions](https://www.quantamagazine.org/terms-conditions): Quanta content is protected and reserved unless permission or legal exception applies, so it belongs in link-only recommendations.
- [Nautilus terms of service](https://nautil.us/terms-of-service/): Nautilus content is protected by copyright and should remain link-only unless permission is obtained.
