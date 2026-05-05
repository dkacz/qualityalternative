package com.qualityalternative.app.data

import com.qualityalternative.app.domain.service.ANNOTATION_DRIVE_SCOPE
import java.io.IOException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class AndroidGoogleDriveTokenProviderTest {
    @Test
    fun driveAuthorizationAccessTokenAcceptsSilentDriveScopedToken() {
        assertEquals(
            "drive-token",
            googleDriveAuthorizationAccessToken(
                accessToken = "drive-token",
                grantedScopes = listOf(ANNOTATION_DRIVE_SCOPE),
                hasResolution = false,
            ),
        )
    }

    @Test
    fun driveAuthorizationAccessTokenRejectsMissingScopeOrResolutionRequirement() {
        assertRefreshRequired {
            googleDriveAuthorizationAccessToken(
                accessToken = "drive-token",
                grantedScopes = emptyList(),
                hasResolution = false,
            )
        }
        assertRefreshRequired {
            googleDriveAuthorizationAccessToken(
                accessToken = "drive-token",
                grantedScopes = listOf(ANNOTATION_DRIVE_SCOPE),
                hasResolution = true,
            )
        }
        assertRefreshRequired {
            googleDriveAuthorizationAccessToken(
                accessToken = null,
                grantedScopes = listOf(ANNOTATION_DRIVE_SCOPE),
                hasResolution = false,
            )
        }
    }

    private fun assertRefreshRequired(block: () -> Unit) {
        val error = assertThrows(IOException::class.java, block)
        assertEquals("Open Settings and tap Save now to refresh Google Drive access.", error.message)
    }
}
