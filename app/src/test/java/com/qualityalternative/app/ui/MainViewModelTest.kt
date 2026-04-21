package com.qualityalternative.app.ui

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import com.qualityalternative.app.analytics.InMemoryAnalyticsTracker
import com.qualityalternative.app.data.InMemoryDelayGate
import com.qualityalternative.app.data.PreferencesDelayGate
import com.qualityalternative.app.data.SupportedCatalog
import com.qualityalternative.app.domain.model.AppSettings
import com.qualityalternative.app.domain.model.AnalyticsEventType
import com.qualityalternative.app.domain.model.ContentAvailability
import com.qualityalternative.app.domain.model.AppThemeMode
import com.qualityalternative.app.domain.model.ContentFormat
import com.qualityalternative.app.domain.model.ContentItem
import com.qualityalternative.app.domain.model.ContentSourceType
import com.qualityalternative.app.domain.model.DistractingApp
import com.qualityalternative.app.domain.model.DurationBucket
import com.qualityalternative.app.domain.model.EditorialPack
import com.qualityalternative.app.domain.model.OnboardingSelection
import com.qualityalternative.app.domain.model.PermissionReadiness
import com.qualityalternative.app.domain.model.PermissionStatus
import com.qualityalternative.app.domain.model.RecommendationSet
import com.qualityalternative.app.domain.model.RecommendationSignals
import com.qualityalternative.app.domain.model.RecommendationSource
import com.qualityalternative.app.domain.model.ReplacementHistoryEntry
import com.qualityalternative.app.domain.model.ReturnToTargetSignal
import com.qualityalternative.app.domain.model.SessionFeedback
import com.qualityalternative.app.domain.model.TopicTag
import com.qualityalternative.app.domain.model.UserLinkDraft
import com.qualityalternative.app.domain.model.UserLinkValidationError
import com.qualityalternative.app.domain.model.UserPreferences
import com.qualityalternative.app.domain.service.AddUserLinkResult
import com.qualityalternative.app.domain.service.ContentRepository
import com.qualityalternative.app.domain.service.DefaultRecommendationEngine
import com.qualityalternative.app.domain.service.DelayGate
import com.qualityalternative.app.domain.service.HistoryRepository
import com.qualityalternative.app.domain.service.InterceptionMonitor
import com.qualityalternative.app.domain.service.RecommendationEngine
import com.qualityalternative.app.domain.service.SettingsRepository
import com.qualityalternative.app.domain.service.UserLinkRepository
import com.qualityalternative.app.interception.FixtureTargetRegistry
import com.qualityalternative.app.interception.InterceptionRuntimeGate
import java.io.File
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
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
            setOf("p1", "user-link"),
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
        assertEquals("user-link:1", event.contentId)
        assertEquals("USER_LINK", event.metadata["sourceType"])
        assertEquals("https://example.com/essay", event.metadata["externalUrl"])

        viewModel.finishAddLinkSuccess()
        assertEquals(MainScreen.Library, viewModel.uiState.screen)
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
        val viewModel = createViewModel()

        advanceUntilIdle()
        viewModel.completeOnboarding()
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
        assertEquals(MainScreen.Intervention, viewModel.uiState.screen)
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun triggerDebugIntervention_reportsNoRecommendationWhenAllInventoryIsCompleted() = runTest {
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

        assertEquals(null, viewModel.uiState.currentRecommendationSet)
        assertEquals(null, viewModel.uiState.currentInterventionId)
        assertEquals(MainScreen.Home, viewModel.uiState.screen)
        assertEquals("No replacement inventory is available yet.", viewModel.uiState.latestMessage)
        val event = analyticsTracker.allEvents().first { it.type == AnalyticsEventType.NO_RECOMMENDATION_AVAILABLE }
        assertEquals("1", event.metadata["eligibleInventoryCount"])
        assertEquals("1", event.metadata["eligibleEditorialCount"])
        assertEquals("0", event.metadata["eligibleUserLinkCount"])
        assertEquals("0", event.metadata["unavailableUserLinkCount"])
        assertEquals("2", event.metadata["completedContentCount"])
        assertEquals("philosophy", event.metadata["selectedPackIds"])
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
        assertEquals("1", event.metadata["eligibleInventoryCount"])
        assertEquals("1", event.metadata["eligibleEditorialCount"])
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
        recommendationEngine: RecommendationEngine = DefaultRecommendationEngine(),
        nowProvider: () -> Long = { 1_000L },
    ): MainViewModel {
        return track(
            MainViewModel(
                contentRepository = contentRepository,
                userLinkRepository = userLinkRepository,
                settingsRepository = settingsRepository,
                recommendationEngine = recommendationEngine,
                delayGate = delayGate,
                analyticsTracker = analyticsTracker,
                historyRepository = historyRepository,
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
    ) : ContentRepository {
        private val ready = MutableStateFlow(isReady)
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
        private val inventory = packs.flatMap(EditorialPack::items) + extraItems

        override fun starterPacks(): List<EditorialPack> = packs

        override fun inventory(): List<ContentItem> = inventory

        override fun contentBody(item: ContentItem): String = item.title

        override fun isReady(): Boolean = ready.value

        override fun observeReady(): Flow<Boolean> = ready

        fun setReady(value: Boolean) {
            ready.value = value
        }

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

    private class FakeUserLinkRepository(
        initialLinks: List<ContentItem> = emptyList(),
        private val throwOnAdd: Boolean = false,
    ) : UserLinkRepository {
        val links = MutableStateFlow(initialLinks)
        val markedUnavailableIds = mutableListOf<String>()
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
                id = "user-link:${++nextId}",
                packId = "user-links",
                title = draft.title.trim(),
                description = draft.url.trim(),
                durationMinutes = draft.durationMinutes,
                format = ContentFormat.HTML,
                topicTags = draft.topicTags,
                externalUrl = draft.url.trim(),
                sourceType = ContentSourceType.USER_LINK,
                availability = ContentAvailability.NEEDS_FALLBACK,
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

        override fun generate(
            targetApp: DistractingApp,
            preferences: UserPreferences,
            inventory: List<ContentItem>,
            primaryExcludedIds: Set<String>,
            signals: RecommendationSignals,
            nowMillis: Long,
        ): RecommendationSet? {
            lastInventory = inventory
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
            primaryExcludedIds: Set<String>,
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
        )
    }

    private fun testDataStore(file: File): DataStore<Preferences> {
        return PreferenceDataStoreFactory.create(produceFile = { file })
    }
}
