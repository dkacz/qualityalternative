package com.qualityalternative.app.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.qualityalternative.app.data.local.AnalyticsEventEntity
import com.qualityalternative.app.data.local.QualityAlternativeDatabase
import com.qualityalternative.app.data.local.ReplacementSessionEntity
import com.qualityalternative.app.domain.model.AnalyticsEvent
import com.qualityalternative.app.domain.model.AnalyticsSemanticKeys
import com.qualityalternative.app.domain.model.AppSettings
import com.qualityalternative.app.domain.model.AnalyticsEventType
import com.qualityalternative.app.domain.model.AppThemeMode
import com.qualityalternative.app.domain.model.ContentFormat
import com.qualityalternative.app.domain.model.ContentItem
import com.qualityalternative.app.domain.model.DelayWindow
import com.qualityalternative.app.domain.model.DistractingApp
import com.qualityalternative.app.domain.model.DurationBucket
import com.qualityalternative.app.domain.model.OnboardingSelection
import com.qualityalternative.app.domain.model.PermissionReadiness
import com.qualityalternative.app.domain.model.PermissionStatus
import com.qualityalternative.app.domain.model.RecommendationSource
import com.qualityalternative.app.domain.model.ReplacementHistoryEntry
import com.qualityalternative.app.domain.model.ReturnToTargetSignal
import com.qualityalternative.app.domain.model.SessionFeedback
import com.qualityalternative.app.domain.model.TopicTag
import com.qualityalternative.app.domain.service.HistoryRepository
import com.qualityalternative.app.domain.service.InterceptionMonitor
import com.qualityalternative.app.domain.service.SettingsRepository
import com.qualityalternative.app.ui.MainViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.json.JSONObject

@RunWith(AndroidJUnit4::class)
class RoomAnalyticsTrackerTest {
    @Test
    fun activeDelayReplay_repairsPersistedMetricsExactlyOnceAfterRestart() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val databaseName = "room-analytics-active-replay-${System.nanoTime()}"
        val delayFile = File.createTempFile("room-analytics-active-replay", ".preferences_pb").apply { deleteOnExit() }
        val targetApp = SupportedCatalog.distractingApps.first()
        val dataStore = testDataStore(delayFile)
        val appScope = integrationScope()
        val created = DelayWindow(
            id = "delay-active-replay",
            targetAppPackage = targetApp.packageName,
            startsAtMillis = 1_000L,
            endsAtMillis = 1_000L + 15 * 60_000L,
            interventionId = "intervention-1",
            interventionShownAtMillis = 900L,
            primaryContentId = "primary-1",
            backupContentIds = listOf("backup-1", "backup-2"),
            firstReturnAttemptAtMillis = 2_000L,
        )

