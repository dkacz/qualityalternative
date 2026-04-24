import DeviceActivity
import Foundation

final class DeviceActivityMonitorExtension: DeviceActivityMonitor {
    private let shieldApplier = QAManagedSettingsShieldApplier()

    override func intervalDidStart(for activity: DeviceActivityName) {
        super.intervalDidStart(for: activity)
        handle(kind: .intervalStarted, activity: activity, event: nil)
    }

    override func eventDidReachThreshold(_ event: DeviceActivityEvent.Name, activity: DeviceActivityName) {
        super.eventDidReachThreshold(event, activity: activity)
        handle(kind: .thresholdReached, activity: activity, event: event)
    }

    override func intervalDidEnd(for activity: DeviceActivityName) {
        super.intervalDidEnd(for: activity)
        handle(kind: .intervalEnded, activity: activity, event: nil)
        shieldApplier.clear()
    }

    private func handle(
        kind: QADeviceActivityMonitorEventKind,
        activity: DeviceActivityName,
        event: DeviceActivityEvent.Name?
    ) {
        let now = Date()
        QADeviceActivityScheduleStore.record(
            QADeviceActivityMonitorEventRecord(
                kind: kind,
                activityName: activity.rawValue,
                eventName: event?.rawValue,
                createdAt: now
            )
        )

        guard activity == QADeviceActivityNames.protectedWindow else {
            return
        }

        let selection = QAFamilyActivitySelectionStore.load()
        switch QADeviceActivityMonitorPolicy.action(
            session: QAShieldSessionStore.load(),
            selection: QAScreenTimeSelectionSummary(selection: selection),
            now: now
        ) {
        case .applyShield:
            shieldApplier.apply(selection: selection)
        case .clearShield:
            shieldApplier.clear()
        case .keepShield:
            break
        }
    }
}
