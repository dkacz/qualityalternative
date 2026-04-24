import FamilyControls
import Foundation
import ManagedSettings
import OSLog

enum QAAppGroup {
    static let identifier = "group.com.qualityalternative.ios"
    private static let logger = Logger(subsystem: "com.qualityalternative.ios", category: "AppGroup")

    static var userDefaults: UserDefaults? {
        guard let userDefaults = UserDefaults(suiteName: identifier) else {
            logger.error("App Group storage unavailable for \(identifier, privacy: .public)")
            return nil
        }
        return userDefaults
    }
}

private let shieldStateLogger = Logger(subsystem: "com.qualityalternative.ios", category: "ShieldState")

enum QAShieldActionMode: String, Codable, Equatable {
    case inactive
    case armed
    case paused
    case openAnyway
}

struct QAShieldSessionState: Codable, Equatable, Identifiable {
    let id: String
    let triggerContextID: String
    let primaryContentID: String
    let backupContentIDs: [String]
    let selection: QAScreenTimeSelectionSummary
    let actionMode: QAShieldActionMode
    let pauseExpiresAt: Date?
    let updatedAt: Date

    static func armed(
        session: QAReplacementSession,
        selection: QAScreenTimeSelectionSummary,
        now: Date
    ) -> QAShieldSessionState {
        QAShieldSessionState(
            id: "shield-current",
            triggerContextID: "screen-time-selection",
            primaryContentID: session.primary.id,
            backupContentIDs: session.backups.map(\.id),
            selection: selection,
            actionMode: .armed,
            pauseExpiresAt: nil,
            updatedAt: now
        )
    }

    func paused(until pauseExpiresAt: Date, now: Date) -> QAShieldSessionState {
        QAShieldSessionState(
            id: id,
            triggerContextID: triggerContextID,
            primaryContentID: primaryContentID,
            backupContentIDs: backupContentIDs,
            selection: selection,
            actionMode: .paused,
            pauseExpiresAt: pauseExpiresAt,
            updatedAt: now
        )
    }

    func openAnyway(now: Date) -> QAShieldSessionState {
        QAShieldSessionState(
            id: id,
            triggerContextID: triggerContextID,
            primaryContentID: primaryContentID,
            backupContentIDs: backupContentIDs,
            selection: selection,
            actionMode: .openAnyway,
            pauseExpiresAt: nil,
            updatedAt: now
        )
    }

    var containsOnlyTokenSafeMetadata: Bool {
        triggerContextID == "screen-time-selection" && selection.totalCount >= 0
    }

    func isPauseActive(now: Date) -> Bool {
        guard actionMode == .paused, let pauseExpiresAt else {
            return false
        }
        return pauseExpiresAt > now
    }

    func needsManualReapply(now: Date) -> Bool {
        guard actionMode == .paused, let pauseExpiresAt else {
            return false
        }
        return pauseExpiresAt <= now
    }
}

struct QAShieldControlSnapshot: Equatable {
    let setup: QAScreenTimeSetupSnapshot
    let session: QAShieldSessionState?
    let now: Date

    var canApplyShieldRules: Bool {
        setup.canPrepareShielding
    }

    var canPauseShieldRules: Bool {
        session?.actionMode == .armed
    }

    var canResumeShieldRules: Bool {
        guard let session else {
            return false
        }
        return session.actionMode == .paused && setup.canPrepareShielding
    }

    var canClearShieldRules: Bool {
        session != nil
    }

    var statusTitle: String {
        guard let session else {
            return "No shield session"
        }
        if session.needsManualReapply(now: now) {
            return "Pause expired"
        }
        switch session.actionMode {
        case .inactive:
            return "Shield inactive"
        case .armed:
            return "Shield rules prepared"
        case .paused:
            return "Shield paused"
        case .openAnyway:
            return "Open once allowed"
        }
    }

    var detailText: String {
        guard let session else {
            return "Apply shield rules after Screen Time access and protected selection are ready."
        }
        if session.needsManualReapply(now: now) {
            return "The host app must reapply shield rules after an expired pause; simulator state is not device enforcement proof."
        }
        switch session.actionMode {
        case .inactive:
            return "No active ManagedSettings shield rules are expected."
        case .armed:
            return "\(session.selection.totalCount) opaque protected tokens are ready for ManagedSettings shielding."
        case .paused:
            return "ManagedSettings rules are cleared until the pause expires; later slices need extension/device validation."
        case .openAnyway:
            return "The next open-anyway attempt should clear rules once, then return to a protected state later."
        }
    }
}

protocol QAShieldRuleApplying {
    func apply(selection: FamilyActivitySelection)
    func clear()
}

struct QAManagedSettingsShieldApplier: QAShieldRuleApplying {
    private let store = ManagedSettingsStore(named: ManagedSettingsStore.Name("quality-alternative-shield"))

    func apply(selection: FamilyActivitySelection) {
        store.shield.applications = selection.applicationTokens.isEmpty ? nil : selection.applicationTokens
        store.shield.applicationCategories = selection.categoryTokens.isEmpty ? nil : .specific(selection.categoryTokens)
        store.shield.webDomains = selection.webDomainTokens.isEmpty ? nil : selection.webDomainTokens
    }

    func clear() {
        store.clearAllSettings()
    }
}

enum QAShieldSessionStore {
    private static let key = "qa.shieldSession.v1"

    static func load(userDefaults: UserDefaults? = QAAppGroup.userDefaults) -> QAShieldSessionState? {
        guard let userDefaults else {
            return nil
        }
        guard let data = userDefaults.data(forKey: key) else {
            return nil
        }
        return try? JSONDecoder().decode(QAShieldSessionState.self, from: data)
    }

    static func save(_ state: QAShieldSessionState, userDefaults: UserDefaults? = QAAppGroup.userDefaults) {
        guard let userDefaults else {
            shieldStateLogger.error("Refusing to write shield state because App Group storage is unavailable.")
            return
        }
        guard let data = try? JSONEncoder().encode(state) else {
            return
        }
        userDefaults.set(data, forKey: key)
    }

    static func clear(userDefaults: UserDefaults? = QAAppGroup.userDefaults) {
        guard let userDefaults else {
            return
        }
        userDefaults.removeObject(forKey: key)
    }
}

enum QAFamilyActivitySelectionStore {
    private static let key = "qa.familyActivitySelection.v1"

    static func load(userDefaults: UserDefaults? = QAAppGroup.userDefaults) -> FamilyActivitySelection {
        guard let userDefaults else {
            return FamilyActivitySelection()
        }
        guard let data = userDefaults.data(forKey: key) else {
            return FamilyActivitySelection()
        }
        return (try? JSONDecoder().decode(FamilyActivitySelection.self, from: data)) ?? FamilyActivitySelection()
    }

    static func save(_ selection: FamilyActivitySelection, userDefaults: UserDefaults? = QAAppGroup.userDefaults) {
        guard let userDefaults else {
            shieldStateLogger.error("Refusing to write protected selection because App Group storage is unavailable.")
            return
        }
        guard let data = try? JSONEncoder().encode(selection) else {
            return
        }
        userDefaults.set(data, forKey: key)
    }
}

extension QAScreenTimeSelectionSummary {
    init(selection: FamilyActivitySelection) {
        self.init(
            applicationCount: selection.applicationTokens.count,
            categoryCount: selection.categoryTokens.count,
            webDomainCount: selection.webDomainTokens.count
        )
    }
}
