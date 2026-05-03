package com.qualityalternative.app.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.qualityalternative.app.data.AppContainer
import com.qualityalternative.app.data.ReadingTimeEstimateSource
import com.qualityalternative.app.data.ReadingTimeEstimator
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
import com.qualityalternative.app.domain.model.ContentPriority
import com.qualityalternative.app.domain.model.ContentSourceType
import com.qualityalternative.app.domain.model.DEFAULT_MEDITATION_MINUTES
import com.qualityalternative.app.domain.model.DEFAULT_OPEN_ANYWAY_UNLOCK_MINUTES
import com.qualityalternative.app.domain.model.DelayWindow
import com.qualityalternative.app.domain.model.DistractingApp
import com.qualityalternative.app.domain.model.DurationBucket
import com.qualityalternative.app.domain.model.EditorialPack
import com.qualityalternative.app.domain.model.MEDITATION_TIMER_CONTENT_ID
import com.qualityalternative.app.domain.model.MAX_OPEN_ANYWAY_UNLOCK_MINUTES
import com.qualityalternative.app.domain.model.MIN_OPEN_ANYWAY_UNLOCK_MINUTES
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
import com.qualityalternative.app.domain.model.ReaderDocument
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
import com.qualityalternative.app.domain.model.meditationTimerContentItem
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
import com.qualityalternative.app.domain.service.ReadingAnnotationRepository
import com.qualityalternative.app.domain.service.ReadingAnnotationDriveSyncClient
import com.qualityalternative.app.domain.service.ReadingAnnotationDriveSyncRequest
import com.qualityalternative.app.domain.service.ReadingAnnotationDriveTokenProvider
import com.qualityalternative.app.domain.service.ReadingAnnotationExportFormatter
import com.qualityalternative.app.domain.service.ReadingAnnotationExportWriter
import com.qualityalternative.app.domain.service.ReadingProgressRepository
import com.qualityalternative.app.domain.service.SettingsRepository
import com.qualityalternative.app.domain.service.UserDocumentRepository
import com.qualityalternative.app.domain.service.UserLinkRepository
import com.qualityalternative.app.interception.InterceptionRuntimeGate
import java.io.InputStream
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
    val currentReaderDocument: ReaderDocument? = null,
    val currentContentBody: String = "",
    val currentReadingProgress: ReadingProgress? = null,
    val currentReaderStartParagraphIndex: Int? = null,
    val currentSessionId: String? = null,
    val currentSessionStartedAtMillis: Long? = null,
    val activeDelayWindow: DelayWindow? = null,
    val activeDelaySuggestion: ContentItem? = null,
    val permissionReadiness: PermissionReadiness = unavailablePermissionReadiness(),
    val historyEntries: List<ReplacementHistoryEntry> = emptyList(),
    val completedContentIds: Set<String> = emptySet(),
    val readingProgress: List<ReadingProgress> = emptyList(),
    val readingAnnotations: List<ReadingAnnotation> = emptyList(),
    val userLinks: List<ContentItem> = emptyList(),
    val userDocuments: List<ContentItem> = emptyList(),
    val addLinkForm: AddLinkFormState = AddLinkFormState(),
    val addDocumentForm: AddDocumentFormState = AddDocumentFormState(),
    val savedLinkConfirmation: AddLinkConfirmation? = null,
    val themeMode: AppThemeMode = AppThemeMode.LIGHT,
    val meditationDurationMinutes: Int = DEFAULT_MEDITATION_MINUTES,
    val contentPriority: ContentPriority = ContentPriority.BALANCED,
    val priorityContentIds: Set<String> = emptySet(),
    val reactivatedCompletedContentIds: Set<String> = emptySet(),
    val openAnywayUnlockMinutes: Int = DEFAULT_OPEN_ANYWAY_UNLOCK_MINUTES,
    val annotationExportUri: String? = null,
    val annotationExportDisplayName: String? = null,
    val annotationExportLastSuccessfulAtMillis: Long? = null,
    val annotationExportLastError: String? = null,
    val annotationDriveSyncEnabled: Boolean = false,
    val annotationDriveFolderId: String? = null,
    val annotationDriveLastSuccessfulAtMillis: Long? = null,
    val annotationDriveLastError: String? = null,
    val isAnnotationDriveSyncing: Boolean = false,
    val isManagingLibrary: Boolean = false,
    val selectedLibraryContentIds: Set<String> = emptySet(),
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
    val savedCount: Int = 1,
    val skippedCount: Int = 0,
    val priorityMarked: Boolean = false,
)

data class AddLinkFormState(
    val url: String = "",
    val title: String = "",
    val durationMinutes: String = ReadingTimeEstimator.DEFAULT_LINK_MINUTES.toString(),
    val selectedTopics: Set<TopicTag> = emptySet(),
    val markPriority: Boolean = false,
    val validationErrors: Set<UserLinkValidationError> = emptySet(),
    val canSave: Boolean = false,
    val isSaving: Boolean = false,
)

data class DocumentImportCandidate(
    val uri: String,
    val displayName: String,
    val mimeType: String? = null,
    val title: String,
    val durationMinutes: String,
    val format: ContentFormat? = null,
    val estimateSource: ReadingTimeEstimateSource = ReadingTimeEstimateSource.FALLBACK_DEFAULT,
    val estimatedWordCount: Int? = null,
)

data class AddDocumentFormState(
    val uri: String = "",
    val displayName: String = "",
    val mimeType: String? = null,
    val title: String = "",
    val durationMinutes: String = ReadingTimeEstimator.DEFAULT_DOCUMENT_MINUTES.toString(),
    val candidates: List<DocumentImportCandidate> = emptyList(),
    val selectedTopics: Set<TopicTag> = emptySet(),
    val markPriority: Boolean = false,
    val validationErrors: Set<UserDocumentValidationError> = emptySet(),
    val canSave: Boolean = false,
    val isSaving: Boolean = false,
) {
    val importCount: Int
        get() = candidates.size.coerceAtLeast(if (uri.isBlank()) 0 else 1)

    val supportedImportCount: Int
        get() = documentCandidates().count { candidate -> candidate.format != null }

    val unsupportedImportCount: Int
        get() = documentCandidates().count { candidate -> candidate.format == null }
}

