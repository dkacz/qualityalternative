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
        XCTAssertEqual(session.primary.renderMode, .externalHandoff)
        XCTAssertTrue(session.backups.allSatisfy { $0.durationMinutes <= session.primary.durationMinutes })
        XCTAssertEqual(Set(session.backups.map(\.renderMode)), [.inAppReader, .meditationTimer])
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
        XCTAssertEqual(state.primaryContentID, QASampleData.session.primary.id)
        XCTAssertEqual(state.backupContentIDs, QASampleData.session.backups.map(\.id))
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
        XCTAssertEqual(state.openAnywayExpiresAt, now.addingTimeInterval(1 + QAShieldSessionState.openAnywayDuration))
        XCTAssertTrue(state.isOpenAnywayActive(now: now.addingTimeInterval(2)))
        XCTAssertFalse(
            state.isOpenAnywayActive(
                now: now.addingTimeInterval(2 + QAShieldSessionState.openAnywayDuration)
            )
        )
        XCTAssertEqual(state.rearmedAfterOpenAnyway(now: now.addingTimeInterval(3)).actionMode, .armed)
        XCTAssertNil(state.rearmedAfterOpenAnyway(now: now.addingTimeInterval(3)).openAnywayExpiresAt)
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
        XCTAssertEqual(plan.intent?.selectedContentID, QASampleData.session.primary.id)
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
        guard let event = QADeviceActivityMonitorEventRecord.tokenSafe(
            kind: .intervalStarted,
            activityName: QADeviceActivityNames.protectedWindow.rawValue,
            eventName: nil,
            createdAt: now
        ) else {
            XCTFail("Expected generic protected-window event metadata to be token safe.")
            return
        }
        let state = QADeviceActivityScheduleState.scheduled(
            selection: QAScreenTimeSelectionSummary(applicationCount: 2, categoryCount: 0, webDomainCount: 1),
            now: now
        ).recording(event)

        XCTAssertEqual(state.selection.totalCount, 3)
        XCTAssertTrue(state.containsOnlyTokenSafeMetadata)
    }

    func testDeviceActivityScheduleRejectsReadableCallbackMetadata() {
        let now = Date(timeIntervalSince1970: 1_777_000_000)

        XCTAssertNil(
            QADeviceActivityMonitorEventRecord.tokenSafe(
                kind: .thresholdReached,
                activityName: "Instagram",
                eventName: QADeviceActivityNames.firstMinute.rawValue,
                createdAt: now
            )
        )
        XCTAssertNil(
            QADeviceActivityMonitorEventRecord.tokenSafe(
                kind: .thresholdReached,
                activityName: QADeviceActivityNames.protectedWindow.rawValue,
                eventName: "TikTok",
                createdAt: now
            )
        )
        XCTAssertEqual(
            QADeviceActivityMonitorEventRecord.tokenSafe(
                kind: .thresholdReached,
                activityName: QADeviceActivityNames.protectedWindow.rawValue,
                eventName: QADeviceActivityNames.firstMinute.rawValue,
                createdAt: now
            )?.eventName,
            .firstMinute
        )
    }

    func testDeviceActivityFailureStateDoesNotPersistRawErrorText() {
        struct ReadableError: LocalizedError {
            var errorDescription: String? {
                "Instagram authorization failed"
            }
        }

        let now = Date(timeIntervalSince1970: 1_777_000_000)
        let state = QADeviceActivityScheduleState.failed(
            selection: QAScreenTimeSelectionSummary(applicationCount: 1, categoryCount: 0, webDomainCount: 0),
            error: ReadableError(),
            now: now
        )
        let snapshot = QADeviceActivityScheduleSnapshot(
            setup: QAScreenTimeSetupSnapshot(
                authorization: .approved,
                selection: state.selection
            ),
            state: state
        )

        XCTAssertEqual(state.lastFailureReason, .startMonitoringFailed)
        XCTAssertTrue(state.containsOnlyTokenSafeMetadata)
        XCTAssertFalse(snapshot.detailText.contains("Instagram"))
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

    func testDeviceActivityMonitorPolicyConsumesOpenAnywayOnce() {
        let now = Date(timeIntervalSince1970: 1_777_000_000)
        let selection = QAScreenTimeSelectionSummary(applicationCount: 1, categoryCount: 0, webDomainCount: 0)
        let openAnyway = QAShieldSessionState
            .armed(session: QASampleData.session, selection: selection, now: now)
            .openAnyway(now: now)

        let firstDecision = QADeviceActivityMonitorPolicy.decision(
            session: openAnyway,
            selection: selection,
            now: now.addingTimeInterval(1)
        )
        let secondDecision = QADeviceActivityMonitorPolicy.decision(
            session: firstDecision.updatedSession,
            selection: selection,
            now: now.addingTimeInterval(2)
        )
        let expiredDecision = QADeviceActivityMonitorPolicy.decision(
            session: openAnyway,
            selection: selection,
            now: now.addingTimeInterval(QAShieldSessionState.openAnywayDuration + 1)
        )

        XCTAssertEqual(firstDecision.action, .clearShield)
        XCTAssertEqual(firstDecision.updatedSession?.actionMode, .armed)
        XCTAssertEqual(secondDecision.action, .applyShield)
        XCTAssertEqual(expiredDecision.action, .applyShield)
        XCTAssertEqual(expiredDecision.updatedSession?.actionMode, .armed)
    }

    func testDeviceActivityCallbackPlannerIgnoresUnrecognizedActivity() {
        let now = Date(timeIntervalSince1970: 1_777_000_000)
        let selection = QAScreenTimeSelectionSummary(applicationCount: 1, categoryCount: 0, webDomainCount: 0)
        let armed = QAShieldSessionState.armed(session: QASampleData.session, selection: selection, now: now)

        let decision = QADeviceActivityMonitorCallbackPlanner.decision(
            kind: .intervalEnded,
            activityName: "qa.future-or-readable-activity",
            eventName: nil,
            session: armed,
            selection: selection,
            now: now
        )

        XCTAssertNil(decision.event)
        XCTAssertNil(decision.updatedSession)
        XCTAssertEqual(decision.action, .keepShield)
    }

    func testDeviceActivityCallbackPlannerKeepsShieldForEmptySelection() {
        let now = Date(timeIntervalSince1970: 1_777_000_000)
        let emptySelection = QAScreenTimeSelectionSummary(applicationCount: 0, categoryCount: 0, webDomainCount: 0)
        let armed = QAShieldSessionState.armed(
            session: QASampleData.session,
            selection: emptySelection,
            now: now
        )

        let started = QADeviceActivityMonitorCallbackPlanner.decision(
            kind: .intervalStarted,
            activityName: QADeviceActivityNames.protectedWindow.rawValue,
            eventName: nil,
            session: armed,
            selection: emptySelection,
            now: now
        )
        let threshold = QADeviceActivityMonitorCallbackPlanner.decision(
            kind: .thresholdReached,
            activityName: QADeviceActivityNames.protectedWindow.rawValue,
            eventName: QADeviceActivityNames.firstMinute.rawValue,
            session: armed,
            selection: emptySelection,
            now: now
        )
        let ended = QADeviceActivityMonitorCallbackPlanner.decision(
            kind: .intervalEnded,
            activityName: QADeviceActivityNames.protectedWindow.rawValue,
            eventName: nil,
            session: armed,
            selection: emptySelection,
            now: now
        )

        XCTAssertNil(started.event)
        XCTAssertEqual(started.action, .keepShield)
        XCTAssertNil(threshold.event)
        XCTAssertEqual(threshold.action, .keepShield)
        XCTAssertNil(ended.event)
        XCTAssertEqual(ended.action, .keepShield)
    }

    func testDeviceActivityCallbackPlannerRecordsOnlyGenericThresholdEvent() {
        let now = Date(timeIntervalSince1970: 1_777_000_000)
        let selection = QAScreenTimeSelectionSummary(applicationCount: 1, categoryCount: 0, webDomainCount: 0)
        let armed = QAShieldSessionState.armed(session: QASampleData.session, selection: selection, now: now)

        let safeDecision = QADeviceActivityMonitorCallbackPlanner.decision(
            kind: .thresholdReached,
            activityName: QADeviceActivityNames.protectedWindow.rawValue,
            eventName: QADeviceActivityNames.firstMinute.rawValue,
            session: armed,
            selection: selection,
            now: now
        )
        let hostileDecision = QADeviceActivityMonitorCallbackPlanner.decision(
            kind: .thresholdReached,
            activityName: QADeviceActivityNames.protectedWindow.rawValue,
            eventName: "Readable app name",
            session: armed,
            selection: selection,
            now: now
        )

        XCTAssertEqual(safeDecision.event?.eventName, .firstMinute)
        XCTAssertEqual(safeDecision.action, .applyShield)
        XCTAssertNil(hostileDecision.event)
        XCTAssertEqual(hostileDecision.action, .keepShield)
    }

    func testDeviceActivityScheduleStoreDoesNotRecordWithoutActiveNonEmptySchedule() {
        let suiteName = "qa.deviceActivitySchedule.tests.\(UUID().uuidString)"
        let userDefaults = UserDefaults(suiteName: suiteName)
        defer {
            userDefaults?.removePersistentDomain(forName: suiteName)
        }
        let now = Date(timeIntervalSince1970: 1_777_000_000)
        guard let event = QADeviceActivityMonitorEventRecord.tokenSafe(
            kind: .intervalStarted,
            activityName: QADeviceActivityNames.protectedWindow.rawValue,
            eventName: nil,
            createdAt: now
        ) else {
            XCTFail("Expected generic protected-window event metadata to be token safe.")
            return
        }

        QADeviceActivityScheduleStore.record(event, userDefaults: userDefaults)

        XCTAssertNil(QADeviceActivityScheduleStore.load(userDefaults: userDefaults))
    }

    func testDeviceActivityScheduleStoreRecordsOnlyForActiveNonEmptySchedule() {
        let suiteName = "qa.deviceActivitySchedule.tests.\(UUID().uuidString)"
        let userDefaults = UserDefaults(suiteName: suiteName)
        defer {
            userDefaults?.removePersistentDomain(forName: suiteName)
        }
        let now = Date(timeIntervalSince1970: 1_777_000_000)
        let scheduled = QADeviceActivityScheduleState.scheduled(
            selection: QAScreenTimeSelectionSummary(applicationCount: 1, categoryCount: 0, webDomainCount: 0),
            now: now
        )
        guard let event = QADeviceActivityMonitorEventRecord.tokenSafe(
            kind: .thresholdReached,
            activityName: QADeviceActivityNames.protectedWindow.rawValue,
            eventName: QADeviceActivityNames.firstMinute.rawValue,
            createdAt: now.addingTimeInterval(1)
        ) else {
            XCTFail("Expected generic protected-window event metadata to be token safe.")
            return
        }

        QADeviceActivityScheduleStore.save(scheduled, userDefaults: userDefaults)
        QADeviceActivityScheduleStore.record(event, userDefaults: userDefaults)

        XCTAssertEqual(QADeviceActivityScheduleStore.load(userDefaults: userDefaults)?.lastEvent, event)
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
        if let event = QADeviceActivityMonitorEventRecord.tokenSafe(
            kind: .intervalStarted,
            activityName: QADeviceActivityNames.protectedWindow.rawValue,
            eventName: nil,
            createdAt: now
        ) {
            QADeviceActivityScheduleStore.record(event, userDefaults: nil)
        }

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

    func testAndroidEditorialCatalogParityLoadsBundledStarterPacks() {
        let editorialItems = QASampleData.editorialLibrary

        XCTAssertEqual(QASampleData.packs.count, 5)
        XCTAssertEqual(editorialItems.count, 45)
        XCTAssertEqual(editorialItems.filter { $0.renderMode == .inAppReader }.count, 25)
        XCTAssertEqual(editorialItems.filter { $0.renderMode == .externalHandoff }.count, 20)
        XCTAssertEqual(editorialItems.filter { $0.rightsClass == .renderable }.count, 25)
        XCTAssertEqual(editorialItems.filter { $0.rightsClass == .linkOnly }.count, 20)
        XCTAssertEqual(Set(editorialItems.map(\.sourceType)), [.editorial])
    }

    func testEditorialCatalogPreservesRightsAndSourceMetadata() {
        let item = QASampleData.editorialLibrary.first { $0.id == "care-for-the-soul-first" }

        XCTAssertEqual(item?.sourceURL, "https://www.gutenberg.org/ebooks/1656")
        XCTAssertEqual(item?.licenseURL, "https://www.gutenberg.org/policy/license.html")
        XCTAssertEqual(item?.rightsReviewedAt, "2026-04-22")
        XCTAssertTrue(item?.licenseName?.contains("Public domain") == true)
        XCTAssertTrue(item?.attribution?.contains("Plato") == true)
    }

    func testEditorialReaderBodyLoadsMarkdownAsset() {
        let item = QASampleData.editorialLibrary.first { $0.bodyAssetPath != nil }
        let body = item.map(QASampleData.body(for:)) ?? ""

        XCTAssertNotNil(item)
        XCTAssertGreaterThan(body.count, item?.description.count ?? 0)
        XCTAssertTrue(body.contains("\n\n"))
        XCTAssertFalse(body.contains(item?.description ?? ""))
    }

    func testContentPriorityChangesReplacementPrimaryWithoutGrowingTheChoiceSet() {
        let myFiles = QASampleData.replacementSession(meditationMinutes: 10, priority: .myFiles)
        let savedLinks = QASampleData.replacementSession(meditationMinutes: 10, priority: .savedLinks)
        let meditation = QASampleData.replacementSession(meditationMinutes: 10, priority: .meditation)

        XCTAssertEqual(myFiles.primary.id, QASampleData.epubDocumentItem.id)
        XCTAssertEqual(savedLinks.primary.id, QASampleData.userLinkItem.id)
        XCTAssertEqual(meditation.primary.id, "meditation-10m")
        XCTAssertEqual(myFiles.backups.count, 2)
        XCTAssertEqual(savedLinks.backups.count, 2)
        XCTAssertEqual(meditation.backups.count, 2)
    }

    func testReplacementBackupsStayLowerCommitmentAcrossPrioritiesAndDurations() {
        for priority in QAContentPriority.allCases {
            for duration in [3, 5, 10, 15] {
                let session = QASampleData.replacementSession(meditationMinutes: duration, priority: priority)

                XCTAssertEqual(session.backups.count, 2, "Expected two backups for \(priority) at \(duration)m")
                XCTAssertFalse(
                    session.backups.contains { $0.id == session.primary.id },
                    "Backups must not repeat primary for \(priority) at \(duration)m"
                )
                XCTAssertTrue(
                    session.backups.allSatisfy { $0.durationMinutes <= session.primary.durationMinutes },
                    "Backups must not exceed primary duration for \(priority) at \(duration)m"
                )
            }
        }
    }

    func testLocalContentStorePersistsSavedLinksAndDocuments() {
        let suiteName = "qa.localContent.tests.\(UUID().uuidString)"
        let userDefaults = UserDefaults(suiteName: suiteName)!
        defer {
            userDefaults.removePersistentDomain(forName: suiteName)
        }
        var state = QALocalLibraryState.defaults
        let link = QALocalContentStore.makeSavedLink(
            title: "Simulator Saved Link",
            url: "https://example.com/simulator-saved-link",
            durationMinutes: 12
        )
        let pdf = QALocalContentStore.makeDocument(title: "Simulator PDF", format: .pdf, durationMinutes: 15)
        state.userLinks.append(link)
        state.userDocuments.append(pdf)

        QALocalContentStore.save(state, defaults: userDefaults)
        let reloaded = QALocalContentStore.load(defaults: userDefaults)

        XCTAssertTrue(reloaded.userLinks.contains(link))
        XCTAssertTrue(reloaded.userDocuments.contains(pdf))
        XCTAssertEqual(pdf.renderMode, .externalHandoff)
        XCTAssertEqual(link.sourceType, .userLink)
    }

    func testLocalSettingsStorePersistsMeditationDurationAndPriority() {
        let suiteName = "qa.localSettings.tests.\(UUID().uuidString)"
        let userDefaults = UserDefaults(suiteName: suiteName)!
        defer {
            userDefaults.removePersistentDomain(forName: suiteName)
        }
        let settings = QALocalSettings(meditationDurationMinutes: 15, contentPriority: .myFiles)

        QALocalSettingsStore.save(settings, defaults: userDefaults)

        XCTAssertEqual(QALocalSettingsStore.load(defaults: userDefaults), settings)
    }
}
