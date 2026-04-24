import FamilyControls
import SwiftUI

struct AppRootView: View {
    @Environment(\.scenePhase) private var scenePhase
    @State private var route: QARoute
    @State private var themeMode: QAThemeMode
    @State private var screenTimeAuthorization: QAScreenTimeAuthorizationState
    @State private var screenTimeAuthorizationError: String?
    @State private var protectedSelection: FamilyActivitySelection
    @State private var shieldSession: QAShieldSessionState?
    @State private var deviceActivitySchedule: QADeviceActivityScheduleState?
    @State private var selectedContent: QAContentItem
    @State private var meditationDurationMinutes: Int
    @State private var contentPriority: QAContentPriority
    @State private var localLibrary: QALocalLibraryState
    @State private var activeDelayState: QASimulatorDelayState?
    private let shouldConsumeShieldIntent: Bool
    private let shieldApplier = QAManagedSettingsShieldApplier()

    init() {
        let arguments = ProcessInfo.processInfo.arguments
        if arguments.contains("--qa-reset-local-state") {
            QALocalContentStore.reset()
            QALocalSettingsStore.reset()
        }
        let hasExplicitRoute = arguments.contains("--qa-route")
        let requestedRoute = arguments.argumentValue(after: "--qa-route").flatMap(QARoute.init(rawValue:)) ?? .home
        let requestedTheme = arguments.contains("--qa-dark") ? QAThemeMode.dark : QAThemeMode.light
        let settings = QALocalSettingsStore.load()
        let initialMeditationDuration = settings.meditationDurationMinutes
        let initialLibrary = QALocalContentStore.load()
        let requestedContentID = arguments.argumentValue(after: "--qa-content-id")
        _route = State(initialValue: requestedRoute)
        _themeMode = State(initialValue: requestedTheme)
        _screenTimeAuthorization = State(initialValue: QAScreenTimeAuthorizationState(AuthorizationCenter.shared.authorizationStatus))
        _screenTimeAuthorizationError = State(initialValue: nil)
        _protectedSelection = State(initialValue: QAFamilyActivitySelectionStore.load())
        _shieldSession = State(initialValue: QAShieldSessionStore.load())
        _deviceActivitySchedule = State(initialValue: QADeviceActivityScheduleStore.load())
        _selectedContent = State(
            initialValue: Self.initialContent(
                for: requestedRoute,
                meditationMinutes: initialMeditationDuration,
                localLibrary: initialLibrary,
                contentID: requestedContentID
            )
        )
        _meditationDurationMinutes = State(initialValue: initialMeditationDuration)
        _contentPriority = State(initialValue: settings.contentPriority)
        _localLibrary = State(initialValue: initialLibrary)
        _activeDelayState = State(initialValue: nil)
        shouldConsumeShieldIntent = !hasExplicitRoute
    }

    private static func initialContent(
        for route: QARoute,
        meditationMinutes: Int,
        localLibrary: QALocalLibraryState,
        contentID: String?
    ) -> QAContentItem {
        if let contentID, let item = QASampleData.item(withID: contentID, localLibrary: localLibrary) {
            return item
        }
        return switch route {
        case .handoff:
            QASampleData.linkOnlyItem
        case .meditation:
            QASampleData.meditationContentItem(minutes: meditationMinutes)
        case .reader, .feedback:
            QASampleData.readerItem
        case .home, .library, .addLink, .addDocument, .intervention, .progress, .settings:
            QASampleData.readerItem
        }
    }

    var body: some View {
        RootContent(
            route: $route,
            themeMode: $themeMode,
            screenTimeAuthorization: $screenTimeAuthorization,
            screenTimeAuthorizationError: $screenTimeAuthorizationError,
            protectedSelection: $protectedSelection,
            shieldSession: $shieldSession,
            deviceActivitySchedule: $deviceActivitySchedule,
            selectedContent: $selectedContent,
            meditationDurationMinutes: $meditationDurationMinutes,
            contentPriority: $contentPriority,
            localLibrary: $localLibrary,
            activeDelayState: $activeDelayState,
            shieldApplier: shieldApplier
        )
            .environment(\.qaTokens, QATokens.tokens(for: themeMode))
            .preferredColorScheme(themeMode == .dark ? .dark : .light)
            .onReceive(AuthorizationCenter.shared.$authorizationStatus) { status in
                screenTimeAuthorization = QAScreenTimeAuthorizationState(status)
            }
            .onChange(of: protectedSelection) { _, selection in
                QAFamilyActivitySelectionStore.save(selection)
            }
            .onAppear {
                consumePendingShieldIntentIfNeeded()
            }
            .onChange(of: scenePhase) { _, phase in
                guard phase == .active else {
                    return
                }
                consumePendingShieldIntentIfNeeded()
            }
    }

