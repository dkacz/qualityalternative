package com.qualityalternative.app.data

import com.qualityalternative.app.domain.model.DelayWindow
import com.qualityalternative.app.domain.model.DistractingApp
import com.qualityalternative.app.domain.service.DelayGate

class InMemoryDelayGate : DelayGate {
    private val windows = mutableMapOf<String, DelayWindow>()

    override fun activeDelay(targetApp: DistractingApp, nowMillis: Long): DelayWindow? {
        val current = windows[targetApp.packageName] ?: return null
        return current.takeIf { it.isActive(nowMillis) }
    }

    override fun storeDelay(targetApp: DistractingApp, nowMillis: Long, durationMinutes: Int): DelayWindow {
        val window = DelayWindow(
            targetAppPackage = targetApp.packageName,
            startsAtMillis = nowMillis,
            endsAtMillis = nowMillis + durationMinutes * 60_000L,
        )
        windows[targetApp.packageName] = window
        return window
    }
}