enum class MainScreen {
    Onboarding,
    Home,
    Library,
    Annotations,
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
    private val readingProgressRepository: ReadingProgressRepository = EmptyReadingProgressRepository,
    private val readingAnnotationRepository: ReadingAnnotationRepository = EmptyReadingAnnotationRepository,
    private val readingAnnotationExportWriter: ReadingAnnotationExportWriter = NoOpReadingAnnotationExportWriter,
    private val readingAnnotationDriveSyncClient: ReadingAnnotationDriveSyncClient = NoOpReadingAnnotationDriveSyncClient,
    private val readingAnnotationDriveTokenProvider: ReadingAnnotationDriveTokenProvider = NoOpReadingAnnotationDriveTokenProvider,
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
    private var readingProgressReady = readingProgressRepository.isReady()
    private var readingAnnotationReady = readingAnnotationRepository.isReady()
    private var delayReady = delayGate.isReady()
    private var historyCompletedContentIds: Set<String> = emptySet()
    private var readingProgressCompletedContentIds: Set<String> = emptySet()
    private var pendingSystemInterception: PendingSystemInterception? = null
    private var annotationDriveAccessToken: String? = null
    private val readingAnnotationExportFormatter = ReadingAnnotationExportFormatter()

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
            readingProgress = readingProgressRepository.readingProgress(),
            readingAnnotations = readingAnnotationRepository.readingAnnotations(),
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
                historyCompletedContentIds = completedIds.trackedCompletedContentIds()
                updateCompletedContentIds()
            }
        }
        viewModelScope.launch {
            historyRepository.observeReady().collect { ready ->
                historyReady = ready
                updateHydrationState()
            }
        }
        viewModelScope.launch {
            readingProgressRepository.observeReadingProgress().collect { progress ->
                readingProgressCompletedContentIds = progress.filter(ReadingProgress::isCompleted)
                    .mapTo(mutableSetOf(), ReadingProgress::contentId)
                    .trackedCompletedContentIds()
                val unfinishedIds = progress.unfinishedContentIds()
                uiState = uiState.copy(
                    readingProgress = progress,
                    preferences = uiState.preferences?.copy(unfinishedContentIds = unfinishedIds),
                    currentReadingProgress = uiState.currentContent?.id?.let { contentId ->
                        progress.firstOrNull { candidate -> candidate.contentId == contentId && candidate.isUnfinished() }
                    },
                )
                updateCompletedContentIds()
            }
        }
        viewModelScope.launch {
            readingProgressRepository.observeCompletedContentIds().collect { completedIds ->
                readingProgressCompletedContentIds = completedIds.trackedCompletedContentIds()
                updateCompletedContentIds()
            }
        }
        viewModelScope.launch {
            readingProgressRepository.observeReady().collect { ready ->
                readingProgressReady = ready
                updateHydrationState()
            }
        }
        viewModelScope.launch {
            readingAnnotationRepository.observeReadingAnnotations().collect { annotations ->
                uiState = uiState.copy(readingAnnotations = annotations)
            }
        }
        viewModelScope.launch {
            readingAnnotationRepository.observeReady().collect { ready ->
                readingAnnotationReady = ready
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

    fun setMeditationDurationMinutes(minutes: Int) {
        val preferences = uiState.preferences
        val meditation = meditationTimerContentItem(minutes)
        val isCurrentMeditation = uiState.currentContent?.usesMeditationTimer() == true
        val activeSessionId = uiState.currentSessionId
        uiState = uiState.copy(
            meditationDurationMinutes = meditation.durationMinutes,
            preferences = preferences?.copy(meditationDurationMinutes = meditation.durationMinutes),
            currentRecommendationSet = uiState.currentRecommendationSet?.withMeditationDuration(meditation),
            currentContent = uiState.currentContent?.replaceIfMeditation(meditation),
            currentSessionStartedAtMillis = if (isCurrentMeditation) {
                nowProvider()
            } else {
                uiState.currentSessionStartedAtMillis
            },
            latestMessage = null,
        )
        viewModelScope.launch {
            settingsRepository.saveMeditationDurationMinutes(meditation.durationMinutes)
            if (isCurrentMeditation && activeSessionId != null) {
                historyRepository.updateAcceptedSessionContent(sessionId = activeSessionId, content = meditation)
            }
        }
    }

    fun setContentPriority(priority: ContentPriority) {
        val preferences = uiState.preferences
        uiState = uiState.copy(
            contentPriority = priority,
            preferences = preferences?.copy(contentPriority = priority),
            latestMessage = null,
        )
        viewModelScope.launch {
            settingsRepository.saveContentPriority(priority)
        }
    }

    fun togglePriorityContent(item: ContentItem) {
        val selectedIds = uiState.priorityContentIds.toMutableSet()
        val isPrioritized = if (selectedIds.add(item.id)) {
            true
        } else {
            selectedIds.remove(item.id)
            false
        }
        val updatedIds = selectedIds.toSet()
        val preferences = uiState.preferences
        uiState = uiState.copy(
            priorityContentIds = updatedIds,
            preferences = preferences?.copy(priorityContentIds = updatedIds),
            latestMessage = if (isPrioritized) {
                "Prioritizing ${item.title}."
            } else {
                "Removed ${item.title} from priority picks."
            },
        )
        viewModelScope.launch {
            settingsRepository.savePriorityContentIds(updatedIds)
            recordEventDurably(
                AnalyticsEvent(
                    type = AnalyticsEventType.PRIORITY_CONTENT_TOGGLED,
                    timestampMillis = nowProvider(),
                    contentId = item.id,
                    metadata = item.analyticsMetadata() + mapOf(
                        "priorityEnabled" to isPrioritized.toString(),
                        "priorityContentCount" to updatedIds.size.toString(),
                    ),
                ),
            )
        }
    }

    fun toggleCompletedContentActivation(item: ContentItem) {
        if (item.id !in uiState.completedContentIds) {
            uiState = uiState.copy(latestMessage = "${item.title} is not completed yet.")
            return
        }
        val activeIds = uiState.reactivatedCompletedContentIds.toMutableSet()
        val isReactivated = if (activeIds.add(item.id)) {
            true
        } else {
            activeIds.remove(item.id)
            false
        }
        val updatedIds = activeIds.toSet()
        val activeDelaySuggestion = activeDelaySuggestionFor(
            delayWindow = uiState.activeDelayWindow,
            excludedContentIds = uiState.completedContentIds - updatedIds,
        )
        uiState = uiState.copy(
            reactivatedCompletedContentIds = updatedIds,
            activeDelaySuggestion = activeDelaySuggestion,
            latestMessage = if (isReactivated) {
                "${item.title} can appear in suggestions again."
            } else {
                "${item.title} is hidden from suggestions again."
            },
        )
        viewModelScope.launch {
            settingsRepository.saveReactivatedCompletedContentIds(updatedIds)
            recordEventDurably(
                AnalyticsEvent(
                    type = AnalyticsEventType.COMPLETED_CONTENT_ACTIVATION_TOGGLED,
                    timestampMillis = nowProvider(),
                    contentId = item.id,
                    metadata = item.analyticsMetadata() + mapOf(
                        "reactivated" to isReactivated.toString(),
                        "reactivatedCompletedContentCount" to updatedIds.size.toString(),
                    ),
                ),
            )
        }
    }

    fun setOpenAnywayUnlockMinutes(minutes: Int) {
        val safeMinutes = minutes.coerceIn(MIN_OPEN_ANYWAY_UNLOCK_MINUTES, MAX_OPEN_ANYWAY_UNLOCK_MINUTES)
        uiState = uiState.copy(
            openAnywayUnlockMinutes = safeMinutes,
            latestMessage = null,
        )
        viewModelScope.launch {
            settingsRepository.saveOpenAnywayUnlockMinutes(safeMinutes)
        }
    }

    fun configureReadingAnnotationExport(
        uri: String,
        displayName: String,
        persistWritePermission: (String) -> Unit = {},
        nowMillis: Long = nowProvider(),
    ) {
        val normalizedUri = uri.trim()
        if (normalizedUri.isBlank()) {
            uiState = uiState.copy(latestMessage = "Choose a file for annotation autosave.")
            return
        }
        val normalizedDisplayName = displayName.trim().ifBlank { "quality-alternative-annotations.jsonld" }
        viewModelScope.launch {
            val permissionError = runCatching { persistWritePermission(normalizedUri) }.exceptionOrNull()
            settingsRepository.saveAnnotationExportDestination(
                uri = normalizedUri,
                displayName = normalizedDisplayName,
            )
            if (permissionError != null) {
                val message = permissionError.annotationExportErrorMessage()
                settingsRepository.saveAnnotationExportFailure(message)
                uiState = uiState.copy(
                    annotationExportUri = normalizedUri,
                    annotationExportDisplayName = normalizedDisplayName,
                    annotationExportLastSuccessfulAtMillis = null,
                    annotationExportLastError = message,
                    latestMessage = "Annotation autosave needs file permission.",
                )
                return@launch
            }
            val exported = exportReadingAnnotationsTo(
                uri = normalizedUri,
                nowMillis = nowMillis,
            )
            uiState = uiState.copy(
                annotationExportUri = normalizedUri,
                annotationExportDisplayName = normalizedDisplayName,
                annotationExportLastSuccessfulAtMillis = if (exported) nowMillis else null,
                latestMessage = if (exported) {
                    "Annotation autosave enabled."
                } else {
                    "Annotation autosave enabled, but the first write failed."
                },
            )
        }
    }

    fun clearReadingAnnotationExport(releaseWritePermission: (String) -> Unit = {}) {
        val uri = uiState.annotationExportUri
        viewModelScope.launch {
            uri?.takeIf(String::isNotBlank)?.let { configuredUri ->
                runCatching { releaseWritePermission(configuredUri) }
            }
            settingsRepository.clearAnnotationExportDestination()
            uiState = uiState.copy(
                annotationExportUri = null,
                annotationExportDisplayName = null,
                annotationExportLastSuccessfulAtMillis = null,
                annotationExportLastError = null,
                latestMessage = "Annotation autosave disabled.",
            )
        }
    }

    fun retryReadingAnnotationExport(nowMillis: Long = nowProvider()) {
        val uri = uiState.annotationExportUri
        if (uri.isNullOrBlank()) {
            uiState = uiState.copy(latestMessage = "Choose a file for annotation autosave.")
            return
        }
        viewModelScope.launch {
            val exported = exportReadingAnnotationsTo(uri = uri, nowMillis = nowMillis)
            uiState = uiState.copy(
                latestMessage = if (exported) {
                    "Annotations autosaved."
                } else {
                    "Annotation autosave failed."
                },
            )
        }
    }

    fun beginAnnotationDriveAuthorization() {
        uiState = uiState.copy(
            isAnnotationDriveSyncing = true,
            annotationDriveLastError = null,
            latestMessage = null,
        )
    }

    fun connectAnnotationDriveSync(accessToken: String, nowMillis: Long = nowProvider()) {
        val normalizedToken = accessToken.trim()
        if (normalizedToken.isBlank()) {
            reportAnnotationDriveAuthorizationFailure("Google Drive did not return an access token.")
            return
        }
        annotationDriveAccessToken = normalizedToken
        uiState = uiState.copy(isAnnotationDriveSyncing = true, latestMessage = null)
        viewModelScope.launch {
            settingsRepository.saveAnnotationDriveSyncConnection(folderId = uiState.annotationDriveFolderId)
            val synced = syncReadingAnnotationsToDrive(accessToken = normalizedToken, nowMillis = nowMillis)
            uiState = uiState.copy(
                isAnnotationDriveSyncing = false,
                annotationDriveSyncEnabled = true,
                latestMessage = if (synced) {
                    "Google Drive sync enabled."
                } else {
                    "Google Drive connected, but sync failed."
                },
            )
            recordEventDurably(
                AnalyticsEvent(
                    type = AnalyticsEventType.ANNOTATION_DRIVE_SYNC_CONNECTED,
                    timestampMillis = nowMillis,
                    metadata = mapOf("synced" to synced.toString()),
                ),
            )
        }
    }

    fun retryAnnotationDriveSync(accessToken: String, nowMillis: Long = nowProvider()) {
        val normalizedToken = accessToken.trim()
        if (normalizedToken.isBlank()) {
            reportAnnotationDriveAuthorizationFailure("Google Drive did not return an access token.")
            return
        }
        annotationDriveAccessToken = normalizedToken
        uiState = uiState.copy(isAnnotationDriveSyncing = true, latestMessage = null)
        viewModelScope.launch {
            val synced = syncReadingAnnotationsToDrive(accessToken = normalizedToken, nowMillis = nowMillis)
            uiState = uiState.copy(
                isAnnotationDriveSyncing = false,
                latestMessage = if (synced) {
                    "Annotations synced to Google Drive."
                } else {
                    "Google Drive sync failed."
                },
            )
        }
    }

    fun reportAnnotationDriveAuthorizationFailure(errorMessage: String) {
        val message = errorMessage.trim().ifBlank { "Google Drive authorization failed." }
        uiState = uiState.copy(
            isAnnotationDriveSyncing = false,
            annotationDriveLastError = message,
            latestMessage = "Google Drive connection failed.",
        )
        viewModelScope.launch {
            settingsRepository.saveAnnotationDriveSyncFailure(message)
        }
    }

    fun disconnectAnnotationDriveSync(nowMillis: Long = nowProvider()) {
        annotationDriveAccessToken = null
        uiState = uiState.copy(
            annotationDriveSyncEnabled = false,
            annotationDriveFolderId = null,
            annotationDriveLastSuccessfulAtMillis = null,
            annotationDriveLastError = null,
            isAnnotationDriveSyncing = false,
            latestMessage = "Google Drive sync disconnected.",
        )
        viewModelScope.launch {
            settingsRepository.clearAnnotationDriveSyncConnection()
            recordEventDurably(
                AnalyticsEvent(
                    type = AnalyticsEventType.ANNOTATION_DRIVE_SYNC_DISCONNECTED,
                    timestampMillis = nowMillis,
                ),
            )
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
        openInputStream: () -> InputStream? = { null },
    ) {
        val candidate = DocumentImportCandidateFactory.fromPickedDocument(
            uri = uri,
            displayName = displayName,
            mimeType = mimeType,
            openInputStream = openInputStream,
        )
        updateAddDocumentForm(
            form = AddDocumentFormState(
                uri = candidate.uri,
                displayName = candidate.displayName,
                mimeType = mimeType,
                title = candidate.title,
                durationMinutes = candidate.durationMinutes,
                candidates = listOf(candidate),
            ),
            screen = MainScreen.AddDocument,
        )
    }

    fun prepareUserDocumentBatchImport(
        candidates: List<DocumentImportCandidate>,
        nowMillis: Long = nowProvider(),
    ) {
        val cleanedCandidates = candidates
            .map(DocumentImportCandidate::cleaned)
            .distinctBy(DocumentImportCandidate::uri)
        if (cleanedCandidates.isEmpty()) {
            uiState = uiState.copy(latestMessage = "Choose local PDF, Markdown, or EPUB files first.")
            return
        }

        val primary = cleanedCandidates.first()
        updateAddDocumentForm(
            form = AddDocumentFormState(
                uri = primary.uri,
                displayName = primary.displayName,
                mimeType = primary.mimeType,
                title = primary.title,
                durationMinutes = primary.durationMinutes,
                candidates = cleanedCandidates,
            ),
            screen = MainScreen.AddDocument,
        )
        viewModelScope.launch {
            recordEventDurably(
                AnalyticsEvent(
                    type = AnalyticsEventType.BATCH_DOCUMENT_IMPORT_ATTEMPTED,
                    timestampMillis = nowMillis,
                    metadata = mapOf(
                        "selectedCount" to cleanedCandidates.size.toString(),
                        "supportedCount" to cleanedCandidates.count { it.format != null }.toString(),
                        "unsupportedCount" to cleanedCandidates.count { it.format == null }.toString(),
                        "formats" to cleanedCandidates.map { it.format?.name ?: "UNSUPPORTED" }.sorted().joinToString(","),
                    ),
                ),
            )
        }
    }

    fun openHome() {
        uiState = uiState.copy(
            screen = MainScreen.Home,
            isManagingLibrary = false,
            selectedLibraryContentIds = emptySet(),
            currentReaderStartParagraphIndex = null,
            latestMessage = null,
        )
    }

    fun openLibrary() {
        uiState = uiState.copy(
            screen = MainScreen.Library,
            currentReaderStartParagraphIndex = null,
            latestMessage = null,
        )
    }

    fun toggleLibraryManageMode() {
        val willManage = !uiState.isManagingLibrary
        uiState = uiState.copy(
            isManagingLibrary = willManage,
            selectedLibraryContentIds = if (willManage) uiState.selectedLibraryContentIds else emptySet(),
            latestMessage = null,
        )
    }

    fun toggleLibraryContentSelection(content: ContentItem) {
        if (!uiState.isManagingLibrary) {
            return
        }
        if (!content.isUserManagedContent()) {
            uiState = uiState.copy(latestMessage = "Editorial pieces stay in the starter packs.")
            return
        }

        val selectedIds = uiState.selectedLibraryContentIds.toMutableSet()
        if (!selectedIds.add(content.id)) {
            selectedIds.remove(content.id)
        }
        uiState = uiState.copy(
            selectedLibraryContentIds = selectedIds,
            latestMessage = null,
        )
    }

    fun deleteSelectedLibraryContent(
        nowMillis: Long = nowProvider(),
        releaseDocumentPermission: (String) -> Unit = {},
    ) {
        val userContent = (uiState.userLinks + uiState.userDocuments).associateBy(ContentItem::id)
        val selectedItems = uiState.selectedLibraryContentIds.mapNotNull(userContent::get)
        if (selectedItems.isEmpty()) {
            uiState = uiState.copy(latestMessage = "Select saved links or files to delete.")
            return
        }

        uiState = uiState.copy(latestMessage = null)
        viewModelScope.launch {
            try {
                val deletedIds = selectedItems.mapTo(mutableSetOf(), ContentItem::id)
                selectedItems.filter { item -> item.sourceType == ContentSourceType.USER_LINK }
                    .forEach { item -> userLinkRepository.deleteLink(item.id) }
                selectedItems.filter { item -> item.sourceType == ContentSourceType.USER_DOCUMENT }
                    .forEach { item ->
                        userDocumentRepository.deleteDocument(item.id)
                        item.rights.sourceUrl?.takeIf(String::isNotBlank)?.let { uri ->
                            runCatching { releaseDocumentPermission(uri) }
                        }
                }
                readingProgressRepository.deleteProgressForContentIds(deletedIds)
                readingAnnotationRepository.deleteAnnotationsForContentIds(deletedIds, nowMillis = nowMillis)
                val annotationAutosaveResult = autosaveReadingAnnotations(nowMillis = nowMillis)

                val updatedPriorityIds = uiState.priorityContentIds - deletedIds
                if (updatedPriorityIds != uiState.priorityContentIds) {
                    settingsRepository.savePriorityContentIds(updatedPriorityIds)
                }
                selectedItems.forEach { item ->
                    recordEventDurably(
                        AnalyticsEvent(
                            type = AnalyticsEventType.USER_CONTENT_DELETED,
                            timestampMillis = nowMillis,
                            contentId = item.id,
                            metadata = item.analyticsMetadata() + mapOf(
                                "deletedSelectionCount" to selectedItems.size.toString(),
                            ),
                        ),
                    )
                }
                val preferences = uiState.preferences
                uiState = uiState.copy(
                    priorityContentIds = updatedPriorityIds,
                    preferences = preferences?.copy(priorityContentIds = updatedPriorityIds),
                    selectedLibraryContentIds = emptySet(),
                    isManagingLibrary = false,
                    latestMessage = deletedLibraryMessage(selectedItems.size)
                        .withAnnotationAutosaveResult(annotationAutosaveResult),
                )
            } catch (error: Throwable) {
                if (error is CancellationException) throw error
                uiState = uiState.copy(latestMessage = "The selected content could not be deleted.")
            }
        }
    }

    fun openProgress() {
        uiState = uiState.copy(
            screen = MainScreen.Progress,
            currentReaderStartParagraphIndex = null,
            latestMessage = null,
        )
    }

    fun openSettings() {
        uiState = uiState.copy(
            screen = MainScreen.Settings,
            currentReaderStartParagraphIndex = null,
            latestMessage = null,
        )
    }

    fun openAnnotationLibrary() {
        uiState = uiState.copy(
            screen = MainScreen.Annotations,
            isManagingLibrary = false,
            selectedLibraryContentIds = emptySet(),
            currentReaderStartParagraphIndex = null,
            latestMessage = null,
        )
    }

    fun openAnnotationTarget(annotationId: String) {
        val annotation = uiState.readingAnnotations.firstOrNull { candidate -> candidate.id == annotationId }
        if (annotation == null) {
            uiState = uiState.copy(latestMessage = "That annotation is no longer available.")
            return
        }
        val content = contentForAnnotation(annotation)
        if (content == null) {
            uiState = uiState.copy(latestMessage = "The source for that annotation is no longer in Library.")
            return
        }
        if (!content.usesRepositoryBody()) {
            uiState = uiState.copy(latestMessage = "That source cannot be opened in the reader.")
            return
        }
        openLibraryItem(
            content = content,
            origin = "annotation-library",
            startParagraphIndex = annotation.paragraphIndex,
        )
    }

    fun openLibraryItem(
        content: ContentItem,
        origin: String = "library",
        startParagraphIndex: Int? = null,
    ) {
        val startedAtMillis = nowProvider()
        val readerDocument = if (content.usesRepositoryBody()) {
            try {
                contentRepository.readerDocument(content)
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
            ReaderDocument.fromPlainText("")
        }
        val existingProgress = unfinishedProgressFor(content.id)
        if (existingProgress != null) {
            recordEvent(
                AnalyticsEvent(
                    type = AnalyticsEventType.MANUAL_CONTINUE_STARTED,
                    timestampMillis = startedAtMillis,
                    contentId = content.id,
                    metadata = content.analyticsMetadata() + existingProgress.analyticsMetadata() + mapOf(
                        "origin" to origin,
                    ),
                ),
            )
        }
        uiState = uiState.copy(
            currentInterventionId = null,
            currentInterventionShownAtMillis = null,
            currentRecommendationSet = null,
            currentInterventionOrigin = null,
            currentContent = content,
            currentReaderDocument = readerDocument,
            currentContentBody = readerDocument.plainText,
            currentReadingProgress = existingProgress,
            currentReaderStartParagraphIndex = startParagraphIndex,
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

    fun toggleAddLinkPriority() {
        updateAddLinkForm(uiState.addLinkForm.copy(markPriority = !uiState.addLinkForm.markPriority))
    }

    fun saveUserLink(nowMillis: Long = nowProvider()) {
        val form = uiState.addLinkForm
        val draft = form.toDraftOrNull()
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
                        val updatedPriorityIds = if (form.markPriority) {
                            val ids = uiState.priorityContentIds + result.item.id
                            settingsRepository.savePriorityContentIds(ids)
                            recordPrioritySetDuringAdd(
                                item = result.item,
                                nowMillis = nowMillis,
                                priorityContentIds = ids,
                            )
                            ids
                        } else {
                            uiState.priorityContentIds
                        }
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
                            priorityContentIds = updatedPriorityIds,
                            preferences = uiState.preferences?.copy(priorityContentIds = updatedPriorityIds),
                            savedLinkConfirmation = result.item.toAddLinkConfirmation(priorityMarked = form.markPriority),
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

    fun toggleAddDocumentTopic(topic: TopicTag) {
        val selectedTopics = uiState.addDocumentForm.selectedTopics.toMutableSet()
        if (!selectedTopics.add(topic)) {
            selectedTopics.remove(topic)
        }
        updateAddDocumentForm(uiState.addDocumentForm.copy(selectedTopics = selectedTopics))
    }

    fun toggleAddDocumentPriority() {
        updateAddDocumentForm(uiState.addDocumentForm.copy(markPriority = !uiState.addDocumentForm.markPriority))
    }

    fun saveUserDocument(
        nowMillis: Long = nowProvider(),
        persistReadPermission: (String) -> Unit = {},
    ) {
        val form = uiState.addDocumentForm
        val candidates = form.documentCandidates()
        val supportedCandidates = candidates.filter { candidate -> candidate.format != null }
        val drafts = supportedCandidates.mapNotNull { candidate -> candidate.toDraftOrNull(form.selectedTopics) }
        if (drafts.isEmpty()) {
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
                val addedItems = mutableListOf<ContentItem>()
                var rejectedCount = candidates.count { candidate -> candidate.format == null }
                drafts.forEach { draft ->
                    when (val result = userDocumentRepository.addDocument(draft = draft, nowMillis = nowMillis)) {
                        is AddUserDocumentResult.Added -> {
                            addedItems += result.item
                            runCatching { persistReadPermission(draft.uri) }
                            recordReadingTimeEstimateApplied(
                                item = result.item,
                                draft = draft,
                                candidate = candidates.firstOrNull { candidate -> candidate.uri == draft.uri },
                                nowMillis = nowMillis,
                            )
                            recordEventDurably(
                                AnalyticsEvent(
                                    type = AnalyticsEventType.USER_DOCUMENT_ADDED,
                                    timestampMillis = nowMillis,
                                    contentId = result.item.id,
                                    metadata = result.item.analyticsMetadata() + mapOf(
                                        "durationMinutes" to result.item.durationMinutes.toString(),
                                        "topicCount" to result.item.topicTags.size.toString(),
                                        "batchSelectedCount" to candidates.size.toString(),
                                    ),
                                ),
                            )
                        }

                        is AddUserDocumentResult.Rejected -> {
                            rejectedCount += 1
                        }
                    }
                }

                if (addedItems.isEmpty()) {
                    uiState = uiState.copy(
                        screen = MainScreen.AddDocument,
                        addDocumentForm = uiState.addDocumentForm.copy(
                            validationErrors = setOf(UserDocumentValidationError.UNSUPPORTED_FORMAT),
                            canSave = false,
                            isSaving = false,
                        ),
                        latestMessage = "No selected files could be saved locally.",
                    )
                    return@launch
                }

                val updatedPriorityIds = if (form.markPriority) {
                    val ids = uiState.priorityContentIds + addedItems.map(ContentItem::id)
                    settingsRepository.savePriorityContentIds(ids)
                    addedItems.forEach { item ->
                        recordPrioritySetDuringAdd(
                            item = item,
                            nowMillis = nowMillis,
                            priorityContentIds = ids,
                        )
                    }
                    ids
                } else {
                    uiState.priorityContentIds
                }

                recordEventDurably(
                    AnalyticsEvent(
                        type = AnalyticsEventType.BATCH_DOCUMENT_IMPORT_COMPLETED,
                        timestampMillis = nowMillis,
                        metadata = mapOf(
                            "selectedCount" to candidates.size.toString(),
                            "savedCount" to addedItems.size.toString(),
                            "rejectedCount" to rejectedCount.toString(),
                            "priorityMarked" to form.markPriority.toString(),
                        ),
                    ),
                )

                uiState = uiState.copy(
                    screen = MainScreen.AddLinkSuccess,
                    addDocumentForm = AddDocumentFormState(),
                    priorityContentIds = updatedPriorityIds,
                    preferences = uiState.preferences?.copy(priorityContentIds = updatedPriorityIds),
                    savedLinkConfirmation = addedItems.toAddLinkConfirmation(
                        skippedCount = rejectedCount,
                        priorityMarked = form.markPriority,
                    ),
                    latestMessage = null,
                )
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

            if (
                origin == InterventionOrigin.SYSTEM &&
                InterceptionRuntimeGate.shouldSuppress(targetApp.packageName, processingNowMillis)
            ) {
                uiState = uiState.copy(
                    selectedTargetApp = targetApp,
                    currentInterventionId = null,
                    currentInterventionShownAtMillis = null,
                    currentRecommendationSet = null,
                    activeDelayWindow = null,
                    activeDelaySuggestion = null,
                    currentInterventionOrigin = null,
                    latestMessage = "${targetApp.displayName} is still unlocked.",
                    screen = MainScreen.Home,
                )
                return@launch
            }

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
                excludedContentIds = excludedContentIdsForRecommendation(),
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
            unfinishedProgressFor(recommendationSet.primary.id)?.let { progress ->
                recordEvent(
                    AnalyticsEvent(
                        type = AnalyticsEventType.UNFINISHED_CONTENT_RECOMMENDED_AS_PRIMARY,
                        timestampMillis = processingNowMillis,
                        interventionId = interventionId,
                        targetAppPackage = targetApp.packageName,
                        primaryContentId = recommendationSet.primary.id,
                        backupContentIds = backupIds,
                        contentId = recommendationSet.primary.id,
                        metadata = recommendationSet.primary.analyticsMetadata() + progress.analyticsMetadata() + mapOf(
                            "unfinishedContentCount" to preferences.unfinishedContentIds.size.toString(),
                        ),
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
        val nowMillis = nowProvider()
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
        val nowMillis = nowProvider()
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
                activeDelaySuggestion = activeDelaySuggestionFor(window),
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
        val nowMillis = nowProvider()
        val shouldExitToTarget = uiState.currentInterventionOrigin == InterventionOrigin.SYSTEM
        val unlockMinutes = uiState.openAnywayUnlockMinutes
            .coerceIn(MIN_OPEN_ANYWAY_UNLOCK_MINUTES, MAX_OPEN_ANYWAY_UNLOCK_MINUTES)
        val unlockUntilMillis = nowMillis + unlockMinutes * 60_000L
        recordEvent(
            AnalyticsEvent(
                type = AnalyticsEventType.OPEN_ANYWAY_SELECTED,
                timestampMillis = nowMillis,
                interventionId = uiState.currentInterventionId,
                targetAppPackage = targetApp.packageName,
                primaryContentId = recommendationSet?.primary?.id,
                backupContentIds = recommendationSet?.backups.orEmpty().map(ContentItem::id),
                metadata = mapOf(
                    "openAnywayUnlockMinutes" to unlockMinutes.toString(),
                    "openAnywayUnlockUntilMillis" to unlockUntilMillis.toString(),
                ),
            ),
        )
        if (shouldExitToTarget) {
            InterceptionRuntimeGate.suppressPackage(
                targetAppPackage = targetApp.packageName,
                untilMillis = unlockUntilMillis,
            )
        }
        uiState = uiState.copy(
            screen = MainScreen.Home,
            currentInterventionId = null,
            currentInterventionShownAtMillis = null,
            currentRecommendationSet = null,
            currentInterventionOrigin = null,
            activeDelaySuggestion = null,
            latestMessage = "${targetApp.displayName} unlocked for $unlockMinutes minutes.",
        )
        return shouldExitToTarget
    }

    fun saveCurrentReadingProgress(
        progressPercent: Int,
        lastVisibleParagraphIndex: Int,
        paragraphCount: Int,
        nowMillis: Long = nowProvider(),
    ) {
        val content = uiState.currentContent ?: return
        if (!content.usesRepositoryBody()) {
            return
        }
        val safeParagraphCount = paragraphCount.coerceAtLeast(1)
        val progress = ReadingProgress(
            contentId = content.id,
            progressPercent = progressPercent.coerceIn(1, 99),
            lastVisibleParagraphIndex = lastVisibleParagraphIndex.coerceIn(0, safeParagraphCount - 1),
            paragraphCount = safeParagraphCount,
            updatedAtMillis = nowMillis,
        )
        if (uiState.currentReadingProgress?.sameVisiblePosition(progress) == true) {
            return
        }
        uiState = uiState.copy(currentReadingProgress = progress)
        viewModelScope.launch {
            readingProgressRepository.saveProgress(progress)
            recordEventDurably(
                AnalyticsEvent(
                    type = AnalyticsEventType.READING_PROGRESS_SAVED,
                    timestampMillis = nowMillis,
                    interventionId = uiState.currentInterventionId,
                    sessionId = uiState.currentSessionId,
                    targetAppPackage = uiState.selectedTargetApp?.packageName,
                    primaryContentId = uiState.currentRecommendationSet?.primary?.id,
                    backupContentIds = uiState.currentRecommendationSet?.backups.orEmpty().map(ContentItem::id),
                    contentId = content.id,
                    metadata = content.analyticsMetadata() + progress.analyticsMetadata(),
                ),
            )
        }
    }

    fun saveCurrentReadingAnnotation(
        paragraphIndex: Int,
        quotedText: String,
        noteText: String,
        existingAnnotationId: String? = null,
        selector: ReadingAnnotationSelector = ReadingAnnotationSelector(),
        nowMillis: Long = nowProvider(),
    ) {
        val content = uiState.currentContent ?: return
        if (!content.usesRepositoryBody()) {
            return
        }
        val normalizedQuote = quotedText.trim().take(MAX_READING_ANNOTATION_QUOTE_LENGTH)
        val normalizedNote = noteText.trim()
        if (normalizedQuote.isBlank() || normalizedNote.isBlank()) {
            uiState = uiState.copy(latestMessage = "Write a note before saving.")
            return
        }
        viewModelScope.launch {
            readingAnnotationRepository.saveAnnotation(
	                draft = ReadingAnnotationDraft(
	                    id = existingAnnotationId,
	                    contentId = content.id,
	                    paragraphIndex = paragraphIndex,
	                    quotedText = normalizedQuote,
	                    noteText = normalizedNote,
	                    sourceTitle = content.title,
	                    sourceLabel = content.sourceLabel,
	                    sourceType = content.sourceType,
	                    sourceFormat = content.format,
	                    selector = selector,
	                ),
                nowMillis = nowMillis,
            )
            val annotationAutosaveResult = autosaveReadingAnnotations(nowMillis = nowMillis)
            val savedMessage = if (existingAnnotationId == null) {
                "Annotation saved."
            } else {
                "Annotation updated."
            }
            uiState = uiState.copy(
                latestMessage = savedMessage.withAnnotationAutosaveResult(annotationAutosaveResult),
            )
        }
    }

    fun deleteReadingAnnotation(
        annotationId: String,
        nowMillis: Long = nowProvider(),
    ) {
        if (uiState.readingAnnotations.none { annotation -> annotation.id == annotationId }) {
            uiState = uiState.copy(latestMessage = "That annotation is no longer available.")
            return
        }
        viewModelScope.launch {
            readingAnnotationRepository.deleteAnnotation(
                annotationId = annotationId,
                nowMillis = nowMillis,
            )
            val annotationAutosaveResult = autosaveReadingAnnotations(nowMillis = nowMillis)
            uiState = uiState.copy(
                latestMessage = "Annotation deleted.".withAnnotationAutosaveResult(annotationAutosaveResult),
            )
        }
    }

    fun finishReading() {
        val content = uiState.currentContent ?: return
        val sessionId = uiState.currentSessionId
        val nowMillis = nowProvider()
        viewModelScope.launch {
            val completedProgress = completedProgressFor(content = content, completedAtMillis = nowMillis)
            readingProgressRepository.saveProgress(completedProgress)
            if (sessionId != null) {
                historyRepository.markCompleted(sessionId = sessionId, completedAtMillis = nowMillis)
            }
            deactivateCompletedContentOverride(contentId = content.id)
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
                    metadata = sessionDurationMetadata(nowMillis) + content.analyticsMetadata() + completedProgress.analyticsMetadata(),
                ),
            )
            uiState = uiState.copy(
                screen = MainScreen.Feedback,
                currentReadingProgress = completedProgress,
            )
        }
    }

    fun finishMeditationReset(nowMillis: Long = System.currentTimeMillis()) {
        val content = uiState.currentContent ?: return
        val sessionId = uiState.currentSessionId
        viewModelScope.launch {
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
        val reactivatedCompletedContentIds = settings.reactivatedCompletedContentIds.trackedCompletedContentIds()
        val preferences = settings.toUserPreferences(
            supportedApps = supportedApps,
            fallbackPackIds = defaultSelectedPackIds,
        ).withReadingProgress(uiState.readingProgress)
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
            meditationDurationMinutes = settings.meditationDurationMinutes,
            contentPriority = settings.contentPriority,
            priorityContentIds = settings.priorityContentIds,
            reactivatedCompletedContentIds = reactivatedCompletedContentIds,
            openAnywayUnlockMinutes = settings.openAnywayUnlockMinutes,
            annotationExportUri = settings.annotationExportUri,
            annotationExportDisplayName = settings.annotationExportDisplayName,
            annotationExportLastSuccessfulAtMillis = settings.annotationExportLastSuccessfulAtMillis,
            annotationExportLastError = settings.annotationExportLastError,
            annotationDriveSyncEnabled = settings.annotationDriveSyncEnabled,
            annotationDriveFolderId = settings.annotationDriveFolderId,
            annotationDriveLastSuccessfulAtMillis = settings.annotationDriveLastSuccessfulAtMillis,
            annotationDriveLastError = settings.annotationDriveLastError,
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
                uiState.screen == MainScreen.Annotations -> MainScreen.Annotations
                uiState.screen == MainScreen.Progress -> MainScreen.Progress
                uiState.screen == MainScreen.Settings -> MainScreen.Settings
                else -> uiState.screen
            },
        )
        if (reactivatedCompletedContentIds != settings.reactivatedCompletedContentIds) {
            viewModelScope.launch {
                settingsRepository.saveReactivatedCompletedContentIds(reactivatedCompletedContentIds)
            }
        }
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
            completedTopics = history.filter(ReplacementHistoryEntry::isCompletedReadingReplacement)
                .flatMapTo(mutableSetOf(), ReplacementHistoryEntry::contentTopics),
            skippedTopics = history.filter(ReplacementHistoryEntry::isSkipped)
                .flatMapTo(mutableSetOf(), ReplacementHistoryEntry::contentTopics),
            successfulPackIds = history.filter { entry ->
                entry.isCompletedReadingReplacement() && entry.feedbackHelpedAvoidScrolling != false
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
        val readerDocument = if (content.usesRepositoryBody()) {
            try {
                contentRepository.readerDocument(content)
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
            ReaderDocument.fromPlainText("")
        }
        val existingProgress = unfinishedProgressFor(content.id)
        uiState = uiState.copy(
            currentContent = content,
            currentReaderDocument = readerDocument,
            currentContentBody = readerDocument.plainText,
            currentReadingProgress = existingProgress,
            currentReaderStartParagraphIndex = null,
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
        return contentRepository.inventory() + meditationTimerContentItem(uiState.meditationDurationMinutes)
    }

    private fun contentForAnnotation(annotation: ReadingAnnotation): ContentItem? {
        return fullReplacementInventory()
            .firstOrNull { content -> content.id == annotation.contentId }
    }

    private suspend fun autosaveReadingAnnotations(nowMillis: Long): AnnotationAutosaveResult {
        return AnnotationAutosaveResult(
            fileResult = exportReadingAnnotationsIfConfigured(nowMillis = nowMillis),
            driveResult = syncReadingAnnotationsToDriveIfConnected(nowMillis = nowMillis),
        )
    }

    private suspend fun exportReadingAnnotationsIfConfigured(nowMillis: Long): Boolean? {
        val uri = uiState.annotationExportUri?.takeIf(String::isNotBlank) ?: return null
        return exportReadingAnnotationsTo(uri = uri, nowMillis = nowMillis)
    }

    private suspend fun exportReadingAnnotationsTo(uri: String, nowMillis: Long): Boolean {
        return try {
            val contentById = fullReplacementInventory()
                .distinctBy(ContentItem::id)
                .associateBy(ContentItem::id)
            val files = readingAnnotationExportFormatter.formatJsonLdFiles(
                annotations = readingAnnotationRepository.readingAnnotations(),
                contentById = contentById,
            )
            readingAnnotationExportWriter.writeJsonLdFiles(uri = uri, files = files)
            settingsRepository.saveAnnotationExportSuccess(timestampMillis = nowMillis)
            true
        } catch (error: Throwable) {
            if (error is CancellationException) throw error
            settingsRepository.saveAnnotationExportFailure(error.annotationExportErrorMessage())
            false
        }
    }

    private suspend fun syncReadingAnnotationsToDriveIfConnected(nowMillis: Long): Boolean? {
        if (!uiState.annotationDriveSyncEnabled) {
            return null
        }
        val cachedAccessToken = annotationDriveAccessToken?.takeIf(String::isNotBlank)
        val accessToken = runCatching { readingAnnotationDriveTokenProvider.driveAccessToken() }
            .onSuccess { token -> annotationDriveAccessToken = token }
            .getOrElse { cachedAccessToken }
        if (accessToken == null) {
            val message = "Open Settings and tap Save now to refresh Google Drive access."
            settingsRepository.saveAnnotationDriveSyncFailure(message)
            uiState = uiState.copy(annotationDriveLastError = message)
            return false
        }
        return syncReadingAnnotationsToDrive(accessToken = accessToken, nowMillis = nowMillis)
    }

    private suspend fun syncReadingAnnotationsToDrive(accessToken: String, nowMillis: Long): Boolean {
        return try {
            val contentById = fullReplacementInventory()
                .distinctBy(ContentItem::id)
                .associateBy(ContentItem::id)
            val files = readingAnnotationExportFormatter.formatJsonLdFiles(
                annotations = readingAnnotationRepository.readingAnnotations(),
                contentById = contentById,
            )
            val indexJson = readingAnnotationExportFormatter.formatIndexJson(files)
            val result = readingAnnotationDriveSyncClient.syncJsonLdFiles(
                ReadingAnnotationDriveSyncRequest(
                    accessToken = accessToken,
                    folderId = uiState.annotationDriveFolderId,
                    files = files,
                    indexJson = indexJson,
                ),
            )
            settingsRepository.saveAnnotationDriveSyncSuccess(
                timestampMillis = nowMillis,
                folderId = result.folderId,
            )
            uiState = uiState.copy(
                annotationDriveSyncEnabled = true,
                annotationDriveFolderId = result.folderId,
                annotationDriveLastSuccessfulAtMillis = nowMillis,
                annotationDriveLastError = null,
            )
            recordEventDurably(
                AnalyticsEvent(
                    type = AnalyticsEventType.ANNOTATION_DRIVE_SYNC_SUCCEEDED,
                    timestampMillis = nowMillis,
                    metadata = mapOf(
                        "syncedFileCount" to result.syncedFileNames.size.toString(),
                        "sourceFileCount" to files.size.toString(),
                    ),
                ),
            )
            true
        } catch (error: Throwable) {
            if (error is CancellationException) throw error
            val message = error.annotationDriveSyncErrorMessage()
            settingsRepository.saveAnnotationDriveSyncFailure(message)
            uiState = uiState.copy(
                annotationDriveSyncEnabled = true,
                annotationDriveLastError = message,
            )
            recordEventDurably(
                AnalyticsEvent(
                    type = AnalyticsEventType.ANNOTATION_DRIVE_SYNC_FAILED,
                    timestampMillis = nowMillis,
                    metadata = mapOf("reason" to message),
                ),
            )
            false
        }
    }

    private fun excludedContentIdsForRecommendation(): Set<String> {
        return uiState.completedContentIds -
            uiState.reactivatedCompletedContentIds
    }

    private suspend fun deactivateCompletedContentOverride(contentId: String) {
        if (contentId !in uiState.reactivatedCompletedContentIds) {
            return
        }
        val updatedIds = uiState.reactivatedCompletedContentIds - contentId
        uiState = uiState.copy(reactivatedCompletedContentIds = updatedIds)
        settingsRepository.saveReactivatedCompletedContentIds(updatedIds)
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
            currentReaderDocument = null,
            currentContentBody = "",
            currentRecommendationSet = null,
            currentInterventionOrigin = null,
            currentSessionId = null,
            currentSessionStartedAtMillis = null,
            currentReadingProgress = null,
            currentReaderStartParagraphIndex = null,
            lastFeedback = lastFeedback,
            latestMessage = latestMessage,
        )
    }

    private fun sessionDurationMetadata(nowMillis: Long): Map<String, String> {
        val startedAtMillis = uiState.currentSessionStartedAtMillis ?: return emptyMap()
        val durationSeconds = ((nowMillis - startedAtMillis) / 1000L).coerceAtLeast(0)
        return mapOf("sessionDurationSeconds" to durationSeconds.toString())
    }

    private suspend fun recordPrioritySetDuringAdd(
        item: ContentItem,
        nowMillis: Long,
        priorityContentIds: Set<String>,
    ) {
        recordEventDurably(
            AnalyticsEvent(
                type = AnalyticsEventType.PRIORITY_SET_DURING_ADD,
                timestampMillis = nowMillis,
                contentId = item.id,
                metadata = item.analyticsMetadata() + mapOf(
                    "priorityContentCount" to priorityContentIds.size.toString(),
                    "source" to "add_flow",
                ),
            ),
        )
    }

    private suspend fun recordReadingTimeEstimateApplied(
        item: ContentItem,
        draft: UserDocumentDraft,
        candidate: DocumentImportCandidate?,
        nowMillis: Long,
    ) {
        val source = candidate?.estimateSource ?: return
        recordEventDurably(
            AnalyticsEvent(
                type = AnalyticsEventType.READING_TIME_ESTIMATE_APPLIED,
                timestampMillis = nowMillis,
                contentId = item.id,
                metadata = item.analyticsMetadata() + mapOf(
                    "estimateSource" to source.name,
                    "durationMinutes" to draft.durationMinutes.toString(),
                    "displayName" to draft.displayName,
                    "wordCount" to candidate.estimatedWordCount.orZeroString(),
                ),
            ),
        )
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
        excludedContentIds: Set<String> = excludedContentIdsForRecommendation(),
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
                    item.id !in excludedContentIds &&
                    item.availability != ContentAvailability.UNAVAILABLE
            }
            .minWithOrNull(compareBy<ContentItem> { it.durationMinutes }.thenBy { it.title })
    }

    private fun recommendationSetForDelayWindow(
        delayWindow: DelayWindow,
        fallbackContent: ContentItem,
    ): RecommendationSet {
        val excludedContentIds = excludedContentIdsForRecommendation()
        val candidatesById = fullReplacementInventory()
            .filter { item ->
                item.id !in excludedContentIds &&
                    item.availability != ContentAvailability.UNAVAILABLE
            }
            .associateBy(ContentItem::id)
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
        val isReady = settingsLoaded && contentReady && analyticsReady && historyReady && readingProgressReady &&
            readingAnnotationReady && delayReady
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

    private fun updateCompletedContentIds() {
        val completedIds = historyCompletedContentIds + readingProgressCompletedContentIds
        if (uiState.completedContentIds != completedIds) {
            val activeDelaySuggestion = activeDelaySuggestionFor(
                delayWindow = uiState.activeDelayWindow,
                excludedContentIds = completedIds - uiState.reactivatedCompletedContentIds,
            )
            uiState = uiState.copy(
                completedContentIds = completedIds,
                activeDelaySuggestion = activeDelaySuggestion,
            )
        }
    }

    private fun unfinishedProgressFor(contentId: String): ReadingProgress? {
        return uiState.readingProgress.firstOrNull { progress ->
            progress.contentId == contentId && progress.isUnfinished()
        }
    }

    private fun completedProgressFor(
        content: ContentItem,
        completedAtMillis: Long,
    ): ReadingProgress {
        val current = uiState.currentReadingProgress
            ?.takeIf { progress -> progress.contentId == content.id }
        val paragraphCount = current?.paragraphCount ?: 1
        return ReadingProgress(
            contentId = content.id,
            progressPercent = 100,
            lastVisibleParagraphIndex = (current?.lastVisibleParagraphIndex ?: (paragraphCount - 1))
                .coerceIn(0, paragraphCount - 1),
            paragraphCount = paragraphCount,
            updatedAtMillis = completedAtMillis,
            completedAtMillis = completedAtMillis,
        )
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
            latestMessage = null,
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
            readingProgressRepository = appContainer.readingProgressRepository,
            readingAnnotationRepository = appContainer.readingAnnotationRepository,
            readingAnnotationExportWriter = appContainer.readingAnnotationExportWriter,
            readingAnnotationDriveSyncClient = appContainer.readingAnnotationDriveSyncClient,
            readingAnnotationDriveTokenProvider = appContainer.readingAnnotationDriveTokenProvider,
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
    SPRINT9_PACK_IDS
        .filterTo(selected) { packId -> starterPacks.any { pack -> pack.id == packId } }
    return selected
}

private fun AppSettings.toUserPreferences(
    supportedApps: List<DistractingApp>,
    fallbackPackIds: Set<String>,
): UserPreferences {
    val selectedApps = selectedAppPackages.mapNotNull(SupportedCatalog::findByPackage)
        .ifEmpty { supportedApps.take(3) }
    val selectedTopics = preferredTopics.ifEmpty { defaultPrototypeTopics() }
    val packs = selectedPackIds.ifEmpty { fallbackPackIds }
    return UserPreferences(
        selectedApps = selectedApps,
        preferredTopics = selectedTopics,
        preferredDurationBucket = preferredDurationBucket,
        selectedPackIds = packs,
        meditationDurationMinutes = meditationDurationMinutes,
        contentPriority = contentPriority,
        priorityContentIds = priorityContentIds,
    )
}

private fun RecommendationSet.withMeditationDuration(meditation: ContentItem): RecommendationSet {
    return copy(
        primary = primary.replaceIfMeditation(meditation),
        backups = backups.map { item -> item.replaceIfMeditation(meditation) },
    )
}

private fun UserPreferences.withReadingProgress(progress: List<ReadingProgress>): UserPreferences {
    return copy(unfinishedContentIds = progress.unfinishedContentIds())
}

private fun List<ReadingProgress>.unfinishedContentIds(): Set<String> {
    return filter(ReadingProgress::isUnfinished)
        .mapTo(mutableSetOf(), ReadingProgress::contentId)
}

private fun ReadingProgress.sameVisiblePosition(other: ReadingProgress): Boolean {
    return contentId == other.contentId &&
        progressPercent == other.progressPercent &&
        lastVisibleParagraphIndex == other.lastVisibleParagraphIndex &&
        paragraphCount == other.paragraphCount &&
        completedAtMillis == other.completedAtMillis
}

private fun ReadingProgress.analyticsMetadata(): Map<String, String> {
    return mapOf(
        "progressPercent" to progressPercent.toString(),
        "lastVisibleParagraphIndex" to lastVisibleParagraphIndex.toString(),
        "paragraphCount" to paragraphCount.toString(),
        "completed" to isCompleted().toString(),
        "updatedAtMillis" to updatedAtMillis.toString(),
        "completedAtMillis" to (completedAtMillis?.toString() ?: ""),
    )
}

private fun ContentItem.replaceIfMeditation(meditation: ContentItem): ContentItem {
    return if (usesMeditationTimer()) meditation else this
}

private fun ContentItem.toAddLinkConfirmation(priorityMarked: Boolean = false): AddLinkConfirmation {
    return AddLinkConfirmation(
        title = title,
        host = when (sourceType) {
            ContentSourceType.USER_DOCUMENT -> sourceLabel.orEmpty().ifBlank { "your file" }
            else -> externalUrl?.hostLabel().orEmpty().ifBlank { "your link" }
        },
        durationMinutes = durationMinutes,
        topicLabel = topicTags.firstOrNull()?.displayName().orEmpty().ifBlank { "Reading" },
        priorityMarked = priorityMarked,
    )
}

private fun List<ContentItem>.toAddLinkConfirmation(
    skippedCount: Int,
    priorityMarked: Boolean,
): AddLinkConfirmation {
    if (size == 1) {
        return single().toAddLinkConfirmation(priorityMarked = priorityMarked).copy(skippedCount = skippedCount)
    }
    val firstTopic = flatMap { item -> item.topicTags }.firstOrNull()?.displayName().orEmpty().ifBlank { "Reading" }
    return AddLinkConfirmation(
        title = "$size files saved",
        host = "Batch import",
        durationMinutes = maxOf(1, maxOfOrNull(ContentItem::durationMinutes) ?: ReadingTimeEstimator.DEFAULT_DOCUMENT_MINUTES),
        topicLabel = firstTopic,
        savedCount = size,
        skippedCount = skippedCount,
        priorityMarked = priorityMarked,
    )
}

private fun defaultDocumentDuration(mimeType: String?, displayName: String): String {
    return when (UserDocumentValidator.detectFormat(displayName = displayName, mimeType = mimeType)) {
        ContentFormat.MARKDOWN -> ReadingTimeEstimator.DEFAULT_DOCUMENT_MINUTES.toString()
        ContentFormat.PDF -> ReadingTimeEstimator.DEFAULT_PDF_MINUTES.toString()
        ContentFormat.EPUB -> ReadingTimeEstimator.MAX_SESSION_MINUTES.toString()
        else -> ReadingTimeEstimator.DEFAULT_DOCUMENT_MINUTES.toString()
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
        TopicTag.ATTENTION -> "Attention"
        TopicTag.PRACTICAL -> "Practical"
        TopicTag.BODY -> "Body"
        TopicTag.NATURE -> "Nature"
        TopicTag.HISTORY_CULTURE -> "History & culture"
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
        TopicTag.CREATIVITY -> "Creativity"
        TopicTag.PSYCHOLOGY -> "Psychology"
        TopicTag.OTHER -> "Other"
    }
}

private fun defaultPrototypeTopics(): Set<TopicTag> = setOf(
    TopicTag.ATTENTION,
    TopicTag.PRACTICAL,
    TopicTag.SCIENCE,
)

private const val ATTENTION_CLASSICS_PACK_ID = "attention-classics-v1"
private const val PUBLIC_DOMAIN_EXPANSION_PACK_ID = "public-domain-expansion-v2"
private const val LINK_ONLY_MODERN_PACK_ID = "link-only-modern-v1"
private val SPRINT9_PACK_IDS = setOf(
    "attention_practical_agency_v1",
    "embodied_calm_v1",
    "wonder_science_v1",
    "long_view_history_v1",
    "creativity_play_v1",
)

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
private const val INTERVENTION_DEGRADED_THRESHOLD_MILLIS = 2_000L
private const val MIN_SELECTED_DISTRACTING_APPS = 3
private const val MAX_READING_ANNOTATION_QUOTE_LENGTH = 1_200
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
    return documentCandidates()
        .singleOrNull()
        ?.toDraftOrNull(selectedTopics)
        ?.takeIf { localValidationErrors().isEmpty() }
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
    val candidates = documentCandidates()
    if (candidates.isEmpty()) {
        return setOf(UserDocumentValidationError.EMPTY_URI)
    }
    val supported = candidates.filter { candidate -> candidate.format != null }
    if (supported.isEmpty()) {
        return setOf(UserDocumentValidationError.UNSUPPORTED_FORMAT)
    }
    val errors = mutableSetOf<UserDocumentValidationError>()
    supported.forEach { candidate ->
        errors += candidate.validationErrors(selectedTopics)
    }
    return errors
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
    val candidates = documentCandidates()
    if (candidates.isEmpty()) {
        return setOf(UserDocumentValidationError.EMPTY_URI)
    }
    val errors = mutableSetOf<UserDocumentValidationError>()
    if (candidates.all { candidate -> candidate.format == null }) {
        errors += UserDocumentValidationError.UNSUPPORTED_FORMAT
    }
    val editableCandidate = candidates.singleOrNull()
    if (editableCandidate != null && editableCandidate.uri.isBlank()) {
        errors += UserDocumentValidationError.EMPTY_URI
    }
    if (editableCandidate != null) {
        if (editableCandidate.format == null) {
            errors += UserDocumentValidationError.UNSUPPORTED_FORMAT
        }
        if (editableCandidate.title.isBlank() && (editableCandidate.uri.isNotBlank() || selectedTopics.isNotEmpty())) {
            errors += UserDocumentValidationError.BLANK_TITLE
        }
        if (selectedTopics.isEmpty() && editableCandidate.uri.isNotBlank() && editableCandidate.title.isNotBlank()) {
            errors += UserDocumentValidationError.NO_TOPICS
        }
    } else if (selectedTopics.isEmpty() && candidates.any { candidate -> candidate.format != null }) {
        errors += UserDocumentValidationError.NO_TOPICS
    }
    return errors
}

private fun AddDocumentFormState.documentCandidates(): List<DocumentImportCandidate> {
    if (candidates.isNotEmpty()) {
        return if (candidates.size == 1) {
            listOf(
                candidates.first().copy(
                    uri = uri.ifBlank { candidates.first().uri },
                    displayName = displayName.ifBlank { candidates.first().displayName },
                    mimeType = mimeType ?: candidates.first().mimeType,
                    title = title,
                    durationMinutes = durationMinutes,
                ).cleaned(),
            )
        } else {
            candidates.map(DocumentImportCandidate::cleaned)
        }
    }
    if (uri.isBlank()) {
        return emptyList()
    }
    return listOf(
        DocumentImportCandidate(
            uri = uri,
            displayName = displayName,
            mimeType = mimeType,
            title = title,
            durationMinutes = durationMinutes,
            format = UserDocumentValidator.detectFormat(displayName = displayName, mimeType = mimeType),
        ).cleaned(),
    )
}

private fun DocumentImportCandidate.cleaned(): DocumentImportCandidate {
    val cleanedName = displayName.trim().ifBlank { "Untitled document" }
    val cleanedTitle = title.trim().ifBlank {
        cleanedName.substringBeforeLast('.', cleanedName).trim().ifBlank { cleanedName }
    }
    val detectedFormat = format ?: UserDocumentValidator.detectFormat(displayName = cleanedName, mimeType = mimeType)
    val cleanedDuration = durationMinutes.trim().ifBlank {
        defaultDocumentDuration(mimeType = mimeType, displayName = cleanedName)
    }
    return copy(
        uri = uri.trim(),
        displayName = cleanedName,
        title = cleanedTitle,
        durationMinutes = cleanedDuration,
        format = detectedFormat,
    )
}

private fun DocumentImportCandidate.validationErrors(selectedTopics: Set<TopicTag>): Set<UserDocumentValidationError> {
    val duration = durationMinutes.toIntOrNull() ?: ReadingTimeEstimator.DEFAULT_DOCUMENT_MINUTES
    return UserDocumentValidator.validate(
        UserDocumentDraft(
            uri = uri,
            displayName = displayName,
            mimeType = mimeType,
            title = title,
            durationMinutes = duration,
            topicTags = selectedTopics,
        ),
    ).errors
}

private fun DocumentImportCandidate.toDraftOrNull(selectedTopics: Set<TopicTag>): UserDocumentDraft? {
    val duration = durationMinutes.toIntOrNull() ?: ReadingTimeEstimator.DEFAULT_DOCUMENT_MINUTES
    if (validationErrors(selectedTopics).isNotEmpty()) {
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

private fun RecommendationSet.analyticsMetadata(): Map<String, String> {
    return primary.analyticsMetadata(prefix = "primary") +
        backups.flatMapIndexed { index, content ->
            content.analyticsMetadata(prefix = "backup${index + 1}").entries
        }.associate { it.toPair() }
}

private fun Set<String>.trackedCompletedContentIds(): Set<String> {
    return this - MEDITATION_TIMER_CONTENT_ID
}

private fun ReplacementHistoryEntry.isCompletedReadingReplacement(): Boolean {
    return isCompleted() && contentId != MEDITATION_TIMER_CONTENT_ID
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
        "priorityContentCount" to preferences.priorityContentIds.size.toString(),
        "unfinishedContentCount" to preferences.unfinishedContentIds.size.toString(),
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

private fun ContentItem.isUserManagedContent(): Boolean {
    return sourceType == ContentSourceType.USER_LINK || sourceType == ContentSourceType.USER_DOCUMENT
}

private fun deletedLibraryMessage(count: Int): String {
    return if (count == 1) "Deleted 1 saved item." else "Deleted $count saved items."
}

private fun Int?.orZeroString(): String = (this ?: 0).toString()

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

    override suspend fun deleteLink(contentId: String) = Unit
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

    override suspend fun deleteDocument(contentId: String) = Unit
}

private object EmptyReadingProgressRepository : ReadingProgressRepository {
    override fun readingProgress(): List<ReadingProgress> = emptyList()

    override fun observeReadingProgress() = flowOf(emptyList<ReadingProgress>())

    override suspend fun saveProgress(progress: ReadingProgress) = Unit

    override suspend fun deleteProgress(contentId: String) = Unit
}

private object EmptyReadingAnnotationRepository : ReadingAnnotationRepository {
    override fun readingAnnotations(): List<ReadingAnnotation> = emptyList()

    override fun observeReadingAnnotations() = flowOf(emptyList<ReadingAnnotation>())

    override suspend fun saveAnnotation(
        draft: com.qualityalternative.app.domain.model.ReadingAnnotationDraft,
        nowMillis: Long,
    ): ReadingAnnotation = error("Reading annotations are not available in this build.")

    override suspend fun deleteAnnotation(
        annotationId: String,
        nowMillis: Long,
    ) = Unit
}

private object NoOpReadingAnnotationExportWriter : ReadingAnnotationExportWriter {
    override suspend fun writeMarkdown(uri: String, markdown: String) = Unit
}

private object NoOpReadingAnnotationDriveSyncClient : ReadingAnnotationDriveSyncClient {
    override suspend fun syncJsonLdFiles(
        request: ReadingAnnotationDriveSyncRequest,
    ) = error("Google Drive sync is not available in this build.")
}

private object NoOpReadingAnnotationDriveTokenProvider : ReadingAnnotationDriveTokenProvider {
    override suspend fun driveAccessToken(): String = error("Google Drive authorization is not available in this build.")
}

private data class AnnotationAutosaveResult(
    val fileResult: Boolean?,
    val driveResult: Boolean?,
)

private fun String.withAnnotationAutosaveResult(result: AnnotationAutosaveResult): String {
    val successes = listOfNotNull(
        "file".takeIf { result.fileResult == true },
        "Drive".takeIf { result.driveResult == true },
    )
    val failures = listOfNotNull(
        "file".takeIf { result.fileResult == false },
        "Drive".takeIf { result.driveResult == false },
    )
    return when {
        successes.isEmpty() && failures.isEmpty() -> this
        failures.isEmpty() -> removeSuffix(".") + " and autosaved."
        successes.isEmpty() -> this + " Autosave failed."
        else -> this + " Autosaved to ${successes.joinToString(" and ")}; ${failures.joinToString(" and ")} failed."
    }
}

private fun Throwable.annotationExportErrorMessage(): String {
    return "Choose the file again or retry."
}

private fun Throwable.annotationDriveSyncErrorMessage(): String {
    return "Google Drive sync failed. Retry from Settings."
}
