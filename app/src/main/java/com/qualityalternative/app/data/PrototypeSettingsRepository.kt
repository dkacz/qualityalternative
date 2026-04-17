package com.qualityalternative.app.data

import com.qualityalternative.app.domain.model.DistractingApp
import com.qualityalternative.app.domain.model.DurationBucket
import com.qualityalternative.app.domain.model.TopicTag
import com.qualityalternative.app.domain.model.UserPreferences
import com.qualityalternative.app.domain.service.SettingsRepository

class PrototypeSettingsRepository : SettingsRepository {
    private val apps = listOf(
        DistractingApp(packageName = "com.instagram.android", displayName = "Instagram"),
        DistractingApp(packageName = "com.twitter.android", displayName = "X"),
        DistractingApp(packageName = "com.google.android.youtube", displayName = "YouTube"),
        DistractingApp(packageName = "com.reddit.frontpage", displayName = "Reddit"),
        DistractingApp(packageName = "com.zhiliaoapp.musically", displayName = "TikTok"),
    )

    override fun currentPreferences(): UserPreferences = UserPreferences(
        selectedApps = apps,
        preferredTopics = setOf(TopicTag.PHILOSOPHY, TopicTag.SCIENCE, TopicTag.PSYCHOLOGY),
        preferredDurationBucket = DurationBucket.FOCUS,
    )

    override fun supportedDistractingApps(): List<DistractingApp> = apps
}
