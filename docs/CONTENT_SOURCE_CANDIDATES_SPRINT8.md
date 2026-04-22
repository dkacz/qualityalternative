# Sprint 8 Content Source Candidates

Status: `Slice 8.3 review-ready candidate inventory`

This document expands the Sprint 7 source map into a larger candidate pool for the mixed Sprint 8 batch. It is a discovery and scoring artifact, not a final rights ledger and not an app manifest. Items listed as `link_only_candidate` must stay external unless a later, explicit licensing decision changes their render mode.

Copyright and reuse decisions remain selection-time decisions. The app runtime should follow explicit item metadata and should not add hidden copyright blocking.

## Scoring Rubric

- `Fit`: 1-5 estimate of how well the item can interrupt a social-media impulse.
- `Durability`: `evergreen`, `mostly_evergreen`, or `time_sensitive`.
- `First batch`: `strong`, `possible`, `reserve`, or `hold`.
- `Next step`: the concrete triage step before integration.

First integrated link-only pack should cap any one modern source family at four items, even if this candidate pool contains more.

## Web Verification Notes

Checked on 2026-04-22:

- Long Now Ideas pages resolve for `The Big Here and Long Now`, `Brian Eno, The Long Now`, `Taking the Long View`, and related long-term thinking material.
- Psyche/Aeon pages were identified through web search/browser checks for attention, indistractability, thinking time, self/attention, digital technology, and literacy/design items; automated URL checks may be throttled by the source.
- Quanta pages resolve for the listed science/math candidates; Quanta remains link-only by policy.
- SAPIENS pages resolve for several anthropology candidates; some rows intentionally point to collection pages and require exact URL selection before integration.
- SEP `Attention` resolves and was recently revised; SEP/IEP candidates remain link-only by default.
- Project Gutenberg pages resolve for several renderable candidates, including Mill, Montaigne, Dhammapada, Aristotle, and Booker T. Washington.
- NASA Earth Observatory image-use policy and selected NASA science pages resolve; item-level image/credit checks remain required.
- The Conversation search was blocked by robots in this review lane, so The Conversation remains a source-family candidate until exact pages are manually verified before Slice 8.4.
- A lightweight automated URL check returned `200` for the exact Long Now, SAPIENS, Quanta, SEP, IEP, Project Gutenberg, Wikisource, Our World in Data, NASA, and Earth Observatory URLs listed here. Aeon/Psyche returned throttling responses to automation and NOAA Climate.gov blocked automation, so those remain browser/manual checks before integration.
- Rows with collection, homepage, source-family, or `Source TBD` URLs are not eligible for direct integration until exact canonical item URLs are selected and manually verified.

## Link-Only Candidate Pool

