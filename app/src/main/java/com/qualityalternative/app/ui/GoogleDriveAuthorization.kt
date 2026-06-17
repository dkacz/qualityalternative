package com.qualityalternative.app.ui

import com.google.android.gms.auth.api.identity.AuthorizationRequest
import com.google.android.gms.common.api.Scope
import com.qualityalternative.app.domain.service.AGENT_INBOX_DRIVE_READONLY_SCOPE
import com.qualityalternative.app.domain.service.ANNOTATION_DRIVE_SCOPE

internal enum class GoogleDriveAuthorizationMode {
    ANNOTATION_CONNECT,
    ANNOTATION_RETRY,
    AGENT_INBOX_BROWSE_READONLY,
    AGENT_INBOX_CONNECT_READONLY,
    AGENT_INBOX_READONLY_SCAN,
    AGENT_INBOX_READONLY_IMPORT,
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
    return when (mode) {
        GoogleDriveAuthorizationMode.AGENT_INBOX_CONNECT_READONLY -> {
            GoogleDriveAuthorizationRequestSpec(
                requestedScopes = listOf(AGENT_INBOX_DRIVE_READONLY_SCOPE),
            )
        }

        GoogleDriveAuthorizationMode.AGENT_INBOX_BROWSE_READONLY -> {
            GoogleDriveAuthorizationRequestSpec(
                requestedScopes = listOf(AGENT_INBOX_DRIVE_READONLY_SCOPE),
            )
        }

        GoogleDriveAuthorizationMode.AGENT_INBOX_READONLY_SCAN,
        GoogleDriveAuthorizationMode.AGENT_INBOX_READONLY_IMPORT,
        -> {
            GoogleDriveAuthorizationRequestSpec(
                requestedScopes = listOf(AGENT_INBOX_DRIVE_READONLY_SCOPE),
            )
        }

        GoogleDriveAuthorizationMode.ANNOTATION_CONNECT,
        GoogleDriveAuthorizationMode.ANNOTATION_RETRY,
        -> {
            GoogleDriveAuthorizationRequestSpec(
                requestedScopes = listOf(ANNOTATION_DRIVE_SCOPE),
            )
        }
    }
}

internal fun googleDriveAuthorizationScopeFor(mode: GoogleDriveAuthorizationMode): String {
    return googleDriveAuthorizationRequestSpecFor(mode).requestedScopes.single()
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
