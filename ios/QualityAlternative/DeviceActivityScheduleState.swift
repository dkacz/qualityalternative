import DeviceActivity
import FamilyControls
import Foundation
import OSLog

private let deviceActivityScheduleLogger = Logger(subsystem: "com.qualityalternative.ios", category: "DeviceActivitySchedule")

enum QADeviceActivityScheduleMode: String, Codable, Equatable {
    case inactive
    case scheduled
    case stopped
    case failed
}

enum QADeviceActivityMonitorEventKind: String, Codable, Equatable {
    case intervalStarted
    case intervalEnded
    case thresholdReached
}

enum QADeviceActivityMonitorAction: Equatable {
    case applyShield
    case clearShield
    case keepShield
}

struct QADeviceActivityMonitorEventRecord: Codable, Equatable {
    let kind: QADeviceActivityMonitorEventKind
    let activityName: String
    let eventName: String?
    let createdAt: Date

    var containsOnlyTokenSafeMetadata: Bool {
        activityName == QADeviceActivityNames.protectedWindow.rawValue
    }
}

struct QADeviceActivityScheduleState: Codable, Equatable, Identifiable {
    let id: String
    let mode: QADeviceActivityScheduleMode
    let selection: QAScreenTimeSelectionSummary
    let intervalStartHour: Int
    let intervalStartMinute: Int
    let intervalEndHour: Int
    let intervalEndMinute: Int
    let repeats: Bool
    let updatedAt: Date
    let lastErrorDescription: String?
    let lastEvent: QADeviceActivityMonitorEventRecord?

    static func scheduled(selection: QAScreenTimeSelectionSummary, now: Date) -> QADeviceActivityScheduleState {
        QADeviceActivityScheduleState(
            id: QADeviceActivityNames.protectedWindow.rawValue,
            mode: .scheduled,
            selection: selection,
            intervalStartHour: 0,
            intervalStartMinute: 0,
            intervalEndHour: 23,
            intervalEndMinute: 59,
            repeats: true,
            updatedAt: now,
            lastErrorDescription: nil,
            lastEvent: nil
        )
    }

    static func stopped(previous: QADeviceActivityScheduleState?, now: Date) -> QADeviceActivityScheduleState {
        QADeviceActivityScheduleState(
            id: QADeviceActivityNames.protectedWindow.rawValue,
            mode: .stopped,
            selection: previous?.selection ?? .empty,
            intervalStartHour: previous?.intervalStartHour ?? 0,
            intervalStartMinute: previous?.intervalStartMinute ?? 0,
            intervalEndHour: previous?.intervalEndHour ?? 23,
            intervalEndMinute: previous?.intervalEndMinute ?? 59,
            repeats: previous?.repeats ?? true,
            updatedAt: now,
            lastErrorDescription: nil,
            lastEvent: previous?.lastEvent
        )
    }

    static func failed(selection: QAScreenTimeSelectionSummary, error: Error, now: Date) -> QADeviceActivityScheduleState {
        QADeviceActivityScheduleState(
            id: QADeviceActivityNames.protectedWindow.rawValue,
            mode: .failed,
            selection: selection,
            intervalStartHour: 0,
            intervalStartMinute: 0,
            intervalEndHour: 23,
            intervalEndMinute: 59,
            repeats: true,
            updatedAt: now,
            lastErrorDescription: error.localizedDescription,
            lastEvent: nil
        )
    }

    func recording(_ event: QADeviceActivityMonitorEventRecord) -> QADeviceActivityScheduleState {
        QADeviceActivityScheduleState(
            id: id,
            mode: mode,
            selection: selection,
            intervalStartHour: intervalStartHour,
            intervalStartMinute: intervalStartMinute,
            intervalEndHour: intervalEndHour,
            intervalEndMinute: intervalEndMinute,
            repeats: repeats,
            updatedAt: event.createdAt,
            lastErrorDescription: lastErrorDescription,
            lastEvent: event
        )
    }

    var containsOnlyTokenSafeMetadata: Bool {
        id == QADeviceActivityNames.protectedWindow.rawValue
            && selection.totalCount >= 0
            && (lastEvent?.containsOnlyTokenSafeMetadata ?? true)
    }
}

struct QADeviceActivityScheduleSnapshot: Equatable {
    let setup: QAScreenTimeSetupSnapshot
    let state: QADeviceActivityScheduleState?

    var canStartMonitoring: Bool {
        setup.canPrepareShielding
    }

    var canStopMonitoring: Bool {
        state?.mode == .scheduled
    }

    var statusTitle: String {
        switch state?.mode {
        case .scheduled:
            return "Monitor scheduled"
        case .stopped:
            return "Monitor stopped"
        case .failed:
            return "Monitor failed"
        case .inactive, nil:
            return "No monitor schedule"
        }
    }

    var detailText: String {
        guard let state else {
            return "Start the DeviceActivity schedule after Screen Time access and protected selection are ready."
        }
        switch state.mode {
        case .scheduled:
            return "\(state.selection.totalCount) opaque protected tokens are monitored during the daily protected window."
        case .stopped:
            return "The DeviceActivity schedule is stopped; existing shield rules can still be managed by host controls."
        case .failed:
            return state.lastErrorDescription ?? "DeviceActivity could not start monitoring on this environment."
        case .inactive:
            return "No active DeviceActivity monitoring is expected."
        }
    }
}

