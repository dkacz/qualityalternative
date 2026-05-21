package com.qualityalternative.app.interception

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ForegroundAppDetectionPolicyTest {
    @Test
    fun shouldLog_acceptsSelectedPackage_onFirstObservation() {
        val policy = ForegroundAppDetectionPolicy()

        val shouldLog = policy.shouldLog(
            packageName = "com.instagram.android",
            selectedPackages = setOf("com.instagram.android"),
            nowMillis = 1_000L,
        )

        assertTrue(shouldLog)
    }

    @Test
    fun shouldLog_suppressesFastDuplicateForSamePackage() {
        val policy = ForegroundAppDetectionPolicy()

        assertTrue(
            policy.shouldLog(
                packageName = "com.instagram.android",
                selectedPackages = setOf("com.instagram.android"),
                nowMillis = 1_000L,
            ),
        )
        assertFalse(
            policy.shouldLog(
                packageName = "com.instagram.android",
                selectedPackages = setOf("com.instagram.android"),
                nowMillis = 1_500L,
            ),
        )
    }

    @Test
    fun shouldLog_allowsSamePackageWhenForegroundEventCrossesIntoBedtime() {
        val policy = ForegroundAppDetectionPolicy()

        assertTrue(
            policy.shouldLog(
                packageName = "com.instagram.android",
                selectedPackages = setOf("com.instagram.android"),
                nowMillis = 1_000L,
                bedtimeActive = false,
            ),
        )
        assertTrue(
            policy.shouldLog(
                packageName = "com.instagram.android",
                selectedPackages = setOf("com.instagram.android"),
                nowMillis = 1_500L,
                bedtimeActive = true,
            ),
        )
    }

    @Test
    fun shouldLog_allowsSamePackageDuplicateDuringActiveBedtime() {
        val policy = ForegroundAppDetectionPolicy()

        assertTrue(
            policy.shouldLog(
                packageName = "com.instagram.android",
                selectedPackages = setOf("com.instagram.android"),
                nowMillis = 1_000L,
                bedtimeActive = true,
            ),
        )
        assertTrue(
            policy.shouldLog(
                packageName = "com.instagram.android",
                selectedPackages = setOf("com.instagram.android"),
                nowMillis = 1_500L,
                bedtimeActive = true,
            ),
        )
    }

    @Test
    fun shouldLog_allowsSamePackageAfterDifferentForegroundApp() {
        val policy = ForegroundAppDetectionPolicy()

        assertTrue(
            policy.shouldLog(
                packageName = "com.instagram.android",
                selectedPackages = setOf("com.instagram.android"),
                nowMillis = 1_000L,
            ),
        )
        assertFalse(
            policy.shouldLog(
                packageName = "com.android.launcher",
                selectedPackages = setOf("com.instagram.android"),
                nowMillis = 1_300L,
            ),
        )
        assertTrue(
            policy.shouldLog(
                packageName = "com.instagram.android",
                selectedPackages = setOf("com.instagram.android"),
                nowMillis = 1_600L,
            ),
        )
    }
}
