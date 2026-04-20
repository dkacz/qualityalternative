package com.qualityalternative.app.ui

import com.qualityalternative.app.domain.model.PermissionReadiness
import com.qualityalternative.app.domain.model.PermissionStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HomeHeroCopyTest {
    @Test
    fun readyInterceptionUsesCalmReadingSetupCopy() {
        val copy = homeHeroCopy(
            PermissionReadiness(
                overlayStatus = PermissionStatus.READY,
                accessibilityStatus = PermissionStatus.READY,
                interceptionReady = true,
                summary = "System intervention ready",
            ),
        )

        assertEquals("You're set up for quieter reading today.", copy.title)
        assertTrue(copy.body.contains("pause it"))
        assertTrue(copy.showAddLinkAction)
    }

    @Test
    fun missingInterceptionDoesNotClaimSetupIsReady() {
        val copy = homeHeroCopy(
            PermissionReadiness(
                overlayStatus = PermissionStatus.MISSING,
                accessibilityStatus = PermissionStatus.MISSING,
                interceptionReady = false,
                summary = "Setup still in progress",
            ),
        )

        assertEquals("Interception needs one more step.", copy.title)
        assertTrue(copy.body.contains("Finish the Android setup"))
        assertFalse(copy.showAddLinkAction)
    }
}
