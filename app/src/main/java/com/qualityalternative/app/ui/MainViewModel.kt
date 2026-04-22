package com.qualityalternative.app.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.qualityalternative.app.data.AppContainer
import com.qualityalternative.app.data.SupportedCatalog
import com.qualityalternative.app.data.UserDocumentValidator
import com.qualityalternative.app.data.UserLinkValidator
import com.qualityalternative.app.domain.model.AnalyticsEvent
import com.qualityalternative.app.domain.model.AnalyticsSemanticKeys
import com.qualityalternative.app.domain.model.AnalyticsEventType
import com.qualityalternative.app.domain.model.AppSettings
import com.qualityalternative.app.domain.model.AppThemeMode
import com.qualityalternative.app.domain.model.ContentAvailability
import com.qualityalternative.app.domain.model.ContentFormat
import com.qualityalternative.app.domain.model.ContentItem
import com.qualityalternative.app.domain.model.ContentSourceType
import com.qualityalternative.app.domain.model.DelayWindow
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
import com.qualityalternative.app.domain.model.ReplacementHistoryEntry
import com.qualityalternative.app.domain.model.ReturnToTargetSignal
import com.qualityalternative.app.domain.model.SessionFeedback
import com.qualityalternative.app.domain.model.TimeOfDayBucket
import com.qualityalternative.app.domain.model.TopicTag
import com.qualityalternative.app.domain.model.UserDocumentDraft
import com.qualityalternative.app.domain.model.UserDocumentValidationError
import com.qualityalternative.app.domain.model.UserLinkDraft
import com.qualityalternative.app.domain.model.UserLinkValidationError
import com.qualityalternative.app.domain.model.UserPreferences
import com.qualityalternative.app.domain.model.usesExternalHandoff
import com.qualityalternative.app.domain.model.usesMeditationTimer
import com.qualityalternative.app.domain.model.usesRepositoryBody
import com.qualityalternative.app.domain.service.AddUserDocumentResult
import com.qualityalternative.app.domain.service.AddUserLinkResult
import com.qualityalternative.app.domain.service.AnalyticsTracker
import com.qualityalternative.app.domain.service.ContentRepository
import com.qualityalternative.app.domain.service.DelayGate
import com.qualityalternative.app.domain.service.HistoryRepository
import com.qualityalternative.app.domain.service.InterceptionMonitor
import com.qualityalternative.app.domain.service.RecommendationEngine
import com.qualityalternative.app.domain.service.SettingsRepository
import com.qualityalternative.app.domain.service.UserDocumentRepository
import com.qualityalternative.app.domain.service.UserLinkRepository
import com.qualityalternative.app.interception.InterceptionRuntimeGate
import java.util.UUID
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

data class MainUiState(
    val isLoadingSettings: Boolean = true,
    val hasCompletedOnboarding: Boolean = false,
    val allSupportedApps: List<DistractingApp> = emptyList(),
    val availableTargetApps: List<DistractingApp> = emptyList(),
    val onboardingSelection: OnboardingSelection = OnboardingSelection(
        selectedAppPackages = emptySet(),
        preferredTopics = emptySet(),
        preferredDurationBucket = DurationBucket.FOCUS,
        selectedPackIds = emptySet(),
    ),
    val selectedTargetApp: DistractingApp? = null,
    val preferences: UserPreferences? = null,
    val starterPacks: List<EditorialPack> = emptyList(),
    val currentInterventionId: String? = null,
    val currentInterventionShownAtMillis: Long? = null,
    val currentRecommendationSet: RecommendationSet? = null,
    val currentInterventionOrigin: InterventionOrigin? = null,
    val currentContent: ContentItem? = null,
    val currentContentBody: String = "",
    val currentSessionId: String? = null,
    val currentSessionStartedAtMillis: Long? = null,
    val activeDelayWindow: DelayWindow? = null,
    val activeDelaySuggestion: ContentItem? = null,
    val permissionReadiness: PermissionReadiness = unavailablePermissionReadiness(),
    val historyEntries: List<ReplacementHistoryEntry> = emptyList(),
    val completedContentIds: Set<String> = emptySet(),
    val userLinks: List<ContentItem> = emptyList(),
    val userDocuments: List<ContentItem> = emptyList(),
    val addLinkForm: AddLinkFormState = AddLinkFormState(),
    val addDocumentForm: AddDocumentFormState = AddDocumentFormState(),
    val savedLinkConfirmation: AddLinkConfirmation? = null,
    val themeMode: AppThemeMode = AppThemeMode.LIGHT,
    val latestMessage: String? = null,
    val events: List<AnalyticsEvent> = emptyList(),
    val screen: MainScreen = MainScreen.Onboarding,
    val lastFeedback: SessionFeedback? = null,
)

data class AddLinkConfirmation(
    val title: String,
    val host: String,
    val durationMinutes: Int,
    val topicLabel: String,
)

data class AddLinkFormState(
    val url: String = "",
    val title: String = "",
    val durationMinutes: String = "8",
    val selectedTopics: Set<TopicTag> = emptySet(),
    val validationErrors: Set<UserLinkValidationError> = emptySet(),
    val canSave: Boolean = false,
    val isSaving: Boolean = false,
)

data class AddDocumentFormState(
    val uri: String = "",
    val displayName: String = "",
    val mimeType: String? = null,
    val title: String = "",
    val durationMinutes: String = "10",
    val selectedTopics: Set<TopicTag> = emptySet(),
    val validationErrors: Set<UserDocumentValidationError> = emptySet(),
    val canSave: Boolean = false,
    val isSaving: Boolean = false,
)

enum class MainScreen {
    Onboarding,
    Home,
    Library,
    Progress,
    Settings,
    AddLink,
    AddDocument,
    AddLinkSuccess,
    Intervention,
    Reader,
    ExternalHandoff,
    MeditationTimer,
    Feedback,
}

enum class InterventionOrigin {
    DEBUG,
    SYSTEM,
}

