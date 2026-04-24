import Foundation

enum QASampleData {
    static let packs: [QAEditorialPack] = QAEditorialCatalog.loadPacks()
    static let editorialLibrary: [QAContentItem] = packs.flatMap(\.items)

    static let userLinkItem = QAContentItem(
        id: "ios-saved-link-deep-work",
        packId: "saved-links",
        title: "A Saved Link for Later",
        description: "A private link-only item used to prove the iOS saved-link handoff surface without scraping or rehosting.",
        durationMinutes: 8,
        format: .html,
        topics: ["PSYCHOLOGY", "TECH"],
        bodyAssetPath: nil,
        externalURL: "https://example.com/quality-alternative-saved-link",
        source: "Saved link",
        sourceType: .userLink,
        rightsClass: .linkOnly,
        renderMode: .externalHandoff,
        whyThisNow: "Use when the replacement should stay external but still be finite."
    )

    static let markdownDocumentItem = QAContentItem(
        id: "ios-private-markdown",
        packId: "private-files",
        title: "Private Markdown Note",
        description: "A local Markdown-style private file represented in the calm in-app reader.",
        durationMinutes: 10,
        format: .markdown,
        topics: ["ESSAYS", "CREATIVITY"],
        bodyAssetPath: nil,
        externalURL: nil,
        source: "Private file",
        sourceType: .userDocument,
        rightsClass: .userPrivate,
        renderMode: .userPrivateReader,
        whyThisNow: "Use when your own saved material is the better alternative."
    )

    static let epubDocumentItem = QAContentItem(
        id: "ios-private-epub",
        packId: "private-files",
        title: "Private EPUB Extract",
        description: "A simulator EPUB stand-in that proves iOS can render imported-book shaped content without network access.",
        durationMinutes: 20,
        format: .epub,
        topics: ["PHILOSOPHY", "ESSAYS"],
        bodyAssetPath: nil,
        externalURL: nil,
        source: "Private EPUB",
        sourceType: .userDocument,
        rightsClass: .userPrivate,
        renderMode: .userPrivateReader,
        whyThisNow: "Use when a longer saved book is preferable to a feed."
    )

    static let pdfDocumentItem = QAContentItem(
        id: "ios-private-pdf",
        packId: "private-files",
        title: "Private PDF Handoff",
        description: "A PDF remains an intentional external document handoff, matching Android's release scope.",
        durationMinutes: 15,
        format: .pdf,
        topics: ["SCIENCE"],
        bodyAssetPath: nil,
        externalURL: bundledPDFURLString(),
        source: "Private PDF",
        sourceType: .userDocument,
        rightsClass: .userPrivate,
        renderMode: .externalHandoff,
        whyThisNow: "Use when the better action is opening a chosen file instead of a feed."
    )

    private static func bundledPDFURLString() -> String {
        Bundle.main.url(forResource: "private-paper", withExtension: "pdf")?.absoluteString
            ?? "file:///simulator/private-paper.pdf"
    }

    static func meditationContentItem(minutes: Int = 5) -> QAContentItem {
        QAContentItem(
            id: "meditation-\(minutes)m",
            packId: "utilities",
            title: "\(minutes)-Minute Reset",
            description: "A short timer for closing the loop without opening another feed.",
            durationMinutes: minutes,
            format: .markdown,
            topics: ["MEDITATION", "RESET"],
            bodyAssetPath: nil,
            externalURL: nil,
            source: "Quality Alternative",
            sourceType: .meditation,
            rightsClass: .appUtility,
            renderMode: .meditationTimer,
            whyThisNow: "Best when reading would be too much and you only need to downshift."
        )
    }

    static let readerItem: QAContentItem = editorialLibrary.first(where: { $0.renderMode == .inAppReader })
        ?? QAEditorialCatalog.fallbackPacks.flatMap(\.items).first!
    static let linkOnlyItem: QAContentItem = editorialLibrary.first(where: { $0.renderMode == .externalHandoff })
        ?? userLinkItem
    static let meditationItem: QAContentItem = meditationContentItem()
    static let defaultUserLinks: [QAContentItem] = [userLinkItem]
    static let defaultUserDocuments: [QAContentItem] = [markdownDocumentItem, epubDocumentItem, pdfDocumentItem]
    static let userLinks: [QAContentItem] = defaultUserLinks
    static let userDocuments: [QAContentItem] = defaultUserDocuments
    static let library: [QAContentItem] = editorialLibrary + defaultUserLinks + defaultUserDocuments + [meditationItem]

