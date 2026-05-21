package com.qualityalternative.app.interception

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import com.qualityalternative.app.MainActivity
import com.qualityalternative.app.QualityAlternativeApplication
import com.qualityalternative.app.domain.model.AnalyticsEvent
import com.qualityalternative.app.domain.model.AnalyticsEventType
import com.qualityalternative.app.domain.model.DEFAULT_BEDTIME_ENABLED
import com.qualityalternative.app.domain.model.DEFAULT_BEDTIME_END_MINUTES
import com.qualityalternative.app.domain.model.DEFAULT_BEDTIME_START_MINUTES
import com.qualityalternative.app.domain.model.bedtimeWindowIsActive
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
    private var interceptionSettings = InterceptionSettingsSnapshot()

    override fun onServiceConnected() {
        super.onServiceConnected()
        val appContainer = (application as QualityAlternativeApplication).appContainer
        serviceScope.launch {
            appContainer.settingsRepository.observeAppSettings().collect { settings ->
                interceptionSettings = InterceptionSettingsSnapshot(
                    selectedPackages = settings.selectedAppPackages,
                    bedtimeEnabled = settings.bedtimeEnabled,
                    bedtimeStartMinutes = settings.bedtimeStartMinutes,
                    bedtimeEndMinutes = settings.bedtimeEndMinutes,
                )
            }
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            return
        }
        val packageName = event.packageName?.toString()?.takeIf(String::isNotBlank) ?: return
        val className = event.className?.toString()
        val appContainer = (application as QualityAlternativeApplication).appContainer
        val settings = interceptionSettings
        val targetApp = InterceptionTargetResolver.resolve(
            foregroundPackage = packageName,
            foregroundClass = className,
            selectedPackages = settings.selectedPackages,
            appPackage = packageName(),
        ) ?: return
        val nowMillis = System.currentTimeMillis()
        val bedtimeActive = bedtimeWindowIsActive(
            enabled = settings.bedtimeEnabled,
            startMinutes = settings.bedtimeStartMinutes,
            endMinutes = settings.bedtimeEndMinutes,
            nowMillis = nowMillis,
        )
        if (InterceptionRuntimeGate.shouldSuppress(targetApp.packageName, nowMillis, bedtimeActive = bedtimeActive)) {
            return
        }
        if (!detectionPolicy.shouldLog(
                packageName = targetApp.packageName,
                selectedPackages = settings.selectedPackages,
                nowMillis = nowMillis,
                bedtimeActive = bedtimeActive,
            )
        ) {
            return
        }

        serviceScope.launch {
            appContainer.analyticsTracker.recordDurably(
                AnalyticsEvent(
                    type = AnalyticsEventType.TARGET_APP_FOREGROUND_DETECTED,
                    timestampMillis = nowMillis,
                    targetAppPackage = targetApp.packageName,
                    metadata = mapOf(
                        "triggerSource" to "accessibility_service",
                        "interceptionStage" to "sprint3_live_surface",
                        "foregroundPackage" to packageName,
                        "foregroundClass" to (className ?: ""),
                    ),
                ),
            )
        }

        if (!appContainer.interceptionMonitor.currentReadiness().interceptionReady) {
            return
        }
        startActivity(
            MainActivity.createSystemInterceptionIntent(
                context = this,
                targetAppPackage = targetApp.packageName,
                triggeredAtMillis = nowMillis,
            ),
        )
    }

    override fun onInterrupt() = Unit

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }

    private fun packageName(): String = applicationContext.packageName
}

private data class InterceptionSettingsSnapshot(
    val selectedPackages: Set<String> = emptySet(),
    val bedtimeEnabled: Boolean = DEFAULT_BEDTIME_ENABLED,
    val bedtimeStartMinutes: Int = DEFAULT_BEDTIME_START_MINUTES,
    val bedtimeEndMinutes: Int = DEFAULT_BEDTIME_END_MINUTES,
)