class MainViewModel(
    private val contentRepository: ContentRepository,
    private val userLinkRepository: UserLinkRepository = EmptyUserLinkRepository,
    private val userDocumentRepository: UserDocumentRepository = EmptyUserDocumentRepository,
    private val settingsRepository: SettingsRepository,
    private val recommendationEngine: RecommendationEngine,
    private val delayGate: DelayGate,
    private val analyticsTracker: AnalyticsTracker,
    private val historyRepository: HistoryRepository,
    private val interceptionMonitor: InterceptionMonitor,
    private val enableDelayRefreshTicker: Boolean = true,
    private val nowProvider: () -> Long = System::currentTimeMillis,
) : ViewModel() {
    private val supportedApps = settingsRepository.supportedDistractingApps()
    private val starterPacks = contentRepository.starterPacks()
    private val defaultSelectedPackIds = defaultStarterPackIds(starterPacks)
    private var settingsLoaded = false
    private var contentReady = contentRepository.isReady()
    private var analyticsReady = analyticsTracker.isReady()
    private var historyReady = historyRepository.isReady()
    private var delayReady = delayGate.isReady()
    private var pendingSystemInterception: PendingSystemInterception? = null

    var uiState by mutableStateOf(
        MainUiState(
            allSupportedApps = supportedApps,
            starterPacks = starterPacks,
            onboardingSelection = defaultOnboardingSelection(
                supportedApps = supportedApps,
                starterPacks = starterPacks,
            ),
            events = analyticsTracker.allEvents(),
            historyEntries = historyRepository.recentHistory(),
            permissionReadiness = interceptionMonitor.currentReadiness(),
        ),
    )
        private set

    init {
        viewModelScope.launch {
            settingsRepository.observeAppSettings().collect { settings ->
                settingsLoaded = true
                applySettings(settings)
                updateHydrationState()
            }
        }
        viewModelScope.launch {
            contentRepository.observeReady().collect { ready ->
                contentReady = ready
                updateHydrationState()
            }
        }
        viewModelScope.launch {
            userLinkRepository.observeUserLinks().collect { links ->
                uiState = uiState.copy(userLinks = links)
            }
        }
        viewModelScope.launch {
            userDocumentRepository.observeUserDocuments().collect { documents ->
                uiState = uiState.copy(userDocuments = documents)
            }
        }
        viewModelScope.launch {
            analyticsTracker.observeEvents().collect { events ->
                uiState = uiState.copy(events = events)
            }
        }
        viewModelScope.launch {
            analyticsTracker.observeReady().collect { ready ->
                analyticsReady = ready
                updateHydrationState()
            }
        }
        viewModelScope.launch {
            historyRepository.observeRecentHistory(windowDays = PROGRESS_HISTORY_WINDOW_DAYS).collect { history ->
                uiState = uiState.copy(
                    historyEntries = history,
                )
            }
        }
        viewModelScope.launch {
            historyRepository.observeCompletedContentIds().collect { completedIds ->
                uiState = uiState.copy(completedContentIds = completedIds)
            }
        }
        viewModelScope.launch {
            historyRepository.observeReady().collect { ready ->
                historyReady = ready
                updateHydrationState()
            }
        }
        viewModelScope.launch {
            delayGate.observeReady().collect { ready ->
                delayReady = ready
                refreshActiveDelayWindow()
                if (ready) {
                    uiState.selectedTargetApp?.let { targetApp ->
                        reconcilePersistedDelayAnalytics(targetApp = targetApp)
                    }
                }
                updateHydrationState()
            }
        }
        if (enableDelayRefreshTicker) {
            viewModelScope.launch {
                while (true) {
                    delay(ACTIVE_DELAY_REFRESH_INTERVAL_MILLIS)
                    refreshActiveDelayWindow()
                }
            }
        }
    }

    fun selectTargetApp(app: DistractingApp) {
        val activeDelayWindow = if (delayReady) delayGate.activeDelay(app) else null
        uiState = uiState.copy(
            selectedTargetApp = app,
            activeDelayWindow = activeDelayWindow,
            activeDelaySuggestion = activeDelaySuggestionFor(activeDelayWindow),
            latestMessage = null,
        )
        if (delayReady) {
            viewModelScope.launch {
                reconcilePersistedDelayAnalytics(targetApp = app)
            }
        }
    }

    fun refreshPermissionReadiness() {
        uiState = uiState.copy(permissionReadiness = interceptionMonitor.currentReadiness())
    }

    fun toggleOnboardingApp(app: DistractingApp) {
        val selected = uiState.onboardingSelection.selectedAppPackages.toMutableSet()
        if (!selected.add(app.packageName)) {
            selected.remove(app.packageName)
        }
        uiState = uiState.copy(
            onboardingSelection = uiState.onboardingSelection.copy(selectedAppPackages = selected),
        )
    }

    fun toggleOnboardingTopic(topic: TopicTag) {
        val selected = uiState.onboardingSelection.preferredTopics.toMutableSet()
        if (!selected.add(topic)) {
            selected.remove(topic)
        }
        uiState = uiState.copy(
            onboardingSelection = uiState.onboardingSelection.copy(preferredTopics = selected),
        )
    }

    fun setOnboardingDuration(durationBucket: DurationBucket) {
        uiState = uiState.copy(
            onboardingSelection = uiState.onboardingSelection.copy(preferredDurationBucket = durationBucket),
        )
    }

    fun toggleSettingsApp(app: DistractingApp) {
        val preferences = uiState.preferences ?: return
        val selectedPackages = preferences.selectedApps.mapTo(mutableSetOf(), DistractingApp::packageName)
        if (!selectedPackages.add(app.packageName)) {
            selectedPackages.remove(app.packageName)
        }
        if (selectedPackages.size < MIN_SELECTED_DISTRACTING_APPS) {
            uiState = uiState.copy(latestMessage = "Keep at least 3 apps selected for the alpha.")
            return
        }

        val selectedApps = supportedApps.filter { supportedApp ->
            supportedApp.packageName in selectedPackages
        }
        val selectedTargetApp = uiState.selectedTargetApp
            ?.takeIf { it.packageName in selectedPackages }
            ?: selectedApps.firstOrNull()
        val activeDelayWindow = selectedTargetApp?.takeIf { delayReady }?.let { delayGate.activeDelay(it) }

        uiState = uiState.copy(
            availableTargetApps = selectedApps,
            selectedTargetApp = selectedTargetApp,
            preferences = preferences.copy(selectedApps = selectedApps),
            onboardingSelection = uiState.onboardingSelection.copy(selectedAppPackages = selectedPackages),
            activeDelayWindow = activeDelayWindow,
            activeDelaySuggestion = activeDelaySuggestionFor(activeDelayWindow),
            latestMessage = null,
        )
        viewModelScope.launch {
            settingsRepository.saveSelectedAppPackages(selectedPackages)
        }
    }

    fun setPreferredDuration(durationBucket: DurationBucket) {
        val preferences = uiState.preferences
        uiState = uiState.copy(
            preferences = preferences?.copy(preferredDurationBucket = durationBucket),
            onboardingSelection = uiState.onboardingSelection.copy(preferredDurationBucket = durationBucket),
            latestMessage = null,
        )
        viewModelScope.launch {
            settingsRepository.savePreferredDurationBucket(durationBucket)
        }
    }

    fun toggleOnboardingPack(pack: EditorialPack) {
        val selected = uiState.onboardingSelection.selectedPackIds.toMutableSet()
        if (!selected.add(pack.id)) {
            selected.remove(pack.id)
        }
        uiState = uiState.copy(
            onboardingSelection = uiState.onboardingSelection.copy(selectedPackIds = selected),
        )
    }

    fun completeOnboarding() {
        val selection = uiState.onboardingSelection
        if (!selection.isValid()) {
            uiState = uiState.copy(
                latestMessage = "Select at least 3 distracting apps, 3 topics, and 1 starter pack.",
            )
            return
        }

        viewModelScope.launch {
            settingsRepository.saveOnboardingSelection(selection)
            uiState = uiState.copy(latestMessage = null)
        }
    }

    fun openAddLink() {
        uiState = uiState.copy(
            screen = MainScreen.AddLink,
            addLinkForm = AddLinkFormState(),
            addDocumentForm = AddDocumentFormState(),
            savedLinkConfirmation = null,
            latestMessage = null,
        )
    }

    fun prepareUserDocumentImport(
        uri: String,
        displayName: String,
        mimeType: String?,
    ) {
        val cleanedName = displayName.trim().ifBlank { "Untitled document" }
        val title = cleanedName
            .substringBeforeLast('.', cleanedName)
            .trim()
            .ifBlank { cleanedName }
        updateAddDocumentForm(
            form = AddDocumentFormState(
                uri = uri,
                displayName = cleanedName,
                mimeType = mimeType,
                title = title,
                durationMinutes = defaultDocumentDuration(mimeType = mimeType, displayName = cleanedName),
            ),
            screen = MainScreen.AddDocument,
        )
    }

    fun openHome() {
        uiState = uiState.copy(screen = MainScreen.Home, latestMessage = null)
    }

    fun openLibrary() {
        uiState = uiState.copy(screen = MainScreen.Library, latestMessage = null)
    }

    fun openProgress() {
        uiState = uiState.copy(screen = MainScreen.Progress, latestMessage = null)
    }

    fun openSettings() {
        uiState = uiState.copy(screen = MainScreen.Settings, latestMessage = null)
    }

    fun openLibraryItem(content: ContentItem) {
        val startedAtMillis = nowProvider()
        val contentBody = if (content.usesRepositoryBody()) {
            try {
                contentRepository.contentBody(content)
            } catch (error: Throwable) {
                if (error is CancellationException) throw error
                viewModelScope.launch {
                    handleRepositoryBodyLoadFailure(
                        content = content,
                        sessionId = null,
                        failedAtMillis = startedAtMillis,
                        error = error,
                    )
                }
                return
            }
        } else {
            ""
        }
        uiState = uiState.copy(
            currentInterventionId = null,
            currentInterventionShownAtMillis = null,
            currentRecommendationSet = null,
            currentInterventionOrigin = null,
            currentContent = content,
            currentContentBody = contentBody,
            currentSessionId = null,
            currentSessionStartedAtMillis = startedAtMillis,
            screen = screenForReplacement(content),
            latestMessage = null,
        )
    }

    fun cancelAddLink() {
        uiState = uiState.copy(
            screen = MainScreen.Home,
            addLinkForm = AddLinkFormState(),
            addDocumentForm = AddDocumentFormState(),
            savedLinkConfirmation = null,
            latestMessage = null,
        )
    }

    fun updateAddLinkUrl(url: String) {
        updateAddLinkForm(uiState.addLinkForm.copy(url = url))
    }

    fun updateAddLinkTitle(title: String) {
        updateAddLinkForm(uiState.addLinkForm.copy(title = title))
    }

    fun updateAddLinkDuration(durationMinutes: String) {
        updateAddLinkForm(uiState.addLinkForm.copy(durationMinutes = durationMinutes))
    }

    fun toggleAddLinkTopic(topic: TopicTag) {
        val selectedTopics = uiState.addLinkForm.selectedTopics.toMutableSet()
        if (!selectedTopics.add(topic)) {
            selectedTopics.remove(topic)
        }
        updateAddLinkForm(uiState.addLinkForm.copy(selectedTopics = selectedTopics))
    }

    fun saveUserLink(nowMillis: Long = nowProvider()) {
        val draft = uiState.addLinkForm.toDraftOrNull()
        if (draft == null) {
            uiState = uiState.copy(
                addLinkForm = uiState.addLinkForm.copy(
                    validationErrors = uiState.addLinkForm.localValidationErrors(),
                    canSave = false,
                    isSaving = false,
                ),
                latestMessage = "This link needs a little cleanup before saving.",
            )
            return
        }

        uiState = uiState.copy(addLinkForm = uiState.addLinkForm.copy(isSaving = true))
        viewModelScope.launch {
            try {
                when (val result = userLinkRepository.addLink(draft = draft, nowMillis = nowMillis)) {
                    is AddUserLinkResult.Added -> {
                        recordEventDurably(
                            AnalyticsEvent(
                                type = AnalyticsEventType.USER_LINK_ADDED,
                                timestampMillis = nowMillis,
                                contentId = result.item.id,
                                metadata = result.item.analyticsMetadata() + mapOf(
                                    "durationMinutes" to result.item.durationMinutes.toString(),
                                    "topicCount" to result.item.topicTags.size.toString(),
                                ),
                            ),
                        )
                        uiState = uiState.copy(
                            screen = MainScreen.AddLinkSuccess,
                            addLinkForm = AddLinkFormState(),
                            savedLinkConfirmation = result.item.toAddLinkConfirmation(),
                            latestMessage = null,
                        )
                    }

                    is AddUserLinkResult.Rejected -> {
                        uiState = uiState.copy(
                            addLinkForm = uiState.addLinkForm.copy(
                                validationErrors = result.errors,
                                canSave = false,
                                isSaving = false,
                            ),
                            latestMessage = "This link needs a little cleanup before saving.",
                        )
                    }
                }
            } catch (error: Throwable) {
                if (error is CancellationException) throw error
                uiState = uiState.copy(
                    screen = MainScreen.AddLink,
                    addLinkForm = uiState.addLinkForm.copy(isSaving = false),
                    latestMessage = "The link could not be saved locally. Try again.",
                )
            }
        }
    }

    fun finishAddLinkSuccess() {
        uiState = uiState.copy(
            screen = MainScreen.Library,
            savedLinkConfirmation = null,
            latestMessage = "Saved for future replacement moments.",
        )
    }

    fun updateAddDocumentTitle(title: String) {
        updateAddDocumentForm(uiState.addDocumentForm.copy(title = title))
    }

    fun updateAddDocumentDuration(durationMinutes: String) {
        updateAddDocumentForm(uiState.addDocumentForm.copy(durationMinutes = durationMinutes))
    }

    fun toggleAddDocumentTopic(topic: TopicTag) {
        val selectedTopics = uiState.addDocumentForm.selectedTopics.toMutableSet()
        if (!selectedTopics.add(topic)) {
            selectedTopics.remove(topic)
        }
        updateAddDocumentForm(uiState.addDocumentForm.copy(selectedTopics = selectedTopics))
    }

    fun saveUserDocument(
        nowMillis: Long = nowProvider(),
        persistReadPermission: (String) -> Unit = {},
    ) {
        val draft = uiState.addDocumentForm.toDraftOrNull()
        if (draft == null) {
            uiState = uiState.copy(
                addDocumentForm = uiState.addDocumentForm.copy(
                    validationErrors = uiState.addDocumentForm.localValidationErrors(),
                    canSave = false,
                    isSaving = false,
                ),
                latestMessage = "This file needs a little cleanup before saving.",
            )
            return
        }

        uiState = uiState.copy(addDocumentForm = uiState.addDocumentForm.copy(isSaving = true))
        viewModelScope.launch {
            try {
                when (val result = userDocumentRepository.addDocument(draft = draft, nowMillis = nowMillis)) {
                    is AddUserDocumentResult.Added -> {
                        runCatching { persistReadPermission(draft.uri) }
                        recordEventDurably(
                            AnalyticsEvent(
                                type = AnalyticsEventType.USER_DOCUMENT_ADDED,
                                timestampMillis = nowMillis,
                                contentId = result.item.id,
                                metadata = result.item.analyticsMetadata() + mapOf(
                                    "durationMinutes" to result.item.durationMinutes.toString(),
                                    "topicCount" to result.item.topicTags.size.toString(),
                                ),
                            ),
                        )
                        uiState = uiState.copy(
                            screen = MainScreen.AddLinkSuccess,
                            addDocumentForm = AddDocumentFormState(),
                            savedLinkConfirmation = result.item.toAddLinkConfirmation(),
                            latestMessage = null,
                        )
                    }

                    is AddUserDocumentResult.Rejected -> {
                        uiState = uiState.copy(
                            addDocumentForm = uiState.addDocumentForm.copy(
                                validationErrors = result.errors,
                                canSave = false,
                                isSaving = false,
                            ),
                            latestMessage = "This file needs a little cleanup before saving.",
                        )
                    }
                }
            } catch (error: Throwable) {
                if (error is CancellationException) throw error
                uiState = uiState.copy(
                    screen = MainScreen.AddDocument,
                    addDocumentForm = uiState.addDocumentForm.copy(isSaving = false),
                    latestMessage = "The file could not be saved locally. Try again.",
                )
            }
        }
    }

    fun triggerDebugIntervention(nowMillis: Long = System.currentTimeMillis()) {
        val targetApp = uiState.selectedTargetApp ?: return
        triggerIntervention(
            targetApp = targetApp,
            origin = InterventionOrigin.DEBUG,
            triggeredAtMillis = nowMillis,
            processingNowMillis = nowMillis,
        )
    }

    fun requestSystemInterception(
        targetAppPackage: String,
        nowMillis: Long = nowProvider(),
    ) {
        if (uiState.isLoadingSettings) {
            pendingSystemInterception = PendingSystemInterception(
                targetAppPackage = targetAppPackage,
                triggeredAtMillis = nowMillis,
            )
            return
        }

        val targetApp = findTargetApp(targetAppPackage) ?: return
        triggerIntervention(
            targetApp = targetApp,
            origin = InterventionOrigin.SYSTEM,
            triggeredAtMillis = nowMillis,
            processingNowMillis = nowProvider(),
        )
    }

    fun triggerIntervention(
        targetApp: DistractingApp,
        origin: InterventionOrigin,
        triggeredAtMillis: Long = nowProvider(),
        processingNowMillis: Long = triggeredAtMillis,
    ) {
        val preferences = uiState.preferences ?: return
        if (uiState.isLoadingSettings) {
            uiState = uiState.copy(latestMessage = "Local replacement state is still loading.")
            return
        }

        viewModelScope.launch {
            maybeRecordInterceptionLatencyDegradation(
                origin = origin,
                targetApp = targetApp,
                triggeredAtMillis = triggeredAtMillis,
                shownAtMillis = processingNowMillis,
            )

            val delayInspection = delayGate.inspectDelay(targetApp = targetApp, nowMillis = processingNowMillis)
            if (delayInspection.activeWindow != null) {
                recordDelayReturnDuringActiveWindow(targetApp = targetApp, nowMillis = processingNowMillis)
                val activeDelayWindow = delayGate.activeDelay(targetApp = targetApp, nowMillis = processingNowMillis)
                uiState = uiState.copy(
                    selectedTargetApp = targetApp,
                    activeDelayWindow = activeDelayWindow,
                    activeDelaySuggestion = activeDelaySuggestionFor(activeDelayWindow),
                    latestMessage = "${targetApp.displayName} is paused for 15 minutes.",
                    screen = MainScreen.Home,
                    currentInterventionOrigin = null,
                )
                return@launch
            }

            var delayReturnHandled = false
            delayInspection.expiredWindow?.let { expiredWindow ->
                delayReturnHandled = true
                recordDelayReturnAfterExpiry(expiredWindow = expiredWindow, nowMillis = processingNowMillis)
                delayGate.consumeExpiredDelay(
                    targetApp = targetApp,
                    delayId = expiredWindow.id,
                    nowMillis = processingNowMillis,
                )
            }

            if (!delayReturnHandled) {
                recordReturnSignalIfNeeded(targetApp = targetApp, nowMillis = processingNowMillis)
            }

            val interventionId = UUID.randomUUID().toString()
            val rawInventory = fullReplacementInventory()
            val filteredInventory = rawInventory.filter { item ->
                when (item.sourceType) {
                    ContentSourceType.MEDITATION -> true
                    ContentSourceType.USER_LINK -> item.availability != ContentAvailability.UNAVAILABLE
                    ContentSourceType.USER_DOCUMENT -> item.availability != ContentAvailability.UNAVAILABLE
                    ContentSourceType.EDITORIAL -> item.packId in preferences.selectedPackIds
                }
            }
            val inventoryDiagnostics = inventoryDiagnostics(
                rawInventory = rawInventory,
                eligibleInventory = filteredInventory,
                preferences = preferences,
                completedContentIds = uiState.completedContentIds,
                userLinks = uiState.userLinks,
                userDocuments = uiState.userDocuments,
            )
            val signals = buildRecommendationSignals(nowMillis = processingNowMillis)
            val recommendationSet = recommendationEngine.generate(
                targetApp = targetApp,
                preferences = preferences,
                inventory = filteredInventory,
                primaryExcludedIds = primaryExcludedIdsForRecommendation(),
                signals = signals,
                nowMillis = processingNowMillis,
            )

            if (recommendationSet == null) {
                recordEvent(
                    AnalyticsEvent(
                        type = AnalyticsEventType.NO_RECOMMENDATION_AVAILABLE,
                        timestampMillis = processingNowMillis,
                        interventionId = interventionId,
                        targetAppPackage = targetApp.packageName,
                        metadata = inventoryDiagnostics,
                    ),
                )
                uiState = uiState.copy(
                    selectedTargetApp = targetApp,
                    currentInterventionId = null,
                    currentInterventionShownAtMillis = null,
                    currentRecommendationSet = null,
                    activeDelayWindow = null,
                    activeDelaySuggestion = null,
                    currentInterventionOrigin = null,
                    latestMessage = "No replacement inventory is available yet.",
                    screen = MainScreen.Home,
                )
                return@launch
            }

            val backupIds = recommendationSet.backups.map(ContentItem::id)

            recordEvent(
                AnalyticsEvent(
                    type = AnalyticsEventType.INTERVENTION_SHOWN,
                    timestampMillis = processingNowMillis,
                    interventionId = interventionId,
                    targetAppPackage = targetApp.packageName,
                    primaryContentId = recommendationSet.primary.id,
                    backupContentIds = backupIds,
                    contentId = recommendationSet.primary.id,
                    metadata = recommendationSet.analyticsMetadata() + inventoryDiagnostics,
                ),
            )

            if (recommendationSet.inventoryShortage) {
                recordEvent(
                    AnalyticsEvent(
                        type = AnalyticsEventType.INVENTORY_SHORTAGE,
                        timestampMillis = processingNowMillis,
                        interventionId = interventionId,
                        targetAppPackage = targetApp.packageName,
                        primaryContentId = recommendationSet.primary.id,
                        backupContentIds = backupIds,
                        contentId = recommendationSet.primary.id,
                        metadata = recommendationSet.analyticsMetadata() + inventoryDiagnostics,
                    ),
                )
            }

            uiState = uiState.copy(
                selectedTargetApp = targetApp,
                currentInterventionId = interventionId,
                currentInterventionShownAtMillis = processingNowMillis,
                currentRecommendationSet = recommendationSet,
                currentInterventionOrigin = origin,
                activeDelayWindow = null,
                activeDelaySuggestion = null,
                latestMessage = null,
                screen = MainScreen.Intervention,
            )
        }
    }

    fun acceptPrimary() {
        val targetApp = uiState.selectedTargetApp ?: return
        val recommendationSet = uiState.currentRecommendationSet ?: return
        val interventionId = uiState.currentInterventionId ?: return
        val interventionShownAtMillis = uiState.currentInterventionShownAtMillis ?: return
        val nowMillis = System.currentTimeMillis()
        viewModelScope.launch {
            val sessionId = historyRepository.recordAcceptedSession(
                targetApp = targetApp,
                interventionId = interventionId,
                interventionShownAtMillis = interventionShownAtMillis,
                primaryContentId = recommendationSet.primary.id,
                backupContentIds = recommendationSet.backups.map(ContentItem::id),
                content = recommendationSet.primary,
                source = RecommendationSource.PRIMARY,
                acceptedAtMillis = nowMillis,
            )
            recordEvent(
                AnalyticsEvent(
                    type = AnalyticsEventType.PRIMARY_ACCEPTED,
                    timestampMillis = nowMillis,
                    interventionId = interventionId,
                    sessionId = sessionId,
                    targetAppPackage = targetApp.packageName,
                    primaryContentId = recommendationSet.primary.id,
                    backupContentIds = recommendationSet.backups.map(ContentItem::id),
                    contentId = recommendationSet.primary.id,
                    metadata = recommendationSet.primary.analyticsMetadata(),
                ),
            )
            openReplacementSession(content = recommendationSet.primary, sessionId = sessionId, startedAtMillis = nowMillis)
        }
    }

    fun acceptBackup(content: ContentItem) {
        val targetApp = uiState.selectedTargetApp ?: return
        val recommendationSet = uiState.currentRecommendationSet ?: return
        val interventionId = uiState.currentInterventionId ?: return
        val interventionShownAtMillis = uiState.currentInterventionShownAtMillis ?: return
        val nowMillis = System.currentTimeMillis()
        viewModelScope.launch {
            val sessionId = historyRepository.recordAcceptedSession(
                targetApp = targetApp,
                interventionId = interventionId,
                interventionShownAtMillis = interventionShownAtMillis,
                primaryContentId = recommendationSet.primary.id,
                backupContentIds = recommendationSet.backups.map(ContentItem::id),
                content = content,
                source = RecommendationSource.BACKUP,
                acceptedAtMillis = nowMillis,
            )
            recordEvent(
                AnalyticsEvent(
                    type = AnalyticsEventType.BACKUP_ACCEPTED,
                    timestampMillis = nowMillis,
                    interventionId = interventionId,
                    sessionId = sessionId,
                    targetAppPackage = targetApp.packageName,
                    primaryContentId = recommendationSet.primary.id,
                    backupContentIds = recommendationSet.backups.map(ContentItem::id),
                    contentId = content.id,
                    metadata = content.analyticsMetadata(),
                ),
            )
            openReplacementSession(content = content, sessionId = sessionId, startedAtMillis = nowMillis)
        }
    }

    fun delayFor15Minutes() {
        val targetApp = uiState.selectedTargetApp ?: return
        val recommendationSet = uiState.currentRecommendationSet ?: return
        val interventionId = uiState.currentInterventionId ?: return
        val nowMillis = System.currentTimeMillis()
        viewModelScope.launch {
            val window = delayGate.storeDelayDurably(
                targetApp = targetApp,
                nowMillis = nowMillis,
                interventionId = interventionId,
                interventionShownAtMillis = uiState.currentInterventionShownAtMillis,
                primaryContentId = recommendationSet.primary.id,
                backupContentIds = recommendationSet.backups.map(ContentItem::id),
            )
            recordDelaySelectedDurably(window)
            uiState = uiState.copy(
                screen = MainScreen.Home,
                currentInterventionId = null,
                currentInterventionShownAtMillis = null,
                currentRecommendationSet = null,
                currentInterventionOrigin = null,
                activeDelayWindow = window,
                activeDelaySuggestion = recommendationSet.shortestDelaySuggestion(),
                latestMessage = "${targetApp.displayName} paused for 15 minutes.",
            )
        }
    }

    fun startActiveDelayAlternative() {
        val targetApp = uiState.selectedTargetApp ?: return
        val delayWindow = uiState.activeDelayWindow ?: return
        val nowMillis = nowProvider()
        val activeDelayWindow = if (delayWindow.targetAppPackage == targetApp.packageName) {
            delayGate.activeDelay(targetApp = targetApp, nowMillis = nowMillis)
        } else {
            null
        }
        if (activeDelayWindow == null || activeDelayWindow.id != delayWindow.id || !activeDelayWindow.isActive(nowMillis)) {
            refreshActiveDelayWindow(nowMillis = nowMillis)
            return
        }
        val content = activeDelaySuggestionFor(activeDelayWindow, nowMillis = nowMillis)
        if (content == null) {
            uiState = uiState.copy(
                activeDelayWindow = activeDelayWindow,
                activeDelaySuggestion = null,
                latestMessage = "No paused alternative is available right now.",
            )
            return
        }
        val recommendationSet = recommendationSetForDelayWindow(delayWindow = activeDelayWindow, fallbackContent = content)
        val interventionId = activeDelayWindow.interventionId ?: "delay-${activeDelayWindow.id}"
        val interventionShownAtMillis = activeDelayWindow.interventionShownAtMillis ?: activeDelayWindow.startsAtMillis

        viewModelScope.launch {
            val source = if (content.id == recommendationSet.primary.id) {
                RecommendationSource.PRIMARY
            } else {
                RecommendationSource.BACKUP
            }
            val sessionId = historyRepository.recordAcceptedSession(
                targetApp = targetApp,
                interventionId = interventionId,
                interventionShownAtMillis = interventionShownAtMillis,
                primaryContentId = recommendationSet.primary.id,
                backupContentIds = recommendationSet.backups.map(ContentItem::id),
                content = content,
                source = source,
                acceptedAtMillis = nowMillis,
            )
            recordEvent(
                AnalyticsEvent(
                    type = AnalyticsEventType.DELAY_ALTERNATIVE_STARTED,
                    timestampMillis = nowMillis,
                    semanticKey = AnalyticsSemanticKeys.delayAlternativeStarted(
                        delayId = activeDelayWindow.id,
                        sessionId = sessionId,
                    ),
                    interventionId = interventionId,
                    sessionId = sessionId,
                    targetAppPackage = targetApp.packageName,
                    primaryContentId = recommendationSet.primary.id,
                    backupContentIds = recommendationSet.backups.map(ContentItem::id),
                    contentId = content.id,
                    metadata = content.analyticsMetadata() + mapOf(
                        "delayId" to activeDelayWindow.id,
                        "origin" to "active_delay_card",
                    ),
                ),
            )
            uiState = uiState.copy(
                currentInterventionId = interventionId,
                currentInterventionShownAtMillis = interventionShownAtMillis,
                currentRecommendationSet = recommendationSet,
                currentInterventionOrigin = null,
            )
            openReplacementSession(content = content, sessionId = sessionId, startedAtMillis = nowMillis)
        }
    }

    fun openAnyway(): Boolean {
        val targetApp = uiState.selectedTargetApp ?: return false
        val recommendationSet = uiState.currentRecommendationSet
        val nowMillis = System.currentTimeMillis()
        val shouldExitToTarget = uiState.currentInterventionOrigin == InterventionOrigin.SYSTEM
        recordEvent(
            AnalyticsEvent(
                type = AnalyticsEventType.OPEN_ANYWAY_SELECTED,
                timestampMillis = nowMillis,
                interventionId = uiState.currentInterventionId,
                targetAppPackage = targetApp.packageName,
                primaryContentId = recommendationSet?.primary?.id,
                backupContentIds = recommendationSet?.backups.orEmpty().map(ContentItem::id),
            ),
        )
        if (shouldExitToTarget) {
            InterceptionRuntimeGate.suppressPackage(
                targetAppPackage = targetApp.packageName,
                untilMillis = nowMillis + OPEN_ANYWAY_SUPPRESSION_WINDOW_MILLIS,
            )
        }
        uiState = uiState.copy(
            screen = MainScreen.Home,
            currentInterventionId = null,
            currentInterventionShownAtMillis = null,
            currentRecommendationSet = null,
            currentInterventionOrigin = null,
            activeDelaySuggestion = null,
            latestMessage = "Prototype override recorded for ${targetApp.displayName}.",
        )
        return shouldExitToTarget
    }

    fun finishReading() {
        val content = uiState.currentContent ?: return
        val sessionId = uiState.currentSessionId
        val nowMillis = System.currentTimeMillis()
        viewModelScope.launch {
            if (sessionId != null) {
                historyRepository.markCompleted(sessionId = sessionId, completedAtMillis = nowMillis)
                recordEvent(
                    AnalyticsEvent(
                        type = AnalyticsEventType.READER_COMPLETED,
                        timestampMillis = nowMillis,
                        interventionId = uiState.currentInterventionId,
                        sessionId = sessionId,
                        targetAppPackage = uiState.selectedTargetApp?.packageName,
                        primaryContentId = uiState.currentRecommendationSet?.primary?.id,
                        backupContentIds = uiState.currentRecommendationSet?.backups.orEmpty().map(ContentItem::id),
                        contentId = content.id,
                        metadata = sessionDurationMetadata(nowMillis) + content.analyticsMetadata(),
                    ),
                )
            }
            uiState = uiState.copy(
                screen = MainScreen.Feedback,
            )
        }
    }

    fun finishMeditationReset(nowMillis: Long = System.currentTimeMillis()) {
        val content = uiState.currentContent ?: return
        val sessionId = uiState.currentSessionId
        viewModelScope.launch {
            if (sessionId != null) {
                historyRepository.markCompleted(sessionId = sessionId, completedAtMillis = nowMillis)
                recordEvent(
                    AnalyticsEvent(
                        type = AnalyticsEventType.MEDITATION_TIMER_COMPLETED,
                        timestampMillis = nowMillis,
                        interventionId = uiState.currentInterventionId,
                        sessionId = sessionId,
                        targetAppPackage = uiState.selectedTargetApp?.packageName,
                        primaryContentId = uiState.currentRecommendationSet?.primary?.id,
                        backupContentIds = uiState.currentRecommendationSet?.backups.orEmpty().map(ContentItem::id),
                        contentId = content.id,
                        metadata = sessionDurationMetadata(nowMillis) + content.analyticsMetadata(),
                    ),
                )
            }
            uiState = uiState.copy(screen = MainScreen.Feedback)
        }
    }

    fun skipReading() {
        val content = uiState.currentContent ?: return
        val sessionId = uiState.currentSessionId
        val nowMillis = System.currentTimeMillis()
        viewModelScope.launch {
            if (sessionId != null) {
                historyRepository.markSkipped(sessionId = sessionId, skippedAtMillis = nowMillis)
                recordEvent(
                    AnalyticsEvent(
                        type = AnalyticsEventType.READER_SKIPPED,
                        timestampMillis = nowMillis,
                        interventionId = uiState.currentInterventionId,
                        sessionId = sessionId,
                        targetAppPackage = uiState.selectedTargetApp?.packageName,
                        primaryContentId = uiState.currentRecommendationSet?.primary?.id,
                        backupContentIds = uiState.currentRecommendationSet?.backups.orEmpty().map(ContentItem::id),
                        contentId = content.id,
                        metadata = sessionDurationMetadata(nowMillis) + content.analyticsMetadata(),
                    ),
                )
            }
            clearActiveSession(
                latestMessage = "Replacement session skipped.",
            )
        }
    }

    fun skipMeditationReset(nowMillis: Long = System.currentTimeMillis()) {
        val content = uiState.currentContent ?: return
        val sessionId = uiState.currentSessionId
        viewModelScope.launch {
            if (sessionId != null) {
                historyRepository.markSkipped(sessionId = sessionId, skippedAtMillis = nowMillis)
                recordEvent(
                    AnalyticsEvent(
                        type = AnalyticsEventType.MEDITATION_TIMER_SKIPPED,
                        timestampMillis = nowMillis,
                        interventionId = uiState.currentInterventionId,
                        sessionId = sessionId,
                        targetAppPackage = uiState.selectedTargetApp?.packageName,
                        primaryContentId = uiState.currentRecommendationSet?.primary?.id,
                        backupContentIds = uiState.currentRecommendationSet?.backups.orEmpty().map(ContentItem::id),
                        contentId = content.id,
                        metadata = sessionDurationMetadata(nowMillis) + content.analyticsMetadata(),
                    ),
                )
            }
            clearActiveSession(latestMessage = "Meditation reset skipped.")
        }
    }

    fun submitFeedback(fitRating: String, scrollRating: String) {
        val wasGoodFit = fitRating != FEEDBACK_FIT_NOT
        val helpedAvoidScrolling = scrollRating != FEEDBACK_SCROLL_NO
        val feedback = SessionFeedback(
            wasGoodFit = wasGoodFit,
            helpedAvoidScrolling = helpedAvoidScrolling,
            fitRating = fitRating,
            scrollRating = scrollRating,
            submittedAtMillis = System.currentTimeMillis(),
        )
        viewModelScope.launch {
            uiState.currentSessionId?.let { sessionId ->
                historyRepository.attachFeedback(sessionId = sessionId, feedback = feedback)
            }
            recordEvent(
                AnalyticsEvent(
                    type = AnalyticsEventType.FEEDBACK_SUBMITTED,
                    timestampMillis = feedback.submittedAtMillis,
                    interventionId = uiState.currentInterventionId,
                    sessionId = uiState.currentSessionId,
                    targetAppPackage = uiState.selectedTargetApp?.packageName,
                    primaryContentId = uiState.currentRecommendationSet?.primary?.id,
                    backupContentIds = uiState.currentRecommendationSet?.backups.orEmpty().map(ContentItem::id),
                    contentId = uiState.currentContent?.id,
                    metadata = mapOf(
                        "goodFit" to wasGoodFit.toString(),
                        "helpedAvoidScrolling" to helpedAvoidScrolling.toString(),
                        "fitRating" to fitRating,
                        "scrollRating" to scrollRating,
                    ),
                ),
            )
            clearActiveSession(
                lastFeedback = feedback,
                screen = MainScreen.Progress,
                latestMessage = "Feedback captured for the replacement session.",
            )
        }
    }

    fun skipFeedback() {
        clearActiveSession(
            screen = MainScreen.Progress,
            latestMessage = "Feedback skipped for this session.",
        )
    }

    fun selectThemeMode(themeMode: AppThemeMode) {
        if (uiState.themeMode == themeMode) return
        uiState = uiState.copy(themeMode = themeMode)
        viewModelScope.launch {
            settingsRepository.saveThemeMode(themeMode)
        }
    }

    fun dismissMessage() {
        uiState = uiState.copy(latestMessage = null)
    }

    private fun applySettings(settings: AppSettings) {
        val preferences = settings.toUserPreferences(
            supportedApps = supportedApps,
            fallbackPackIds = defaultSelectedPackIds,
        )
        val availableTargetApps = if (settings.hasCompletedOnboarding) preferences.selectedApps else emptyList()
        val selectedTargetApp = if (settings.hasCompletedOnboarding) {
            uiState.selectedTargetApp.takeIf { candidate ->
                availableTargetApps.any { it.packageName == candidate?.packageName }
            } ?: availableTargetApps.firstOrNull()
        } else {
            null
        }
        val activeDelayWindow = selectedTargetApp?.takeIf { delayReady }?.let { delayGate.activeDelay(it) }

        uiState = uiState.copy(
            hasCompletedOnboarding = settings.hasCompletedOnboarding,
            availableTargetApps = availableTargetApps,
            selectedTargetApp = selectedTargetApp,
            preferences = preferences.takeIf { settings.hasCompletedOnboarding },
            themeMode = settings.themeMode,
            onboardingSelection = settings.toOnboardingSelection(
                supportedApps = supportedApps,
                starterPacks = starterPacks,
            ),
            activeDelayWindow = activeDelayWindow,
            activeDelaySuggestion = activeDelaySuggestionFor(activeDelayWindow),
            permissionReadiness = interceptionMonitor.currentReadiness(),
            screen = when {
                !settings.hasCompletedOnboarding -> MainScreen.Onboarding
                uiState.screen == MainScreen.Onboarding -> MainScreen.Home
                uiState.screen == MainScreen.AddLink -> MainScreen.AddLink
                uiState.screen == MainScreen.AddDocument -> MainScreen.AddDocument
                uiState.screen == MainScreen.AddLinkSuccess -> MainScreen.AddLinkSuccess
                uiState.screen == MainScreen.Library -> MainScreen.Library
                uiState.screen == MainScreen.Progress -> MainScreen.Progress
                uiState.screen == MainScreen.Settings -> MainScreen.Settings
                else -> uiState.screen
            },
        )
        if (settings.hasCompletedOnboarding && delayReady) {
            selectedTargetApp?.let { targetApp ->
                viewModelScope.launch {
                    reconcilePersistedDelayAnalytics(targetApp = targetApp)
                }
            }
        }
    }

    private fun buildRecommendationSignals(nowMillis: Long): RecommendationSignals {
        val history = historyRepository.recentHistory(nowMillis = nowMillis)
        return RecommendationSignals(
            completedTopics = history.filter(ReplacementHistoryEntry::isCompleted)
                .flatMapTo(mutableSetOf(), ReplacementHistoryEntry::contentTopics),
            skippedTopics = history.filter(ReplacementHistoryEntry::isSkipped)
                .flatMapTo(mutableSetOf(), ReplacementHistoryEntry::contentTopics),
            successfulPackIds = history.filter { entry ->
                entry.isCompleted() && entry.feedbackHelpedAvoidScrolling != false
            }.mapTo(mutableSetOf(), ReplacementHistoryEntry::packId),
            timeOfDay = TimeOfDayBucket.from(nowMillis),
        )
    }

    private suspend fun recordReturnSignalIfNeeded(targetApp: DistractingApp, nowMillis: Long) {
        val signal = historyRepository.markReturnedToTarget(
            targetAppPackage = targetApp.packageName,
            returnedAtMillis = nowMillis,
        ) ?: return
        recordSessionReturnMetricsDurably(signal)
    }

    private fun maybeRecordInterceptionLatencyDegradation(
        origin: InterventionOrigin,
        targetApp: DistractingApp,
        triggeredAtMillis: Long,
        shownAtMillis: Long,
    ) {
        if (origin != InterventionOrigin.SYSTEM) {
            return
        }
        val delayMillis = shownAtMillis - triggeredAtMillis
        if (delayMillis <= INTERVENTION_DEGRADED_THRESHOLD_MILLIS) {
            return
        }
        recordEvent(
            AnalyticsEvent(
                type = AnalyticsEventType.INTERVENTION_DEGRADED_PERFORMANCE,
                timestampMillis = shownAtMillis,
                targetAppPackage = targetApp.packageName,
                metadata = mapOf(
                    "triggeredAtMillis" to triggeredAtMillis.toString(),
                    "shownAtMillis" to shownAtMillis.toString(),
                    "interceptionDelayMillis" to delayMillis.toString(),
                ),
            ),
        )
    }

    private suspend fun recordDelayReturnDuringActiveWindow(targetApp: DistractingApp, nowMillis: Long) {
        val activeWindow = delayGate.activeDelay(targetApp = targetApp, nowMillis = nowMillis) ?: return
        val recordedWindow = delayGate.recordFirstReturnAttemptDurably(targetApp = targetApp, nowMillis = nowMillis)
            ?: activeWindow
        recordDelaySelectedDurably(recordedWindow)
        recordDelayReturnMetricsDurably(
            window = recordedWindow,
            eventTimestampMillis = recordedWindow.firstReturnAttemptAtMillis ?: nowMillis,
            origin = "active_delay",
        )
    }

    private suspend fun recordDelayReturnAfterExpiry(expiredWindow: DelayWindow, nowMillis: Long) {
        recordDelaySelectedDurably(expiredWindow)
        recordEventDurably(
            AnalyticsEvent(
                type = AnalyticsEventType.RETURN_AFTER_DELAY_ENDED,
                timestampMillis = nowMillis,
                semanticKey = AnalyticsSemanticKeys.delayEnded(expiredWindow.id),
                interventionId = expiredWindow.interventionId,
                targetAppPackage = expiredWindow.targetAppPackage,
                primaryContentId = expiredWindow.primaryContentId,
                backupContentIds = expiredWindow.backupContentIds,
                contentId = expiredWindow.primaryContentId,
                metadata = mapOf(
                    "delayId" to expiredWindow.id,
                    "delayReturnOrigin" to "after_delay_expired",
                    "returnAttribution" to "delay",
                    "delayStartedAtMillis" to expiredWindow.startsAtMillis.toString(),
                    "delayEndedAtMillis" to expiredWindow.endsAtMillis.toString(),
                    "hadActiveDelayReturn" to (expiredWindow.firstReturnAttemptAtMillis != null).toString(),
                ),
            ),
        )
        if (expiredWindow.firstReturnAttemptAtMillis == null) {
            recordDelayReturnMetricsDurably(
                window = expiredWindow,
                eventTimestampMillis = nowMillis,
                origin = "after_delay_expired",
            )
        } else {
            recordDelayReturnMetricsDurably(
                window = expiredWindow,
                eventTimestampMillis = expiredWindow.firstReturnAttemptAtMillis,
                origin = "active_delay",
            )
        }
    }

    private suspend fun recordDelayReturnMetricsDurably(
        window: DelayWindow,
        eventTimestampMillis: Long,
        origin: String,
    ) {
        delayReturnMetricEvents(
            window = window,
            eventTimestampMillis = eventTimestampMillis,
            origin = origin,
        ).forEach { event ->
            recordEventDurably(event)
        }
    }

    private fun delayReturnMetricEvents(
        window: DelayWindow,
        eventTimestampMillis: Long,
        origin: String,
    ): List<AnalyticsEvent> {
        val anchorMillis = window.interventionShownAtMillis ?: window.startsAtMillis
        val delta = eventTimestampMillis - anchorMillis
        val metadata = mapOf(
            "delayId" to window.id,
            "delayReturnOrigin" to origin,
            "returnAttribution" to "delay",
            "delayStartedAtMillis" to window.startsAtMillis.toString(),
            "delayEndedAtMillis" to window.endsAtMillis.toString(),
        )
        val events = mutableListOf<AnalyticsEvent>()

        if (delta <= 15 * 60_000L) {
            events += AnalyticsEvent(
                type = AnalyticsEventType.RETURN_TO_APP_WITHIN_15_MINUTES,
                timestampMillis = eventTimestampMillis,
                semanticKey = AnalyticsSemanticKeys.delayReturn(
                    delayId = window.id,
                    origin = origin,
                    type = AnalyticsEventType.RETURN_TO_APP_WITHIN_15_MINUTES,
                ),
                interventionId = window.interventionId,
                targetAppPackage = window.targetAppPackage,
                primaryContentId = window.primaryContentId,
                backupContentIds = window.backupContentIds,
                contentId = window.primaryContentId,
                metadata = metadata,
            )
        }

        if (delta <= 60 * 60_000L) {
            events += AnalyticsEvent(
                type = AnalyticsEventType.RETURN_TO_APP_WITHIN_60_MINUTES,
                timestampMillis = eventTimestampMillis,
                semanticKey = AnalyticsSemanticKeys.delayReturn(
                    delayId = window.id,
                    origin = origin,
                    type = AnalyticsEventType.RETURN_TO_APP_WITHIN_60_MINUTES,
                ),
                interventionId = window.interventionId,
                targetAppPackage = window.targetAppPackage,
                primaryContentId = window.primaryContentId,
                backupContentIds = window.backupContentIds,
                contentId = window.primaryContentId,
                metadata = metadata,
            )
        }

        return events
    }

    private suspend fun recordSessionReturnMetricsDurably(signal: ReturnToTargetSignal) {
        if (signal.within15Minutes) {
            recordEventDurably(
                AnalyticsEvent(
                    type = AnalyticsEventType.RETURN_TO_APP_WITHIN_15_MINUTES,
                    timestampMillis = signal.returnedAtMillis,
                    semanticKey = AnalyticsSemanticKeys.sessionReturn(
                        sessionId = signal.sessionId,
                        type = AnalyticsEventType.RETURN_TO_APP_WITHIN_15_MINUTES,
                    ),
                    interventionId = signal.interventionId,
                    sessionId = signal.sessionId,
                    targetAppPackage = signal.targetAppPackage,
                    primaryContentId = signal.primaryContentId,
                    backupContentIds = signal.backupContentIds,
                    contentId = signal.contentId,
                    metadata = mapOf("returnAttribution" to "session"),
                ),
            )
        }

        if (signal.within60Minutes) {
            recordEventDurably(
                AnalyticsEvent(
                    type = AnalyticsEventType.RETURN_TO_APP_WITHIN_60_MINUTES,
                    timestampMillis = signal.returnedAtMillis,
                    semanticKey = AnalyticsSemanticKeys.sessionReturn(
                        sessionId = signal.sessionId,
                        type = AnalyticsEventType.RETURN_TO_APP_WITHIN_60_MINUTES,
                    ),
                    interventionId = signal.interventionId,
                    sessionId = signal.sessionId,
                    targetAppPackage = signal.targetAppPackage,
                    primaryContentId = signal.primaryContentId,
                    backupContentIds = signal.backupContentIds,
                    contentId = signal.contentId,
                    metadata = mapOf("returnAttribution" to "session"),
                ),
            )
        }
    }

    private suspend fun recordDelaySelectedDurably(window: DelayWindow) {
        recordEventDurably(
            AnalyticsEvent(
                type = AnalyticsEventType.DELAY_SELECTED,
                timestampMillis = window.startsAtMillis,
                semanticKey = AnalyticsSemanticKeys.delaySelected(window.id),
                interventionId = window.interventionId,
                targetAppPackage = window.targetAppPackage,
                primaryContentId = window.primaryContentId,
                backupContentIds = window.backupContentIds,
                contentId = window.primaryContentId,
                metadata = mapOf(
                    "delayId" to window.id,
                    "delayStartedAtMillis" to window.startsAtMillis.toString(),
                    "delayEndedAtMillis" to window.endsAtMillis.toString(),
                    "interventionShownAtMillis" to (window.interventionShownAtMillis?.toString() ?: ""),
                ),
            ),
        )
    }

    private suspend fun reconcilePersistedDelayAnalytics(
        targetApp: DistractingApp,
        nowMillis: Long = System.currentTimeMillis(),
    ) {
        val activeWindow = delayGate.activeDelay(targetApp = targetApp, nowMillis = nowMillis) ?: return
        recordDelaySelectedDurably(activeWindow)
        activeWindow.firstReturnAttemptAtMillis?.let { firstReturnAtMillis ->
            recordDelayReturnMetricsDurably(
                window = activeWindow,
                eventTimestampMillis = firstReturnAtMillis,
                origin = "active_delay",
            )
        }
    }

    fun currentExternalLinkUrl(): String? {
        return uiState.currentContent?.externalUrl
    }

    fun currentExternalContentMimeType(): String? {
        return when (uiState.currentContent?.format) {
            ContentFormat.PDF -> "application/pdf"
            ContentFormat.EPUB -> "application/epub+zip"
            ContentFormat.MARKDOWN -> "text/markdown"
            else -> null
        }
    }

    fun recordExternalLinkOpened(nowMillis: Long = System.currentTimeMillis()) {
        val content = uiState.currentContent ?: return
        val sessionId = uiState.currentSessionId ?: return
        content.externalUrl ?: return
        recordEvent(
            AnalyticsEvent(
                type = if (content.sourceType == ContentSourceType.USER_LINK) {
                    AnalyticsEventType.USER_LINK_FALLBACK_OPENED
                } else {
                    AnalyticsEventType.EXTERNAL_HANDOFF_OPENED
                },
                timestampMillis = nowMillis,
                interventionId = uiState.currentInterventionId,
                sessionId = sessionId,
                targetAppPackage = uiState.selectedTargetApp?.packageName,
                primaryContentId = uiState.currentRecommendationSet?.primary?.id,
                backupContentIds = uiState.currentRecommendationSet?.backups.orEmpty().map(ContentItem::id),
                contentId = content.id,
                metadata = content.analyticsMetadata(),
            ),
        )
    }

    fun recordExternalLinkHandoffFailed(
        reason: String,
        nowMillis: Long = System.currentTimeMillis(),
    ) {
        val content = uiState.currentContent ?: return
        val sessionId = uiState.currentSessionId
        viewModelScope.launch {
            recordEvent(
                AnalyticsEvent(
                    type = if (content.sourceType == ContentSourceType.USER_LINK) {
                        AnalyticsEventType.USER_LINK_HANDOFF_FAILED
                    } else {
                        AnalyticsEventType.EXTERNAL_HANDOFF_FAILED
                    },
                    timestampMillis = nowMillis,
                    interventionId = uiState.currentInterventionId,
                    sessionId = sessionId,
                    targetAppPackage = uiState.selectedTargetApp?.packageName,
                    primaryContentId = uiState.currentRecommendationSet?.primary?.id,
                    backupContentIds = uiState.currentRecommendationSet?.backups.orEmpty().map(ContentItem::id),
                    contentId = content.id,
                    metadata = content.analyticsMetadata() + mapOf("failureReason" to reason),
                ),
            )
            when (content.sourceType) {
                ContentSourceType.USER_LINK -> userLinkRepository.markUnavailable(contentId = content.id, nowMillis = nowMillis)
                ContentSourceType.USER_DOCUMENT -> userDocumentRepository.markUnavailable(contentId = content.id, nowMillis = nowMillis)
                else -> Unit
            }
            if (sessionId != null) {
                historyRepository.markSkipped(sessionId = sessionId, skippedAtMillis = nowMillis)
            }
            clearActiveSession(
                latestMessage = content.handoffFailureMessage(),
            )
        }
    }

    private suspend fun openReplacementSession(content: ContentItem, sessionId: String, startedAtMillis: Long) {
        val contentBody = if (content.usesRepositoryBody()) {
            try {
                contentRepository.contentBody(content)
            } catch (error: Throwable) {
                if (error is CancellationException) throw error
                handleRepositoryBodyLoadFailure(
                    content = content,
                    sessionId = sessionId,
                    failedAtMillis = startedAtMillis,
                    error = error,
                )
                return
            }
        } else {
            ""
        }
        uiState = uiState.copy(
            currentContent = content,
            currentContentBody = contentBody,
            currentSessionId = sessionId,
            currentSessionStartedAtMillis = startedAtMillis,
            screen = screenForReplacement(content),
        )
    }

    private suspend fun handleRepositoryBodyLoadFailure(
        content: ContentItem,
        sessionId: String?,
        failedAtMillis: Long,
        error: Throwable,
    ) {
        recordEvent(
            AnalyticsEvent(
                type = AnalyticsEventType.USER_DOCUMENT_BODY_LOAD_FAILED,
                timestampMillis = failedAtMillis,
                interventionId = uiState.currentInterventionId,
                sessionId = sessionId,
                targetAppPackage = uiState.selectedTargetApp?.packageName,
                primaryContentId = uiState.currentRecommendationSet?.primary?.id,
                backupContentIds = uiState.currentRecommendationSet?.backups.orEmpty().map(ContentItem::id),
                contentId = content.id,
                metadata = content.analyticsMetadata() + mapOf(
                    "failureReason" to (error::class.simpleName ?: "body_load_failed"),
                ),
            ),
        )
        if (content.sourceType == ContentSourceType.USER_DOCUMENT) {
            userDocumentRepository.markUnavailable(contentId = content.id, nowMillis = failedAtMillis)
        }
        if (sessionId != null) {
            historyRepository.markSkipped(sessionId = sessionId, skippedAtMillis = failedAtMillis)
        }
        clearActiveSession(
            latestMessage = content.handoffFailureMessage(),
        )
    }

    private fun screenForReplacement(content: ContentItem): MainScreen {
        return when {
            content.usesExternalHandoff() -> MainScreen.ExternalHandoff
            content.usesMeditationTimer() -> MainScreen.MeditationTimer
            else -> MainScreen.Reader
        }
    }

    private fun fullReplacementInventory(): List<ContentItem> {
        return contentRepository.inventory() + MeditationTimerContentItem
    }

    private fun primaryExcludedIdsForRecommendation(): Set<String> {
        return uiState.completedContentIds - MEDITATION_TIMER_CONTENT_ID
    }

    private fun clearActiveSession(
        lastFeedback: SessionFeedback? = uiState.lastFeedback,
        screen: MainScreen = MainScreen.Home,
        latestMessage: String,
    ) {
        uiState = uiState.copy(
            screen = screen,
            currentInterventionId = null,
            currentInterventionShownAtMillis = null,
            currentContent = null,
            currentContentBody = "",
            currentRecommendationSet = null,
            currentInterventionOrigin = null,
            currentSessionId = null,
            currentSessionStartedAtMillis = null,
            lastFeedback = lastFeedback,
            latestMessage = latestMessage,
        )
    }

    private fun sessionDurationMetadata(nowMillis: Long): Map<String, String> {
        val startedAtMillis = uiState.currentSessionStartedAtMillis ?: return emptyMap()
        val durationSeconds = ((nowMillis - startedAtMillis) / 1000L).coerceAtLeast(0)
        return mapOf("sessionDurationSeconds" to durationSeconds.toString())
    }

    private fun recordEvent(event: AnalyticsEvent) {
        analyticsTracker.record(event)
    }

    private suspend fun recordEventDurably(event: AnalyticsEvent) {
        analyticsTracker.recordDurably(event)
    }

    private fun refreshActiveDelayWindow(nowMillis: Long = System.currentTimeMillis()) {
        val selectedTargetApp = uiState.selectedTargetApp ?: return
        if (!delayReady) {
            return
        }
        val activeDelayWindow = delayGate.activeDelay(targetApp = selectedTargetApp, nowMillis = nowMillis)
        val activeDelaySuggestion = activeDelaySuggestionFor(activeDelayWindow)
        if (uiState.activeDelayWindow != activeDelayWindow || uiState.activeDelaySuggestion != activeDelaySuggestion) {
            uiState = uiState.copy(
                activeDelayWindow = activeDelayWindow,
                activeDelaySuggestion = activeDelaySuggestion,
            )
        }
    }

    private fun activeDelaySuggestionFor(
        delayWindow: DelayWindow?,
        nowMillis: Long = nowProvider(),
    ): ContentItem? {
        delayWindow ?: return null
        if (!delayWindow.isActive(nowMillis)) {
            return null
        }
        val candidateIds = (delayWindow.backupContentIds + listOfNotNull(delayWindow.primaryContentId)).toSet()
        if (candidateIds.isEmpty()) {
            return null
        }
        return fullReplacementInventory()
            .asSequence()
            .filter { item ->
                item.id in candidateIds &&
                    item.availability != ContentAvailability.UNAVAILABLE
            }
            .minWithOrNull(compareBy<ContentItem> { it.durationMinutes }.thenBy { it.title })
    }

    private fun RecommendationSet.shortestDelaySuggestion(): ContentItem {
        return (backups + primary).minWith(compareBy<ContentItem> { it.durationMinutes }.thenBy { it.title })
    }

    private fun recommendationSetForDelayWindow(
        delayWindow: DelayWindow,
        fallbackContent: ContentItem,
    ): RecommendationSet {
        val candidatesById = fullReplacementInventory().associateBy(ContentItem::id)
        val primary = delayWindow.primaryContentId
            ?.let(candidatesById::get)
            ?: fallbackContent
        val backups = delayWindow.backupContentIds
            .mapNotNull(candidatesById::get)
            .filterNot { it.id == primary.id }
        return RecommendationSet(
            primary = primary,
            backups = backups,
            inventoryShortage = backups.size < 2,
            generatedAtMillis = delayWindow.startsAtMillis,
        )
    }

    private fun updateHydrationState() {
        val isReady = settingsLoaded && contentReady && analyticsReady && historyReady && delayReady
        if (uiState.isLoadingSettings != !isReady) {
            uiState = uiState.copy(isLoadingSettings = !isReady)
        }
        if (isReady) {
            val pending = pendingSystemInterception ?: return
            pendingSystemInterception = null
            requestSystemInterception(
                targetAppPackage = pending.targetAppPackage,
                nowMillis = pending.triggeredAtMillis,
            )
        }
    }

    internal fun closeForTests() {
        viewModelScope.cancel()
    }

    private fun findTargetApp(targetAppPackage: String): DistractingApp? {
        return uiState.availableTargetApps.firstOrNull { it.packageName == targetAppPackage }
    }

    private fun updateAddLinkForm(form: AddLinkFormState) {
        uiState = uiState.copy(
            addLinkForm = form.copy(
                validationErrors = form.visibleValidationErrors(),
                canSave = form.localValidationErrors().isEmpty(),
                isSaving = false,
            ),
            savedLinkConfirmation = null,
        )
    }

    private fun updateAddDocumentForm(
        form: AddDocumentFormState,
        screen: MainScreen = uiState.screen,
    ) {
        uiState = uiState.copy(
            screen = screen,
            addDocumentForm = form.copy(
                validationErrors = form.visibleValidationErrors(),
                canSave = form.localValidationErrors().isEmpty(),
                isSaving = false,
            ),
            savedLinkConfirmation = null,
        )
    }
}

