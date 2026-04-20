package com.qualityalternative.app.domain.service

import com.qualityalternative.app.domain.model.AnalyticsEvent
import com.qualityalternative.app.domain.model.AppSettings
import com.qualityalternative.app.domain.model.ContentItem
import com.qualityalternative.app.domain.model.DelayInspection
import com.qualityalternative.app.domain.model.DelayWindow
import com.qualityalternative.app.domain.model.DistractingApp
import com.qualityalternative.app.domain.model.EditorialPack
import com.qualityalternative.app.domain.model.OnboardingSelection
import com.qualityalternative.app.domain.model.PermissionReadiness
import com.qualityalternative.app.domain.model.RecommendationSet
import com.qualityalternative.app.domain.model.RecommendationSignals
import com.qualityalternative.app.domain.model.RecommendationSource
import com.qualityalternative.app.domain.model.ReplacementHistoryEntry
import com.qualityalternative.app.domain.model.ReturnToTargetSignal
import com.qualityalternative.app.domain.model.SessionFeedback
import com.qualityalternative.app.domain.model.UserLinkDraft
import com.qualityalternative.app.domain.model.UserLinkValidationError
import com.qualityalternative.app.domain.model.UserPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

interface ContentRepository {
    fun starterPacks(): List<EditorialPack>
    fun inventory(): List<ContentItem>
    fun contentBody(item: ContentItem): String
}

sealed class AddUserLinkResult {
    data class Added(val item: ContentItem) : AddUserLinkResult()

    data class Rejected(val errors: Set<UserLinkValidationError>) : AddUserLinkResult()
}

interface UserLinkRepository {
    fun userLinks(): List<ContentItem>

    fun observeUserLinks(): Flow<List<ContentItem>> = flowOf(userLinks())

    suspend fun addLink(
        draft: UserLinkDraft,
        nowMillis: Long = System.currentTimeMillis(),
    ): AddUserLinkResult

    suspend fun markUnavailable(
        contentId: String,
        nowMillis: Long = System.currentTimeMillis(),
    )

    fun isReady(): Boolean = true

    fun observeReady(): Flow<Boolean> = flowOf(isReady())
}

interface SettingsRepository {
    fun observeAppSettings(): Flow<AppSettings>
    fun supportedDistractingApps(): List<DistractingApp>
    suspend fun saveOnboardingSelection(selection: OnboardingSelection)
}

interface RecommendationEngine {
    fun generate(
        targetApp: DistractingApp,
        preferences: UserPreferences,
        inventory: List<ContentItem>,
        primaryExcludedIds: Set<String>,
        signals: RecommendationSignals,
        nowMillis: Long = System.currentTimeMillis(),
    ): RecommendationSet?
}

interface InterceptionMonitor {
    fun isAvailable(): Boolean
    fun currentReadiness(): PermissionReadiness
}

interface DelayGate {
    /**
     * Returns the current delay state for an intervention attempt without consuming expired delay
     * provenance. The trigger path must explicitly consume an expired window after it finishes
     * any required analytics or follow-up work.
     */
    fun inspectDelay(targetApp: DistractingApp, nowMillis: Long = System.currentTimeMillis()): DelayInspection

    /**
     * Returns the currently active delay window without consuming expired provenance.
     */
    fun activeDelay(targetApp: DistractingApp, nowMillis: Long = System.currentTimeMillis()): DelayWindow?

    suspend fun consumeExpiredDelay(
        targetApp: DistractingApp,
        delayId: String,
        nowMillis: Long = System.currentTimeMillis(),
    ): Boolean

    fun storeDelay(
        targetApp: DistractingApp,
        nowMillis: Long = System.currentTimeMillis(),
        durationMinutes: Int = 15,
        interventionId: String? = null,
        interventionShownAtMillis: Long? = null,
        primaryContentId: String? = null,
        backupContentIds: List<String> = emptyList(),
    ): DelayWindow

    suspend fun storeDelayDurably(
        targetApp: DistractingApp,
        nowMillis: Long = System.currentTimeMillis(),
        durationMinutes: Int = 15,
        interventionId: String? = null,
        interventionShownAtMillis: Long? = null,
        primaryContentId: String? = null,
        backupContentIds: List<String> = emptyList(),
    ): DelayWindow {
        return storeDelay(
            targetApp = targetApp,
            nowMillis = nowMillis,
            durationMinutes = durationMinutes,
            interventionId = interventionId,
            interventionShownAtMillis = interventionShownAtMillis,
            primaryContentId = primaryContentId,
            backupContentIds = backupContentIds,
        )
    }

    fun recordFirstReturnAttempt(
        targetApp: DistractingApp,
        nowMillis: Long = System.currentTimeMillis(),
    ): DelayWindow?

    suspend fun recordFirstReturnAttemptDurably(
        targetApp: DistractingApp,
        nowMillis: Long = System.currentTimeMillis(),
    ): DelayWindow? {
        return recordFirstReturnAttempt(targetApp = targetApp, nowMillis = nowMillis)
    }

    fun isReady(): Boolean = true

    fun observeReady(): Flow<Boolean> = flowOf(isReady())
}

interface AnalyticsTracker {
    fun record(event: AnalyticsEvent)
    suspend fun recordDurably(event: AnalyticsEvent) {
        record(event)
    }
    fun allEvents(): List<AnalyticsEvent>
    fun observeEvents(): Flow<List<AnalyticsEvent>> = flowOf(allEvents())
    fun isReady(): Boolean = true
    fun observeReady(): Flow<Boolean> = flowOf(isReady())
}

interface HistoryRepository {
    fun recentHistory(nowMillis: Long = System.currentTimeMillis(), windowDays: Int = 7): List<ReplacementHistoryEntry>
    fun observeRecentHistory(nowMillis: Long = System.currentTimeMillis(), windowDays: Int = 7): Flow<List<ReplacementHistoryEntry>> =
        flowOf(recentHistory(nowMillis = nowMillis, windowDays = windowDays))

    fun observeCompletedContentIds(): Flow<Set<String>> =
        flowOf(
            recentHistory().filter(ReplacementHistoryEntry::isCompleted)
                .mapTo(mutableSetOf(), ReplacementHistoryEntry::contentId),
        )

    suspend fun recordAcceptedSession(
        targetApp: DistractingApp,
        interventionId: String,
        interventionShownAtMillis: Long,
        primaryContentId: String,
        backupContentIds: List<String>,
        content: ContentItem,
        source: RecommendationSource,
        acceptedAtMillis: Long = System.currentTimeMillis(),
    ): String

    suspend fun markCompleted(sessionId: String, completedAtMillis: Long = System.currentTimeMillis())

    suspend fun markSkipped(sessionId: String, skippedAtMillis: Long = System.currentTimeMillis())

    suspend fun attachFeedback(sessionId: String, feedback: SessionFeedback)

    suspend fun markReturnedToTarget(
        targetAppPackage: String,
        returnedAtMillis: Long = System.currentTimeMillis(),
    ): ReturnToTargetSignal?

    fun isReady(): Boolean = true

    fun observeReady(): Flow<Boolean> = flowOf(isReady())
}
