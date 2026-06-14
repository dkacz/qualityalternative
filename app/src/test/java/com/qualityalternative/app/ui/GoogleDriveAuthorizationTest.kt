package com.qualityalternative.app.ui

import com.google.android.gms.auth.api.identity.AuthorizationRequest
import com.qualityalternative.app.domain.service.ANNOTATION_DRIVE_SCOPE
import org.junit.Assert.assertEquals
import org.junit.Test

class GoogleDriveAuthorizationTest {
    @Test
    fun pickerFolderAuthorizationUsesDriveFileWithExplicitPickerFolderGrant() {
        val spec = googleDriveAuthorizationRequestSpecFor(
            GoogleDriveAuthorizationMode.AGENT_INBOX_PICK_FOLDER,
        )

        assertEquals(listOf(ANNOTATION_DRIVE_SCOPE), spec.requestedScopes)
        assertEquals(true, spec.optOutIncludingGrantedScopes)
        assertEquals(AuthorizationRequest.Prompt.CONSENT, spec.prompt)
        assertEquals(
            GOOGLE_DRIVE_PICKER_TRUE,
            spec.resourceParameters[AuthorizationRequest.ResourceParameter.PICKER_OAUTH_TRIGGER],
        )
        assertEquals(
            GOOGLE_DRIVE_PICKER_TRUE,
            spec.resourceParameters[AuthorizationRequest.ResourceParameter.PICKER_ALLOW_FOLDER_SELECTION],
        )
    }

    @Test
    fun nonPickerAuthorizationDoesNotForcePickerFolderGrant() {
        val nonPickerModes = GoogleDriveAuthorizationMode.entries
            .filterNot { mode -> mode == GoogleDriveAuthorizationMode.AGENT_INBOX_PICK_FOLDER }

        nonPickerModes.forEach { mode ->
            val spec = googleDriveAuthorizationRequestSpecFor(mode)

            assertEquals(listOf(ANNOTATION_DRIVE_SCOPE), spec.requestedScopes)
            assertEquals(false, spec.optOutIncludingGrantedScopes)
            assertEquals(AuthorizationRequest.Prompt.NOT_SET, spec.prompt)
            assertEquals(emptyMap<AuthorizationRequest.ResourceParameter, String>(), spec.resourceParameters)
        }
    }

    @Test
    fun pickedDriveFileIdsParsesPickerTokenResponseParams() {
        assertEquals(
            listOf("folder-1", "folder-2"),
            pickedDriveFileIdsFromTokenResponseValue(" folder-1 , folder-2 ,, "),
        )
    }

    @Test
    fun pickedDriveFileIdsReturnsEmptyListWhenPickerParamsAreMissing() {
        assertEquals(emptyList<String>(), pickedDriveFileIdsFromTokenResponseValue(null))
    }
}
