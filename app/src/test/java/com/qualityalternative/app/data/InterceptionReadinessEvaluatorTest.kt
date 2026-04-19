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
        assertEquals(
            "Turn on the Accessibility Service to activate system interception for selected distracting apps.",
            readiness.summary,
        )
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
        assertEquals(true, readiness.interceptionReady)
        assertEquals(
            "System intervention is ready through Accessibility. Overlay permission is optional for later floating-surface experiments, but the current alpha can already interrupt selected app opens.",
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
            "Overlay permission is available, but interception is still off. Turn on the Accessibility Service so the app can detect and interrupt selected distracting app opens.",
            readiness.summary,
        )
    }

    @Test
    fun evaluate_marksSystemInterceptionReady_whenBothPermissionsGranted() {
        val readiness = evaluator.evaluate(
            InterceptionPermissionSnapshot(
                overlayGranted = true,
                accessibilityGranted = true,
            ),
        )

        assertEquals(PermissionStatus.READY, readiness.overlayStatus)
        assertEquals(PermissionStatus.READY, readiness.accessibilityStatus)
        assertEquals(true, readiness.interceptionReady)
        assertEquals(
            "System intervention is ready. Accessibility can interrupt selected app opens now, and overlay permission is also available for future floating-surface experiments.",
            readiness.summary,
        )
    }
}