| # | Candidate | Source family | Candidate URL / source URL | Fit | Durability | First batch | Why this now? | Next step |
|---|---|---|---|---:|---|---|---|---|
| L01 | The Big Here and Long Now | Long Now | https://longnow.org/ideas/the-big-here-and-long-now/ | 5 | evergreen | strong | Reframes the impulse from short-now reaction to long-now attention. | Already smoke-tested as link-only. |
| L02 | Brian Eno, The Long Now | Long Now | https://longnow.org/ideas/brian-eno-the-long-now/ | 4 | evergreen | possible | A clear antidote to short-term loops. | Confirm title/description fit for card copy. |
| L03 | Taking the Long View | Long Now | https://longnow.org/ideas/taking-the-long-view/ | 4 | evergreen | possible | Moves attention from immediate feed cycles to slow systems. | Check length and choose 8-12 min estimate. |
| L04 | The Orrery at The Interval | Long Now | https://longnow.org/ideas/orrery-interval-invitation-long-term-thinking/ | 3 | evergreen | reserve | Physical long-term thinking may be a calmer replacement. | Verify whether it reads as article or announcement. |
| L05 | Reframing Education for the Long Now | Long Now | https://longnow.org/ideas/reframing-education-part-1/ | 3 | mostly_evergreen | reserve | Useful for learning-oriented downtime. | Check if too institutional for impulse use. |
| L06 | The Time Machine | Long Now | https://longnow.org/ideas/the-time-machine/ | 3 | mostly_evergreen | reserve | Climate/time perspective can widen the user's frame. | Confirm article format and source context. |
| L07 | How to reclaim your attention | Psyche | https://psyche.co/guides/how-to-reclaim-your-attention | 5 | evergreen | strong | Directly matches the product promise. | Use external handoff only; estimate 12-15 min. |
| L08 | To become indistractable, recognise that it starts within you | Psyche | https://psyche.co/guides/to-become-indistractable-recognise-that-it-starts-within-you | 5 | evergreen | strong | Gives a practical alternative to reflex checking. | Verify no paywall/gate and final duration. |
| L09 | What is the self if not that which pays attention? | Aeon | https://aeon.co/essays/what-is-the-self-if-not-that-which-pays-attention | 5 | evergreen | strong | Makes attention feel identity-level, not productivity-only. | Keep title concise in app card. |
| L10 | Do we all need a little time simply to sit and think? | Aeon | https://aeon.co/essays/do-we-all-need-a-little-time-simply-to-sit-and-think | 5 | evergreen | strong | A near-perfect replacement for checking the phone. | Verify duration and external handoff copy. |
| L11 | Can students who are constantly on their devices actually learn? | Aeon | https://aeon.co/essays/can-students-who-are-constantly-on-their-devices-actually-learn | 4 | mostly_evergreen | possible | Useful for younger testers and digital attention framing. | Check if education framing is too narrow. |
| L12 | Has technology set us free, or shackled us to our screens? | Aeon | https://aeon.co/essays/has-technology-set-us-free-or-shackled-us-to-our-screens | 4 | evergreen | possible | Directly names the ambivalence of digital tools. | Verify title and length before selection. |
| L13 | What we think is a decline in literacy is a design problem | Aeon | https://aeon.co/essays/what-we-think-is-a-decline-in-literacy-is-a-design-problem | 4 | mostly_evergreen | possible | Connects reading difficulty to design rather than shame. | Check if 2026-specific claims age quickly. |
| L14 | The sovereign individual and the paradox of the digital age | Aeon | https://aeon.co/essays/the-sovereign-individual-and-the-paradox-of-the-digital-age | 3 | mostly_evergreen | reserve | Good for agency and digital identity. | Likely too dense; score after reread. |
| L15 | Can we make consciousness into an engineering problem? | Aeon | https://aeon.co/essays/can-we-make-consciousness-into-an-engineering-problem | 3 | evergreen | reserve | Attention/consciousness angle for science-minded users. | Check if too technical for first batch. |
| L16 | The Quantified Self is a spirituality for our times | Aeon | https://aeon.co/essays/the-quantified-self-is-a-spirituality-for-our-times | 3 | mostly_evergreen | reserve | Useful counterpoint to metrics/streaks. | Check tone against product's non-gimmick stance. |
| L17 | What the Anthropology of Smell Reveals About Humanity | SAPIENS | https://www.sapiens.org/biology/anthropology-of-smell/ | 4 | evergreen | possible | Pulls attention into embodied perception. | Verify link-only copy and duration. |
| L18 | What Cargo Cult Rituals Reveal About Human Nature | SAPIENS | https://www.sapiens.org/culture/cargo-cult-rituals/ | 4 | evergreen | possible | Human behavior through anthropology, not outrage. | Confirm source terms and sensitivity. |
| L19 | Why Social Distancing Feels So Strange | SAPIENS | https://www.sapiens.org/biology/covid-19-social-distancing/ | 3 | time_sensitive | reserve | Strong human-social behavior angle, but pandemic-specific. | Use only if still feels evergreen. |
| L20 | Five Strange Things About the Evolution of Childhood | SAPIENS | https://www.sapiens.org/biology/strangest-things-evolution-childhood/ | 4 | evergreen | possible | Wonder-oriented, low-drama science. | Check excerpt/adaptation note because article is adapted. |
| L21 | Bird nests as vitality and status | SAPIENS | https://www.sapiens.org/culture/ | 3 | mostly_evergreen | reserve | Cultural anthropology as curiosity replacement. | Select exact article URL before integration. |
| L22 | Following the life of an abandoned bull in Nepal | SAPIENS | https://www.sapiens.org/culture/ | 3 | mostly_evergreen | reserve | Place-based human/animal attention reset. | Select exact article URL before integration. |
| L23 | Connections and conflicts with seals in a Scottish archipelago | SAPIENS | https://www.sapiens.org/culture/ | 3 | mostly_evergreen | reserve | Deep-time place story, likely calming. | Select exact article URL before integration. |
| L24 | In Japan, the philosophical stance against having children | SAPIENS | https://www.sapiens.org/culture/ | 2 | mostly_evergreen | hold | Interesting but potentially heavy at impulse moment. | Reassess tone before any first batch use. |
| L25 | Mathematical Thinking Isn't What You Think It Is | Quanta | https://www.quantamagazine.org/mathematical-thinking-isnt-what-you-think-it-is-20241118/ | 5 | evergreen | strong | Makes deep thinking feel playful and accessible. | External handoff only; check card title length. |
| L26 | What Is Life? | Quanta | https://www.quantamagazine.org/what-is-life-20220615/ | 4 | evergreen | possible | Big science question, good curiosity alternative. | Check format; may be podcast/transcript-like. |
| L27 | Evolution: Fast or Slow? Lizards Help Resolve a Paradox | Quanta | https://www.quantamagazine.org/evolution-fast-or-slow-lizards-help-resolve-a-paradox-20240102/ | 4 | evergreen | possible | Concrete science story with wonder. | Estimate duration and topic tags. |
| L28 | Why Everything in the Universe Turns More Complex | Quanta | https://www.quantamagazine.org/why-everything-in-the-universe-turns-more-complex-20250402/ | 4 | evergreen | possible | Replaces scroll with big-picture complexity. | Check if too long for first batch. |
| L29 | A Life in Games | Quanta | https://www.quantamagazine.org/john-conways-life-in-games-20150828/ | 4 | evergreen | possible | Conway/Game of Life is a beautiful finite curiosity object. | Verify duration and source label. |
| L30 | AI Is Nothing Like a Brain, and That's OK | Quanta | https://www.quantamagazine.org/ai-is-nothing-like-a-brain-and-thats-ok-20250430/ | 3 | mostly_evergreen | reserve | Relevant to tech users, but current AI angle may age. | Use later if testers ask for AI/science. |
| L31 | How Much Energy Does It Take To Think? | Quanta | https://www.quantamagazine.org/how-much-energy-does-it-take-to-think-20250604/ | 4 | mostly_evergreen | possible | Embodied brain-energy question fits attention theme. | Verify if date/facts need freshness warning. |
| L32 | Andreas Wagner pursues evolutionary success | Quanta | https://www.quantamagazine.org/andreas-wagner-pursues-the-secrets-to-evolutionary-success-20230815/ | 3 | evergreen | reserve | Good science profile, less immediate. | Keep for broader science pack. |
| L33 | Attention | Stanford Encyclopedia of Philosophy | https://plato.stanford.edu/entries/attention/ | 4 | evergreen | possible | Authoritative deep dive on attention. | Use only as deep link; likely 20+ min. |
| L34 | Stoicism | Stanford Encyclopedia of Philosophy | https://plato.stanford.edu/entries/stoicism/ | 3 | evergreen | reserve | Useful context for classics pack readers. | Deep-reference link, not default first batch. |
| L35 | Personal Identity | Stanford Encyclopedia of Philosophy | https://plato.stanford.edu/entries/identity-personal/ | 3 | evergreen | reserve | Identity/self question for philosophy users. | Verify exact page and duration before integration. |
| L36 | Free Will | Stanford Encyclopedia of Philosophy | https://plato.stanford.edu/entries/freewill/ | 3 | evergreen | reserve | Choice and agency link, but long. | Deep-reference only. |
| L37 | Consciousness | Stanford Encyclopedia of Philosophy | https://plato.stanford.edu/entries/consciousness/ | 3 | evergreen | hold | Too long and abstract for most interventions. | Use only after personalization. |
| L38 | Philosophy of Technology | Stanford Encyclopedia of Philosophy | https://plato.stanford.edu/entries/technology/ | 3 | evergreen | reserve | Useful for tech-and-agency framing. | Confirm URL and length before integration. |
| L39 | Free Will | Internet Encyclopedia of Philosophy | https://iep.utm.edu/freewill/ | 3 | evergreen | possible | More accessible agency reference than SEP for some users. | Use as a deep external-handoff backup, not a default quick read. |
| L40 | Stoicism | Internet Encyclopedia of Philosophy | https://iep.utm.edu/stoicism/ | 3 | evergreen | possible | Context link for Stoic readings. | Use as a bounded context link for users who choose philosophy. |
| L41 | Personal Identity | Internet Encyclopedia of Philosophy | https://iep.utm.edu/person-i/ | 3 | evergreen | reserve | Good philosophy-curious backup. | Verify exact URL before selection. |
| L42 | Consciousness | Internet Encyclopedia of Philosophy | https://iep.utm.edu/consciousness/ | 2 | evergreen | hold | Likely too broad for first batch. | Reassess after tester preferences. |
| L43 | Digital distraction and attention article | The Conversation | https://theconversation.com/ | 4 | mostly_evergreen | possible | Accessible academic journalism on the core problem. | Manual exact URL verification required; search was robots-blocked. |
| L44 | Habit formation psychology article | The Conversation | https://theconversation.com/ | 4 | mostly_evergreen | possible | Practical behavior-science support for replacement loop. | Manual exact URL verification required. |
| L45 | Science literacy / misinformation article | The Conversation | https://theconversation.com/ | 3 | mostly_evergreen | reserve | Good antidote to social-media misinformation loops. | Manual exact URL verification required. |
| L46 | Long-term climate thinking article | The Conversation | https://theconversation.com/ | 3 | mostly_evergreen | reserve | Good context shift if non-alarmist. | Manual exact URL verification required. |
| L47 | Mind and perception candidate | Nautilus | https://nautil.us/ | 3 | mostly_evergreen | reserve | Nautilus has strong science/culture fit. | Select exact article and verify URL in Slice 8.4. |
| L48 | Scale/time candidate | Nautilus | https://nautil.us/ | 3 | mostly_evergreen | reserve | Could support long-view replacement. | Select exact article and verify URL in Slice 8.4. |
| L49 | Complexity/life candidate | Nautilus | https://nautil.us/ | 3 | mostly_evergreen | reserve | Good for wonder-oriented readers. | Select exact article and verify URL in Slice 8.4. |
| L50 | Author-approved independent essay candidate | Independent/Substack | Source TBD | 3 | variable | hold | Could add taste and freshness once permission/link fit is clear. | Do not integrate until exact public canonical URL and author fit are selected. |

