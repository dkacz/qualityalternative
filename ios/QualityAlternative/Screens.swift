import FamilyControls
import SwiftUI

struct HomeScreen: View {
    @Environment(\.qaTokens) private var tokens
    let progress: QAProgressSnapshot
    let session: QAReplacementSession
    let onStartIntervention: () -> Void

    var body: some View {
        QAScreen(accessibilityIdentifier: "home-screen") {
            ScrollView {
                VStack(alignment: .leading, spacing: 18) {
                    HeaderBlock(
                        eyebrow: "QUALITY ALTERNATIVE",
                        title: "One better thing before the feed.",
                        subtitle: "A soft intervention that turns the impulse into a finite replacement choice."
                    )

                    QACard {
                        VStack(alignment: .leading, spacing: 14) {
                            Text("Ready replacement")
                                .font(.qaBody(13, weight: .semibold))
                                .foregroundStyle(tokens.colors.mutedText)
                            Text(session.primary.title)
                                .font(.qaDisplay(26, weight: .medium))
                                .foregroundStyle(tokens.colors.primaryText)
                            Text(session.primary.description)
                                .font(.qaBody(15))
                                .foregroundStyle(tokens.colors.mutedText)
                            QAButton(title: "Preview intervention", style: .primary, action: onStartIntervention)
                        }
                    }

                    HStack(spacing: 12) {
                        MetricCard(label: "Streak", value: "\(progress.streakDays)d")
                        MetricCard(label: "Done", value: "\(progress.replacementsCompleted)")
                        MetricCard(label: "Recovered", value: "\(progress.minutesRecovered)m")
                    }
                }
                .padding(20)
            }
        }
    }
}

struct LibraryScreen: View {
    @Environment(\.qaTokens) private var tokens
    let items: [QAContentItem]

    var body: some View {
        QAScreen(accessibilityIdentifier: "library-screen") {
            ScrollView {
                VStack(alignment: .leading, spacing: 16) {
                    HeaderBlock(
                        eyebrow: "LIBRARY",
                        title: "Finite replacements, not a feed.",
                        subtitle: "Renderable readings, link-only handoffs, and utility replacements share one editorial surface."
                    )
                    ForEach(items) { item in
                        ContentRow(item: item)
                    }
                }
                .padding(20)
            }
        }
    }
}

struct InterventionScreen: View {
    @Environment(\.qaTokens) private var tokens
    let session: QAReplacementSession
    let onReadPrimary: () -> Void
    let onOpenLink: () -> Void
    let onMeditate: () -> Void
    let onPause: () -> Void
    let onContinue: () -> Void

    var body: some View {
        QAScreen(accessibilityIdentifier: "intervention-screen") {
            VStack(alignment: .leading, spacing: 18) {
                HeaderBlock(
                    eyebrow: "OPENING \(session.triggerLabel.uppercased())",
                    title: "Try this first.",
                    subtitle: "Same product shape as Android: one primary suggestion and two backups, adapted to iOS constraints."
                )

                QACard {
                    VStack(alignment: .leading, spacing: 14) {
                        HStack {
                            QATag(text: session.primary.duration)
                            QATag(text: "Reader")
                        }
                        Text(session.primary.title)
                            .font(.qaDisplay(30, weight: .medium))
                            .foregroundStyle(tokens.colors.primaryText)
                        Text(session.primary.whyThisNow)
                            .font(.qaBody(16))
                            .foregroundStyle(tokens.colors.mutedText)
                        QAButton(title: "Read this", style: .primary, action: onReadPrimary)
                    }
                }

                VStack(spacing: 10) {
                    ForEach(session.backups) { backup in
                        QAButton(
                            title: backupActionTitle(for: backup),
                            style: .secondary,
                            accessibilityIdentifier: "backup-action-\(backup.id)",
                            action: backupAction(for: backup)
                        )
                    }
                    QAButton(
                        title: "Pause for 15 min",
                        style: .quiet,
                        accessibilityIdentifier: "pause-action",
                        action: onPause
                    )
                    QAButton(
                        title: "Continue intentionally",
                        style: .quiet,
                        accessibilityIdentifier: "continue-intentionally-action",
                        action: onContinue
                    )
                }
                Spacer(minLength: 0)
            }
            .padding(20)
        }
    }

