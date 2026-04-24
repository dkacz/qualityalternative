import FamilyControls
import SwiftUI

struct HomeScreen: View {
    @Environment(\.qaTokens) private var tokens
    let progress: QAProgressSnapshot
    let session: QAReplacementSession
    let editorialPacks: [QAEditorialPack]
    let userLinks: [QAContentItem]
    let userDocuments: [QAContentItem]
    let meditation: QAContentItem
    let activeDelayState: QASimulatorDelayState?
    let onStartIntervention: () -> Void
    let onAddLink: () -> Void
    let onImportDocument: () -> Void
    let onStartDelayAlternative: () -> Void

    private var editorialItems: [QAContentItem] {
        editorialPacks.flatMap(\.items)
    }

    private var totalItems: Int {
        editorialItems.count + userLinks.count + userDocuments.count + 1
    }

    private var totalMinutes: Int {
        (editorialItems + userLinks + userDocuments + [meditation]).reduce(0) { $0 + $1.durationMinutes }
    }

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

                    QACard {
                        VStack(alignment: .leading, spacing: 14) {
                            SettingsSectionTitle("Your library")
                            LibrarySummaryLine(title: "Editorial picks", value: "\(editorialItems.count) curated")
                            LibrarySummaryLine(title: "Your added links", value: "\(userLinks.count) saved")
                            LibrarySummaryLine(title: "Your files", value: "\(userDocuments.count) saved")
                            LibrarySummaryLine(title: "Utility reset", value: meditation.duration)
                            Text("\(totalItems) items · \(totalMinutes) min available in simulator parity mode")
                                .font(.qaBody(12))
                                .foregroundStyle(tokens.colors.mutedText)
                        }
                    }

                    QAButton(
                        title: "Add a link",
                        style: .secondary,
                        accessibilityIdentifier: "home-add-link",
                        action: onAddLink
                    )
                    QAButton(
                        title: "Import PDF / MD / EPUB",
                        style: .quiet,
                        accessibilityIdentifier: "home-import-document",
                        action: onImportDocument
                    )

                    if let activeDelayState {
                        QACard {
                            VStack(alignment: .leading, spacing: 12) {
                                SettingsSectionTitle("Active delay")
                                Text("Paused replacement · \(activeDelayState.remainingMinutesText)")
                                    .font(.qaBody(15, weight: .semibold))
                                    .foregroundStyle(tokens.colors.primaryText)
                                    .accessibilityIdentifier("active-delay-state")
                                Text("A simulator-only delay card mirrors Android's active pause state without claiming real iOS target-app reopening.")
                                    .font(.qaBody(14))
                                    .foregroundStyle(tokens.colors.mutedText)
                                QAButton(
                                    title: "Read current alternative",
                                    style: .secondary,
                                    accessibilityIdentifier: "active-delay-alternative",
                                    action: onStartDelayAlternative
                                )
                            }
                        }
                    }
                }
                .padding(20)
            }
        }
    }
}

struct LibraryScreen: View {
    @Environment(\.qaTokens) private var tokens
    let packs: [QAEditorialPack]
    let userLinks: [QAContentItem]
    let userDocuments: [QAContentItem]
    let meditation: QAContentItem
    let onAddLink: () -> Void
    let onImportDocument: () -> Void
    let onOpen: (QAContentItem) -> Void
    @State private var filter: Filter = .all

    enum Filter: String, CaseIterable {
        case all
        case editorial
        case yours
        case files

        var label: String {
            switch self {
            case .all:
                "All"
            case .editorial:
                "Editorial"
            case .yours:
                "Your links"
            case .files:
                "Files"
            }
        }
    }

    private var editorialItems: [QAContentItem] {
        packs.flatMap(\.items)
    }

    private var filteredItems: [QAContentItem] {
        switch filter {
        case .all:
            editorialItems + userLinks + userDocuments + [meditation]
        case .editorial:
            editorialItems
        case .yours:
            userLinks
        case .files:
            userDocuments
        }
    }

