package com.qualityalternative.app.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.qualityalternative.app.data.AppContainer
import com.qualityalternative.app.data.SupportedCatalog
import com.qualityalternative.app.data.UserLinkValidator
import com.qualityalternative.app.domain.model.AnalyticsEvent
import com.qualityalternative.app.domain.model.AnalyticsSemanticKeys
import com.qualityalternative.app.domain.model.AnalyticsEventType
import com.qualityalternative.app.domain.model.AppSettings
import com.qualityalternative.app.domain.model.ContentItem
import com.qualityalternative.app.domain.model.ContentSourceType
import com.qualityalternative.app.domain.model.DelayWindow
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
import com.qualityalternative.app.domain.model.TimeOfDayBucket
import com.qualityalternative.app.domain.model.TopicTag
import com.qualityalternative.app.domain.model.UserLinkDraft
import com.qualityalternative.app.domain.model.UserLinkValidationError
import com.qualityalternative.app.domain.model.UserPreferences
import com.qualityalternative.app.domain.service.AddUserLinkResult
import com.qualityalternative.app.domain.service.AnalyticsTracker
import com.qualityalternative.app.domain.service.ContentRepository
import com.qualityalternative.app.domain.service.DelayGate
import com.qualityalternative.app.domain.service.HistoryRepository
import com.qualityalternative.app.domain.service.InterceptionMonitor
import com.qualityalternative.app.domain.service.RecommendationEngine
import com.qualityalternative.app.domain.service.SettingsRepository
import com.qualityalternative.app.domain.service.UserLinkRepository
import com.qualityalternative.app.interception.InterceptionRuntimeGate
import java.util.UUID
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
    val permissionReadiness: PermissionReadiness = unavailablePermissionReadiness(),
    val historyEntries: List<ReplacementHistoryEntry> = emptyList(),
    val completedContentIds: Set<String> = emptySet(),
    val userLinks: List<ContentItem> = emptyList(),
    val addLinkForm: AddLinkFormState = AddLinkFormState(),
    val latestMessage: String? = null,
    val events: List<AnalyticsEvent> = emptyList(),
    val screen: MainScreen = MainScreen.Onboarding,
    val lastFeedback: SessionFeedback? = null,
)

data class AddLinkFormState(
    val url: String = "",
    val title: String = "",
    val durationMinutes: String = "7",
    val selectedTopics: Set<TopicTag> = emptySet(),
    val validationErrors: Set<UserLinkValidationError> = emptySet(),
    val canSave: Boolean = false,
    val isSaving: Boolean = false,
)

enum class MainScreen {
    Onboarding,
    Home,
    AddLink,
    Intervention,
    Reader,
    Feedback,
}

enum class InterventionOrigin {
    DEBUG,
    SYSTEM,
}

