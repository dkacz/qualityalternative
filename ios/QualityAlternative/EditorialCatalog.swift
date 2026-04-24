import Foundation

enum QAEditorialCatalog {
    static func loadPacks(bundle: Bundle = .main) -> [QAEditorialPack] {
        guard
            let url = QAResourceLookup.url(forPath: "editorial/starter_packs.json", bundle: bundle),
            let data = try? Data(contentsOf: url),
            let payload = try? JSONDecoder().decode(StarterPackPayload.self, from: data)
        else {
            return fallbackPacks
        }

        return payload.packs.map { pack in
            QAEditorialPack(
                id: pack.id,
                title: pack.title,
                description: pack.description,
                items: pack.items.map { item in
                    item.contentItem(packId: pack.id)
                }
            )
        }
    }

    static func body(for item: QAContentItem, bundle: Bundle = .main) -> String {
        guard let path = item.bodyAssetPath else {
            return item.description
        }
        guard let url = QAResourceLookup.url(forPath: path, bundle: bundle) else {
            return item.description
        }
        return (try? String(contentsOf: url, encoding: .utf8))?.trimmingCharacters(in: .whitespacesAndNewlines).nonEmpty
            ?? item.description
    }

    static var fallbackPacks: [QAEditorialPack] {
        [
            QAEditorialPack(
                id: "fallback",
                title: "Fallback Starter Pack",
                description: "Bundled fallback content used only when editorial assets are unavailable.",
                items: [QASampleFallback.readerItem, QASampleFallback.linkOnlyItem]
            )
        ]
    }
}

private enum QAResourceLookup {
    static func url(forPath path: String, bundle: Bundle) -> URL? {
        let resourceRoot = bundle.resourceURL
        if let directURL = resourceRoot?.appendingPathComponent(path), FileManager.default.fileExists(atPath: directURL.path) {
            return directURL
        }

        let nsPath = path as NSString
        let basename = (nsPath.lastPathComponent as NSString).deletingPathExtension
        let ext = nsPath.pathExtension
        if let url = bundle.url(forResource: basename, withExtension: ext) {
            return url
        }

        guard let resourceRoot else {
            return nil
        }
        let targetSuffix = "/" + path
        guard let enumerator = FileManager.default.enumerator(at: resourceRoot, includingPropertiesForKeys: nil) else {
            return nil
        }
        for case let url as URL in enumerator where url.path.hasSuffix(targetSuffix) {
            return url
        }
        return nil
    }
}

private struct StarterPackPayload: Codable {
    let packs: [StarterPack]
}

private struct StarterPack: Codable {
    let id: String
    let title: String
    let description: String
    let items: [StarterPackItem]
}

private struct StarterPackItem: Codable {
    let id: String
    let title: String
    let description: String
    let durationMinutes: Int
    let format: QAContentFormat
    let source: String
    let rightsClass: QAContentRightsClass
    let renderMode: QARenderMode
    let topics: [String]
    let whyThisNow: String
    let bodyAssetPath: String?
    let externalURL: String?
    let attribution: String?
    let sourceURL: String?
    let licenseName: String?
    let licenseURL: String?
    let rightsReviewedAt: String?

    enum CodingKeys: String, CodingKey {
        case id
        case title
        case description
        case durationMinutes
        case format
        case source
        case rightsClass
        case renderMode
        case topics
        case whyThisNow
        case bodyAssetPath
        case externalURL = "externalUrl"
        case attribution
        case sourceURL = "sourceUrl"
        case licenseName
        case licenseURL = "licenseUrl"
        case rightsReviewedAt
    }

    func contentItem(packId: String) -> QAContentItem {
        QAContentItem(
            id: id,
            packId: packId,
            title: title,
            description: description,
            durationMinutes: durationMinutes,
            format: format,
            topics: topics,
            bodyAssetPath: bodyAssetPath,
            externalURL: externalURL,
            source: source,
            sourceType: .editorial,
            rightsClass: rightsClass,
            renderMode: renderMode,
            whyThisNow: whyThisNow,
            attribution: attribution,
            sourceURL: sourceURL,
            licenseName: licenseName,
            licenseURL: licenseURL,
            rightsReviewedAt: rightsReviewedAt
        )
    }
}

private enum QASampleFallback {
    static let readerItem = QAContentItem(
        id: "live-deliberately",
        packId: "fallback",
        title: "Live Deliberately",
        description: "A compact public-domain reading about choosing attention before habit chooses for you.",
        durationMinutes: 4,
        format: .markdown,
        topics: ["PHILOSOPHY"],
        bodyAssetPath: nil,
        externalURL: nil,
        source: "Henry David Thoreau",
        sourceType: .editorial,
        rightsClass: .renderable,
        renderMode: .inAppReader,
        whyThisNow: "Good when the impulse is automatic and you need one clean thought before continuing.",
        attribution: "Henry David Thoreau, Walden",
        licenseName: "Public domain fallback fixture"
    )

    static let linkOnlyItem = QAContentItem(
        id: "psyche-boredom",
        packId: "fallback",
        title: "How Boredom Opens the Mind",
        description: "A link-only modern essay surfaced as an intentional external handoff.",
        durationMinutes: 8,
        format: .html,
        topics: ["PSYCHOLOGY"],
        bodyAssetPath: nil,
        externalURL: "https://psyche.co/",
        source: "Psyche",
        sourceType: .userLink,
        rightsClass: .linkOnly,
        renderMode: .externalHandoff,
        whyThisNow: "Useful when scrolling is trying to erase a quiet moment.",
        sourceURL: "https://psyche.co/"
    )
}

private extension String {
    var nonEmpty: String? {
        isEmpty ? nil : self
    }
}