    var body: some View {
        QAScreen(accessibilityIdentifier: "library-screen") {
            ScrollView {
                VStack(alignment: .leading, spacing: 16) {
                    HStack(alignment: .firstTextBaseline) {
                        HeaderBlock(
                            eyebrow: "LIBRARY",
                            title: "Finite replacements.",
                            subtitle: "\(editorialItems.count) Android editorial items plus simulator saved links, files, and utility reset."
                        )
                        Spacer(minLength: 10)
                        QAButton(title: "Add", style: .secondary, accessibilityIdentifier: "library-add-link", action: onAddLink)
                            .frame(width: 88)
                    }
                    QAButton(
                        title: "Import PDF / MD / EPUB",
                        style: .quiet,
                        accessibilityIdentifier: "library-import-document",
                        action: onImportDocument
                    )
                    HStack(spacing: 8) {
                        ForEach(Filter.allCases, id: \.self) { option in
                            FilterChip(title: option.label, isSelected: filter == option) {
                                filter = option
                            }
                        }
                    }
                    .accessibilityIdentifier("library-filter-row")

                    if filter == .editorial {
                        ForEach(packs) { pack in
                            PackHeader(pack: pack)
                            ForEach(pack.items) { item in
                                ContentRow(item: item, onOpen: { onOpen(item) })
                            }
                        }
                    } else {
                        ForEach(filteredItems) { item in
                            ContentRow(item: item, onOpen: { onOpen(item) })
                        }
                    }
                }
                .padding(20)
            }
        }
    }
}

struct AddLinkScreen: View {
    let onCancel: () -> Void
    let onSave: (QAContentItem) -> Void
    let onImportDocument: () -> Void
    @State private var link = "https://example.com/quality-alternative-saved-link"
    @State private var title = "A Saved Link for Later"
    @State private var durationMinutes = 8

    var body: some View {
        QAScreen(accessibilityIdentifier: "add-link-screen") {
            ScrollView {
                VStack(alignment: .leading, spacing: 16) {
                    HeaderBlock(
                        eyebrow: "SAVED LINK",
                        title: "Add to your quality alternative.",
                        subtitle: "Simulator parity shows Android's saved-link flow without scraping, caching, summarizing, or rehosting the link."
                    )
                    QAEditableField(label: "Link", text: $link, accessibilityIdentifier: "add-link-url-field")
                    QAEditableField(label: "Title", text: $title, accessibilityIdentifier: "add-link-title-field")
                    DurationChipRow(label: "Estimated read", values: [3, 5, 8, 12, 20], selected: durationMinutes) { minutes in
                        durationMinutes = minutes
                    }
                    TopicChipRow(label: "Topic", selected: ["PSYCHOLOGY", "TECH"])
                    QAButton(
                        title: "Add to library",
                        style: .primary,
                        accessibilityIdentifier: "add-link-save"
                    ) {
                        onSave(QALocalContentStore.makeSavedLink(title: title, url: link, durationMinutes: durationMinutes))
                    }
                    QAButton(title: "Import PDF / MD / EPUB instead", style: .quiet, accessibilityIdentifier: "add-link-import-document", action: onImportDocument)
                    QAButton(title: "Cancel", style: .quiet, action: onCancel)
                }
                .padding(20)
            }
        }
    }
}

struct AddDocumentScreen: View {
    @Environment(\.qaTokens) private var tokens
    let onCancel: () -> Void
    let onSave: (QAContentItem) -> Void
    @State private var title = "Private EPUB Extract"
    @State private var format: QAContentFormat = .epub
    @State private var durationMinutes = 20

    var body: some View {
        QAScreen(accessibilityIdentifier: "add-document-screen") {
            ScrollView {
                VStack(alignment: .leading, spacing: 16) {
                    HeaderBlock(
                        eyebrow: "PRIVATE FILE",
                        title: "Add a private reading file.",
                        subtitle: "Markdown and EPUB use the calm in-app reader. PDF remains an external handoff, matching Android scope."
                    )
                    QACard {
                        VStack(alignment: .leading, spacing: 8) {
                            Text("\(format.rawValue) fixture")
                                .font(.qaMono(12))
                                .foregroundStyle(tokens.colors.faintText)
                            Text(fileName)
                                .font(.qaDisplay(24, weight: .medium))
                            Text("File stays on this device. Simulator parity uses local fixtures, not a production file picker.")
                                .font(.qaBody(13))
                                .foregroundStyle(tokens.colors.mutedText)
                        }
                    }
                    FlowLayout(spacing: 8) {
                        documentFormatChip(title: "Markdown", format: .markdown)
                        documentFormatChip(title: "EPUB", format: .epub)
                        documentFormatChip(title: "PDF", format: .pdf)
                    }
                    QAEditableField(label: "Title", text: $title, accessibilityIdentifier: "add-document-title-field")
                    DurationChipRow(label: "Estimated session", values: [5, 10, 15, 20, 30], selected: durationMinutes) { minutes in
                        durationMinutes = minutes
                    }
                    TopicChipRow(label: "Topic", selected: format == .pdf ? ["SCIENCE", "PDF"] : ["PHILOSOPHY", "ESSAYS"])
                    QAButton(
                        title: "Add file to library",
                        style: .primary,
                        accessibilityIdentifier: "add-document-save"
                    ) {
                        onSave(QALocalContentStore.makeDocument(title: title, format: format, durationMinutes: durationMinutes))
                    }
                    QAButton(title: "Cancel", style: .quiet, action: onCancel)
                }
                .padding(20)
            }
        }
    }

