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
                    QAButton(title: "Open link-only backup", style: .secondary, action: onOpenLink)
                    QAButton(title: "Start 3 min meditation", style: .secondary, action: onMeditate)
                    QAButton(title: "Pause for 15 min", style: .quiet, action: onPause)
                }
                Spacer(minLength: 0)
            }
            .padding(20)
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

    var body: some View {
        QAScreen(accessibilityIdentifier: "settings-screen") {
            VStack(alignment: .leading, spacing: 18) {
                HeaderBlock(
                    eyebrow: "SETTINGS",
                    title: "iOS setup will stay explicit.",
                    subtitle: "Screen Time permissions and protected app selection land in Slice 12.2."
                )
                QACard {
                    VStack(alignment: .leading, spacing: 12) {
                        Text("Theme")
                            .font(.qaBody(13, weight: .semibold))
                            .foregroundStyle(tokens.colors.mutedText)
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
                Spacer()
            }
            .padding(20)
        }
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
