package com.qualityalternative.app.data

import com.qualityalternative.app.domain.model.DelayInspection
import com.qualityalternative.app.domain.model.DelayWindow
import com.qualityalternative.app.domain.model.DistractingApp
import com.qualityalternative.app.domain.service.DelayGate
import java.util.UUID

class InMemoryDelayGate : DelayGate {
    private val windows = mutableMapOf<String, DelayWindow>()

    override fun inspectDelay(targetApp: DistractingApp, nowMillis: Long): DelayInspection {
        val current = windows[targetApp.packageName] ?: return DelayInspection()
        return if (current.isActive(nowMillis)) {
            DelayInspection(activeWindow = current)
        } else {
            windows.remove(targetApp.packageName)
            DelayInspection(expiredWindow = current)
        }
    }

    override fun activeDelay(targetApp: DistractingApp, nowMillis: Long): DelayWindow? {
        return inspectDelay(targetApp = targetApp, nowMillis = nowMillis).activeWindow
    }

    override fun storeDelay(
        targetApp: DistractingApp,
        nowMillis: Long,
        durationMinutes: Int,
        interventionId: String?,
        interventionShownAtMillis: Long?,
        primaryContentId: String?,
        backupContentIds: List<String>,
    ): DelayWindow {
        val window = DelayWindow(
            id = UUID.randomUUID().toString(),
            targetAppPackage = targetApp.packageName,
            startsAtMillis = nowMillis,
            endsAtMillis = nowMillis + durationMinutes * 60_000L,
            interventionId = interventionId,
            interventionShownAtMillis = interventionShownAtMillis,
            primaryContentId = primaryContentId,
            backupContentIds = backupContentIds,
        )
        windows[targetApp.packageName] = window
        return window
    }

    override fun recordFirstReturnAttempt(targetApp: DistractingApp, nowMillis: Long): DelayWindow? {
        val current = windows[targetApp.packageName] ?: return null
        if (current.firstReturnAttemptAtMillis != null) {
            return null
        }
        val updated = current.copy(firstReturnAttemptAtMillis = nowMillis)
        windows[targetApp.packageName] = updated
        return updated
    }
}