    private var fileName: String {
        switch format {
        case .markdown:
            "private-note.md"
        case .epub:
            "private-book.epub"
        case .pdf:
            "private-paper.pdf"
        case .html:
            "private-page.html"
        }
    }

    private func documentFormatChip(title: String, format chipFormat: QAContentFormat) -> some View {
        FilterChip(
            title: title,
            isSelected: format == chipFormat,
            accessibilityIdentifier: "document-format-\(chipFormat.rawValue.lowercased())"
        ) {
            format = chipFormat
            titleDidFollowFormat(chipFormat)
        }
    }

    private func titleDidFollowFormat(_ format: QAContentFormat) {
        switch format {
        case .markdown:
            title = "Private Markdown Note"
            durationMinutes = 10
        case .epub:
            title = "Private EPUB Extract"
            durationMinutes = 20
        case .pdf:
            title = "Private PDF Handoff"
            durationMinutes = 15
        case .html:
            title = "Private Web Archive"
            durationMinutes = 10
        }
    }
}

struct InterventionScreen: View {
    @Environment(\.qaTokens) private var tokens
    let session: QAReplacementSession
    let meditationDurationMinutes: Int
    let onAcceptPrimary: () -> Void
    let onAcceptBackup: (QAContentItem) -> Void
    let onSelectMeditationDuration: (Int) -> Void
    let onPause: () -> Void
    let onContinue: () -> Void