## Renderable Candidate Pool

| # | Candidate | Source family | Candidate URL / source URL | Fit | Durability | First batch | Why this now? | Next step |
|---|---|---|---|---:|---|---|---|---|
| R01 | Start With What Is Yours | Project Gutenberg | https://www.gutenberg.org/ebooks/10661 | 5 | evergreen | integrated | Separates what the user controls from the feed's pull. | Already integrated in `attention-classics-v1`. |
| R02 | The Morning Test | Project Gutenberg | https://www.gutenberg.org/ebooks/2680 | 5 | evergreen | integrated | A fast Stoic reset for default phone checking. | Already integrated in `attention-classics-v1`. |
| R03 | The Flywheel of Habit | Project Gutenberg | https://www.gutenberg.org/ebooks/57628 | 5 | evergreen | integrated | Explains the habit groove the product interrupts. | Already integrated in `attention-classics-v1`. |
| R04 | Live Deliberately | Project Gutenberg | https://www.gutenberg.org/ebooks/205 | 5 | evergreen | integrated | Turns automatic open into a values question. | Already integrated in `attention-classics-v1`. |
| R05 | Walk Before You Scroll | Project Gutenberg | https://www.gutenberg.org/ebooks/9846 | 5 | evergreen | integrated | Offers a non-screen alternative frame. | Already integrated in `attention-classics-v1`. |
| R06 | Trust the First Honest Thought | Project Gutenberg | https://www.gutenberg.org/ebooks/2944 | 4 | evergreen | integrated | Counters comparison with self-trust. | Already integrated in `attention-classics-v1`. |
| R07 | The Desert Resets the Eye | Project Gutenberg | https://www.gutenberg.org/ebooks/10217 | 4 | evergreen | integrated | Vivid place-writing pulls attention outward. | Already integrated in `attention-classics-v1`. |
| R08 | Read to Weigh | Project Gutenberg | https://www.gutenberg.org/ebooks/575 | 5 | evergreen | integrated | Short, dense case for reading as judgment. | Already integrated in `attention-classics-v1`. |
| R09 | A Naturalist Notices Everything | Project Gutenberg | https://www.gutenberg.org/ebooks/944 | 4 | evergreen | strong | Observational science as an anti-scroll mode. | Cut one vivid 6-8 min passage from `Voyage of the Beagle`. |
| R10 | Evolution's Slow Work | Project Gutenberg | https://www.gutenberg.org/ebooks/1228 | 3 | evergreen | reserve | Origin excerpts are important but dense. | Use only if a short accessible passage is found. |
| R11 | Starting From Almost Nothing | Project Gutenberg | https://www.gutenberg.org/ebooks/2376 | 4 | evergreen | strong | Booker T. Washington gives agency/resilience without productivity fluff. | Cut a self-contained 5-7 min passage. |
| R12 | One Very Simple Principle | Project Gutenberg | https://www.gutenberg.org/ebooks/34901 | 4 | evergreen | possible | Mill connects liberty and intentional choice. | Cut carefully; avoid overly abstract sections. |
| R13 | A Larger Half of Human Ability | Project Gutenberg | https://www.gutenberg.org/ebooks/27083 | 3 | evergreen | reserve | Strong social thought, but not always impulse-fit. | Use after tone review. |
| R14 | Nature as Interpreter | Project Gutenberg | https://www.gutenberg.org/files/29433/29433-h/29433-h.htm | 4 | evergreen | possible | Emerson's nature writing can be a calm outward reset. | Cut one accessible section. |
| R15 | Life Without Principle | Public-domain source TBD | Source verification needed | 5 | evergreen | strong | Direct critique of attention sold to triviality. | Find a clean public-domain source and cut 4-6 min. |
| R16 | Montaigne: Of Books | Project Gutenberg | https://www.gutenberg.org/files/60014/60014-h/60014-h.htm | 3 | evergreen | reserve | Reflective but translation/length need care. | Select one short chapter and verify Cotton/Hazlitt status. |
| R17 | Montaigne: That Our Mind Hinders Itself | Project Gutenberg | https://www.gutenberg.org/files/60014/60014-h/60014-h.htm | 4 | evergreen | possible | Direct fit for rumination and impulse. | Cut tightly; avoid archaic overload. |
| R18 | Seneca: Of a Happy Life | Project Gutenberg | https://www.gutenberg.org/files/59025/59025-h/59025-h.htm | 4 | evergreen | possible | Calm values frame for craving and comparison. | Verify translation/source and excerpt. |
| R19 | Seneca: Moral Letters to Lucilius | Wikisource / public-domain translation | https://en.wikisource.org/wiki/Moral_letters_to_Lucilius | 5 | evergreen | possible | Stoic letters are highly modular. | Verify Gummere translation status and exact letter. |
| R20 | The Dhammapada, short verses | Project Gutenberg | https://www.gutenberg.org/ebooks/2017 | 4 | evergreen | possible | Craving/attention themes fit, but framing must be careful. | Use only with respectful context and no wellness claims. |
| R21 | Tao Te Ching, Legge translation | Public-domain source TBD | Source verification needed | 4 | evergreen | reserve | Short contemplative alternatives could work well. | Verify source and translation rights before any use. |
| R22 | Aristotle: Habit and Character | Project Gutenberg | https://www.gutenberg.org/files/8438/8438-h/8438-h.htm | 3 | evergreen | reserve | Strong habit theme but translation is dense. | Test readability before selection. |
| R23 | Plato: Apology, examined life passage | Project Gutenberg / Jowett source TBD | Source verification needed | 3 | evergreen | reserve | Agency and reflection, but may feel like homework. | Verify source and cut only if accessible. |
| R24 | Tolstoy: Confession excerpt | Project Gutenberg | https://www.gutenberg.org/ebooks/46447 | 2 | evergreen | hold | Meaning-heavy, but emotionally intense for impulse flow. | Defer unless tester demand for deep material appears. |
| R25 | Our World in Data: Mental Health | Our World in Data | https://ourworldindata.org/global-mental-health | 3 | mostly_evergreen | reserve | Relevant, but sensitive and chart-dependent. | Use only text-first sections with careful framing. |
| R26 | Our World in Data: Life Expectancy | Our World in Data | https://ourworldindata.org/life-expectancy | 4 | evergreen | possible | Positive progress lens can replace doomscrolling. | Verify CC BY page and avoid chart-heavy excerpt. |
| R27 | Our World in Data: Energy | Our World in Data | https://ourworldindata.org/energy | 3 | mostly_evergreen | reserve | Useful but can age and depend on data/charts. | Use only if offline excerpt remains durable. |
| R28 | SeaWiFS Views the Global Carbon Cycle | NASA Science / Earth Observatory | https://science.nasa.gov/earth/earth-observatory/seawifs-views-the-global-carbon-cycle-1297/ | 4 | evergreen | possible | Earth-system wonder with clear NASA source path. | Check image credits and text-only viability. |
| R29 | NASA Earth Observatory source family | NASA Earth Observatory | https://earthobservatory.nasa.gov/image-use-policy | 4 | evergreen | possible | Strong science/wonder inventory if item credits are clean. | Pick exact image-free or NASA-credit-safe item. |
| R30 | NOAA Climate.gov explainer source family | NOAA Climate.gov | https://www.climate.gov/ | 3 | mostly_evergreen | reserve | Useful climate literacy, but asset provenance varies. | Browser/manual verification required because automated checks can be blocked. |

