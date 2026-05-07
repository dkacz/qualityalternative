package com.qualityalternative.app.ui

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import com.qualityalternative.app.analytics.InMemoryAnalyticsTracker
import com.qualityalternative.app.data.AccountLightProfileExporter
import com.qualityalternative.app.data.CompositeContentRepository
import com.qualityalternative.app.data.InMemoryDelayGate
import com.qualityalternative.app.data.PreferencesDelayGate
import com.qualityalternative.app.data.ReadingTimeEstimateSource
import com.qualityalternative.app.data.SupportedCatalog
import com.qualityalternative.app.domain.model.AppSettings
import com.qualityalternative.app.domain.model.AnalyticsEventType
import com.qualityalternative.app.domain.model.ContentAvailability
import com.qualityalternative.app.domain.model.AppThemeMode
import com.qualityalternative.app.domain.model.ContentFormat
import com.qualityalternative.app.domain.model.ContentItem
import com.qualityalternative.app.domain.model.ContentPriority
import com.qualityalternative.app.domain.model.ContentRenderMode
import com.qualityalternative.app.domain.model.ContentRightsClass
import com.qualityalternative.app.domain.model.ContentRightsMetadata
import com.qualityalternative.app.domain.model.ContentSourceType
import com.qualityalternative.app.domain.model.DistractingApp
import com.qualityalternative.app.domain.model.DurationBucket
import com.qualityalternative.app.domain.model.EditorialPack
import com.qualityalternative.app.domain.model.MEDITATION_TIMER_CONTENT_ID
import com.qualityalternative.app.domain.model.MeditationTimerContentItem
import com.qualityalternative.app.domain.model.OnboardingSelection
import com.qualityalternative.app.domain.model.PermissionReadiness
import com.qualityalternative.app.domain.model.PermissionStatus
import com.qualityalternative.app.domain.model.RecommendationSet
import com.qualityalternative.app.domain.model.RecommendationSignals
import com.qualityalternative.app.domain.model.RecommendationSource
import com.qualityalternative.app.domain.model.ReadingAnnotation
import com.qualityalternative.app.domain.model.ReadingAnnotationDraft
import com.qualityalternative.app.domain.model.ReadingAnnotationSelector
import com.qualityalternative.app.domain.model.ReadingProgress
import com.qualityalternative.app.domain.model.ReplacementHistoryEntry
import com.qualityalternative.app.domain.model.ReturnToTargetSignal
import com.qualityalternative.app.domain.model.SessionFeedback
import com.qualityalternative.app.domain.model.TopicTag
import com.qualityalternative.app.domain.model.UserLinkDraft
import com.qualityalternative.app.domain.model.UserLinkValidationError
import com.qualityalternative.app.domain.model.UserDocumentDraft
import com.qualityalternative.app.domain.model.UserDocumentValidationError
import com.qualityalternative.app.domain.model.UserPreferences
import com.qualityalternative.app.domain.model.meditationTimerContentItem
import com.qualityalternative.app.domain.service.AccountLightProfileAutosaveWriter
import com.qualityalternative.app.domain.service.AddUserDocumentResult
import com.qualityalternative.app.domain.service.AddUserLinkResult
import com.qualityalternative.app.domain.service.ContentRepository
import com.qualityalternative.app.domain.service.DefaultRecommendationEngine
import com.qualityalternative.app.domain.service.DelayGate
import com.qualityalternative.app.domain.service.HistoryRepository
import com.qualityalternative.app.domain.service.InterceptionMonitor
import com.qualityalternative.app.domain.service.RecommendationEngine
import com.qualityalternative.app.domain.service.ReadingAnnotationRepository
import com.qualityalternative.app.domain.service.ReadingAnnotationDriveSyncClient
import com.qualityalternative.app.domain.service.ReadingAnnotationDriveSyncRequest
import com.qualityalternative.app.domain.service.ReadingAnnotationDriveSyncResult
import com.qualityalternative.app.domain.service.ReadingAnnotationDriveTokenProvider
import com.qualityalternative.app.domain.service.ReadingAnnotationExportFile
import com.qualityalternative.app.domain.service.ReadingAnnotationExportWriter
import com.qualityalternative.app.domain.service.ReadingProgressRepository
import com.qualityalternative.app.domain.service.SettingsRepository
import com.qualityalternative.app.domain.service.UserDocumentRepository
import com.qualityalternative.app.domain.service.UserLinkRepository
import com.qualityalternative.app.interception.FixtureTargetRegistry
import com.qualityalternative.app.interception.InterceptionRuntimeGate
import java.io.ByteArrayInputStream
import java.io.File
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class MainViewModelTest {
    private val createdViewModels = mutableListOf<MainViewModel>()

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    @After
    fun tearDown() {
        createdViewModels.forEach(MainViewModel::closeForTests)
        createdViewModels.clear()
        InterceptionRuntimeGate.clearAll()
    }

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
        assertEquals(4, viewModel.uiState.availableTargetApps.size)
        assertEquals(setOf("philosophy"), viewModel.uiState.preferences?.selectedPackIds)
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun completeOnboarding_selectsAttentionClassicsPackWhenItIsAvailable() = runTest {
        val viewModel = createViewModel(
            contentRepository = FakeContentRepository(includeAttentionClassics = true),
        )

        advanceUntilIdle()
        viewModel.completeOnboarding()
        advanceUntilIdle()
        viewModel.triggerDebugIntervention(nowMillis = 2_000L)
        advanceUntilIdle()

        assertEquals(
            setOf("philosophy", "attention-classics-v1"),
            viewModel.uiState.preferences?.selectedPackIds,
        )
        val shownItems = listOfNotNull(viewModel.uiState.currentRecommendationSet?.primary) +
            viewModel.uiState.currentRecommendationSet?.backups.orEmpty()
        assertTrue(shownItems.any { item -> item.packId == "attention-classics-v1" })
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun completeOnboarding_selectsModernLinkOnlyPackWhenItIsAvailable() = runTest {
        val analyticsTracker = InMemoryAnalyticsTracker()
        val viewModel = createViewModel(
            contentRepository = FakeContentRepository(includeLinkOnlyModern = true),
            recommendationEngine = DefaultRecommendationEngine(),
            analyticsTracker = analyticsTracker,
        )

        advanceUntilIdle()
        viewModel.completeOnboarding()
        advanceUntilIdle()
        viewModel.triggerDebugIntervention(nowMillis = 2_000L)
        advanceUntilIdle()

        assertEquals(
            setOf("philosophy", "link-only-modern-v1"),
            viewModel.uiState.preferences?.selectedPackIds,
        )
        val shownItems = listOfNotNull(viewModel.uiState.currentRecommendationSet?.primary) +
            viewModel.uiState.currentRecommendationSet?.backups.orEmpty()
        val linkOnlyItem = shownItems.first { item -> item.packId == "link-only-modern-v1" }

        viewModel.acceptBackup(linkOnlyItem)
        advanceUntilIdle()

        assertEquals(MainScreen.ExternalHandoff, viewModel.uiState.screen)
        assertEquals("https://longnow.org/ideas/the-big-here-and-long-now/", viewModel.currentExternalLinkUrl())

        viewModel.recordExternalLinkOpened(nowMillis = 3_000L)

        val event = analyticsTracker.allEvents().first {
            it.type == AnalyticsEventType.EXTERNAL_HANDOFF_OPENED &&
                it.contentId == "big-here-long-now"
        }
        assertEquals("EDITORIAL", event.metadata["sourceType"])
        assertEquals("LINK_ONLY", event.metadata["rightsClass"])
        assertEquals("EXTERNAL_HANDOFF", event.metadata["renderMode"])
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun completeOnboarding_selectsAllDefaultSharedPacksWhenAvailable() = runTest {
        val viewModel = createViewModel(
            contentRepository = FakeContentRepository(
                includeAttentionClassics = true,
                includePublicDomainExpansion = true,
                includeLinkOnlyModern = true,
            ),
        )

        advanceUntilIdle()
        viewModel.completeOnboarding()
        advanceUntilIdle()

        assertEquals(
            setOf("philosophy", "attention-classics-v1", "public-domain-expansion-v2", "link-only-modern-v1"),
            viewModel.uiState.preferences?.selectedPackIds,
        )
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun completeOnboarding_selectsSprint9PacksWhenAvailable() = runTest {
        val viewModel = createViewModel(
            contentRepository = FakeContentRepository(includeSprint9Packs = true),
            recommendationEngine = DefaultRecommendationEngine(),
        )

        advanceUntilIdle()
        viewModel.completeOnboarding()
        advanceUntilIdle()
        viewModel.triggerDebugIntervention(nowMillis = 2_000L)
        advanceUntilIdle()

        val expectedSprint9PackIds = setOf(
            "attention_practical_agency_v1",
            "embodied_calm_v1",
            "wonder_science_v1",
            "long_view_history_v1",
            "creativity_play_v1",
        )
        assertTrue(viewModel.uiState.preferences?.selectedPackIds.orEmpty().containsAll(expectedSprint9PackIds))
        val shownItems = listOfNotNull(viewModel.uiState.currentRecommendationSet?.primary) +
            viewModel.uiState.currentRecommendationSet?.backups.orEmpty()
        assertTrue(shownItems.any { item -> item.packId in expectedSprint9PackIds })
    }

    @Test
    fun prototypeTopicsExposeSprint9TopicsWithoutUserFacingOtherBucket() {
        val topics = prototypeTopics()

        assertTrue(topics.containsAll(listOf(
            TopicTag.ATTENTION,
            TopicTag.PRACTICAL,
            TopicTag.BODY,
            TopicTag.NATURE,
            TopicTag.HISTORY_CULTURE,
        )))
        assertFalse(topics.contains(TopicTag.OTHER))
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun selectThemeMode_persistsAndUpdatesUiState() = runTest {
        val settingsRepository = FakeSettingsRepository()
        val viewModel = createViewModel(settingsRepository = settingsRepository)

        advanceUntilIdle()
        assertEquals(AppThemeMode.LIGHT, viewModel.uiState.themeMode)

        viewModel.selectThemeMode(AppThemeMode.DARK)
        advanceUntilIdle()

        assertEquals(AppThemeMode.DARK, viewModel.uiState.themeMode)
        assertEquals(AppThemeMode.DARK, settingsRepository.state.value.themeMode)
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun configuredProfileAutosaveRunsAfterThemeModeChange() = runTest {
        val settingsRepository = FakeSettingsRepository(
            initial = completedSettings(selectedAppPackages = setOf("com.instagram.android")).copy(
                themeMode = AppThemeMode.LIGHT,
                profileAutosaveUri = "content://tree/profile-folder",
                profileAutosaveDisplayName = "QA profile",
            ),
        )
        val profileWriter = RecordingAccountLightProfileAutosaveWriter()
        val viewModel = createViewModel(
            settingsRepository = settingsRepository,
            accountLightProfileAutosaveWriter = profileWriter,
        )

        advanceUntilIdle()
        viewModel.selectThemeMode(AppThemeMode.DARK)
        advanceUntilIdle()

        assertEquals(AppThemeMode.DARK, settingsRepository.state.value.themeMode)
        val write = profileWriter.writes.single()
        assertEquals("content://tree/profile-folder", write.first)
        assertEquals("quality-alternative-profile.json", write.second)
        assertTrue(write.third.contains("\"themeMode\": \"DARK\""))
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun hydrationWaitsForContentRepositoryReadiness() = runTest {
        val contentRepository = FakeContentRepository(isReady = false)
        val viewModel = createViewModel(contentRepository = contentRepository)

        advanceUntilIdle()
        viewModel.completeOnboarding()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.isLoadingSettings)

        contentRepository.setReady(true)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.isLoadingSettings)
        assertEquals(MainScreen.Home, viewModel.uiState.screen)
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun interventionInventoryIncludesUserLinksOnceFallbackFlowExists() = runTest {
        val userLink = savedUserLink()
        val contentRepository = FakeContentRepository(extraItems = listOf(userLink))
        val recommendationEngine = RecordingRecommendationEngine()
        val viewModel = createViewModel(
            contentRepository = contentRepository,
            recommendationEngine = recommendationEngine,
        )

        advanceUntilIdle()
        viewModel.completeOnboarding()
        advanceUntilIdle()
        viewModel.triggerDebugIntervention(nowMillis = 1_000L)
        advanceUntilIdle()

        assertEquals(
            setOf("p1", "user-link", MEDITATION_TIMER_CONTENT_ID),
            recommendationEngine.lastInventory.mapTo(mutableSetOf(), ContentItem::id),
        )
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun acceptingUserLinkRoutesToExternalHandoffAndRecordsFallbackOpen() = runTest {
        val userLink = savedUserLink()
        val analyticsTracker = InMemoryAnalyticsTracker()
        val viewModel = createViewModel(
            contentRepository = FakeContentRepository(extraItems = listOf(userLink)),
            recommendationEngine = FixedRecommendationEngine(
                RecommendationSet(
                    primary = userLink,
                    backups = emptyList(),
                    inventoryShortage = true,
                    generatedAtMillis = 1_000L,
                ),
            ),
            analyticsTracker = analyticsTracker,
        )

        advanceUntilIdle()
        viewModel.completeOnboarding()
        advanceUntilIdle()
        viewModel.triggerDebugIntervention(nowMillis = 1_000L)
        advanceUntilIdle()
        viewModel.acceptPrimary()
        advanceUntilIdle()

        assertEquals(MainScreen.ExternalHandoff, viewModel.uiState.screen)
        assertEquals("https://example.com/essay", viewModel.currentExternalLinkUrl())
        assertFalse(analyticsTracker.allEvents().any { it.type == AnalyticsEventType.USER_LINK_FALLBACK_OPENED })

        viewModel.recordExternalLinkOpened(nowMillis = 2_000L)

        val event = analyticsTracker.allEvents().first {
            it.type == AnalyticsEventType.USER_LINK_FALLBACK_OPENED
        }
        assertEquals(userLink.id, event.contentId)
        assertEquals("USER_LINK", event.metadata["sourceType"])
        assertEquals("NEEDS_FALLBACK", event.metadata["availability"])
        assertEquals("https://example.com/essay", event.metadata["externalUrl"])
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun acceptingSharedLinkOnlyRoutesToExternalHandoffAndRecordsGenericOpen() = runTest {
        val linkOnly = sharedLinkOnlyItem()
        val analyticsTracker = InMemoryAnalyticsTracker()
        val viewModel = createViewModel(
            contentRepository = FakeContentRepository(extraItems = listOf(linkOnly)),
            recommendationEngine = FixedRecommendationEngine(
                RecommendationSet(
                    primary = linkOnly,
                    backups = emptyList(),
                    inventoryShortage = true,
                    generatedAtMillis = 1_000L,
                ),
            ),
            analyticsTracker = analyticsTracker,
        )

        advanceUntilIdle()
        viewModel.completeOnboarding()
        advanceUntilIdle()
        viewModel.triggerDebugIntervention(nowMillis = 1_000L)
        advanceUntilIdle()
        viewModel.acceptPrimary()
        advanceUntilIdle()

        assertEquals(MainScreen.ExternalHandoff, viewModel.uiState.screen)
        assertEquals("https://longnow.org/ideas/the-big-here-and-long-now/", viewModel.currentExternalLinkUrl())
        assertFalse(analyticsTracker.allEvents().any { it.type == AnalyticsEventType.USER_LINK_FALLBACK_OPENED })

        viewModel.recordExternalLinkOpened(nowMillis = 2_000L)

        val event = analyticsTracker.allEvents().first {
            it.type == AnalyticsEventType.EXTERNAL_HANDOFF_OPENED
        }
        assertEquals(linkOnly.id, event.contentId)
        assertEquals("EDITORIAL", event.metadata["sourceType"])
        assertEquals("LINK_ONLY", event.metadata["rightsClass"])
        assertEquals("EXTERNAL_HANDOFF", event.metadata["renderMode"])
        assertEquals("https://longnow.org/ideas/the-big-here-and-long-now/", event.metadata["externalUrl"])
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun acceptingPrivateMarkdownDocumentRoutesToReaderAndRecordsPrivateMetadata() = runTest {
        val document = savedUserDocument(format = ContentFormat.MARKDOWN)
        val analyticsTracker = InMemoryAnalyticsTracker()
        val viewModel = createViewModel(
            contentRepository = FakeContentRepository(extraItems = listOf(document)),
            recommendationEngine = FixedRecommendationEngine(
                RecommendationSet(
                    primary = document,
                    backups = emptyList(),
                    inventoryShortage = true,
                    generatedAtMillis = 1_000L,
                ),
            ),
            analyticsTracker = analyticsTracker,
        )

        advanceUntilIdle()
        viewModel.completeOnboarding()
        advanceUntilIdle()
        viewModel.triggerDebugIntervention(nowMillis = 1_000L)
        advanceUntilIdle()
        viewModel.acceptPrimary()
        advanceUntilIdle()

        assertEquals(MainScreen.Reader, viewModel.uiState.screen)
        assertEquals("Private notes", viewModel.uiState.currentContentBody)
        val accepted = analyticsTracker.allEvents().first { it.type == AnalyticsEventType.PRIMARY_ACCEPTED }
        assertEquals("USER_DOCUMENT", accepted.metadata["sourceType"])
        assertEquals("USER_PRIVATE", accepted.metadata["rightsClass"])
        assertEquals("USER_PRIVATE_READER", accepted.metadata["renderMode"])
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun acceptingPrivatePdfDocumentRoutesToExternalHandoffAndRecordsGenericOpen() = runTest {
        val document = savedUserDocument(format = ContentFormat.PDF)
        val analyticsTracker = InMemoryAnalyticsTracker()
        val viewModel = createViewModel(
            contentRepository = FakeContentRepository(extraItems = listOf(document)),
            recommendationEngine = FixedRecommendationEngine(
                RecommendationSet(
                    primary = document,
                    backups = emptyList(),
                    inventoryShortage = true,
                    generatedAtMillis = 1_000L,
                ),
            ),
            analyticsTracker = analyticsTracker,
        )

        advanceUntilIdle()
        viewModel.completeOnboarding()
        advanceUntilIdle()
        viewModel.triggerDebugIntervention(nowMillis = 1_000L)
        advanceUntilIdle()
        viewModel.acceptPrimary()
        advanceUntilIdle()

        assertEquals(MainScreen.ExternalHandoff, viewModel.uiState.screen)
        assertEquals("content://quality/document.pdf", viewModel.currentExternalLinkUrl())
        assertEquals("application/pdf", viewModel.currentExternalContentMimeType())

        viewModel.recordExternalLinkOpened(nowMillis = 2_000L)

        val event = analyticsTracker.allEvents().first {
            it.type == AnalyticsEventType.EXTERNAL_HANDOFF_OPENED
        }
        assertEquals(document.id, event.contentId)
        assertEquals("USER_DOCUMENT", event.metadata["sourceType"])
        assertEquals("EXTERNAL_HANDOFF", event.metadata["renderMode"])
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun acceptingMeditationRoutesToTimerAndCompletionRecordsMeditationEvent() = runTest {
        val analyticsTracker = InMemoryAnalyticsTracker()
        val historyRepository = FakeHistoryRepository()
        val viewModel = createViewModel(
            recommendationEngine = FixedRecommendationEngine(
                RecommendationSet(
                    primary = MeditationTimerContentItem,
                    backups = emptyList(),
                    inventoryShortage = true,
                    generatedAtMillis = 1_000L,
                ),
            ),
            analyticsTracker = analyticsTracker,
            historyRepository = historyRepository,
        )

        advanceUntilIdle()
        viewModel.completeOnboarding()
        advanceUntilIdle()
        viewModel.triggerDebugIntervention(nowMillis = 1_000L)
        advanceUntilIdle()
        viewModel.acceptPrimary()
        advanceUntilIdle()

        assertEquals(MainScreen.MeditationTimer, viewModel.uiState.screen)
        assertEquals(MEDITATION_TIMER_CONTENT_ID, viewModel.uiState.currentContent?.id)

        viewModel.finishMeditationReset(nowMillis = System.currentTimeMillis() + 3 * 60_000L)
        advanceUntilIdle()

        assertEquals(MainScreen.Feedback, viewModel.uiState.screen)
        val completedEvent = analyticsTracker.allEvents().first {
            it.type == AnalyticsEventType.MEDITATION_TIMER_COMPLETED
        }
        assertEquals(MEDITATION_TIMER_CONTENT_ID, completedEvent.contentId)
        assertEquals("MEDITATION", completedEvent.metadata["sourceType"])
        assertEquals("MEDITATION_TIMER", completedEvent.metadata["renderMode"])
        assertFalse(historyRepository.historyEntries.value.single().isCompleted())
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun meditationDurationSettingUpdatesUtilityReplacementInventory() = runTest {
        val recommendationEngine = RecordingRecommendationEngine()
        val settingsRepository = FakeSettingsRepository()
        val viewModel = createViewModel(
            settingsRepository = settingsRepository,
            recommendationEngine = recommendationEngine,
        )

        advanceUntilIdle()
        viewModel.completeOnboarding()
        advanceUntilIdle()

        viewModel.setMeditationDurationMinutes(5)
        advanceUntilIdle()
        viewModel.triggerDebugIntervention(nowMillis = 2_000L)
        advanceUntilIdle()

        val meditation = recommendationEngine.lastInventory.first { it.id == MEDITATION_TIMER_CONTENT_ID }
        assertEquals(5, meditation.durationMinutes)
        assertEquals("5-minute reset", meditation.title)
        assertEquals(5, settingsRepository.state.value.meditationDurationMinutes)
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun readerFontScaleSettingPersistsAndAutosavesPortableProfile() = runTest {
        val settingsRepository = FakeSettingsRepository(
            initial = completedSettings(selectedAppPackages = setOf("feed.one")).copy(
                profileAutosaveUri = "content://tree/profile-folder",
                profileAutosaveDisplayName = "QA profile",
            ),
        )
        val profileWriter = RecordingAccountLightProfileAutosaveWriter()
        val viewModel = createViewModel(
            settingsRepository = settingsRepository,
            accountLightProfileAutosaveWriter = profileWriter,
            nowProvider = { 4_000L },
        )

        advanceUntilIdle()
        viewModel.setReaderFontScale(1.3)
        advanceUntilIdle()

        assertEquals(1.3, viewModel.uiState.readerFontScale, 0.0)
        assertEquals(1.3, settingsRepository.state.value.readerFontScale, 0.0)
        assertEquals(1, profileWriter.writes.size)
        assertTrue(profileWriter.writes.single().third.contains("\"readerFontScale\": 1.3"))
        assertEquals(4_000L, settingsRepository.state.value.profileAutosaveLastSuccessfulAtMillis)
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun readerFontScaleSettingClampsToPortableRange() = runTest {
        val settingsRepository = FakeSettingsRepository(
            initial = completedSettings(selectedAppPackages = setOf("feed.one")),
        )
        val viewModel = createViewModel(settingsRepository = settingsRepository)

        advanceUntilIdle()
        viewModel.setReaderFontScale(4.0)
        advanceUntilIdle()

        assertEquals(1.6, viewModel.uiState.readerFontScale, 0.0)
        assertEquals(1.6, settingsRepository.state.value.readerFontScale, 0.0)

        viewModel.setReaderFontScale(0.1)
        advanceUntilIdle()

        assertEquals(0.8, viewModel.uiState.readerFontScale, 0.0)
        assertEquals(0.8, settingsRepository.state.value.readerFontScale, 0.0)
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun interfaceTextScaleSettingPersistsAndAutosavesPortableProfile() = runTest {
        val settingsRepository = FakeSettingsRepository(
            initial = completedSettings(selectedAppPackages = setOf("feed.one")).copy(
                profileAutosaveUri = "content://tree/profile-folder",
                profileAutosaveDisplayName = "QA profile",
            ),
        )
        val profileWriter = RecordingAccountLightProfileAutosaveWriter()
        val viewModel = createViewModel(
            settingsRepository = settingsRepository,
            accountLightProfileAutosaveWriter = profileWriter,
            nowProvider = { 4_500L },
        )

        advanceUntilIdle()
        viewModel.setInterfaceTextScale(1.2)
        advanceUntilIdle()

        assertEquals(1.2, viewModel.uiState.interfaceTextScale, 0.0)
        assertEquals(1.2, settingsRepository.state.value.interfaceTextScale, 0.0)
        assertEquals(1, profileWriter.writes.size)
        assertTrue(profileWriter.writes.single().third.contains("\"interfaceTextScale\": 1.2"))
        assertEquals(4_500L, settingsRepository.state.value.profileAutosaveLastSuccessfulAtMillis)
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun interfaceTextScaleSettingClampsToPortableRange() = runTest {
        val settingsRepository = FakeSettingsRepository(
            initial = completedSettings(selectedAppPackages = setOf("feed.one")),
        )
        val viewModel = createViewModel(settingsRepository = settingsRepository)

        advanceUntilIdle()
        viewModel.setInterfaceTextScale(2.0)
        advanceUntilIdle()

        assertEquals(1.3, viewModel.uiState.interfaceTextScale, 0.0)
        assertEquals(1.3, settingsRepository.state.value.interfaceTextScale, 0.0)

        viewModel.setInterfaceTextScale(0.1)
        advanceUntilIdle()

        assertEquals(0.9, viewModel.uiState.interfaceTextScale, 0.0)
        assertEquals(0.9, settingsRepository.state.value.interfaceTextScale, 0.0)
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun meditationDurationCanBeChangedBeforeStartingCurrentMeditation() = runTest {
        val recommendationEngine = FixedRecommendationEngine(
            RecommendationSet(
                primary = MeditationTimerContentItem,
                backups = emptyList(),
                inventoryShortage = true,
                generatedAtMillis = 1_000L,
            ),
        )
        val settingsRepository = FakeSettingsRepository()
        val viewModel = createViewModel(
            settingsRepository = settingsRepository,
            recommendationEngine = recommendationEngine,
        )

        advanceUntilIdle()
        viewModel.completeOnboarding()
        advanceUntilIdle()
        viewModel.triggerDebugIntervention(nowMillis = 1_000L)
        advanceUntilIdle()

        viewModel.setMeditationDurationMinutes(10)
        advanceUntilIdle()
        viewModel.acceptPrimary()
        advanceUntilIdle()

        assertEquals(MainScreen.MeditationTimer, viewModel.uiState.screen)
        assertEquals(10, viewModel.uiState.currentContent?.durationMinutes)
        assertEquals("10-minute reset", viewModel.uiState.currentContent?.title)
        assertEquals(10, settingsRepository.state.value.meditationDurationMinutes)
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun meditationDurationChangeOnTimerKeepsAcceptedSessionConsistent() = runTest {
        var nowMillis = 4_000L
        val historyRepository = FakeHistoryRepository()
        val recommendationEngine = FixedRecommendationEngine(
            RecommendationSet(
                primary = MeditationTimerContentItem,
                backups = emptyList(),
                inventoryShortage = true,
                generatedAtMillis = 1_000L,
            ),
        )
        val settingsRepository = FakeSettingsRepository()
        val viewModel = createViewModel(
            settingsRepository = settingsRepository,
            historyRepository = historyRepository,
            recommendationEngine = recommendationEngine,
            nowProvider = { nowMillis },
        )

        advanceUntilIdle()
        viewModel.completeOnboarding()
        advanceUntilIdle()
        viewModel.triggerDebugIntervention(nowMillis = 1_000L)
        advanceUntilIdle()
        viewModel.acceptPrimary()
        advanceUntilIdle()

        assertEquals(MainScreen.MeditationTimer, viewModel.uiState.screen)
        assertEquals("3-minute reset", historyRepository.historyEntries.value.single().contentTitle)

        nowMillis = 9_000L
        viewModel.setMeditationDurationMinutes(5)
        advanceUntilIdle()

        assertEquals(5, viewModel.uiState.currentContent?.durationMinutes)
        assertEquals("5-minute reset", viewModel.uiState.currentContent?.title)
        assertEquals(9_000L, viewModel.uiState.currentSessionStartedAtMillis)
        assertEquals(5, settingsRepository.state.value.meditationDurationMinutes)
        assertEquals("5-minute reset", historyRepository.historyEntries.value.single().contentTitle)
        assertEquals(5, historyRepository.historyEntries.value.single().contentDurationMinutes)
        assertEquals("A quiet timer for breathing through the impulse before choosing what comes next.", historyRepository.historyEntries.value.single().contentDescription)
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun setContentPriorityUpdatesSettingsAndRecommendationPreferences() = runTest {
        val recommendationEngine = RecordingRecommendationEngine()
        val settingsRepository = FakeSettingsRepository()
        val viewModel = createViewModel(
            settingsRepository = settingsRepository,
            recommendationEngine = recommendationEngine,
        )

        advanceUntilIdle()
        viewModel.completeOnboarding()
        advanceUntilIdle()
        viewModel.setContentPriority(ContentPriority.MY_FILES)
        advanceUntilIdle()
        viewModel.triggerDebugIntervention(nowMillis = 2_000L)
        advanceUntilIdle()

        assertEquals(ContentPriority.MY_FILES, viewModel.uiState.contentPriority)
        assertEquals(ContentPriority.MY_FILES, settingsRepository.state.value.contentPriority)
        assertEquals(ContentPriority.MY_FILES, recommendationEngine.lastPreferences?.contentPriority)
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun togglePriorityContentUpdatesSettingsRecommendationPreferencesAndAnalytics() = runTest {
        val recommendationEngine = RecordingRecommendationEngine()
        val analyticsTracker = InMemoryAnalyticsTracker()
        val settingsRepository = FakeSettingsRepository()
        val contentRepository = FakeContentRepository()
        val priorityItem = contentRepository.inventory().first()
        val viewModel = createViewModel(
            settingsRepository = settingsRepository,
            contentRepository = contentRepository,
            recommendationEngine = recommendationEngine,
            analyticsTracker = analyticsTracker,
        )

        advanceUntilIdle()
        viewModel.completeOnboarding()
        advanceUntilIdle()
        viewModel.togglePriorityContent(priorityItem)
        advanceUntilIdle()
        viewModel.triggerDebugIntervention(nowMillis = 2_000L)
        advanceUntilIdle()

        assertTrue(priorityItem.id in viewModel.uiState.priorityContentIds)
        assertEquals(setOf(priorityItem.id), settingsRepository.state.value.priorityContentIds)
        assertEquals(setOf(priorityItem.id), recommendationEngine.lastPreferences?.priorityContentIds)
        val event = analyticsTracker.allEvents().first { it.type == AnalyticsEventType.PRIORITY_CONTENT_TOGGLED }
        assertEquals(priorityItem.id, event.contentId)
        assertEquals("true", event.metadata["priorityEnabled"])

        viewModel.togglePriorityContent(priorityItem)
        advanceUntilIdle()

        assertFalse(priorityItem.id in viewModel.uiState.priorityContentIds)
        assertEquals(emptySet<String>(), settingsRepository.state.value.priorityContentIds)
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun deleteSelectedLibraryContentDeletesUserItemsCleansPriorityReleasesDocumentPermissionAndExcludesRecommendations() = runTest {
        val userLink = savedUserLink(id = "user-link:delete")
        val userDocument = savedUserDocument(format = ContentFormat.MARKDOWN)
        val readingProgressRepository = FakeReadingProgressRepository(
            initialProgress = listOf(
                ReadingProgress(
                    contentId = userLink.id,
                    progressPercent = 35,
                    lastVisibleParagraphIndex = 2,
                    paragraphCount = 8,
                    updatedAtMillis = 1_500L,
                ),
                ReadingProgress(
                    contentId = userDocument.id,
                    progressPercent = 60,
                    lastVisibleParagraphIndex = 4,
                    paragraphCount = 10,
                    updatedAtMillis = 1_600L,
                ),
            ),
        )
        val userLinkRepository = FakeUserLinkRepository(initialLinks = listOf(userLink))
        val userDocumentRepository = FakeUserDocumentRepository(initialDocuments = listOf(userDocument))
        val readingAnnotationRepository = FakeReadingAnnotationRepository(
            initialAnnotations = listOf(
                ReadingAnnotation(
                    id = "deleted-document-note",
                    contentId = userDocument.id,
                    paragraphIndex = 0,
                    quotedText = "Private notes",
                    noteText = "Remove when the source is deleted.",
                    createdAtMillis = 1_400L,
                    updatedAtMillis = 1_700L,
                ),
            ),
        )
        val exportWriter = RecordingReadingAnnotationExportWriter()
        val editorialRepository = FakeContentRepository()
        val contentRepository = CompositeContentRepository(
            editorialRepository = editorialRepository,
            userLinkRepository = userLinkRepository,
            userDocumentRepository = userDocumentRepository,
        )
        val recommendationEngine = RecordingRecommendationEngine()
        val analyticsTracker = InMemoryAnalyticsTracker()
        val settingsRepository = FakeSettingsRepository(
            initial = completedSettings(
                selectedAppPackages = setOf(FixtureTargetRegistry.fixtureDistractors.first().packageName),
            ).copy(
                priorityContentIds = setOf(userLink.id, userDocument.id, "p1"),
                annotationExportUri = "content://drive/qa-annotations.jsonld",
                annotationExportDisplayName = "qa-annotations.jsonld",
            ),
        )
        val releasedUris = mutableListOf<String>()
        val viewModel = createViewModel(
            settingsRepository = settingsRepository,
            contentRepository = contentRepository,
            userLinkRepository = userLinkRepository,
            userDocumentRepository = userDocumentRepository,
            recommendationEngine = recommendationEngine,
            analyticsTracker = analyticsTracker,
            readingProgressRepository = readingProgressRepository,
            readingAnnotationRepository = readingAnnotationRepository,
            readingAnnotationExportWriter = exportWriter,
        )

        advanceUntilIdle()
        viewModel.openLibrary()
        viewModel.toggleLibraryManageMode()
        viewModel.toggleLibraryContentSelection(userLink)
        viewModel.toggleLibraryContentSelection(userDocument)
        advanceUntilIdle()

        assertEquals(setOf(userLink.id, userDocument.id), viewModel.uiState.selectedLibraryContentIds)

        viewModel.deleteSelectedLibraryContent(
            nowMillis = 2_000L,
            releaseDocumentPermission = releasedUris::add,
        )
        advanceUntilIdle()
        viewModel.triggerDebugIntervention(nowMillis = 3_000L)
        advanceUntilIdle()

        assertEquals(listOf(userLink.id), userLinkRepository.deletedIds)
        assertEquals(listOf(userDocument.id), userDocumentRepository.deletedIds)
        assertEquals(emptyList<ContentItem>(), userLinkRepository.links.value)
        assertEquals(emptyList<ContentItem>(), userDocumentRepository.documents.value)
        assertEquals(emptyList<ContentItem>(), viewModel.uiState.userLinks)
        assertEquals(emptyList<ContentItem>(), viewModel.uiState.userDocuments)
        assertEquals(setOf(userLink.id, userDocument.id), readingProgressRepository.deletedIds)
        assertEquals(emptyList<ReadingProgress>(), viewModel.uiState.readingProgress)
        assertEquals(emptyList<ReadingAnnotation>(), readingAnnotationRepository.readingAnnotations())
        assertEquals(emptyList<ReadingAnnotationExportFile>(), exportWriter.jsonWrites.last().second)
        assertEquals(listOf("content://quality/notes.md"), releasedUris)
        assertEquals(setOf("p1"), viewModel.uiState.priorityContentIds)
        assertEquals(setOf("p1"), settingsRepository.state.value.priorityContentIds)
        assertFalse(viewModel.uiState.isManagingLibrary)
        assertEquals(emptySet<String>(), viewModel.uiState.selectedLibraryContentIds)
        assertFalse(recommendationEngine.lastInventory.any { item -> item.id == userLink.id || item.id == userDocument.id })
        assertFalse(viewModel.uiState.preferences?.unfinishedContentIds.orEmpty().any { it == userLink.id || it == userDocument.id })

        val deletedEvents = analyticsTracker.allEvents().filter { it.type == AnalyticsEventType.USER_CONTENT_DELETED }
        assertEquals(2, deletedEvents.size)
        assertTrue(deletedEvents.any { event ->
            event.contentId == userLink.id && event.metadata["sourceType"] == "USER_LINK"
        })
        assertTrue(deletedEvents.any { event ->
            event.contentId == userDocument.id && event.metadata["sourceType"] == "USER_DOCUMENT"
        })
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun manualLibraryReadingSavesRestoresAndCompletesProgressWithoutInterventionSession() = runTest {
        val document = savedUserDocument(format = ContentFormat.MARKDOWN)
        val readingProgressRepository = FakeReadingProgressRepository()
        val analyticsTracker = InMemoryAnalyticsTracker()
        val viewModel = createViewModel(
            contentRepository = FakeContentRepository(extraItems = listOf(document)),
            readingProgressRepository = readingProgressRepository,
            analyticsTracker = analyticsTracker,
            nowProvider = { 5_000L },
        )

        advanceUntilIdle()
        viewModel.completeOnboarding()
        advanceUntilIdle()
        viewModel.openLibraryItem(document)
        advanceUntilIdle()

        assertEquals(MainScreen.Reader, viewModel.uiState.screen)
        assertEquals(null, viewModel.uiState.currentSessionId)

        viewModel.saveCurrentReadingProgress(
            progressPercent = 42,
            lastVisibleParagraphIndex = 3,
            paragraphCount = 9,
            nowMillis = 5_100L,
        )
        advanceUntilIdle()

        assertEquals(42, readingProgressRepository.progress.value.single().progressPercent)
        assertTrue(analyticsTracker.allEvents().any { event -> event.type == AnalyticsEventType.READING_PROGRESS_SAVED })

        viewModel.skipReading()
        advanceUntilIdle()
        viewModel.openLibraryItem(document)
        advanceUntilIdle()

        assertEquals(42, viewModel.uiState.currentReadingProgress?.progressPercent)
        val continueEvent = analyticsTracker.allEvents().first { it.type == AnalyticsEventType.MANUAL_CONTINUE_STARTED }
        assertEquals(document.id, continueEvent.contentId)
        assertEquals("library", continueEvent.metadata["origin"])

        viewModel.finishReading()
        advanceUntilIdle()

        assertTrue(readingProgressRepository.progress.value.single().isCompleted())
        assertTrue(document.id in viewModel.uiState.completedContentIds)
        val completedEvent = analyticsTracker.allEvents().last { it.type == AnalyticsEventType.READER_COMPLETED }
        assertEquals(document.id, completedEvent.contentId)
        assertEquals(null, completedEvent.sessionId)
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun missingPortableDocumentDoesNotOpenFromLibrary() = runTest {
        val document = savedUserDocument(format = ContentFormat.MARKDOWN).copy(
            availability = ContentAvailability.UNAVAILABLE,
            sourceLabel = "book.epub (missing)",
            rights = ContentRightsMetadata.userPrivateReader(
                sourceUrl = "portable-missing:user-document-44444444-4444-4444-8444-444444444444",
                attribution = "book.epub",
            ),
        )
        val analyticsTracker = InMemoryAnalyticsTracker()
        val viewModel = createViewModel(
            contentRepository = FakeContentRepository(extraItems = listOf(document)),
            analyticsTracker = analyticsTracker,
        )

        advanceUntilIdle()
        viewModel.completeOnboarding()
        advanceUntilIdle()
        viewModel.openLibraryItem(document)
        advanceUntilIdle()

        assertEquals(MainScreen.Home, viewModel.uiState.screen)
        assertEquals("Reattach this file before reading can continue.", viewModel.uiState.latestMessage)
        assertEquals(null, viewModel.uiState.currentContent)
        assertTrue(analyticsTracker.allEvents().filter { event ->
            event.contentId == document.id
        }.isEmpty())
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun openAnnotationTargetOpensReaderAtSavedParagraph() = runTest {
        val document = savedUserDocument(format = ContentFormat.MARKDOWN)
        val selector = ReadingAnnotationSelector(
            sourceBlockIndex = 0,
            textStartOffset = 3,
            textEndOffset = 16,
            prefixText = "Pri",
            suffixText = "otes",
        )
        val annotationRepository = FakeReadingAnnotationRepository(
            initialAnnotations = listOf(
                ReadingAnnotation(
                    id = "annotation-target",
                    contentId = document.id,
                    paragraphIndex = 2,
                    quotedText = "Final paragraph",
                    noteText = "Return here from the library.",
                    createdAtMillis = 1_500L,
                    updatedAtMillis = 2_500L,
                    selector = selector,
                ),
            ),
        )
        val viewModel = createViewModel(
            settingsRepository = FakeSettingsRepository(initial = completedSettings(selectedAppPackages = setOf("feed.one"))),
            contentRepository = FakeContentRepository(extraItems = listOf(document)),
            readingAnnotationRepository = annotationRepository,
        )

        advanceUntilIdle()
        viewModel.openAnnotationLibrary()
        viewModel.openAnnotationTarget("annotation-target")
        advanceUntilIdle()

        assertEquals(MainScreen.Reader, viewModel.uiState.screen)
        assertEquals(document.id, viewModel.uiState.currentContent?.id)
        assertEquals("Private notes", viewModel.uiState.currentContentBody)
        assertEquals(2, viewModel.uiState.currentReaderStartParagraphIndex)
        assertEquals(selector, viewModel.uiState.currentReaderStartSelector)
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun openAnnotationTargetWithMissingSourceStaysInAnnotationLibrary() = runTest {
        val annotationRepository = FakeReadingAnnotationRepository(
            initialAnnotations = listOf(
                ReadingAnnotation(
                    id = "missing-source-annotation",
                    contentId = "deleted-content",
                    paragraphIndex = 4,
                    quotedText = "Old quote",
                    noteText = "Old note",
                    createdAtMillis = 1_500L,
                    updatedAtMillis = 2_500L,
                ),
            ),
        )
        val viewModel = createViewModel(
            settingsRepository = FakeSettingsRepository(initial = completedSettings(selectedAppPackages = setOf("feed.one"))),
            readingAnnotationRepository = annotationRepository,
        )

        advanceUntilIdle()
        viewModel.openAnnotationLibrary()
        viewModel.openAnnotationTarget("missing-source-annotation")
        advanceUntilIdle()

        assertEquals(MainScreen.Annotations, viewModel.uiState.screen)
        assertEquals(null, viewModel.uiState.currentContent)
        assertEquals("The source for that annotation is no longer in Library.", viewModel.uiState.latestMessage)
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun configuredAnnotationExportAutosavesSavedReaderNotes() = runTest {
        val document = savedUserDocument(format = ContentFormat.MARKDOWN)
        val settingsRepository = FakeSettingsRepository(
            initial = completedSettings(selectedAppPackages = setOf("feed.one")),
        )
        val annotationRepository = FakeReadingAnnotationRepository()
        val exportWriter = RecordingReadingAnnotationExportWriter()
        val persistedUris = mutableListOf<String>()
        val viewModel = createViewModel(
            settingsRepository = settingsRepository,
            contentRepository = FakeContentRepository(extraItems = listOf(document)),
            readingAnnotationRepository = annotationRepository,
            readingAnnotationExportWriter = exportWriter,
            nowProvider = { 2_000L },
        )

        advanceUntilIdle()
        viewModel.configureReadingAnnotationExport(
            uri = "content://drive/qa-annotations.jsonld",
            displayName = "qa-annotations.jsonld",
            persistWritePermission = persistedUris::add,
            nowMillis = 2_500L,
        )
        advanceUntilIdle()
        viewModel.openLibraryItem(document)
        advanceUntilIdle()
        viewModel.saveCurrentReadingAnnotation(
            paragraphIndex = 1,
            quotedText = "Private notes",
            noteText = "This belongs in the intervention notes library.",
            nowMillis = 3_000L,
        )
        advanceUntilIdle()

        assertEquals(listOf("content://drive/qa-annotations.jsonld"), persistedUris)
        assertEquals("content://drive/qa-annotations.jsonld", settingsRepository.state.value.annotationExportUri)
        assertEquals("qa-annotations.jsonld", settingsRepository.state.value.annotationExportDisplayName)
        assertEquals(3_000L, settingsRepository.state.value.annotationExportLastSuccessfulAtMillis)
        assertEquals(null, settingsRepository.state.value.annotationExportLastError)
        assertEquals("Annotation saved and autosaved.", viewModel.uiState.latestMessage)
        assertEquals("content://drive/qa-annotations.jsonld", exportWriter.writes.last().first)
        assertEquals("Saved document", exportWriter.jsonWrites.last().second.single().sourceTitle)
        assertTrue(exportWriter.jsonWrites.last().second.single().fileName.contains("saved-document"))
        assertTrue(exportWriter.writes.last().second.contains("AnnotationCollection"))
        assertTrue(exportWriter.writes.last().second.contains("Private notes"))
        assertTrue(exportWriter.writes.last().second.contains("This belongs in the intervention notes library."))
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun googleDriveFolderProviderFallbackConnectsAnnotationExportWithoutOAuth() = runTest {
        val document = savedUserDocument(format = ContentFormat.MARKDOWN)
        val driveFolderUri = "content://com.google.android.apps.docs.storage/document/tree%3Aqa-annotations"
        val settingsRepository = FakeSettingsRepository(
            initial = completedSettings(selectedAppPackages = setOf("feed.one")).copy(
                annotationDriveLastError =
                    "Authorization was cancelled or blocked by Google. No folder destination was changed.",
            ),
        )
        val annotationRepository = FakeReadingAnnotationRepository()
        val exportWriter = RecordingReadingAnnotationExportWriter()
        val persistedUris = mutableListOf<String>()
        val viewModel = createViewModel(
            settingsRepository = settingsRepository,
            contentRepository = FakeContentRepository(extraItems = listOf(document)),
            readingAnnotationRepository = annotationRepository,
            readingAnnotationExportWriter = exportWriter,
            nowProvider = { 2_000L },
        )

        advanceUntilIdle()
        viewModel.connectAnnotationDriveFolderProvider(
            uri = driveFolderUri,
            displayName = "QA annotations",
            persistWritePermission = persistedUris::add,
            nowMillis = 2_500L,
        )
        advanceUntilIdle()
        viewModel.openLibraryItem(document)
        advanceUntilIdle()
        viewModel.saveCurrentReadingAnnotation(
            paragraphIndex = 1,
            quotedText = "Private notes",
            noteText = "Drive folder provider fallback writes this note.",
            nowMillis = 3_000L,
        )
        advanceUntilIdle()

        assertEquals(listOf(driveFolderUri), persistedUris)
        assertEquals(driveFolderUri, settingsRepository.state.value.annotationExportUri)
        assertEquals("QA annotations", settingsRepository.state.value.annotationExportDisplayName)
        assertEquals(false, settingsRepository.state.value.annotationDriveSyncEnabled)
        assertEquals(null, settingsRepository.state.value.annotationDriveLastError)
        assertEquals(3_000L, settingsRepository.state.value.annotationExportLastSuccessfulAtMillis)
        assertEquals("Annotation saved and autosaved.", viewModel.uiState.latestMessage)
        assertEquals(driveFolderUri, exportWriter.writes.last().first)
        assertTrue(exportWriter.writes.last().second.contains("Drive folder provider fallback writes this note."))
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun googleDriveAnnotationSyncConnectsAndAutosavesPerSourceJsonLd() = runTest {
        val document = savedUserDocument(format = ContentFormat.MARKDOWN)
        val settingsRepository = FakeSettingsRepository(
            initial = completedSettings(selectedAppPackages = setOf("feed.one")),
        )
        val annotationRepository = FakeReadingAnnotationRepository()
        val driveClient = RecordingReadingAnnotationDriveSyncClient(folderId = "drive-folder-annotations")
        val analyticsTracker = InMemoryAnalyticsTracker()
        val viewModel = createViewModel(
            settingsRepository = settingsRepository,
            contentRepository = FakeContentRepository(extraItems = listOf(document)),
            readingAnnotationRepository = annotationRepository,
            readingAnnotationDriveSyncClient = driveClient,
            analyticsTracker = analyticsTracker,
            nowProvider = { 2_000L },
        )

        advanceUntilIdle()
        viewModel.connectAnnotationDriveSync(accessToken = "drive-token", nowMillis = 2_500L)
        advanceUntilIdle()
        viewModel.openLibraryItem(document)
        advanceUntilIdle()
        viewModel.saveCurrentReadingAnnotation(
            paragraphIndex = 1,
            quotedText = "Private notes",
            noteText = "Sync this note to Drive.",
            nowMillis = 3_000L,
        )
        advanceUntilIdle()

        assertEquals(true, settingsRepository.state.value.annotationDriveSyncEnabled)
        assertEquals("drive-folder-annotations", settingsRepository.state.value.annotationDriveFolderId)
        assertEquals(3_000L, settingsRepository.state.value.annotationDriveLastSuccessfulAtMillis)
        assertEquals(null, settingsRepository.state.value.annotationDriveLastError)
        assertEquals("Annotation saved and autosaved.", viewModel.uiState.latestMessage)
        assertEquals(2, driveClient.requests.size)
        val latestRequest = driveClient.requests.last()
        assertEquals("drive-token", latestRequest.accessToken)
        assertEquals("drive-folder-annotations", latestRequest.folderId)
        assertEquals("Saved document", latestRequest.files.single().sourceTitle)
        assertTrue(latestRequest.files.single().fileName.endsWith(".annotations.jsonld"))
        assertTrue(latestRequest.files.single().jsonLd.contains("AnnotationCollection"))
        assertTrue(latestRequest.files.single().jsonLd.contains("Sync this note to Drive."))
        assertTrue(analyticsTracker.allEvents().any { event ->
            event.type == AnalyticsEventType.ANNOTATION_DRIVE_SYNC_CONNECTED
        })
        assertTrue(analyticsTracker.allEvents().any { event ->
            event.type == AnalyticsEventType.ANNOTATION_DRIVE_SYNC_SUCCEEDED &&
                event.metadata["sourceFileCount"] == "1"
        })
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun googleDriveAnnotationSyncFailureKeepsLocalNoteAndRecoverableStatus() = runTest {
        val document = savedUserDocument(format = ContentFormat.MARKDOWN)
        val settingsRepository = FakeSettingsRepository(
            initial = completedSettings(selectedAppPackages = setOf("feed.one")),
        )
        val annotationRepository = FakeReadingAnnotationRepository()
        val driveClient = RecordingReadingAnnotationDriveSyncClient(
            failure = IllegalStateException("HTTP 503"),
        )
        val viewModel = createViewModel(
            settingsRepository = settingsRepository,
            contentRepository = FakeContentRepository(extraItems = listOf(document)),
            readingAnnotationRepository = annotationRepository,
            readingAnnotationDriveSyncClient = driveClient,
            nowProvider = { 2_000L },
        )

        advanceUntilIdle()
        viewModel.connectAnnotationDriveSync(accessToken = "drive-token", nowMillis = 2_500L)
        advanceUntilIdle()
        viewModel.openLibraryItem(document)
        advanceUntilIdle()
        viewModel.saveCurrentReadingAnnotation(
            paragraphIndex = 0,
            quotedText = "Private notes",
            noteText = "Local safety is more important than Drive availability.",
            nowMillis = 3_000L,
        )
        advanceUntilIdle()

        assertEquals(1, annotationRepository.readingAnnotations().size)
        assertEquals("Annotation saved. Autosave failed.", viewModel.uiState.latestMessage)
        assertEquals(true, settingsRepository.state.value.annotationDriveSyncEnabled)
        assertEquals("Google Drive sync failed. Retry from Settings.", settingsRepository.state.value.annotationDriveLastError)
        assertEquals("Google Drive sync failed. Retry from Settings.", viewModel.uiState.annotationDriveLastError)
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun googleDriveAnnotationAutosaveRefreshesTokenSilentlyAfterProcessRestart() = runTest {
        val document = savedUserDocument(format = ContentFormat.MARKDOWN)
        val settingsRepository = FakeSettingsRepository(
            initial = completedSettings(selectedAppPackages = setOf("feed.one")).copy(
                annotationDriveSyncEnabled = true,
                annotationDriveFolderId = "drive-folder-annotations",
            ),
        )
        val annotationRepository = FakeReadingAnnotationRepository()
        val driveClient = RecordingReadingAnnotationDriveSyncClient(folderId = "drive-folder-annotations")
        val viewModel = createViewModel(
            settingsRepository = settingsRepository,
            contentRepository = FakeContentRepository(extraItems = listOf(document)),
            readingAnnotationRepository = annotationRepository,
            readingAnnotationDriveSyncClient = driveClient,
            readingAnnotationDriveTokenProvider = StaticReadingAnnotationDriveTokenProvider("silent-token"),
            nowProvider = { 2_000L },
        )

        advanceUntilIdle()
        viewModel.openLibraryItem(document)
        advanceUntilIdle()
        viewModel.saveCurrentReadingAnnotation(
            paragraphIndex = 0,
            quotedText = "Private notes",
            noteText = "A restarted app should still sync when consent already exists.",
            nowMillis = 3_000L,
        )
        advanceUntilIdle()

        assertEquals("silent-token", driveClient.requests.single().accessToken)
        assertEquals("drive-folder-annotations", driveClient.requests.single().folderId)
        assertEquals(3_000L, settingsRepository.state.value.annotationDriveLastSuccessfulAtMillis)
        assertEquals(null, settingsRepository.state.value.annotationDriveLastError)
        assertEquals("Annotation saved and autosaved.", viewModel.uiState.latestMessage)
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun googleDriveAnnotationAutosaveRefreshesTokenSilentlyWhenCachedTokenIsStale() = runTest {
        val document = savedUserDocument(format = ContentFormat.MARKDOWN)
        val settingsRepository = FakeSettingsRepository(
            initial = completedSettings(selectedAppPackages = setOf("feed.one")),
        )
        val annotationRepository = FakeReadingAnnotationRepository()
        val driveClient = RecordingReadingAnnotationDriveSyncClient(folderId = "drive-folder-annotations")
        val viewModel = createViewModel(
            settingsRepository = settingsRepository,
            contentRepository = FakeContentRepository(extraItems = listOf(document)),
            readingAnnotationRepository = annotationRepository,
            readingAnnotationDriveSyncClient = driveClient,
            readingAnnotationDriveTokenProvider = StaticReadingAnnotationDriveTokenProvider("fresh-silent-token"),
            nowProvider = { 2_000L },
        )

        advanceUntilIdle()
        viewModel.connectAnnotationDriveSync(accessToken = "stale-session-token", nowMillis = 2_500L)
        advanceUntilIdle()
        viewModel.openLibraryItem(document)
        advanceUntilIdle()
        viewModel.saveCurrentReadingAnnotation(
            paragraphIndex = 0,
            quotedText = "Private notes",
            noteText = "Autosync should refresh a stale same-process token silently.",
            nowMillis = 3_000L,
        )
        advanceUntilIdle()

        assertEquals("stale-session-token", driveClient.requests.first().accessToken)
        assertEquals("fresh-silent-token", driveClient.requests.last().accessToken)
        assertEquals(2, driveClient.requests.size)
        assertEquals(3_000L, settingsRepository.state.value.annotationDriveLastSuccessfulAtMillis)
        assertEquals(null, settingsRepository.state.value.annotationDriveLastError)
        assertEquals("Annotation saved and autosaved.", viewModel.uiState.latestMessage)
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun disconnectGoogleDriveAnnotationSyncClearsPersistedState() = runTest {
        val settingsRepository = FakeSettingsRepository(
            initial = completedSettings(selectedAppPackages = setOf("feed.one")).copy(
                annotationDriveSyncEnabled = true,
                annotationDriveFolderId = "drive-folder-annotations",
                annotationDriveLastSuccessfulAtMillis = 4_000L,
            ),
        )
        val viewModel = createViewModel(settingsRepository = settingsRepository)

        advanceUntilIdle()
        viewModel.disconnectAnnotationDriveSync(nowMillis = 5_000L)
        advanceUntilIdle()

        assertEquals(false, settingsRepository.state.value.annotationDriveSyncEnabled)
        assertEquals(null, settingsRepository.state.value.annotationDriveFolderId)
        assertEquals(null, settingsRepository.state.value.annotationDriveLastSuccessfulAtMillis)
        assertEquals(null, settingsRepository.state.value.annotationDriveLastError)
        assertEquals("Google Drive sync disconnected.", viewModel.uiState.latestMessage)
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun configureAnnotationExportReportsPersistPermissionFailureWithoutWriting() = runTest {
        val settingsRepository = FakeSettingsRepository(
            initial = completedSettings(selectedAppPackages = setOf("feed.one")).copy(
                annotationExportLastSuccessfulAtMillis = 1_500L,
            ),
        )
        val exportWriter = RecordingReadingAnnotationExportWriter()
        val viewModel = createViewModel(
            settingsRepository = settingsRepository,
            readingAnnotationExportWriter = exportWriter,
        )

        advanceUntilIdle()
        viewModel.configureReadingAnnotationExport(
            uri = "content://drive/qa-annotations.jsonld",
            displayName = "qa-annotations.jsonld",
            persistWritePermission = { error("Persistable grant denied") },
            nowMillis = 2_500L,
        )
        advanceUntilIdle()

        assertEquals(emptyList<Pair<String, String>>(), exportWriter.writes)
        assertEquals("content://drive/qa-annotations.jsonld", settingsRepository.state.value.annotationExportUri)
        assertEquals(null, settingsRepository.state.value.annotationExportLastSuccessfulAtMillis)
        assertEquals("Choose the file again or retry.", settingsRepository.state.value.annotationExportLastError)
        assertEquals("Annotation sync needs folder permission.", viewModel.uiState.latestMessage)
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun clearAnnotationExportReleasesPermissionAndClearsStatus() = runTest {
        val settingsRepository = FakeSettingsRepository(
            initial = completedSettings(selectedAppPackages = setOf("feed.one")).copy(
                annotationExportUri = "content://drive/qa-annotations.jsonld",
                annotationExportDisplayName = "qa-annotations.jsonld",
                annotationExportLastSuccessfulAtMillis = 3_000L,
            ),
        )
        val releasedUris = mutableListOf<String>()
        val viewModel = createViewModel(settingsRepository = settingsRepository)

        advanceUntilIdle()
        viewModel.clearReadingAnnotationExport(releaseWritePermission = releasedUris::add)
        advanceUntilIdle()

        assertEquals(listOf("content://drive/qa-annotations.jsonld"), releasedUris)
        assertEquals(null, settingsRepository.state.value.annotationExportUri)
        assertEquals(null, settingsRepository.state.value.annotationExportDisplayName)
        assertEquals(null, settingsRepository.state.value.annotationExportLastSuccessfulAtMillis)
        assertEquals(null, settingsRepository.state.value.annotationExportLastError)
        assertEquals("Annotation autosave disabled.", viewModel.uiState.latestMessage)
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun localDefaultsEnableAnnotationSyncAndProfileBackupWithoutFolderSelection() = runTest {
        val settingsRepository = FakeSettingsRepository(
            initial = completedSettings(selectedAppPackages = setOf("feed.one")),
        )
        val exportWriter = RecordingReadingAnnotationExportWriter()
        val profileWriter = RecordingAccountLightProfileAutosaveWriter()
        val annotationDefaultUri = "file:///local/qualityalternative/annotation-sync"
        val profileDefaultUri = "file:///local/qualityalternative/profile-backup"
        val viewModel = createViewModel(
            settingsRepository = settingsRepository,
            readingAnnotationExportWriter = exportWriter,
            accountLightProfileAutosaveWriter = profileWriter,
            defaultAnnotationExportUri = annotationDefaultUri,
            defaultProfileAutosaveUri = profileDefaultUri,
        )

        advanceUntilIdle()

        assertEquals(annotationDefaultUri, viewModel.uiState.annotationExportUri)
        assertEquals("App storage - Annotation sync", viewModel.uiState.annotationExportDisplayName)
        assertTrue(viewModel.uiState.annotationExportUsesLocalDefault)
        assertEquals(profileDefaultUri, viewModel.uiState.profileAutosaveUri)
        assertEquals("App storage - Profile backup", viewModel.uiState.profileAutosaveDisplayName)
        assertTrue(viewModel.uiState.profileAutosaveUsesLocalDefault)
        assertEquals(null, settingsRepository.state.value.annotationExportUri)
        assertEquals(null, settingsRepository.state.value.profileAutosaveUri)

        viewModel.retryReadingAnnotationExport(nowMillis = 3_000L)
        advanceUntilIdle()

        assertEquals(annotationDefaultUri, exportWriter.jsonWrites.single().first)
        assertEquals(3_000L, settingsRepository.state.value.annotationExportLastSuccessfulAtMillis)

        viewModel.retryAccountLightProfileAutosave(nowMillis = 4_000L)
        advanceUntilIdle()

        assertEquals(profileDefaultUri, profileWriter.writes.last().first)
        assertEquals("quality-alternative-profile.json", profileWriter.writes.last().second)
        assertEquals(4_000L, settingsRepository.state.value.profileAutosaveLastSuccessfulAtMillis)
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun changedDestinationsRemainExplicitAndCanReturnToLocalDefaults() = runTest {
        val settingsRepository = FakeSettingsRepository(
            initial = completedSettings(selectedAppPackages = setOf("feed.one")),
        )
        val releasedUris = mutableListOf<String>()
        val annotationDefaultUri = "file:///local/qualityalternative/annotation-sync"
        val profileDefaultUri = "file:///local/qualityalternative/profile-backup"
        val viewModel = createViewModel(
            settingsRepository = settingsRepository,
            defaultAnnotationExportUri = annotationDefaultUri,
            defaultProfileAutosaveUri = profileDefaultUri,
        )
        advanceUntilIdle()

        viewModel.configureReadingAnnotationExport(
            uri = "content://tree/custom-annotations",
            displayName = "Custom annotations",
            nowMillis = 2_000L,
        )
        advanceUntilIdle()
        viewModel.configureAccountLightProfileAutosave(
            uri = "content://tree/custom-profile",
            displayName = "Custom profile backup",
            nowMillis = 3_000L,
        )
        advanceUntilIdle()

        assertEquals("content://tree/custom-annotations", settingsRepository.state.value.annotationExportUri)
        assertEquals("Custom annotations", viewModel.uiState.annotationExportDisplayName)
        assertFalse(viewModel.uiState.annotationExportUsesLocalDefault)
        assertEquals("content://tree/custom-profile", settingsRepository.state.value.profileAutosaveUri)
        assertEquals("Custom profile backup", viewModel.uiState.profileAutosaveDisplayName)
        assertFalse(viewModel.uiState.profileAutosaveUsesLocalDefault)

        viewModel.clearReadingAnnotationExport(releaseWritePermission = releasedUris::add)
        advanceUntilIdle()
        viewModel.clearAccountLightProfileAutosave(releaseWritePermission = releasedUris::add)
        advanceUntilIdle()

        assertEquals(listOf("content://tree/custom-annotations", "content://tree/custom-profile"), releasedUris)
        assertEquals(null, settingsRepository.state.value.annotationExportUri)
        assertEquals(null, settingsRepository.state.value.profileAutosaveUri)
        assertEquals(annotationDefaultUri, viewModel.uiState.annotationExportUri)
        assertEquals(profileDefaultUri, viewModel.uiState.profileAutosaveUri)
        assertTrue(viewModel.uiState.annotationExportUsesLocalDefault)
        assertTrue(viewModel.uiState.profileAutosaveUsesLocalDefault)
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun localAnnotationDefaultFailureStaysVisibleAndKeepsDefaultDestination() = runTest {
        val settingsRepository = FakeSettingsRepository(
            initial = completedSettings(selectedAppPackages = setOf("feed.one")),
        )
        val annotationDefaultUri = "file:///local/qualityalternative/annotation-sync"
        val viewModel = createViewModel(
            settingsRepository = settingsRepository,
            readingAnnotationExportWriter = RecordingReadingAnnotationExportWriter(
                failure = IllegalStateException("Could not write local annotations"),
            ),
            defaultAnnotationExportUri = annotationDefaultUri,
        )

        advanceUntilIdle()
        viewModel.retryReadingAnnotationExport(nowMillis = 3_000L)
        advanceUntilIdle()

        assertEquals(annotationDefaultUri, viewModel.uiState.annotationExportUri)
        assertTrue(viewModel.uiState.annotationExportUsesLocalDefault)
        assertEquals(
            "Retry or change annotation sync destination.",
            settingsRepository.state.value.annotationExportLastError,
        )
        assertEquals("Retry or change annotation sync destination.", viewModel.uiState.annotationExportLastError)
        assertEquals("Annotation sync failed.", viewModel.uiState.latestMessage)
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun localProfileDefaultFailureStaysVisibleAndKeepsDefaultDestination() = runTest {
        val settingsRepository = FakeSettingsRepository(
            initial = completedSettings(selectedAppPackages = setOf("feed.one")),
        )
        val profileDefaultUri = "file:///local/qualityalternative/profile-backup"
        val viewModel = createViewModel(
            settingsRepository = settingsRepository,
            accountLightProfileAutosaveWriter = RecordingAccountLightProfileAutosaveWriter(
                failure = IllegalStateException("Could not write local profile backup"),
            ),
            defaultProfileAutosaveUri = profileDefaultUri,
        )

        advanceUntilIdle()
        viewModel.retryAccountLightProfileAutosave(nowMillis = 4_000L)
        advanceUntilIdle()

        assertEquals(profileDefaultUri, viewModel.uiState.profileAutosaveUri)
        assertTrue(viewModel.uiState.profileAutosaveUsesLocalDefault)
        assertEquals(
            "Retry or change profile backup destination.",
            settingsRepository.state.value.profileAutosaveLastError,
        )
        assertEquals("Retry or change profile backup destination.", viewModel.uiState.profileAutosaveLastError)
        assertEquals("Profile backup failed.", viewModel.uiState.latestMessage)
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun annotationAutosaveFailureKeepsSavedNoteAndShowsRecoverableStatus() = runTest {
        val document = savedUserDocument(format = ContentFormat.MARKDOWN)
        val settingsRepository = FakeSettingsRepository(
            initial = completedSettings(selectedAppPackages = setOf("feed.one")).copy(
                annotationExportUri = "content://drive/missing.md",
                annotationExportDisplayName = "missing.md",
            ),
        )
        val annotationRepository = FakeReadingAnnotationRepository()
        val exportWriter = RecordingReadingAnnotationExportWriter(
            failure = IllegalStateException("No content provider: content://com.qualityalternative.missing/export.md"),
        )
        val viewModel = createViewModel(
            settingsRepository = settingsRepository,
            contentRepository = FakeContentRepository(extraItems = listOf(document)),
            readingAnnotationRepository = annotationRepository,
            readingAnnotationExportWriter = exportWriter,
            nowProvider = { 2_000L },
        )

        advanceUntilIdle()
        viewModel.openLibraryItem(document)
        advanceUntilIdle()
        viewModel.saveCurrentReadingAnnotation(
            paragraphIndex = 0,
            quotedText = "Private notes",
            noteText = "Still keep this locally.",
            nowMillis = 3_000L,
        )
        advanceUntilIdle()

        assertEquals(1, annotationRepository.readingAnnotations().size)
        assertEquals("Annotation saved. Autosave failed.", viewModel.uiState.latestMessage)
        assertEquals("Choose the file again or retry.", settingsRepository.state.value.annotationExportLastError)
        assertEquals("Choose the file again or retry.", viewModel.uiState.annotationExportLastError)
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun deleteReadingAnnotationAutosavesUpdatedJsonLd() = runTest {
        val document = savedUserDocument(format = ContentFormat.MARKDOWN)
        val annotationRepository = FakeReadingAnnotationRepository(
            initialAnnotations = listOf(
                ReadingAnnotation(
                    id = "annotation-to-delete",
                    contentId = document.id,
                    paragraphIndex = 0,
                    quotedText = "Private notes",
                    noteText = "Delete me from export.",
                    createdAtMillis = 1_000L,
                    updatedAtMillis = 2_000L,
                ),
            ),
        )
        val settingsRepository = FakeSettingsRepository(
            initial = completedSettings(selectedAppPackages = setOf("feed.one")).copy(
                annotationExportUri = "content://drive/qa-annotations.jsonld",
                annotationExportDisplayName = "qa-annotations.jsonld",
            ),
        )
        val exportWriter = RecordingReadingAnnotationExportWriter()
        val viewModel = createViewModel(
            settingsRepository = settingsRepository,
            contentRepository = FakeContentRepository(extraItems = listOf(document)),
            readingAnnotationRepository = annotationRepository,
            readingAnnotationExportWriter = exportWriter,
        )

        advanceUntilIdle()
        viewModel.deleteReadingAnnotation(annotationId = "annotation-to-delete", nowMillis = 4_000L)
        advanceUntilIdle()

        assertEquals(emptyList<ReadingAnnotation>(), annotationRepository.readingAnnotations())
        assertEquals("Annotation deleted and autosaved.", viewModel.uiState.latestMessage)
        assertEquals(4_000L, settingsRepository.state.value.annotationExportLastSuccessfulAtMillis)
        assertEquals(emptyList<ReadingAnnotationExportFile>(), exportWriter.jsonWrites.last().second)
        assertFalse(exportWriter.writes.last().second.contains("Delete me from export."))
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun unfinishedContentGetsAbsolutePrimaryPriorityAndAnalytics() = runTest {
        val unfinished = savedUserDocument(format = ContentFormat.MARKDOWN).copy(
            id = "unfinished-doc",
            title = "Half-read private essay",
            durationMinutes = 5,
            topicTags = setOf(TopicTag.HISTORY),
        )
        val strongMatch = savedUserLink(
            id = "fresh-link",
            durationMinutes = 7,
            topics = setOf(TopicTag.PHILOSOPHY, TopicTag.SCIENCE),
        )
        val readingProgressRepository = FakeReadingProgressRepository(
            initialProgress = listOf(
                ReadingProgress(
                    contentId = unfinished.id,
                    progressPercent = 55,
                    lastVisibleParagraphIndex = 5,
                    paragraphCount = 12,
                    updatedAtMillis = 1_500L,
                ),
            ),
        )
        val analyticsTracker = InMemoryAnalyticsTracker()
        val viewModel = createViewModel(
            contentRepository = FakeContentRepository(extraItems = listOf(strongMatch, unfinished)),
            recommendationEngine = DefaultRecommendationEngine(),
            readingProgressRepository = readingProgressRepository,
            analyticsTracker = analyticsTracker,
        )

        advanceUntilIdle()
        viewModel.completeOnboarding()
        advanceUntilIdle()
        viewModel.triggerDebugIntervention(nowMillis = 2_000L)
        advanceUntilIdle()

        assertEquals(unfinished.id, viewModel.uiState.currentRecommendationSet?.primary?.id)
        assertEquals(setOf(unfinished.id), viewModel.uiState.preferences?.unfinishedContentIds)
        val event = analyticsTracker.allEvents().first {
            it.type == AnalyticsEventType.UNFINISHED_CONTENT_RECOMMENDED_AS_PRIMARY
        }
        assertEquals(unfinished.id, event.contentId)
        assertEquals("55", event.metadata["progressPercent"])
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun libraryManageModeDoesNotSelectOrDeleteEditorialContent() = runTest {
        val editorialRepository = FakeContentRepository()
        val editorialItem = editorialRepository.inventory().first()
        val analyticsTracker = InMemoryAnalyticsTracker()
        val recommendationEngine = RecordingRecommendationEngine()
        val releasedUris = mutableListOf<String>()
        val viewModel = createViewModel(
            contentRepository = editorialRepository,
            recommendationEngine = recommendationEngine,
            analyticsTracker = analyticsTracker,
            settingsRepository = FakeSettingsRepository(
                initial = completedSettings(
                    selectedAppPackages = setOf(FixtureTargetRegistry.fixtureDistractors.first().packageName),
                ),
            ),
        )

        advanceUntilIdle()
        viewModel.openLibrary()
        viewModel.toggleLibraryManageMode()
        viewModel.toggleLibraryContentSelection(editorialItem)
        advanceUntilIdle()

        assertEquals(emptySet<String>(), viewModel.uiState.selectedLibraryContentIds)

        viewModel.deleteSelectedLibraryContent(
            nowMillis = 2_000L,
            releaseDocumentPermission = releasedUris::add,
        )
        advanceUntilIdle()
        viewModel.triggerDebugIntervention(nowMillis = 3_000L)
        advanceUntilIdle()

        assertTrue(releasedUris.isEmpty())
        assertTrue(editorialItem.id in editorialRepository.inventory().map(ContentItem::id))
        assertTrue(editorialItem.id in recommendationEngine.lastInventory.map(ContentItem::id))
        assertFalse(analyticsTracker.allEvents().any { it.type == AnalyticsEventType.USER_CONTENT_DELETED })
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun skippingMeditationMarksSessionSkippedAndRecordsMeditationEvent() = runTest {
        val analyticsTracker = InMemoryAnalyticsTracker()
        val historyRepository = FakeHistoryRepository()
        val viewModel = createViewModel(
            recommendationEngine = FixedRecommendationEngine(
                RecommendationSet(
                    primary = MeditationTimerContentItem,
                    backups = emptyList(),
                    inventoryShortage = true,
                    generatedAtMillis = 1_000L,
                ),
            ),
            analyticsTracker = analyticsTracker,
            historyRepository = historyRepository,
        )

        advanceUntilIdle()
        viewModel.completeOnboarding()
        advanceUntilIdle()
        viewModel.triggerDebugIntervention(nowMillis = 1_000L)
        advanceUntilIdle()
        viewModel.acceptPrimary()
        advanceUntilIdle()

        viewModel.skipMeditationReset(nowMillis = 4_000L)
        advanceUntilIdle()

        assertEquals(MainScreen.Home, viewModel.uiState.screen)
        assertEquals("Meditation reset skipped.", viewModel.uiState.latestMessage)
        assertTrue(historyRepository.historyEntries.value.first().isSkipped())
        assertFalse(historyRepository.historyEntries.value.first().isCompleted())
        val skippedEvent = analyticsTracker.allEvents().first {
            it.type == AnalyticsEventType.MEDITATION_TIMER_SKIPPED
        }
        assertEquals(MEDITATION_TIMER_CONTENT_ID, skippedEvent.contentId)
        assertEquals("MEDITATION", skippedEvent.metadata["sourceType"])
        assertEquals("MEDITATION_TIMER", skippedEvent.metadata["renderMode"])
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun failedUserLinkHandoffMarksLinkUnavailableAndDoesNotRecordSuccessfulOpen() = runTest {
        val userLink = savedUserLink()
        val userLinkRepository = FakeUserLinkRepository(initialLinks = listOf(userLink))
        val analyticsTracker = InMemoryAnalyticsTracker()
        val viewModel = createViewModel(
            contentRepository = FakeContentRepository(extraItems = listOf(userLink)),
            userLinkRepository = userLinkRepository,
            recommendationEngine = FixedRecommendationEngine(
                RecommendationSet(
                    primary = userLink,
                    backups = emptyList(),
                    inventoryShortage = true,
                    generatedAtMillis = 1_000L,
                ),
            ),
            analyticsTracker = analyticsTracker,
        )

        advanceUntilIdle()
        viewModel.completeOnboarding()
        advanceUntilIdle()
        viewModel.triggerDebugIntervention(nowMillis = 1_000L)
        advanceUntilIdle()
        viewModel.acceptPrimary()
        advanceUntilIdle()

        viewModel.recordExternalLinkHandoffFailed(reason = "no_handler", nowMillis = 2_000L)
        advanceUntilIdle()

        assertFalse(analyticsTracker.allEvents().any { it.type == AnalyticsEventType.USER_LINK_FALLBACK_OPENED })
        val failure = analyticsTracker.allEvents().first {
            it.type == AnalyticsEventType.USER_LINK_HANDOFF_FAILED
        }
        assertEquals(userLink.id, failure.contentId)
        assertEquals("no_handler", failure.metadata["failureReason"])
        assertEquals("USER_LINK", failure.metadata["sourceType"])
        assertEquals(ContentAvailability.UNAVAILABLE, userLinkRepository.links.value.single().availability)
        assertEquals(listOf(userLink.id), userLinkRepository.markedUnavailableIds)
        assertEquals(MainScreen.Home, viewModel.uiState.screen)
        assertEquals(
            "This saved link could not be opened and was removed from future recommendations.",
            viewModel.uiState.latestMessage,
        )
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun saveUserLinkFromForm_persistsLinkAndRecordsAnalytics() = runTest {
        val userLinkRepository = FakeUserLinkRepository()
        val analyticsTracker = InMemoryAnalyticsTracker()
        val viewModel = createViewModel(
            userLinkRepository = userLinkRepository,
            analyticsTracker = analyticsTracker,
        )

        advanceUntilIdle()
        viewModel.completeOnboarding()
        advanceUntilIdle()

        viewModel.openAddLink()
        viewModel.updateAddLinkUrl("https://example.com/essay")
        viewModel.updateAddLinkTitle("Saved essay")
        viewModel.updateAddLinkDuration("9")
        viewModel.toggleAddLinkTopic(TopicTag.SCIENCE)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.addLinkForm.canSave)

        viewModel.saveUserLink(nowMillis = 2_000L)
        advanceUntilIdle()

        assertEquals(MainScreen.AddLinkSuccess, viewModel.uiState.screen)
        assertEquals("Saved essay", viewModel.uiState.savedLinkConfirmation?.title)
        assertEquals("Saved essay", userLinkRepository.links.value.single().title)
        assertEquals("Saved essay", viewModel.uiState.userLinks.single().title)
        val event = analyticsTracker.allEvents().first { it.type == AnalyticsEventType.USER_LINK_ADDED }
        assertEquals(userLinkRepository.links.value.single().id, event.contentId)
        assertEquals("USER_LINK", event.metadata["sourceType"])
        assertEquals("USER_PRIVATE", event.metadata["rightsClass"])
        assertEquals("EXTERNAL_HANDOFF", event.metadata["renderMode"])
        assertEquals("https://example.com/essay", event.metadata["externalUrl"])

        viewModel.finishAddLinkSuccess()
        assertEquals(MainScreen.Library, viewModel.uiState.screen)
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun saveUserDocumentFromForm_persistsFileAndRecordsAnalytics() = runTest {
        val userDocumentRepository = FakeUserDocumentRepository()
        val analyticsTracker = InMemoryAnalyticsTracker()
        val viewModel = createViewModel(
            userDocumentRepository = userDocumentRepository,
            analyticsTracker = analyticsTracker,
        )

        advanceUntilIdle()
        viewModel.completeOnboarding()
        advanceUntilIdle()

        viewModel.prepareUserDocumentImport(
            uri = "content://quality/notes",
            displayName = "notes.md",
            mimeType = "text/markdown",
        )
        viewModel.toggleAddDocumentTopic(TopicTag.SCIENCE)
        advanceUntilIdle()

        assertEquals(MainScreen.AddDocument, viewModel.uiState.screen)
        assertTrue(viewModel.uiState.addDocumentForm.canSave)

        viewModel.saveUserDocument(nowMillis = 2_000L)
        advanceUntilIdle()

        assertEquals(MainScreen.AddLinkSuccess, viewModel.uiState.screen)
        assertEquals("notes", viewModel.uiState.savedLinkConfirmation?.title)
        assertEquals("notes", userDocumentRepository.documents.value.single().title)
        assertEquals("notes", viewModel.uiState.userDocuments.single().title)
        val event = analyticsTracker.allEvents().first { it.type == AnalyticsEventType.USER_DOCUMENT_ADDED }
        assertEquals(userDocumentRepository.documents.value.single().id, event.contentId)
        assertEquals("USER_DOCUMENT", event.metadata["sourceType"])
        assertEquals("USER_PRIVATE", event.metadata["rightsClass"])
        assertEquals("USER_PRIVATE_READER", event.metadata["renderMode"])
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun prepareUserDocumentImportUsesSharedCandidateFactoryEstimate() = runTest {
        val viewModel = createViewModel()

        advanceUntilIdle()
        viewModel.prepareUserDocumentImport(
            uri = "content://quality/normal.md",
            displayName = "normal.md",
            mimeType = "text/markdown",
        ) {
            ByteArrayInputStream(List(1_125) { "word" }.joinToString(" ").toByteArray(Charsets.UTF_8))
        }
        advanceUntilIdle()

        val candidate = viewModel.uiState.addDocumentForm.candidates.single()
        assertEquals("5", viewModel.uiState.addDocumentForm.durationMinutes)
        assertEquals("5", candidate.durationMinutes)
        assertEquals(1_125, candidate.estimatedWordCount)
        assertEquals(ReadingTimeEstimateSource.EXTRACTED_TEXT, candidate.estimateSource)
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun saveUserDocument_retainsReadPermissionOnlyAfterSuccessfulSave() = runTest {
        val userDocumentRepository = FakeUserDocumentRepository()
        val retainedUris = mutableListOf<String>()
        val viewModel = createViewModel(userDocumentRepository = userDocumentRepository)

        advanceUntilIdle()
        viewModel.prepareUserDocumentImport(
            uri = "content://quality/notes",
            displayName = "notes.md",
            mimeType = "text/markdown",
        )
        advanceUntilIdle()

        assertTrue(retainedUris.isEmpty())

        viewModel.saveUserDocument(
            nowMillis = 2_000L,
            persistReadPermission = retainedUris::add,
        )
        advanceUntilIdle()

        assertTrue(retainedUris.isEmpty())
        assertEquals(MainScreen.AddDocument, viewModel.uiState.screen)

        viewModel.toggleAddDocumentTopic(TopicTag.SCIENCE)
        advanceUntilIdle()

        viewModel.saveUserDocument(
            nowMillis = 3_000L,
            persistReadPermission = retainedUris::add,
        )
        advanceUntilIdle()

        assertEquals(listOf("content://quality/notes"), retainedUris)
        assertEquals(MainScreen.AddLinkSuccess, viewModel.uiState.screen)
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun saveUserLinkWithPriorityAtAddPersistsPriorityAndRecordsDistinctAnalytics() = runTest {
        val userLinkRepository = FakeUserLinkRepository()
        val analyticsTracker = InMemoryAnalyticsTracker()
        val settingsRepository = FakeSettingsRepository(
            initial = completedSettings(
                selectedAppPackages = setOf(FixtureTargetRegistry.fixtureDistractors.first().packageName),
            ),
        )
        val viewModel = createViewModel(
            userLinkRepository = userLinkRepository,
            analyticsTracker = analyticsTracker,
            settingsRepository = settingsRepository,
        )

        advanceUntilIdle()
        viewModel.openAddLink()
        viewModel.updateAddLinkUrl("https://example.com/priority")
        viewModel.updateAddLinkTitle("Priority essay")
        viewModel.updateAddLinkDuration("8")
        viewModel.toggleAddLinkTopic(TopicTag.SCIENCE)
        viewModel.toggleAddLinkPriority()
        advanceUntilIdle()

        viewModel.saveUserLink(nowMillis = 2_000L)
        advanceUntilIdle()

        val saved = userLinkRepository.links.value.single()
        assertEquals(setOf(saved.id), viewModel.uiState.priorityContentIds)
        assertEquals(setOf(saved.id), settingsRepository.state.value.priorityContentIds)
        assertEquals(true, viewModel.uiState.savedLinkConfirmation?.priorityMarked)
        assertTrue(analyticsTracker.allEvents().any { event ->
            event.type == AnalyticsEventType.PRIORITY_SET_DURING_ADD &&
                event.contentId == saved.id &&
                event.metadata["source"] == "add_flow"
        })
        assertTrue(analyticsTracker.allEvents().none { event ->
            event.type == AnalyticsEventType.PRIORITY_CONTENT_TOGGLED &&
                event.contentId == saved.id
        })
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun batchDocumentImportSavesSupportedFilesSkipsUnsupportedPersistsPriorityAndAnalytics() = runTest {
        val userDocumentRepository = FakeUserDocumentRepository()
        val analyticsTracker = InMemoryAnalyticsTracker()
        val settingsRepository = FakeSettingsRepository(
            initial = completedSettings(
                selectedAppPackages = setOf(FixtureTargetRegistry.fixtureDistractors.first().packageName),
            ),
        )
        val retainedUris = mutableListOf<String>()
        val viewModel = createViewModel(
            userDocumentRepository = userDocumentRepository,
            analyticsTracker = analyticsTracker,
            settingsRepository = settingsRepository,
        )

        advanceUntilIdle()
        viewModel.prepareUserDocumentBatchImport(
            candidates = listOf(
                DocumentImportCandidate(
                    uri = "content://quality/notes",
                    displayName = "notes.md",
                    mimeType = "text/markdown",
                    title = "Notes",
                    durationMinutes = "4",
                    format = ContentFormat.MARKDOWN,
                    estimateSource = ReadingTimeEstimateSource.EXTRACTED_TEXT,
                    estimatedWordCount = 800,
                ),
                DocumentImportCandidate(
                    uri = "content://quality/book",
                    displayName = "book.epub",
                    mimeType = "application/epub+zip",
                    title = "Book",
                    durationMinutes = "20",
                    format = ContentFormat.EPUB,
                    estimateSource = ReadingTimeEstimateSource.EXTRACTED_TEXT,
                    estimatedWordCount = 10_000,
                ),
                DocumentImportCandidate(
                    uri = "content://quality/archive",
                    displayName = "archive.zip",
                    mimeType = "application/zip",
                    title = "Archive",
                    durationMinutes = "10",
                    format = null,
                    estimateSource = ReadingTimeEstimateSource.FALLBACK_DEFAULT,
                ),
            ),
            nowMillis = 1_000L,
        )
        viewModel.toggleAddDocumentTopic(TopicTag.SCIENCE)
        viewModel.toggleAddDocumentPriority()
        advanceUntilIdle()

        assertEquals(MainScreen.AddDocument, viewModel.uiState.screen)
        assertTrue(viewModel.uiState.addDocumentForm.canSave)
        assertEquals(3, viewModel.uiState.addDocumentForm.importCount)
        assertEquals(2, viewModel.uiState.addDocumentForm.supportedImportCount)
        assertEquals(1, viewModel.uiState.addDocumentForm.unsupportedImportCount)

        viewModel.saveUserDocument(
            nowMillis = 2_000L,
            persistReadPermission = retainedUris::add,
        )
        advanceUntilIdle()

        assertEquals(listOf("content://quality/notes", "content://quality/book"), retainedUris)
        assertEquals(listOf("content://quality/notes", "content://quality/book"), userDocumentRepository.addedDrafts.map(UserDocumentDraft::uri))
        assertEquals(listOf(4, 20), userDocumentRepository.addedDrafts.map(UserDocumentDraft::durationMinutes))
        val savedIds = userDocumentRepository.documents.value.map(ContentItem::id).toSet()
        assertEquals(2, savedIds.size)
        assertEquals(savedIds, viewModel.uiState.priorityContentIds)
        assertEquals(savedIds, settingsRepository.state.value.priorityContentIds)
        assertEquals(MainScreen.AddLinkSuccess, viewModel.uiState.screen)
        assertEquals(2, viewModel.uiState.savedLinkConfirmation?.savedCount)
        assertEquals(1, viewModel.uiState.savedLinkConfirmation?.skippedCount)
        assertEquals(true, viewModel.uiState.savedLinkConfirmation?.priorityMarked)

        val events = analyticsTracker.allEvents()
        val attempted = events.first { it.type == AnalyticsEventType.BATCH_DOCUMENT_IMPORT_ATTEMPTED }
        assertEquals("3", attempted.metadata["selectedCount"])
        assertEquals("2", attempted.metadata["supportedCount"])
        assertEquals("1", attempted.metadata["unsupportedCount"])
        val completed = events.first { it.type == AnalyticsEventType.BATCH_DOCUMENT_IMPORT_COMPLETED }
        assertEquals("2", completed.metadata["savedCount"])
        assertEquals("1", completed.metadata["rejectedCount"])
        assertEquals(2, events.count { it.type == AnalyticsEventType.USER_DOCUMENT_ADDED })
        assertEquals(2, events.count { it.type == AnalyticsEventType.READING_TIME_ESTIMATE_APPLIED })
        assertEquals(2, events.count { it.type == AnalyticsEventType.PRIORITY_SET_DURING_ADD })
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun failedUserDocumentHandoffMarksDocumentUnavailableAndDiagnosticsCountIt() = runTest {
        val userDocument = savedUserDocument(format = ContentFormat.PDF)
        val userDocumentRepository = FakeUserDocumentRepository(initialDocuments = listOf(userDocument))
        val analyticsTracker = InMemoryAnalyticsTracker()
        val viewModel = createViewModel(
            contentRepository = FakeContentRepository(extraItems = listOf(userDocument)),
            userDocumentRepository = userDocumentRepository,
            recommendationEngine = FixedRecommendationEngine(
                RecommendationSet(
                    primary = userDocument,
                    backups = emptyList(),
                    inventoryShortage = true,
                    generatedAtMillis = 1_000L,
                ),
            ),
            analyticsTracker = analyticsTracker,
        )

        advanceUntilIdle()
        viewModel.completeOnboarding()
        advanceUntilIdle()
        viewModel.triggerDebugIntervention(nowMillis = 1_000L)
        advanceUntilIdle()
        viewModel.acceptPrimary()
        advanceUntilIdle()

        viewModel.recordExternalLinkHandoffFailed(reason = "no_handler", nowMillis = 2_000L)
        advanceUntilIdle()

        val failure = analyticsTracker.allEvents().first {
            it.type == AnalyticsEventType.EXTERNAL_HANDOFF_FAILED
        }
        assertEquals("USER_DOCUMENT", failure.metadata["sourceType"])
        assertEquals("no_handler", failure.metadata["failureReason"])
        assertEquals(ContentAvailability.UNAVAILABLE, userDocumentRepository.documents.value.single().availability)
        assertEquals(listOf(userDocument.id), userDocumentRepository.markedUnavailableIds)
        assertEquals(
            "This saved file could not be opened and was removed from future recommendations.",
            viewModel.uiState.latestMessage,
        )

        val diagnosticsTracker = InMemoryAnalyticsTracker()
        val diagnosticsViewModel = createViewModel(
            userDocumentRepository = userDocumentRepository,
            analyticsTracker = diagnosticsTracker,
        )
        advanceUntilIdle()
        diagnosticsViewModel.completeOnboarding()
        advanceUntilIdle()
        diagnosticsViewModel.triggerDebugIntervention(nowMillis = 3_000L)
        advanceUntilIdle()

        val nextEvent = diagnosticsTracker.allEvents().first { it.type == AnalyticsEventType.INTERVENTION_SHOWN }
        assertEquals("1", nextEvent.metadata["unavailableUserDocumentCount"])
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun libraryPdfHandoffFailureMarksDocumentUnavailableWithoutSession() = runTest {
        val userDocument = savedUserDocument(format = ContentFormat.PDF)
        val userDocumentRepository = FakeUserDocumentRepository(initialDocuments = listOf(userDocument))
        val analyticsTracker = InMemoryAnalyticsTracker()
        val viewModel = createViewModel(
            contentRepository = FakeContentRepository(extraItems = listOf(userDocument)),
            userDocumentRepository = userDocumentRepository,
            analyticsTracker = analyticsTracker,
        )

        advanceUntilIdle()
        viewModel.openLibraryItem(userDocument)
        advanceUntilIdle()

        assertEquals(MainScreen.ExternalHandoff, viewModel.uiState.screen)
        assertEquals(null, viewModel.uiState.currentSessionId)

        viewModel.recordExternalLinkHandoffFailed(reason = "no_handler", nowMillis = 2_000L)
        advanceUntilIdle()

        val failure = analyticsTracker.allEvents().first {
            it.type == AnalyticsEventType.EXTERNAL_HANDOFF_FAILED
        }
        assertEquals(null, failure.sessionId)
        assertEquals(userDocument.id, failure.contentId)
        assertEquals("USER_DOCUMENT", failure.metadata["sourceType"])
        assertEquals("no_handler", failure.metadata["failureReason"])
        assertEquals(ContentAvailability.UNAVAILABLE, userDocumentRepository.documents.value.single().availability)
        assertEquals(listOf(userDocument.id), userDocumentRepository.markedUnavailableIds)
        assertEquals(MainScreen.Home, viewModel.uiState.screen)
        assertEquals(
            "This saved file could not be opened and was removed from future recommendations.",
            viewModel.uiState.latestMessage,
        )
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun unreadableMarkdownDocumentIsMarkedUnavailableInsteadOfOpeningBlankReader() = runTest {
        val userDocument = savedUserDocument(format = ContentFormat.MARKDOWN)
        val userDocumentRepository = FakeUserDocumentRepository(initialDocuments = listOf(userDocument))
        val analyticsTracker = InMemoryAnalyticsTracker()
        val viewModel = createViewModel(
            contentRepository = FakeContentRepository(
                extraItems = listOf(userDocument),
                failUserDocumentBodyLoad = true,
            ),
            userDocumentRepository = userDocumentRepository,
            recommendationEngine = FixedRecommendationEngine(
                RecommendationSet(
                    primary = userDocument,
                    backups = emptyList(),
                    inventoryShortage = true,
                    generatedAtMillis = 1_000L,
                ),
            ),
            analyticsTracker = analyticsTracker,
        )

        advanceUntilIdle()
        viewModel.completeOnboarding()
        advanceUntilIdle()
        viewModel.triggerDebugIntervention(nowMillis = 1_000L)
        advanceUntilIdle()
        viewModel.acceptPrimary()
        advanceUntilIdle()

        assertEquals(MainScreen.Home, viewModel.uiState.screen)
        assertEquals("", viewModel.uiState.currentContentBody)
        assertEquals(ContentAvailability.UNAVAILABLE, userDocumentRepository.documents.value.single().availability)
        val failure = analyticsTracker.allEvents().first {
            it.type == AnalyticsEventType.USER_DOCUMENT_BODY_LOAD_FAILED
        }
        assertEquals(userDocument.id, failure.contentId)
        assertEquals("USER_DOCUMENT", failure.metadata["sourceType"])
        assertEquals("USER_PRIVATE_READER", failure.metadata["renderMode"])
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun libraryUnreadableMarkdownDocumentIsMarkedUnavailableInsteadOfCrashing() = runTest {
        val userDocument = savedUserDocument(format = ContentFormat.MARKDOWN)
        val userDocumentRepository = FakeUserDocumentRepository(initialDocuments = listOf(userDocument))
        val analyticsTracker = InMemoryAnalyticsTracker()
        val viewModel = createViewModel(
            contentRepository = FakeContentRepository(
                extraItems = listOf(userDocument),
                failUserDocumentBodyLoad = true,
            ),
            userDocumentRepository = userDocumentRepository,
            analyticsTracker = analyticsTracker,
            nowProvider = { 4_000L },
        )

        advanceUntilIdle()
        viewModel.openLibraryItem(userDocument)
        advanceUntilIdle()

        assertEquals(MainScreen.Home, viewModel.uiState.screen)
        assertEquals("", viewModel.uiState.currentContentBody)
        assertEquals(ContentAvailability.UNAVAILABLE, userDocumentRepository.documents.value.single().availability)
        assertEquals(listOf(userDocument.id), userDocumentRepository.markedUnavailableIds)
        assertEquals(
            "This saved file could not be opened and was removed from future recommendations.",
            viewModel.uiState.latestMessage,
        )
        val failure = analyticsTracker.allEvents().first {
            it.type == AnalyticsEventType.USER_DOCUMENT_BODY_LOAD_FAILED
        }
        assertEquals(null, failure.sessionId)
        assertEquals(userDocument.id, failure.contentId)
        assertEquals("USER_DOCUMENT", failure.metadata["sourceType"])
        assertEquals("USER_PRIVATE_READER", failure.metadata["renderMode"])
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun inAppRenderModeRoutesToReaderWithoutRuntimeRightsBlocking() = runTest {
        val inAppItem = ContentItem(
            id = "link-only-in-app-mode",
            packId = "philosophy",
            title = "In-app mode item",
            description = "Inventory audit owns rights validation; runtime follows render mode.",
            durationMinutes = 7,
            format = ContentFormat.MARKDOWN,
            topicTags = setOf(TopicTag.PHILOSOPHY),
            bodyAssetPath = "example.md",
            externalUrl = "https://example.com/malformed",
            rights = ContentRightsMetadata(
                rightsClass = ContentRightsClass.LINK_ONLY,
                renderMode = ContentRenderMode.IN_APP_READER,
            ),
        )
        val viewModel = createViewModel(
            contentRepository = FakeContentRepository(extraItems = listOf(inAppItem)),
        )

        advanceUntilIdle()
        viewModel.openLibraryItem(inAppItem)

        assertEquals(MainScreen.Reader, viewModel.uiState.screen)
        assertEquals("In-app mode item", viewModel.uiState.currentContentBody)
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun saveUserLinkWithRejectedDraft_keepsAddLinkOpenWithErrors() = runTest {
        val viewModel = createViewModel()

        advanceUntilIdle()
        viewModel.completeOnboarding()
        advanceUntilIdle()

        viewModel.openAddLink()
        viewModel.updateAddLinkUrl("quality://bad")
        viewModel.updateAddLinkTitle("Bad link")
        viewModel.updateAddLinkDuration("7")
        viewModel.toggleAddLinkTopic(TopicTag.SCIENCE)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.addLinkForm.canSave)
        assertTrue(UserLinkValidationError.UNSUPPORTED_SCHEME in viewModel.uiState.addLinkForm.validationErrors)

        viewModel.saveUserLink(nowMillis = 2_000L)
        advanceUntilIdle()

        assertEquals(MainScreen.AddLink, viewModel.uiState.screen)
        assertTrue(UserLinkValidationError.UNSUPPORTED_SCHEME in viewModel.uiState.addLinkForm.validationErrors)
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun saveUserLinkWithRepositoryFailure_keepsAddLinkOpenAndClearsSaving() = runTest {
        val viewModel = createViewModel(
            userLinkRepository = FakeUserLinkRepository(throwOnAdd = true),
        )

        advanceUntilIdle()
        viewModel.completeOnboarding()
        advanceUntilIdle()

        viewModel.openAddLink()
        viewModel.updateAddLinkUrl("https://example.com/essay")
        viewModel.updateAddLinkTitle("Saved essay")
        viewModel.updateAddLinkDuration("7")
        viewModel.toggleAddLinkTopic(TopicTag.SCIENCE)
        advanceUntilIdle()

        viewModel.saveUserLink(nowMillis = 2_000L)
        advanceUntilIdle()

        assertEquals(MainScreen.AddLink, viewModel.uiState.screen)
        assertFalse(viewModel.uiState.addLinkForm.isSaving)
        assertEquals("The link could not be saved locally. Try again.", viewModel.uiState.latestMessage)
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun malformedWebUrl_staysDisabledAndShowsMissingHostError() = runTest {
        val viewModel = createViewModel()

        advanceUntilIdle()
        viewModel.completeOnboarding()
        advanceUntilIdle()

        viewModel.openAddLink()
        viewModel.updateAddLinkUrl("https://")
        viewModel.updateAddLinkTitle("Missing host")
        viewModel.updateAddLinkDuration("7")
        viewModel.toggleAddLinkTopic(TopicTag.SCIENCE)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.addLinkForm.canSave)
        assertTrue(UserLinkValidationError.MISSING_HOST in viewModel.uiState.addLinkForm.validationErrors)
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun addLinkWithBlankTitle_staysDisabledAndShowsTitleError() = runTest {
        val viewModel = createViewModel()

        advanceUntilIdle()
        viewModel.completeOnboarding()
        advanceUntilIdle()

        viewModel.openAddLink()
        viewModel.updateAddLinkUrl("https://example.com/essay")
        viewModel.updateAddLinkDuration("7")
        viewModel.toggleAddLinkTopic(TopicTag.SCIENCE)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.addLinkForm.canSave)
        assertTrue(UserLinkValidationError.BLANK_TITLE in viewModel.uiState.addLinkForm.validationErrors)
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun addLinkWithNoTopic_staysDisabledAndShowsTopicError() = runTest {
        val viewModel = createViewModel()

        advanceUntilIdle()
        viewModel.completeOnboarding()
        advanceUntilIdle()

        viewModel.openAddLink()
        viewModel.updateAddLinkUrl("https://example.com/essay")
        viewModel.updateAddLinkTitle("Saved essay")
        viewModel.updateAddLinkDuration("7")
        advanceUntilIdle()

        assertFalse(viewModel.uiState.addLinkForm.canSave)
        assertTrue(UserLinkValidationError.NO_TOPICS in viewModel.uiState.addLinkForm.validationErrors)
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun addLinkWithBlankDuration_staysDisabledAndShowsDurationError() = runTest {
        val viewModel = createViewModel()

        advanceUntilIdle()
        viewModel.completeOnboarding()
        advanceUntilIdle()

        viewModel.openAddLink()
        viewModel.updateAddLinkUrl("https://example.com/essay")
        viewModel.updateAddLinkTitle("Saved essay")
        viewModel.updateAddLinkDuration("")
        viewModel.toggleAddLinkTopic(TopicTag.SCIENCE)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.addLinkForm.canSave)
        assertTrue(UserLinkValidationError.INVALID_DURATION in viewModel.uiState.addLinkForm.validationErrors)
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun addLinkWithBlankUrl_staysDisabledAndShowsUrlError() = runTest {
        val viewModel = createViewModel()

        advanceUntilIdle()
        viewModel.completeOnboarding()
        advanceUntilIdle()

        viewModel.openAddLink()
        viewModel.updateAddLinkTitle("Saved essay")
        viewModel.updateAddLinkDuration("7")
        viewModel.toggleAddLinkTopic(TopicTag.SCIENCE)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.addLinkForm.canSave)
        assertTrue(UserLinkValidationError.EMPTY_URL in viewModel.uiState.addLinkForm.validationErrors)
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun finishReading_marksHistoryCompletedAndExcludesContent() = runTest {
        val settingsRepository = FakeSettingsRepository(
            initial = completedSettings(
                selectedAppPackages = SupportedCatalog.distractingApps.take(3).mapTo(mutableSetOf(), DistractingApp::packageName),
            ).copy(contentPriority = ContentPriority.READINGS),
        )
        val viewModel = createViewModel(settingsRepository = settingsRepository)

        advanceUntilIdle()
        viewModel.triggerDebugIntervention(nowMillis = 1_000L)
        advanceUntilIdle()
        viewModel.acceptPrimary()
        advanceUntilIdle()
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
                interventionId = "intervention-1",
                interventionShownAtMillis = 1_000L,
                primaryContentId = "p1",
                backupContentIds = listOf("s1"),
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
        val returnEvent = analyticsTracker.allEvents().first {
            it.type == AnalyticsEventType.RETURN_TO_APP_WITHIN_15_MINUTES
        }
        assertEquals("session", returnEvent.metadata["returnAttribution"])
        assertTrue(viewModel.uiState.historyEntries.first().returnedToTarget())
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun triggerDebugIntervention_clearsExpiredDelayWindowFromUiState() = runTest {
        val delayGate = InMemoryDelayGate()
        val viewModel = track(
            MainViewModel(
                contentRepository = FakeContentRepository(),
                settingsRepository = FakeSettingsRepository(),
                recommendationEngine = DefaultRecommendationEngine(),
                delayGate = delayGate,
                analyticsTracker = InMemoryAnalyticsTracker(),
                historyRepository = FakeHistoryRepository(),
                interceptionMonitor = FakeInterceptionMonitor(),
                enableDelayRefreshTicker = false,
            ),
        )

        advanceUntilIdle()
        viewModel.completeOnboarding()
        advanceUntilIdle()

        val targetApp = viewModel.uiState.availableTargetApps.first()
        viewModel.selectTargetApp(targetApp)
        delayGate.storeDelay(targetApp = targetApp, nowMillis = 1_000L, durationMinutes = 15)

        viewModel.triggerDebugIntervention(nowMillis = 1_000L + 16 * 60_000L)
        advanceUntilIdle()

        assertEquals(null, viewModel.uiState.activeDelayWindow)
        assertEquals(MainScreen.Intervention, viewModel.uiState.screen)
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun triggerDebugIntervention_keepsLoadingUntilPersistedSourcesAreReady() = runTest {
        val settingsRepository = FakeSettingsRepository()
        val historyRepository = FakeHistoryRepository(isReady = false)
        val analyticsTracker = InMemoryAnalyticsTracker()
        val delayGate = FakeDelayGate(isReady = false)
        val viewModel = track(
            MainViewModel(
                contentRepository = FakeContentRepository(),
                settingsRepository = settingsRepository,
                recommendationEngine = DefaultRecommendationEngine(),
                delayGate = delayGate,
                analyticsTracker = analyticsTracker,
                historyRepository = historyRepository,
                interceptionMonitor = FakeInterceptionMonitor(),
                enableDelayRefreshTicker = false,
            ),
        )

        advanceUntilIdle()
        assertTrue(viewModel.uiState.isLoadingSettings)

        historyRepository.setReady(true)
        delayGate.setReady(true)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.isLoadingSettings)
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun completedExclusion_usesAllCompletedIdsNotJustVisibleHistoryWindow() = runTest {
        val settingsRepository = FakeSettingsRepository(
            initial = AppSettings(
                hasCompletedOnboarding = true,
                selectedAppPackages = SupportedCatalog.distractingApps.take(3).mapTo(mutableSetOf(), DistractingApp::packageName),
                preferredTopics = setOf(TopicTag.PHILOSOPHY, TopicTag.SCIENCE, TopicTag.HISTORY),
                preferredDurationBucket = DurationBucket.FOCUS,
                selectedPackIds = setOf("philosophy", "science"),
            ),
        )
        val historyRepository = FakeHistoryRepository(
            initialHistory = emptyList(),
            initialCompletedIds = setOf("p1"),
        )
        val viewModel = createViewModel(
            settingsRepository = settingsRepository,
            historyRepository = historyRepository,
        )

        advanceUntilIdle()
        viewModel.triggerDebugIntervention(nowMillis = 2_000L)
        advanceUntilIdle()

        assertEquals("s1", viewModel.uiState.currentRecommendationSet?.primary?.id)
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun completedActivation_reactivatesCompletedContentForRecommendations() = runTest {
        val settingsRepository = FakeSettingsRepository(
            initial = AppSettings(
                hasCompletedOnboarding = true,
                selectedAppPackages = SupportedCatalog.distractingApps.take(3).mapTo(mutableSetOf(), DistractingApp::packageName),
                preferredTopics = setOf(TopicTag.PHILOSOPHY, TopicTag.SCIENCE, TopicTag.HISTORY),
                preferredDurationBucket = DurationBucket.FOCUS,
                selectedPackIds = setOf("philosophy", "science"),
            ),
        )
        val historyRepository = FakeHistoryRepository(
            initialHistory = emptyList(),
            initialCompletedIds = setOf("p1"),
        )
        val analyticsTracker = InMemoryAnalyticsTracker()
        val viewModel = createViewModel(
            settingsRepository = settingsRepository,
            historyRepository = historyRepository,
            analyticsTracker = analyticsTracker,
        )

        advanceUntilIdle()
        viewModel.triggerDebugIntervention(nowMillis = 2_000L)
        advanceUntilIdle()
        assertEquals("s1", viewModel.uiState.currentRecommendationSet?.primary?.id)

        val completedItem = viewModel.uiState.starterPacks
            .flatMap(EditorialPack::items)
            .first { it.id == "p1" }
        viewModel.toggleCompletedContentActivation(completedItem)
        advanceUntilIdle()

        assertEquals(setOf("p1"), settingsRepository.state.value.reactivatedCompletedContentIds)
        assertEquals(setOf("p1"), viewModel.uiState.reactivatedCompletedContentIds)
        assertTrue(
            analyticsTracker.allEvents().any {
                it.type == AnalyticsEventType.COMPLETED_CONTENT_ACTIVATION_TOGGLED &&
                    it.contentId == "p1" &&
                    it.metadata["reactivated"] == "true"
            },
        )

        viewModel.triggerDebugIntervention(nowMillis = 3_000L)
        advanceUntilIdle()

        assertEquals("p1", viewModel.uiState.currentRecommendationSet?.primary?.id)
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun finishReading_deactivatesReactivatedCompletedContentAgain() = runTest {
        val settingsRepository = FakeSettingsRepository(
            initial = completedSettings(
                selectedAppPackages = SupportedCatalog.distractingApps.take(3).mapTo(mutableSetOf(), DistractingApp::packageName),
            ).copy(reactivatedCompletedContentIds = setOf("p1")),
        )
        val viewModel = createViewModel(settingsRepository = settingsRepository)

        advanceUntilIdle()
        viewModel.triggerDebugIntervention(nowMillis = 1_000L)
        advanceUntilIdle()
        viewModel.acceptPrimary()
        advanceUntilIdle()
        viewModel.finishReading()
        advanceUntilIdle()

        assertTrue("p1" in viewModel.uiState.completedContentIds)
        assertEquals(emptySet<String>(), settingsRepository.state.value.reactivatedCompletedContentIds)
        assertEquals(emptySet<String>(), viewModel.uiState.reactivatedCompletedContentIds)
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun triggerDebugIntervention_recordsStructuredDelayReturnEvents() = runTest {
        val delayGate = InMemoryDelayGate()
        val analyticsTracker = InMemoryAnalyticsTracker()
        val viewModel = createViewModel(
            analyticsTracker = analyticsTracker,
            delayGate = delayGate,
        )

        advanceUntilIdle()
        viewModel.completeOnboarding()
        advanceUntilIdle()
        viewModel.triggerDebugIntervention(nowMillis = 1_000L)
        advanceUntilIdle()
        viewModel.delayFor15Minutes()
        advanceUntilIdle()

        viewModel.triggerDebugIntervention(nowMillis = 2_000L)
        advanceUntilIdle()

        val returnEvents = analyticsTracker.allEvents().filter {
            it.type.name.startsWith("RETURN_TO_APP")
        }
        assertTrue(returnEvents.isNotEmpty())
        assertTrue(returnEvents.all { it.interventionId != null })
        assertTrue(returnEvents.all { it.metadata["delayReturnOrigin"] == "active_delay" })
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun delayActionKeepsShortAlternativeAvailableWithSeparateAnalytics() = runTest {
        val primary = savedUserLink(id = "long-link", durationMinutes = 12)
        val backup = savedUserLink(id = "short-link", durationMinutes = 4)
        val analyticsTracker = InMemoryAnalyticsTracker()
        val viewModel = createViewModel(
            contentRepository = FakeContentRepository(extraItems = listOf(primary, backup)),
            recommendationEngine = FixedRecommendationEngine(
                RecommendationSet(
                    primary = primary,
                    backups = listOf(backup),
                    inventoryShortage = true,
                    generatedAtMillis = 1_000L,
                ),
            ),
            analyticsTracker = analyticsTracker,
        )

        advanceUntilIdle()
        viewModel.completeOnboarding()
        advanceUntilIdle()
        viewModel.triggerDebugIntervention(nowMillis = 1_000L)
        advanceUntilIdle()
        viewModel.delayFor15Minutes()
        advanceUntilIdle()

        assertEquals(MainScreen.Home, viewModel.uiState.screen)
        assertEquals("short-link", viewModel.uiState.activeDelaySuggestion?.id)

        viewModel.startActiveDelayAlternative()
        advanceUntilIdle()

        assertEquals(MainScreen.ExternalHandoff, viewModel.uiState.screen)
        assertEquals("short-link", viewModel.uiState.currentContent?.id)
        val event = analyticsTracker.allEvents().first { it.type == AnalyticsEventType.DELAY_ALTERNATIVE_STARTED }
        assertEquals("short-link", event.contentId)
        assertEquals("active_delay_card", event.metadata["origin"])
        assertNotNull(event.semanticKey)
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun finishReadingRemovesCompletedContentFromActiveDelaySuggestion() = runTest {
        val contentRepository = FakeContentRepository()
        val primary = contentRepository.inventory().first { it.id == "p1" }
        val viewModel = createViewModel(
            contentRepository = contentRepository,
            recommendationEngine = FixedRecommendationEngine(
                RecommendationSet(
                    primary = primary,
                    backups = emptyList(),
                    inventoryShortage = true,
                    generatedAtMillis = 1_000L,
                ),
            ),
        )

        advanceUntilIdle()
        viewModel.completeOnboarding()
        advanceUntilIdle()
        viewModel.triggerDebugIntervention(nowMillis = 1_000L)
        advanceUntilIdle()
        viewModel.delayFor15Minutes()
        advanceUntilIdle()
        assertEquals("p1", viewModel.uiState.activeDelaySuggestion?.id)

        viewModel.startActiveDelayAlternative()
        advanceUntilIdle()
        viewModel.finishReading()
        advanceUntilIdle()

        assertTrue("p1" in viewModel.uiState.completedContentIds)
        assertEquals(null, viewModel.uiState.activeDelaySuggestion)
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun finishMeditationResetKeepsMeditationAsActiveDelaySuggestion() = runTest {
        val meditation = meditationTimerContentItem(3)
        val settingsRepository = FakeSettingsRepository(
            initial = completedSettings(
                selectedAppPackages = SupportedCatalog.distractingApps.take(3).mapTo(mutableSetOf(), DistractingApp::packageName),
            ).copy(
                preferredTopics = setOf(TopicTag.PSYCHOLOGY),
                preferredDurationBucket = DurationBucket.QUICK,
                selectedPackIds = emptySet(),
                contentPriority = ContentPriority.MEDITATION,
            ),
        )
        val viewModel = createViewModel(
            settingsRepository = settingsRepository,
            recommendationEngine = FixedRecommendationEngine(
                RecommendationSet(
                    primary = meditation,
                    backups = emptyList(),
                    inventoryShortage = true,
                    generatedAtMillis = 1_000L,
                ),
            ),
        )

        advanceUntilIdle()
        viewModel.triggerDebugIntervention(nowMillis = 1_000L)
        advanceUntilIdle()
        viewModel.delayFor15Minutes()
        advanceUntilIdle()
        assertEquals(MEDITATION_TIMER_CONTENT_ID, viewModel.uiState.activeDelaySuggestion?.id)

        viewModel.startActiveDelayAlternative()
        advanceUntilIdle()
        viewModel.finishMeditationReset(nowMillis = 5_000L)
        advanceUntilIdle()

        assertFalse(MEDITATION_TIMER_CONTENT_ID in viewModel.uiState.completedContentIds)
        assertEquals(MEDITATION_TIMER_CONTENT_ID, viewModel.uiState.activeDelaySuggestion?.id)
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun toggleSettingsAppRecalculatesActiveDelaySuggestionForNewSelectedApp() = runTest {
        val delayGate = InMemoryDelayGate()
        val viewModel = createViewModel(delayGate = delayGate)

        advanceUntilIdle()
        viewModel.completeOnboarding()
        advanceUntilIdle()

        val firstTarget = viewModel.uiState.selectedTargetApp!!
        val remainingPackages = viewModel.uiState.availableTargetApps
            .mapTo(mutableSetOf(), DistractingApp::packageName)
            .minus(firstTarget.packageName)
        val nextTarget = SupportedCatalog.distractingApps.first { it.packageName in remainingPackages }
        val nowMillis = System.currentTimeMillis()
        delayGate.storeDelay(
            targetApp = firstTarget,
            nowMillis = nowMillis,
            primaryContentId = "p1",
            backupContentIds = listOf("s1"),
        )
        delayGate.storeDelay(
            targetApp = nextTarget,
            nowMillis = nowMillis,
            primaryContentId = "p1",
        )

        viewModel.selectTargetApp(firstTarget)
        advanceUntilIdle()

        assertEquals("s1", viewModel.uiState.activeDelaySuggestion?.id)

        viewModel.toggleSettingsApp(firstTarget)
        advanceUntilIdle()

        assertEquals(nextTarget.packageName, viewModel.uiState.selectedTargetApp?.packageName)
        assertEquals(nextTarget.packageName, viewModel.uiState.activeDelayWindow?.targetAppPackage)
        assertEquals("p1", viewModel.uiState.activeDelaySuggestion?.id)
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun startActiveDelayAlternativeRefusesExpiredDelayWindow() = runTest {
        var nowMillis = System.currentTimeMillis()
        val analyticsTracker = InMemoryAnalyticsTracker()
        val viewModel = createViewModel(
            analyticsTracker = analyticsTracker,
            nowProvider = { nowMillis },
        )

        advanceUntilIdle()
        viewModel.completeOnboarding()
        advanceUntilIdle()
        viewModel.triggerDebugIntervention(nowMillis = nowMillis)
        advanceUntilIdle()
        viewModel.delayFor15Minutes()
        advanceUntilIdle()

        nowMillis = viewModel.uiState.activeDelayWindow!!.endsAtMillis
        viewModel.startActiveDelayAlternative()
        advanceUntilIdle()

        assertEquals(MainScreen.Home, viewModel.uiState.screen)
        assertEquals(null, viewModel.uiState.currentContent)
        assertEquals(null, viewModel.uiState.activeDelayWindow)
        assertFalse(analyticsTracker.allEvents().any { it.type == AnalyticsEventType.DELAY_ALTERNATIVE_STARTED })
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun startActiveDelayAlternativeRefusesUnavailableSuggestion() = runTest {
        val primary = savedUserLink(id = "long-link", durationMinutes = 12)
        val backup = savedUserLink(id = "short-link", durationMinutes = 4)
        val contentRepository = FakeContentRepository(extraItems = listOf(primary, backup))
        val analyticsTracker = InMemoryAnalyticsTracker()
        val viewModel = createViewModel(
            contentRepository = contentRepository,
            recommendationEngine = FixedRecommendationEngine(
                RecommendationSet(
                    primary = primary,
                    backups = listOf(backup),
                    inventoryShortage = true,
                    generatedAtMillis = 1_000L,
                ),
            ),
            analyticsTracker = analyticsTracker,
        )

        advanceUntilIdle()
        viewModel.completeOnboarding()
        advanceUntilIdle()
        viewModel.triggerDebugIntervention(nowMillis = 1_000L)
        advanceUntilIdle()
        viewModel.delayFor15Minutes()
        advanceUntilIdle()

        contentRepository.setExtraItems(
            listOf(
                primary.copy(availability = ContentAvailability.UNAVAILABLE),
                backup.copy(availability = ContentAvailability.UNAVAILABLE),
            ),
        )
        viewModel.startActiveDelayAlternative()
        advanceUntilIdle()

        assertEquals(MainScreen.Home, viewModel.uiState.screen)
        assertEquals(null, viewModel.uiState.currentContent)
        assertEquals(null, viewModel.uiState.activeDelaySuggestion)
        assertEquals("No paused alternative is available right now.", viewModel.uiState.latestMessage)
        assertFalse(analyticsTracker.allEvents().any { it.type == AnalyticsEventType.DELAY_ALTERNATIVE_STARTED })
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun triggerDebugIntervention_preservesExpiredDelayProvenanceAcrossActiveDelayReads() = runTest {
        val delayGate = InMemoryDelayGate()
        val analyticsTracker = InMemoryAnalyticsTracker()
        val viewModel = createViewModel(
            analyticsTracker = analyticsTracker,
            delayGate = delayGate,
        )

        advanceUntilIdle()
        viewModel.completeOnboarding()
        advanceUntilIdle()

        val targetApp = viewModel.uiState.selectedTargetApp!!
        delayGate.storeDelay(
            targetApp = targetApp,
            nowMillis = 1_000L,
            durationMinutes = 15,
            interventionId = "delay-intervention",
            interventionShownAtMillis = 900L,
            primaryContentId = "p1",
            backupContentIds = listOf("s1"),
        )
        val expiredAt = 1_000L + 16 * 60_000L

        assertEquals(null, delayGate.activeDelay(targetApp = targetApp, nowMillis = expiredAt))

        viewModel.triggerDebugIntervention(nowMillis = expiredAt)
        advanceUntilIdle()

        val events = analyticsTracker.allEvents()
        val expiredDelayEvent = events.firstOrNull { it.type == AnalyticsEventType.RETURN_AFTER_DELAY_ENDED }
        val within60 = events.firstOrNull { it.type == AnalyticsEventType.RETURN_TO_APP_WITHIN_60_MINUTES }

        assertNotNull(expiredDelayEvent)
        assertEquals("delay-intervention", expiredDelayEvent?.interventionId)
        assertEquals("after_delay_expired", within60?.metadata?.get("delayReturnOrigin"))
        assertEquals("delay", within60?.metadata?.get("returnAttribution"))
        assertEquals(MainScreen.Intervention, viewModel.uiState.screen)
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun expiredDelayReturnDoesNotAlsoRecordSessionReturnForSameAttempt() = runTest {
        val delayGate = InMemoryDelayGate()
        val analyticsTracker = InMemoryAnalyticsTracker()
        val historyRepository = FakeHistoryRepository().apply {
            recordAcceptedSession(
                targetApp = SupportedCatalog.distractingApps.first(),
                interventionId = "session-intervention",
                interventionShownAtMillis = 1_000L,
                primaryContentId = "p1",
                backupContentIds = listOf("s1"),
                content = FakeContentRepository().inventory().first(),
                source = RecommendationSource.PRIMARY,
                acceptedAtMillis = 1_000L,
            )
        }
        val viewModel = createViewModel(
            analyticsTracker = analyticsTracker,
            delayGate = delayGate,
            historyRepository = historyRepository,
        )

        advanceUntilIdle()
        viewModel.completeOnboarding()
        advanceUntilIdle()

        val targetApp = viewModel.uiState.selectedTargetApp!!
        delayGate.storeDelay(
            targetApp = targetApp,
            nowMillis = 2_000L,
            durationMinutes = 15,
            interventionId = "delay-intervention",
            interventionShownAtMillis = 1_900L,
            primaryContentId = "p1",
            backupContentIds = listOf("s1"),
        )
        val expiredAt = 2_000L + 16 * 60_000L

        viewModel.triggerDebugIntervention(nowMillis = expiredAt)
        advanceUntilIdle()

        val within60Events = analyticsTracker.allEvents().filter {
            it.type == AnalyticsEventType.RETURN_TO_APP_WITHIN_60_MINUTES
        }
        assertEquals(1, within60Events.size)
        assertEquals("delay", within60Events.single().metadata["returnAttribution"])
        assertEquals("after_delay_expired", within60Events.single().metadata["delayReturnOrigin"])
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun triggerDebugIntervention_ignoresCompletedMeditationForExclusions() = runTest {
        val recommendationEngine = RecordingRecommendationEngine()
        val historyRepository = FakeHistoryRepository(
            initialCompletedIds = setOf("p1", MEDITATION_TIMER_CONTENT_ID),
        )
        val viewModel = createViewModel(
            recommendationEngine = recommendationEngine,
            historyRepository = historyRepository,
        )

        advanceUntilIdle()
        viewModel.completeOnboarding()
        advanceUntilIdle()
        viewModel.triggerDebugIntervention(nowMillis = 2_000L)
        advanceUntilIdle()

        assertTrue(recommendationEngine.lastInventory.any { it.id == MEDITATION_TIMER_CONTENT_ID })
        assertFalse(MEDITATION_TIMER_CONTENT_ID in viewModel.uiState.completedContentIds)
        assertEquals(setOf("p1"), recommendationEngine.lastExcludedContentIds)
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun triggerDebugIntervention_ignoresStaleCompletedMeditationReadingProgress() = runTest {
        val recommendationEngine = RecordingRecommendationEngine()
        val readingProgressRepository = FakeReadingProgressRepository(
            initialProgress = listOf(
                ReadingProgress(
                    contentId = MEDITATION_TIMER_CONTENT_ID,
                    progressPercent = 100,
                    lastVisibleParagraphIndex = 1,
                    paragraphCount = 1,
                    updatedAtMillis = 1_000L,
                    completedAtMillis = 1_000L,
                ),
            ),
        )
        val viewModel = createViewModel(
            recommendationEngine = recommendationEngine,
            readingProgressRepository = readingProgressRepository,
        )

        advanceUntilIdle()
        viewModel.completeOnboarding()
        advanceUntilIdle()
        viewModel.triggerDebugIntervention(nowMillis = 2_000L)
        advanceUntilIdle()

        assertTrue(recommendationEngine.lastInventory.any { it.id == MEDITATION_TIMER_CONTENT_ID })
        assertFalse(MEDITATION_TIMER_CONTENT_ID in viewModel.uiState.completedContentIds)
        assertFalse(MEDITATION_TIMER_CONTENT_ID in recommendationEngine.lastExcludedContentIds)
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun triggerDebugIntervention_keepsMeditationAvailableWhenReadingInventoryIsCompleted() = runTest {
        val analyticsTracker = InMemoryAnalyticsTracker()
        val historyRepository = FakeHistoryRepository(initialCompletedIds = setOf("p1", "s1"))
        val viewModel = createViewModel(
            analyticsTracker = analyticsTracker,
            historyRepository = historyRepository,
        )

        advanceUntilIdle()
        viewModel.completeOnboarding()
        advanceUntilIdle()
        viewModel.triggerDebugIntervention(nowMillis = 2_000L)
        advanceUntilIdle()

        assertEquals(MEDITATION_TIMER_CONTENT_ID, viewModel.uiState.currentRecommendationSet?.primary?.id)
        assertEquals(MainScreen.Intervention, viewModel.uiState.screen)
        val event = analyticsTracker.allEvents().first { it.type == AnalyticsEventType.INTERVENTION_SHOWN }
        assertEquals("2", event.metadata["eligibleInventoryCount"])
        assertEquals("1", event.metadata["eligibleEditorialCount"])
        assertEquals("1", event.metadata["eligibleMeditationCount"])
        assertEquals("0", event.metadata["eligibleUserLinkCount"])
        assertEquals("0", event.metadata["unavailableUserLinkCount"])
        assertEquals("2", event.metadata["completedContentCount"])
        assertEquals("philosophy", event.metadata["selectedPackIds"])
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun applySettingsDropsMeditationFromReactivatedCompletedContent() = runTest {
        val meditation = meditationTimerContentItem(3)
        val settingsRepository = FakeSettingsRepository(
            initial = completedSettings(
                selectedAppPackages = SupportedCatalog.distractingApps.take(3).mapTo(mutableSetOf(), DistractingApp::packageName),
            ).copy(
                preferredTopics = setOf(TopicTag.PSYCHOLOGY),
                preferredDurationBucket = DurationBucket.QUICK,
                selectedPackIds = emptySet(),
                contentPriority = ContentPriority.MEDITATION,
                reactivatedCompletedContentIds = setOf(MEDITATION_TIMER_CONTENT_ID),
            ),
        )
        val viewModel = createViewModel(
            settingsRepository = settingsRepository,
            recommendationEngine = FixedRecommendationEngine(
                RecommendationSet(
                    primary = meditation,
                    backups = emptyList(),
                    inventoryShortage = true,
                    generatedAtMillis = 1_000L,
                ),
            ),
        )

        advanceUntilIdle()
        assertEquals(emptySet<String>(), viewModel.uiState.reactivatedCompletedContentIds)
        assertEquals(emptySet<String>(), settingsRepository.state.value.reactivatedCompletedContentIds)
        viewModel.triggerDebugIntervention(nowMillis = 1_000L)
        advanceUntilIdle()
        viewModel.acceptPrimary()
        advanceUntilIdle()
        viewModel.finishMeditationReset(nowMillis = 5_000L)
        advanceUntilIdle()

        assertFalse(MEDITATION_TIMER_CONTENT_ID in viewModel.uiState.completedContentIds)
        assertEquals(emptySet<String>(), settingsRepository.state.value.reactivatedCompletedContentIds)
        assertEquals(emptySet<String>(), viewModel.uiState.reactivatedCompletedContentIds)
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun finishMeditationReset_doesNotCreateCompletedContentEvenForDefensiveDirectOpen() = runTest {
        val meditation = meditationTimerContentItem(3)
        val analyticsTracker = InMemoryAnalyticsTracker()
        val recommendationEngine = RecordingRecommendationEngine()
        val viewModel = createViewModel(
            analyticsTracker = analyticsTracker,
            recommendationEngine = recommendationEngine,
        )

        advanceUntilIdle()
        viewModel.completeOnboarding()
        advanceUntilIdle()
        viewModel.openLibraryItem(meditation)
        advanceUntilIdle()
        viewModel.finishMeditationReset(nowMillis = 5_000L)
        advanceUntilIdle()

        assertFalse(MEDITATION_TIMER_CONTENT_ID in viewModel.uiState.completedContentIds)
        assertTrue(
            analyticsTracker.allEvents().any {
                it.type == AnalyticsEventType.MEDITATION_TIMER_COMPLETED &&
                    it.contentId == MEDITATION_TIMER_CONTENT_ID &&
                    it.sessionId == null
            },
        )

        viewModel.triggerDebugIntervention(nowMillis = 6_000L)
        advanceUntilIdle()

        assertFalse(MEDITATION_TIMER_CONTENT_ID in recommendationEngine.lastExcludedContentIds)
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun inventoryShortageRecordsSourceDiagnostics() = runTest {
        val analyticsTracker = InMemoryAnalyticsTracker()
        val viewModel = createViewModel(analyticsTracker = analyticsTracker)

        advanceUntilIdle()
        viewModel.completeOnboarding()
        advanceUntilIdle()
        viewModel.triggerDebugIntervention(nowMillis = 2_000L)
        advanceUntilIdle()

        val event = analyticsTracker.allEvents().first { it.type == AnalyticsEventType.INVENTORY_SHORTAGE }
        assertEquals("2", event.metadata["eligibleInventoryCount"])
        assertEquals("1", event.metadata["eligibleEditorialCount"])
        assertEquals("1", event.metadata["eligibleMeditationCount"])
        assertEquals("0", event.metadata["eligibleUserLinkCount"])
        assertEquals("0", event.metadata["unavailableUserLinkCount"])
        assertEquals("0", event.metadata["completedContentCount"])
        assertEquals("philosophy", event.metadata["selectedPackIds"])
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun triggerDebugIntervention_preservesExpiredDelayProvenanceWithPreferencesDelayGate() = runTest {
        val file = File.createTempFile("delay-gate-viewmodel", ".preferences_pb").apply { deleteOnExit() }
        val delayGate = PreferencesDelayGate(
            dataStore = testDataStore(file),
            scope = backgroundScope,
        )
        delayGate.observeReady().first { it }
        val analyticsTracker = InMemoryAnalyticsTracker()
        val viewModel = createViewModel(
            analyticsTracker = analyticsTracker,
            delayGate = delayGate,
        )

        advanceUntilIdle()
        assertFalse(viewModel.uiState.isLoadingSettings)
        viewModel.completeOnboarding()
        advanceUntilIdle()

        val targetApp = viewModel.uiState.selectedTargetApp!!
        val created = delayGate.storeDelay(
            targetApp = targetApp,
            nowMillis = 1_000L,
            durationMinutes = 15,
            interventionId = "delay-intervention",
            interventionShownAtMillis = 900L,
            primaryContentId = "p1",
            backupContentIds = listOf("s1"),
        )
        val expiredAt = 1_000L + 16 * 60_000L

        assertEquals(null, delayGate.activeDelay(targetApp = targetApp, nowMillis = expiredAt))

        viewModel.triggerDebugIntervention(nowMillis = expiredAt)
        advanceUntilIdle()

        val events = analyticsTracker.allEvents()
        val expiredDelayEvent = events.firstOrNull { it.type == AnalyticsEventType.RETURN_AFTER_DELAY_ENDED }
        val within60 = events.firstOrNull { it.type == AnalyticsEventType.RETURN_TO_APP_WITHIN_60_MINUTES }
        val postConsumeInspection = delayGate.inspectDelay(targetApp = targetApp, nowMillis = expiredAt)

        assertNotNull(expiredDelayEvent)
        assertEquals("delay-intervention", expiredDelayEvent?.interventionId)
        assertEquals("after_delay_expired", within60?.metadata?.get("delayReturnOrigin"))
        assertEquals("delay", within60?.metadata?.get("returnAttribution"))
        assertEquals(null, postConsumeInspection.activeWindow)
        assertEquals(null, postConsumeInspection.expiredWindow)
        assertEquals(false, delayGate.consumeExpiredDelay(targetApp = targetApp, delayId = created.id, nowMillis = expiredAt))
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun requestSystemInterception_opensFixtureTargetAndOpenAnywayReturnsTrue() = runTest {
        val fixtureTarget = FixtureTargetRegistry.fixtureDistractors.first()
        val viewModel = createViewModel(
            settingsRepository = FakeSettingsRepository(
                initial = completedSettings(selectedAppPackages = setOf(fixtureTarget.packageName)),
            ),
        )

        advanceUntilIdle()

        viewModel.requestSystemInterception(targetAppPackage = fixtureTarget.packageName, nowMillis = 5_000L)
        advanceUntilIdle()

        assertEquals(MainScreen.Intervention, viewModel.uiState.screen)
        assertEquals(InterventionOrigin.SYSTEM, viewModel.uiState.currentInterventionOrigin)
        assertEquals(fixtureTarget.packageName, viewModel.uiState.selectedTargetApp?.packageName)

        val exitsToTarget = viewModel.openAnyway()

        assertTrue(exitsToTarget)
        assertTrue(
            InterceptionRuntimeGate.shouldSuppress(
                targetAppPackage = fixtureTarget.packageName,
                nowMillis = 5_001L,
            ),
        )
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun openAnyway_usesConfiguredUnlockWindowAndSuppressesRepeatedSystemIntervention() = runTest {
        val fixtureTarget = FixtureTargetRegistry.fixtureDistractors.first()
        var nowMillis = 10_000L
        val analyticsTracker = InMemoryAnalyticsTracker()
        val viewModel = createViewModel(
            settingsRepository = FakeSettingsRepository(
                initial = completedSettings(selectedAppPackages = setOf(fixtureTarget.packageName))
                    .copy(openAnywayUnlockMinutes = 120),
            ),
            analyticsTracker = analyticsTracker,
            nowProvider = { nowMillis },
        )

        advanceUntilIdle()

        viewModel.requestSystemInterception(targetAppPackage = fixtureTarget.packageName, nowMillis = nowMillis)
        advanceUntilIdle()
        assertEquals(MainScreen.Intervention, viewModel.uiState.screen)

        assertTrue(viewModel.openAnyway())
        advanceUntilIdle()

        assertTrue(
            InterceptionRuntimeGate.shouldSuppress(
                targetAppPackage = fixtureTarget.packageName,
                nowMillis = 10_000L + 119 * 60_000L,
            ),
        )
        val openEvent = analyticsTracker.allEvents().first { it.type == AnalyticsEventType.OPEN_ANYWAY_SELECTED }
        assertEquals("120", openEvent.metadata["openAnywayUnlockMinutes"])

        nowMillis = 20_000L
        viewModel.requestSystemInterception(targetAppPackage = fixtureTarget.packageName, nowMillis = nowMillis)
        advanceUntilIdle()

        assertEquals(MainScreen.Home, viewModel.uiState.screen)
        assertEquals(null, viewModel.uiState.currentRecommendationSet)
        assertEquals(null, viewModel.uiState.currentInterventionOrigin)
        assertFalse(
            InterceptionRuntimeGate.shouldSuppress(
                targetAppPackage = fixtureTarget.packageName,
                nowMillis = 10_000L + 121 * 60_000L,
            ),
        )
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun requestSystemInterception_ignoresUnselectedTargetPackage() = runTest {
        val viewModel = createViewModel()

        advanceUntilIdle()
        viewModel.completeOnboarding()
        advanceUntilIdle()

        val fixtureTarget = FixtureTargetRegistry.fixtureDistractors.first()
        viewModel.requestSystemInterception(targetAppPackage = fixtureTarget.packageName, nowMillis = 5_000L)
        advanceUntilIdle()

        assertEquals(MainScreen.Home, viewModel.uiState.screen)
        assertEquals(null, viewModel.uiState.currentInterventionOrigin)
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun requestSystemInterception_recordsDegradedPerformanceWhenShownTooLate() = runTest {
        val analyticsTracker = InMemoryAnalyticsTracker()
        val fixtureTarget = FixtureTargetRegistry.fixtureDistractors.first()
        val viewModel = createViewModel(
            settingsRepository = FakeSettingsRepository(
                initial = completedSettings(selectedAppPackages = setOf(fixtureTarget.packageName)),
            ),
            analyticsTracker = analyticsTracker,
            nowProvider = { 5_000L },
        )

        advanceUntilIdle()

        viewModel.requestSystemInterception(targetAppPackage = fixtureTarget.packageName, nowMillis = 1_000L)
        advanceUntilIdle()

        val degradedEvent = analyticsTracker.allEvents().firstOrNull {
            it.type == AnalyticsEventType.INTERVENTION_DEGRADED_PERFORMANCE
        }
        assertNotNull(degradedEvent)
        assertEquals("4000", degradedEvent?.metadata?.get("interceptionDelayMillis"))
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun requestSystemInterception_waitsUntilHydrationCompletes() = runTest {
        val fixtureTarget = FixtureTargetRegistry.fixtureDistractors.first()
        val settingsRepository = FakeSettingsRepository(
            initial = completedSettings(selectedAppPackages = setOf(fixtureTarget.packageName)),
        )
        val historyRepository = FakeHistoryRepository(isReady = false)
        val delayGate = FakeDelayGate(isReady = false)
        val viewModel = createViewModel(
            settingsRepository = settingsRepository,
            historyRepository = historyRepository,
            delayGate = delayGate,
        )

        viewModel.requestSystemInterception(
            targetAppPackage = fixtureTarget.packageName,
            nowMillis = 7_000L,
        )
        advanceUntilIdle()
        assertTrue(viewModel.uiState.isLoadingSettings)
        assertEquals(MainScreen.Home, viewModel.uiState.screen)

        historyRepository.setReady(true)
        delayGate.setReady(true)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.isLoadingSettings)
        assertEquals(MainScreen.Intervention, viewModel.uiState.screen)
        assertEquals(InterventionOrigin.SYSTEM, viewModel.uiState.currentInterventionOrigin)
    }

    @Test
    fun previewAccountLightImportShowsValidationErrorWithoutMutatingSettings() = runTest {
        val settingsRepository = FakeSettingsRepository(
            initial = completedSettings(selectedAppPackages = setOf("com.instagram.android")).copy(
                themeMode = AppThemeMode.DARK,
                contentPriority = ContentPriority.MY_FILES,
            ),
        )
        val viewModel = createViewModel(settingsRepository = settingsRepository)
        advanceUntilIdle()

        viewModel.previewAccountLightImport("{not-json")

        assertEquals("Portable profile is not valid JSON.", viewModel.uiState.accountLightImportError)
        assertEquals(null, viewModel.uiState.accountLightImportPreview)
        assertEquals(AppThemeMode.DARK, settingsRepository.state.value.themeMode)
        assertEquals(ContentPriority.MY_FILES, settingsRepository.state.value.contentPriority)
    }

    @Test
    fun configuredProfileAutosaveWritesPortableProfileAndStatus() = runTest {
        val settingsRepository = FakeSettingsRepository(
            initial = completedSettings(selectedAppPackages = setOf("com.instagram.android")),
        )
        val profileWriter = RecordingAccountLightProfileAutosaveWriter()
        val persistedUris = mutableListOf<String>()
        val viewModel = createViewModel(
            settingsRepository = settingsRepository,
            accountLightProfileAutosaveWriter = profileWriter,
            nowProvider = { 5_000L },
        )
        advanceUntilIdle()

        viewModel.configureAccountLightProfileAutosave(
            uri = "content://tree/profile-folder",
            displayName = "QA profile",
            persistWritePermission = persistedUris::add,
            nowMillis = 6_000L,
        )
        advanceUntilIdle()

        assertEquals(listOf("content://tree/profile-folder"), persistedUris)
        assertEquals("content://tree/profile-folder", settingsRepository.state.value.profileAutosaveUri)
        assertEquals("QA profile", settingsRepository.state.value.profileAutosaveDisplayName)
        assertEquals(6_000L, settingsRepository.state.value.profileAutosaveLastSuccessfulAtMillis)
        assertEquals(null, settingsRepository.state.value.profileAutosaveLastError)
        assertEquals("Profile backup destination changed.", viewModel.uiState.latestMessage)
        val write = profileWriter.writes.single()
        assertEquals("content://tree/profile-folder", write.first)
        assertEquals("quality-alternative-profile.json", write.second)
        assertTrue(write.third.contains("\"schemaVersion\""))
        assertTrue(write.third.contains("\"provider\": \"ANDROID_DOCUMENT_TREE\""))
        assertFalse(write.third.contains("content://tree/profile-folder"))
    }

    @Test
    fun profileAutosaveFailureKeepsDestinationAndDoesNotBlockAppUse() = runTest {
        val settingsRepository = FakeSettingsRepository(
            initial = completedSettings(selectedAppPackages = setOf("com.instagram.android")),
        )
        val profileWriter = RecordingAccountLightProfileAutosaveWriter(
            failure = IllegalStateException("No provider for content://tree/missing"),
        )
        val viewModel = createViewModel(
            settingsRepository = settingsRepository,
            accountLightProfileAutosaveWriter = profileWriter,
        )
        advanceUntilIdle()

        viewModel.configureAccountLightProfileAutosave(
            uri = "content://tree/missing",
            displayName = "Missing folder",
            nowMillis = 6_000L,
        )
        advanceUntilIdle()

        assertEquals("content://tree/missing", settingsRepository.state.value.profileAutosaveUri)
        assertEquals("Missing folder", settingsRepository.state.value.profileAutosaveDisplayName)
        assertEquals(null, settingsRepository.state.value.profileAutosaveLastSuccessfulAtMillis)
        assertEquals("Choose the folder again or retry.", settingsRepository.state.value.profileAutosaveLastError)
        assertEquals("Profile backup destination changed, but the first write failed.", viewModel.uiState.latestMessage)
        viewModel.openSettings()
        assertEquals(MainScreen.Settings, viewModel.uiState.screen)
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun configuredProfileAutosaveRunsAfterPortableProfileMutations() = runTest {
        val settingsRepository = FakeSettingsRepository(
            initial = completedSettings(selectedAppPackages = setOf("com.instagram.android")).copy(
                profileAutosaveUri = "content://tree/profile-folder",
                profileAutosaveDisplayName = "QA profile",
            ),
        )
        val userLinkRepository = FakeUserLinkRepository()
        val userDocumentRepository = FakeUserDocumentRepository()
        val readingProgressRepository = FakeReadingProgressRepository()
        val profileWriter = RecordingAccountLightProfileAutosaveWriter()
        val viewModel = createViewModel(
            settingsRepository = settingsRepository,
            userLinkRepository = userLinkRepository,
            userDocumentRepository = userDocumentRepository,
            readingProgressRepository = readingProgressRepository,
            accountLightProfileAutosaveWriter = profileWriter,
        )
        advanceUntilIdle()

        viewModel.openAddLink()
        viewModel.updateAddLinkUrl("https://example.com/essay")
        viewModel.updateAddLinkTitle("Saved essay")
        viewModel.updateAddLinkDuration("9")
        viewModel.toggleAddLinkTopic(TopicTag.SCIENCE)
        viewModel.saveUserLink(nowMillis = 2_100L)
        advanceUntilIdle()

        assertEquals(1, profileWriter.writes.size)
        assertTrue(profileWriter.writes.last().third.contains("Saved essay"))
        assertEquals(2_100L, settingsRepository.state.value.profileAutosaveLastSuccessfulAtMillis)

        viewModel.prepareUserDocumentImport(
            uri = "content://quality/notes",
            displayName = "notes.md",
            mimeType = "text/markdown",
        )
        viewModel.toggleAddDocumentTopic(TopicTag.SCIENCE)
        viewModel.saveUserDocument(nowMillis = 2_200L)
        advanceUntilIdle()

        assertEquals(2, profileWriter.writes.size)
        assertTrue(profileWriter.writes.last().third.contains("notes"))

        val document = userDocumentRepository.documents.value.single()
        viewModel.openLibraryItem(document)
        advanceUntilIdle()
        viewModel.saveCurrentReadingProgress(
            progressPercent = 35,
            lastVisibleParagraphIndex = 2,
            paragraphCount = 10,
            nowMillis = 2_300L,
        )
        advanceUntilIdle()

        assertEquals(3, profileWriter.writes.size)
        assertTrue(profileWriter.writes.last().third.contains("\"progressPercent\""))

        val link = userLinkRepository.links.value.single()
        viewModel.togglePriorityContent(link)
        advanceUntilIdle()

        assertEquals(4, profileWriter.writes.size)
        assertTrue(profileWriter.writes.last().third.contains("\"priorityContentIds\""))

        viewModel.finishReading()
        advanceUntilIdle()

        assertEquals(5, profileWriter.writes.size)
        assertTrue(profileWriter.writes.last().third.contains("\"completedAtMillis\""))

        viewModel.openLibrary()
        viewModel.toggleLibraryManageMode()
        viewModel.toggleLibraryContentSelection(link)
        viewModel.toggleLibraryContentSelection(document)
        viewModel.deleteSelectedLibraryContent(nowMillis = 2_500L)
        advanceUntilIdle()

        assertEquals(6, profileWriter.writes.size)
        assertFalse(profileWriter.writes.last().third.contains("Saved essay"))
        assertFalse(profileWriter.writes.last().third.contains("notes"))
        assertEquals(2_500L, settingsRepository.state.value.profileAutosaveLastSuccessfulAtMillis)
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun accountLightMergeImportKeepsLocalPortableSettings() = runTest {
        val importedSettingsRepository = FakeSettingsRepository(
            initial = completedSettings(selectedAppPackages = setOf("com.instagram.android")).copy(
                themeMode = AppThemeMode.DARK,
                contentPriority = ContentPriority.MY_FILES,
                readerFontScale = 1.35,
            ),
        )
        val targetSettingsRepository = FakeSettingsRepository(
            initial = completedSettings(selectedAppPackages = setOf("com.google.android.youtube")).copy(
                themeMode = AppThemeMode.LIGHT,
                contentPriority = ContentPriority.BALANCED,
                readerFontScale = 1.0,
                profileAutosaveUri = "content://tree/profile-folder",
                profileAutosaveDisplayName = "QA profile",
            ),
        )
        val profileWriter = RecordingAccountLightProfileAutosaveWriter()
        val viewModel = createViewModel(
            settingsRepository = targetSettingsRepository,
            accountLightProfileAutosaveWriter = profileWriter,
        )
        val rawJson = AccountLightProfileExporter(
            settingsRepository = importedSettingsRepository,
            appVersionName = "0.8.1-alpha",
            appVersionCode = 13,
        ).exportSettingsOnlyProfileJson(nowMillis = 20_000L)
        advanceUntilIdle()

        viewModel.previewAccountLightImport(rawJson)
        viewModel.applyAccountLightMergeImport()
        advanceUntilIdle()

        assertEquals(null, viewModel.uiState.accountLightImportError)
        assertEquals(null, viewModel.uiState.accountLightImportPreview)
        assertEquals("Profile merged. Local settings were kept.", viewModel.uiState.accountLightStatus)
        assertEquals(AppThemeMode.LIGHT, targetSettingsRepository.state.value.themeMode)
        assertEquals(ContentPriority.BALANCED, targetSettingsRepository.state.value.contentPriority)
        assertEquals(1.0, targetSettingsRepository.state.value.readerFontScale, 0.0)
        assertEquals("Portable profile merged and autosaved.", viewModel.uiState.latestMessage)
        assertEquals("content://tree/profile-folder", profileWriter.writes.single().first)
        assertEquals(1_000L, targetSettingsRepository.state.value.profileAutosaveLastSuccessfulAtMillis)
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun accountLightMergeImportFailureShowsVisibleRollbackError() = runTest {
        val importedLink = savedUserLink(id = "user-link-99999999-9999-4999-8999-999999999999")
        val importedProgress = ReadingProgress(
            contentId = importedLink.id,
            lastVisibleParagraphIndex = 2,
            paragraphCount = 10,
            progressPercent = 20,
            updatedAtMillis = 20_000L,
        )
        val rawJson = AccountLightProfileExporter(
            settingsRepository = FakeSettingsRepository(
                initial = completedSettings(selectedAppPackages = setOf("com.instagram.android")),
            ),
            appVersionName = "0.8.1-alpha",
            appVersionCode = 13,
            userLinkRepository = FakeUserLinkRepository(initialLinks = listOf(importedLink)),
            readingProgressRepository = FakeReadingProgressRepository(initialProgress = listOf(importedProgress)),
        ).exportSettingsOnlyProfileJson(nowMillis = 20_000L)
        val viewModel = createViewModel(
            readingProgressRepository = FakeReadingProgressRepository(throwOnSaveProgress = true),
        )
        advanceUntilIdle()

        viewModel.previewAccountLightImport(rawJson)
        viewModel.applyAccountLightMergeImport()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.isAccountLightImporting)
        assertEquals("Import failed before any settings were changed.", viewModel.uiState.accountLightImportError)
        assertEquals(null, viewModel.uiState.accountLightStatus)
        assertEquals("Portable profile import failed.", viewModel.uiState.latestMessage)
        assertNotNull(viewModel.uiState.accountLightImportPreview)
    }

    @Test
    fun accountLightReplaceImportRequiresConfirmationAndAppliesPortableSettings() = runTest {
        val importedSettingsRepository = FakeSettingsRepository(
            initial = completedSettings(selectedAppPackages = setOf("com.instagram.android")).copy(
                themeMode = AppThemeMode.DARK,
                contentPriority = ContentPriority.MY_FILES,
                readerFontScale = 1.35,
                openAnywayUnlockMinutes = 60,
            ),
        )
        val targetSettingsRepository = FakeSettingsRepository(
            initial = completedSettings(selectedAppPackages = setOf("com.google.android.youtube")).copy(
                themeMode = AppThemeMode.LIGHT,
                contentPriority = ContentPriority.BALANCED,
                readerFontScale = 1.0,
                openAnywayUnlockMinutes = 15,
            ),
        )
        val viewModel = createViewModel(settingsRepository = targetSettingsRepository)
        val rawJson = AccountLightProfileExporter(
            settingsRepository = importedSettingsRepository,
            appVersionName = "0.8.1-alpha",
            appVersionCode = 13,
        ).exportSettingsOnlyProfileJson(nowMillis = 20_000L)
        advanceUntilIdle()

        viewModel.previewAccountLightImport(rawJson)
        assertNotNull(viewModel.uiState.accountLightImportPreview)
        assertFalse(viewModel.uiState.isAccountLightReplaceConfirming)

        viewModel.requestAccountLightReplaceConfirmation()
        assertTrue(viewModel.uiState.isAccountLightReplaceConfirming)

        viewModel.confirmAccountLightReplaceImport()
        advanceUntilIdle()

        assertEquals("Imported profile replaced local portable settings and library.", viewModel.uiState.accountLightStatus)
        assertEquals(null, viewModel.uiState.accountLightImportPreview)
        assertFalse(viewModel.uiState.isAccountLightReplaceConfirming)
        assertEquals(AppThemeMode.DARK, targetSettingsRepository.state.value.themeMode)
        assertEquals(ContentPriority.MY_FILES, targetSettingsRepository.state.value.contentPriority)
        assertEquals(1.35, targetSettingsRepository.state.value.readerFontScale, 0.0)
        assertEquals(60, targetSettingsRepository.state.value.openAnywayUnlockMinutes)
    }

    private fun completedSettings(selectedAppPackages: Set<String>): AppSettings {
        return AppSettings(
            hasCompletedOnboarding = true,
            selectedAppPackages = selectedAppPackages,
            preferredTopics = setOf(TopicTag.PHILOSOPHY, TopicTag.SCIENCE, TopicTag.HISTORY),
            preferredDurationBucket = DurationBucket.FOCUS,
            selectedPackIds = setOf("philosophy"),
        )
    }

    private fun createViewModel(
        settingsRepository: FakeSettingsRepository = FakeSettingsRepository(),
        historyRepository: FakeHistoryRepository = FakeHistoryRepository(),
        analyticsTracker: InMemoryAnalyticsTracker = InMemoryAnalyticsTracker(),
        delayGate: DelayGate = InMemoryDelayGate(),
        contentRepository: ContentRepository = FakeContentRepository(),
        userLinkRepository: UserLinkRepository = FakeUserLinkRepository(),
        userDocumentRepository: UserDocumentRepository = FakeUserDocumentRepository(),
        readingProgressRepository: ReadingProgressRepository = FakeReadingProgressRepository(),
        readingAnnotationRepository: ReadingAnnotationRepository = FakeReadingAnnotationRepository(),
        readingAnnotationExportWriter: ReadingAnnotationExportWriter = RecordingReadingAnnotationExportWriter(),
        readingAnnotationDriveSyncClient: ReadingAnnotationDriveSyncClient = RecordingReadingAnnotationDriveSyncClient(),
        readingAnnotationDriveTokenProvider: ReadingAnnotationDriveTokenProvider = FailingReadingAnnotationDriveTokenProvider(),
        accountLightProfileAutosaveWriter: AccountLightProfileAutosaveWriter = RecordingAccountLightProfileAutosaveWriter(),
        defaultAnnotationExportUri: String? = null,
        defaultAnnotationExportDisplayName: String = "App storage - Annotation sync",
        defaultProfileAutosaveUri: String? = null,
        defaultProfileAutosaveDisplayName: String = "App storage - Profile backup",
        recommendationEngine: RecommendationEngine = DefaultRecommendationEngine(),
        nowProvider: () -> Long = { 1_000L },
    ): MainViewModel {
        return track(
            MainViewModel(
                contentRepository = contentRepository,
                userLinkRepository = userLinkRepository,
                userDocumentRepository = userDocumentRepository,
                settingsRepository = settingsRepository,
                recommendationEngine = recommendationEngine,
                delayGate = delayGate,
                analyticsTracker = analyticsTracker,
                historyRepository = historyRepository,
                readingProgressRepository = readingProgressRepository,
                readingAnnotationRepository = readingAnnotationRepository,
                readingAnnotationExportWriter = readingAnnotationExportWriter,
                readingAnnotationDriveSyncClient = readingAnnotationDriveSyncClient,
                readingAnnotationDriveTokenProvider = readingAnnotationDriveTokenProvider,
                accountLightProfileAutosaveWriter = accountLightProfileAutosaveWriter,
                defaultAnnotationExportUri = defaultAnnotationExportUri,
                defaultAnnotationExportDisplayName = defaultAnnotationExportDisplayName,
                defaultProfileAutosaveUri = defaultProfileAutosaveUri,
                defaultProfileAutosaveDisplayName = defaultProfileAutosaveDisplayName,
                interceptionMonitor = FakeInterceptionMonitor(),
                enableDelayRefreshTicker = false,
                nowProvider = nowProvider,
            ),
        )
    }

    private fun track(viewModel: MainViewModel): MainViewModel {
        createdViewModels += viewModel
        return viewModel
    }

    private class FakeSettingsRepository(
        initial: AppSettings = AppSettings(
            hasCompletedOnboarding = false,
            selectedAppPackages = emptySet(),
            preferredTopics = emptySet(),
            preferredDurationBucket = DurationBucket.FOCUS,
            selectedPackIds = emptySet(),
        ),
    ) : SettingsRepository {
        val state = MutableStateFlow(initial)

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
                meditationDurationMinutes = state.value.meditationDurationMinutes,
                contentPriority = state.value.contentPriority,
                priorityContentIds = state.value.priorityContentIds,
                reactivatedCompletedContentIds = state.value.reactivatedCompletedContentIds,
                openAnywayUnlockMinutes = state.value.openAnywayUnlockMinutes,
                readerFontScale = state.value.readerFontScale,
                interfaceTextScale = state.value.interfaceTextScale,
                annotationExportUri = state.value.annotationExportUri,
                annotationExportDisplayName = state.value.annotationExportDisplayName,
                annotationExportLastSuccessfulAtMillis = state.value.annotationExportLastSuccessfulAtMillis,
                annotationExportLastError = state.value.annotationExportLastError,
                annotationDriveSyncEnabled = state.value.annotationDriveSyncEnabled,
                annotationDriveFolderId = state.value.annotationDriveFolderId,
                annotationDriveLastSuccessfulAtMillis = state.value.annotationDriveLastSuccessfulAtMillis,
                annotationDriveLastError = state.value.annotationDriveLastError,
                profileAutosaveUri = state.value.profileAutosaveUri,
                profileAutosaveDisplayName = state.value.profileAutosaveDisplayName,
                profileAutosaveLastSuccessfulAtMillis = state.value.profileAutosaveLastSuccessfulAtMillis,
                profileAutosaveLastError = state.value.profileAutosaveLastError,
            )
        }

        override suspend fun ensureLocalProfileIdentity(nowMillis: Long) =
            com.qualityalternative.app.domain.model.LocalProfileIdentity(
                profileId = "qa-local-00000000-0000-0000-0000-000000000000",
                createdAtMillis = nowMillis,
            )

        override suspend fun replacePortableSettings(
            settings: AppSettings,
            profileIdentity: com.qualityalternative.app.domain.model.LocalProfileIdentity?,
        ) {
            state.value = state.value.copy(
                hasCompletedOnboarding = settings.hasCompletedOnboarding,
                selectedAppPackages = settings.selectedAppPackages,
                preferredTopics = settings.preferredTopics,
                preferredDurationBucket = settings.preferredDurationBucket,
                selectedPackIds = settings.selectedPackIds,
                themeMode = settings.themeMode,
                meditationDurationMinutes = settings.meditationDurationMinutes,
                readerFontScale = settings.readerFontScale,
                interfaceTextScale = settings.interfaceTextScale,
                contentPriority = settings.contentPriority,
                priorityContentIds = settings.priorityContentIds,
                reactivatedCompletedContentIds = settings.reactivatedCompletedContentIds,
                openAnywayUnlockMinutes = settings.openAnywayUnlockMinutes,
            )
        }

        override suspend fun saveSelectedAppPackages(packages: Set<String>) {
            state.value = state.value.copy(selectedAppPackages = packages)
        }

        override suspend fun savePreferredDurationBucket(bucket: DurationBucket) {
            state.value = state.value.copy(preferredDurationBucket = bucket)
        }

        override suspend fun saveThemeMode(themeMode: AppThemeMode) {
            state.value = state.value.copy(themeMode = themeMode)
        }

        override suspend fun saveMeditationDurationMinutes(minutes: Int) {
            state.value = state.value.copy(meditationDurationMinutes = minutes)
        }

        override suspend fun saveReaderFontScale(scale: Double) {
            state.value = state.value.copy(readerFontScale = scale)
        }

        override suspend fun saveInterfaceTextScale(scale: Double) {
            state.value = state.value.copy(interfaceTextScale = scale)
        }

        override suspend fun saveContentPriority(priority: ContentPriority) {
            state.value = state.value.copy(contentPriority = priority)
        }

        override suspend fun savePriorityContentIds(contentIds: Set<String>) {
            state.value = state.value.copy(priorityContentIds = contentIds)
        }

        override suspend fun saveReactivatedCompletedContentIds(contentIds: Set<String>) {
            state.value = state.value.copy(reactivatedCompletedContentIds = contentIds)
        }

        override suspend fun saveOpenAnywayUnlockMinutes(minutes: Int) {
            state.value = state.value.copy(openAnywayUnlockMinutes = minutes)
        }

        override suspend fun saveAnnotationExportDestination(uri: String, displayName: String) {
            state.value = state.value.copy(
                annotationExportUri = uri,
                annotationExportDisplayName = displayName,
                annotationExportLastSuccessfulAtMillis = null,
                annotationExportLastError = null,
            )
        }

        override suspend fun clearAnnotationExportDestination() {
            state.value = state.value.copy(
                annotationExportUri = null,
                annotationExportDisplayName = null,
                annotationExportLastSuccessfulAtMillis = null,
                annotationExportLastError = null,
            )
        }

        override suspend fun saveAnnotationExportSuccess(timestampMillis: Long) {
            state.value = state.value.copy(
                annotationExportLastSuccessfulAtMillis = timestampMillis,
                annotationExportLastError = null,
            )
        }

        override suspend fun saveAnnotationExportFailure(errorMessage: String) {
            state.value = state.value.copy(annotationExportLastError = errorMessage)
        }

        override suspend fun saveAnnotationDriveSyncConnection(folderId: String?) {
            state.value = state.value.copy(
                annotationDriveSyncEnabled = true,
                annotationDriveFolderId = folderId,
                annotationDriveLastError = null,
            )
        }

        override suspend fun clearAnnotationDriveSyncConnection() {
            state.value = state.value.copy(
                annotationDriveSyncEnabled = false,
                annotationDriveFolderId = null,
                annotationDriveLastSuccessfulAtMillis = null,
                annotationDriveLastError = null,
            )
        }

        override suspend fun saveAnnotationDriveSyncSuccess(timestampMillis: Long, folderId: String) {
            state.value = state.value.copy(
                annotationDriveSyncEnabled = true,
                annotationDriveFolderId = folderId,
                annotationDriveLastSuccessfulAtMillis = timestampMillis,
                annotationDriveLastError = null,
            )
        }

        override suspend fun saveAnnotationDriveSyncFailure(errorMessage: String) {
            state.value = state.value.copy(
                annotationDriveLastError = errorMessage,
            )
        }

        override suspend fun saveProfileAutosaveDestination(uri: String, displayName: String) {
            state.value = state.value.copy(
                profileAutosaveUri = uri,
                profileAutosaveDisplayName = displayName,
                profileAutosaveLastSuccessfulAtMillis = null,
                profileAutosaveLastError = null,
            )
        }

        override suspend fun clearProfileAutosaveDestination() {
            state.value = state.value.copy(
                profileAutosaveUri = null,
                profileAutosaveDisplayName = null,
                profileAutosaveLastSuccessfulAtMillis = null,
                profileAutosaveLastError = null,
            )
        }

        override suspend fun saveProfileAutosaveSuccess(timestampMillis: Long) {
            state.value = state.value.copy(
                profileAutosaveLastSuccessfulAtMillis = timestampMillis,
                profileAutosaveLastError = null,
            )
        }

        override suspend fun saveProfileAutosaveFailure(errorMessage: String) {
            state.value = state.value.copy(profileAutosaveLastError = errorMessage)
        }
    }

    private class FakeHistoryRepository(
        initialHistory: List<ReplacementHistoryEntry> = emptyList(),
        initialCompletedIds: Set<String> = emptySet(),
        isReady: Boolean = true,
    ) : HistoryRepository {
        val historyEntries = MutableStateFlow(initialHistory)
        private val completedIds = MutableStateFlow(initialCompletedIds)
        private val ready = MutableStateFlow(isReady)

        override fun recentHistory(nowMillis: Long, windowDays: Int): List<ReplacementHistoryEntry> = historyEntries.value

        override fun observeRecentHistory(nowMillis: Long, windowDays: Int): Flow<List<ReplacementHistoryEntry>> = historyEntries

        override fun observeCompletedContentIds(): Flow<Set<String>> = completedIds

        override fun isReady(): Boolean = ready.value

        override fun observeReady(): Flow<Boolean> = ready

        fun setReady(value: Boolean) {
            ready.value = value
        }

        override suspend fun recordAcceptedSession(
            targetApp: DistractingApp,
            interventionId: String,
            interventionShownAtMillis: Long,
            primaryContentId: String,
            backupContentIds: List<String>,
            content: ContentItem,
            source: RecommendationSource,
            acceptedAtMillis: Long,
        ): String {
            val entry = ReplacementHistoryEntry(
                sessionId = "session-${historyEntries.value.size}",
                interventionId = interventionId,
                targetAppPackage = targetApp.packageName,
                targetAppDisplayName = targetApp.displayName,
                interventionShownAtMillis = interventionShownAtMillis,
                primaryContentId = primaryContentId,
                backupContentIds = backupContentIds,
                contentId = content.id,
                contentTitle = content.title,
                contentDescription = content.description,
                contentDurationMinutes = content.durationMinutes,
                contentTopics = content.topicTags,
                packId = content.packId,
                recommendationSource = source,
                acceptedAtMillis = acceptedAtMillis,
            )
            historyEntries.value = listOf(entry) + historyEntries.value
            return entry.sessionId
        }

        override suspend fun markCompleted(sessionId: String, completedAtMillis: Long) {
            historyEntries.value = historyEntries.value.map { entry ->
                if (entry.sessionId == sessionId) entry.copy(completedAtMillis = completedAtMillis) else entry
            }
            completedIds.value = historyEntries.value.filter(ReplacementHistoryEntry::isCompleted)
                .mapTo(mutableSetOf(), ReplacementHistoryEntry::contentId)
        }

        override suspend fun updateAcceptedSessionContent(sessionId: String, content: ContentItem) {
            historyEntries.value = historyEntries.value.map { entry ->
                if (entry.sessionId == sessionId) {
                    entry.copy(
                        contentId = content.id,
                        contentTitle = content.title,
                        contentDescription = content.description,
                        contentDurationMinutes = content.durationMinutes,
                        contentTopics = content.topicTags,
                        packId = content.packId,
                    )
                } else {
                    entry
                }
            }
        }

        override suspend fun markSkipped(sessionId: String, skippedAtMillis: Long) {
            historyEntries.value = historyEntries.value.map { entry ->
                if (entry.sessionId == sessionId) entry.copy(skippedAtMillis = skippedAtMillis) else entry
            }
        }

        override suspend fun attachFeedback(sessionId: String, feedback: SessionFeedback) {
            historyEntries.value = historyEntries.value.map { entry ->
                if (entry.sessionId == sessionId) {
                    entry.copy(
                        feedbackGoodFit = feedback.wasGoodFit,
                        feedbackHelpedAvoidScrolling = feedback.helpedAvoidScrolling,
                        feedbackFitRating = feedback.fitRating,
                        feedbackScrollRating = feedback.scrollRating,
                    )
                } else {
                    entry
                }
            }
        }

        override suspend fun markReturnedToTarget(targetAppPackage: String, returnedAtMillis: Long): ReturnToTargetSignal? {
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
                interventionId = candidate.interventionId,
                targetAppPackage = candidate.targetAppPackage,
                primaryContentId = candidate.primaryContentId,
                backupContentIds = candidate.backupContentIds,
                contentId = candidate.contentId,
                returnedAtMillis = returnedAtMillis,
                within15Minutes = true,
                within60Minutes = true,
            )
        }
    }

    private class FakeReadingProgressRepository(
        initialProgress: List<ReadingProgress> = emptyList(),
        isReady: Boolean = true,
        private val throwOnSaveProgress: Boolean = false,
    ) : ReadingProgressRepository {
        val progress = MutableStateFlow(initialProgress)
        val deletedIds = mutableSetOf<String>()
        private val ready = MutableStateFlow(isReady)

        override fun readingProgress(): List<ReadingProgress> = progress.value

        override fun observeReadingProgress(): Flow<List<ReadingProgress>> = progress.asStateFlow()

        override fun observeCompletedContentIds(): Flow<Set<String>> {
            return progress.asStateFlow().map { currentProgress ->
                currentProgress.filter(ReadingProgress::isCompleted)
                    .mapTo(mutableSetOf(), ReadingProgress::contentId)
            }
        }

        override suspend fun saveProgress(progress: ReadingProgress) {
            if (throwOnSaveProgress) {
                error("Simulated reading progress persistence failure")
            }
            this.progress.value = this.progress.value
                .filterNot { it.contentId == progress.contentId }
                .plus(progress)
                .sortedByDescending(ReadingProgress::updatedAtMillis)
        }

        override suspend fun deleteProgress(contentId: String) {
            deletedIds += contentId
            progress.value = progress.value.filterNot { it.contentId == contentId }
        }

        override suspend fun deleteProgressForContentIds(contentIds: Set<String>) {
            deletedIds += contentIds
            progress.value = progress.value.filterNot { it.contentId in contentIds }
        }

        override fun isReady(): Boolean = ready.value

        override fun observeReady(): Flow<Boolean> = ready
    }

    private class FakeReadingAnnotationRepository(
        initialAnnotations: List<ReadingAnnotation> = emptyList(),
        isReady: Boolean = true,
    ) : ReadingAnnotationRepository {
        private val annotations = MutableStateFlow(initialAnnotations)
        private val ready = MutableStateFlow(isReady)
        private var nextId = initialAnnotations.size

        override fun readingAnnotations(): List<ReadingAnnotation> = annotations.value

        override fun observeReadingAnnotations(): Flow<List<ReadingAnnotation>> = annotations.asStateFlow()

        override suspend fun saveAnnotation(
            draft: ReadingAnnotationDraft,
            nowMillis: Long,
        ): ReadingAnnotation {
            val existing = draft.id?.let { id -> annotations.value.firstOrNull { annotation -> annotation.id == id } }
            val saved = ReadingAnnotation(
                id = draft.id ?: "reading-annotation:${++nextId}",
                contentId = draft.contentId,
                paragraphIndex = draft.paragraphIndex,
                quotedText = draft.quotedText,
                noteText = draft.noteText,
                createdAtMillis = existing?.createdAtMillis ?: nowMillis,
                updatedAtMillis = nowMillis,
                sourceTitle = draft.sourceTitle,
                sourceLabel = draft.sourceLabel,
                sourceType = draft.sourceType,
                sourceFormat = draft.sourceFormat,
                selector = draft.selector,
            )
            annotations.value = annotations.value
                .filterNot { annotation -> annotation.id == saved.id }
                .plus(saved)
                .sortedByDescending(ReadingAnnotation::updatedAtMillis)
            return saved
        }

        override suspend fun deleteAnnotation(
            annotationId: String,
            nowMillis: Long,
        ) {
            annotations.value = annotations.value.filterNot { annotation -> annotation.id == annotationId }
        }

        override suspend fun deleteAnnotationsForContentIds(
            contentIds: Set<String>,
            nowMillis: Long,
        ) {
            annotations.value = annotations.value.filterNot { annotation -> annotation.contentId in contentIds }
        }

        override fun isReady(): Boolean = ready.value

        override fun observeReady(): Flow<Boolean> = ready
    }

    private class RecordingReadingAnnotationExportWriter(
        private val failure: Throwable? = null,
    ) : ReadingAnnotationExportWriter {
        val writes = mutableListOf<Pair<String, String>>()
        val jsonWrites = mutableListOf<Pair<String, List<ReadingAnnotationExportFile>>>()

        override suspend fun writeMarkdown(uri: String, markdown: String) {
            failure?.let { throw it }
            writes += uri to markdown
        }

        override suspend fun writeJsonLdFiles(uri: String, files: List<ReadingAnnotationExportFile>) {
            failure?.let { throw it }
            jsonWrites += uri to files
            writes += uri to files.joinToString(separator = "\n") { file -> file.jsonLd }
        }
    }

    private class RecordingAccountLightProfileAutosaveWriter(
        private val failure: Throwable? = null,
    ) : AccountLightProfileAutosaveWriter {
        val writes = mutableListOf<Triple<String, String, String>>()

        override suspend fun writeProfileJson(uri: String, fileName: String, json: String) {
            failure?.let { throw it }
            writes += Triple(uri, fileName, json)
        }
    }

    private class RecordingReadingAnnotationDriveSyncClient(
        private val folderId: String = "drive-folder-1",
        private val failure: Throwable? = null,
    ) : ReadingAnnotationDriveSyncClient {
        val requests = mutableListOf<ReadingAnnotationDriveSyncRequest>()

        override suspend fun syncJsonLdFiles(
            request: ReadingAnnotationDriveSyncRequest,
        ): ReadingAnnotationDriveSyncResult {
            failure?.let { throw it }
            requests += request
            return ReadingAnnotationDriveSyncResult(
                folderId = folderId,
                syncedFileNames = listOf("quality-alternative-annotations.index.json") +
                    request.files.map(ReadingAnnotationExportFile::fileName),
            )
        }
    }

    private class StaticReadingAnnotationDriveTokenProvider(
        private val accessToken: String,
    ) : ReadingAnnotationDriveTokenProvider {
        override suspend fun driveAccessToken(): String = accessToken
    }

    private class FailingReadingAnnotationDriveTokenProvider : ReadingAnnotationDriveTokenProvider {
        override suspend fun driveAccessToken(): String = error("No test Drive token configured.")
    }

    private class FakeDelayGate(isReady: Boolean) : DelayGate {
        private val delegate = InMemoryDelayGate()
        private val ready = MutableStateFlow(isReady)

        override fun inspectDelay(targetApp: DistractingApp, nowMillis: Long) =
            delegate.inspectDelay(targetApp = targetApp, nowMillis = nowMillis)

        override fun activeDelay(targetApp: DistractingApp, nowMillis: Long) =
            delegate.activeDelay(targetApp = targetApp, nowMillis = nowMillis)

        override suspend fun consumeExpiredDelay(targetApp: DistractingApp, delayId: String, nowMillis: Long) =
            delegate.consumeExpiredDelay(targetApp = targetApp, delayId = delayId, nowMillis = nowMillis)

        override fun storeDelay(
            targetApp: DistractingApp,
            nowMillis: Long,
            durationMinutes: Int,
            interventionId: String?,
            interventionShownAtMillis: Long?,
            primaryContentId: String?,
            backupContentIds: List<String>,
        ) = delegate.storeDelay(
            targetApp = targetApp,
            nowMillis = nowMillis,
            durationMinutes = durationMinutes,
            interventionId = interventionId,
            interventionShownAtMillis = interventionShownAtMillis,
            primaryContentId = primaryContentId,
            backupContentIds = backupContentIds,
        )

        override fun recordFirstReturnAttempt(targetApp: DistractingApp, nowMillis: Long) =
            delegate.recordFirstReturnAttempt(targetApp = targetApp, nowMillis = nowMillis)

        override fun isReady(): Boolean = ready.value

        override fun observeReady(): Flow<Boolean> = ready

        fun setReady(value: Boolean) {
            ready.value = value
        }
    }

    private class FakeContentRepository(
        extraItems: List<ContentItem> = emptyList(),
        isReady: Boolean = true,
        includeAttentionClassics: Boolean = false,
        includePublicDomainExpansion: Boolean = false,
        includeLinkOnlyModern: Boolean = false,
        includeSprint9Packs: Boolean = false,
        private val failUserDocumentBodyLoad: Boolean = false,
    ) : ContentRepository {
        private val ready = MutableStateFlow(isReady)
        private var extraItems = extraItems
        private val packs = buildList {
            add(
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
            )
            add(
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
            if (includeAttentionClassics) {
                add(
                    EditorialPack(
                        id = "attention-classics-v1",
                        title = "Attention Classics v1",
                        description = "Pack",
                        items = listOf(
                            contentItem(
                                id = "attention-quick",
                                packId = "attention-classics-v1",
                                durationMinutes = 4,
                                topics = setOf(TopicTag.ESSAYS, TopicTag.PHILOSOPHY),
                            ),
                            contentItem(
                                id = "attention-backup",
                                packId = "attention-classics-v1",
                                durationMinutes = 3,
                                topics = setOf(TopicTag.ESSAYS),
                            ),
                        ),
                    ),
                )
            }
            if (includeLinkOnlyModern) {
                add(
                    EditorialPack(
                        id = "link-only-modern-v1",
                        title = "Modern Deep Reads v1",
                        description = "Pack",
                        items = listOf(
                            contentItem(
                                id = "big-here-long-now",
                                packId = "link-only-modern-v1",
                                durationMinutes = 5,
                                topics = setOf(TopicTag.ESSAYS, TopicTag.SCIENCE),
                                format = ContentFormat.HTML,
                                bodyAssetPath = null,
                                externalUrl = "https://longnow.org/ideas/the-big-here-and-long-now/",
                                rights = ContentRightsMetadata(
                                    rightsClass = ContentRightsClass.LINK_ONLY,
                                    renderMode = ContentRenderMode.EXTERNAL_HANDOFF,
                                    sourceUrl = "https://longnow.org/ideas/the-big-here-and-long-now/",
                                ),
                            ),
                        ),
                    ),
                )
            }
            if (includePublicDomainExpansion) {
                add(
                    EditorialPack(
                        id = "public-domain-expansion-v2",
                        title = "Public-Domain Deep Reads v2",
                        description = "Pack",
                        items = listOf(
                            contentItem(
                                id = "darwin-noticing",
                                packId = "public-domain-expansion-v2",
                                durationMinutes = 3,
                                topics = setOf(TopicTag.SCIENCE, TopicTag.ESSAYS),
                            ),
                        ),
                    ),
                )
            }
            if (includeSprint9Packs) {
                listOf(
                    "attention_practical_agency_v1" to TopicTag.ATTENTION,
                    "embodied_calm_v1" to TopicTag.BODY,
                    "wonder_science_v1" to TopicTag.NATURE,
                    "long_view_history_v1" to TopicTag.HISTORY_CULTURE,
                    "creativity_play_v1" to TopicTag.CREATIVITY,
                ).forEach { (packId, topic) ->
                    add(
                        EditorialPack(
                            id = packId,
                            title = packId,
                            description = "Pack",
                            items = listOf(
                                contentItem(
                                    id = "$packId-item",
                                    packId = packId,
                                    durationMinutes = 7,
                                    topics = setOf(topic, TopicTag.PRACTICAL),
                                ),
                            ),
                        ),
                    )
                }
            }
        }

        override fun starterPacks(): List<EditorialPack> = packs

        override fun inventory(): List<ContentItem> = packs.flatMap(EditorialPack::items) + extraItems

        override fun contentBody(item: ContentItem): String {
            if (item.sourceType == ContentSourceType.USER_DOCUMENT && failUserDocumentBodyLoad) {
                error("Simulated private document body load failure")
            }
            return if (item.sourceType == ContentSourceType.USER_DOCUMENT) item.description else item.title
        }

        override fun isReady(): Boolean = ready.value

        override fun observeReady(): Flow<Boolean> = ready

        fun setReady(value: Boolean) {
            ready.value = value
        }

        fun setExtraItems(items: List<ContentItem>) {
            extraItems = items
        }

        private fun contentItem(
            id: String,
            packId: String,
            durationMinutes: Int,
            topics: Set<TopicTag>,
            format: ContentFormat = ContentFormat.MARKDOWN,
            bodyAssetPath: String? = "unused",
            externalUrl: String? = null,
            rights: ContentRightsMetadata = ContentRightsMetadata.renderableEditorial(),
        ): ContentItem = ContentItem(
            id = id,
            packId = packId,
            title = id,
            description = id,
            durationMinutes = durationMinutes,
            format = format,
            topicTags = topics,
            bodyAssetPath = bodyAssetPath,
            externalUrl = externalUrl,
            rights = rights,
        )
    }

    private class FakeUserLinkRepository(
        initialLinks: List<ContentItem> = emptyList(),
        private val throwOnAdd: Boolean = false,
    ) : UserLinkRepository {
        val links = MutableStateFlow(initialLinks)
        val markedUnavailableIds = mutableListOf<String>()
        val deletedIds = mutableListOf<String>()
        private var nextId = 0

        override fun userLinks(): List<ContentItem> = links.value

        override fun observeUserLinks(): Flow<List<ContentItem>> = links.asStateFlow()

        override suspend fun addLink(
            draft: UserLinkDraft,
            nowMillis: Long,
        ): AddUserLinkResult {
            if (throwOnAdd) {
                error("Simulated local persistence failure")
            }
            if (!draft.url.startsWith("http://") && !draft.url.startsWith("https://")) {
                return AddUserLinkResult.Rejected(setOf(UserLinkValidationError.UNSUPPORTED_SCHEME))
            }
            val item = ContentItem(
                id = "user-link-00000000-0000-4000-8000-${(++nextId).toString().padStart(12, '0')}",
                packId = "user-links",
                title = draft.title.trim(),
                description = draft.url.trim(),
                durationMinutes = draft.durationMinutes,
                format = ContentFormat.HTML,
                topicTags = draft.topicTags,
                externalUrl = draft.url.trim(),
                sourceType = ContentSourceType.USER_LINK,
                availability = ContentAvailability.NEEDS_FALLBACK,
                rights = ContentRightsMetadata.userPrivateExternal(sourceUrl = draft.url.trim()),
            )
            links.value = links.value + item
            return AddUserLinkResult.Added(item)
        }

        override suspend fun markUnavailable(
            contentId: String,
            nowMillis: Long,
        ) {
            markedUnavailableIds += contentId
            links.value = links.value.map { item ->
                if (item.id == contentId) {
                    item.copy(availability = ContentAvailability.UNAVAILABLE)
                } else {
                    item
                }
            }
        }

        override suspend fun deleteLink(contentId: String) {
            deletedIds += contentId
            links.value = links.value.filterNot { item -> item.id == contentId }
        }
    }

    private class FakeUserDocumentRepository(
        initialDocuments: List<ContentItem> = emptyList(),
        private val throwOnAdd: Boolean = false,
    ) : UserDocumentRepository {
        val documents = MutableStateFlow(initialDocuments)
        val markedUnavailableIds = mutableListOf<String>()
        val deletedIds = mutableListOf<String>()
        val addedDrafts = mutableListOf<UserDocumentDraft>()
        private var nextId = 0

        override fun userDocuments(): List<ContentItem> = documents.value

        override fun observeUserDocuments(): Flow<List<ContentItem>> = documents.asStateFlow()

        override suspend fun addDocument(
            draft: UserDocumentDraft,
            nowMillis: Long,
        ): AddUserDocumentResult {
            if (throwOnAdd) {
                error("Simulated local persistence failure")
            }
            addedDrafts += draft
            val item = ContentItem(
                id = "user-document-00000000-0000-4000-8000-${(++nextId).toString().padStart(12, '0')}",
                packId = "user-documents",
                title = draft.title.trim(),
                description = draft.displayName,
                durationMinutes = draft.durationMinutes,
                format = ContentFormat.MARKDOWN,
                topicTags = draft.topicTags,
                sourceLabel = draft.displayName,
                sourceType = ContentSourceType.USER_DOCUMENT,
                availability = ContentAvailability.AVAILABLE,
                rights = ContentRightsMetadata.userPrivateReader(sourceUrl = draft.uri),
            )
            documents.value = documents.value + item
            return AddUserDocumentResult.Added(item)
        }

        override suspend fun markUnavailable(
            contentId: String,
            nowMillis: Long,
        ) {
            markedUnavailableIds += contentId
            documents.value = documents.value.map { item ->
                if (item.id == contentId) {
                    item.copy(availability = ContentAvailability.UNAVAILABLE)
                } else {
                    item
                }
            }
        }

        override suspend fun deleteDocument(contentId: String) {
            deletedIds += contentId
            documents.value = documents.value.filterNot { item -> item.id == contentId }
        }
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

    private class RecordingRecommendationEngine : RecommendationEngine {
        var lastInventory: List<ContentItem> = emptyList()
            private set
        var lastPreferences: UserPreferences? = null
            private set
        var lastExcludedContentIds: Set<String> = emptySet()
            private set

        override fun generate(
            targetApp: DistractingApp,
            preferences: UserPreferences,
            inventory: List<ContentItem>,
            excludedContentIds: Set<String>,
            signals: RecommendationSignals,
            nowMillis: Long,
        ): RecommendationSet? {
            lastPreferences = preferences
            lastInventory = inventory
            lastExcludedContentIds = excludedContentIds
            return null
        }
    }

    private class FixedRecommendationEngine(
        private val recommendationSet: RecommendationSet,
    ) : RecommendationEngine {
        override fun generate(
            targetApp: DistractingApp,
            preferences: UserPreferences,
            inventory: List<ContentItem>,
            excludedContentIds: Set<String>,
            signals: RecommendationSignals,
            nowMillis: Long,
        ): RecommendationSet = recommendationSet
    }

    private fun savedUserLink(
        id: String = "user-link",
        durationMinutes: Int = 6,
        topics: Set<TopicTag> = setOf(TopicTag.PSYCHOLOGY),
    ): ContentItem {
        return ContentItem(
            id = id,
            packId = "user-links",
            title = "Saved link",
            description = "External link",
            durationMinutes = durationMinutes,
            format = ContentFormat.HTML,
            topicTags = topics,
            externalUrl = "https://example.com/essay",
            sourceType = ContentSourceType.USER_LINK,
            availability = ContentAvailability.NEEDS_FALLBACK,
            rights = ContentRightsMetadata.userPrivateExternal(sourceUrl = "https://example.com/essay"),
        )
    }

    private fun sharedLinkOnlyItem(): ContentItem {
        return ContentItem(
            id = "big-here-long-now",
            packId = "link-only-modern-v1",
            title = "The Big Here and Long Now",
            description = "A perspective reset for the moment the feed narrows your sense of time and place.",
            durationMinutes = 10,
            format = ContentFormat.HTML,
            topicTags = setOf(TopicTag.ESSAYS, TopicTag.PHILOSOPHY),
            externalUrl = "https://longnow.org/ideas/the-big-here-and-long-now/",
            sourceLabel = "Long Now",
            rights = ContentRightsMetadata(
                rightsClass = ContentRightsClass.LINK_ONLY,
                renderMode = ContentRenderMode.EXTERNAL_HANDOFF,
                sourceUrl = "https://longnow.org/ideas/the-big-here-and-long-now/",
                attribution = "Brian Eno, The Long Now Foundation",
                rightsReviewedAt = "2026-04-22",
            ),
        )
    }

    private fun savedUserDocument(format: ContentFormat): ContentItem {
        val externalUrl = when (format) {
            ContentFormat.PDF -> "content://quality/document.pdf"
            else -> null
        }
        return ContentItem(
            id = "user-document",
            packId = "user-documents",
            title = "Saved document",
            description = "Private notes",
            durationMinutes = 8,
            format = format,
            topicTags = setOf(TopicTag.PSYCHOLOGY),
            externalUrl = externalUrl,
            sourceLabel = "notes.md",
            sourceType = ContentSourceType.USER_DOCUMENT,
            availability = if (externalUrl == null) ContentAvailability.AVAILABLE else ContentAvailability.NEEDS_FALLBACK,
            rights = if (externalUrl == null) {
                ContentRightsMetadata.userPrivateReader(sourceUrl = "content://quality/notes.md")
            } else {
                ContentRightsMetadata.userPrivateExternal(sourceUrl = externalUrl)
            },
        )
    }

    private fun testDataStore(file: File): DataStore<Preferences> {
        return PreferenceDataStoreFactory.create(produceFile = { file })
    }
}