    private func consumePendingShieldIntentIfNeeded() {
        let plan = QAShieldHostForegroundResolver.resolve(
            refreshedSession: QAShieldSessionStore.load(),
            pendingIntent: shouldConsumeShieldIntent ? QAShieldActionIntentStore.load() : nil
        )
        shieldSession = plan.refreshedSession
        guard shouldConsumeShieldIntent else {
            return
        }
        guard let routedIntent = plan.route else {
            return
        }
        route = routedIntent
        if plan.shouldClearIntent {
            QAShieldActionIntentStore.clear()
        }
    }
}

private struct RootContent: View {
    @Binding var route: QARoute
    @Binding var themeMode: QAThemeMode
    @Binding var screenTimeAuthorization: QAScreenTimeAuthorizationState
    @Binding var screenTimeAuthorizationError: String?
    @Binding var protectedSelection: FamilyActivitySelection
    @Binding var shieldSession: QAShieldSessionState?
    @Binding var deviceActivitySchedule: QADeviceActivityScheduleState?
    @Binding var selectedContent: QAContentItem
    @Binding var meditationDurationMinutes: Int
    @Binding var contentPriority: QAContentPriority
    @Binding var localLibrary: QALocalLibraryState
    @Binding var activeDelayState: QASimulatorDelayState?
    let shieldApplier: QAManagedSettingsShieldApplier

    private var activeSession: QAReplacementSession {
        QASampleData.replacementSession(
            meditationMinutes: meditationDurationMinutes,
            priority: contentPriority,
            userLinks: localLibrary.userLinks,
            userDocuments: localLibrary.userDocuments
        )
    }

