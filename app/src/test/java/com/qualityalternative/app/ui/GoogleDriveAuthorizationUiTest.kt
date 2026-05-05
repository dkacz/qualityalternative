package com.qualityalternative.app.ui

import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.Status
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class GoogleDriveAuthorizationUiTest {
    @Test
    fun authorizationResultWithIntentIsParsedEvenWhenResultCodeIsNotOk() {
        assertNull(
            googleDriveAuthorizationMissingResultMessage(
                resultCode = 123,
                hasResultIntent = true,
            ),
        )
    }

    @Test
    fun authorizationResultWithoutIntentDistinguishesCancelFromTechnicalFailure() {
        assertEquals(
            "Google Drive authorization was cancelled.",
            googleDriveAuthorizationMissingResultMessage(
                resultCode = 0,
                hasResultIntent = false,
            ),
        )
        assertEquals(
            "Google Drive authorization returned no result. Retry Google Drive connection.",
            googleDriveAuthorizationMissingResultMessage(
                resultCode = 123,
                hasResultIntent = false,
            ),
        )
    }

    @Test
    fun apiExceptionMessagesDistinguishConfigurationAndServiceFailures() {
        assertEquals(
            "Google Drive authorization is not configured for this app build.",
            ApiException(Status(CommonStatusCodes.DEVELOPER_ERROR, "bad client")).googleDriveAuthMessage(),
        )
        assertEquals(
            "Google Play services must be available and updated to connect Google Drive.",
            ApiException(Status(CommonStatusCodes.API_NOT_CONNECTED)).googleDriveAuthMessage(),
        )
        assertEquals(
            "Google Drive authorization could not reach Google services. Check connection and retry.",
            ApiException(Status(CommonStatusCodes.NETWORK_ERROR)).googleDriveAuthMessage(),
        )
    }
}
