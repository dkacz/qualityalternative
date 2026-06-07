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
import com.qualityalternative.app.domain.model.DistractingApp
import com.qualityalternative.app.domain.model.PermissionReadiness
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
    // Permission readiness is read from Settings/ContentResolver (three reads). Caching it off the main
    // thread keeps onAccessibilityEvent (which runs on the main thread, and in bedtime mode without
    // duplicate suppression) free of that blocking I/O. Null means "not yet evaluated", which safely
    // blocks an intervention until the first refresh lands.
    @Volatile
    private var cachedReadiness: PermissionReadiness? = null

    override fun onServiceConnected() {
        super.onServiceConnected()
        val appContainer = (application as QualityAlternativeApplication).appContainer
        serviceScope.launch {
            cachedReadiness = appContainer.interceptionMonitor.currentReadiness()
        }
        serviceScope.launch {
            appContainer.settingsRepository.observeAppSettings().collect { settings ->
                interceptionSettings = InterceptionSettingsSnapshot(
                    selectedPackages = settings.selectedAppPackages,
                    knownTargets = appContainer.settingsRepository.supportedDistractingApps(),
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
            knownTargets = settings.knownTargets,
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
            // Refresh the readiness cache off the main thread as part of event processing, so the next
            // event's synchronous gate sees a fresh snapshot without ever blocking the event callback.
            cachedReadiness = appContainer.interceptionMonitor.currentReadiness()
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

        if (cachedReadiness?.interceptionReady != true) {
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
    val knownTargets: List<DistractingApp> = emptyList(),
    val bedtimeEnabled: Boolean = DEFAULT_BEDTIME_ENABLED,
    val bedtimeStartMinutes: Int = DEFAULT_BEDTIME_START_MINUTES,
    val bedtimeEndMinutes: Int = DEFAULT_BEDTIME_END_MINUTES,
)
