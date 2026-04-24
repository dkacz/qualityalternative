import Foundation

enum QASampleData {
    static let readerItem = QAContentItem(
        id: "live-deliberately",
        title: "Live Deliberately",
        source: "Henry David Thoreau",
        duration: "4 min",
        description: "A compact public-domain reading about choosing attention before habit chooses for you.",
        topics: ["attention", "philosophy"],
        renderMode: .inAppReader,
        whyThisNow: "Good when the impulse is automatic and you need one clean thought before continuing."
    )

    static let linkOnlyItem = QAContentItem(
        id: "psyche-boredom",
        title: "How Boredom Opens the Mind",
        source: "Psyche",
        duration: "8 min",
        description: "A link-only modern essay surfaced as an intentional external handoff, not scraped or rehosted.",
        topics: ["psychology", "attention"],
        renderMode: .externalHandoff,
        whyThisNow: "Useful when scrolling is trying to erase a quiet moment."
    )

    static let meditationItem = QAContentItem(
        id: "meditation-3m",
        title: "Three-Minute Reset",
        source: "Quality Alternative",
        duration: "3 min",
        description: "A short timer for closing the loop without opening another feed.",
        topics: ["meditation", "reset"],
        renderMode: .meditationTimer,
        whyThisNow: "Best when reading would be too much and you only need to downshift."
    )

    static let library: [QAContentItem] = [
        readerItem,
        linkOnlyItem,
        meditationItem,
        QAContentItem(
            id: "naturalist-notices",
            title: "A Naturalist Notices Everything",
            source: "Charles Darwin",
            duration: "5 min",
            description: "A renderable public-domain excerpt that rewards careful observation.",
            topics: ["science", "nature"],
            renderMode: .inAppReader,
            whyThisNow: "Use it when your attention wants novelty but can still handle depth."
        )
    ]

    static let session = QAReplacementSession(
        triggerLabel: "Instagram",
        primary: readerItem,
        backups: [linkOnlyItem, meditationItem]
    )

    static let progress = QAProgressSnapshot(
        streakDays: 4,
        replacementsCompleted: 18,
        minutesRecovered: 73
    )
}
