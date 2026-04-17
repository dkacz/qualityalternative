package com.qualityalternative.app.domain.service

import com.qualityalternative.app.domain.model.AnalyticsEvent
import com.qualityalternative.app.domain.model.AppSettings
import com.qualityalternative.app.domain.model.ContentItem
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
import com.qualityalternative.app.domain.model.UserPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

interface ContentRepository {
    fun starterPacks(): List<EditorialPack>
    fun inventory(): List<ContentItem>
    fun contentBody(item: ContentItem): String
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
        excludedIds: Set<String>,
        signals: RecommendationSignals,
        nowMillis: Long = System.currentTimeMillis(),
    ): RecommendationSet?
}

interface InterceptionMonitor {
    fun isAvailable(): Boolean
    fun currentReadiness(): PermissionReadiness
}

interface DelayGate {
    fun activeDelay(targetApp: DistractingApp, nowMillis: Long = System.currentTimeMillis()): DelayWindow?
    fun storeDelay(
        targetApp: DistractingApp,
        nowMillis: Long = System.currentTimeMillis(),
        durationMinutes: Int = 15,
    ): DelayWindow
}

interface AnalyticsTracker {
    fun record(event: AnalyticsEvent)
    fun allEvents(): List<AnalyticsEvent>
    fun observeEvents(): Flow<List<AnalyticsEvent>> = flowOf(allEvents())
}

interface HistoryRepository {
    fun recentHistory(nowMillis: Long = System.currentTimeMillis(), windowDays: Int = 7): List<ReplacementHistoryEntry>
    fun observeRecentHistory(nowMillis: Long = System.currentTimeMillis(), windowDays: Int = 7): Flow<List<ReplacementHistoryEntry>> =
        flowOf(recentHistory(nowMillis = nowMillis, windowDays = windowDays))

    fun recordAcceptedSession(
        targetApp: DistractingApp,
        content: ContentItem,
        source: RecommendationSource,
        acceptedAtMillis: Long = System.currentTimeMillis(),
    ): String

    fun markCompleted(sessionId: String, completedAtMillis: Long = System.currentTimeMillis())

    fun markSkipped(sessionId: String, skippedAtMillis: Long = System.currentTimeMillis())

    fun attachFeedback(sessionId: String, feedback: SessionFeedback)

    fun markReturnedToTarget(
        targetAppPackage: String,
        returnedAtMillis: Long = System.currentTimeMillis(),
    ): ReturnToTargetSignal?
}
