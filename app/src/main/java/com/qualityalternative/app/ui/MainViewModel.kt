package com.qualityalternative.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.qualityalternative.app.data.AppContainer
import com.qualityalternative.app.domain.model.AnalyticsEvent
import com.qualityalternative.app.domain.model.AnalyticsEventType
import com.qualityalternative.app.domain.model.ContentItem
import com.qualityalternative.app.domain.model.DistractingApp
import com.qualityalternative.app.domain.model.EditorialPack
import com.qualityalternative.app.domain.model.RecommendationSet
import com.qualityalternative.app.domain.model.SessionFeedback
import com.qualityalternative.app.domain.model.UserPreferences

data class MainUiState(
    val selectedTargetApp: DistractingApp? = null,
    val supportedApps: List<DistractingApp> = emptyList(),
    val preferences: UserPreferences? = null,
    val starterPacks: List<EditorialPack> = emptyList(),
    val currentRecommendationSet: RecommendationSet? = null,
    val currentContent: ContentItem? = null,
    val currentContentBody: String = "",
    val completedContentIds: Set<String> = emptySet(),
    val latestMessage: String? = null,
    val events: List<AnalyticsEvent> = emptyList(),
    val screen: MainScreen = MainScreen.Home,
    val lastFeedback: SessionFeedback? = null,
)

enum class MainScreen {
    Home,
    Intervention,
    Reader,
    Feedback,
}

class MainViewModel(
    private val appContainer: AppContainer,
) : ViewModel() {
    private val contentRepository = appContainer.contentRepository
    private val settingsRepository = appContainer.settingsRepository
    private val recommendationEngine = appContainer.recommendationEngine
    private val delayGate = appContainer.delayGate
    private val analyticsTracker = appContainer.analyticsTracker

    var uiState: MainUiState = MainUiState()
        private set

    init {
        val preferences = settingsRepository.currentPreferences()
        val apps = settingsRepository.supportedDistractingApps()
        uiState = uiState.copy(
            selectedTargetApp = apps.firstOrNull(),
            supportedApps = apps,
            preferences = preferences,
            starterPacks = contentRepository.starterPacks(),
            events = analyticsTracker.allEvents(),
        )
    }

    fun selectTargetApp(app: DistractingApp) {
        uiState = uiState.copy(selectedTargetApp = app, latestMessage = null)
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

        val recommendationSet = recommendationEngine.generate(
            targetApp = targetApp,
            preferences = preferences,
            inventory = contentRepository.inventory(),
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
        return MainViewModel(appContainer = appContainer) as T
    }
}