    var body: some View {
        switch route {
        case .home:
            TabShell(activeRoute: .home, route: $route) {
                HomeScreen(
                    progress: QASampleData.progress,
                    session: activeSession,
                    editorialPacks: QASampleData.packs,
                    userLinks: localLibrary.userLinks,
                    userDocuments: localLibrary.userDocuments,
                    meditation: QASampleData.meditationContentItem(minutes: meditationDurationMinutes),
                    activeDelayState: activeDelayState?.isActive() == true ? activeDelayState : nil,
                    onStartIntervention: { route = .intervention },
                    onAddLink: { route = .addLink },
                    onImportDocument: { route = .addDocument },
                    onStartDelayAlternative: {
                        if let contentID = activeDelayState?.selectedContentID,
                           let item = QASampleData.item(withID: contentID, localLibrary: localLibrary) {
                            selectedContent = item
                        } else {
                            selectedContent = activeSession.primary
                        }
                        route = route(for: selectedContent)
                    }
                )
            }
        case .library:
            TabShell(activeRoute: .library, route: $route) {
                LibraryScreen(
                    packs: QASampleData.packs,
                    userLinks: localLibrary.userLinks,
                    userDocuments: localLibrary.userDocuments,
                    meditation: QASampleData.meditationContentItem(minutes: meditationDurationMinutes),
                    onAddLink: { route = .addLink },
                    onImportDocument: { route = .addDocument },
                    onOpen: { item in
                        selectedContent = item
                        route = route(for: item)
                    }
                )
            }
        case .addLink:
            AddLinkScreen(
                onCancel: { route = .library },
                onSave: { item in
                    localLibrary.userLinks.append(item)
                    QALocalContentStore.save(localLibrary)
                    route = .library
                },
                onImportDocument: { route = .addDocument }
            )
        case .addDocument:
            AddDocumentScreen(
                onCancel: { route = .library },
                onSave: { item in
                    localLibrary.userDocuments.append(item)
                    QALocalContentStore.save(localLibrary)
                    route = .library
                }
            )
        case .intervention:
            InterventionScreen(
                session: activeSession,
                meditationDurationMinutes: meditationDurationMinutes,
                onAcceptPrimary: {
                    selectedContent = activeSession.primary
                    route = route(for: activeSession.primary)
                },
                onAcceptBackup: { item in
                    selectedContent = item
                    route = route(for: item)
                },
                onSelectMeditationDuration: { minutes in
                    setMeditationDuration(minutes)
                    if selectedContent.renderMode == .meditationTimer {
                        selectedContent = QASampleData.meditationContentItem(minutes: minutes)
                    }
                },
                onPause: {
                    let now = Date()
                    activeDelayState = QASimulatorDelayState(
                        startedAt: now,
                        expiresAt: now.addingTimeInterval(15 * 60),
                        selectedContentID: activeSession.primary.id
                    )
                    route = .home
                },
                onContinue: {
                    activeDelayState = nil
                    route = .home
                }
            )
        case .reader:
            ReaderScreen(item: selectedContent, readerBody: QASampleData.body(for: selectedContent), onDone: { route = .feedback })
        case .handoff:
            ExternalHandoffScreen(item: selectedContent, onDone: { route = .feedback })
        case .meditation:
            MeditationTimerScreen(
                item: selectedContent.renderMode == .meditationTimer ? selectedContent : QASampleData.meditationContentItem(minutes: meditationDurationMinutes),
                meditationDurationMinutes: meditationDurationMinutes,
                onSelectMeditationDuration: { minutes in
                    setMeditationDuration(minutes)
                    selectedContent = QASampleData.meditationContentItem(minutes: minutes)
                },
                onDone: { route = .feedback }
            )
        case .feedback:
            FeedbackScreen(item: selectedContent, onDone: { route = .progress })
        case .progress:
            TabShell(activeRoute: .progress, route: $route) {
                ProgressScreen(progress: QASampleData.progress)
            }
        case .settings:
            TabShell(activeRoute: .settings, route: $route) {
                SettingsScreen(
                    themeMode: $themeMode,
                    screenTimeAuthorization: screenTimeAuthorization,
                    screenTimeAuthorizationError: screenTimeAuthorizationError,
                    protectedSelection: $protectedSelection,
                    shieldSession: shieldSession,
                    deviceActivitySchedule: deviceActivitySchedule,
                    meditationDurationMinutes: Binding(
                        get: { meditationDurationMinutes },
                        set: { setMeditationDuration($0) }
                    ),
                    contentPriority: Binding(
                        get: { contentPriority },
                        set: { setContentPriority($0) }
                    ),
                    onRequestScreenTimeAuthorization: requestScreenTimeAuthorization,
                    onApplyShieldRules: applyShieldRules,
                    onPauseShieldRules: pauseShieldRules,
                    onResumeShieldRules: resumeShieldRules,
                    onClearShieldRules: clearShieldRules,
                    onStartDeviceActivityMonitoring: startDeviceActivityMonitoring,
                    onStopDeviceActivityMonitoring: stopDeviceActivityMonitoring
                )
            }
        }
    }

    private func route(for item: QAContentItem) -> QARoute {
        switch item.renderMode {
        case .inAppReader, .userPrivateReader:
            .reader
        case .externalHandoff:
            .handoff
        case .meditationTimer:
            .meditation
        }
    }

    private func setMeditationDuration(_ minutes: Int) {
        meditationDurationMinutes = minutes
        QALocalSettingsStore.save(
            QALocalSettings(meditationDurationMinutes: minutes, contentPriority: contentPriority)
        )
    }

    private func setContentPriority(_ priority: QAContentPriority) {
        contentPriority = priority
        QALocalSettingsStore.save(
            QALocalSettings(meditationDurationMinutes: meditationDurationMinutes, contentPriority: priority)
        )
    }

    private func requestScreenTimeAuthorization() {
        screenTimeAuthorizationError = nil
        Task {
            do {
                try await AuthorizationCenter.shared.requestAuthorization(for: .individual)
                screenTimeAuthorization = QAScreenTimeAuthorizationState(AuthorizationCenter.shared.authorizationStatus)
            } catch {
                screenTimeAuthorization = QAScreenTimeAuthorizationState(AuthorizationCenter.shared.authorizationStatus)
                screenTimeAuthorizationError = error.localizedDescription
            }
        }
    }

