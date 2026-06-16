package com.qualityalternative.app.ui

import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.Status
import com.qualityalternative.app.domain.service.AGENT_INBOX_DRIVE_GRANT_MODE_DOCUMENT_TREE_FOLDER
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
            "Authorization was cancelled or blocked by Google. No folder destination was changed.",
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
            "Authorization was cancelled or blocked by Google. No folder destination was changed.",
            ApiException(Status(CommonStatusCodes.CANCELED)).googleDriveAuthMessage(),
        )
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

    @Test
    fun googleDriveDocumentTreeProviderIsRecognizedAsDriveConnectionFallback() {
        assertEquals(
            true,
            annotationExportUsesGoogleDriveProvider(
                "content://com.google.android.apps.docs.storage/tree/acc%3Duser%40example.com%3Bdoc%3Dfolder",
            ),
        )
        assertEquals(false, annotationExportUsesGoogleDriveProvider("content://com.android.externalstorage.documents/tree/home"))
        assertEquals(false, annotationExportUsesGoogleDriveProvider(null))
    }

    @Test
    fun agentInboxReadonlyDrivePickerIsUsedForGoogleDriveConfiguredEmptyInbox() {
        assertEquals(
            true,
            agentInboxShouldUseReadonlyDrivePicker(
                MainUiState(annotationDriveSyncEnabled = true),
            ),
        )
        assertEquals(
            true,
            agentInboxShouldUseReadonlyDrivePicker(
                MainUiState(
                    annotationExportUri = "content://com.google.android.apps.docs.storage/tree/acc%3Duser%3Bdoc%3Dannotations",
                ),
            ),
        )
        assertEquals(false, agentInboxShouldUseReadonlyDrivePicker(MainUiState()))
    }

    @Test
    fun legacyGoogleDriveDocumentTreeAgentInboxUsesReadonlyDrivePicker() {
        assertEquals(
            true,
            agentInboxShouldUseReadonlyDrivePicker(
                MainUiState(
                    agentInboxDriveEnabled = true,
                    agentInboxDriveFolderId = "content://com.google.android.apps.docs.storage/tree/acc%3Duser%40example.com%3Bdoc%3Dfolder",
                    agentInboxDriveGrantMode = AGENT_INBOX_DRIVE_GRANT_MODE_DOCUMENT_TREE_FOLDER,
                ),
            ),
        )
        assertEquals(
            false,
            agentInboxShouldUseReadonlyDrivePicker(
                MainUiState(
                    agentInboxDriveEnabled = true,
                    agentInboxDriveFolderId = "content://com.android.externalstorage.documents/tree/home",
                    agentInboxDriveGrantMode = AGENT_INBOX_DRIVE_GRANT_MODE_DOCUMENT_TREE_FOLDER,
                ),
            ),
        )
    }
}
