# iOS Competitive Research

Status: `initial_research`
Date checked: 2026-04-24

This document checks whether Quality Alternative is speculating about iOS or whether there are already comparable iOS products proving useful patterns. Conclusion: comparable products exist, but they mostly optimize for blocking, delay, breathwork, limits, or accountability. Quality Alternative can still differentiate on high-quality replacement content, but the iOS strategy should copy the best proven iOS patterns rather than trying to port Android overlay behavior directly.

## Sources Checked

| Product/source | Source link | Relevant evidence |
| --- | --- | --- |
| one sec | [App Store](https://apps.apple.com/us/app/one-sec-screen-time-focus/id1532875441), [website](https://one-sec.app/), [intervention troubleshooting](https://tutorials.one-sec.app/en/articles/3262210), [Screen Time API issues](https://tutorials.one-sec.app/en/articles/3036354) | Uses intervention modes, delayed intervention, "always ask", Re-Intervention, strict blocks, Shortcuts automations, and Screen Time shielding for some features. |
| ScreenZen | [App Store](https://apps.apple.com/us/app/screenzen-screen-time-control/id1541027222), [website](https://www.screenzen.co/) | Offers delay before opening apps, interrupt-scrolling windows, app goals, strict blocks, settings locks, breathing/activities, and streaks. |
| ClearSpace | [App Store](https://apps.apple.com/us/app/clearspace-reduce-screen-time/id1572515807), [YC profile](https://www.ycombinator.com/companies/clearspace) | Uses app limits/blocking and a pause before opening selected apps; frames the product around sober-minded decisions rather than cold-turkey blocking. |
| SOMA | [App Store](https://apps.apple.com/us/app/soma-mindful-app-blocker/id6758464485), [website](https://getsoma.eu/) | Uses a shield that intercepts distracting apps and requires breathwork, walking, or waiting before unlock. |
| Jomo | [App Store](https://apps.apple.com/us/app/jomo-screen-time-blocker/id1609960918), [help: Screen Time blocking](https://help.jomo.so/en/article/how-to-block-apps-without-apples-screen-time-lv0ux6/), [blog: block apps](https://jomo.so/blog/block-apps-on-iphone), [help: keep notifications with breaks](https://help.jomo.so/en/article/how-to-block-apps-and-keep-notifications-on-iphone-with-jomo-t9yj0h/) | Uses Apple's Screen Time API for app/site blocking, supports breaks/extra time, and documents Apple Screen Time limitations. |
| Opal | [App Store](https://apps.apple.com/us/app/opal-screen-time-control/id1497465230), [Screen Time API FAQ](https://www.opal.so/help/how-is-opal-different-from-screen-time-settings-on-ios), [permissions FAQ](https://www.opal.so/help/why-do-you-need-to-grant-screen-time-api-access), [blocking issues](https://www.opal.so/help/blocking-doesnt-seem-to-be-working), [time limits](https://opalapp.com/help/what-are-app-limits-in-opal) | Uses Apple's Screen Time API, sessions, time limits, open limits, emergency pass, analytics, and Shortcuts/Focus integrations. |
| Freedom / Limit | [Freedom for iOS](https://freedom.to/freedom-for-ios), [Limit app](https://freedom.to/limitapp) | Uses Screen Time and VPN frameworks for app/site blocking; Limit uses Screen Time data and notes Screen Time stability constraints. |
| Apple platform docs | [FamilyControls](https://developer.apple.com/documentation/familycontrols), [DeviceActivity](https://developer.apple.com/documentation/deviceactivity), [ManagedSettings](https://developer.apple.com/documentation/managedsettings), [ManagedSettingsUI](https://developer.apple.com/documentation/managedsettingsui), [App Intents](https://developer.apple.com/documentation/appintents/app-intents) | Official API stack for selection, monitoring, shielding, shield UI, shield actions, and system actions. |

## Demand Signal

This is not an empty or speculative category. App Store listings checked on 2026-04-24 show visible user demand:

- Opal: 71K ratings, 4.8.
- ScreenZen: 38K ratings, 4.9.
- one sec: 23K ratings, 4.8.
- ClearSpace: 8.4K ratings, 4.7.

These numbers are not a market-size estimate, but they are enough to reject the weak assumption that iOS users will not accept app-open friction, Screen Time permissions, or blocker/intervention products.

## Market Pattern Map

### 1. Session and Blocker Apps

Examples: Opal, Jomo, Freedom / Limit.

What they prove:

- Users understand iOS Screen Time permission flows for third-party apps.
- App/site/category blocking is viable as a product category.
- Breaks, emergency passes, time limits, schedules, focus sessions, and strict modes are established patterns.
- Monetization exists around premium focus/blocking features.

What they do not prove:

- They do not prove direct routing from a shield into a rich replacement session.
- They mostly frame success as less screen time, more focus, or harder blocking.
- They do not center a curated high-quality alternative at the impulse moment.

### 2. Friction / Intervention Apps

Examples: one sec, ScreenZen, ClearSpace, SOMA.

What they prove:

- iOS users accept "pause before app opens" as a product shape.
- Breathing, waiting, intention setting, movement, and physical tolls are viable friction mechanisms.
- Shortcuts automations and Screen Time shield patterns can both support intervention-like experiences.
- "Delay instead of block" is already a strong iOS-native mental model.

What they do not prove:

- They do not prove that the shield can show one primary quality replacement plus two backups exactly like Android.
- Their "alternative" is usually a pause, breath, reflection, movement, or timer, not a local library of quality readings/documents.
- They usually optimize to reduce usage, not to convert the impulse into meaningful content consumption.

### 3. Physical / Accountability Friction

Examples: SOMA, Jomo actions/extra time, Opal hard modes, ScreenZen settings locks.

What they prove:

- The iOS market accepts stronger friction when it is framed as self-control.
- Physical actions and accountability can make iOS blockers less bypassable.
- Screen Time settings and permissions are a known weak point, so products add passcodes, Settings locks, Shortcuts, or external accountability.

What they do not prove:

- They do not remove the need for Family Controls entitlement and App Review approval.
- They do not solve the Quality Alternative-specific routing and content-selection problem.

## Product-by-Product Notes

### one sec

one sec is the closest strategic comparison. It explicitly positions itself as delaying access rather than simply blocking. Current App Store notes mention delayed intervention, "always ask", Re-Intervention, strict blocks, journaling intervention, and Screen Time permission protections. Its support docs still reference Shortcuts automations for opening/closing app flows, while Screen Time API support appears important for shielding/Re-Intervention and strict modes.

Implication for Quality Alternative:

- We should study one sec as proof that iOS users tolerate an app-open intervention.
- one sec's moat is intervention/reflection; our potential moat is replacement quality.
- A Sprint 12 spike should include a "one-sec-style" intervention path as a reference design, not just a generic shield test.

### ScreenZen

ScreenZen's App Store listing is unusually explicit: delay before opening apps, interrupt scrolling, app goals, strict blocks after opens or Screen Time, Settings locks, breathing/activities, and streaks. It is a strong proof that the market accepts configurable friction before app opens.

Implication for Quality Alternative:

- Delay before opening is a proven iOS UX pattern.
- ScreenZen likely sets user expectations for high configurability.
- We should not try to beat ScreenZen on settings depth. We should beat it on "what you do instead."

### ClearSpace

ClearSpace frames itself around sober-minded decisions and app limits, with a pause before opening selected apps. Its App Store copy is very close to the behavioral thesis: the user selects an app, clicks it, then decides whether to proceed after a deep breath or opt out.

Implication for Quality Alternative:

- "Pause before app opens" is not only technically possible; it is a mainstream App Store product story.
- Our differentiation should not be "pause"; it should be "pause plus a high-quality replacement that is worth choosing."

### SOMA

SOMA is a newer but very relevant product. It describes "The Shield" intercepting distracting apps and requiring breathing, walking, or waiting to unlock. It uses the body as the unlock mechanism and positions itself as a mindful alternative to harsh blockers.

Implication for Quality Alternative:

- Physical/mindful unlock is a viable competitor pattern.
- Our meditation timer could be upgraded into a stronger unlock/replacement mode later, but Sprint 12 should not expand scope until shield routing is proven.

### Jomo

Jomo is a mature Screen Time API blocker. It supports blocks, websites/categories, breaks, templates, extra time, and actions. Its docs state that by default it sends selected app information to Screen Time, which then grays out and restricts the app.

Implication for Quality Alternative:

- Screen Time API blocking is commercially viable.
- Jomo's break/extra-time model is relevant to our `Pause 15 min` and `Open anyway` flow.
- Jomo is more blocker/routine-oriented than replacement-oriented.

### Opal

Opal is the scaled, polished category leader. It uses Screen Time API, sessions, time limits, open limits, block/allow lists, analytics, Shortcut automations, Focus Filters, emergency pass, and community/rewards.

Implication for Quality Alternative:

- Opal proves the category can support a large iOS business.
- Competing directly on "best blocker" would be weak.
- Quality Alternative must position away from productivity leaderboard/gamified blocker and toward replacement quality.

### Freedom / Limit

Freedom uses Screen Time and VPN frameworks to block apps and websites across devices. Limit is a narrower iOS app for mindful time boundaries.

Implication for Quality Alternative:

- Cross-device blocking is a separate product category.
- We should not chase cross-platform sync or VPN/site-blocking breadth in the first iOS spike.

## Updated Technical Interpretation

The research changes Sprint 11's framing:

- Before competitive research: "Can iOS support anything close to our Android flow?"
- After competitive research: "Which proven iOS intervention pattern should Quality Alternative adapt, and can we connect it to our replacement session?"

The strongest evidence says:

- App/site/category shielding is real and widely used.
- Delay-before-open is real and accepted.
- Breathwork/reflection/action-based unlocks are real and accepted.
- Breaks/extra time/open-anyway equivalents are real but need careful state handling.
- Screen Time API reliability and entitlement/distribution remain real risks.

The still-unproven Quality Alternative-specific questions are narrower:

- Can shield action reliably open our app into a specific replacement session?
- Can shield submenu items represent two backup alternatives well enough?
- Can pause/open-anyway be implemented without awkward two-step UX?
- Can we preserve "one primary + two backups" without turning iOS into either a blocker or a generic read-later app?

## What This Research Changes

The prior discovery memo was too conservative if read as "iOS may not support the product at all." The stronger, source-backed position is:

- iOS absolutely supports a serious digital-wellbeing product category.
- iOS supports app/site/category shields, delay-before-open, strict blocks, breaks, timers, mindful prompts, and physical/mindful unlock patterns.
- The missing proof is not general feasibility; it is Quality Alternative's specific loop: shielded impulse -> one high-quality replacement -> two backups -> intentional open-anyway/pause -> reliable return state.

Therefore Sprint 12 should not be a broad discovery sprint. It should be a physical-device prototype that proves or falsifies the replacement-routing loop.

## Strategic Conclusion

We should not ask "can we make an iOS app?" The answer is yes; comparable apps already exist.

The correct question is:

Can Quality Alternative make a better iOS intervention than one sec / ScreenZen / ClearSpace / SOMA by replacing the impulse with a genuinely worthwhile alternative rather than only delay, breathwork, movement, or blocking?

That requires a revised Sprint 12 spike:

- Start with the best proven iOS market patterns: one sec-style intervention, ScreenZen-style delay, ClearSpace-style intentional proceed/opt-out, SOMA-style shield toll.
- Test whether those patterns can route into our unique replacement session.
- Keep the spike narrow: no full iOS app, no content expansion, no cross-platform sync.

If routing works, iOS is not just possible; it is strategically interesting. If routing does not work, we should defer iOS or design a lighter voluntary replacement app, but not pretend it matches the core product.

## Sprint 12 Research-Driven Test Cases

The iOS feasibility spike should explicitly test:

1. `one_sec_style_shortcut_intervention`: app-open automation routes user into Quality Alternative, then back to target app after a completed pause/replacement decision.
2. `screen_time_shield_primary`: selected app opens into Apple shield with one primary replacement action.
3. `shield_backup_submenu`: secondary shield submenu can represent two backup choices and preserve context.
4. `clearspace_style_continue_or_opt_out`: user can either proceed intentionally or choose a better alternative without confusing state.
5. `soma_style_mindful_toll`: a meditation/breath/wait alternative can act as a first iOS replacement mode.
6. `pause_and_reopen`: temporary unshield/open-anyway works without treating `.close` as continuation or `.defer` as a timer.
7. `reliability_guardrails`: repeated attempts, app restarts, permission revocation, Screen Time glitches, and Settings bypass behavior are documented rather than hidden.

The pass condition is not "iOS can block apps." Competitors already prove that. The pass condition is "iOS can route an impulse into Quality Alternative's replacement-first loop with acceptable friction."
