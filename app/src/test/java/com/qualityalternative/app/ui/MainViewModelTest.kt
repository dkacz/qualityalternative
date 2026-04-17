package com.qualityalternative.app.ui

import com.qualityalternative.app.analytics.InMemoryAnalyticsTracker
import com.qualityalternative.app.data.InMemoryDelayGate
import com.qualityalternative.app.data.SupportedCatalog
import com.qualityalternative.app.domain.model.AppSettings
import com.qualityalternative.app.domain.model.ContentFormat
import com.qualityalternative.app.domain.model.ContentItem
import com.qualityalternative.app.domain.model.DistractingApp
import com.qualityalternative.app.domain.model.DurationBucket
import com.qualityalternative.app.domain.model.EditorialPack
import com.qualityalternative.app.domain.model.OnboardingSelection
import com.qualityalternative.app.domain.model.PermissionReadiness
import com.qualityalternative.app.domain.model.PermissionStatus
import com.qualityalternative.app.domain.model.RecommendationSource
import com.qualityalternative.app.domain.model.ReplacementHistoryEntry
import com.qualityalternative.app.domain.model.ReturnToTargetSignal
import com.qualityalternative.app.domain.model.SessionFeedback
import com.qualityalternative.app.domain.model.TopicTag
import com.qualityalternative.app.domain.service.ContentRepository
import com.qualityalternative.app.domain.service.DefaultRecommendationEngine
import com.qualityalternative.app.domain.service.HistoryRepository
import com.qualityalternative.app.domain.service.InterceptionMonitor
import com.qualityalternative.app.domain.service.SettingsRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class MainViewModelTest {
    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun completeOnboarding_updatesUiStateFromRepositoryFlow() = runTest {
        val settingsRepository = FakeSettingsRepository()
        val historyRepository = FakeHistoryRepository()
        val viewModel = createViewModel(
            settingsRepository = settingsRepository,
            historyRepository = historyRepository,
        )

        advanceUntilIdle()
        assertFalse(viewModel.uiState.hasCompletedOnboarding)

        viewModel.completeOnboarding()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.hasCompletedOnboarding)
        assertEquals(MainScreen.Home, viewModel.uiState.screen)
        assertEquals(3, viewModel.uiState.availableTargetApps.size)
        assertEquals(setOf("philosophy"), viewModel.uiState.preferences?.selectedPackIds)
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun finishReading_marksHistoryCompletedAndExcludesContent() = runTest {
        val viewModel = createViewModel()

        advanceUntilIdle()
        viewModel.completeOnboarding()
        advanceUntilIdle()
        viewModel.triggerDebugIntervention(nowMillis = 1_000L)
        viewModel.acceptPrimary()
        viewModel.finishReading()
        advanceUntilIdle()

        assertEquals(MainScreen.Feedback, viewModel.uiState.screen)
        assertEquals(setOf("p1"), viewModel.uiState.completedContentIds)
        assertTrue(viewModel.uiState.historyEntries.first().isCompleted())
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun triggerDebugIntervention_marksReturnSignalForRecentSession() = runTest {
        val historyRepository = FakeHistoryRepository().apply {
            recordAcceptedSession(
                targetApp = SupportedCatalog.distractingApps.first(),
                content = FakeContentRepository().inventory().first(),
                source = RecommendationSource.PRIMARY,
                acceptedAtMillis = 1_000L,
            )
            markCompleted(sessionId = historyEntries.value.first().sessionId, completedAtMillis = 2_000L)
        }
        val analyticsTracker = InMemoryAnalyticsTracker()
        val viewModel = createViewModel(
            historyRepository = historyRepository,
            analyticsTracker = analyticsTracker,
        )

        advanceUntilIdle()
        viewModel.completeOnboarding()
        advanceUntilIdle()
        viewModel.triggerDebugIntervention(nowMillis = 5_000L)
        advanceUntilIdle()

        val types = analyticsTracker.allEvents().map { it.type }
        assertTrue(types.contains(com.qualityalternative.app.domain.model.AnalyticsEventType.RETURN_TO_APP_WITHIN_15_MINUTES))
        assertTrue(viewModel.uiState.historyEntries.first().returnedToTarget())
    }

    private fun createViewModel(
        settingsRepository: FakeSettingsRepository = FakeSettingsRepository(),
        historyRepository: FakeHistoryRepository = FakeHistoryRepository(),
        analyticsTracker: InMemoryAnalyticsTracker = InMemoryAnalyticsTracker(),
    ): MainViewModel {
        return MainViewModel(
            contentRepository = FakeContentRepository(),
            settingsRepository = settingsRepository,
            recommendationEngine = DefaultRecommendationEngine(),
            delayGate = InMemoryDelayGate(),
            analyticsTracker = analyticsTracker,
            historyRepository = historyRepository,
            interceptionMonitor = FakeInterceptionMonitor(),
        )
    }

    private class FakeSettingsRepository : SettingsRepository {
        private val state = MutableStateFlow(
            AppSettings(
                hasCompletedOnboarding = false,
                selectedAppPackages = emptySet(),
                preferredTopics = emptySet(),
                preferredDurationBucket = DurationBucket.FOCUS,
                selectedPackIds = emptySet(),
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

    private class FakeHistoryRepository : HistoryRepository {
        val historyEntries = MutableStateFlow<List<ReplacementHistoryEntry>>(emptyList())

        override fun recentHistory(nowMillis: Long, windowDays: Int): List<ReplacementHistoryEntry> = historyEntries.value

        override fun observeRecentHistory(nowMillis: Long, windowDays: Int): Flow<List<ReplacementHistoryEntry>> = historyEntries

        override fun recordAcceptedSession(
            targetApp: DistractingApp,
            content: ContentItem,
            source: RecommendationSource,
            acceptedAtMillis: Long,
        ): String {
            val entry = ReplacementHistoryEntry(
                sessionId = "session-${historyEntries.value.size}",
                targetAppPackage = targetApp.packageName,
                targetAppDisplayName = targetApp.displayName,
                contentId = content.id,
                contentTitle = content.title,
                contentDescription = content.description,
                contentTopics = content.topicTags,
                packId = content.packId,
                recommendationSource = source,
                acceptedAtMillis = acceptedAtMillis,
            )
            historyEntries.value = listOf(entry) + historyEntries.value
            return entry.sessionId
        }

        override fun markCompleted(sessionId: String, completedAtMillis: Long) {
            historyEntries.value = historyEntries.value.map { entry ->
                if (entry.sessionId == sessionId) entry.copy(completedAtMillis = completedAtMillis) else entry
            }
        }

        override fun markSkipped(sessionId: String, skippedAtMillis: Long) {
            historyEntries.value = historyEntries.value.map { entry ->
                if (entry.sessionId == sessionId) entry.copy(skippedAtMillis = skippedAtMillis) else entry
            }
        }

        override fun attachFeedback(sessionId: String, feedback: SessionFeedback) {
            historyEntries.value = historyEntries.value.map { entry ->
                if (entry.sessionId == sessionId) {
                    entry.copy(
                        feedbackGoodFit = feedback.wasGoodFit,
                        feedbackHelpedAvoidScrolling = feedback.helpedAvoidScrolling,
                    )
                } else {
                    entry
                }
            }
        }

        override fun markReturnedToTarget(targetAppPackage: String, returnedAtMillis: Long): ReturnToTargetSignal? {
            val candidate = historyEntries.value.firstOrNull { it.targetAppPackage == targetAppPackage } ?: return null
            historyEntries.value = historyEntries.value.map { entry ->
                if (entry.sessionId == candidate.sessionId) {
                    entry.copy(returnedToTargetAtMillis = returnedAtMillis)
                } else {
                    entry
                }
            }
            return ReturnToTargetSignal(
                sessionId = candidate.sessionId,
                targetAppPackage = candidate.targetAppPackage,
                contentId = candidate.contentId,
                returnedAtMillis = returnedAtMillis,
                within15Minutes = true,
                within60Minutes = true,
            )
        }
    }

    private class FakeContentRepository : ContentRepository {
        private val packs = listOf(
            EditorialPack(
                id = "philosophy",
                title = "Philosophy",
                description = "Pack",
                items = listOf(
                    contentItem(
                        id = "p1",
                        packId = "philosophy",
                        durationMinutes = 7,
                        topics = setOf(TopicTag.PHILOSOPHY),
                    ),
                ),
            ),
            EditorialPack(
                id = "science",
                title = "Science",
                description = "Pack",
                items = listOf(
                    contentItem(
                        id = "s1",
                        packId = "science",
                        durationMinutes = 6,
                        topics = setOf(TopicTag.SCIENCE),
                    ),
                ),
            ),
        )

        override fun starterPacks(): List<EditorialPack> = packs

        override fun inventory(): List<ContentItem> = packs.flatMap(EditorialPack::items)

        override fun contentBody(item: ContentItem): String = item.title

        private fun contentItem(
            id: String,
            packId: String,
            durationMinutes: Int,
            topics: Set<TopicTag>,
        ): ContentItem = ContentItem(
            id = id,
            packId = packId,
            title = id,
            description = id,
            durationMinutes = durationMinutes,
            format = ContentFormat.MARKDOWN,
            topicTags = topics,
            bodyAssetPath = "unused",
        )
    }

    private class FakeInterceptionMonitor : InterceptionMonitor {
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
}
