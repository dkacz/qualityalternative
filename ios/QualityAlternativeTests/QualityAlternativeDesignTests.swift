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

    func testShieldControlsRequireAuthorizedProtectedSelection() {
        let readySetup = QAScreenTimeSetupSnapshot(
            authorization: .approved,
            selection: QAScreenTimeSelectionSummary(applicationCount: 1, categoryCount: 1, webDomainCount: 0)
        )
        let missingSelection = QAScreenTimeSetupSnapshot(
            authorization: .approved,
            selection: QAScreenTimeSelectionSummary(applicationCount: 0, categoryCount: 0, webDomainCount: 0)
        )

        XCTAssertTrue(QAShieldControlSnapshot(setup: readySetup, session: nil, now: Date()).canApplyShieldRules)
        XCTAssertFalse(QAShieldControlSnapshot(setup: missingSelection, session: nil, now: Date()).canApplyShieldRules)
    }

    func testShieldSessionStoresOnlyTokenSafeReplacementMetadata() {
        let now = Date(timeIntervalSince1970: 1_777_000_000)
        let state = QAShieldSessionState.armed(
            session: QASampleData.session,
            selection: QAScreenTimeSelectionSummary(applicationCount: 2, categoryCount: 0, webDomainCount: 1),
            now: now
        )

        XCTAssertEqual(state.triggerContextID, "screen-time-selection")
        XCTAssertEqual(state.primaryContentID, QASampleData.readerItem.id)
        XCTAssertEqual(state.backupContentIDs, [QASampleData.linkOnlyItem.id, QASampleData.meditationItem.id])
        XCTAssertEqual(state.selection.totalCount, 3)
        XCTAssertEqual(state.actionMode, .armed)
        XCTAssertTrue(state.containsOnlyTokenSafeMetadata)
    }

    func testPausedShieldStateTracksExpiryWithoutPretendingAutomaticDeviceReapply() {
        let now = Date(timeIntervalSince1970: 1_777_000_000)
        let pauseEnd = now.addingTimeInterval(15 * 60)
        let state = QAShieldSessionState
            .armed(
                session: QASampleData.session,
                selection: QAScreenTimeSelectionSummary(applicationCount: 1, categoryCount: 0, webDomainCount: 0),
                now: now
            )
            .paused(until: pauseEnd, now: now)

        XCTAssertTrue(state.isPauseActive(now: now.addingTimeInterval(60)))
        XCTAssertFalse(state.needsManualReapply(now: now.addingTimeInterval(60)))
        XCTAssertFalse(state.isPauseActive(now: pauseEnd.addingTimeInterval(1)))
        XCTAssertTrue(state.needsManualReapply(now: pauseEnd.addingTimeInterval(1)))
    }

    func testOpenAnywayStateIsExplicitAndTemporary() {
        let now = Date(timeIntervalSince1970: 1_777_000_000)
        let state = QAShieldSessionState
            .armed(
                session: QASampleData.session,
                selection: QAScreenTimeSelectionSummary(applicationCount: 1, categoryCount: 0, webDomainCount: 0),
                now: now
            )
            .openAnyway(now: now.addingTimeInterval(1))

        XCTAssertEqual(state.actionMode, .openAnyway)
        XCTAssertNil(state.pauseExpiresAt)
    }

    func testLibraryIncludesRenderableLinkOnlyAndMeditationItems() {
        let modes = Set(QASampleData.library.map(\.renderMode))

        XCTAssertTrue(modes.contains(.inAppReader))
        XCTAssertTrue(modes.contains(.externalHandoff))
        XCTAssertTrue(modes.contains(.meditationTimer))
    }
}
