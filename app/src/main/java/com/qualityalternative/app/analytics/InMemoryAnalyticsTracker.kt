package com.qualityalternative.app.analytics

import com.qualityalternative.app.domain.model.AnalyticsEvent
import com.qualityalternative.app.domain.service.AnalyticsTracker

class InMemoryAnalyticsTracker : AnalyticsTracker {
    private val events = mutableListOf<AnalyticsEvent>()

    override fun record(event: AnalyticsEvent) {
        events += event
    }

    override fun allEvents(): List<AnalyticsEvent> = events.toList().sortedByDescending(AnalyticsEvent::timestampMillis)
}
