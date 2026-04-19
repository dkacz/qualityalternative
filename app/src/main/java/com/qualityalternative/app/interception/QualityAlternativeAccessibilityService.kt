package com.qualityalternative.app.interception

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import com.qualityalternative.app.QualityAlternativeApplication
import com.qualityalternative.app.domain.model.AnalyticsEvent
import com.qualityalternative.app.domain.model.AnalyticsEventType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class QualityAlternativeAccessibilityService : AccessibilityService() {
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val detectionPolicy = ForegroundAppDetectionPolicy()
    @Volatile
    private var selectedPackages: Set<String> = emptySet()

    override fun onServiceConnected() {
        super.onServiceConnected()
        val appContainer = (application as QualityAlternativeApplication).appContainer
        serviceScope.launch {
            appContainer.settingsRepository.observeAppSettings().collect { settings ->
                selectedPackages = settings.selectedAppPackages
            }
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            return
        }
        val packageName = event.packageName?.toString()?.takeIf(String::isNotBlank) ?: return
        val nowMillis = System.currentTimeMillis()
        if (!detectionPolicy.shouldLog(packageName, selectedPackages, nowMillis)) {
            return
        }

        val appContainer = (application as QualityAlternativeApplication).appContainer
        serviceScope.launch {
            appContainer.analyticsTracker.recordDurably(
                AnalyticsEvent(
                    type = AnalyticsEventType.TARGET_APP_FOREGROUND_DETECTED,
                    timestampMillis = nowMillis,
                    targetAppPackage = packageName,
                    metadata = mapOf(
                        "triggerSource" to "accessibility_service",
                        "interceptionStage" to "sprint3_skeleton",
                    ),
                ),
            )
        }
    }

    override fun onInterrupt() = Unit

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }
}
