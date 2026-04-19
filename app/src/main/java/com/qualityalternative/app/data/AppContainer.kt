package com.qualityalternative.app.data

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import com.qualityalternative.app.data.local.QualityAlternativeDatabase
import com.qualityalternative.app.domain.service.AnalyticsTracker
import com.qualityalternative.app.domain.service.ContentRepository
import com.qualityalternative.app.domain.service.DefaultRecommendationEngine
import com.qualityalternative.app.domain.service.DelayGate
import com.qualityalternative.app.domain.service.HistoryRepository
import com.qualityalternative.app.domain.service.InterceptionMonitor
import com.qualityalternative.app.domain.service.RecommendationEngine
import com.qualityalternative.app.domain.service.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

private val Context.appSettingsDataStore by preferencesDataStore(name = "app_settings")
private val Context.delayGateDataStore by preferencesDataStore(name = "delay_gate")

class AppContainer(context: Context) {
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val database = QualityAlternativeDatabase.build(context)

    val analyticsTracker: AnalyticsTracker = RoomAnalyticsTracker(
        dao = database.analyticsEventDao(),
        scope = appScope,
    )
    val contentRepository: ContentRepository = AssetContentRepository(context = context)
    val historyRepository: HistoryRepository = RoomHistoryRepository(
        dao = database.replacementSessionDao(),
        scope = appScope,
    )
    val settingsRepository: SettingsRepository = PreferencesSettingsRepository(
        dataStore = context.appSettingsDataStore,
    )
    val delayGate: DelayGate = PreferencesDelayGate(
        dataStore = context.delayGateDataStore,
        scope = appScope,
    )
    val interceptionMonitor: InterceptionMonitor = AndroidInterceptionMonitor(context = context)
    val recommendationEngine: RecommendationEngine = DefaultRecommendationEngine()
}
