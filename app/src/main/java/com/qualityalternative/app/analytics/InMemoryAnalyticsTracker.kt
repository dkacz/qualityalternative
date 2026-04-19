package com.qualityalternative.app.analytics

import com.qualityalternative.app.domain.model.AnalyticsEvent
import com.qualityalternative.app.domain.service.AnalyticsTracker
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class InMemoryAnalyticsTracker : AnalyticsTracker {
    private val events = MutableStateFlow<List<AnalyticsEvent>>(emptyList())

    override fun record(event: AnalyticsEvent) {
        val current = events.value
        if (event.semanticKey != null && current.any { it.semanticKey == event.semanticKey }) {
            return
        }
        events.value = (current + event).sortedByDescending(AnalyticsEvent::timestampMillis)
    }

    override fun allEvents(): List<AnalyticsEvent> = events.value

    override fun observeEvents(): Flow<List<AnalyticsEvent>> = events.asStateFlow()
}
