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

enum QADeviceActivityScheduleFailureReason: String, Codable, Equatable {
    case notReady
    case emptySelection
    case startMonitoringFailed

    var detailText: String {
        switch self {
        case .notReady:
            return "Screen Time access and protected selection are required before monitoring can start."
        case .emptySelection:
            return "DeviceActivity monitoring needs at least one opaque protected token."
        case .startMonitoringFailed:
            return "DeviceActivity could not start monitoring on this environment."
        }
    }
}

enum QADeviceActivityMonitorAction: Equatable {
    case applyShield
    case clearShield
    case keepShield
}

enum QADeviceActivityMonitorActivityName: String, Codable, Equatable {
    case protectedWindow = "qa.protected-window"
}

enum QADeviceActivityMonitorEventName: String, Codable, Equatable {
    case firstMinute = "qa.first-minute"
}

struct QADeviceActivityMonitorEventRecord: Codable, Equatable {
    let kind: QADeviceActivityMonitorEventKind
    let activityName: QADeviceActivityMonitorActivityName
    let eventName: QADeviceActivityMonitorEventName?
    let createdAt: Date

    private init(
        kind: QADeviceActivityMonitorEventKind,
        activityName: QADeviceActivityMonitorActivityName,
        eventName: QADeviceActivityMonitorEventName?,
        createdAt: Date
    ) {
        self.kind = kind
        self.activityName = activityName
        self.eventName = eventName
        self.createdAt = createdAt
    }

    static func tokenSafe(
        kind: QADeviceActivityMonitorEventKind,
        activityName: String,
        eventName: String?,
        createdAt: Date
    ) -> QADeviceActivityMonitorEventRecord? {
        guard activityName == QADeviceActivityNames.protectedWindow.rawValue else {
            return nil
        }
        let safeEventName: QADeviceActivityMonitorEventName?
        if let eventName {
            guard eventName == QADeviceActivityNames.firstMinute.rawValue else {
                return nil
            }
            safeEventName = .firstMinute
        } else {
            safeEventName = nil
        }
        return QADeviceActivityMonitorEventRecord(
            kind: kind,
            activityName: .protectedWindow,
            eventName: safeEventName,
            createdAt: createdAt
        )
    }

    var containsOnlyTokenSafeMetadata: Bool {
        activityName == .protectedWindow && (eventName == nil || eventName == .firstMinute)
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
    let lastFailureReason: QADeviceActivityScheduleFailureReason?
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
            lastFailureReason: nil,
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
            lastFailureReason: nil,
            lastEvent: previous?.lastEvent
        )
    }

    static func failed(selection: QAScreenTimeSelectionSummary, error: Error, now: Date) -> QADeviceActivityScheduleState {
        deviceActivityScheduleLogger.error("DeviceActivity monitoring failed: \(String(describing: error), privacy: .private)")
        return QADeviceActivityScheduleState(
            id: QADeviceActivityNames.protectedWindow.rawValue,
            mode: .failed,
            selection: selection,
            intervalStartHour: 0,
            intervalStartMinute: 0,
            intervalEndHour: 23,
            intervalEndMinute: 59,
            repeats: true,
            updatedAt: now,
            lastFailureReason: QADeviceActivityScheduleFailureReason.from(error),
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
            lastFailureReason: lastFailureReason,
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
            return state.lastFailureReason?.detailText ?? QADeviceActivityScheduleFailureReason.startMonitoringFailed.detailText
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

extension QADeviceActivityScheduleFailureReason {
    static func from(_ error: Error) -> QADeviceActivityScheduleFailureReason {
        guard let schedulingError = error as? QADeviceActivitySchedulingError else {
            return .startMonitoringFailed
        }
        switch schedulingError {
        case .notReady:
            return .notReady
        case .emptySelection:
            return .emptySelection
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
    struct Decision: Equatable {
        let action: QADeviceActivityMonitorAction
        let updatedSession: QAShieldSessionState?
    }

    static func decision(
        session: QAShieldSessionState?,
        selection: QAScreenTimeSelectionSummary,
        now: Date
    ) -> Decision {
        guard let session, selection.hasProtectedTargets else {
            return Decision(action: .keepShield, updatedSession: nil)
        }
        if session.isPauseActive(now: now) {
            return Decision(action: .clearShield, updatedSession: nil)
        }
        if session.actionMode == .openAnyway {
            let rearmedSession = session.rearmedAfterOpenAnyway(now: now)
            if session.isOpenAnywayActive(now: now) {
                return Decision(action: .clearShield, updatedSession: rearmedSession)
            }
            return Decision(action: .applyShield, updatedSession: rearmedSession)
        }
        if session.actionMode == .armed || session.needsManualReapply(now: now) {
            return Decision(action: .applyShield, updatedSession: nil)
        }
        return Decision(action: .keepShield, updatedSession: nil)
    }

    static func action(
        session: QAShieldSessionState?,
        selection: QAScreenTimeSelectionSummary,
        now: Date
    ) -> QADeviceActivityMonitorAction {
        decision(session: session, selection: selection, now: now).action
    }
}

struct QADeviceActivityMonitorCallbackDecision: Equatable {
    let event: QADeviceActivityMonitorEventRecord?
    let action: QADeviceActivityMonitorAction
    let updatedSession: QAShieldSessionState?
}

enum QADeviceActivityMonitorCallbackPlanner {
    static func decision(
        kind: QADeviceActivityMonitorEventKind,
        activityName: String,
        eventName: String?,
        session: QAShieldSessionState?,
        selection: QAScreenTimeSelectionSummary,
        now: Date
    ) -> QADeviceActivityMonitorCallbackDecision {
        guard let event = QADeviceActivityMonitorEventRecord.tokenSafe(
            kind: kind,
            activityName: activityName,
            eventName: eventName,
            createdAt: now
        ) else {
            return QADeviceActivityMonitorCallbackDecision(
                event: nil,
                action: .keepShield,
                updatedSession: nil
            )
        }

        if kind == .intervalEnded {
            return QADeviceActivityMonitorCallbackDecision(
                event: event,
                action: .clearShield,
                updatedSession: nil
            )
        }

        let policyDecision = QADeviceActivityMonitorPolicy.decision(
            session: session,
            selection: selection,
            now: now
        )
        return QADeviceActivityMonitorCallbackDecision(
            event: event,
            action: policyDecision.action,
            updatedSession: policyDecision.updatedSession
        )
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
        do {
            let state = try JSONDecoder().decode(QADeviceActivityScheduleState.self, from: data)
            guard state.containsOnlyTokenSafeMetadata else {
                deviceActivityScheduleLogger.error("Discarding DeviceActivity schedule state with non-token-safe metadata.")
                return nil
            }
            return state
        } catch {
            deviceActivityScheduleLogger.error("Discarding unreadable DeviceActivity schedule state: \(String(describing: error), privacy: .private)")
            return nil
        }
    }

    static func save(_ state: QADeviceActivityScheduleState, userDefaults: UserDefaults? = QAAppGroup.userDefaults) {
        guard let userDefaults else {
            deviceActivityScheduleLogger.error("Refusing to write DeviceActivity schedule because App Group storage is unavailable.")
            return
        }
        guard state.containsOnlyTokenSafeMetadata else {
            deviceActivityScheduleLogger.error("Refusing to write DeviceActivity schedule with non-token-safe metadata.")
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