class MainViewModelFactory(
    private val appContainer: AppContainer,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(MainViewModel::class.java))
        @Suppress("UNCHECKED_CAST")
        return MainViewModel(
            contentRepository = appContainer.contentRepository,
            userLinkRepository = appContainer.userLinkRepository,
            userDocumentRepository = appContainer.userDocumentRepository,
            settingsRepository = appContainer.settingsRepository,
            recommendationEngine = appContainer.recommendationEngine,
            delayGate = appContainer.delayGate,
            analyticsTracker = appContainer.analyticsTracker,
            historyRepository = appContainer.historyRepository,
            interceptionMonitor = appContainer.interceptionMonitor,
        ) as T
    }
}

private fun defaultOnboardingSelection(
    supportedApps: List<DistractingApp>,
    starterPacks: List<EditorialPack>,
): OnboardingSelection {
    val defaultSocialPackages = listOf(
        "com.instagram.android",
        "com.zhiliaoapp.musically",
        "com.twitter.android",
        "com.reddit.frontpage",
    )
    val defaultSelectedPackages = defaultSocialPackages
        .filterTo(mutableSetOf()) { candidate ->
            supportedApps.any { app -> app.packageName == candidate }
        }
        .ifEmpty {
            supportedApps.take(3).mapTo(mutableSetOf(), DistractingApp::packageName)
    }
    return OnboardingSelection(
        selectedAppPackages = defaultSelectedPackages,
        preferredTopics = defaultPrototypeTopics(),
        preferredDurationBucket = DurationBucket.FOCUS,
        selectedPackIds = defaultStarterPackIds(starterPacks),
    )
}

