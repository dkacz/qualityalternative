import Foundation

enum QARenderMode: String, CaseIterable {
    case inAppReader
    case externalHandoff
    case meditationTimer
}

struct QAContentItem: Identifiable, Equatable {
    let id: String
    let title: String
    let source: String
    let duration: String
    let description: String
    let topics: [String]
    let renderMode: QARenderMode
    let whyThisNow: String
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

enum QARoute: String {
    case home
    case library
    case intervention
    case reader
    case handoff
    case meditation
    case progress
    case settings
}
