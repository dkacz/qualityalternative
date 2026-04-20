package com.qualityalternative.app

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.qualityalternative.app.data.AppContainer
import com.qualityalternative.app.data.SupportedCatalog
import com.qualityalternative.app.domain.model.DistractingApp
import com.qualityalternative.app.domain.model.DurationBucket
import com.qualityalternative.app.domain.model.OnboardingSelection
import com.qualityalternative.app.domain.model.RecommendationSet
import com.qualityalternative.app.domain.model.TopicTag
import com.qualityalternative.app.domain.model.UserLinkDraft
import com.qualityalternative.app.domain.service.AddUserLinkResult
import com.qualityalternative.app.ui.MainScreen
import com.qualityalternative.app.ui.MainViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UserLinkRecommendationIntegrationTest {
    @Test
    fun productionContainerFeedsSavedLinkIntoInterventionAndExternalHandoff() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        context.deleteDatabase("quality_alternative.db")
        context.filesDir.resolve("datastore").deleteRecursively()
        val appContainer = AppContainer(context)
        val selectedApps = SupportedCatalog.distractingApps
            .take(3)
            .mapTo(mutableSetOf(), DistractingApp::packageName)

        appContainer.userLinkRepository.observeReady().first { it }
        appContainer.settingsRepository.saveOnboardingSelection(
            OnboardingSelection(
                selectedAppPackages = selectedApps,
                preferredTopics = setOf(TopicTag.ECONOMICS, TopicTag.HISTORY, TopicTag.CREATIVITY),
                preferredDurationBucket = DurationBucket.FOCUS,
                selectedPackIds = setOf("science"),
            ),
        )
        val saved = appContainer.userLinkRepository.addLink(
            draft = UserLinkDraft(
                url = "https://example.com/focused-essay",
                title = "AAA saved essay",
                durationMinutes = 7,
                topicTags = setOf(TopicTag.ECONOMICS, TopicTag.HISTORY, TopicTag.CREATIVITY),
            ),
            nowMillis = 1_000L,
        ) as AddUserLinkResult.Added

        val viewModel = MainViewModel(
            contentRepository = appContainer.contentRepository,
            userLinkRepository = appContainer.userLinkRepository,
            settingsRepository = appContainer.settingsRepository,
            recommendationEngine = appContainer.recommendationEngine,
            delayGate = appContainer.delayGate,
            analyticsTracker = appContainer.analyticsTracker,
            historyRepository = appContainer.historyRepository,
            interceptionMonitor = appContainer.interceptionMonitor,
            enableDelayRefreshTicker = false,
        )

        try {
            withTimeout(10_000L) {
                while (viewModel.uiState.isLoadingSettings || !viewModel.uiState.hasCompletedOnboarding) {
                    delay(50)
                }
            }

            viewModel.triggerDebugIntervention(nowMillis = 2_000L)
            val recommendationSet = withTimeout(10_000L) {
                var current: RecommendationSet?
                do {
                    current = viewModel.uiState.currentRecommendationSet
                    if (current == null) delay(50)
                } while (current == null)
                current
            }
            val recommendedIds = listOf(recommendationSet.primary) + recommendationSet.backups
            assertTrue(recommendedIds.any { it.id == saved.item.id })

            val savedRecommendation = recommendedIds.first { it.id == saved.item.id }
            if (recommendationSet.primary.id == saved.item.id) {
                viewModel.acceptPrimary()
            } else {
                viewModel.acceptBackup(savedRecommendation)
            }

            withTimeout(10_000L) {
                while (viewModel.uiState.screen != MainScreen.ExternalHandoff) {
                    delay(50)
                }
            }

            assertEquals(saved.item.id, viewModel.uiState.currentContent?.id)
            assertEquals("https://example.com/focused-essay", viewModel.currentExternalLinkUrl())
        } finally {
            viewModel.closeForTests()
        }
    }
}