private fun defaultStarterPackIds(starterPacks: List<EditorialPack>): Set<String> {
    val selected = starterPacks.take(1).mapTo(mutableSetOf(), EditorialPack::id)
    if (starterPacks.any { pack -> pack.id == ATTENTION_CLASSICS_PACK_ID }) {
        selected += ATTENTION_CLASSICS_PACK_ID
    }
    if (starterPacks.any { pack -> pack.id == PUBLIC_DOMAIN_EXPANSION_PACK_ID }) {
        selected += PUBLIC_DOMAIN_EXPANSION_PACK_ID
    }
    if (starterPacks.any { pack -> pack.id == LINK_ONLY_MODERN_PACK_ID }) {
        selected += LINK_ONLY_MODERN_PACK_ID
    }
    return selected
}

private fun AppSettings.toUserPreferences(
    supportedApps: List<DistractingApp>,
    fallbackPackIds: Set<String>,
): UserPreferences {
    val selectedApps = selectedAppPackages.mapNotNull(SupportedCatalog::findByPackage)
        .ifEmpty { supportedApps.take(3) }
    val selectedTopics = preferredTopics.ifEmpty { defaultPrototypeTopics() }
    val packs = selectedPackIds.ifEmpty { fallbackPackIds.take(1).toSet() }
    return UserPreferences(
        selectedApps = selectedApps,
        preferredTopics = selectedTopics,
        preferredDurationBucket = preferredDurationBucket,
        selectedPackIds = packs,
    )
}

