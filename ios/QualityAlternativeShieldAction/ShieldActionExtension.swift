import Foundation
import ManagedSettings

final class ShieldActionExtension: ShieldActionDelegate {
    private let shieldApplier = QAManagedSettingsShieldApplier()

    override func handle(
        action: ShieldAction,
        for application: ApplicationToken,
        completionHandler: @escaping (ShieldActionResponse) -> Void
    ) {
        handle(action: action, completionHandler: completionHandler)
    }

    override func handle(
        action: ShieldAction,
        for webDomain: WebDomainToken,
        completionHandler: @escaping (ShieldActionResponse) -> Void
    ) {
        handle(action: action, completionHandler: completionHandler)
    }

    override func handle(
        action: ShieldAction,
        for category: ActivityCategoryToken,
        completionHandler: @escaping (ShieldActionResponse) -> Void
    ) {
        handle(action: action, completionHandler: completionHandler)
    }

    private func handle(action: ShieldAction, completionHandler: @escaping (ShieldActionResponse) -> Void) {
        switch action {
        case .primaryButtonPressed:
            complete(planFor: .primary, completionHandler: completionHandler)
        case .secondaryButtonPressed:
            complete(planFor: .secondary, completionHandler: completionHandler)
        @unknown default:
            completionHandler(.none)
        }
    }

    private func complete(
        planFor button: QAShieldActionButton,
        completionHandler: @escaping (ShieldActionResponse) -> Void
    ) {
        let plan = QAShieldActionPlanner.plan(
            for: button,
            session: QAShieldSessionStore.load(),
            now: Date()
        )
        if let updatedSession = plan.updatedSession {
            QAShieldSessionStore.save(updatedSession)
            shieldApplier.clear()
        }
        if let intent = plan.intent {
            QAShieldActionIntentStore.save(intent)
        }
        completionHandler(plan.response.shieldActionResponse)
    }
}

private extension QAShieldActionResponsePlan {
    var shieldActionResponse: ShieldActionResponse {
        switch self {
        case .keepShield:
            return .none
        case .redrawShield:
            return .defer
        case .closeShield:
            return .close
        }
    }
}
