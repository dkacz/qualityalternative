package com.qualityalternative.app.data

import com.qualityalternative.app.domain.model.PermissionReadiness
import com.qualityalternative.app.domain.model.PermissionStatus
import com.qualityalternative.app.domain.service.InterceptionMonitor

class StubInterceptionMonitor : InterceptionMonitor {
    override fun isAvailable(): Boolean = false

    override fun currentReadiness(): PermissionReadiness {
        return PermissionReadiness(
            overlayStatus = PermissionStatus.MISSING,
            accessibilityStatus = PermissionStatus.UNAVAILABLE_IN_BUILD,
            interceptionReady = false,
            summary = "Interception is not available in this build.",
        )
    }
}
