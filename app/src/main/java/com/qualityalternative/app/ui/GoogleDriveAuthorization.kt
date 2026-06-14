package com.qualityalternative.app.ui

import com.google.android.gms.auth.api.identity.AuthorizationRequest
import com.google.android.gms.auth.api.identity.AuthorizationResult
import com.google.android.gms.common.api.Scope
import com.qualityalternative.app.domain.service.ANNOTATION_DRIVE_SCOPE

internal enum class GoogleDriveAuthorizationMode {
    ANNOTATION_CONNECT,
    ANNOTATION_RETRY,
    AGENT_INBOX_PICK_FOLDER,
    AGENT_INBOX_SCAN,
    AGENT_INBOX_IMPORT,
}

internal data class GoogleDriveAuthorizationRequestSpec(
    val requestedScopes: List<String>,
    val optOutIncludingGrantedScopes: Boolean = false,
    @param:AuthorizationRequest.Prompt val prompt: Int = AuthorizationRequest.Prompt.NOT_SET,
    val resourceParameters: Map<AuthorizationRequest.ResourceParameter, String> = emptyMap(),
)

internal fun googleDriveAuthorizationRequestSpecFor(
    mode: GoogleDriveAuthorizationMode,
): GoogleDriveAuthorizationRequestSpec {
    return if (mode == GoogleDriveAuthorizationMode.AGENT_INBOX_PICK_FOLDER) {
        GoogleDriveAuthorizationRequestSpec(
            requestedScopes = listOf(ANNOTATION_DRIVE_SCOPE),
            optOutIncludingGrantedScopes = true,
            prompt = AuthorizationRequest.Prompt.CONSENT,
            resourceParameters = mapOf(
                AuthorizationRequest.ResourceParameter.PICKER_OAUTH_TRIGGER to GOOGLE_DRIVE_PICKER_TRUE,
                AuthorizationRequest.ResourceParameter.PICKER_ALLOW_FOLDER_SELECTION to GOOGLE_DRIVE_PICKER_TRUE,
            ),
        )
    } else {
        GoogleDriveAuthorizationRequestSpec(
            requestedScopes = listOf(ANNOTATION_DRIVE_SCOPE),
        )
    }
}

internal fun googleDriveAuthorizationRequestFor(
    mode: GoogleDriveAuthorizationMode,
): AuthorizationRequest {
    val spec = googleDriveAuthorizationRequestSpecFor(mode)
    val builder = AuthorizationRequest.builder()
        .setRequestedScopes(spec.requestedScopes.map(::Scope))
    if (spec.optOutIncludingGrantedScopes) {
        builder.setOptOutIncludingGrantedScopes(true)
    }
    if (spec.prompt != AuthorizationRequest.Prompt.NOT_SET) {
        builder.setPrompt(spec.prompt)
    }
    spec.resourceParameters.forEach { (parameter, value) ->
        builder.addResourceParameter(parameter, value)
    }
    return builder.build()
}

internal fun AuthorizationResult.pickedDriveFileIds(): List<String> {
    return pickedDriveFileIdsFromTokenResponseValue(
        getTokenResponseParams()?.getString(GOOGLE_DRIVE_PICKED_FILE_IDS),
    )
}

internal fun pickedDriveFileIdsFromTokenResponseValue(value: String?): List<String> {
    return value.orEmpty()
        .split(",")
        .map(String::trim)
        .filter(String::isNotBlank)
}

internal const val GOOGLE_DRIVE_PICKER_TRUE = "true"
internal const val GOOGLE_DRIVE_PICKED_FILE_IDS = "picked_file_ids"
