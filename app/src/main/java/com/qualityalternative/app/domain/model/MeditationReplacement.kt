package com.qualityalternative.app.domain.model

const val MEDITATION_TIMER_CONTENT_ID = "qa-meditation-3-minute-reset"
const val MEDITATION_TIMER_PACK_ID = "quality-alternative-utilities"
const val DEFAULT_MEDITATION_MINUTES = 3

val MeditationTimerContentItem = ContentItem(
    id = MEDITATION_TIMER_CONTENT_ID,
    packId = MEDITATION_TIMER_PACK_ID,
    title = "3-minute reset",
    description = "A quiet timer for breathing through the impulse before choosing what comes next.",
    durationMinutes = DEFAULT_MEDITATION_MINUTES,
    format = ContentFormat.MARKDOWN,
    topicTags = setOf(TopicTag.PSYCHOLOGY),
    sourceLabel = "Quality Alternative",
    sourceType = ContentSourceType.MEDITATION,
    rights = ContentRightsMetadata.appUtility(),
)
