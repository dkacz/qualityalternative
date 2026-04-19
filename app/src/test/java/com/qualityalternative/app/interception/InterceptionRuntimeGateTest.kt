package com.qualityalternative.app.interception

import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class InterceptionRuntimeGateTest {
    @After
    fun tearDown() {
        InterceptionRuntimeGate.clearAll()
    }

    @Test
    fun shouldSuppress_returnsTrueWithinSuppressionWindow() {
        InterceptionRuntimeGate.suppressPackage(
            targetAppPackage = "com.instagram.android",
            untilMillis = 5_000L,
        )

        assertTrue(InterceptionRuntimeGate.shouldSuppress("com.instagram.android", nowMillis = 4_000L))
        assertFalse(InterceptionRuntimeGate.shouldSuppress("com.instagram.android", nowMillis = 5_000L))
    }
}