    var body: some View {
        QAScreen(accessibilityIdentifier: "intervention-screen") {
            VStack(alignment: .leading, spacing: 18) {
                HeaderBlock(
                    eyebrow: "PROTECTED SELECTION",
                    title: "A brief detour, if you'd like one.",
                    subtitle: "One primary replacement, two backups, pause, and intentional continuation. iOS keeps the surface native and token-safe."
                )

                QACard {
                    VStack(alignment: .leading, spacing: 14) {
                        HStack {
                            QATag(text: session.primary.duration)
                            QATag(text: renderModeLabel(session.primary))
                        }
                        Text(session.primary.title)
                            .font(.qaDisplay(30, weight: .medium))
                            .foregroundStyle(tokens.colors.primaryText)
                        Text(session.primary.whyThisNow)
                            .font(.qaBody(16))
                            .foregroundStyle(tokens.colors.mutedText)
                        QAButton(
                            title: primaryActionLabel(session.primary),
                            style: .primary,
                            accessibilityIdentifier: "primary-replacement-action",
                            action: onAcceptPrimary
                        )
                    }
                }

                VStack(spacing: 10) {
                    if session.primary.renderMode == .meditationTimer {
                        DurationChipRow(
                            label: "Meditation length",
                            values: [3, 5, 10, 15],
                            selected: meditationDurationMinutes,
                            onSelect: onSelectMeditationDuration
                        )
                    }
                    Text("Other options")
                        .font(.qaMono(12, weight: .medium))
                        .foregroundStyle(tokens.colors.faintText)
                        .frame(maxWidth: .infinity, alignment: .leading)
                    ForEach(Array(session.backups.enumerated()), id: \.element.id) { index, backup in
                        QAButton(
                            title: backupActionTitle(for: backup),
                            style: .secondary,
                            accessibilityIdentifier: "backup-action-\(index)",
                            action: { onAcceptBackup(backup) }
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
        case .inAppReader, .userPrivateReader:
            "Read backup · \(item.duration)"
        case .externalHandoff:
            item.sourceType == .userDocument ? "Open file · \(item.duration)" : "Open link · \(item.duration)"
        case .meditationTimer:
            "Start meditation · \(item.duration)"
        }
    }

    private func primaryActionLabel(_ item: QAContentItem) -> String {
        switch item.renderMode {
        case .inAppReader, .userPrivateReader:
            "Read this · \(item.duration)"
        case .externalHandoff:
            item.sourceType == .userDocument ? "Open file · \(item.duration)" : "Open link · \(item.duration)"
        case .meditationTimer:
            "Start timer · \(item.duration)"
        }
    }

    private func renderModeLabel(_ item: QAContentItem) -> String {
        switch item.renderMode {
        case .inAppReader:
            "Reader"
        case .userPrivateReader:
            item.format == .epub ? "EPUB" : "Markdown"
        case .externalHandoff:
            item.format == .pdf ? "PDF handoff" : "Link-only"
        case .meditationTimer:
            "Timer"
        }
    }
}

struct ReaderScreen: View {
    @Environment(\.qaTokens) private var tokens
    let item: QAContentItem
    let readerBody: String
    let onDone: () -> Void

    private var blocks: [QAReaderBlock] {
        QAReaderMarkdownParser.blocks(for: readerBody, fallback: item.description)
    }

    var body: some View {
        QAScreen(accessibilityIdentifier: "reader-screen") {
            ScrollView {
                VStack(alignment: .leading, spacing: 18) {
                    HeaderBlock(
                        eyebrow: "\(item.source.uppercased()) · \(item.topicLine)",
                        title: item.title,
                        subtitle: item.description
                    )
                    ProgressView(value: 0.42)
                        .tint(tokens.colors.accent)
                        .accessibilityIdentifier("reader-progress")
                    VStack(alignment: .leading, spacing: 14) {
                        ForEach(blocks) { block in
                            ReaderBlockView(block: block)
                        }
                    }
                    QAButton(title: "I'm done reading", style: .primary, action: onDone)
                }
                .padding(20)
            }
        }
    }
}

struct ExternalHandoffScreen: View {
    @Environment(\.qaTokens) private var tokens
    @Environment(\.openURL) private var openURL
    let item: QAContentItem
    let onDone: () -> Void

    private var isFile: Bool {
        item.sourceType == .userDocument
    }

    var body: some View {
        QAScreen(accessibilityIdentifier: "handoff-screen") {
            VStack(alignment: .leading, spacing: 18) {
                HeaderBlock(
                    eyebrow: isFile ? "PRIVATE FILE" : "EXTERNAL HANDOFF",
                    title: item.title,
                    subtitle: isFile
                        ? "PDF stays an external document handoff. Markdown and EPUB use the reader."
                        : "Modern third-party content stays external. We link out intentionally instead of scraping, caching, summarizing, or rehosting."
                )
                QACard {
                    VStack(alignment: .leading, spacing: 14) {
                        Text(item.source)
                            .font(.qaBody(13, weight: .semibold))
                            .foregroundStyle(tokens.colors.mutedText)
                        Text(item.whyThisNow)
                            .font(.qaBody(16))
                            .foregroundStyle(tokens.colors.primaryText)
                        Text(item.externalURL ?? item.description)
                            .font(.qaMono(12))
                            .foregroundStyle(tokens.colors.faintText)
                        QAButton(
                            title: isFile ? "Open file" : "Open link",
                            style: .primary,
                            accessibilityIdentifier: "external-open-action",
                            isEnabled: targetURL != nil
                        ) {
                            if let targetURL {
                                openURL(targetURL)
                            }
                        }
                        QAButton(
                            title: isFile ? "I've finished this file" : "I've finished this link",
                            style: .secondary,
                            accessibilityIdentifier: "external-link-done",
                            action: onDone
                        )
                    }
                }
                Spacer()
            }
            .padding(20)
        }
    }

    private var targetURL: URL? {
        guard let externalURL = item.externalURL else {
            return nil
        }
        return URL(string: externalURL)
    }
}

struct MeditationTimerScreen: View {
    @Environment(\.qaTokens) private var tokens
    let item: QAContentItem
    let meditationDurationMinutes: Int
    let onSelectMeditationDuration: (Int) -> Void
    let onDone: () -> Void

    var body: some View {
        QAScreen(accessibilityIdentifier: "meditation-screen") {
            VStack(spacing: 22) {
                HeaderBlock(
                    eyebrow: "MEDITATION",
                    title: item.title,
                    subtitle: "Put the phone down if you can. A simulator timer surface mirrors Android's adjustable utility replacement."
                )
                DurationChipRow(
                    label: "Length for this reset",
                    values: [3, 5, 10, 15],
                    selected: meditationDurationMinutes,
                    onSelect: onSelectMeditationDuration
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
                        Text(String(format: "%02d:00", item.durationMinutes))
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
                QAButton(title: "End early", style: .quiet, accessibilityIdentifier: "meditation-skip", action: onDone)
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

struct FeedbackScreen: View {
    @Environment(\.qaTokens) private var tokens
    let item: QAContentItem
    let onDone: () -> Void

    var body: some View {
        QAScreen(accessibilityIdentifier: "feedback-screen") {
            VStack(alignment: .leading, spacing: 18) {
                HeaderBlock(
                    eyebrow: "FEEDBACK",
                    title: "Did this help?",
                    subtitle: "Simulator parity keeps Android's quick reflection after a finite replacement session."
                )
                QACard {
                    VStack(alignment: .leading, spacing: 14) {
                        Text(item.title)
                            .font(.qaDisplay(24, weight: .medium))
                            .foregroundStyle(tokens.colors.primaryText)
                        Text("One tap is enough; analytics wiring is separate from this simulator-only parity pass.")
                            .font(.qaBody(14))
                            .foregroundStyle(tokens.colors.mutedText)
                        HStack(spacing: 8) {
                            QATag(text: "Helpful")
                            QATag(text: "Too long")
                            QATag(text: "Not now")
                        }
                    }
                }
                QAButton(title: "Save feedback", style: .primary, accessibilityIdentifier: "feedback-save", action: onDone)
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
    let deviceActivitySchedule: QADeviceActivityScheduleState?
    @Binding var meditationDurationMinutes: Int
    @Binding var contentPriority: QAContentPriority
    let onRequestScreenTimeAuthorization: () -> Void
    let onApplyShieldRules: () -> Void
    let onPauseShieldRules: () -> Void
    let onResumeShieldRules: () -> Void
    let onClearShieldRules: () -> Void
    let onStartDeviceActivityMonitoring: () -> Void
    let onStopDeviceActivityMonitoring: () -> Void
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

    private var deviceActivitySnapshot: QADeviceActivityScheduleSnapshot {
        QADeviceActivityScheduleSnapshot(
            setup: setupSnapshot,
            state: deviceActivitySchedule
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
                        VStack(alignment: .leading, spacing: 14) {
                            SettingsSectionTitle("Device Activity monitor")
                            HStack(spacing: 8) {
                                StatusPill(
                                    text: deviceActivitySnapshot.statusTitle,
                                    tone: deviceActivityStatusTone,
                                    accessibilityIdentifier: "device-activity-state-pill"
                                )
                                StatusPill(
                                    text: deviceActivitySnapshot.canStartMonitoring ? "Ready to schedule" : "Needs setup",
                                    tone: deviceActivitySnapshot.canStartMonitoring ? .success : .neutral
                                )
                            }
                            Text(deviceActivitySnapshot.detailText)
                                .font(.qaBody(15))
                                .foregroundStyle(tokens.colors.primaryText)
                                .accessibilityIdentifier("device-activity-detail")

                            QAButton(
                                title: "Start monitor schedule",
                                style: .primary,
                                accessibilityIdentifier: "start-device-activity-monitoring",
                                isEnabled: deviceActivitySnapshot.canStartMonitoring,
                                action: onStartDeviceActivityMonitoring
                            )
                            if deviceActivitySnapshot.canStopMonitoring {
                                QAButton(
                                    title: "Stop monitor schedule",
                                    style: .quiet,
                                    accessibilityIdentifier: "stop-device-activity-monitoring",
                                    action: onStopDeviceActivityMonitoring
                                )
                            }

                            Text("DeviceActivity can reapply shield rules during protected windows; simulator checks compile and host state only.")
                                .font(.qaBody(12))
                                .lineSpacing(2)
                                .foregroundStyle(tokens.colors.mutedText)
                        }
                    }
                    .accessibilityIdentifier("device-activity-card")

                    QACard {
                        VStack(alignment: .leading, spacing: 12) {
                            SettingsSectionTitle("Content priority")
                            Text("Keep recommendations balanced, or gently prioritize one type.")
                                .font(.qaBody(13))
                                .foregroundStyle(tokens.colors.mutedText)
                            FlowLayout(spacing: 8) {
                                ForEach(QAContentPriority.allCases, id: \.self) { priority in
                                    FilterChip(
                                        title: priority.label,
                                        isSelected: contentPriority == priority,
                                        accessibilityIdentifier: "content-priority-\(priority.rawValue)"
                                    ) {
                                        contentPriority = priority
                                    }
                                }
                            }
                            Text(contentPriority.description)
                                .font(.qaBody(12))
                                .lineSpacing(2)
                                .foregroundStyle(tokens.colors.mutedText)
                        }
                    }

                    QACard {
                        VStack(alignment: .leading, spacing: 12) {
                            SettingsSectionTitle("Default session length")
                            DurationChipRow(label: "Meditation reset", values: [3, 5, 10, 15], selected: meditationDurationMinutes) { minutes in
                                meditationDurationMinutes = minutes
                            }
                            Text("This default is also adjustable immediately before starting a meditation replacement.")
                                .font(.qaBody(12))
                                .lineSpacing(2)
                                .foregroundStyle(tokens.colors.mutedText)
                        }
                    }

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
            .padding(.top, 28)
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

    private var deviceActivityStatusTone: StatusPill.Tone {
        switch deviceActivitySchedule?.mode {
        case .scheduled:
            return .success
        case .failed:
            return .warning
        case .inactive, .stopped, nil:
            return .neutral
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

private struct LibrarySummaryLine: View {
    @Environment(\.qaTokens) private var tokens
    let title: String
    let value: String

    var body: some View {
        HStack {
            Text(title)
                .font(.qaBody(14, weight: .medium))
                .foregroundStyle(tokens.colors.primaryText)
            Spacer()
            Text(value)
                .font(.qaMono(12))
                .foregroundStyle(tokens.colors.mutedText)
        }
    }
}

private struct PackHeader: View {
    @Environment(\.qaTokens) private var tokens
    let pack: QAEditorialPack

    var body: some View {
        VStack(alignment: .leading, spacing: 4) {
            Text(pack.title)
                .font(.qaBody(14, weight: .semibold))
                .foregroundStyle(tokens.colors.primaryText)
            Text(pack.description)
                .font(.qaBody(12))
                .foregroundStyle(tokens.colors.mutedText)
        }
        .padding(.top, 10)
    }
}

private struct FilterChip: View {
    @Environment(\.qaTokens) private var tokens
    let title: String
    let isSelected: Bool
    let accessibilityIdentifier: String?
    let action: () -> Void

    init(title: String, isSelected: Bool, accessibilityIdentifier: String? = nil, action: @escaping () -> Void) {
        self.title = title
        self.isSelected = isSelected
        self.accessibilityIdentifier = accessibilityIdentifier
        self.action = action
    }

    var body: some View {
        Button(action: action) {
            Text(title)
                .font(.qaBody(12, weight: .semibold))
                .foregroundStyle(isSelected ? selectedText : tokens.colors.mutedText)
                .padding(.horizontal, 11)
                .padding(.vertical, 8)
                .background(isSelected ? tokens.colors.accent : tokens.colors.background)
                .clipShape(Capsule())
                .overlay(Capsule().stroke(isSelected ? tokens.colors.accent : tokens.colors.line, lineWidth: 1))
        }
        .buttonStyle(.plain)
        .accessibilityIdentifier(accessibilityIdentifier ?? title)
    }

    private var selectedText: Color {
        tokens.mode == .dark ? Color(hex: 0x21140E) : .white
    }
}

private struct DurationChipRow: View {
    @Environment(\.qaTokens) private var tokens
    let label: String
    let values: [Int]
    let selected: Int
    let onSelect: (Int) -> Void

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            Text(label)
                .font(.qaBody(13, weight: .semibold))
                .foregroundStyle(tokens.colors.mutedText)
            HStack(spacing: 7) {
                ForEach(values, id: \.self) { minutes in
                    FilterChip(title: "\(minutes) min", isSelected: selected == minutes, accessibilityIdentifier: "duration-\(minutes)") {
                        onSelect(minutes)
                    }
                    .frame(maxWidth: .infinity)
                }
            }
        }
    }
}

private struct TopicChipRow: View {
    @Environment(\.qaTokens) private var tokens
    let label: String
    let selected: [String]

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            Text(label)
                .font(.qaBody(13, weight: .semibold))
                .foregroundStyle(tokens.colors.mutedText)
            FlowLayout(spacing: 8) {
                ForEach(selected, id: \.self) { topic in
                    QATag(text: topic)
                }
            }
        }
    }
}

private struct QAFormField: View {
    @Environment(\.qaTokens) private var tokens
    let label: String
    let value: String

    var body: some View {
        VStack(alignment: .leading, spacing: 7) {
            Text(label)
                .font(.qaBody(13, weight: .semibold))
                .foregroundStyle(tokens.colors.mutedText)
            Text(value)
                .font(.qaBody(15))
                .foregroundStyle(tokens.colors.primaryText)
                .frame(maxWidth: .infinity, alignment: .leading)
                .padding(14)
                .background(tokens.colors.elevatedSurface)
                .clipShape(RoundedRectangle(cornerRadius: 14, style: .continuous))
                .overlay(
                    RoundedRectangle(cornerRadius: 14, style: .continuous)
                        .stroke(tokens.colors.line, lineWidth: 1)
                )
        }
    }
}

private struct QAEditableField: View {
    @Environment(\.qaTokens) private var tokens
    let label: String
    @Binding var text: String
    let accessibilityIdentifier: String

    var body: some View {
        VStack(alignment: .leading, spacing: 7) {
            Text(label)
                .font(.qaBody(13, weight: .semibold))
                .foregroundStyle(tokens.colors.mutedText)
            TextField(label, text: $text)
                .font(.qaBody(15))
                .foregroundStyle(tokens.colors.primaryText)
                .textInputAutocapitalization(.never)
                .autocorrectionDisabled()
                .padding(14)
                .background(tokens.colors.elevatedSurface)
                .clipShape(RoundedRectangle(cornerRadius: 14, style: .continuous))
                .overlay(
                    RoundedRectangle(cornerRadius: 14, style: .continuous)
                        .stroke(tokens.colors.line, lineWidth: 1)
                )
                .accessibilityIdentifier(accessibilityIdentifier)
        }
    }
}

private struct FlowLayout: Layout {
    let spacing: CGFloat

    init(spacing: CGFloat = 8) {
        self.spacing = spacing
    }

    func sizeThatFits(proposal: ProposedViewSize, subviews: Subviews, cache: inout ()) -> CGSize {
        let maxWidth = proposal.width ?? 320
        var rows: [CGSize] = [.zero]
        for subview in subviews {
            let size = subview.sizeThatFits(.unspecified)
            let row = rows.count - 1
            let nextWidth = rows[row].width == 0 ? size.width : rows[row].width + spacing + size.width
            if nextWidth > maxWidth, rows[row].width > 0 {
                rows.append(size)
            } else {
                rows[row].width = nextWidth
                rows[row].height = max(rows[row].height, size.height)
            }
        }
        return CGSize(
            width: maxWidth,
            height: rows.reduce(0) { $0 + $1.height } + CGFloat(max(0, rows.count - 1)) * spacing
        )
    }

    func placeSubviews(in bounds: CGRect, proposal: ProposedViewSize, subviews: Subviews, cache: inout ()) {
        var x = bounds.minX
        var y = bounds.minY
        var rowHeight: CGFloat = 0
        for subview in subviews {
            let size = subview.sizeThatFits(.unspecified)
            if x > bounds.minX, x + size.width > bounds.maxX {
                x = bounds.minX
                y += rowHeight + spacing
                rowHeight = 0
            }
            subview.place(at: CGPoint(x: x, y: y), proposal: ProposedViewSize(size))
            x += size.width + spacing
            rowHeight = max(rowHeight, size.height)
        }
    }
}

private struct ContentRow: View {
    @Environment(\.qaTokens) private var tokens
    let item: QAContentItem
    let onOpen: () -> Void

    var body: some View {
        Button(action: onOpen) {
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
        .buttonStyle(.plain)
        .padding(18)
        .background(tokens.colors.elevatedSurface)
        .clipShape(RoundedRectangle(cornerRadius: 18, style: .continuous))
        .overlay(
            RoundedRectangle(cornerRadius: 18, style: .continuous)
                .stroke(tokens.colors.line, lineWidth: 1)
        )
        .accessibilityIdentifier("library-item-\(item.id)")
    }
}

private enum QAReaderBlockKind {
    case heading
    case quote
    case list
    case code
    case body
}

private struct QAReaderBlock: Identifiable {
    let id = UUID()
    let text: String
    let kind: QAReaderBlockKind
}

private enum QAReaderMarkdownParser {
    static func blocks(for body: String, fallback: String) -> [QAReaderBlock] {
        let source = body.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty ? fallback : body
        let rawBlocks = source
            .components(separatedBy: "\n\n")
            .map { $0.trimmingCharacters(in: .whitespacesAndNewlines) }
            .filter { !$0.isEmpty }
        let parsed = rawBlocks.map(block)
        return parsed.isEmpty ? [QAReaderBlock(text: fallback, kind: .body)] : parsed
    }

    private static func block(_ raw: String) -> QAReaderBlock {
        if raw.hasPrefix("```") {
            return QAReaderBlock(text: raw.replacingOccurrences(of: "```", with: "").trimmed, kind: .code)
        }
        if raw.hasPrefix("#") {
            return QAReaderBlock(text: raw.replacingOccurrences(of: #"^#{1,6}\s*"#, with: "", options: .regularExpression).trimmed, kind: .heading)
        }
        if raw.hasPrefix(">") {
            return QAReaderBlock(text: raw.replacingOccurrences(of: #"^>\s*"#, with: "", options: .regularExpression).trimmed, kind: .quote)
        }
        if raw.hasPrefix("- ") || raw.range(of: #"^\d+\.\s"#, options: .regularExpression) != nil {
            return QAReaderBlock(
                text: raw
                    .components(separatedBy: .newlines)
                    .map { line in line.replacingOccurrences(of: #"^[-*]\s+|^\d+\.\s+"#, with: "• ", options: .regularExpression) }
                    .joined(separator: "\n")
                    .trimmed,
                kind: .list
            )
        }
        return QAReaderBlock(text: raw.replacingInlineMarkdown.trimmed, kind: .body)
    }
}

private struct ReaderBlockView: View {
    @Environment(\.qaTokens) private var tokens
    let block: QAReaderBlock

    var body: some View {
        Text(block.text)
            .font(font)
            .lineSpacing(lineSpacing)
            .foregroundStyle(color)
            .frame(maxWidth: .infinity, alignment: .leading)
            .padding(padding)
            .background(background)
            .clipShape(RoundedRectangle(cornerRadius: 14, style: .continuous))
            .overlay(
                RoundedRectangle(cornerRadius: 14, style: .continuous)
                    .stroke(border, lineWidth: block.kind == .code ? 1 : 0)
            )
    }

    private var font: Font {
        switch block.kind {
        case .heading:
            .qaDisplay(24, weight: .semibold)
        case .code:
            .qaMono(14)
        case .quote:
            .qaDisplay(20).italic()
        case .list, .body:
            .qaDisplay(21)
        }
    }

    private var lineSpacing: CGFloat {
        block.kind == .code ? 3 : 5
    }

    private var color: Color {
        block.kind == .quote ? tokens.colors.mutedText : tokens.colors.primaryText
    }

    private var padding: EdgeInsets {
        block.kind == .code ? EdgeInsets(top: 14, leading: 14, bottom: 14, trailing: 14) : EdgeInsets()
    }

    private var background: Color {
        block.kind == .code ? tokens.colors.elevatedSurface : .clear
    }

    private var border: Color {
        block.kind == .code ? tokens.colors.line : .clear
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

private extension String {
    var trimmed: String {
        trimmingCharacters(in: .whitespacesAndNewlines)
    }

    var replacingInlineMarkdown: String {
        replacingOccurrences(of: #"(\*\*|__)(.*?)\1"#, with: "$2", options: .regularExpression)
            .replacingOccurrences(of: #"(\*|_)(.*?)\1"#, with: "$2", options: .regularExpression)
            .replacingOccurrences(of: #"`([^`]+)`"#, with: "$1", options: .regularExpression)
            .replacingOccurrences(of: #"\[([^\]]+)\]\([^\)]+\)"#, with: "$1", options: .regularExpression)
    }
}
