import Foundation

struct QALocalLibraryState: Codable, Equatable {
    var userLinks: [QAContentItem]
    var userDocuments: [QAContentItem]

    static var defaults: QALocalLibraryState {
        QALocalLibraryState(
            userLinks: QASampleData.defaultUserLinks,
            userDocuments: QASampleData.defaultUserDocuments
        )
    }

    var libraryAdditions: [QAContentItem] {
        userLinks + userDocuments
    }
}

enum QALocalContentStore {
    private static let key = "qa.localLibraryState.v1"

    static func load(defaults: UserDefaults = .standard) -> QALocalLibraryState {
        guard
            let data = defaults.data(forKey: key),
            let state = try? JSONDecoder().decode(QALocalLibraryState.self, from: data)
        else {
            return .defaults
        }
        return state
    }

    static func save(_ state: QALocalLibraryState, defaults: UserDefaults = .standard) {
        guard let data = try? JSONEncoder().encode(state) else {
            return
        }
        defaults.set(data, forKey: key)
    }

    static func reset(defaults: UserDefaults = .standard) {
        defaults.removeObject(forKey: key)
    }

    static func makeSavedLink(title: String, url: String, durationMinutes: Int) -> QAContentItem {
        let normalizedTitle = title.trimmingCharacters(in: .whitespacesAndNewlines).nonEmpty ?? "Saved Link"
        let normalizedURL = url.trimmingCharacters(in: .whitespacesAndNewlines).nonEmpty
            ?? "https://example.com/quality-alternative-saved-link"
        return QAContentItem(
            id: "ios-saved-link-\(stableIDFragment(normalizedTitle + normalizedURL))",
            packId: "saved-links",
            title: normalizedTitle,
            description: "A private link-only item saved locally in the iOS simulator.",
            durationMinutes: durationMinutes,
            format: .html,
            topics: ["SAVED", "LINK"],
            bodyAssetPath: nil,
            externalURL: normalizedURL,
            source: "Saved link",
            sourceType: .userLink,
            rightsClass: .linkOnly,
            renderMode: .externalHandoff,
            whyThisNow: "Use when the replacement should stay external but still be finite.",
            sourceURL: normalizedURL
        )
    }

    static func makeDocument(title: String, format: QAContentFormat, durationMinutes: Int) -> QAContentItem {
        let normalizedTitle = title.trimmingCharacters(in: .whitespacesAndNewlines).nonEmpty ?? defaultTitle(for: format)
        let isPDF = format == .pdf
        return QAContentItem(
            id: "ios-private-\(format.rawValue.lowercased())-\(stableIDFragment(normalizedTitle))",
            packId: "private-files",
            title: normalizedTitle,
            description: description(for: format),
            durationMinutes: durationMinutes,
            format: format,
            topics: ["PRIVATE", format.rawValue],
            bodyAssetPath: nil,
            externalURL: isPDF ? bundledPDFURLString() : nil,
            source: isPDF ? "Private PDF" : "Private file",
            sourceType: .userDocument,
            rightsClass: .userPrivate,
            renderMode: isPDF ? .externalHandoff : .userPrivateReader,
            whyThisNow: isPDF
                ? "Use when the better action is opening a chosen file instead of a feed."
                : "Use when your own saved material is the better alternative."
        )
    }

    private static func defaultTitle(for format: QAContentFormat) -> String {
        switch format {
        case .markdown:
            "Private Markdown Note"
        case .epub:
            "Private EPUB Extract"
        case .pdf:
            "Private PDF Handoff"
        case .html:
            "Private Web Archive"
        }
    }

    private static func description(for format: QAContentFormat) -> String {
        switch format {
        case .markdown:
            "A local Markdown-style private file represented in the calm in-app reader."
        case .epub:
            "A simulator EPUB stand-in rendered through the same normalized reader blocks."
        case .pdf:
            "A PDF remains an intentional external document handoff, matching Android scope."
        case .html:
            "A private HTML-style document saved locally for simulator parity."
        }
    }

    private static func bundledPDFURLString() -> String {
        Bundle.main.url(forResource: "private-paper", withExtension: "pdf")?.absoluteString
            ?? "file:///simulator/private-paper.pdf"
    }

    private static func stableIDFragment(_ source: String) -> String {
        let allowed = CharacterSet.alphanumerics
        let slug = source
            .lowercased()
            .unicodeScalars
            .map { allowed.contains($0) ? Character($0) : "-" }
            .reduce(into: "") { $0.append($1) }
            .split(separator: "-")
            .prefix(6)
            .joined(separator: "-")
        return slug.isEmpty ? UUID().uuidString.lowercased() : slug
    }
}

struct QALocalSettings: Codable, Equatable {
    var meditationDurationMinutes: Int
    var contentPriority: QAContentPriority

    static let defaults = QALocalSettings(
        meditationDurationMinutes: 5,
        contentPriority: .balanced
    )
}

enum QALocalSettingsStore {
    private static let key = "qa.localSettings.v1"

    static func load(defaults: UserDefaults = .standard) -> QALocalSettings {
        guard
            let data = defaults.data(forKey: key),
            let settings = try? JSONDecoder().decode(QALocalSettings.self, from: data)
        else {
            return .defaults
        }
        return settings
    }

    static func save(_ settings: QALocalSettings, defaults: UserDefaults = .standard) {
        guard let data = try? JSONEncoder().encode(settings) else {
            return
        }
        defaults.set(data, forKey: key)
    }

    static func reset(defaults: UserDefaults = .standard) {
        defaults.removeObject(forKey: key)
    }
}

private extension String {
    var nonEmpty: String? {
        isEmpty ? nil : self
    }
}
