package com.qualityalternative.app.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.qualityalternative.app.data.local.QualityAlternativeDatabase
import com.qualityalternative.app.domain.model.AppSettings
import com.qualityalternative.app.domain.model.AnalyticsEventType
import com.qualityalternative.app.domain.model.DistractingApp
import com.qualityalternative.app.domain.model.DurationBucket
import com.qualityalternative.app.domain.model.OnboardingSelection
import com.qualityalternative.app.domain.model.PermissionReadiness
import com.qualityalternative.app.domain.model.PermissionStatus
import com.qualityalternative.app.domain.model.ReplacementHistoryEntry
import com.qualityalternative.app.domain.model.ReturnToTargetSignal
import com.qualityalternative.app.domain.model.SessionFeedback
import com.qualityalternative.app.domain.model.TopicTag
import com.qualityalternative.app.domain.service.HistoryRepository
import com.qualityalternative.app.domain.service.InterceptionMonitor
import com.qualityalternative.app.domain.service.SettingsRepository
import com.qualityalternative.app.ui.MainViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RoomAnalyticsTrackerTest {
    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun productionActiveDelayPath_persistsRoomAnalyticsWithExpectedMetadata() = runTest {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val database = Room.inMemoryDatabaseBuilder(
            context,
            QualityAlternativeDatabase::class.java,
        ).allowMainThreadQueries().build()
        val delayFile = File.createTempFile("room-analytics-active-delay-gate", ".preferences_pb").apply { deleteOnExit() }

        try {
            val tracker = RoomAnalyticsTracker(
                dao = database.analyticsEventDao(),
                scope = backgroundScope,
            )
            val delayGate = PreferencesDelayGate(
                dataStore = testDataStore(delayFile),
                scope = backgroundScope,
            )
            val viewModel = MainViewModel(
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
            advanceUntilIdle()

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
            advanceUntilIdle()

            val events = tracker.observeEvents().first { tracked ->
                tracked.any { it.type == AnalyticsEventType.RETURN_TO_APP_WITHIN_15_MINUTES } &&
                    tracked.any { it.type == AnalyticsEventType.RETURN_TO_APP_WITHIN_60_MINUTES }
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
        } finally {
            database.close()
        }
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun productionExpiryPath_persistsRoomAnalyticsWithExpectedMetadata() = runTest {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val database = Room.inMemoryDatabaseBuilder(
            context,
            QualityAlternativeDatabase::class.java,
        ).allowMainThreadQueries().build()
        val delayFile = File.createTempFile("room-analytics-delay-gate", ".preferences_pb").apply { deleteOnExit() }

        try {
            val tracker = RoomAnalyticsTracker(
                dao = database.analyticsEventDao(),
                scope = backgroundScope,
            )
            val delayGate = PreferencesDelayGate(
                dataStore = testDataStore(delayFile),
                scope = backgroundScope,
            )
            val viewModel = MainViewModel(
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
            advanceUntilIdle()

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
            advanceUntilIdle()

            val events = tracker.observeEvents().first { tracked ->
                tracked.any { it.type == AnalyticsEventType.RETURN_AFTER_DELAY_ENDED } &&
                    tracked.any { it.type == AnalyticsEventType.RETURN_TO_APP_WITHIN_60_MINUTES }
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
        } finally {
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
            )
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
}
