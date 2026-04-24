import SwiftUI

struct AppRootView: View {
    @State private var route: QARoute
    @State private var themeMode: QAThemeMode

    init() {
        let arguments = ProcessInfo.processInfo.arguments
        let requestedRoute = arguments.argumentValue(after: "--qa-route").flatMap(QARoute.init(rawValue:)) ?? .home
        let requestedTheme = arguments.contains("--qa-dark") ? QAThemeMode.dark : QAThemeMode.light
        _route = State(initialValue: requestedRoute)
        _themeMode = State(initialValue: requestedTheme)
    }

    var body: some View {
        RootContent(route: $route, themeMode: $themeMode)
            .environment(\.qaTokens, QATokens.tokens(for: themeMode))
            .preferredColorScheme(themeMode == .dark ? .dark : .light)
    }
}

private struct RootContent: View {
    @Binding var route: QARoute
    @Binding var themeMode: QAThemeMode

    var body: some View {
        switch route {
        case .home:
            TabShell(activeRoute: .home, route: $route) {
                HomeScreen(
                    progress: QASampleData.progress,
                    session: QASampleData.session,
                    onStartIntervention: { route = .intervention }
                )
            }
        case .library:
            TabShell(activeRoute: .library, route: $route) {
                LibraryScreen(items: QASampleData.library)
            }
        case .intervention:
            InterventionScreen(
                session: QASampleData.session,
                onReadPrimary: { route = .reader },
                onOpenLink: { route = .handoff },
                onMeditate: { route = .meditation },
                onPause: { route = .home }
            )
        case .reader:
            ReaderScreen(item: QASampleData.readerItem, onDone: { route = .progress })
        case .handoff:
            ExternalHandoffScreen(item: QASampleData.linkOnlyItem, onDone: { route = .progress })
        case .meditation:
            MeditationTimerScreen(item: QASampleData.meditationItem, onDone: { route = .progress })
        case .progress:
            TabShell(activeRoute: .progress, route: $route) {
                ProgressScreen(progress: QASampleData.progress)
            }
        case .settings:
            TabShell(activeRoute: .settings, route: $route) {
                SettingsScreen(themeMode: $themeMode)
            }
        }
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
