package com.qualityalternative.app.data

import android.accessibilityservice.AccessibilityService
import android.content.ComponentName
import android.content.Context
import android.provider.Settings
import com.qualityalternative.app.domain.model.PermissionReadiness
import com.qualityalternative.app.domain.service.InterceptionMonitor
import com.qualityalternative.app.interception.QualityAlternativeAccessibilityService

class AndroidInterceptionMonitor(
    private val context: Context,
    private val evaluator: InterceptionReadinessEvaluator = InterceptionReadinessEvaluator(),
) : InterceptionMonitor {
    override fun isAvailable(): Boolean = true

    override fun currentReadiness(): PermissionReadiness {
        return evaluator.evaluate(
            InterceptionPermissionSnapshot(
                overlayGranted = Settings.canDrawOverlays(context),
                accessibilityGranted = isAccessibilityServiceEnabled(
                    context = context,
                    serviceClass = QualityAlternativeAccessibilityService::class.java,
                ),
            ),
        )
    }
}

internal fun isAccessibilityServiceEnabled(
    context: Context,
    serviceClass: Class<out AccessibilityService>,
): Boolean {
    val accessibilityEnabled = Settings.Secure.getInt(
        context.contentResolver,
        Settings.Secure.ACCESSIBILITY_ENABLED,
        0,
    ) == 1
    if (!accessibilityEnabled) {
        return false
    }

    val enabledServices = Settings.Secure.getString(
        context.contentResolver,
        Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES,
    ).orEmpty()
    val expectedComponent = ComponentName(context, serviceClass)
    return isAccessibilityServiceEnabled(
        accessibilityEnabled = accessibilityEnabled,
        enabledServices = enabledServices,
        expectedServices = setOf(
            expectedComponent.flattenToString(),
            expectedComponent.flattenToShortString(),
        ),
    )
}

internal fun isAccessibilityServiceEnabled(
    accessibilityEnabled: Boolean,
    enabledServices: String,
    expectedServices: Set<String>,
): Boolean {
    if (!accessibilityEnabled) {
        return false
    }
    if (enabledServices.isBlank()) {
        return false
    }

    return enabledServices
        .split(':')
        .any { it in expectedServices }
}
