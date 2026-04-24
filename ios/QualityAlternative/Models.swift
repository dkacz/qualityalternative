import Foundation

enum QAContentFormat: String, CaseIterable, Codable {
    case markdown = "MARKDOWN"
    case html = "HTML"
    case pdf = "PDF"
    case epub = "EPUB"
}

enum QAContentSourceType: String, CaseIterable, Codable {
    case editorial = "EDITORIAL"
    case userLink = "USER_LINK"
    case userDocument = "USER_DOCUMENT"
    case meditation = "MEDITATION"
}

enum QAContentRightsClass: String, CaseIterable, Codable {
    case renderable = "RENDERABLE"
    case linkOnly = "LINK_ONLY"
    case userPrivate = "USER_PRIVATE"
    case appUtility = "APP_UTILITY"
}

enum QARenderMode: String, CaseIterable, Codable {
    case inAppReader = "IN_APP_READER"
    case externalHandoff = "EXTERNAL_HANDOFF"
    case userPrivateReader = "USER_PRIVATE_READER"
    case meditationTimer = "MEDITATION_TIMER"
}

struct QAContentItem: Identifiable, Equatable, Codable {
    let id: String
    let packId: String
    let title: String
    let description: String
    let durationMinutes: Int
    let format: QAContentFormat
    let topics: [String]
    let bodyAssetPath: String?
    let externalURL: String?
    let source: String
    let sourceType: QAContentSourceType
    let rightsClass: QAContentRightsClass
    let renderMode: QARenderMode
    let whyThisNow: String
    let attribution: String?
    let sourceURL: String?
    let licenseName: String?
    let licenseURL: String?
    let rightsReviewedAt: String?

    var duration: String {
        "\(durationMinutes) min"
    }

    var topicLine: String {
        topics.joined(separator: " / ")
    }

    var isRenderable: Bool {
        renderMode == .inAppReader || renderMode == .userPrivateReader
    }

    init(
        id: String,
        packId: String,
        title: String,
        description: String,
        durationMinutes: Int,
        format: QAContentFormat,
        topics: [String],
        bodyAssetPath: String?,
        externalURL: String?,
        source: String,
        sourceType: QAContentSourceType,
        rightsClass: QAContentRightsClass,
        renderMode: QARenderMode,
        whyThisNow: String,
        attribution: String? = nil,
        sourceURL: String? = nil,
        licenseName: String? = nil,
        licenseURL: String? = nil,
        rightsReviewedAt: String? = nil
    ) {
        self.id = id
        self.packId = packId
        self.title = title
        self.description = description
        self.durationMinutes = durationMinutes
        self.format = format
        self.topics = topics
        self.bodyAssetPath = bodyAssetPath
        self.externalURL = externalURL
        self.source = source
        self.sourceType = sourceType
        self.rightsClass = rightsClass
        self.renderMode = renderMode
        self.whyThisNow = whyThisNow
        self.attribution = attribution
        self.sourceURL = sourceURL
        self.licenseName = licenseName
        self.licenseURL = licenseURL
        self.rightsReviewedAt = rightsReviewedAt
    }

    enum CodingKeys: String, CodingKey {
        case id
        case packId
        case title
        case description
        case durationMinutes
        case format
        case topics
        case bodyAssetPath
        case externalURL = "externalUrl"
        case source
        case sourceType
        case rightsClass
        case renderMode
        case whyThisNow
        case attribution
        case sourceURL = "sourceUrl"
        case licenseName
        case licenseURL = "licenseUrl"
        case rightsReviewedAt
    }
}

struct QAEditorialPack: Identifiable, Equatable, Codable {
    let id: String
    let title: String
    let description: String
    let items: [QAContentItem]
}

enum QAContentPriority: String, CaseIterable, Equatable, Codable {
    case balanced
    case readings
    case myFiles
    case savedLinks
    case meditation

    var label: String {
        switch self {
        case .balanced:
            "Balanced"
        case .readings:
            "Readings"
        case .myFiles:
            "My files"
        case .savedLinks:
            "Saved links"
        case .meditation:
            "Meditation"
        }
    }

    var description: String {
        switch self {
        case .balanced:
            "Mix curated readings, your files, saved links, and utility resets."
        case .readings:
            "Prefer curated reader pieces when they fit."
        case .myFiles:
            "Prefer imported Markdown, EPUB, or PDF handoffs."
        case .savedLinks:
            "Prefer saved link-only handoffs."
        case .meditation:
            "Prefer short utility resets when reading is too much."
        }
    }
}

struct QAProgressSnapshot: Equatable {
    let streakDays: Int
    let replacementsCompleted: Int
    let minutesRecovered: Int
}

enum QAScreenTimeAuthorizationState: String, Equatable {
    case notDetermined
    case denied
    case approved
}

struct QAScreenTimeSelectionSummary: Codable, Equatable {
    let applicationCount: Int
    let categoryCount: Int
    let webDomainCount: Int

    var totalCount: Int {
        applicationCount + categoryCount + webDomainCount
    }

    var hasProtectedTargets: Bool {
        totalCount > 0
    }
}

struct QAScreenTimeSetupSnapshot: Equatable {
    let authorization: QAScreenTimeAuthorizationState
    let selection: QAScreenTimeSelectionSummary

    var canPrepareShielding: Bool {
        authorization == .approved && selection.hasProtectedTargets
    }
}

struct QAReplacementSession: Equatable {
    let triggerLabel: String
    let primary: QAContentItem
    let backups: [QAContentItem]

    init(triggerLabel: String, primary: QAContentItem, backups: [QAContentItem]) {
        self.triggerLabel = triggerLabel
        self.primary = primary
        self.backups = Array(backups.prefix(2))
    }
}

struct QASimulatorDelayState: Codable, Equatable {
    let startedAt: Date
    let expiresAt: Date
    let selectedContentID: String

    var remainingMinutesText: String {
        let remaining = max(0, Int(ceil(expiresAt.timeIntervalSince(Date()) / 60)))
        return "\(remaining) min left"
    }

    func isActive(now: Date = Date()) -> Bool {
        expiresAt > now
    }
}

enum QARoute: String {
    case home
    case library
    case addLink
    case addDocument
    case intervention
    case reader
    case handoff
    case meditation
    case feedback
    case progress
    case settings
}