    private var screenTimeSetupSnapshot: QAScreenTimeSetupSnapshot {
        QAScreenTimeSetupSnapshot(
            authorization: screenTimeAuthorization,
            selection: QAScreenTimeSelectionSummary(selection: protectedSelection)
        )
    }

    private func applyShieldRules() {
        guard screenTimeSetupSnapshot.canPrepareShielding else {
            return
        }
        let now = Date()
        let state = QAShieldSessionState.armed(
            session: activeSession,
            selection: screenTimeSetupSnapshot.selection,
            now: now
        )
        shieldApplier.apply(selection: protectedSelection)
        shieldSession = state
        QAShieldSessionStore.save(state)
    }

    private func pauseShieldRules() {
        guard let shieldSession else {
            return
        }
        let now = Date()
        let state = shieldSession.paused(until: now.addingTimeInterval(15 * 60), now: now)
        shieldApplier.clear()
        self.shieldSession = state
        QAShieldSessionStore.save(state)
    }

    private func resumeShieldRules() {
        guard screenTimeSetupSnapshot.canPrepareShielding else {
            return
        }
        applyShieldRules()
    }

    private func clearShieldRules() {
        shieldApplier.clear()
        shieldSession = nil
        QAShieldSessionStore.clear()
        deviceActivitySchedule = QADeviceActivityScheduler().stopProtectedWindow()
    }

    private func startDeviceActivityMonitoring() {
        do {
            deviceActivitySchedule = try QADeviceActivityScheduler().startProtectedWindow(
                selection: protectedSelection,
                setup: screenTimeSetupSnapshot
            )
        } catch {
            let state = QADeviceActivityScheduleState.failed(
                selection: screenTimeSetupSnapshot.selection,
                error: error,
                now: Date()
            )
            QADeviceActivityScheduleStore.save(state)
            deviceActivitySchedule = state
        }
    }

    private func stopDeviceActivityMonitoring() {
        deviceActivitySchedule = QADeviceActivityScheduler().stopProtectedWindow()
    }
}

private struct TabShell<Content: View>: View {
    @Environment(\.qaTokens) private var tokens
    let activeRoute: QARoute
    @Binding var route: QARoute
    let content: Content

    init(activeRoute: QARoute, route: Binding<QARoute>, @ViewBuilder content: () -> Content) {
        self.activeRoute = activeRoute
        _route = route
        self.content = content()
    }

    var body: some View {
        VStack(spacing: 0) {
            content
            HStack(spacing: 10) {
                tab("Home", .home)
                tab("Library", .library)
                tab("Progress", .progress)
                tab("Settings", .settings)
            }
            .padding(.horizontal, 16)
            .padding(.top, 10)
            .padding(.bottom, 12)
            .background(tokens.colors.elevatedSurface)
            .overlay(alignment: .top) {
                Rectangle()
                    .fill(tokens.colors.line)
                    .frame(height: 1)
            }
        }
        .background(tokens.colors.background)
    }

    private func tab(_ title: String, _ target: QARoute) -> some View {
        Button {
            route = target
        } label: {
            Text(title)
                .font(.qaBody(12, weight: activeRoute == target ? .semibold : .medium))
                .foregroundStyle(activeRoute == target ? tokens.colors.primaryText : tokens.colors.mutedText)
                .frame(maxWidth: .infinity)
                .padding(.vertical, 10)
                .background(activeRoute == target ? tokens.colors.accentSoft : Color.clear)
                .clipShape(Capsule())
        }
        .buttonStyle(.plain)
        .accessibilityIdentifier("tab-\(target.rawValue)")
    }
}

private extension [String] {
    func argumentValue(after flag: String) -> String? {
        guard let index = firstIndex(of: flag) else {
            return nil
        }
        let nextIndex = self.index(after: index)
        guard nextIndex < count else {
            return nil
        }
        return self[nextIndex]
    }
}

private extension QAScreenTimeAuthorizationState {
    init(_ status: AuthorizationStatus) {
        switch status {
        case .notDetermined:
            self = .notDetermined
        case .denied:
            self = .denied
        case .approved:
            self = .approved
        @unknown default:
            self = .denied
        }
    }
}
