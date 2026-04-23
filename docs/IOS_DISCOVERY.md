# iOS Discovery

Status: `slice_11_1_platform_capability_research`
Last updated: 2026-04-23

This document records Sprint 11 discovery for whether Quality Alternative should start an iOS path. It is intentionally decision-oriented: the goal is to decide which iOS path is realistic, not to add iOS code to the Android repository.

## Sprint 11 Progress

- [x] Slice 11.1: Platform capability research.
- [ ] Slice 11.2: UX and flow feasibility.
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