    private func backupActionTitle(for item: QAContentItem) -> String {
        switch item.renderMode {
        case .inAppReader:
            "Read backup - \(item.duration)"
        case .externalHandoff:
            "Open link-only backup - \(item.duration)"
        case .meditationTimer:
            "Start meditation - \(item.duration)"
        }
    }

    private func backupAction(for item: QAContentItem) -> () -> Void {
        switch item.renderMode {
        case .inAppReader:
            onReadPrimary
        case .externalHandoff:
            onOpenLink
        case .meditationTimer:
            onMeditate
        }
    }
}

struct ReaderScreen: View {
    @Environment(\.qaTokens) private var tokens
    let item: QAContentItem
    let onDone: () -> Void

    var body: some View {
        QAScreen(accessibilityIdentifier: "reader-screen") {
            ScrollView {
                VStack(alignment: .leading, spacing: 18) {
                    HeaderBlock(
                        eyebrow: item.source.uppercased(),
                        title: item.title,
                        subtitle: item.description
                    )
                    ProgressView(value: 0.42)
                        .tint(tokens.colors.accent)
                        .accessibilityIdentifier("reader-progress")
                    VStack(alignment: .leading, spacing: 14) {
                        Text("I went to the woods because I wished to live deliberately, to front only the essential facts of life.")
                        Text("The iOS reader will keep the same quiet surface as Android: generous margins, readable type, and visible progress for long documents.")
                        Text("This slice proves the visual foundation before connecting real imported EPUB/Markdown state.")
                    }
                    .font(.qaDisplay(21))
                    .lineSpacing(5)
                    .foregroundStyle(tokens.colors.primaryText)
                    QAButton(title: "I'm done reading", style: .primary, action: onDone)
                }
                .padding(20)
            }
        }
    }
}

struct ExternalHandoffScreen: View {
    @Environment(\.qaTokens) private var tokens
    let item: QAContentItem
    let onDone: () -> Void

    var body: some View {
        QAScreen(accessibilityIdentifier: "handoff-screen") {
            VStack(alignment: .leading, spacing: 18) {
                HeaderBlock(
                    eyebrow: "EXTERNAL HANDOFF",
                    title: item.title,
                    subtitle: "Modern third-party content stays external. We link out intentionally instead of scraping, caching, summarizing, or rehosting."
                )
                QACard {
                    VStack(alignment: .leading, spacing: 14) {
                        Text(item.source)
                            .font(.qaBody(13, weight: .semibold))
                            .foregroundStyle(tokens.colors.mutedText)
                        Text(item.whyThisNow)
                            .font(.qaBody(16))
                            .foregroundStyle(tokens.colors.primaryText)
                        QAButton(title: "Mark handoff complete", style: .primary, action: onDone)
                    }
                }
                Spacer()
            }
            .padding(20)
        }
    }
}

struct MeditationTimerScreen: View {
    @Environment(\.qaTokens) private var tokens
    let item: QAContentItem
    let onDone: () -> Void

    var body: some View {
        QAScreen(accessibilityIdentifier: "meditation-screen") {
            VStack(spacing: 22) {
                HeaderBlock(
                    eyebrow: "MEDITATION",
                    title: item.title,
                    subtitle: "The default iOS replacement timer mirrors Android's utility replacement and will gain duration/gong controls in later slices."
                )
                Spacer()
                ZStack {
                    Circle()
                        .stroke(tokens.colors.line, lineWidth: 14)
                    Circle()
                        .trim(from: 0, to: 0.62)
                        .stroke(tokens.colors.accent, style: StrokeStyle(lineWidth: 14, lineCap: .round))
                        .rotationEffect(.degrees(-90))
                    VStack(spacing: 4) {
                        Text("03:00")
                            .font(.qaMono(42, weight: .medium))
                            .foregroundStyle(tokens.colors.primaryText)
                        Text("breathe")
                            .font(.qaBody(14, weight: .medium))
                            .foregroundStyle(tokens.colors.mutedText)
                    }
                }
                .frame(width: 220, height: 220)
                Spacer()
                QAButton(title: "Complete reset", style: .primary, action: onDone)
            }
            .padding(20)
        }
    }
}

