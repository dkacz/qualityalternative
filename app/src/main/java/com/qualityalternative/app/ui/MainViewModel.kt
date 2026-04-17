package com.qualityalternative.app.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.qualityalternative.app.data.AppContainer
import com.qualityalternative.app.domain.model.AnalyticsEvent
import com.qualityalternative.app.domain.model.AnalyticsEventType
import com.qualityalternative.app.domain.model.AppSettings
import com.qualityalternative.app.domain.model.ContentItem
import com.qualityalternative.app.domain.model.DistractingApp
import com.qualityalternative.app.domain.model.DurationBucket
import com.qualityalternative.app.domain.model.EditorialPack
import com.qualityalternative.app.domain.model.OnboardingSelection
import com.qualityalternative.app.domain.model.RecommendationSet
import com.qualityalternative.app.domain.model.SessionFeedback
import com.qualityalternative.app.domain.model.TopicTag
import com.qualityalternative.app.domain.model.UserPreferences
import com.qualityalternative.app.domain.service.AnalyticsTracker
import com.qualityalternative.app.domain.service.ContentRepository
import com.qualityalternative.app.domain.service.DelayGate
import com.qualityalternative.app.domain.service.RecommendationEngine
import com.qualityalternative.app.domain.service.SettingsRepository
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
    val currentRecommendationSet: RecommendationSet? = null,
    val currentContent: ContentItem? = null,
    val currentContentBody: String = "",
    val completedContentIds: Set<String> = emptySet(),
    val latestMessage: String? = null,
    val events: List<AnalyticsEvent> = emptyList(),
    val screen: MainScreen = MainScreen.Onboarding,
    val lastFeedback: SessionFeedback? = null,
)

enum class MainScreen {
    Onboarding,
    Home,
    Intervention,
    Reader,
    Feedback,
}

