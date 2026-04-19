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
            interceptionReady = snapshot.accessibilityGranted,
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
                "System intervention is ready. Accessibility can interrupt selected app opens now, and overlay permission is also available for future floating-surface experiments."

            accessibilityGranted ->
                "System intervention is ready through Accessibility. Overlay permission is optional for later floating-surface experiments, but the current alpha can already interrupt selected app opens."

            overlayGranted ->
                "Overlay permission is available, but interception is still off. Turn on the Accessibility Service so the app can detect and interrupt selected distracting app opens."

            else ->
                "Turn on the Accessibility Service to activate system interception for selected distracting apps."
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