struct ProgressScreen: View {
    let progress: QAProgressSnapshot

    var body: some View {
        QAScreen(accessibilityIdentifier: "progress-screen") {
            VStack(alignment: .leading, spacing: 18) {
                HeaderBlock(
                    eyebrow: "PROGRESS",
                    title: "\(progress.streakDays)-day streak",
                    subtitle: "Progress remains dynamic and tied to completed replacements, not passive app opens."
                )
                HStack(spacing: 12) {
                    MetricCard(label: "Replacements", value: "\(progress.replacementsCompleted)")
                    MetricCard(label: "Minutes", value: "\(progress.minutesRecovered)")
                }
                Spacer()
            }
            .padding(20)
        }
    }
}

struct SettingsScreen: View {
    @Environment(\.qaTokens) private var tokens
    @Binding var themeMode: QAThemeMode
    let screenTimeAuthorization: QAScreenTimeAuthorizationState
    let screenTimeAuthorizationError: String?
    @Binding var protectedSelection: FamilyActivitySelection
    let shieldSession: QAShieldSessionState?
    let onRequestScreenTimeAuthorization: () -> Void
    let onApplyShieldRules: () -> Void
    let onPauseShieldRules: () -> Void
    let onResumeShieldRules: () -> Void
    let onClearShieldRules: () -> Void
    @State private var isFamilyActivityPickerPresented = false

    private var setupSnapshot: QAScreenTimeSetupSnapshot {
        QAScreenTimeSetupSnapshot(
            authorization: screenTimeAuthorization,
            selection: QAScreenTimeSelectionSummary(
                applicationCount: protectedSelection.applicationTokens.count,
                categoryCount: protectedSelection.categoryTokens.count,
                webDomainCount: protectedSelection.webDomainTokens.count
            )
        )
    }

    private var shieldSnapshot: QAShieldControlSnapshot {
        QAShieldControlSnapshot(
            setup: setupSnapshot,
            session: shieldSession,
            now: Date()
        )
    }

