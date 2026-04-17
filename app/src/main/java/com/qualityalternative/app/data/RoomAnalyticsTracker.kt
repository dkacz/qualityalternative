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
import kotlinx.coroutines.launch
import org.json.JSONObject

class RoomAnalyticsTracker(
    private val dao: AnalyticsEventDao,
    private val scope: CoroutineScope,
) : AnalyticsTracker {
    private val events = MutableStateFlow<List<AnalyticsEvent>>(emptyList())

    init {
        scope.launch {
            val loaded = dao.getAll().map(AnalyticsEventEntity::toModel)
            if (events.value.isEmpty()) {
                events.value = loaded
            }
        }
    }

    override fun record(event: AnalyticsEvent) {
        events.value = (events.value + event).sortedByDescending(AnalyticsEvent::timestampMillis)
        scope.launch {
            dao.insert(event.toEntity())
        }
    }

    override fun allEvents(): List<AnalyticsEvent> = events.value

    override fun observeEvents(): Flow<List<AnalyticsEvent>> = events.asStateFlow()
}

private fun AnalyticsEvent.toEntity(): AnalyticsEventEntity {
    return AnalyticsEventEntity(
        type = type.name,
        timestampMillis = timestampMillis,
        targetAppPackage = targetAppPackage,
        contentId = contentId,
        metadataJson = JSONObject(metadata).toString(),
    )
}

private fun AnalyticsEventEntity.toModel(): AnalyticsEvent {
    return AnalyticsEvent(
        type = AnalyticsEventType.valueOf(type),
        timestampMillis = timestampMillis,
        targetAppPackage = targetAppPackage,
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
