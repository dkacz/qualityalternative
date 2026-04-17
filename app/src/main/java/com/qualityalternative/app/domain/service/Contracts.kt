package com.qualityalternative.app.domain.service

import com.qualityalternative.app.domain.model.AnalyticsEvent
import com.qualityalternative.app.domain.model.ContentItem
import com.qualityalternative.app.domain.model.DelayWindow
import com.qualityalternative.app.domain.model.DistractingApp
import com.qualityalternative.app.domain.model.EditorialPack
import com.qualityalternative.app.domain.model.RecommendationSet
import com.qualityalternative.app.domain.model.UserPreferences

interface ContentRepository {
    fun starterPacks(): List<EditorialPack>
    fun inventory(): List<ContentItem>
    fun contentBody(item: ContentItem): String
}

interface SettingsRepository {
    fun currentPreferences(): UserPreferences
    fun supportedDistractingApps(): List<DistractingApp>
}

interface RecommendationEngine {
    fun generate(
        targetApp: DistractingApp,
        preferences: UserPreferences,
        inventory: List<ContentItem>,
        excludedIds: Set<String>,
        nowMillis: Long = System.currentTimeMillis(),
    ): RecommendationSet?
}

interface InterceptionMonitor {
    fun isAvailable(): Boolean
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
}
