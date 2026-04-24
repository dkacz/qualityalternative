import XCTest
@testable import QualityAlternative

final class QualityAlternativeDesignTests: XCTestCase {
    func testLightAndDarkTokensMatchAndroidPalette() {
        XCTAssertEqual(QATokens.tokens(for: .light).colors, QAColors.light)
        XCTAssertEqual(QATokens.tokens(for: .dark).colors, QAColors.dark)
    }

    func testSampleReplacementSessionStaysFinite() {
        let session = QASampleData.session

        XCTAssertEqual(session.backups.count, 2)
        XCTAssertEqual(session.primary.renderMode, .inAppReader)
        XCTAssertEqual(Set(session.backups.map(\.renderMode)), [.externalHandoff, .meditationTimer])
    }

    func testReplacementSessionCapsBackupsAtTwo() {
        let session = QAReplacementSession(
            triggerLabel: "Fixture",
            primary: QASampleData.readerItem,
            backups: [
                QASampleData.linkOnlyItem,
                QASampleData.meditationItem,
                QASampleData.library[3]
            ]
        )

        XCTAssertEqual(session.backups.map(\.id), [
            QASampleData.linkOnlyItem.id,
            QASampleData.meditationItem.id
        ])
    }

    func testScreenTimeSetupRequiresAuthorizationAndSelection() {
        let emptySelection = QAScreenTimeSelectionSummary(
            applicationCount: 0,
            categoryCount: 0,
            webDomainCount: 0
        )
        let selectedApps = QAScreenTimeSelectionSummary(
            applicationCount: 2,
            categoryCount: 0,
            webDomainCount: 1
        )

        XCTAssertFalse(QAScreenTimeSetupSnapshot(authorization: .approved, selection: emptySelection).canPrepareShielding)
        XCTAssertFalse(QAScreenTimeSetupSnapshot(authorization: .denied, selection: selectedApps).canPrepareShielding)
        XCTAssertTrue(QAScreenTimeSetupSnapshot(authorization: .approved, selection: selectedApps).canPrepareShielding)
    }

    func testLibraryIncludesRenderableLinkOnlyAndMeditationItems() {
        let modes = Set(QASampleData.library.map(\.renderMode))

        XCTAssertTrue(modes.contains(.inAppReader))
        XCTAssertTrue(modes.contains(.externalHandoff))
        XCTAssertTrue(modes.contains(.meditationTimer))
    }
}
