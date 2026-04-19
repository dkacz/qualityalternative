package com.qualityalternative.app.data

import com.qualityalternative.app.domain.model.PermissionReadiness
import com.qualityalternative.app.domain.model.PermissionStatus

data class InterceptionPermissionSnapshot(
    val overlayGranted: Boolean,
    val accessibilityGranted: Boolean,
)

class InterceptionReadinessEvaluator {
    fun evaluate(snapshot: InterceptionPermissionSnapshot): PermissionReadiness {
        val overlayStatus = snapshot.overlayGranted.toPermissionStatus()
        val accessibilityStatus = snapshot.accessibilityGranted.toPermissionStatus()
        return PermissionReadiness(
            overlayStatus = overlayStatus,
            accessibilityStatus = accessibilityStatus,
            interceptionReady = false,
            summary = summaryFor(
                overlayGranted = snapshot.overlayGranted,
                accessibilityGranted = snapshot.accessibilityGranted,
            ),
        )
    }

    private fun summaryFor(
        overlayGranted: Boolean,
        accessibilityGranted: Boolean,
    ): String {
        return when {
            accessibilityGranted && overlayGranted ->
                "Foreground detection and overlay permissions are ready. This slice logs selected app opens; the live intervention surface lands next."

            accessibilityGranted ->
                "Foreground detection is active. Grant overlay permission next so the future intervention surface can appear over selected apps."

            overlayGranted ->
                "Overlay permission is ready. Turn on the Accessibility Service so the app can detect when selected distracting apps reach the foreground."

            else ->
                "Turn on the Accessibility Service to detect selected app opens, then grant overlay permission for the upcoming live intervention surface."
        }
    }
}

private fun Boolean.toPermissionStatus(): PermissionStatus {
    return if (this) {
        PermissionStatus.READY
    } else {
        PermissionStatus.MISSING
    }
}
