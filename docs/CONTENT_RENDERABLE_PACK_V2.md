# Content Renderable Pack v2

Status: `integrated in Sprint 8.5`

Pack id: `public-domain-expansion-v2`

Pack promise: ten short public-domain readings that broaden the replacement library beyond Stoic/self-command classics into observation, education, autonomy, nature, work, rumination, restraint, steadiness, and examined living.

This pack deliberately remains text-only and local. It does not add first-party essays, modern article scraping, AI summaries, reader-mode extraction, PDFs, images, charts, or runtime copyright blocking. Product-authored copy is limited to metadata: title, description, topic tags, duration, attribution, and "why this now?" framing.

## Integrated Items

| # | App title | Source work | Author / translator | Source URL | Duration | Topics | Why this now? |
|---|---|---|---|---|---:|---|---|
| 1 | A Naturalist Notices Everything | `The Voyage of the Beagle` | Charles Darwin | https://www.gutenberg.org/ebooks/944 | 3 min | science, essays | Use when the user needs curiosity instead of a reflex refresh. |
| 2 | A Doorway Into Learning | `Up From Slavery` | Booker T. Washington | https://www.gutenberg.org/ebooks/2376 | 3 min | history, essays | Use when the replacement should feel aspirational rather than punitive. |
| 3 | Choose Your Own Plan | `On Liberty` | John Stuart Mill | https://www.gutenberg.org/ebooks/34901 | 3 min | philosophy, essays | Use when opening an app would outsource the next choice. |
| 4 | Look at the Stars | `Nature` | Ralph Waldo Emerson | https://www.gutenberg.org/ebooks/29433 | 3 min | essays, philosophy | Use when the feed is making the world feel small. |
| 5 | A Place of Business | `Life Without Principle` | Henry David Thoreau | https://en.wikisource.org/wiki/Life_Without_Principle | 3 min | essays, philosophy | Use when the user needs permission not to optimize the next moment. |
| 6 | The Mind's Own Snare | `Essays`, selected chapter | Michel de Montaigne; translated by Charles Cotton | https://www.gutenberg.org/ebooks/3599 | 3 min | essays, philosophy | Use when the impulse feels like inquiry but behaves like restlessness. |
| 7 | Rest Satisfied With What We Have | `A Happy Life` | Seneca; translated and abstracted by Roger L'Estrange | https://www.gutenberg.org/ebooks/56075 | 3 min | philosophy, psychology | Use when the user is reaching outward for relief that may be inward. |
| 8 | Anger Divides What Life Joins | `Of Anger` | Seneca; translated and abstracted by Roger L'Estrange | https://www.gutenberg.org/ebooks/56075 | 3 min | philosophy, psychology | Use when the social app impulse is tangled with irritation or argument. |
| 9 | Earnestness as an Island | `The Dhammapada` | Translated by F. Max Muller | https://www.gutenberg.org/ebooks/2017 | 3 min | philosophy, poetry | Use when the user needs steadiness more than stimulation. |
| 10 | The Examined Life | `Apology` | Plato; translated by Benjamin Jowett | https://www.gutenberg.org/ebooks/1656 | 3 min | philosophy | Use when the product needs to make the override feel like a real choice. |

## Source and Rights Notes

- Project Gutenberg source pages were verified reachable on April 22, 2026 for Darwin #944, Washington #2376, Mill #34901, Emerson #29433, Montaigne #3599, Seneca #56075, `The Dhammapada` #2017, and Plato #1656.
- Wikisource `Life Without Principle` was verified reachable on April 22, 2026 and used only for Thoreau's public-domain text.
- Seneca Letter 1 / Gummere was not integrated because the translator status is less clean for EU/Poland reuse than the older L'Estrange source. The second Seneca item instead uses `Of Anger` from the same older public-domain source as `A Happy Life`.
- OWID `Life Expectancy` was not integrated in this renderable pack because it depends more heavily on modern page licensing, charts, and attribution/modification handling. It remains a future permissive-source candidate rather than a first mixed-batch renderable item.
- No runtime copyright gate was added. Items are either selected and shipped with render metadata, or not shipped.

## Integration Checks

- All ten items are `ContentSourceType.EDITORIAL`.
- All ten items are `ContentRightsClass.RENDERABLE` with `ContentRenderMode.IN_APP_READER`.
- Every item has a local markdown body asset, source URL, attribution, license URL, reviewed date, duration, topics, and `whyThisNow`.
- Word-count calibration keeps every item within the existing 70-190 words/minute acceptance band.
- The pack is selected by default for new users when present, alongside `attention-classics-v1` and `link-only-modern-v1`.