    static var readerItems: [QAContentItem] {
        editorialLibrary.filter { $0.renderMode == .inAppReader }.sorted { lhs, rhs in
            if lhs.durationMinutes == rhs.durationMinutes {
                return lhs.title < rhs.title
            }
            return lhs.durationMinutes < rhs.durationMinutes
        }
    }

    static var longReaderItem: QAContentItem {
        readerItems.last ?? readerItem
    }

    static func replacementSession(
        meditationMinutes: Int = 5,
        priority: QAContentPriority = .balanced,
        userLinks: [QAContentItem] = defaultUserLinks,
        userDocuments: [QAContentItem] = defaultUserDocuments
    ) -> QAReplacementSession {
        let meditation = meditationContentItem(minutes: meditationMinutes)
        let primary: QAContentItem
        let candidates: [QAContentItem]
        switch priority {
        case .readings:
            primary = longReaderItem
            candidates = readerItems.filter { $0.id != primary.id } + [meditation]
        case .myFiles:
            primary = userDocuments.first(where: { $0.format == .epub }) ?? epubDocumentItem
            candidates = userDocuments.filter { $0.id != primary.id } + [meditation] + readerItems
        case .savedLinks:
            primary = userLinks.first ?? userLinkItem
            candidates = [meditation] + readerItems + userLinks.filter { $0.id != primary.id }
        case .meditation:
            primary = meditation
            candidates = readerItems + userLinks + userDocuments
        case .balanced:
            primary = linkOnlyItem
            candidates = [meditation] + readerItems + userLinks + userDocuments
        }
        return QAReplacementSession(
            triggerLabel: "Protected selection",
            primary: primary,
            backups: lowerCommitmentBackups(for: primary, candidates: candidates, meditationMinutes: meditationMinutes)
        )
    }

    static func lowerCommitmentBackups(
        for primary: QAContentItem,
        candidates: [QAContentItem],
        meditationMinutes: Int = 5
    ) -> [QAContentItem] {
        let direct = candidates
            .filter { $0.id != primary.id && $0.durationMinutes <= primary.durationMinutes }
        let fill = readerItems
            .filter { $0.id != primary.id && $0.durationMinutes <= primary.durationMinutes }
        let cappedMeditation = meditationContentItem(minutes: min(meditationMinutes, primary.durationMinutes))
        let combined = direct + fill + [cappedMeditation]
        var seen: Set<String> = []
        return combined.filter { item in
            guard item.id != primary.id else {
                return false
            }
            guard !seen.contains(item.id) else {
                return false
            }
            seen.insert(item.id)
            return item.durationMinutes <= primary.durationMinutes
        }
        .prefix(2)
        .map { $0 }
    }

    static func item(withID id: String, localLibrary: QALocalLibraryState = .defaults) -> QAContentItem? {
        (editorialLibrary + localLibrary.libraryAdditions + [meditationItem]).first { $0.id == id }
    }

    static let session: QAReplacementSession = replacementSession()

    static let progress = QAProgressSnapshot(
        streakDays: 4,
        replacementsCompleted: 18,
        minutesRecovered: 73
    )

    static func body(for item: QAContentItem) -> String {
        if item.id == markdownDocumentItem.id {
            return """
            # Private Markdown Note

            This simulator note proves the same reader surface used for Android Markdown imports.

            - Keep the body local.
            - Preserve list formatting.
            - Make completion finite.

            > The point is not a bigger library. The point is one better thing at the impulse moment.
            """
        }
        if item.id == epubDocumentItem.id {
            return """
            # Private EPUB Extract

            This is a local EPUB-shaped simulator fixture. Android extracts EPUB text into reader markdown; iOS renders the same normalized block types here without claiming a real file picker/import pipeline yet.

            ## Reading Contract

            The native reader keeps headings, paragraphs, quotes, and lists readable. PDF remains an external handoff; Markdown and EPUB-shaped content stay renderable.
            """
        }
        if item.sourceType == .userDocument, item.renderMode == .userPrivateReader {
            return """
            # \(item.title)

            This is a simulator-local \(item.format.rawValue) item rendered through the same native reader as editorial Markdown.

            - It stays private to local state.
            - It preserves finite replacement behavior.
            - It avoids scraping, rehosting, or web retrieval.
            """
        }
        return QAEditorialCatalog.body(for: item)
    }
}