    var body: some View {
        QAScreen(accessibilityIdentifier: "settings-screen") {
            ScrollView {
                VStack(alignment: .leading, spacing: 18) {
                    HeaderBlock(
                        eyebrow: "SETTINGS",
                        title: "iOS setup stays explicit.",
                        subtitle: "Screen Time access and protected-app selection use Apple's privacy-preserving picker before any shield rules exist."
                    )

                    QACard {
                        VStack(alignment: .leading, spacing: 14) {
                            SettingsSectionTitle("Screen Time setup")
                            HStack(spacing: 8) {
                                StatusPill(
                                    text: authorizationTitle,
                                    tone: authorizationTone,
                                    accessibilityIdentifier: "screen-time-status-pill"
                                )
                                StatusPill(text: setupSnapshot.canPrepareShielding ? "Ready for shield spike" : "No shield yet", tone: setupSnapshot.canPrepareShielding ? .success : .neutral)
                            }

                            Text(selectionSummaryText)
                                .font(.qaBody(15))
                                .foregroundStyle(tokens.colors.primaryText)
                                .accessibilityIdentifier("screen-time-selection-summary")

                            if let screenTimeAuthorizationError {
                                Text(screenTimeAuthorizationError)
                                    .font(.qaBody(12))
                                    .foregroundStyle(tokens.colors.mutedText)
                                    .accessibilityIdentifier("screen-time-authorization-error")
                            }

                            QAButton(
                                title: screenTimeAuthorization == .approved ? "Screen Time access approved" : "Request Screen Time access",
                                style: screenTimeAuthorization == .approved ? .secondary : .primary,
                                accessibilityIdentifier: "request-screen-time-access",
                                action: onRequestScreenTimeAuthorization
                            )
                            QAButton(
                                title: "Choose protected apps",
                                style: .secondary,
                                accessibilityIdentifier: "choose-protected-apps"
                            ) {
                                isFamilyActivityPickerPresented = true
                            }

                            Text("iOS stores opaque app/site/category tokens. Real shielding still needs later device validation.")
                                .font(.qaBody(12))
                                .lineSpacing(2)
                                .foregroundStyle(tokens.colors.mutedText)
                        }
                    }
                    .accessibilityIdentifier("screen-time-setup-card")

                    QACard {
                        VStack(alignment: .leading, spacing: 14) {
                            SettingsSectionTitle("Shield controls")
                            HStack(spacing: 8) {
                                StatusPill(
                                    text: shieldSnapshot.statusTitle,
                                    tone: shieldStatusTone,
                                    accessibilityIdentifier: "shield-state-pill"
                                )
                                StatusPill(
                                    text: shieldSnapshot.canApplyShieldRules ? "Ready to apply" : "Needs setup",
                                    tone: shieldSnapshot.canApplyShieldRules ? .success : .neutral
                                )
                            }
                            Text(shieldSnapshot.detailText)
                                .font(.qaBody(15))
                                .foregroundStyle(tokens.colors.primaryText)
                                .accessibilityIdentifier("shield-control-detail")

                            Text("Shield actions can queue a replacement or pause; real invocation needs device validation.")
                                .font(.qaBody(12))
                                .lineSpacing(2)
                                .foregroundStyle(tokens.colors.mutedText)
                                .accessibilityIdentifier("shield-extension-mapping-note")

                            QAButton(
                                title: "Apply shield rules",
                                style: .primary,
                                accessibilityIdentifier: "apply-shield-rules",
                                isEnabled: shieldSnapshot.canApplyShieldRules,
                                action: onApplyShieldRules
                            )
                            if shieldSnapshot.canPauseShieldRules {
                                QAButton(
                                    title: "Pause shields for 15 min",
                                    style: .secondary,
                                    accessibilityIdentifier: "pause-shield-rules",
                                    action: onPauseShieldRules
                                )
                            }
                            if shieldSnapshot.canResumeShieldRules {
                                QAButton(
                                    title: "Resume shield rules",
                                    style: .secondary,
                                    accessibilityIdentifier: "resume-shield-rules",
                                    action: onResumeShieldRules
                                )
                            }
                            if shieldSnapshot.canClearShieldRules {
                                QAButton(
                                    title: "Clear shield state",
                                    style: .quiet,
                                    accessibilityIdentifier: "clear-shield-rules",
                                    action: onClearShieldRules
                                )
                            }

                            Text("Simulator checks only host-app state and ManagedSettings wiring. Real shield display still requires a signed physical-device pass.")
                                .font(.qaBody(12))
                                .lineSpacing(2)
                                .foregroundStyle(tokens.colors.mutedText)
                        }
                    }
                    .accessibilityIdentifier("shield-controls-card")

                    QACard {
                        VStack(alignment: .leading, spacing: 12) {
                            SettingsSectionTitle("Theme")
                            HStack(spacing: 10) {
                                QAButton(title: "Light", style: themeMode == .light ? .primary : .secondary) {
                                    themeMode = .light
                                }
                                QAButton(title: "Dark", style: themeMode == .dark ? .primary : .secondary) {
                                    themeMode = .dark
                                }
                            }
                        }
                    }
                    Spacer(minLength: 0)
                }
                .padding(20)
            }
        }
        .familyActivityPicker(
            headerText: "Choose distracting apps",
            footerText: "Quality Alternative stores only Apple's opaque Screen Time tokens.",
            isPresented: $isFamilyActivityPickerPresented,
            selection: $protectedSelection
        )
    }

    private var authorizationTitle: String {
        switch screenTimeAuthorization {
        case .notDetermined:
            "Permission not requested"
        case .denied:
            "Permission denied"
        case .approved:
            "Permission approved"
        }
    }

    private var authorizationTone: StatusPill.Tone {
        switch screenTimeAuthorization {
        case .notDetermined:
            .neutral
        case .denied:
            .warning
        case .approved:
            .success
        }
    }

    private var selectionSummaryText: String {
        let selection = setupSnapshot.selection
        guard selection.hasProtectedTargets else {
            return "No protected apps, categories, or websites selected yet."
        }
        return "\(selection.applicationCount) apps, \(selection.categoryCount) categories, \(selection.webDomainCount) websites selected."
    }

