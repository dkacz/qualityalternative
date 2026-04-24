import SwiftUI

enum QAThemeMode: String, CaseIterable {
    case light
    case dark
}

struct QAColors: Equatable {
    let background: Color
    let elevatedSurface: Color
    let primaryText: Color
    let mutedText: Color
    let faintText: Color
    let line: Color
    let lineStrong: Color
    let accent: Color
    let accentSoft: Color
    let success: Color
    let successSoft: Color

    static let light = QAColors(
        background: Color(hex: 0xF2EADE),
        elevatedSurface: Color(hex: 0xFBF4EA),
        primaryText: Color(hex: 0x271D17),
        mutedText: Color(hex: 0x64554B),
        faintText: Color(hex: 0x9C9086),
        line: Color(hex: 0xDACFC3),
        lineStrong: Color(hex: 0xBDAEA1),
        accent: Color(hex: 0x965630),
        accentSoft: Color(hex: 0xF4D8C5),
        success: Color(hex: 0x647A4E),
        successSoft: Color(hex: 0xDAE2C5)
    )

    static let dark = QAColors(
        background: Color(hex: 0x463B33),
        elevatedSurface: Color(hex: 0x54473F),
        primaryText: Color(hex: 0xEFE6DB),
        mutedText: Color(hex: 0xBAAFA4),
        faintText: Color(hex: 0x8A7E73),
        line: Color(hex: 0x685B50),
        lineStrong: Color(hex: 0x807165),
        accent: Color(hex: 0xE9A679),
        accentSoft: Color(hex: 0x6C4932),
        success: Color(hex: 0xA9C192),
        successSoft: Color(hex: 0x445335)
    )
}

struct QATokens {
    let mode: QAThemeMode
    let colors: QAColors

    static func tokens(for mode: QAThemeMode) -> QATokens {
        QATokens(mode: mode, colors: mode == .dark ? .dark : .light)
    }
}

private struct QATokensKey: EnvironmentKey {
    static let defaultValue = QATokens.tokens(for: .light)
}

extension EnvironmentValues {
    var qaTokens: QATokens {
        get { self[QATokensKey.self] }
        set { self[QATokensKey.self] = newValue }
    }
}

extension Font {
    static func qaDisplay(_ size: CGFloat, weight: Font.Weight = .regular) -> Font {
        .custom("Newsreader", size: size).weight(weight)
    }

    static func qaBody(_ size: CGFloat, weight: Font.Weight = .regular) -> Font {
        .custom("Work Sans", size: size).weight(weight)
    }

    static func qaMono(_ size: CGFloat, weight: Font.Weight = .regular) -> Font {
        .custom("JetBrains Mono", size: size).weight(weight)
    }
}

extension Color {
    init(hex: UInt32) {
        let red = Double((hex >> 16) & 0xFF) / 255.0
        let green = Double((hex >> 8) & 0xFF) / 255.0
        let blue = Double(hex & 0xFF) / 255.0
        self.init(red: red, green: green, blue: blue)
    }
}

struct QAScreen<Content: View>: View {
    @Environment(\.qaTokens) private var tokens
    let accessibilityIdentifier: String
    let content: Content

    init(accessibilityIdentifier: String, @ViewBuilder content: () -> Content) {
        self.accessibilityIdentifier = accessibilityIdentifier
        self.content = content()
    }

    var body: some View {
        ZStack {
            tokens.colors.background.ignoresSafeArea()
            content
            VStack(spacing: 0) {
                tokens.colors.background
                    .frame(height: 64)
                    .frame(maxWidth: .infinity)
                    .ignoresSafeArea(edges: .top)
                Spacer(minLength: 0)
            }
            .allowsHitTesting(false)
            .accessibilityHidden(true)
            Color.clear
                .frame(width: 1, height: 1)
                .accessibilityElement()
                .accessibilityLabel(accessibilityIdentifier)
                .accessibilityIdentifier(accessibilityIdentifier)
        }
    }
}

struct QACard<Content: View>: View {
    @Environment(\.qaTokens) private var tokens
    let content: Content

    init(@ViewBuilder content: () -> Content) {
        self.content = content()
    }

    var body: some View {
        content
            .padding(18)
            .background(tokens.colors.elevatedSurface)
            .clipShape(RoundedRectangle(cornerRadius: 18, style: .continuous))
            .overlay(
                RoundedRectangle(cornerRadius: 18, style: .continuous)
                    .stroke(tokens.colors.line, lineWidth: 1)
            )
    }
}

struct QAButton: View {
    @Environment(\.qaTokens) private var tokens
    let title: String
    let style: Style
    let accessibilityIdentifier: String?
    let isEnabled: Bool
    let action: () -> Void

    enum Style {
        case primary
        case secondary
        case quiet
    }

    init(
        title: String,
        style: Style,
        accessibilityIdentifier: String? = nil,
        isEnabled: Bool = true,
        action: @escaping () -> Void
    ) {
        self.title = title
        self.style = style
        self.accessibilityIdentifier = accessibilityIdentifier
        self.isEnabled = isEnabled
        self.action = action
    }

    var body: some View {
        Button {
            guard isEnabled else {
                return
            }
            action()
        } label: {
            Text(title)
                .font(.qaBody(15, weight: .semibold))
                .frame(maxWidth: .infinity)
                .padding(.vertical, 13)
                .foregroundStyle(foreground)
                .background(background)
                .clipShape(RoundedRectangle(cornerRadius: 14, style: .continuous))
                .overlay(
                    RoundedRectangle(cornerRadius: 14, style: .continuous)
                        .stroke(border, lineWidth: style == .quiet ? 0 : 1)
                )
                .opacity(isEnabled ? 1 : 0.48)
        }
        .buttonStyle(.plain)
        .accessibilityElement(children: .ignore)
        .accessibilityLabel(title)
        .accessibilityIdentifier(accessibilityIdentifier ?? title)
        .accessibilityValue(isEnabled ? "" : "Unavailable until setup is complete")
    }

    private var background: Color {
        switch style {
        case .primary:
            tokens.colors.accent
        case .secondary:
            tokens.colors.accentSoft
        case .quiet:
            Color.clear
        }
    }

    private var foreground: Color {
        switch style {
        case .primary:
            tokens.mode == .dark ? Color(hex: 0x21140E) : .white
        case .secondary, .quiet:
            tokens.colors.primaryText
        }
    }

    private var border: Color {
        style == .primary ? tokens.colors.accent : tokens.colors.lineStrong
    }
}

struct QATag: View {
    @Environment(\.qaTokens) private var tokens
    let text: String

    var body: some View {
        Text(text)
            .font(.qaBody(12, weight: .medium))
            .foregroundStyle(tokens.colors.mutedText)
            .padding(.horizontal, 10)
            .padding(.vertical, 6)
            .background(tokens.colors.background)
            .clipShape(Capsule())
            .overlay(Capsule().stroke(tokens.colors.line, lineWidth: 1))
    }
}
