package com.qualityalternative.app.data

import com.qualityalternative.app.data.local.AnalyticsEventDao
import com.qualityalternative.app.data.local.AnalyticsEventEntity
import com.qualityalternative.app.domain.model.AnalyticsEvent
import com.qualityalternative.app.domain.model.AnalyticsEventType
import com.qualityalternative.app.domain.service.AnalyticsTracker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.json.JSONObject

class RoomAnalyticsTracker(
    private val dao: AnalyticsEventDao,
    private val scope: CoroutineScope,
) : AnalyticsTracker {
    private val events = MutableStateFlow<List<AnalyticsEvent>>(emptyList())
    private val ready = MutableStateFlow(false)

    init {
        scope.launch {
            dao.observeMostRecent(MAX_RETAINED_ANALYTICS_EVENTS)
                .map { rows -> rows.map(AnalyticsEventEntity::toModel) }
                .collect { loadedEvents ->
                    events.value = loadedEvents
                    ready.value = true
                }
        }
    }

    override fun record(event: AnalyticsEvent) {
        scope.launch {
            insertAndPrune(event)
        }
    }

    override suspend fun recordDurably(event: AnalyticsEvent) {
        insertAndPrune(event)
    }

    private suspend fun insertAndPrune(event: AnalyticsEvent) {
        dao.insert(event.toEntity())
        dao.pruneToMostRecent(MAX_RETAINED_ANALYTICS_EVENTS)
    }

    override fun allEvents(): List<AnalyticsEvent> = events.value

    override fun observeEvents(): Flow<List<AnalyticsEvent>> = events.asStateFlow()

    override fun isReady(): Boolean = ready.value

    override fun observeReady(): Flow<Boolean> = ready.asStateFlow()

    private companion object {
        // Generous ceiling that bounds historical growth (and the per-emission remap cost) while
        // staying far above any realistic lifetime event count, so the Progress tab's lifetime
        // counters are not truncated in practice. ~20k rows is well under a few MB of in-memory state.
        const val MAX_RETAINED_ANALYTICS_EVENTS = 20_000
    }
}

private fun AnalyticsEvent.toEntity(): AnalyticsEventEntity {
    return AnalyticsEventEntity(
        type = type.name,
        timestampMillis = timestampMillis,
        semanticKey = semanticKey,
        interventionId = interventionId,
        sessionId = sessionId,
        targetAppPackage = targetAppPackage,
        primaryContentId = primaryContentId,
        backupContentIdsCsv = backupContentIds.joinToString(","),
        contentId = contentId,
        metadataJson = JSONObject(metadata).toString(),
    )
}

private fun AnalyticsEventEntity.toModel(): AnalyticsEvent {
    return AnalyticsEvent(
        type = AnalyticsEventType.valueOf(type),
        timestampMillis = timestampMillis,
        semanticKey = semanticKey,
        interventionId = interventionId,
        sessionId = sessionId,
        targetAppPackage = targetAppPackage,
        primaryContentId = primaryContentId,
        backupContentIds = backupContentIdsCsv.toStringList(),
        contentId = contentId,
        metadata = metadataJson.toMetadataMap(),
    )
}

private fun String.toMetadataMap(): Map<String, String> {
    if (isBlank()) {
        return emptyMap()
    }
    val json = JSONObject(this)
    return buildMap {
        json.keys().forEach { key ->
            put(key, json.optString(key))
        }
    }
}

private fun String.toStringList(): List<String> {
    if (isBlank()) {
        return emptyList()
    }
    return split(",").filter(String::isNotBlank)
}