private fun ContentItem.toAddLinkConfirmation(): AddLinkConfirmation {
    return AddLinkConfirmation(
        title = title,
        host = when (sourceType) {
            ContentSourceType.USER_DOCUMENT -> sourceLabel.orEmpty().ifBlank { "your file" }
            else -> externalUrl?.hostLabel().orEmpty().ifBlank { "your link" }
        },
        durationMinutes = durationMinutes,
        topicLabel = topicTags.firstOrNull()?.displayName().orEmpty().ifBlank { "Reading" },
    )
}

private fun defaultDocumentDuration(mimeType: String?, displayName: String): String {
    return when (UserDocumentValidator.detectFormat(displayName = displayName, mimeType = mimeType)) {
        ContentFormat.MARKDOWN -> "8"
        ContentFormat.PDF -> "15"
        ContentFormat.EPUB -> "20"
        else -> "10"
    }
}

private fun ContentItem.handoffFailureMessage(): String {
    return when (sourceType) {
        ContentSourceType.USER_LINK -> "This saved link could not be opened and was removed from future recommendations."
        ContentSourceType.USER_DOCUMENT -> "This saved file could not be opened and was removed from future recommendations."
        else -> "This external recommendation could not be opened."
    }
}

private fun String.hostLabel(): String {
    return runCatching {
        val normalized = if (startsWith("http://") || startsWith("https://")) this else "https://$this"
        java.net.URI.create(normalized).host.orEmpty().removePrefix("www.")
    }.getOrDefault("")
}