enum QADeviceActivityNames {
    static var protectedWindow: DeviceActivityName {
        DeviceActivityName("qa.protected-window")
    }

    static var firstMinute: DeviceActivityEvent.Name {
        DeviceActivityEvent.Name("qa.first-minute")
    }
}

struct QADeviceActivitySchedulePlan {
    let activityName: DeviceActivityName
    let schedule: DeviceActivitySchedule
    let events: [DeviceActivityEvent.Name: DeviceActivityEvent]

    static func protectedWindow(selection: FamilyActivitySelection) -> QADeviceActivitySchedulePlan? {
        let summary = QAScreenTimeSelectionSummary(selection: selection)
        guard summary.hasProtectedTargets else {
            return nil
        }
        let event = DeviceActivityEvent(
            applications: selection.applicationTokens,
            categories: selection.categoryTokens,
            webDomains: selection.webDomainTokens,
            threshold: DateComponents(minute: 1)
        )
        return QADeviceActivitySchedulePlan(
            activityName: QADeviceActivityNames.protectedWindow,
            schedule: DeviceActivitySchedule(
                intervalStart: DateComponents(hour: 0, minute: 0),
                intervalEnd: DateComponents(hour: 23, minute: 59),
                repeats: true
            ),
            events: [QADeviceActivityNames.firstMinute: event]
        )
    }
}

enum QADeviceActivitySchedulingError: LocalizedError, Equatable {
    case notReady
    case emptySelection

    var errorDescription: String? {
        switch self {
        case .notReady:
            return "Screen Time access and protected selection are required before monitoring can start."
        case .emptySelection:
            return "DeviceActivity monitoring needs at least one opaque protected token."
        }
    }
}

struct QADeviceActivityScheduler {
    private let center: DeviceActivityCenter

    init(center: DeviceActivityCenter = DeviceActivityCenter()) {
        self.center = center
    }

    func startProtectedWindow(
        selection: FamilyActivitySelection,
        setup: QAScreenTimeSetupSnapshot,
        now: Date = Date()
    ) throws -> QADeviceActivityScheduleState {
        guard setup.canPrepareShielding else {
            throw QADeviceActivitySchedulingError.notReady
        }
        guard let plan = QADeviceActivitySchedulePlan.protectedWindow(selection: selection) else {
            throw QADeviceActivitySchedulingError.emptySelection
        }
        try center.startMonitoring(plan.activityName, during: plan.schedule, events: plan.events)
        let state = QADeviceActivityScheduleState.scheduled(selection: setup.selection, now: now)
        QADeviceActivityScheduleStore.save(state)
        return state
    }

    func stopProtectedWindow(now: Date = Date()) -> QADeviceActivityScheduleState {
        center.stopMonitoring([QADeviceActivityNames.protectedWindow])
        let state = QADeviceActivityScheduleState.stopped(
            previous: QADeviceActivityScheduleStore.load(),
            now: now
        )
        QADeviceActivityScheduleStore.save(state)
        return state
    }
}

enum QADeviceActivityMonitorPolicy {
    static func action(
        session: QAShieldSessionState?,
        selection: QAScreenTimeSelectionSummary,
        now: Date
    ) -> QADeviceActivityMonitorAction {
        guard let session, selection.hasProtectedTargets else {
            return .keepShield
        }
        if session.isPauseActive(now: now) || session.actionMode == .openAnyway {
            return .clearShield
        }
        if session.actionMode == .armed || session.needsManualReapply(now: now) {
            return .applyShield
        }
        return .keepShield
    }
}

enum QADeviceActivityScheduleStore {
    private static let key = "qa.deviceActivitySchedule.v1"

    static func load(userDefaults: UserDefaults? = QAAppGroup.userDefaults) -> QADeviceActivityScheduleState? {
        guard let userDefaults else {
            return nil
        }
        guard let data = userDefaults.data(forKey: key) else {
            return nil
        }
        return try? JSONDecoder().decode(QADeviceActivityScheduleState.self, from: data)
    }

    static func save(_ state: QADeviceActivityScheduleState, userDefaults: UserDefaults? = QAAppGroup.userDefaults) {
        guard let userDefaults else {
            deviceActivityScheduleLogger.error("Refusing to write DeviceActivity schedule because App Group storage is unavailable.")
            return
        }
        guard let data = try? JSONEncoder().encode(state) else {
            return
        }
        userDefaults.set(data, forKey: key)
    }

    static func record(_ event: QADeviceActivityMonitorEventRecord, userDefaults: UserDefaults? = QAAppGroup.userDefaults) {
        let state = load(userDefaults: userDefaults)
            ?? QADeviceActivityScheduleState.scheduled(selection: .empty, now: event.createdAt)
        save(state.recording(event), userDefaults: userDefaults)
    }
}

private extension QAScreenTimeSelectionSummary {
    static let empty = QAScreenTimeSelectionSummary(applicationCount: 0, categoryCount: 0, webDomainCount: 0)
}