## Recommended Slice 8.4 Link-Only Shortlist

Start with 20, cutting weak items before integration. Rows with placeholder/source-family URLs are excluded from this shortlist until exact pages are selected.

- 4 attention/agency items from Aeon/Psyche: L07, L08, L09, L10.
- 4 curiosity/science items from Quanta: L25, L26, L27, L29.
- 3 long-view items from Long Now: L01, L02, L03.
- 3 anthropology/wonder items from SAPIENS: L17, L18, L20.
- 3 deep-reference philosophy items: L33, L39, L40.
- 3 Nautilus reserve replacements instead of The Conversation for this batch: L47-L49 represented by exact selected Nautilus URLs in `docs/CONTENT_LINK_ONLY_PACK_V1.md`.

The Conversation remains a future source-family candidate; no The Conversation item should enter Slice 8.4 unless exact canonical item URLs are manually verified first.

## Recommended Slice 8.5 Renderable Shortlist

Target 10 new renderable items:

- R09 Darwin, `Voyage of the Beagle` - integrated in `public-domain-expansion-v2`.
- R11 Booker T. Washington, `Up From Slavery` - integrated in `public-domain-expansion-v2`.
- R12 Mill, `On Liberty` - integrated in `public-domain-expansion-v2`.
- R14 Emerson, `Nature` - integrated in `public-domain-expansion-v2`.
- R15 Thoreau, `Life Without Principle` - source verified through Wikisource and integrated in `public-domain-expansion-v2`.
- R17 Montaigne, `That Our Mind Hinders Itself` - integrated from Project Gutenberg ebook #3599 in `public-domain-expansion-v2`.
- R18 Seneca, `Of a Happy Life` - integrated from Project Gutenberg ebook #56075 in `public-domain-expansion-v2`; the earlier #59025 pointer was not used.
- R19 Seneca, one moral letter, if Gummere/source status is clean - not integrated because the Gummere translation path is less clean for EU/Poland reuse; replaced with Seneca `Of Anger` from the older L'Estrange source in Project Gutenberg ebook #56075.
- R20 `The Dhammapada`, if framing passes quality review - integrated in `public-domain-expansion-v2`.
- R26 OWID `Life Expectancy`, only if a text-first CC BY excerpt is durable without charts - not integrated in this pack; replaced with Plato `Apology` from Project Gutenberg ebook #1656 to keep Slice 8.5 text-only and public-domain.

Resulting pack doc: `docs/CONTENT_RENDERABLE_PACK_V2.md`.

Reserve R10, R13, R21, R22, R23, R24, R27, R28, R29, and R30 for later packs unless a selected item fails.
