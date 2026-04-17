package com.qualityalternative.app.data

import android.content.Context
import android.provider.Settings
import com.qualityalternative.app.domain.model.PermissionReadiness
import com.qualityalternative.app.domain.model.PermissionStatus
import com.qualityalternative.app.domain.service.InterceptionMonitor

class BuildStageInterceptionMonitor(
    private val context: Context,
) : InterceptionMonitor {
    override fun isAvailable(): Boolean = false

    override fun currentReadiness(): PermissionReadiness {
        val overlayStatus = if (Settings.canDrawOverlays(context)) {
            PermissionStatus.READY
        } else {
            PermissionStatus.MISSING
        }

        return PermissionReadiness(
            overlayStatus = overlayStatus,
            accessibilityStatus = PermissionStatus.UNAVAILABLE_IN_BUILD,
            interceptionReady = false,
            summary = "System interception is still unavailable in this build. Sprint 2 exposes the manual replacement loop plus readiness signals, but not live app interception.",
        )
    }
}
