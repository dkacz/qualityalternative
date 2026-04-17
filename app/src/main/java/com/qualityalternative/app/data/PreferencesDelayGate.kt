package com.qualityalternative.app.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.qualityalternative.app.domain.model.DelayWindow
import com.qualityalternative.app.domain.model.DistractingApp
import com.qualityalternative.app.domain.service.DelayGate
import java.io.IOException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class PreferencesDelayGate(
    private val dataStore: DataStore<Preferences>,
    private val scope: CoroutineScope,
) : DelayGate {
    private val windows = MutableStateFlow<Map<String, DelayWindow>>(emptyMap())

    init {
        scope.launch(start = CoroutineStart.UNDISPATCHED) {
            dataStore.data
                .catch { exception ->
                    if (exception is IOException) {
                        emit(emptyPreferences())
                    } else {
                        throw exception
                    }
                }
                .collect { preferences ->
                    windows.value = preferences[DelayWindows]
                        .orEmpty()
                        .mapNotNull(::decodeWindow)
                        .associateBy(DelayWindow::targetAppPackage)
                }
        }
    }

    override fun activeDelay(targetApp: DistractingApp, nowMillis: Long): DelayWindow? {
        val current = windows.value[targetApp.packageName] ?: return null
        if (current.isActive(nowMillis)) {
            return current
        }
        val updated = windows.value - targetApp.packageName
        windows.value = updated
        persist(updated)
        return null
    }

    override fun storeDelay(targetApp: DistractingApp, nowMillis: Long, durationMinutes: Int): DelayWindow {
        val window = DelayWindow(
            targetAppPackage = targetApp.packageName,
            startsAtMillis = nowMillis,
            endsAtMillis = nowMillis + durationMinutes * 60_000L,
        )
        val updated = windows.value + (targetApp.packageName to window)
        windows.value = updated
        persist(updated)
        return window
    }

    private fun persist(currentWindows: Map<String, DelayWindow>) {
        scope.launch {
            dataStore.edit { preferences ->
                preferences[DelayWindows] = currentWindows.values.mapTo(mutableSetOf(), ::encodeWindow)
            }
        }
    }

    internal companion object {
        val DelayWindows = stringSetPreferencesKey("delay_windows")

        fun encodeWindow(window: DelayWindow): String {
            return listOf(
                window.targetAppPackage,
                window.startsAtMillis.toString(),
                window.endsAtMillis.toString(),
            ).joinToString("|")
        }

        fun decodeWindow(raw: String): DelayWindow? {
            val parts = raw.split("|")
            if (parts.size != 3) {
                return null
            }
            val startsAt = parts[1].toLongOrNull() ?: return null
            val endsAt = parts[2].toLongOrNull() ?: return null
            return DelayWindow(
                targetAppPackage = parts[0],
                startsAtMillis = startsAt,
                endsAtMillis = endsAt,
            )
        }
    }
}
