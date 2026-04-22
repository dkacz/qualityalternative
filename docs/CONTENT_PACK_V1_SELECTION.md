# Content Pack v1 Selection

Status: `Sprint 7.2 passed Pro review; first eight items integrated in Slice 7.3`

Pack id: `attention-classics-v1`

Pack promise: short public-domain readings that interrupt an automatic social-media open with something more spacious: self-command, attention, deliberate living, and wonder. This is a renderable-candidate pack, not a final asset ledger. The next slice may render these items in-app only after excerpt boundaries and attribution metadata are carried into assets.

This pack deliberately avoids modern magazine articles, Substack posts, paywalled sources, reader-mode extraction, AI summaries, and QA-authored essays. The product may write short titles, descriptions, and "why this now" notes, but the replacement reading itself should come from the selected source text.

## Selection Criteria

- English-language source text.
- Existing public-domain or public-domain-adjacent source with stable public URL.
- Strong fit for a 5-12 minute impulse replacement.
- Modular enough to become one reading unit without turning into a feed or long anthology.
- Rights path can be explained with source, author/translator status, attribution, and review date.
- No item depends on charts, third-party images, contemporary copyrighted commentary, or external scraping.

## Selected Pack

| # | Working title | Source work and range | Author / translator | Source URL | License / status | Duration | Topic tags | Triage status | Why this now? |
|---|---|---|---|---|---|---:|---|---|---|
| 1 | Start With What Is Yours | `Encheiridion`, opening sections from `A Selection from the Discourses of Epictetus with the Encheiridion` | Epictetus; translated by George Long | https://www.gutenberg.org/ebooks/10661 | Public-domain text sourced from Project Gutenberg; likely EU/Poland safe after translator check because George Long died in 1879. Remove Project Gutenberg boilerplate before app rendering. | 3 min | stoicism, agency, impulse-control | `integrated` | The first question at the moment of impulse is whether this action is actually in the user's control and interest. |
| 2 | The Morning Test | `Meditations`, Book V selections from Project Gutenberg #2680 | Marcus Aurelius; translated by George Long | https://www.gutenberg.org/ebooks/2680 | Public-domain text sourced from Project Gutenberg; underlying Long translation is public-domain candidate after final edition check. | 3 min | stoicism, morning, attention, judgment | `integrated` | A compact reset for the exact moment when the phone becomes a reflex rather than a decision. |
| 3 | The Flywheel of Habit | `The Principles of Psychology`, Chapter IV, `Habit`, selected non-technical passage | William James | https://www.gutenberg.org/ebooks/57628 | Public-domain text sourced from Project Gutenberg; author died in 1910, so likely EU/Poland safe. Remove Project Gutenberg boilerplate before app rendering. | 4 min | habits, psychology, behavior-change | `integrated` | It explains why small repeated actions become grooves, which directly mirrors the product's habit-interruption thesis. |
| 4 | Live Deliberately | `Walden`, `Where I Lived, and What I Lived For`, selected passage | Henry David Thoreau | https://www.gutenberg.org/files/205/205-h/205-h.htm | Public-domain text sourced from Project Gutenberg; author died in 1862, so likely EU/Poland safe. Remove Project Gutenberg boilerplate before app rendering. | 3 min | deliberate-living, simplicity, values | `integrated` | It turns "I opened my phone automatically" into the deeper question: what did I mean to do with this minute? |
| 5 | Walk Before You Scroll | `Walking`, opening section from `Excursions` | Henry David Thoreau | https://www.gutenberg.org/ebooks/9846 | Public-domain text sourced from Project Gutenberg; author died in 1862, so likely EU/Poland safe. Remove Project Gutenberg boilerplate before app rendering. | 3 min | nature, movement, attention, decompression | `integrated` | It gives the user a non-screen alternative frame without making the app feel punitive. |
| 6 | Trust the First Honest Thought | `Self-Reliance`, opening section from `Essays, First Series` | Ralph Waldo Emerson | https://www.gutenberg.org/ebooks/2944 | Public-domain text sourced from Project Gutenberg; author died in 1882, so likely EU/Poland safe. Remove Project Gutenberg boilerplate before app rendering. | 4 min | agency, self-trust, creativity | `integrated` | It offers confidence and inwardness instead of social comparison. |
| 7 | The Desert Resets the Eye | `The Land of Little Rain`, opening essay selection | Mary Austin | https://www.gutenberg.org/ebooks/10217 | Public-domain text sourced from Project Gutenberg; author died in 1934, so likely EU/Poland safe. Remove Project Gutenberg boilerplate before app rendering. | 3 min | nature-writing, observation, wonder | `integrated` | Vivid place-writing can pull attention outward without relying on outrage or novelty loops. |
| 8 | Of Studies | `The Essays or Counsels, Civil and Moral`, `Of Studies` | Francis Bacon | https://www.gutenberg.org/files/575/575-h/575-h.htm | Public-domain text sourced from Project Gutenberg; author died in 1626, so likely EU/Poland safe. Remove Project Gutenberg boilerplate before app rendering. | 3 min | learning, judgment, reflection | `integrated` | A very short, high-density reading for moments when the user will not accept a long replacement. |
| 9 | A Naturalist Notices Everything | `The Voyage of the Beagle`, selected field-observation passage | Charles Darwin | https://www.gutenberg.org/ebooks/944 | Public domain in the U.S.; author died in 1882, so likely EU/Poland safe. Remove Project Gutenberg boilerplate before app rendering. | 8-10 min | science, observation, curiosity | `selected_pending_excerpt_cut` | It trains the attention system toward observation rather than reaction. |