private fun TopicTag.displayName(): String {
    return when (this) {
        TopicTag.ESSAYS -> "Essays"
        TopicTag.PHILOSOPHY -> "Philosophy"
        TopicTag.SCIENCE -> "Science"
        TopicTag.DESIGN -> "Design"
        TopicTag.POETRY -> "Poetry"
        TopicTag.HISTORY -> "History"
        TopicTag.TECH -> "Tech"
        TopicTag.FICTION -> "Fiction"
        TopicTag.CLIMATE -> "Climate"
        TopicTag.ECONOMICS -> "Economics"
        TopicTag.FOOD -> "Food"
        TopicTag.ARCHITECTURE -> "Architecture"
        TopicTag.CREATIVITY -> "Design"
        TopicTag.PSYCHOLOGY -> "Psychology"
    }
}

private fun defaultPrototypeTopics(): Set<TopicTag> = setOf(
    TopicTag.ESSAYS,
    TopicTag.SCIENCE,
    TopicTag.DESIGN,
)

private const val ATTENTION_CLASSICS_PACK_ID = "attention-classics-v1"
private const val PUBLIC_DOMAIN_EXPANSION_PACK_ID = "public-domain-expansion-v2"
private const val LINK_ONLY_MODERN_PACK_ID = "link-only-modern-v1"

