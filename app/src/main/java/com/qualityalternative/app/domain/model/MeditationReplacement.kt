package com.qualityalternative.app.domain.model

const val MEDITATION_TIMER_CONTENT_ID = "qa-meditation-3-minute-reset"
const val MEDITATION_TIMER_PACK_ID = "quality-alternative-utilities"
const val DEFAULT_MEDITATION_MINUTES = 3
const val MIN_MEDITATION_MINUTES = 1
const val MAX_MEDITATION_MINUTES = 10

val MeditationTimerContentItem: ContentItem = meditationTimerContentItem(DEFAULT_MEDITATION_MINUTES)

fun meditationTimerContentItem(durationMinutes: Int): ContentItem {
    val safeMinutes = durationMinutes.coerceIn(MIN_MEDITATION_MINUTES, MAX_MEDITATION_MINUTES)
    return ContentItem(
        id = MEDITATION_TIMER_CONTENT_ID,
        packId = MEDITATION_TIMER_PACK_ID,
        title = "$safeMinutes-minute reset",
        description = "A quiet timer for breathing through the impulse before choosing what comes next.",
        durationMinutes = safeMinutes,
        format = ContentFormat.MARKDOWN,
        topicTags = setOf(TopicTag.PSYCHOLOGY),
        sourceLabel = "Quality Alternative",
        sourceType = ContentSourceType.MEDITATION,
        rights = ContentRightsMetadata.appUtility(),
    )
}
