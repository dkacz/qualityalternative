# Link-Only Modern Pack v1

Status: `Slice 8.4 implemented`

This pack adds 20 shared editorial recommendations that open through external handoff. It deliberately does not scrape, cache, summarize, reader-mode, or rehost modern third-party articles. The app owns only the metadata needed to recommend the canonical page at the moment of impulse.

## Pack Rules

- Pack id: `link-only-modern-v1`.
- Render mode: `EXTERNAL_HANDOFF` for every item.
- Rights class: `LINK_ONLY` for every item.
- Source type: `EDITORIAL`, because these are shared catalog recommendations rather than user-private links.
- First batch source cap: no more than four items from any source label.
- Every item has title, source label, description, canonical URL, duration estimate, topics, attribution/source metadata, and `whyThisNow`.

## Integrated Items

| # | Item id | Title | Source | URL | Min | Topics | Why this now? |
|---|---|---|---|---|---:|---|---|
| 1 | `big-here-long-now` | The Big Here and Long Now | Long Now | https://longnow.org/ideas/the-big-here-and-long-now/ | 10 | essays, philosophy | Use when an app makes the present feel tiny and urgent. |
| 2 | `brian-eno-long-now` | Brian Eno, The Long Now | Long Now | https://longnow.org/ideas/brian-eno-the-long-now/ | 8 | essays, philosophy | Use when the impulse needs a larger time horizon. |
| 3 | `taking-the-long-view` | Taking the Long View | Long Now | https://longnow.org/ideas/taking-the-long-view/ | 8 | essays, philosophy | Use when the user needs distance from short-term social noise. |
| 4 | `reclaim-your-attention` | How to Reclaim Your Attention | Psyche | https://psyche.co/guides/how-to-reclaim-your-attention | 12 | psychology, tech | Use when the user wants a direct antidote to digital pull. |
| 5 | `become-indistractable` | To Become Indistractable | Psyche | https://psyche.co/guides/to-become-indistractable-recognise-that-it-starts-within-you | 10 | psychology, tech | Use when an app open feels automatic but the real trigger may be discomfort. |
| 6 | `attention-and-self` | I Attend, Therefore I Am | Aeon | https://aeon.co/essays/what-is-the-self-if-not-that-which-pays-attention | 16 | philosophy, psychology | Use when the choice is not just what to open, but who gets to direct attention. |
| 7 | `sit-and-think` | Time to Sit and Think | Aeon | https://aeon.co/essays/do-we-all-need-a-little-time-simply-to-sit-and-think | 10 | essays, philosophy | Use when a pause itself may be the higher-quality alternative. |
| 8 | `mathematical-thinking` | Mathematical Thinking Isn't What You Think It Is | Quanta | https://www.quantamagazine.org/mathematical-thinking-isnt-what-you-think-it-is-20241118/ | 8 | science, creativity | Use when the user needs wonder instead of frictionless novelty. |
| 9 | `what-is-life-quanta` | What Is Life? | Quanta | https://www.quantamagazine.org/what-is-life-20220615/ | 10 | science, philosophy | Use when the best replacement is a genuinely large question. |
| 10 | `evolution-fast-or-slow` | Evolution, Fast or Slow? | Quanta | https://www.quantamagazine.org/evolution-fast-or-slow-lizards-help-resolve-a-paradox-20240102/ | 8 | science | Use when the user wants a story with evidence instead of social noise. |
| 11 | `life-in-games` | A Life in Games | Quanta | https://www.quantamagazine.org/john-conways-life-in-games-20150828/ | 12 | science, creativity | Use when the user needs playful structure instead of endless scroll. |
| 12 | `anthropology-of-smell` | The Anthropology of Smell | SAPIENS | https://www.sapiens.org/biology/anthropology-of-smell/ | 8 | science, history | Use when screen attention needs an embodied reset. |
| 13 | `cargo-cult-rituals` | What Cargo Cult Rituals Reveal | SAPIENS | https://www.sapiens.org/culture/cargo-cult-rituals/ | 9 | history, philosophy | Use when reflexive behavior needs a broader human frame. |
| 14 | `evolution-of-childhood` | The Evolution of Childhood | SAPIENS | https://www.sapiens.org/biology/strangest-things-evolution-childhood/ | 8 | science, history | Use when the user needs low-drama science with human stakes. |
| 15 | `sep-attention` | Attention | Stanford Encyclopedia of Philosophy | https://plato.stanford.edu/entries/attention/ | 18 | philosophy, psychology | Use as a deliberate deep backup, not as the easiest default. |
| 16 | `iep-free-will` | Free Will | Internet Encyclopedia of Philosophy | https://iep.utm.edu/freewill/ | 16 | philosophy | Use when the user is thinking about whether a choice is actually theirs. |
| 17 | `iep-stoicism` | Stoicism | Internet Encyclopedia of Philosophy | https://iep.utm.edu/stoicism/ | 14 | philosophy | Use when a user wants context for the Stoic replacements already in the app. |
| 18 | `the-distracted-mind` | The Distracted Mind | Nautilus | https://nautil.us/the-distracted-mind-236467 | 8 | science, tech, psychology | Use when the user wants the problem named plainly. |
| 19 | `why-it-pays-to-play-around` | Why It Pays to Play Around | Nautilus | https://nautil.us/why-it-pays-to-play-around-237419 | 7 | science, creativity | Use when a softer replacement may work better than another productivity demand. |
| 20 | `new-view-of-time` | A New View of Time | Nautilus | https://nautil.us/a-new-view-of-time-237105/ | 6 | science, philosophy | Use when the feed is compressing attention into the immediate present. |

## Source Verification Notes

Checked on 2026-04-22:

- Long Now, SAPIENS, Quanta, SEP, IEP, and Nautilus exact URLs were selected as canonical external pages from the Sprint 8 candidate inventory and live web checks.
- Aeon/Psyche exact pages were selected from browser/search result verification because automated URL checks can be throttled by that source family.
- The Conversation was excluded from this first integration batch because exact item verification was not clean enough in Slice 8.3.
- Placeholder source-family rows from the candidate inventory were not integrated.

## Release Accounting

For Sprint 8.6 inventory accounting, count this pack as 20 link-only shared editorial recommendations. It should not be counted as renderable reading inventory, and it should not be mixed with user-private saved links in analytics.
