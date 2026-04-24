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

struct QAReplacementSession: Equatable {
    let triggerLabel: String
    let primary: QAContentItem
    let backups: [QAContentItem]
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