## Reserve Items

These are strong, but should not enter pack v1 unless one selected item fails excerpt or rights triage.

| Candidate | Reason to reserve |
|---|---|
| John Stuart Mill, `On Liberty` | Excellent values fit, but denser and more argumentative than ideal for a first intervention pack. |
| Montaigne selected essay in Charles Cotton translation | Potentially excellent, but translation/edition selection needs more care before app rendering. |
| Our World in Data text-first explainer | Modern and useful, but any use should select a specific CC BY page or excerpt and preserve attribution plus modification notes. Better as pack v2 after the public-domain pack proves flow quality. |
| NASA Earth Observatory explainer | Strong wonder/science fit, but image and third-party asset checks make it slower than text-only classics. |
| SAPIENS / Aeon / Quanta / Nautilus / The Conversation | Good link-only fallback sources, but not first renderable pack material without permissions or exact republication review. |

## Excerpt Cutting Rules for Slice 7.3

- Cut each item into one self-contained reading unit.
- Target 900-1,800 words per item unless the source itself is shorter.
- Preserve source wording except for removing source boilerplate, typographic cleanup, and obvious OCR artifacts.
- Do not modernize, summarize, translate, or rewrite the source text as replacement content.
- Product-written copy is allowed only for metadata: title, description, topic tags, estimated duration, attribution, and "why this now" framing.
- If an excerpt feels like homework, cut it or drop the item.

## Rights and Attribution Notes

- Project Gutenberg pages list these works as public domain in the U.S., but the app should not reproduce Project Gutenberg license boilerplate or imply Project Gutenberg endorsement.
- Standard Ebooks states that content produced by or for Standard Ebooks is dedicated to the public domain via CC0 1.0; item-level source review should still verify underlying author/translator status for EU/Poland.
- EU/Poland renderability is treated as likely, not final, until the attribution ledger records author and translator death-year checks.
- No runtime copyright blockers should be added. If an item is not cleared during inventory triage, it should not be shipped as renderable inventory.

## 7.3 Integration Recommendation

Integrate the first eight items first. Keep Darwin as a ninth option only if the excerpt is vivid and short enough after cutting. This preserves the finite recommendation surface while giving the ranking engine enough variety across self-command, habit, nature, learning, and observation.

Do not add OpenStax, OWID, NASA, magazine links, PDF support, RSS, crawler behavior, AI summarization, or link discovery in Slice 7.3.

Slice 7.3 integration keeps Darwin in reserve and adds the first eight items as local Markdown assets under `app/src/main/assets/editorial/items/`. The integrated pack now defaults into the fresh-user path when the pack exists, and all integrated items are 3-4 minute quick-session candidates after word-count calibration.

## Source Notes Checked

- [Project Gutenberg license](https://www.gutenberg.org/policy/license.html): most PG ebooks are made of public-domain book text plus separate Project Gutenberg trademark/license material.
- [Project Gutenberg permission guidance](https://www.gutenberg.org/policy/permission.html): redistribution and reuse require care around the Project Gutenberg trademark and any copyrighted items included in a source.
- [Standard Ebooks and the Public Domain](https://standardebooks.org/about/standard-ebooks-and-the-public-domain): Standard Ebooks dedicates content produced by or for it to the public domain via CC0 1.0; it remains a useful future source, but the integrated Slice 7.3 pack uses Project Gutenberg source texts.
- [Standard Ebooks collections policy](https://standardebooks.org/contribute/collections-policy): Standard Ebooks works on books in the U.S. public domain and notes that public-domain status may differ by country.
- Project Gutenberg pages checked for selected items: Epictetus #10661, Marcus Aurelius #2680, William James #57628, Thoreau #205 and #9846, Emerson #2944, Mary Austin #10217, Francis Bacon #575, and Darwin #944.
