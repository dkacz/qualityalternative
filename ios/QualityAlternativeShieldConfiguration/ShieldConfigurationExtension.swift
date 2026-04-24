import ManagedSettings
import ManagedSettingsUI
import UIKit

final class ShieldConfigurationExtension: ShieldConfigurationDataSource {
    override func configuration(shielding application: Application) -> ShieldConfiguration {
        makeConfiguration()
    }

    override func configuration(shielding application: Application, in category: ActivityCategory) -> ShieldConfiguration {
        makeConfiguration()
    }

    override func configuration(shielding webDomain: WebDomain) -> ShieldConfiguration {
        makeConfiguration()
    }

    override func configuration(shielding webDomain: WebDomain, in category: ActivityCategory) -> ShieldConfiguration {
        makeConfiguration()
    }

    private func makeConfiguration() -> ShieldConfiguration {
        let copy = QAShieldCopyFactory.copy(
            for: QAShieldSessionStore.load(),
            pendingIntent: QAShieldActionIntentStore.load()
        )
        return ShieldConfiguration(
            backgroundBlurStyle: .systemUltraThinMaterial,
            backgroundColor: UIColor.qaParchment,
            icon: nil,
            title: ShieldConfiguration.Label(text: copy.title, color: .qaInk),
            subtitle: ShieldConfiguration.Label(text: copy.subtitle, color: .qaMutedInk),
            primaryButtonLabel: ShieldConfiguration.Label(text: copy.primaryButtonLabel, color: .white),
            primaryButtonBackgroundColor: .qaAccent,
            secondaryButtonLabel: ShieldConfiguration.Label(text: copy.secondaryButtonLabel, color: .qaInk)
        )
    }
}

private extension UIColor {
    static let qaParchment = UIColor(red: 0.96, green: 0.92, blue: 0.84, alpha: 1.0)
    static let qaInk = UIColor(red: 0.12, green: 0.10, blue: 0.08, alpha: 1.0)
    static let qaMutedInk = UIColor(red: 0.39, green: 0.34, blue: 0.27, alpha: 1.0)
    static let qaAccent = UIColor(red: 0.62, green: 0.36, blue: 0.20, alpha: 1.0)
}