private fun AppSettings.toOnboardingSelection(
    supportedApps: List<DistractingApp>,
    starterPacks: List<EditorialPack>,
): OnboardingSelection {
    return if (hasCompletedOnboarding) {
        OnboardingSelection(
            selectedAppPackages = selectedAppPackages,
            preferredTopics = preferredTopics,
            preferredDurationBucket = preferredDurationBucket,
            selectedPackIds = selectedPackIds,
        )
    } else {
        defaultOnboardingSelection(
            supportedApps = supportedApps,
            starterPacks = starterPacks,
        )
    }
}

private fun unavailablePermissionReadiness(): PermissionReadiness {
    return PermissionReadiness(
        overlayStatus = PermissionStatus.MISSING,
        accessibilityStatus = PermissionStatus.UNAVAILABLE_IN_BUILD,
        interceptionReady = false,
        summary = "System interception is not active in this build.",
    )
}

private const val ACTIVE_DELAY_REFRESH_INTERVAL_MILLIS = 1_000L
private const val OPEN_ANYWAY_SUPPRESSION_WINDOW_MILLIS = 60_000L
private const val INTERVENTION_DEGRADED_THRESHOLD_MILLIS = 2_000L
private const val MIN_SELECTED_DISTRACTING_APPS = 3
private const val PROGRESS_HISTORY_WINDOW_DAYS = 31
private const val FEEDBACK_FIT_NOT = "not"
private const val FEEDBACK_SCROLL_NO = "no"

