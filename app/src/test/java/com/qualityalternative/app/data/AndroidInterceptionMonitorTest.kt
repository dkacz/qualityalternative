package com.qualityalternative.app.data

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AndroidInterceptionMonitorTest {
    @Test
    fun isAccessibilityServiceEnabled_returnsFalse_whenMasterSwitchIsOff() {
        val enabled = isAccessibilityServiceEnabled(
            accessibilityEnabled = false,
            enabledServices = "com.qualityalternative.app/.interception.QualityAlternativeAccessibilityService",
            expectedServices = setOf(
                "com.qualityalternative.app/com.qualityalternative.app.interception.QualityAlternativeAccessibilityService",
                "com.qualityalternative.app/.interception.QualityAlternativeAccessibilityService",
            ),
        )

        assertFalse(enabled)
    }

    @Test
    fun isAccessibilityServiceEnabled_returnsTrue_whenMasterSwitchIsOnAndServiceIsListed() {
        val enabled = isAccessibilityServiceEnabled(
            accessibilityEnabled = true,
            enabledServices = "com.example/.OtherService:com.qualityalternative.app/.interception.QualityAlternativeAccessibilityService",
            expectedServices = setOf(
                "com.qualityalternative.app/com.qualityalternative.app.interception.QualityAlternativeAccessibilityService",
                "com.qualityalternative.app/.interception.QualityAlternativeAccessibilityService",
            ),
        )

        assertTrue(enabled)
    }
}
