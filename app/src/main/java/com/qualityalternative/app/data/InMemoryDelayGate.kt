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
            DelayInspection(expiredWindow = current)
        }
    }

    override fun activeDelay(targetApp: DistractingApp, nowMillis: Long): DelayWindow? {
        val current = windows[targetApp.packageName] ?: return null
        return current.takeIf { it.isActive(nowMillis) }
    }

    override suspend fun consumeExpiredDelay(targetApp: DistractingApp, delayId: String, nowMillis: Long): Boolean {
        val current = windows[targetApp.packageName] ?: return false
        if (current.id != delayId || current.isActive(nowMillis)) {
            return false
        }
        windows.remove(targetApp.packageName)
        return true
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
        return createWindow(
            targetApp = targetApp,
            nowMillis = nowMillis,
            durationMinutes = durationMinutes,
            interventionId = interventionId,
            interventionShownAtMillis = interventionShownAtMillis,
            primaryContentId = primaryContentId,
            backupContentIds = backupContentIds,
        )
    }

    override suspend fun storeDelayDurably(
        targetApp: DistractingApp,
        nowMillis: Long,
        durationMinutes: Int,
        interventionId: String?,
        interventionShownAtMillis: Long?,
        primaryContentId: String?,
        backupContentIds: List<String>,
    ): DelayWindow {
        return createWindow(
            targetApp = targetApp,
            nowMillis = nowMillis,
            durationMinutes = durationMinutes,
            interventionId = interventionId,
            interventionShownAtMillis = interventionShownAtMillis,
            primaryContentId = primaryContentId,
            backupContentIds = backupContentIds,
        )
    }

    private fun createWindow(
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
        return updateFirstReturnAttempt(targetApp = targetApp, nowMillis = nowMillis)
    }

    override suspend fun recordFirstReturnAttemptDurably(targetApp: DistractingApp, nowMillis: Long): DelayWindow? {
        return updateFirstReturnAttempt(targetApp = targetApp, nowMillis = nowMillis)
    }

    private fun updateFirstReturnAttempt(targetApp: DistractingApp, nowMillis: Long): DelayWindow? {
        val current = windows[targetApp.packageName] ?: return null
        if (current.firstReturnAttemptAtMillis != null) {
            return null
        }
        val updated = current.copy(firstReturnAttemptAtMillis = nowMillis)
        windows[targetApp.packageName] = updated
        return updated
    }
}
