package com.qualityalternative.app.interception

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

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
    fun resolve_ignoresOwnPackageWhenForegroundComponentIsNotFixture() {
        val resolved = InterceptionTargetResolver.resolve(
            foregroundPackage = "com.qualityalternative.app",
            foregroundClass = "com.qualityalternative.app.MainActivity",
            selectedPackages = setOf("com.qualityalternative.fixture.one"),
            appPackage = "com.qualityalternative.app",
        )

        assertNull(resolved)
    }
}
