package com.qualityalternative.app.data

import com.qualityalternative.app.domain.model.DistractingApp
import com.qualityalternative.app.interception.FixtureTargetRegistry

object SupportedCatalog {
    val distractingApps: List<DistractingApp> = listOf(
        DistractingApp(packageName = "com.instagram.android", displayName = "Instagram"),
        DistractingApp(packageName = "com.twitter.android", displayName = "X"),
        DistractingApp(packageName = "com.google.android.youtube", displayName = "YouTube"),
        DistractingApp(packageName = "com.reddit.frontpage", displayName = "Reddit"),
        DistractingApp(packageName = "com.zhiliaoapp.musically", displayName = "TikTok"),
    )

    fun findByPackage(packageName: String): DistractingApp? {
        return distractingApps.firstOrNull { it.packageName == packageName }
            ?: FixtureTargetRegistry.findByPackage(packageName)
    }
}
