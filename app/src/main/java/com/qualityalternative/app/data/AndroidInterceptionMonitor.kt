package com.qualityalternative.app.data

import android.accessibilityservice.AccessibilityService
import android.content.ComponentName
import android.content.Context
import android.provider.Settings
import android.text.TextUtils
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
    val enabledServices = Settings.Secure.getString(
        context.contentResolver,
        Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES,
    ).orEmpty()
    if (enabledServices.isBlank()) {
        return false
    }

    val expectedComponent = ComponentName(context, serviceClass)
    val expectedServices = setOf(
        expectedComponent.flattenToString(),
        expectedComponent.flattenToShortString(),
    )
    val splitter = TextUtils.SimpleStringSplitter(':').apply {
        setString(enabledServices)
    }
    while (splitter.hasNext()) {
        if (splitter.next() in expectedServices) {
            return true
        }
    }
    return false
}
