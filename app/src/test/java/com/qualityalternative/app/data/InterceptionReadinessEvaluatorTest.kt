package com.qualityalternative.app.data

import com.qualityalternative.app.domain.model.PermissionStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class InterceptionReadinessEvaluatorTest {
    private val evaluator = InterceptionReadinessEvaluator()

    @Test
    fun evaluate_marksBothPermissionsMissing_whenNothingGranted() {
        val readiness = evaluator.evaluate(
            InterceptionPermissionSnapshot(
                overlayGranted = false,
                accessibilityGranted = false,
            ),
        )

        assertEquals(PermissionStatus.MISSING, readiness.overlayStatus)
        assertEquals(PermissionStatus.MISSING, readiness.accessibilityStatus)
        assertFalse(readiness.interceptionReady)
    }

    @Test
    fun evaluate_marksAccessibilityReady_whenOnlyAccessibilityGranted() {
        val readiness = evaluator.evaluate(
            InterceptionPermissionSnapshot(
                overlayGranted = false,
                accessibilityGranted = true,
            ),
        )

        assertEquals(PermissionStatus.MISSING, readiness.overlayStatus)
        assertEquals(PermissionStatus.READY, readiness.accessibilityStatus)
        assertEquals(
            "Foreground detection is active. Grant overlay permission next so the future intervention surface can appear over selected apps.",
            readiness.summary,
        )
    }

    @Test
    fun evaluate_marksOverlayReady_whenOnlyOverlayGranted() {
        val readiness = evaluator.evaluate(
            InterceptionPermissionSnapshot(
                overlayGranted = true,
                accessibilityGranted = false,
            ),
        )

        assertEquals(PermissionStatus.READY, readiness.overlayStatus)
        assertEquals(PermissionStatus.MISSING, readiness.accessibilityStatus)
        assertEquals(
            "Overlay permission is ready. Turn on the Accessibility Service so the app can detect when selected distracting apps reach the foreground.",
            readiness.summary,
        )
    }

    @Test
    fun evaluate_keepsInterceptionDisabled_whenBothPermissionsGranted() {
        val readiness = evaluator.evaluate(
            InterceptionPermissionSnapshot(
                overlayGranted = true,
                accessibilityGranted = true,
            ),
        )

        assertEquals(PermissionStatus.READY, readiness.overlayStatus)
        assertEquals(PermissionStatus.READY, readiness.accessibilityStatus)
        assertFalse(readiness.interceptionReady)
        assertEquals(
            "Foreground detection and overlay permissions are ready. This slice logs selected app opens; the live intervention surface lands next.",
            readiness.summary,
        )
    }
}