class MainViewModel(
    private val contentRepository: ContentRepository,
    private val settingsRepository: SettingsRepository,
    private val recommendationEngine: RecommendationEngine,
    private val delayGate: DelayGate,
    private val analyticsTracker: AnalyticsTracker,
) : ViewModel() {
    private val supportedApps = settingsRepository.supportedDistractingApps()
    private val starterPacks = contentRepository.starterPacks()
    private val starterPackIds = starterPacks.mapTo(mutableSetOf(), EditorialPack::id)

    var uiState by mutableStateOf(
        MainUiState(
            allSupportedApps = supportedApps,
            starterPacks = starterPacks,
            onboardingSelection = defaultOnboardingSelection(
                supportedApps = supportedApps,
                starterPacks = starterPacks,
            ),
            events = analyticsTracker.allEvents(),
        ),
    )
        private set

    init {
        viewModelScope.launch {
            settingsRepository.observeAppSettings().collect { settings ->
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
                    isLoadingSettings = false,
                    hasCompletedOnboarding = settings.hasCompletedOnboarding,
                    availableTargetApps = availableTargetApps,
                    selectedTargetApp = selectedTargetApp,
                    preferences = preferences.takeIf { settings.hasCompletedOnboarding },
                    onboardingSelection = settings.toOnboardingSelection(
                        supportedApps = supportedApps,
                        starterPacks = starterPacks,
                    ),
                    screen = when {
                        !settings.hasCompletedOnboarding -> MainScreen.Onboarding
                        uiState.screen == MainScreen.Onboarding -> MainScreen.Home
                        else -> uiState.screen
                    },
                )
            }
        }
    }

    fun selectTargetApp(app: DistractingApp) {
        uiState = uiState.copy(selectedTargetApp = app, latestMessage = null)
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

    fun triggerDebugIntervention(nowMillis: Long = System.currentTimeMillis()) {
        val targetApp = uiState.selectedTargetApp ?: return
        val preferences = uiState.preferences ?: return

        if (delayGate.activeDelay(targetApp = targetApp, nowMillis = nowMillis) != null) {
            uiState = uiState.copy(
                latestMessage = "${targetApp.displayName} is delayed for 15 minutes.",
                screen = MainScreen.Home,
            )
            return
        }

        val filteredInventory = contentRepository.inventory().filter { it.packId in preferences.selectedPackIds }

        val recommendationSet = recommendationEngine.generate(
            targetApp = targetApp,
            preferences = preferences,
            inventory = filteredInventory,
            excludedIds = uiState.completedContentIds,
            nowMillis = nowMillis,
        )

        if (recommendationSet == null) {
            uiState = uiState.copy(latestMessage = "No replacement inventory is available yet.")
            return
        }

        recordEvent(
            AnalyticsEvent(
                type = AnalyticsEventType.INTERVENTION_SHOWN,
                timestampMillis = nowMillis,
                targetAppPackage = targetApp.packageName,
                contentId = recommendationSet.primary.id,
            ),
        )

        if (recommendationSet.inventoryShortage) {
            recordEvent(
                AnalyticsEvent(
                    type = AnalyticsEventType.INVENTORY_SHORTAGE,
                    timestampMillis = nowMillis,
                    targetAppPackage = targetApp.packageName,
                ),
            )
        }

        uiState = uiState.copy(
            currentRecommendationSet = recommendationSet,
            latestMessage = null,
            screen = MainScreen.Intervention,
        )
    }

    fun acceptPrimary() {
        val targetApp = uiState.selectedTargetApp ?: return
        val recommendationSet = uiState.currentRecommendationSet ?: return
        recordEvent(
            AnalyticsEvent(
                type = AnalyticsEventType.PRIMARY_ACCEPTED,
                timestampMillis = System.currentTimeMillis(),
                targetAppPackage = targetApp.packageName,
                contentId = recommendationSet.primary.id,
            ),
        )
        openReader(content = recommendationSet.primary)
    }

    fun acceptBackup(content: ContentItem) {
        val targetApp = uiState.selectedTargetApp ?: return
        recordEvent(
            AnalyticsEvent(
                type = AnalyticsEventType.BACKUP_ACCEPTED,
                timestampMillis = System.currentTimeMillis(),
                targetAppPackage = targetApp.packageName,
                contentId = content.id,
            ),
        )
        openReader(content = content)
    }

    fun delayFor15Minutes() {
        val targetApp = uiState.selectedTargetApp ?: return
        delayGate.storeDelay(targetApp = targetApp)
        recordEvent(
            AnalyticsEvent(
                type = AnalyticsEventType.DELAY_SELECTED,
                timestampMillis = System.currentTimeMillis(),
                targetAppPackage = targetApp.packageName,
            ),
        )
        uiState = uiState.copy(
            screen = MainScreen.Home,
            currentRecommendationSet = null,
            latestMessage = "${targetApp.displayName} delayed for 15 minutes.",
        )
    }

    fun openAnyway() {
        val targetApp = uiState.selectedTargetApp ?: return
        recordEvent(
            AnalyticsEvent(
                type = AnalyticsEventType.OPEN_ANYWAY_SELECTED,
                timestampMillis = System.currentTimeMillis(),
                targetAppPackage = targetApp.packageName,
            ),
        )
        uiState = uiState.copy(
            screen = MainScreen.Home,
            currentRecommendationSet = null,
            latestMessage = "Prototype override recorded for ${targetApp.displayName}.",
        )
    }

    fun finishReading() {
        val content = uiState.currentContent ?: return
        recordEvent(
            AnalyticsEvent(
                type = AnalyticsEventType.READER_COMPLETED,
                timestampMillis = System.currentTimeMillis(),
                targetAppPackage = uiState.selectedTargetApp?.packageName,
                contentId = content.id,
            ),
        )
        uiState = uiState.copy(
            screen = MainScreen.Feedback,
            completedContentIds = uiState.completedContentIds + content.id,
        )
    }

    fun submitFeedback(wasGoodFit: Boolean, helpedAvoidScrolling: Boolean) {
        val feedback = SessionFeedback(
            wasGoodFit = wasGoodFit,
            helpedAvoidScrolling = helpedAvoidScrolling,
            submittedAtMillis = System.currentTimeMillis(),
        )
        recordEvent(
            AnalyticsEvent(
                type = AnalyticsEventType.FEEDBACK_SUBMITTED,
                timestampMillis = feedback.submittedAtMillis,
                targetAppPackage = uiState.selectedTargetApp?.packageName,
                contentId = uiState.currentContent?.id,
                metadata = mapOf(
                    "goodFit" to wasGoodFit.toString(),
                    "helpedAvoidScrolling" to helpedAvoidScrolling.toString(),
                ),
            ),
        )
        uiState = uiState.copy(
            screen = MainScreen.Home,
            currentContent = null,
            currentContentBody = "",
            currentRecommendationSet = null,
            lastFeedback = feedback,
            latestMessage = "Feedback captured for the replacement session.",
        )
    }

    fun dismissMessage() {
        uiState = uiState.copy(latestMessage = null)
    }

    private fun openReader(content: ContentItem) {
        uiState = uiState.copy(
            currentContent = content,
            currentContentBody = contentRepository.contentBody(content),
            screen = MainScreen.Reader,
        )
    }

    private fun recordEvent(event: AnalyticsEvent) {
        analyticsTracker.record(event)
        uiState = uiState.copy(events = analyticsTracker.allEvents())
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
            settingsRepository = appContainer.settingsRepository,
            recommendationEngine = appContainer.recommendationEngine,
            delayGate = appContainer.delayGate,
            analyticsTracker = appContainer.analyticsTracker,
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
