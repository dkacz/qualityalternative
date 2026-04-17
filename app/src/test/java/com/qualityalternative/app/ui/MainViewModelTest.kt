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
import com.qualityalternative.app.domain.model.TopicTag
import com.qualityalternative.app.domain.service.ContentRepository
import com.qualityalternative.app.domain.service.DefaultRecommendationEngine
import com.qualityalternative.app.domain.service.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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
        val viewModel = MainViewModel(
            contentRepository = FakeContentRepository(),
            settingsRepository = settingsRepository,
            recommendationEngine = DefaultRecommendationEngine(),
            delayGate = InMemoryDelayGate(),
            analyticsTracker = InMemoryAnalyticsTracker(),
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
}
