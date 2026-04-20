package com.qualityalternative.app.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.qualityalternative.app.domain.model.DelayInspection
import com.qualityalternative.app.domain.model.DelayWindow
import com.qualityalternative.app.domain.model.DistractingApp
import com.qualityalternative.app.domain.service.DelayGate
import java.io.IOException
import java.util.Base64
import java.util.UUID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class PreferencesDelayGate(
    private val dataStore: DataStore<Preferences>,
    private val scope: CoroutineScope,
) : DelayGate {
    private val windows = MutableStateFlow<Map<String, DelayWindow>>(emptyMap())
    private val ready = MutableStateFlow(false)

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
                    ready.value = true
                }
        }
    }

    override fun inspectDelay(targetApp: DistractingApp, nowMillis: Long): DelayInspection {
        val current = windows.value[targetApp.packageName] ?: return DelayInspection()
        if (current.isActive(nowMillis)) {
            return DelayInspection(activeWindow = current)
        }
        return DelayInspection(expiredWindow = current)
    }

    override fun activeDelay(targetApp: DistractingApp, nowMillis: Long): DelayWindow? {
        val current = windows.value[targetApp.packageName] ?: return null
        return current.takeIf { it.isActive(nowMillis) }
    }

    override suspend fun consumeExpiredDelay(targetApp: DistractingApp, delayId: String, nowMillis: Long): Boolean {
        val current = windows.value[targetApp.packageName] ?: return false
        if (current.id != delayId || current.isActive(nowMillis)) {
            return false
        }
        val updated = windows.value - targetApp.packageName
        windows.value = updated
        persistDurably(updated)
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
        val window = createWindow(
            targetApp = targetApp,
            nowMillis = nowMillis,
            durationMinutes = durationMinutes,
            interventionId = interventionId,
            interventionShownAtMillis = interventionShownAtMillis,
            primaryContentId = primaryContentId,
            backupContentIds = backupContentIds,
        )
        val updated = windows.value + (targetApp.packageName to window)
        windows.value = updated
        persist(updated)
        return window
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
        val window = createWindow(
            targetApp = targetApp,
            nowMillis = nowMillis,
            durationMinutes = durationMinutes,
            interventionId = interventionId,
            interventionShownAtMillis = interventionShownAtMillis,
            primaryContentId = primaryContentId,
            backupContentIds = backupContentIds,
        )
        val updated = windows.value + (targetApp.packageName to window)
        windows.value = updated
        persistDurably(updated)
        return window
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
        return window
    }

    override fun recordFirstReturnAttempt(targetApp: DistractingApp, nowMillis: Long): DelayWindow? {
        val updated = updateFirstReturnAttempt(targetApp = targetApp, nowMillis = nowMillis) ?: return null
        persist(windows.value)
        return updated
    }

    override suspend fun recordFirstReturnAttemptDurably(targetApp: DistractingApp, nowMillis: Long): DelayWindow? {
        val updated = updateFirstReturnAttempt(targetApp = targetApp, nowMillis = nowMillis) ?: return null
        persistDurably(windows.value)
        return updated
    }

    private fun updateFirstReturnAttempt(targetApp: DistractingApp, nowMillis: Long): DelayWindow? {
        val current = windows.value[targetApp.packageName] ?: return null
        if (current.firstReturnAttemptAtMillis != null) {
            return null
        }
        val updated = current.copy(firstReturnAttemptAtMillis = nowMillis)
        val updatedWindows = windows.value + (targetApp.packageName to updated)
        windows.value = updatedWindows
        return updated
    }

    override fun isReady(): Boolean = ready.value

    override fun observeReady(): Flow<Boolean> = ready.asStateFlow()

    suspend fun clearForTests() {
        windows.value = emptyMap()
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }

    private fun persist(currentWindows: Map<String, DelayWindow>) {
        scope.launch {
            persistDurably(currentWindows)
        }
    }

    private suspend fun persistDurably(currentWindows: Map<String, DelayWindow>) {
        dataStore.edit { preferences ->
            preferences[DelayWindows] = currentWindows.values.mapTo(mutableSetOf(), ::encodeWindow)
        }
    }

    internal companion object {
        val DelayWindows = stringSetPreferencesKey("delay_windows")
        private const val WINDOW_ENCODING_VERSION = "v2"

        fun encodeWindow(window: DelayWindow): String {
            return listOf(
                WINDOW_ENCODING_VERSION,
                encodeField(window.id),
                encodeField(window.targetAppPackage),
                window.startsAtMillis.toString(),
                window.endsAtMillis.toString(),
                encodeField(window.interventionId),
                encodeNullableLong(window.interventionShownAtMillis),
                encodeField(window.primaryContentId),
                encodeField(window.backupContentIds.joinToString(",")),
                encodeNullableLong(window.firstReturnAttemptAtMillis),
            ).joinToString("|")
        }

        fun decodeWindow(raw: String): DelayWindow? {
            val parts = raw.split('|')
            if (parts.firstOrNull() == WINDOW_ENCODING_VERSION && parts.size == 10) {
                return DelayWindow(
                    id = decodeField(parts[1]),
                    targetAppPackage = decodeField(parts[2]),
                    startsAtMillis = parts[3].toLongOrNull() ?: return null,
                    endsAtMillis = parts[4].toLongOrNull() ?: return null,
                    interventionId = decodeField(parts[5]).ifBlank { null },
                    interventionShownAtMillis = decodeNullableLong(parts[6]),
                    primaryContentId = decodeField(parts[7]).ifBlank { null },
                    backupContentIds = decodeField(parts[8]).split(",").filter(String::isNotBlank),
                    firstReturnAttemptAtMillis = decodeNullableLong(parts[9]),
                ).takeIf { it.id.isNotBlank() && it.targetAppPackage.isNotBlank() }
            }

            if (parts.size != 4) {
                return null
            }
            val startsAt = parts[2].toLongOrNull() ?: return null
            val endsAt = parts[3].toLongOrNull() ?: return null
            return DelayWindow(
                id = parts[0],
                targetAppPackage = parts[1],
                startsAtMillis = startsAt,
                endsAtMillis = endsAt,
            )
        }
    }
}

private fun encodeField(value: String?): String {
    val bytes = (value ?: "").toByteArray(Charsets.UTF_8)
    return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
}

private fun decodeField(value: String): String {
    if (value.isBlank()) {
        return ""
    }
    return String(Base64.getUrlDecoder().decode(value), Charsets.UTF_8)
}

private fun encodeNullableLong(value: Long?): String = value?.toString().orEmpty()

private fun decodeNullableLong(value: String): Long? = value.toLongOrNull()
