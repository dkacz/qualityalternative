package com.qualityalternative.app.ui

import com.google.android.gms.auth.api.identity.AuthorizationRequest
import com.qualityalternative.app.domain.service.AGENT_INBOX_DRIVE_READONLY_SCOPE
import com.qualityalternative.app.domain.service.ANNOTATION_DRIVE_SCOPE
import org.junit.Assert.assertEquals
import org.junit.Test

class GoogleDriveAuthorizationTest {
    @Test
    fun driveAuthorizationScopeConstantsUseExpectedGoogleDriveScopes() {
        assertEquals("https://www.googleapis.com/auth/drive.file", ANNOTATION_DRIVE_SCOPE)
        assertEquals("https://www.googleapis.com/auth/drive.readonly", AGENT_INBOX_DRIVE_READONLY_SCOPE)
    }

    @Test
    fun pickerFolderAuthorizationUsesDriveFileWithExplicitPickerFolderGrant() {
        val spec = googleDriveAuthorizationRequestSpecFor(
            GoogleDriveAuthorizationMode.AGENT_INBOX_PICK_FOLDER,
        )

        assertEquals(listOf(ANNOTATION_DRIVE_SCOPE), spec.requestedScopes)
        assertEquals(listOf("https://www.googleapis.com/auth/drive.file"), spec.requestedScopes)
        assertEquals(ANNOTATION_DRIVE_SCOPE, googleDriveAuthorizationScopeFor(GoogleDriveAuthorizationMode.AGENT_INBOX_PICK_FOLDER))
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
    fun driveFileAuthorizationDoesNotForcePickerFolderGrant() {
        listOf(
            GoogleDriveAuthorizationMode.ANNOTATION_CONNECT,
            GoogleDriveAuthorizationMode.ANNOTATION_RETRY,
            GoogleDriveAuthorizationMode.AGENT_INBOX_SCAN,
            GoogleDriveAuthorizationMode.AGENT_INBOX_IMPORT,
        ).forEach { mode ->
            val spec = googleDriveAuthorizationRequestSpecFor(mode)

            assertEquals(listOf(ANNOTATION_DRIVE_SCOPE), spec.requestedScopes)
            assertEquals(ANNOTATION_DRIVE_SCOPE, googleDriveAuthorizationScopeFor(mode))
            assertEquals(false, spec.optOutIncludingGrantedScopes)
            assertEquals(AuthorizationRequest.Prompt.NOT_SET, spec.prompt)
            assertEquals(emptyMap<AuthorizationRequest.ResourceParameter, String>(), spec.resourceParameters)
        }
    }

    @Test
    fun readonlyAgentInboxAuthorizationUsesReadonlyScopeWithoutPickerParameters() {
        listOf(
            GoogleDriveAuthorizationMode.AGENT_INBOX_CONNECT_READONLY,
            GoogleDriveAuthorizationMode.AGENT_INBOX_READONLY_SCAN,
            GoogleDriveAuthorizationMode.AGENT_INBOX_READONLY_IMPORT,
        ).forEach { mode ->
            val spec = googleDriveAuthorizationRequestSpecFor(mode)

            assertEquals(listOf(AGENT_INBOX_DRIVE_READONLY_SCOPE), spec.requestedScopes)
            assertEquals(listOf("https://www.googleapis.com/auth/drive.readonly"), spec.requestedScopes)
            assertEquals(AGENT_INBOX_DRIVE_READONLY_SCOPE, googleDriveAuthorizationScopeFor(mode))
            assertEquals(false, spec.optOutIncludingGrantedScopes)
            assertEquals(
                if (mode == GoogleDriveAuthorizationMode.AGENT_INBOX_CONNECT_READONLY) {
                    AuthorizationRequest.Prompt.CONSENT
                } else {
                    AuthorizationRequest.Prompt.NOT_SET
                },
                spec.prompt,
            )
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
