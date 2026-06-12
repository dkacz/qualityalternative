package com.qualityalternative.app.interception

import com.qualityalternative.app.BuildConfig
import com.qualityalternative.app.domain.model.DistractingApp

object FixtureTargetRegistry {
    private const val FIXTURE_ONE_PACKAGE = "com.qualityalternative.fixture.one"
    private const val FIXTURE_TWO_PACKAGE = "com.qualityalternative.fixture.two"

    private const val FIXTURE_ONE_COMPONENT =
        "com.qualityalternative.app.fixture.FixtureDistractorOneActivity"
    private const val FIXTURE_TWO_COMPONENT =
        "com.qualityalternative.app.fixture.FixtureDistractorTwoActivity"

    val fixtureDistractors: List<DistractingApp> = listOf(
        DistractingApp(
            packageName = FIXTURE_ONE_PACKAGE,
            displayName = "Fixture Feed One",
        ),
        DistractingApp(
            packageName = FIXTURE_TWO_PACKAGE,
            displayName = "Fixture Feed Two",
        ),
    )

    private val componentToDistractor = mapOf(
        FIXTURE_ONE_COMPONENT to fixtureDistractors[0],
        FIXTURE_TWO_COMPONENT to fixtureDistractors[1],
    )

    fun findByPackage(
        packageName: String,
        enabled: Boolean = BuildConfig.DEBUG,
    ): DistractingApp? {
        if (!enabled) return null
        return fixtureDistractors.firstOrNull { it.packageName == packageName }
    }

    fun findByComponent(
        componentName: String?,
        enabled: Boolean = BuildConfig.DEBUG,
    ): DistractingApp? {
        if (!enabled) return null
        return componentToDistractor[componentName]
    }
}