    private var shieldStatusTone: StatusPill.Tone {
        guard let shieldSession else {
            return .neutral
        }
        if shieldSession.actionMode == .armed {
            return .success
        }
        return .warning
    }
}

private struct HeaderBlock: View {
    @Environment(\.qaTokens) private var tokens
    let eyebrow: String
    let title: String
    let subtitle: String

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            Text(eyebrow)
                .font(.qaMono(11, weight: .medium))
                .tracking(1.4)
                .foregroundStyle(tokens.colors.faintText)
            Text(title)
                .font(.qaDisplay(34, weight: .medium))
                .lineSpacing(-2)
                .foregroundStyle(tokens.colors.primaryText)
            Text(subtitle)
                .font(.qaBody(15))
                .lineSpacing(3)
                .foregroundStyle(tokens.colors.mutedText)
        }
    }
}

private struct SettingsSectionTitle: View {
    @Environment(\.qaTokens) private var tokens
    let title: String

    init(_ title: String) {
        self.title = title
    }

    var body: some View {
        Text(title)
            .font(.qaBody(13, weight: .semibold))
            .foregroundStyle(tokens.colors.mutedText)
    }
}

private struct StatusPill: View {
    @Environment(\.qaTokens) private var tokens
    let text: String
    let tone: Tone
    let accessibilityIdentifier: String?

    enum Tone {
        case neutral
        case success
        case warning
    }

    init(text: String, tone: Tone, accessibilityIdentifier: String? = nil) {
        self.text = text
        self.tone = tone
        self.accessibilityIdentifier = accessibilityIdentifier
    }

    var body: some View {
        Text(text)
            .font(.qaBody(12, weight: .medium))
            .foregroundStyle(foreground)
            .padding(.horizontal, 10)
            .padding(.vertical, 6)
            .background(background)
            .clipShape(Capsule())
            .overlay(Capsule().stroke(border, lineWidth: 1))
            .accessibilityIdentifier(accessibilityIdentifier ?? text)
    }

    private var foreground: Color {
        switch tone {
        case .neutral:
            tokens.colors.mutedText
        case .success:
            tokens.colors.success
        case .warning:
            tokens.colors.accent
        }
    }

    private var background: Color {
        switch tone {
        case .neutral:
            tokens.colors.background
        case .success:
            tokens.colors.successSoft
        case .warning:
            tokens.colors.accentSoft
        }
    }

    private var border: Color {
        switch tone {
        case .neutral:
            tokens.colors.line
        case .success:
            tokens.colors.success
        case .warning:
            tokens.colors.accent
        }
    }
}

private struct ContentRow: View {
    @Environment(\.qaTokens) private var tokens
    let item: QAContentItem

    var body: some View {
        QACard {
            VStack(alignment: .leading, spacing: 10) {
                HStack {
                    Text(item.source)
                        .font(.qaBody(12, weight: .semibold))
                        .foregroundStyle(tokens.colors.mutedText)
                    Spacer()
                    QATag(text: item.duration)
                }
                Text(item.title)
                    .font(.qaDisplay(23, weight: .medium))
                    .foregroundStyle(tokens.colors.primaryText)
                Text(item.description)
                    .font(.qaBody(14))
                    .foregroundStyle(tokens.colors.mutedText)
                HStack {
                    ForEach(item.topics, id: \.self) { topic in
                        QATag(text: topic)
                    }
                }
            }
        }
        .accessibilityIdentifier("library-item-\(item.id)")
    }
}

private struct MetricCard: View {
    @Environment(\.qaTokens) private var tokens
    let label: String
    let value: String

    var body: some View {
        VStack(alignment: .leading, spacing: 4) {
            Text(value)
                .font(.qaDisplay(28, weight: .medium))
                .foregroundStyle(tokens.colors.primaryText)
            Text(label)
                .font(.qaBody(12, weight: .medium))
                .foregroundStyle(tokens.colors.mutedText)
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(14)
        .background(tokens.colors.elevatedSurface)
        .clipShape(RoundedRectangle(cornerRadius: 16, style: .continuous))
        .overlay(
            RoundedRectangle(cornerRadius: 16, style: .continuous)
                .stroke(tokens.colors.line, lineWidth: 1)
        )
    }
}