class MainViewModel(
    private val contentRepository: ContentRepository,
    private val userLinkRepository: UserLinkRepository = EmptyUserLinkRepository,
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
    private val starterPackIds = starterPacks.mapTo(mutableSetOf(), EditorialPack::id)
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
            historyRepository.observeRecentHistory().collect { history ->
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
        uiState = uiState.copy(
            selectedTargetApp = app,
            activeDelayWindow = if (delayReady) delayGate.activeDelay(app) else null,
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
            uiState = uiState.copy(latestMessage = "Onboarding saved locally.")
        }
    }

    fun openAddLink() {
        uiState = uiState.copy(
            screen = MainScreen.AddLink,
            addLinkForm = AddLinkFormState(),
            latestMessage = null,
        )
    }

    fun cancelAddLink() {
        uiState = uiState.copy(
            screen = MainScreen.Home,
            addLinkForm = AddLinkFormState(),
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
            when (val result = userLinkRepository.addLink(draft = draft, nowMillis = nowMillis)) {
                is AddUserLinkResult.Added -> {
                    recordEventDurably(
                        AnalyticsEvent(
                            type = AnalyticsEventType.USER_LINK_ADDED,
                            timestampMillis = nowMillis,
                            contentId = result.item.id,
                            metadata = mapOf(
                                "sourceType" to result.item.sourceType.name,
                                "externalUrl" to result.item.externalUrl.orEmpty(),
                                "durationMinutes" to result.item.durationMinutes.toString(),
                                "topicCount" to result.item.topicTags.size.toString(),
                            ),
                        ),
                    )
                    uiState = uiState.copy(
                        screen = MainScreen.Home,
                        addLinkForm = AddLinkFormState(),
                        latestMessage = "Link saved for future replacement moments.",
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
                uiState = uiState.copy(
                    selectedTargetApp = targetApp,
                    activeDelayWindow = delayGate.activeDelay(targetApp = targetApp, nowMillis = processingNowMillis),
                    latestMessage = "${targetApp.displayName} is delayed for 15 minutes.",
                    screen = MainScreen.Home,
                    currentInterventionOrigin = null,
                )
                return@launch
            }

            delayInspection.expiredWindow?.let { expiredWindow ->
                recordDelayReturnAfterExpiry(expiredWindow = expiredWindow, nowMillis = processingNowMillis)
                delayGate.consumeExpiredDelay(
                    targetApp = targetApp,
                    delayId = expiredWindow.id,
                    nowMillis = processingNowMillis,
                )
            }

            recordReturnSignalIfNeeded(targetApp = targetApp, nowMillis = processingNowMillis)

            val interventionId = UUID.randomUUID().toString()
            val filteredInventory = contentRepository.inventory().filter { item ->
                item.sourceType == ContentSourceType.USER_LINK || item.packId in preferences.selectedPackIds
            }
            val signals = buildRecommendationSignals(nowMillis = processingNowMillis)
            val recommendationSet = recommendationEngine.generate(
                targetApp = targetApp,
                preferences = preferences,
                inventory = filteredInventory,
                primaryExcludedIds = uiState.completedContentIds,
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
                        metadata = mapOf("selectedPackCount" to preferences.selectedPackIds.size.toString()),
                    ),
                )
                uiState = uiState.copy(
                    selectedTargetApp = targetApp,
                    currentInterventionId = null,
                    currentInterventionShownAtMillis = null,
                    currentRecommendationSet = null,
                    activeDelayWindow = null,
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
                ),
            )
            openReader(content = recommendationSet.primary, sessionId = sessionId, startedAtMillis = nowMillis)
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
                ),
            )
            openReader(content = content, sessionId = sessionId, startedAtMillis = nowMillis)
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
                latestMessage = "${targetApp.displayName} delayed for 15 minutes.",
            )
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
            latestMessage = "Prototype override recorded for ${targetApp.displayName}.",
        )
        return shouldExitToTarget
    }

    fun finishReading() {
        val content = uiState.currentContent ?: return
        val sessionId = uiState.currentSessionId ?: return
        val nowMillis = System.currentTimeMillis()
        viewModelScope.launch {
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
                    metadata = sessionDurationMetadata(nowMillis),
                ),
            )
            uiState = uiState.copy(
                screen = MainScreen.Feedback,
            )
        }
    }

    fun skipReading() {
        val content = uiState.currentContent ?: return
        val sessionId = uiState.currentSessionId ?: return
        val nowMillis = System.currentTimeMillis()
        viewModelScope.launch {
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
                    metadata = sessionDurationMetadata(nowMillis),
                ),
            )
            clearActiveSession(
                latestMessage = "Replacement session skipped.",
            )
        }
    }

    fun submitFeedback(wasGoodFit: Boolean, helpedAvoidScrolling: Boolean) {
        val feedback = SessionFeedback(
            wasGoodFit = wasGoodFit,
            helpedAvoidScrolling = helpedAvoidScrolling,
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
                    ),
                ),
            )
            clearActiveSession(
                lastFeedback = feedback,
                latestMessage = "Feedback captured for the replacement session.",
            )
        }
    }

    fun skipFeedback() {
        clearActiveSession(latestMessage = "Feedback skipped for this session.")
    }

    fun dismissMessage() {
        uiState = uiState.copy(latestMessage = null)
    }

    private fun applySettings(settings: AppSettings) {
        val preferences = settings.toUserPreferences(
            supportedApps = supportedApps,
            fallbackPackIds = starterPackIds,
        )
        val availableTargetApps = if (settings.hasCompletedOnboarding) preferences.selectedApps else emptyList()
        val selectedTargetApp = if (settings.hasCompletedOnboarding) {
            uiState.selectedTargetApp.takeIf { candidate ->
                availableTargetApps.any { it.packageName == candidate?.packageName }
            } ?: availableTargetApps.firstOrNull()
        } else {
            null
        }

        uiState = uiState.copy(
            hasCompletedOnboarding = settings.hasCompletedOnboarding,
            availableTargetApps = availableTargetApps,
            selectedTargetApp = selectedTargetApp,
            preferences = preferences.takeIf { settings.hasCompletedOnboarding },
            onboardingSelection = settings.toOnboardingSelection(
                supportedApps = supportedApps,
                starterPacks = starterPacks,
            ),
            activeDelayWindow = selectedTargetApp?.takeIf { delayReady }?.let { delayGate.activeDelay(it) },
            permissionReadiness = interceptionMonitor.currentReadiness(),
            screen = when {
                !settings.hasCompletedOnboarding -> MainScreen.Onboarding
                uiState.screen == MainScreen.Onboarding -> MainScreen.Home
                uiState.screen == MainScreen.AddLink -> MainScreen.AddLink
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

    private fun openReader(content: ContentItem, sessionId: String, startedAtMillis: Long) {
        uiState = uiState.copy(
            currentContent = content,
            currentContentBody = contentRepository.contentBody(content),
            currentSessionId = sessionId,
            currentSessionStartedAtMillis = startedAtMillis,
            screen = MainScreen.Reader,
        )
    }

    private fun clearActiveSession(
        lastFeedback: SessionFeedback? = uiState.lastFeedback,
        latestMessage: String,
    ) {
        uiState = uiState.copy(
            screen = MainScreen.Home,
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
        if (uiState.activeDelayWindow != activeDelayWindow) {
            uiState = uiState.copy(activeDelayWindow = activeDelayWindow)
        }
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
            ?: SupportedCatalog.findByPackage(targetAppPackage)
    }

    private fun updateAddLinkForm(form: AddLinkFormState) {
        uiState = uiState.copy(
            addLinkForm = form.copy(
                validationErrors = form.visibleValidationErrors(),
                canSave = form.localValidationErrors().isEmpty(),
                isSaving = false,
            ),
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
    return OnboardingSelection(
        selectedAppPackages = supportedApps.take(3).mapTo(mutableSetOf(), DistractingApp::packageName),
        preferredTopics = TopicTag.entries.take(3).toSet(),
        preferredDurationBucket = DurationBucket.FOCUS,
        selectedPackIds = starterPacks.take(1).mapTo(mutableSetOf(), EditorialPack::id),
    )
}

private fun AppSettings.toUserPreferences(
    supportedApps: List<DistractingApp>,
    fallbackPackIds: Set<String>,
): UserPreferences {
    val selectedApps = supportedApps.filter { it.packageName in selectedAppPackages }
        .ifEmpty { supportedApps.take(3) }
    val selectedTopics = preferredTopics.ifEmpty { TopicTag.entries.take(3).toSet() }
    val packs = selectedPackIds.ifEmpty { fallbackPackIds.take(1).toSet() }
    return UserPreferences(
        selectedApps = selectedApps,
        preferredTopics = selectedTopics,
        preferredDurationBucket = preferredDurationBucket,
        selectedPackIds = packs,
    )
}

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