private data class PendingSystemInterception(
    val targetAppPackage: String,
    val triggeredAtMillis: Long,
)

private fun AddLinkFormState.toDraftOrNull(): UserLinkDraft? {
    val duration = durationMinutes.toIntOrNull() ?: return null
    if (localValidationErrors().isNotEmpty()) {
        return null
    }
    return UserLinkDraft(
        url = url,
        title = title,
        durationMinutes = duration,
        topicTags = selectedTopics,
    )
}

private fun AddDocumentFormState.toDraftOrNull(): UserDocumentDraft? {
    val duration = durationMinutes.toIntOrNull() ?: return null
    if (localValidationErrors().isNotEmpty()) {
        return null
    }
    return UserDocumentDraft(
        uri = uri,
        displayName = displayName,
        mimeType = mimeType,
        title = title,
        durationMinutes = duration,
        topicTags = selectedTopics,
    )
}

private fun AddLinkFormState.localValidationErrors(): Set<UserLinkValidationError> {
    val errors = mutableSetOf<UserLinkValidationError>()
    val trimmedUrl = url.trim()
    val duration = durationMinutes.toIntOrNull()
    errors += UserLinkValidator.validateUrl(trimmedUrl).errors
    if (title.isBlank()) {
        errors += UserLinkValidationError.BLANK_TITLE
    }
    if (duration == null || duration !in 1..60) {
        errors += UserLinkValidationError.INVALID_DURATION
    }
    if (selectedTopics.isEmpty()) {
        errors += UserLinkValidationError.NO_TOPICS
    }
    return errors
}

private fun AddDocumentFormState.localValidationErrors(): Set<UserDocumentValidationError> {
    val duration = durationMinutes.toIntOrNull()
    return UserDocumentValidator.validate(
        UserDocumentDraft(
            uri = uri,
            displayName = displayName,
            mimeType = mimeType,
            title = title,
            durationMinutes = duration ?: -1,
            topicTags = selectedTopics,
        ),
    ).errors
}

private fun AddLinkFormState.visibleValidationErrors(): Set<UserLinkValidationError> {
    val errors = mutableSetOf<UserLinkValidationError>()
    val trimmedUrl = url.trim()
    val duration = durationMinutes.trim()
    val validUrl = UserLinkValidator.validateUrl(trimmedUrl).isValid
    val validDuration = duration.toIntOrNull()?.let { it in 1..60 } == true
    if (trimmedUrl.isBlank() && (title.isNotBlank() || selectedTopics.isNotEmpty())) {
        errors += UserLinkValidationError.EMPTY_URL
    } else if (trimmedUrl.isNotBlank()) {
        errors += UserLinkValidator.validateUrl(trimmedUrl).errors
    }
    if (duration.isNotBlank() && !validDuration) {
        errors += UserLinkValidationError.INVALID_DURATION
    }
    if (duration.isBlank() && (trimmedUrl.isNotBlank() || title.isNotBlank() || selectedTopics.isNotEmpty())) {
        errors += UserLinkValidationError.INVALID_DURATION
    }
    if (title.isBlank() && (validUrl || selectedTopics.isNotEmpty())) {
        errors += UserLinkValidationError.BLANK_TITLE
    }
    if (selectedTopics.isEmpty() && validUrl && title.isNotBlank() && validDuration) {
        errors += UserLinkValidationError.NO_TOPICS
    }
    return errors
}

private fun AddDocumentFormState.visibleValidationErrors(): Set<UserDocumentValidationError> {
    val errors = mutableSetOf<UserDocumentValidationError>()
    val duration = durationMinutes.trim()
    val validDuration = duration.toIntOrNull()?.let { it in 1..120 } == true
    val format = UserDocumentValidator.detectFormat(displayName = displayName, mimeType = mimeType)
    if (uri.isBlank()) {
        errors += UserDocumentValidationError.EMPTY_URI
    }
    if (format == null) {
        errors += UserDocumentValidationError.UNSUPPORTED_FORMAT
    }
    if (duration.isNotBlank() && !validDuration) {
        errors += UserDocumentValidationError.INVALID_DURATION
    }
    if (duration.isBlank() && (title.isNotBlank() || selectedTopics.isNotEmpty())) {
        errors += UserDocumentValidationError.INVALID_DURATION
    }
    if (title.isBlank() && (uri.isNotBlank() || selectedTopics.isNotEmpty())) {
        errors += UserDocumentValidationError.BLANK_TITLE
    }
    if (selectedTopics.isEmpty() && uri.isNotBlank() && title.isNotBlank() && validDuration) {
        errors += UserDocumentValidationError.NO_TOPICS
    }
    return errors
}

private fun RecommendationSet.analyticsMetadata(): Map<String, String> {
    return primary.analyticsMetadata(prefix = "primary") +
        backups.flatMapIndexed { index, content ->
            content.analyticsMetadata(prefix = "backup${index + 1}").entries
        }.associate { it.toPair() }
}

private fun inventoryDiagnostics(
    rawInventory: List<ContentItem>,
    eligibleInventory: List<ContentItem>,
    preferences: UserPreferences,
    completedContentIds: Set<String>,
    userLinks: List<ContentItem>,
    userDocuments: List<ContentItem>,
): Map<String, String> {
    val unavailableUserLinkIds = (rawInventory + userLinks)
        .asSequence()
        .filter { item ->
            item.sourceType == ContentSourceType.USER_LINK &&
                item.availability == ContentAvailability.UNAVAILABLE
        }
        .map(ContentItem::id)
        .toSet()
    val unavailableUserDocumentIds = (rawInventory + userDocuments)
        .asSequence()
        .filter { item ->
            item.sourceType == ContentSourceType.USER_DOCUMENT &&
                item.availability == ContentAvailability.UNAVAILABLE
        }
        .map(ContentItem::id)
        .toSet()
    return mapOf(
        "selectedPackCount" to preferences.selectedPackIds.size.toString(),
        "selectedPackIds" to preferences.selectedPackIds.sorted().joinToString(","),
        "eligibleInventoryCount" to eligibleInventory.size.toString(),
        "eligibleEditorialCount" to eligibleInventory.count { it.sourceType == ContentSourceType.EDITORIAL }.toString(),
        "eligibleMeditationCount" to eligibleInventory.count { it.sourceType == ContentSourceType.MEDITATION }.toString(),
        "eligibleUserLinkCount" to eligibleInventory.count { it.sourceType == ContentSourceType.USER_LINK }.toString(),
        "eligibleUserDocumentCount" to eligibleInventory.count { it.sourceType == ContentSourceType.USER_DOCUMENT }.toString(),
        "unavailableUserLinkCount" to unavailableUserLinkIds.size.toString(),
        "unavailableUserDocumentCount" to unavailableUserDocumentIds.size.toString(),
        "completedContentCount" to completedContentIds.size.toString(),
    )
}

private fun ContentItem.analyticsMetadata(prefix: String? = null): Map<String, String> {
    fun key(name: String): String = prefix?.let { "${it}_$name" } ?: name
    return buildMap {
        put(key("sourceType"), sourceType.name)
        put(key("availability"), availability.name)
        put(key("format"), format.name)
        put(key("packId"), packId)
        put(key("rightsClass"), rights.rightsClass.name)
        put(key("renderMode"), rights.renderMode.name)
        externalUrl?.let { url -> put(key("externalUrl"), url) }
    }
}

private object EmptyUserLinkRepository : UserLinkRepository {
    override fun userLinks(): List<ContentItem> = emptyList()

    override fun observeUserLinks() = flowOf(emptyList<ContentItem>())

    override suspend fun addLink(
        draft: UserLinkDraft,
        nowMillis: Long,
    ): AddUserLinkResult = AddUserLinkResult.Rejected(setOf(UserLinkValidationError.UNSUPPORTED_SCHEME))

    override suspend fun markUnavailable(
        contentId: String,
        nowMillis: Long,
    ) = Unit
}

private object EmptyUserDocumentRepository : UserDocumentRepository {
    override fun userDocuments(): List<ContentItem> = emptyList()

    override fun observeUserDocuments() = flowOf(emptyList<ContentItem>())

    override suspend fun addDocument(
        draft: UserDocumentDraft,
        nowMillis: Long,
    ): AddUserDocumentResult = AddUserDocumentResult.Rejected(setOf(UserDocumentValidationError.UNSUPPORTED_FORMAT))

    override suspend fun markUnavailable(
        contentId: String,
        nowMillis: Long,
    ) = Unit
}
