# iOS Discovery

Status: `slice_11_1_platform_capability_research`
Last updated: 2026-04-23

This document records Sprint 11 discovery for whether Quality Alternative should start an iOS path. It is intentionally decision-oriented: the goal is to decide which iOS path is realistic, not to add iOS code to the Android repository.

## Sprint 11 Progress

- [x] Slice 11.1: Platform capability research.
- [x] Slice 11.2: UX and flow feasibility.
- [ ] Slice 11.3: Prototype options and cost.
- [ ] Slice 11.4: Final decision memo.

## Slice 11.1 Sources Checked

Checked on 2026-04-23.

| Area | Official source | What it establishes for this project |
| --- | --- | --- |
| Family Controls setup | [Configuring Family Controls](https://developer.apple.com/documentation/xcode/configuring-family-controls) | Screen Time API work requires Family Controls capability on the app and Screen Time extensions; distribution needs Apple's Family Controls entitlement approval. |
| Family Controls entitlement | [Family Controls entitlement](https://developer.apple.com/documentation/bundleresources/entitlements/com.apple.developer.family-controls) | The entitlement is required before calling Family Controls authorization APIs. |
| App and website usage entitlement | [Entitlements index](https://developer.apple.com/documentation/bundleresources/entitlements) | Apple lists `com.apple.developer.family-controls.app-and-website-usage` for permissioned app and website usage access on the current device. |
| Authorization | [AuthorizationCenter](https://developer.apple.com/documentation/familycontrols/authorizationcenter) | The app requests Family Controls authorization through `AuthorizationCenter`; current API includes individual authorization. |
| User-selected apps/sites/categories | [FamilyActivityPicker](https://developer.apple.com/documentation/familycontrols/familyactivitypicker) and [FamilyActivitySelection](https://developer.apple.com/documentation/familycontrols/familyactivityselection) | Users select apps, domains, and categories through Apple's picker; the app receives privacy-preserving opaque values rather than normal app identity data. |
| Activity monitoring | [DeviceActivity](https://developer.apple.com/documentation/deviceactivity) | Device Activity can monitor app and website activity by schedules and thresholds through extensions; it is not an Android Accessibility-style foreground-app callback. |
| Managed restrictions | [ManagedSettings](https://developer.apple.com/documentation/managedsettings) and [ShieldSettings](https://developer.apple.com/documentation/managedsettings/shieldsettings) | Managed Settings can apply shield restrictions to selected apps, websites, and categories using tokens. |
| Shield UI | [ManagedSettingsUI](https://developer.apple.com/documentation/managedsettingsui), [ShieldConfiguration](https://developer.apple.com/documentation/managedsettingsui/shieldconfiguration), and [ShieldConfiguration submenu initializer](https://developer.apple.com/documentation/managedsettingsui/shieldconfiguration/init%28backgroundblurstyle%3Abackgroundcolor%3Aicon%3Atitle%3Asubtitle%3Aprimarybuttonlabel%3Aprimarybuttonbackgroundcolor%3Asecondarybuttonlabel%3Asecondarybuttonsubmenuitems%3A%29?language=_1) | The shield can be visually configured, but the surface is Apple's shield extension model, not our full Compose/SwiftUI intervention screen. Current beta documentation includes secondary-button submenu support, which may matter for backup options. |
| Shield actions | [ShieldActionDelegate](https://developer.apple.com/documentation/managedsettings/shieldactiondelegate), [ShieldActionResponse](https://developer.apple.com/documentation/managedsettings/shieldactionresponse), and [ShieldAction submenu actions](https://developer.apple.com/documentation/managedsettings/shieldaction/thirdsecondarysubmenuitempressed) | A shield action extension can react to shield button actions. Stable response handling should assume close, defer, or no additional action; current beta SDK/documentation also exposes parent-app-opening and submenu-related surfaces that need separate feasibility validation. |
| App/extension shared state | [Configuring app groups](https://developer.apple.com/documentation/xcode/configuring-app-groups) and [App Groups Entitlement](https://developer.apple.com/documentation/bundleresources/entitlements/com.apple.security.application-groups) | If the iOS app uses Screen Time extensions, shared selections/state likely need App Groups. |
| Shortcuts/System actions | [App Intents](https://developer.apple.com/documentation/appintents/app-intents) | App Intents expose app actions to Shortcuts, Siri, Spotlight, and other system experiences; they do not by themselves monitor arbitrary distracting-app foreground launches. |
| Parent-app routing signal | [Apple Developer Forums: opening parent app from ShieldActionDelegate](https://developer.apple.com/forums/thread/766644) | Apple engineer guidance from October 2024 is historical stable-release risk evidence. Current beta API surface may partially supersede it, so routing risk should now be framed around production availability, deployment target, session context, and App Review behavior rather than "no documented API exists." |

## Capability Matrix

| Product need | iOS capability | Status | Practical interpretation |
| --- | --- | --- | --- |
| Let user choose distracting apps | FamilyActivityPicker returns selected app/domain/category tokens. | `works` | We can ask the user to select distracting apps/sites/categories, but app identity is privacy-preserving and token-based. |
| Store selected distracting targets | `FamilyActivitySelection` is Codable; shared app/extension state can use App Groups. | `works` | A full iOS spike should plan an App Group container for selected tokens and intervention settings. |
| Detect the exact moment a selected app opens | DeviceActivity can monitor schedules and usage thresholds, not an arbitrary foreground-open callback. | `limited` | iOS is not Android parity. The more native iOS model is shielding selected apps during configured windows or after usage rules, not overlaying every foreground launch. |
| Prevent the distracting app from becoming primary | ManagedSettings `shield` can cover selected apps, domains, and categories. | `works_with_constraints` | A Screen Time path can create meaningful friction, but inside Apple's shield model. |
| Show our exact intervention UI over another app | ManagedSettingsUI configures shield appearance only. | `blocked` | We cannot assume the full Android intervention card can be rendered as-is over the target app. |
| Put one primary replacement plus two backups directly in the interruption surface | Shield UI has configurable labels/buttons, and current beta documentation includes `secondaryButtonSubmenuItems`. | `limited_beta_option` | A submenu could potentially represent backup choices, but this is not equivalent to the Android full intervention card. Slice 11.2 must test whether beta availability, visual constraints, and action callbacks can support the product shape. |
| Directly open Quality Alternative from the shield button | Stable production assumptions remain constrained; current beta API surface includes `ShieldActionResponse.openParentalControlsApp`. | `limited_beta_option` | The routing risk is reduced but not solved. Slice 11.2 must verify production availability, deployment target, whether it opens the right parent app/session, whether useful context can be passed, and whether App Review accepts the behavior. |
| Allow conscious continuation | Shield action can close/defer/no-op; ManagedSettingsStore can update shield rules from app/extension state. | `limited` | Continuation is possible only if the app's state machine maps cleanly onto supported shield actions and unshield timing. Needs a spike. |
| Track activity totals/progress | DeviceActivityReport and usage entitlement can report filtered activity in privacy-preserving form. | `works_with_entitlement` | Useful for feedback/progress, but entitlement and privacy constraints remain central. |
| Lightweight tester prototype without Screen Time entitlement | App Intents/Shortcuts can expose actions but cannot reliably intercept selected distracting apps. | `limited` | Useful for testing replacement content and intent, not for validating system-level interruption. |

## Framework Notes

### FamilyControls

FamilyControls is the authorization and selection gateway for the Screen Time stack. The project would need the Family Controls capability in development, and Family Controls distribution approval before TestFlight/App Store distribution. Current `AuthorizationCenter` supports requesting authorization for an individual, so this is not only a parent-child Family Sharing concept anymore. The user can still revoke authorization through system settings, so the product cannot rely on irreversible blocking.

For Quality Alternative, FamilyControls is mandatory for any serious iOS system-interruption path. It is also the largest external dependency because entitlement approval is not under our control.

### DeviceActivity

DeviceActivity is for scheduled and threshold-based monitoring through an extension. It supports app, category, and web activity monitoring in a privacy-preserving way. It does not look like a direct replacement for Android's current foreground-app accessibility interception.

For Quality Alternative, DeviceActivity is better suited to "selected apps are shielded during active focus/replacement mode" or "after X minutes, show friction" than "every attempted open immediately shows our full replacement card."

### ManagedSettings and ManagedSettingsUI

ManagedSettings can shield selected applications, websites, and categories. ManagedSettingsUI lets the app configure the shield's appearance, title, subtitle, icon, and buttons. Current beta documentation also includes `secondaryButtonSubmenuItems`, an array of strings for a secondary-button submenu, with related `ShieldAction` callbacks for selected submenu items.

For Quality Alternative, this means the iOS UX should be designed as a native shield-first product. Trying to reproduce the Android overlay exactly is likely the wrong goal. However, submenu support should be carried into Slice 11.2 as a beta/availability-limited option for representing one primary replacement plus backup choices.

### ShieldActionDelegate

ShieldActionDelegate can respond when the user taps shield actions. Stable-production response handling should still be treated as constrained around close, defer, or no additional action. Current beta SDK/documentation also lists `ShieldActionResponse.openParentalControlsApp`, described as a system instruction to open the parental-controls app responsible for the shielded application or web browser.

For Quality Alternative, the routing problem remains the main UX risk, but the risk is now more specific: production availability, deployment target, whether the response opens the correct Quality Alternative app state, whether useful session context can be passed from the extension, and whether App Review accepts this as a self-control replacement flow. A full iOS spike should prove whether the shield can create an acceptable path into a replacement session without private APIs.

### App Groups

Screen Time extensions and the host app will likely need shared state for selected targets, active shield rules, replacement intent, and analytics handoff. Apple's App Groups documentation is the normal way to share container data between an app and app extension.

For Quality Alternative, any full iOS spike should include App Groups from day one. Otherwise the extension architecture will be misleading.

### App Intents and Shortcuts

App Intents can expose Quality Alternative actions to Shortcuts, Siri, Spotlight, and other system experiences. They do not provide app-open detection or shielding.

For Quality Alternative, App Intents are a useful secondary path for a lightweight prototype or quick actions, but not a substitute for Screen Time APIs.

## Preliminary Architecture Options

### Option A: Full Screen Time Spike

Core pieces:

- SwiftUI host app.
- FamilyControls authorization.
- FamilyActivityPicker target selection.
- App Group state store.
- DeviceActivityMonitor extension.
- ManagedSettingsStore shield rules.
- ManagedSettingsUI shield configuration extension.
- ShieldActionDelegate extension.
- Minimal in-app replacement session screen.

Why it matters:

- This is the only path that can test meaningful system-level iOS friction.
- It directly tests the biggest platform risk: entitlement, shield UX, and routing.

Main risks:

- Requires entitlement approval for distribution.
- Shield routing may not support the desired replacement handoff.
- The native UX may differ materially from Android.
- Extension lifecycle and token privacy can make analytics and personalization less straightforward.

### Option B: Lightweight Shortcuts/App Intents Spike

Core pieces:

- SwiftUI host app or even a narrow prototype target.
- App Intent such as "Start a replacement session" or "Start 3 minute alternative."
- Optional Shortcut automation created manually by tester.
- Existing content/replacement logic copied conceptually, not shared through a backend.

Why it matters:

- Faster to test content acceptance and iOS presentation.
- No Screen Time entitlement dependency for the first learning loop.

Main risks:

- Does not validate app-level interruption.
- Depends on tester setup and may be too weak to represent the product.
- Can create false confidence if users like content but the real system-interruption path later fails.

### Option C: Defer iOS

Core pieces:

- No iOS build now.
- Continue Android pilot, content quality, and replacement-session learning.
- Prepare entitlement story and iOS product framing in parallel.

Why it matters:

- Protects the Android-first PRD scope.
- Avoids spending platform effort before proving the replacement thesis.

Main risks:

- Slower learning about Apple's entitlement and shield constraints.
- Delays answer to "can this be a cross-platform product?"

## Slice 11.1 Preliminary Conclusion

The iOS path is technically plausible only as a Screen Time API product, not as an Android overlay clone. FamilyControls plus DeviceActivity plus ManagedSettings can create real friction around selected apps and websites, but the exact Android behavior of catching every foreground launch and showing a full custom intervention card is not supported as a direct parity target.

The highest-risk unknown is not content, reader UI, or ranking. It is shield routing: whether a user can move from Apple's shield into a Quality Alternative replacement session with acceptable friction and without private APIs. Current beta surfaces such as `openParentalControlsApp` and `secondaryButtonSubmenuItems` may reduce that risk, but they do not prove production-ready Android parity. Slice 11.2 should focus on that UX path before any prototype decision.

Current recommendation is not final. Based on Slice 11.1 only, the most defensible next step is to finish the discovery sprint and then choose between:

- `full_ios_spike` if shield-to-replacement routing looks acceptable enough to test.
- `defer_ios_until_android_pilot` if routing is too constrained or entitlement odds look weak.

The `lightweight_ios_spike` remains useful only for testing replacement-session demand, not for validating the core iOS system product.

## Open Questions for Later Slices

- Can shield button copy, `defer`, `openParentalControlsApp`, and secondary-button submenu items create a coherent "pause first, then choose" experience?
- If beta parent-app routing is unavailable on the deployment target or cannot pass useful context, can a notification or universal-link-style second step be acceptable?
- What exact App Review framing gives Family Controls entitlement the best chance: self-control, digital wellbeing, parental controls, or a narrower "screen time companion" framing?
- Does the product need per-app identity in analytics, or can token-level and source-level analytics be enough?
- Would the user accept iOS as a more scheduled/shielded product rather than Android's immediate overlay-style flow?

## Slice 11.2 UX and Flow Feasibility

Status: `implemented_pending_pro_review`

Goal: compare realistic iOS flows against the Android alpha behavior without assuming private APIs or exact UI parity.

### Android Alpha Baseline

The current Android product shape is:

- User selects distracting apps.
- The app detects a selected app-open attempt.
- The intervention appears quickly, targeting two seconds under normal conditions.
- The surface shows one primary replacement plus two backup choices.
- User can start a replacement, choose a backup, pause the distracting app for 15 minutes, or open anyway.
- Replacement sessions open directly into the reader, external handoff, document reader, or meditation timer.
- Delay suppresses repeated prompts during the delay window by default.
- Analytics record intervention shown, replacement accepted, backup accepted, delay, open anyway, session duration, and post-session feedback.

### Flow Feasibility Matrix

| Android behavior | Best iOS primitive | Status | UX implication | Main validation needed |
| --- | --- | --- | --- | --- |
| User selects distracting apps | FamilyActivityPicker and FamilyActivitySelection. | `works` | The selection UX will be Apple's picker rather than our own app list. This is acceptable and likely trust-positive. | Confirm selected tokens can be stored and reused through App Groups across host app and extensions. |
| Selected app-open attempt triggers an intervention | ManagedSettings shield after user-selected app/category/domain rules; DeviceActivity schedule/threshold logic. | `limited` | iOS should be framed as "shield selected distractions during protected windows or conditions," not as guaranteed every-foreground-open interception. | Decide whether a shield-first model is acceptable for the product promise. |
| Intervention appears within two seconds | System shield appears when shield rules are active. | `works_with_constraints` | The shield can be timely, but only when the target is already shielded by configured rules. There is no Android-style overlay startup path to optimize. | Prototype shield latency on-device if full spike proceeds. |
| Full custom intervention card | ManagedSettingsUI ShieldConfiguration. | `blocked` | The Android card cannot be reproduced exactly. The iOS surface should use Apple shield copy, limited buttons, and possibly submenu items. | None for parity; this is a design constraint. |
| One primary replacement shown immediately | Shield title/subtitle/primary button can present one recommendation conceptually. | `limited` | A concise shield can present one clear primary action, but not the full Android card with rich metadata. | Test copy density and whether the primary action maps cleanly to a replacement session. |
| Two backup recommendations | Current beta `secondaryButtonSubmenuItems` and related shield submenu actions. | `limited_beta_option` | Backups may be representable as submenu items, but this is deployment-target and beta-surface dependent. If unavailable, backups probably need to move into the app after parent-app routing. | Verify availability, callback behavior, visual affordance, and context handoff. |
| Start replacement now | `openParentalControlsApp` beta/current API surface or a second-step app-opening workaround. | `limited_beta_option` | This is the decisive routing question. It may allow a shield action to open the parent app, but we still need to prove it can open the right replacement session, not just the app home screen. | Spike parent-app launch, session state, and App Review-safe behavior. |
| Open anyway | `ShieldActionResponse.close`, temporary unshielding through shared state, or defer-style behavior. | `limited` | iOS can likely provide a conscious continuation path, but the exact mechanics may be more awkward than Android. A hard guarantee of immediate target-app open should not be promised yet. | Prototype whether the target app opens after close/unshield and whether state cleanup is reliable. |
| Pause 15 min | `ShieldActionResponse.defer` or app-controlled temporary shield/unshield windows. | `works_with_constraints` | This maps better to iOS than direct replacement routing. Delay can be native-feeling if framed as "not now" rather than "open later." | Confirm defer duration control, analytics mapping, and repeated-attempt behavior. |
| Delay suppresses repeated prompts | ManagedSettingsStore state plus App Group state. | `works_with_constraints` | Suppression can be modeled by changing shield rules, but extensions and host app need consistent shared state. | Validate extension lifecycle and race conditions. |
| Replacement reader/meditation starts directly | Parent app session screen launched from shield or manually opened second step. | `limited_beta_option` | If parent-app routing works, a direct session is plausible. If not, iOS becomes a two-step flow and loses impulse-moment sharpness. | Prove deep-link/session-state equivalent without private APIs. |
| Feedback after replacement | Host app UI. | `works` | Once the user is in the app, feedback is straightforward. | Ensure analytics can associate feedback to the shield-triggered session. |
| Analytics distinguish accepted/delay/open-anyway | Host app plus extension events in shared storage. | `works_with_constraints` | Event taxonomy can be reused conceptually, but per-target identity may stay tokenized/privacy-preserving. | Define token-safe event schema before implementation. |

### Candidate iOS UX Flows

#### Flow A: Shield Primary Action Opens Replacement Session

Path:

1. User opens a shielded distracting app.
2. Apple shield appears with Quality Alternative copy.
3. Primary button says a short action such as "Read this instead" or "Start alternative."
4. Shield action returns `openParentalControlsApp` where available.
5. The host app opens directly into the selected replacement session using shared App Group state.
6. User reads, opens external handoff, or starts meditation.
7. Feedback and progress are recorded in the host app.

Status: `limited_beta_option`.

Why it is attractive:

- Closest to the Android thesis.
- Preserves the moment-of-impulse replacement loop.
- Avoids making the user manually find the replacement after being interrupted.

Risks:

- Beta/current API availability may not match the desired deployment target.
- The API may open the parent app but not preserve enough session context.
- App Review may scrutinize self-control routing differently from parental-control routing.
- The shield may still be too constrained to show enough recommendation context.

#### Flow B: Shield Uses Primary Plus Backup Submenu

Path:

1. User opens a shielded distracting app.
2. Shield title/subtitle present the primary recommendation.
3. Primary button starts the primary replacement through parent-app routing.
4. Secondary button opens a submenu with backup choices.
5. Submenu callbacks store the chosen backup and route to the host app.

Status: `limited_beta_option`.

Why it is attractive:

- Most closely maps to "one primary plus two backups."
- Keeps the interruption finite without a browsing list.
- Avoids turning iOS into a generic blocker.

Risks:

- Submenu UI may feel hidden or too menu-like for the impulse moment.
- Beta availability may prevent near-term use.
- If callbacks cannot carry enough context, the flow collapses into a generic app open.

#### Flow C: Shield Defers, Then App Handles Replacement

Path:

1. User opens a shielded distracting app.
2. Shield offers "Not now" / defer and possibly a prompt to open Quality Alternative manually.
3. The distracting app remains shielded or deferred.
4. User opens Quality Alternative from home screen, notification, widget, or shortcut.
5. The app starts the replacement session.

Status: `limited`.

Why it is useful:

- More robust if direct parent-app routing is unavailable.
- Keeps iOS within stable shield constraints.
- Still interrupts the automatic app-open loop.

Risks:

- It weakens the "quality replacement at the moment of impulse" thesis.
- The extra step may lose many users.
- It tests blocker value more than replacement value.

#### Flow D: Shortcuts/App Intents Assisted Prototype

Path:

1. User configures a Shortcut or uses a Quality Alternative App Intent.
2. The action opens a replacement session or timer.
3. No system-level selected-app shielding is required.

Status: `limited`.

Why it is useful:

- Can test iOS content presentation and replacement acceptance quickly.
- Does not require Family Controls entitlement for initial learning.

Risks:

- Does not validate selected-app interruption.
- Requires tester setup.
- Can produce misleading positive signal because the hardest platform constraint is absent.

#### Flow E: Exact Android Overlay Clone

Path:

1. User opens selected app.
2. Quality Alternative overlays a full custom intervention card over that app.
3. User chooses primary, backup, delay, or open anyway from our UI.

Status: `blocked`.

Reason:

- Public iOS APIs do not provide an Android Accessibility/overlay equivalent for arbitrary third-party app foreground launches.
- Designing toward this target would create private-API pressure and false parity expectations.

### UX Copy Implications

The iOS copy should avoid promising "we catch every open" or "we show our full intervention over apps." Better framing:

- "Choose apps and sites where you want a pause."
- "When iOS shields them, Quality Alternative gives you a better next step."
- "Some iOS flows depend on Apple's Screen Time permissions and shield behavior."

Avoid:

- "Works exactly like Android."
- "Always intercepts instantly."
- "Blocks apps until you read."
- "Bypasses Screen Time limits."

### Product Recommendation After Slice 11.2

The best iOS candidate is Flow A plus Flow B if beta/current shield-routing and submenu APIs are available on the intended deployment target and pass App Review scrutiny. This would preserve the replacement-first thesis better than a pure blocker.

If those APIs are unavailable or cannot pass usable session context, the fallback is not a full iOS product. It is either:

- a lightweight App Intents prototype for content/replacement acceptance only, or
- deferring iOS until Android pilot signal justifies the platform effort.

Slice 11.3 should therefore estimate two tracks: a full Screen Time spike that explicitly tests Flow A/B, and a lightweight App Intents spike that is clearly labeled as non-validation for system interruption.