        try {
            dataStore.edit { preferences ->
                preferences[PreferencesDelayGate.DelayWindows] = setOf(PreferencesDelayGate.encodeWindow(created))
            }

            val reopenedDatabase = fileBackedDatabase(context, databaseName)
            var viewModel: MainViewModel? = null
            try {
                val tracker = RoomAnalyticsTracker(
                    dao = reopenedDatabase.analyticsEventDao(),
                    scope = appScope,
                )
                val reopenedDelayGate = PreferencesDelayGate(
                    dataStore = dataStore,
                    scope = appScope,
                )
                viewModel = MainViewModel(
                    contentRepository = AssetContentRepository(context),
                    settingsRepository = AndroidSettingsRepository(),
                    recommendationEngine = com.qualityalternative.app.domain.service.DefaultRecommendationEngine(),
                    delayGate = reopenedDelayGate,
                    analyticsTracker = tracker,
                    historyRepository = AndroidHistoryRepository(),
                    interceptionMonitor = AndroidInterceptionMonitor(),
                    enableDelayRefreshTicker = false,
                )

                tracker.observeReady().first { it }
                reopenedDelayGate.observeReady().first { it }
                waitForHydratedMainState(viewModel, targetApp.packageName)

                viewModel.triggerDebugIntervention(nowMillis = 3_000L)

                val repairedEvents = withTimeout(10_000L) {
                    tracker.observeEvents().first { tracked ->
                        tracked.count { it.type == AnalyticsEventType.DELAY_SELECTED } == 1 &&
                            tracked.count { it.type == AnalyticsEventType.RETURN_TO_APP_WITHIN_15_MINUTES } == 1 &&
                            tracked.count { it.type == AnalyticsEventType.RETURN_TO_APP_WITHIN_60_MINUTES } == 1
                    }
                }
                val delaySelected = repairedEvents.first { it.type == AnalyticsEventType.DELAY_SELECTED }
                val within15 = repairedEvents.first { it.type == AnalyticsEventType.RETURN_TO_APP_WITHIN_15_MINUTES }
                val within60 = repairedEvents.first { it.type == AnalyticsEventType.RETURN_TO_APP_WITHIN_60_MINUTES }

                assertEquals(created.id, delaySelected.metadata["delayId"])
                assertEquals(1_000L, delaySelected.timestampMillis)
                assertEquals(created.id, within15.metadata["delayId"])
                assertEquals("active_delay", within15.metadata["delayReturnOrigin"])
                assertEquals(2_000L, within15.timestampMillis)
                assertEquals(created.id, within60.metadata["delayId"])
                assertEquals("active_delay", within60.metadata["delayReturnOrigin"])
                assertEquals(2_000L, within60.timestampMillis)

                viewModel.triggerDebugIntervention(nowMillis = 4_000L)
                delay(250)

                val afterRetry = tracker.allEvents()
                assertEquals(1, afterRetry.count { it.type == AnalyticsEventType.DELAY_SELECTED })
                assertEquals(1, afterRetry.count { it.type == AnalyticsEventType.RETURN_TO_APP_WITHIN_15_MINUTES })
                assertEquals(1, afterRetry.count { it.type == AnalyticsEventType.RETURN_TO_APP_WITHIN_60_MINUTES })
            } finally {
                viewModel?.closeForTests()
                appScope.cancel()
                delay(100)
                reopenedDatabase.close()
            }
        } finally {
            context.deleteDatabase(databaseName)
        }
    }

    @Test
    fun expiredDelayReplay_doesNotDuplicatePersistedAnalytics() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val databaseName = "room-analytics-expired-replay-${System.nanoTime()}"
        val delayFile = File.createTempFile("room-analytics-expired-replay", ".preferences_pb").apply { deleteOnExit() }
        val targetApp = SupportedCatalog.distractingApps.first()
        val dataStoreScope = integrationScope()
        val dataStore = testDataStore(delayFile)
        val delayGate = PreferencesDelayGate(
            dataStore = dataStore,
            scope = dataStoreScope,
        )

        try {
            delayGate.observeReady().first { it }
            val created = delayGate.storeDelayDurably(
                targetApp = targetApp,
                nowMillis = 1_000L,
                durationMinutes = 15,
                interventionId = "intervention-1",
                interventionShownAtMillis = 900L,
                primaryContentId = "primary-1",
                backupContentIds = listOf("backup-1", "backup-2"),
            )
            val firstDatabase = fileBackedDatabase(context, databaseName)
            try {
                firstDatabase.analyticsEventDao().insert(
                    analyticsEntity(
                        AnalyticsEvent(
                            type = AnalyticsEventType.RETURN_AFTER_DELAY_ENDED,
                            timestampMillis = created.endsAtMillis + 1,
                            semanticKey = AnalyticsSemanticKeys.delayEnded(created.id),
                            interventionId = "intervention-1",
                            targetAppPackage = targetApp.packageName,
                            primaryContentId = "primary-1",
                            backupContentIds = listOf("backup-1", "backup-2"),
                            contentId = "primary-1",
                            metadata = mapOf(
                                "delayId" to created.id,
                                "delayReturnOrigin" to "after_delay_expired",
                                "delayStartedAtMillis" to created.startsAtMillis.toString(),
                                "delayEndedAtMillis" to created.endsAtMillis.toString(),
                                "hadActiveDelayReturn" to "false",
                            ),
                        ),
                    ),
                )
                firstDatabase.analyticsEventDao().insert(
                    analyticsEntity(
                        AnalyticsEvent(
                            type = AnalyticsEventType.RETURN_TO_APP_WITHIN_15_MINUTES,
                            timestampMillis = created.endsAtMillis + 1,
                            semanticKey = AnalyticsSemanticKeys.delayReturn(
                                delayId = created.id,
                                origin = "after_delay_expired",
                                type = AnalyticsEventType.RETURN_TO_APP_WITHIN_15_MINUTES,
                            ),
                            interventionId = "intervention-1",
                            targetAppPackage = targetApp.packageName,
                            primaryContentId = "primary-1",
                            backupContentIds = listOf("backup-1", "backup-2"),
                            contentId = "primary-1",
                            metadata = mapOf(
                                "delayId" to created.id,
                                "delayReturnOrigin" to "after_delay_expired",
                                "delayStartedAtMillis" to created.startsAtMillis.toString(),
                                "delayEndedAtMillis" to created.endsAtMillis.toString(),
                            ),
                        ),
                    ),
                )
                firstDatabase.analyticsEventDao().insert(
                    analyticsEntity(
                        AnalyticsEvent(
                            type = AnalyticsEventType.RETURN_TO_APP_WITHIN_60_MINUTES,
                            timestampMillis = created.endsAtMillis + 1,
                            semanticKey = AnalyticsSemanticKeys.delayReturn(
                                delayId = created.id,
                                origin = "after_delay_expired",
                                type = AnalyticsEventType.RETURN_TO_APP_WITHIN_60_MINUTES,
                            ),
                            interventionId = "intervention-1",
                            targetAppPackage = targetApp.packageName,
                            primaryContentId = "primary-1",
                            backupContentIds = listOf("backup-1", "backup-2"),
                            contentId = "primary-1",
                            metadata = mapOf(
                                "delayId" to created.id,
                                "delayReturnOrigin" to "after_delay_expired",
                                "delayStartedAtMillis" to created.startsAtMillis.toString(),
                                "delayEndedAtMillis" to created.endsAtMillis.toString(),
                            ),
                        ),
                    ),
                )
            } finally {
                firstDatabase.close()
            }

            val reopenedDatabase = fileBackedDatabase(context, databaseName)
            val reopenedScope = integrationScope()
            var viewModel: MainViewModel? = null
            try {
                val tracker = RoomAnalyticsTracker(
                    dao = reopenedDatabase.analyticsEventDao(),
                    scope = reopenedScope,
                )
                val reopenedDelayGate = PreferencesDelayGate(
                    dataStore = dataStore,
                    scope = dataStoreScope,
                )
                viewModel = MainViewModel(
                    contentRepository = AssetContentRepository(context),
                    settingsRepository = AndroidSettingsRepository(),
                    recommendationEngine = com.qualityalternative.app.domain.service.DefaultRecommendationEngine(),
                    delayGate = reopenedDelayGate,
                    analyticsTracker = tracker,
                    historyRepository = AndroidHistoryRepository(),
                    interceptionMonitor = AndroidInterceptionMonitor(),
                    enableDelayRefreshTicker = false,
                )

                tracker.observeReady().first { it }
                reopenedDelayGate.observeReady().first { it }
                waitForHydratedMainState(viewModel, targetApp.packageName)

                viewModel.triggerDebugIntervention(nowMillis = created.endsAtMillis + 1)

                val events = withTimeout(10_000L) {
                    tracker.observeEvents().first { tracked ->
                        tracked.count { it.semanticKey == AnalyticsSemanticKeys.delayEnded(created.id) } == 1 &&
                            tracked.count {
                                it.semanticKey == AnalyticsSemanticKeys.delayReturn(
                                    delayId = created.id,
                                    origin = "after_delay_expired",
                                    type = AnalyticsEventType.RETURN_TO_APP_WITHIN_15_MINUTES,
                                )
                            } == 1 &&
                            tracked.count {
                                it.semanticKey == AnalyticsSemanticKeys.delayReturn(
                                    delayId = created.id,
                                    origin = "after_delay_expired",
                                    type = AnalyticsEventType.RETURN_TO_APP_WITHIN_60_MINUTES,
                                )
                            } == 1
                    }
                }
                assertEquals(1, events.count { it.semanticKey == AnalyticsSemanticKeys.delayEnded(created.id) })
                assertEquals(
                    1,
                    events.count {
                        it.semanticKey == AnalyticsSemanticKeys.delayReturn(
                            delayId = created.id,
                            origin = "after_delay_expired",
                            type = AnalyticsEventType.RETURN_TO_APP_WITHIN_15_MINUTES,
                        )
                    },
                )
                assertEquals(
                    1,
                    events.count {
                        it.semanticKey == AnalyticsSemanticKeys.delayReturn(
                            delayId = created.id,
                            origin = "after_delay_expired",
                            type = AnalyticsEventType.RETURN_TO_APP_WITHIN_60_MINUTES,
                        )
                    },
                )
            } finally {
                viewModel?.closeForTests()
                reopenedScope.cancel()
                delay(100)
                reopenedDatabase.close()
            }
        } finally {
            dataStoreScope.cancel()
            context.deleteDatabase(databaseName)
        }
    }

    @Test
    fun returnedSessionReplay_repairsMetricsExactlyOnceAfterRestart() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val databaseName = "room-analytics-session-replay-${System.nanoTime()}"
        val targetApp = SupportedCatalog.distractingApps.first()
        val delayFile = File.createTempFile("room-analytics-session-replay", ".preferences_pb").apply { deleteOnExit() }
        val dataStoreScope = integrationScope()
        val dataStore = testDataStore(delayFile)
        val content = historyContentFixture()
        val backupContentIds = listOf("backup-1", "backup-2")
        val interventionId = "intervention-1"
        var sessionId: String? = null

        try {
            val firstDatabase = fileBackedDatabase(context, databaseName)
            val firstScope = integrationScope()
            var firstViewModel: MainViewModel? = null
            try {
                val tracker = RoomAnalyticsTracker(
                    dao = firstDatabase.analyticsEventDao(),
                    scope = firstScope,
                )
                val historyRepository = RoomHistoryRepository(
                    dao = firstDatabase.replacementSessionDao(),
                    scope = firstScope,
                )
                firstViewModel = MainViewModel(
                    contentRepository = AssetContentRepository(context),
                    settingsRepository = AndroidSettingsRepository(),
                    recommendationEngine = com.qualityalternative.app.domain.service.DefaultRecommendationEngine(),
                    delayGate = PreferencesDelayGate(
                        dataStore = dataStore,
                        scope = dataStoreScope,
                    ),
                    analyticsTracker = tracker,
                    historyRepository = historyRepository,
                    interceptionMonitor = AndroidInterceptionMonitor(),
                    enableDelayRefreshTicker = false,
                )
                tracker.observeReady().first { it }
                historyRepository.observeReady().first { it }
                waitForHydratedMainState(firstViewModel, targetApp.packageName)

                sessionId = historyRepository.recordAcceptedSession(
                    targetApp = targetApp,
                    interventionId = interventionId,
                    interventionShownAtMillis = 1_000L,
                    primaryContentId = content.id,
                    backupContentIds = backupContentIds,
                    content = content,
                    source = RecommendationSource.PRIMARY,
                    acceptedAtMillis = 1_100L,
                )
                val initialSignal = historyRepository.markReturnedToTarget(
                    targetAppPackage = targetApp.packageName,
                    returnedAtMillis = 4_000L,
                )
                assertEquals(sessionId, initialSignal?.sessionId)
                assertEquals(4_000L, initialSignal?.returnedAtMillis)
                assertEquals(true, initialSignal?.within15Minutes)
                assertEquals(true, initialSignal?.within60Minutes)

                val persistedEntry = waitForHistoryEntry {
                    historyRepository.recentHistory(nowMillis = 4_000L)
                        .singleOrNull()
                        ?.takeIf { it.returnedToTargetAtMillis == 4_000L }
                }
                assertEquals(4_000L, persistedEntry.returnedToTargetAtMillis)
                assertTrue(tracker.allEvents().isEmpty())
            } finally {
                firstViewModel?.closeForTests()
                firstScope.cancel()
                delay(100)
                firstDatabase.close()
            }

            val reopenedDatabase = fileBackedDatabase(context, databaseName)
            val secondScope = integrationScope()
            var secondViewModel: MainViewModel? = null
            try {
                val tracker = RoomAnalyticsTracker(
                    dao = reopenedDatabase.analyticsEventDao(),
                    scope = secondScope,
                )
                val historyRepository = RoomHistoryRepository(
                    dao = reopenedDatabase.replacementSessionDao(),
                    scope = secondScope,
                )
                secondViewModel = MainViewModel(
                    contentRepository = AssetContentRepository(context),
                    settingsRepository = AndroidSettingsRepository(),
                    recommendationEngine = com.qualityalternative.app.domain.service.DefaultRecommendationEngine(),
                    delayGate = PreferencesDelayGate(
                        dataStore = dataStore,
                        scope = dataStoreScope,
                    ),
                    analyticsTracker = tracker,
                    historyRepository = historyRepository,
                    interceptionMonitor = AndroidInterceptionMonitor(),
                    enableDelayRefreshTicker = false,
                )

                tracker.observeReady().first { it }
                historyRepository.observeReady().first { it }
                waitForHydratedMainState(secondViewModel, targetApp.packageName)

                secondViewModel.triggerDebugIntervention(nowMillis = 6_000L)

                val events = withTimeout(10_000L) {
                    tracker.observeEvents().first { tracked ->
                        tracked.count {
                            it.semanticKey == AnalyticsSemanticKeys.sessionReturn(
                                sessionId = sessionId!!,
                                type = AnalyticsEventType.RETURN_TO_APP_WITHIN_15_MINUTES,
                            )
                        } == 1 &&
                            tracked.count {
                                it.semanticKey == AnalyticsSemanticKeys.sessionReturn(
                                    sessionId = sessionId!!,
                                    type = AnalyticsEventType.RETURN_TO_APP_WITHIN_60_MINUTES,
                                )
                            } == 1
                    }
                }
                val within15 = events.first {
                    it.semanticKey == AnalyticsSemanticKeys.sessionReturn(
                        sessionId = sessionId!!,
                        type = AnalyticsEventType.RETURN_TO_APP_WITHIN_15_MINUTES,
                    )
                }
                val within60 = events.first {
                    it.semanticKey == AnalyticsSemanticKeys.sessionReturn(
                        sessionId = sessionId!!,
                        type = AnalyticsEventType.RETURN_TO_APP_WITHIN_60_MINUTES,
                    )
                }
                assertEquals(4_000L, within15.timestampMillis)
                assertEquals(4_000L, within60.timestampMillis)
                assertEquals(interventionId, within15.interventionId)
                assertEquals(content.id, within15.contentId)
                assertEquals(targetApp.packageName, within15.targetAppPackage)

                secondViewModel.triggerDebugIntervention(nowMillis = 7_000L)
                val afterRetry = tracker.allEvents()
                assertEquals(
                    1,
                    afterRetry.count {
                        it.semanticKey == AnalyticsSemanticKeys.sessionReturn(
                            sessionId = sessionId!!,
                            type = AnalyticsEventType.RETURN_TO_APP_WITHIN_15_MINUTES,
                        )
                    },
                )
                assertEquals(
                    1,
                    afterRetry.count {
                        it.semanticKey == AnalyticsSemanticKeys.sessionReturn(
                            sessionId = sessionId!!,
                            type = AnalyticsEventType.RETURN_TO_APP_WITHIN_60_MINUTES,
                        )
                    },
                )
                val replayedEntry = historyRepository.recentHistory(nowMillis = 7_000L).single()
                assertEquals(4_000L, replayedEntry.returnedToTargetAtMillis)
            } finally {
                secondViewModel?.closeForTests()
                secondScope.cancel()
                delay(100)
                reopenedDatabase.close()
            }
        } finally {
            dataStoreScope.cancel()
            context.deleteDatabase(databaseName)
        }
    }

    @Test
    fun productionActiveDelayPath_persistsRoomAnalyticsWithExpectedMetadata() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val database = Room.inMemoryDatabaseBuilder(
            context,
            QualityAlternativeDatabase::class.java,
        ).allowMainThreadQueries().build()
        val delayFile = File.createTempFile("room-analytics-active-delay-gate", ".preferences_pb").apply { deleteOnExit() }
        val appScope = integrationScope()

        try {
            val tracker = RoomAnalyticsTracker(
                dao = database.analyticsEventDao(),
                scope = appScope,
            )
            val delayGate = PreferencesDelayGate(
                dataStore = testDataStore(delayFile),
                scope = appScope,
            )
            var viewModel: MainViewModel? = null
            viewModel = MainViewModel(
                contentRepository = AssetContentRepository(context),
                settingsRepository = AndroidSettingsRepository(),
                recommendationEngine = com.qualityalternative.app.domain.service.DefaultRecommendationEngine(),
                delayGate = delayGate,
                analyticsTracker = tracker,
                historyRepository = AndroidHistoryRepository(),
                interceptionMonitor = AndroidInterceptionMonitor(),
                enableDelayRefreshTicker = false,
            )

            tracker.observeReady().first { it }
            delayGate.observeReady().first { it }
            waitForHydratedMainState(viewModel)

            val targetApp = viewModel.uiState.selectedTargetApp!!
            val created = delayGate.storeDelayDurably(
                targetApp = targetApp,
                nowMillis = 1_000L,
                durationMinutes = 15,
                interventionId = "intervention-1",
                interventionShownAtMillis = 900L,
                primaryContentId = "primary-1",
                backupContentIds = listOf("backup-1", "backup-2"),
            )
            viewModel.triggerDebugIntervention(nowMillis = 2_000L)

            val events = withTimeout(10_000L) {
                tracker.observeEvents().first { tracked ->
                    tracked.any { it.type == AnalyticsEventType.RETURN_TO_APP_WITHIN_15_MINUTES } &&
                        tracked.any { it.type == AnalyticsEventType.RETURN_TO_APP_WITHIN_60_MINUTES }
                }
            }
            val within15 = events.first { it.type == AnalyticsEventType.RETURN_TO_APP_WITHIN_15_MINUTES }
            val within60 = events.first { it.type == AnalyticsEventType.RETURN_TO_APP_WITHIN_60_MINUTES }
            val afterTrigger = delayGate.inspectDelay(targetApp = targetApp, nowMillis = 2_000L)

            assertEquals("intervention-1", within15.interventionId)
            assertEquals("primary-1", within15.primaryContentId)
            assertEquals(listOf("backup-1", "backup-2"), within15.backupContentIds)
            assertEquals(created.id, within15.metadata["delayId"])
            assertEquals("active_delay", within15.metadata["delayReturnOrigin"])
            assertEquals(created.id, within60.metadata["delayId"])
            assertEquals("active_delay", within60.metadata["delayReturnOrigin"])
            assertEquals(2_000L, afterTrigger.activeWindow?.firstReturnAttemptAtMillis)
            assertEquals(created.id, afterTrigger.activeWindow?.id)
            assertEquals(null, afterTrigger.expiredWindow)
            viewModel.closeForTests()
        } finally {
            appScope.cancel()
            delay(100)
            database.close()
        }
    }

    @Test
    fun productionExpiryPath_persistsRoomAnalyticsWithExpectedMetadata() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val database = Room.inMemoryDatabaseBuilder(
            context,
            QualityAlternativeDatabase::class.java,
        ).allowMainThreadQueries().build()
        val delayFile = File.createTempFile("room-analytics-delay-gate", ".preferences_pb").apply { deleteOnExit() }
        val appScope = integrationScope()

        try {
            val tracker = RoomAnalyticsTracker(
                dao = database.analyticsEventDao(),
                scope = appScope,
            )
            val delayGate = PreferencesDelayGate(
                dataStore = testDataStore(delayFile),
                scope = appScope,
            )
            var viewModel: MainViewModel? = null
            viewModel = MainViewModel(
                contentRepository = AssetContentRepository(context),
                settingsRepository = AndroidSettingsRepository(),
                recommendationEngine = com.qualityalternative.app.domain.service.DefaultRecommendationEngine(),
                delayGate = delayGate,
                analyticsTracker = tracker,
                historyRepository = AndroidHistoryRepository(),
                interceptionMonitor = AndroidInterceptionMonitor(),
                enableDelayRefreshTicker = false,
            )

            tracker.observeReady().first { it }
            delayGate.observeReady().first { it }
            waitForHydratedMainState(viewModel)

            val targetApp = viewModel.uiState.selectedTargetApp!!
            val created = delayGate.storeDelayDurably(
                targetApp = targetApp,
                nowMillis = 1_000L,
                durationMinutes = 15,
                interventionId = "intervention-1",
                interventionShownAtMillis = 900L,
                primaryContentId = "primary-1",
                backupContentIds = listOf("backup-1", "backup-2"),
            )
            viewModel.triggerDebugIntervention(nowMillis = created.endsAtMillis + 1)

            val events = withTimeout(10_000L) {
                tracker.observeEvents().first { tracked ->
                    tracked.any { it.type == AnalyticsEventType.RETURN_AFTER_DELAY_ENDED } &&
                        tracked.any { it.type == AnalyticsEventType.RETURN_TO_APP_WITHIN_60_MINUTES }
                }
            }
            val expiryEvent = events.first { it.type == AnalyticsEventType.RETURN_AFTER_DELAY_ENDED }
            val within60 = events.first { it.type == AnalyticsEventType.RETURN_TO_APP_WITHIN_60_MINUTES }

            assertEquals(AnalyticsEventType.RETURN_AFTER_DELAY_ENDED, expiryEvent.type)
            assertEquals("intervention-1", expiryEvent.interventionId)
            assertEquals("primary-1", expiryEvent.primaryContentId)
            assertEquals(listOf("backup-1", "backup-2"), expiryEvent.backupContentIds)
            assertEquals("after_delay_expired", expiryEvent.metadata["delayReturnOrigin"])
            assertEquals(created.id, expiryEvent.metadata["delayId"])
            assertEquals(created.id, within60.metadata["delayId"])
            assertEquals("after_delay_expired", within60.metadata["delayReturnOrigin"])
            val afterConsume = waitForDelayInspection {
                delayGate.inspectDelay(targetApp = targetApp, nowMillis = created.endsAtMillis + 1)
            }
            assertEquals(null, afterConsume.activeWindow)
            assertEquals(null, afterConsume.expiredWindow)
            assertFalse(delayGate.consumeExpiredDelay(targetApp = targetApp, delayId = created.id, nowMillis = created.endsAtMillis + 1))
            viewModel.closeForTests()
        } finally {
            appScope.cancel()
            delay(100)
            database.close()
        }
    }

    private class AndroidSettingsRepository : SettingsRepository {
        private val state = MutableStateFlow(
            AppSettings(
                hasCompletedOnboarding = true,
                selectedAppPackages = SupportedCatalog.distractingApps.take(3).mapTo(mutableSetOf(), DistractingApp::packageName),
                preferredTopics = setOf(TopicTag.PHILOSOPHY, TopicTag.SCIENCE, TopicTag.HISTORY),
                preferredDurationBucket = DurationBucket.FOCUS,
                selectedPackIds = setOf("philosophy", "science"),
            ),
        )

        override fun observeAppSettings(): Flow<AppSettings> = state

        override fun supportedDistractingApps(): List<DistractingApp> = SupportedCatalog.distractingApps

        override suspend fun saveOnboardingSelection(selection: OnboardingSelection) {
            state.value = AppSettings(
                hasCompletedOnboarding = true,
                selectedAppPackages = selection.selectedAppPackages,
                preferredTopics = selection.preferredTopics,
                preferredDurationBucket = selection.preferredDurationBucket,
                selectedPackIds = selection.selectedPackIds,
                themeMode = state.value.themeMode,
            )
        }

        override suspend fun saveThemeMode(themeMode: AppThemeMode) {
            state.value = state.value.copy(themeMode = themeMode)
        }
    }

    private class AndroidHistoryRepository : HistoryRepository {
        override fun recentHistory(nowMillis: Long, windowDays: Int): List<ReplacementHistoryEntry> = emptyList()

        override suspend fun recordAcceptedSession(
            targetApp: DistractingApp,
            interventionId: String,
            interventionShownAtMillis: Long,
            primaryContentId: String,
            backupContentIds: List<String>,
            content: com.qualityalternative.app.domain.model.ContentItem,
            source: com.qualityalternative.app.domain.model.RecommendationSource,
            acceptedAtMillis: Long,
        ): String = "unused-session"

        override suspend fun markCompleted(sessionId: String, completedAtMillis: Long) = Unit

        override suspend fun markSkipped(sessionId: String, skippedAtMillis: Long) = Unit

        override suspend fun attachFeedback(sessionId: String, feedback: SessionFeedback) = Unit

        override suspend fun markReturnedToTarget(targetAppPackage: String, returnedAtMillis: Long): ReturnToTargetSignal? = null
    }

    private class AndroidInterceptionMonitor : InterceptionMonitor {
        override fun isAvailable(): Boolean = false

        override fun currentReadiness(): PermissionReadiness {
            return PermissionReadiness(
                overlayStatus = PermissionStatus.MISSING,
                accessibilityStatus = PermissionStatus.UNAVAILABLE_IN_BUILD,
                interceptionReady = false,
                summary = "Manual mode only",
            )
        }
    }

    private fun testDataStore(file: File): DataStore<Preferences> {
        return PreferenceDataStoreFactory.create(produceFile = { file })
    }

    private fun fileBackedDatabase(context: Context, name: String): QualityAlternativeDatabase {
        return Room.databaseBuilder(
            context,
            QualityAlternativeDatabase::class.java,
            name,
        ).allowMainThreadQueries().build()
    }

    private fun integrationScope(): CoroutineScope {
        return CoroutineScope(SupervisorJob() + Dispatchers.IO)
    }

    private fun analyticsEntity(event: AnalyticsEvent): AnalyticsEventEntity {
        return AnalyticsEventEntity(
            type = event.type.name,
            timestampMillis = event.timestampMillis,
            semanticKey = event.semanticKey,
            interventionId = event.interventionId,
            sessionId = event.sessionId,
            targetAppPackage = event.targetAppPackage,
            primaryContentId = event.primaryContentId,
            backupContentIdsCsv = event.backupContentIds.joinToString(","),
            contentId = event.contentId,
            metadataJson = JSONObject(event.metadata).toString(),
        )
    }

    private suspend fun waitForDelayInspection(
        provider: () -> com.qualityalternative.app.domain.model.DelayInspection,
    ): com.qualityalternative.app.domain.model.DelayInspection {
        repeat(20) {
            val inspection = provider()
            if (inspection.activeWindow == null && inspection.expiredWindow == null) {
                return inspection
            }
            delay(25)
        }
        return provider()
    }

    private suspend fun waitForHistoryEntry(
        provider: () -> ReplacementHistoryEntry?,
    ): ReplacementHistoryEntry {
        repeat(400) {
            provider()?.let { return it }
            delay(25)
        }
        throw AssertionError("Timed out waiting for history entry")
    }

    private fun historyContentFixture(): ContentItem {
        return ContentItem(
            id = "content-1",
            packId = "philosophy",
            title = "Stoic note",
            description = "A short reflective text",
            durationMinutes = 7,
            format = ContentFormat.MARKDOWN,
            topicTags = setOf(TopicTag.PHILOSOPHY),
            bodyAssetPath = "unused",
        )
    }

    private suspend fun waitForHydratedMainState(
        viewModel: MainViewModel,
        expectedTargetPackage: String? = null,
    ) {
        withTimeout(10_000L) {
            while (true) {
                val uiState = viewModel.uiState
                val targetMatches = expectedTargetPackage == null ||
                    uiState.selectedTargetApp?.packageName == expectedTargetPackage
                if (!uiState.isLoadingSettings && targetMatches) {
                    return@withTimeout
                }
                delay(25)
            }
        }
    }
}
