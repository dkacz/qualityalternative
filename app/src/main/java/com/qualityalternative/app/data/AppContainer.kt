package com.qualityalternative.app.data

import android.content.Context
import com.qualityalternative.app.analytics.InMemoryAnalyticsTracker
import com.qualityalternative.app.domain.service.AnalyticsTracker
import com.qualityalternative.app.domain.service.ContentRepository
import com.qualityalternative.app.domain.service.DefaultRecommendationEngine
import com.qualityalternative.app.domain.service.DelayGate
import com.qualityalternative.app.domain.service.InterceptionMonitor
import com.qualityalternative.app.domain.service.RecommendationEngine
import com.qualityalternative.app.domain.service.SettingsRepository

class AppContainer(context: Context) {
    val analyticsTracker: AnalyticsTracker = InMemoryAnalyticsTracker()
    val contentRepository: ContentRepository = AssetContentRepository(context = context)
    val settingsRepository: SettingsRepository = PrototypeSettingsRepository()
    val delayGate: DelayGate = InMemoryDelayGate()
    val interceptionMonitor: InterceptionMonitor = StubInterceptionMonitor()
    val recommendationEngine: RecommendationEngine = DefaultRecommendationEngine()
}
