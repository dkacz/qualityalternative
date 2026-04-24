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
        externalURL: "file:///simulator/private-paper.pdf",
        source: "Private PDF",
        sourceType: .userDocument,
        rightsClass: .userPrivate,
        renderMode: .externalHandoff,
        whyThisNow: "Use when the better action is opening a chosen file instead of a feed."
    )

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
    static let userLinks: [QAContentItem] = [userLinkItem]
    static let userDocuments: [QAContentItem] = [markdownDocumentItem, epubDocumentItem, pdfDocumentItem]
    static let library: [QAContentItem] = editorialLibrary + userLinks + userDocuments + [meditationItem]

    static func replacementSession(meditationMinutes: Int = 5, priority: QAContentPriority = .balanced) -> QAReplacementSession {
        let meditation = meditationContentItem(minutes: meditationMinutes)
        let primary: QAContentItem
        let backups: [QAContentItem]
        switch priority {
        case .readings:
            primary = readerItem
            backups = [linkOnlyItem, meditation]
        case .myFiles:
            primary = epubDocumentItem
            backups = [markdownDocumentItem, meditation]
        case .savedLinks:
            primary = userLinkItem
            backups = [readerItem, meditation]
        case .meditation:
            primary = meditation
            backups = [readerItem, linkOnlyItem]
        case .balanced:
            primary = readerItem
            backups = [linkOnlyItem, meditation]
        }
        return QAReplacementSession(
            triggerLabel: "Protected selection",
            primary: primary,
            backups: backups
        )
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
        return QAEditorialCatalog.body(for: item)
    }
}
