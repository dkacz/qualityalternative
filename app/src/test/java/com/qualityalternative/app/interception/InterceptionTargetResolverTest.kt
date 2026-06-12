package com.qualityalternative.app.interception

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import com.qualityalternative.app.domain.model.DistractingApp

class InterceptionTargetResolverTest {
    @Test
    fun resolve_returnsRealTargetWhenSelectedPackageMatchesForegroundPackage() {
        val resolved = InterceptionTargetResolver.resolve(
            foregroundPackage = "com.instagram.android",
            foregroundClass = "com.instagram.mainactivity.InstagramMainActivity",
            selectedPackages = setOf("com.instagram.android"),
            appPackage = "com.qualityalternative.app",
        )

        assertEquals("com.instagram.android", resolved?.packageName)
    }

    @Test
    fun resolve_returnsFixtureTargetWhenFixtureComponentMatchesOwnPackage() {
        val resolved = InterceptionTargetResolver.resolve(
            foregroundPackage = "com.qualityalternative.app",
            foregroundClass = "com.qualityalternative.app.fixture.FixtureDistractorOneActivity",
            selectedPackages = setOf("com.qualityalternative.fixture.one"),
            appPackage = "com.qualityalternative.app",
        )

        assertEquals("com.qualityalternative.fixture.one", resolved?.packageName)
    }

    @Test
    fun resolve_ignoresFixtureTargetsWhenFixtureGateIsDisabled() {
        val ownPackageResolved = InterceptionTargetResolver.resolve(
            foregroundPackage = "com.qualityalternative.app",
            foregroundClass = "com.qualityalternative.app.fixture.FixtureDistractorOneActivity",
            selectedPackages = setOf("com.qualityalternative.fixture.one"),
            appPackage = "com.qualityalternative.app",
            enableFixtureTargets = false,
        )
        val fixturePackageResolved = InterceptionTargetResolver.resolve(
            foregroundPackage = "com.qualityalternative.fixture.one",
            foregroundClass = "com.qualityalternative.app.fixture.FixtureDistractorOneActivity",
            selectedPackages = setOf("com.qualityalternative.fixture.one"),
            appPackage = "com.qualityalternative.app",
            enableFixtureTargets = false,
        )

        assertNull(ownPackageResolved)
        assertNull(fixturePackageResolved)
    }

    @Test
    fun resolve_ignoresOwnPackageWhenForegroundComponentIsNotFixture() {
        val resolved = InterceptionTargetResolver.resolve(
            foregroundPackage = "com.qualityalternative.app",
            foregroundClass = "com.qualityalternative.app.MainActivity",
            selectedPackages = setOf("com.qualityalternative.fixture.one"),
            appPackage = "com.qualityalternative.app",
        )

        assertNull(resolved)
    }

    @Test
    fun resolve_returnsCustomTargetWhenSelectedPackageMatchesKnownTarget() {
        val customTarget = DistractingApp(
            packageName = "com.example.deepwork",
            displayName = "Deep Work Trap",
        )

        val resolved = InterceptionTargetResolver.resolve(
            foregroundPackage = customTarget.packageName,
            foregroundClass = "com.example.deepwork.MainActivity",
            selectedPackages = setOf(customTarget.packageName),
            knownTargets = listOf(customTarget),
            appPackage = "com.qualityalternative.app",
        )

        assertEquals(customTarget, resolved)
    }
}
