import XCTest
import FamilyControls
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

    func testShieldConfigurationCopyStaysFiniteAndTokenSafe() {
        let now = Date(timeIntervalSince1970: 1_777_000_000)
        let state = QAShieldSessionState.armed(
            session: QASampleData.session,
            selection: QAScreenTimeSelectionSummary(applicationCount: 2, categoryCount: 0, webDomainCount: 1),
            now: now
        )

        let copy = QAShieldCopyFactory.copy(for: state)

        XCTAssertEqual(copy.title, "Try one better thing first.")
        XCTAssertEqual(copy.primaryButtonLabel, "Queue replacement")
        XCTAssertEqual(copy.secondaryButtonLabel, "Pause 15 min")
        XCTAssertTrue(copy.subtitle.contains("3 opaque protected tokens"))
        XCTAssertFalse(copy.subtitle.contains("Instagram"))
        XCTAssertFalse(copy.subtitle.contains("TikTok"))
    }

    func testQueuedShieldConfigurationCopyDoesNotClaimDirectHostOpen() {
        let intent = QAShieldActionIntent.make(
            kind: .showReplacementChoices,
            session: nil,
            now: Date(timeIntervalSince1970: 1_777_000_000)
        )

        let copy = QAShieldCopyFactory.copy(for: nil, pendingIntent: intent)

        XCTAssertEqual(copy.title, "Replacement is queued.")
        XCTAssertEqual(copy.primaryButtonLabel, "Keep replacement queued")
        XCTAssertTrue(copy.subtitle.contains("cannot directly open the host app"))
    }

    func testPrimaryShieldActionQueuesHostInterventionIntent() {
        let now = Date(timeIntervalSince1970: 1_777_000_000)
        let state = QAShieldSessionState.armed(
            session: QASampleData.session,
            selection: QAScreenTimeSelectionSummary(applicationCount: 1, categoryCount: 0, webDomainCount: 0),
            now: now
        )

        let plan = QAShieldActionPlanner.plan(for: .primary, session: state, now: now)

        XCTAssertEqual(plan.response, .redrawShield)
        XCTAssertNil(plan.updatedSession)
        XCTAssertEqual(plan.intent?.kind, .showReplacementChoices)
        XCTAssertEqual(plan.intent?.selectedContentID, QASampleData.readerItem.id)
        XCTAssertEqual(QAShieldHostIntentRouter.route(for: plan.intent), .intervention)
        XCTAssertEqual(plan.intent?.containsOnlyTokenSafeMetadata, true)
    }

    func testSecondaryShieldActionPausesWithoutUsingSystemDeferAsTimer() {
        let now = Date(timeIntervalSince1970: 1_777_000_000)
        let state = QAShieldSessionState.armed(
            session: QASampleData.session,
            selection: QAScreenTimeSelectionSummary(applicationCount: 1, categoryCount: 0, webDomainCount: 0),
            now: now
        )

        let plan = QAShieldActionPlanner.plan(for: .secondary, session: state, now: now)

        XCTAssertEqual(plan.response, .closeShield)
        XCTAssertEqual(plan.intent?.kind, .pauseForFifteenMinutes)
        XCTAssertEqual(plan.updatedSession?.actionMode, .paused)
        XCTAssertEqual(plan.updatedSession?.pauseExpiresAt, now.addingTimeInterval(QAShieldActionPlanner.pauseDuration))
        XCTAssertEqual(QAShieldHostIntentRouter.route(for: plan.intent), .home)
    }

    func testSecondaryShieldActionFailsClosedWithoutSession() {
        let plan = QAShieldActionPlanner.plan(
            for: .secondary,
            session: nil,
            now: Date(timeIntervalSince1970: 1_777_000_000)
        )

        XCTAssertEqual(plan.response, .keepShield)
        XCTAssertNil(plan.intent)
        XCTAssertNil(plan.updatedSession)
    }

    func testHostForegroundConsumptionRefreshesShieldSessionBeforeRouting() {
        let now = Date(timeIntervalSince1970: 1_777_000_000)
        let refreshedSession = QAShieldSessionState.armed(
            session: QASampleData.session,
            selection: QAScreenTimeSelectionSummary(applicationCount: 1, categoryCount: 0, webDomainCount: 0),
            now: now
        ).paused(until: now.addingTimeInterval(60), now: now)
        let pendingIntent = QAShieldActionIntent.make(
            kind: .pauseForFifteenMinutes,
            session: refreshedSession,
            now: now
        )

        let plan = QAShieldHostForegroundResolver.resolve(
            refreshedSession: refreshedSession,
            pendingIntent: pendingIntent
        )

        XCTAssertEqual(plan.refreshedSession, refreshedSession)
        XCTAssertEqual(plan.route, .home)
        XCTAssertTrue(plan.shouldClearIntent)
    }

    func testDeviceActivityScheduleRequiresAuthorizedProtectedSelection() {
        let selectedApps = QAScreenTimeSelectionSummary(applicationCount: 1, categoryCount: 0, webDomainCount: 0)
        let emptySelection = QAScreenTimeSelectionSummary(applicationCount: 0, categoryCount: 0, webDomainCount: 0)

        XCTAssertTrue(
            QADeviceActivityScheduleSnapshot(
                setup: QAScreenTimeSetupSnapshot(authorization: .approved, selection: selectedApps),
                state: nil
            ).canStartMonitoring
        )
        XCTAssertFalse(
            QADeviceActivityScheduleSnapshot(
                setup: QAScreenTimeSetupSnapshot(authorization: .approved, selection: emptySelection),
                state: nil
            ).canStartMonitoring
        )
        XCTAssertFalse(
            QADeviceActivityScheduleSnapshot(
                setup: QAScreenTimeSetupSnapshot(authorization: .denied, selection: selectedApps),
                state: nil
            ).canStartMonitoring
        )
    }

    func testDeviceActivitySchedulePlanDoesNotMonitorEmptySelection() {
        XCTAssertNil(QADeviceActivitySchedulePlan.protectedWindow(selection: FamilyActivitySelection()))
    }

    func testDeviceActivityScheduleStateIsTokenSafe() {
        let now = Date(timeIntervalSince1970: 1_777_000_000)
        let state = QADeviceActivityScheduleState.scheduled(
            selection: QAScreenTimeSelectionSummary(applicationCount: 2, categoryCount: 0, webDomainCount: 1),
            now: now
        ).recording(
            QADeviceActivityMonitorEventRecord(
                kind: .intervalStarted,
                activityName: QADeviceActivityNames.protectedWindow.rawValue,
                eventName: nil,
                createdAt: now
            )
        )

        XCTAssertEqual(state.selection.totalCount, 3)
        XCTAssertTrue(state.containsOnlyTokenSafeMetadata)
    }

    func testDeviceActivityMonitorPolicyRespectsPauseAndReapply() {
        let now = Date(timeIntervalSince1970: 1_777_000_000)
        let selection = QAScreenTimeSelectionSummary(applicationCount: 1, categoryCount: 0, webDomainCount: 0)
        let armed = QAShieldSessionState.armed(
            session: QASampleData.session,
            selection: selection,
            now: now
        )
        let paused = armed.paused(until: now.addingTimeInterval(60), now: now)
        let expiredPause = armed.paused(until: now.addingTimeInterval(-1), now: now)

        XCTAssertEqual(QADeviceActivityMonitorPolicy.action(session: nil, selection: selection, now: now), .keepShield)
        XCTAssertEqual(QADeviceActivityMonitorPolicy.action(session: armed, selection: selection, now: now), .applyShield)
        XCTAssertEqual(QADeviceActivityMonitorPolicy.action(session: paused, selection: selection, now: now), .clearShield)
        XCTAssertEqual(QADeviceActivityMonitorPolicy.action(session: expiredPause, selection: selection, now: now), .applyShield)
    }

    func testShieldActionIntentStorePersistsAndClearsThroughInjectedDefaults() {
        let suiteName = "qa.shieldActionIntent.tests.\(UUID().uuidString)"
        let userDefaults = UserDefaults(suiteName: suiteName)
        defer {
            userDefaults?.removePersistentDomain(forName: suiteName)
        }
        let intent = QAShieldActionIntent.make(
            kind: .showReplacementChoices,
            session: nil,
            now: Date(timeIntervalSince1970: 1_777_000_000)
        )

        QAShieldActionIntentStore.save(intent, userDefaults: userDefaults)
        XCTAssertEqual(QAShieldActionIntentStore.load(userDefaults: userDefaults), intent)

        QAShieldActionIntentStore.clear(userDefaults: userDefaults)
        XCTAssertNil(QAShieldActionIntentStore.load(userDefaults: userDefaults))
    }

    func testAppGroupStoresFailClosedWhenSharedDefaultsAreUnavailable() {
        let now = Date(timeIntervalSince1970: 1_777_000_000)
        let state = QAShieldSessionState.armed(
            session: QASampleData.session,
            selection: QAScreenTimeSelectionSummary(applicationCount: 1, categoryCount: 0, webDomainCount: 0),
            now: now
        )

        XCTAssertNil(QAShieldSessionStore.load(userDefaults: nil))
        QAShieldSessionStore.save(state, userDefaults: nil)
        QAShieldSessionStore.clear(userDefaults: nil)
        QAShieldActionIntentStore.save(
            QAShieldActionIntent.make(kind: .showReplacementChoices, session: state, now: now),
            userDefaults: nil
        )
        QAShieldActionIntentStore.clear(userDefaults: nil)
        let schedule = QADeviceActivityScheduleState.scheduled(selection: state.selection, now: now)
        XCTAssertNil(QADeviceActivityScheduleStore.load(userDefaults: nil))
        QADeviceActivityScheduleStore.save(schedule, userDefaults: nil)
        QADeviceActivityScheduleStore.record(
            QADeviceActivityMonitorEventRecord(
                kind: .intervalStarted,
                activityName: QADeviceActivityNames.protectedWindow.rawValue,
                eventName: nil,
                createdAt: now
            ),
            userDefaults: nil
        )

        let selection = QAFamilyActivitySelectionStore.load(userDefaults: nil)
        XCTAssertEqual(selection.applicationTokens.count, 0)
        XCTAssertEqual(selection.categoryTokens.count, 0)
        XCTAssertEqual(selection.webDomainTokens.count, 0)
    }

    func testLibraryIncludesRenderableLinkOnlyAndMeditationItems() {
        let modes = Set(QASampleData.library.map(\.renderMode))

        XCTAssertTrue(modes.contains(.inAppReader))
        XCTAssertTrue(modes.contains(.externalHandoff))
        XCTAssertTrue(modes.contains(.meditationTimer))
    }
}
