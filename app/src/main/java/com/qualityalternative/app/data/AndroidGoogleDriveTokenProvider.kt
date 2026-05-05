package com.qualityalternative.app.data

import android.content.Context
import com.google.android.gms.auth.api.identity.AuthorizationRequest
import com.google.android.gms.auth.api.identity.AuthorizationResult
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.common.api.Scope
import com.google.android.gms.tasks.Task
import com.qualityalternative.app.domain.service.ANNOTATION_DRIVE_SCOPE
import com.qualityalternative.app.domain.service.ReadingAnnotationDriveTokenProvider
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine

class AndroidGoogleDriveTokenProvider internal constructor(
    private val taskSource: GoogleDriveAuthorizationTaskSource,
) : ReadingAnnotationDriveTokenProvider {
    constructor(context: Context) : this(
        GoogleDriveAuthorizationTaskSource { request ->
            Identity.getAuthorizationClient(context.applicationContext).authorize(request)
        },
    )

    override suspend fun driveAccessToken(): String = suspendCancellableCoroutine { continuation ->
        val request = AuthorizationRequest.builder()
            .setRequestedScopes(listOf(Scope(ANNOTATION_DRIVE_SCOPE)))
            .build()
        taskSource.authorize(request)
            .addOnSuccessListener { result ->
                runCatching {
                    googleDriveAuthorizationAccessToken(
                        accessToken = result.accessToken,
                        grantedScopes = result.grantedScopes,
                        hasResolution = result.hasResolution(),
                    )
                }.onSuccess { token ->
                    continuation.resume(token)
                }.onFailure { error ->
                    continuation.resumeWithException(error)
                }
            }
            .addOnFailureListener { error ->
                continuation.resumeWithException(error)
            }
    }
}

internal fun interface GoogleDriveAuthorizationTaskSource {
    fun authorize(request: AuthorizationRequest): Task<AuthorizationResult>
}

internal fun googleDriveAuthorizationAccessToken(
    accessToken: String?,
    grantedScopes: Collection<String>?,
    hasResolution: Boolean,
): String {
    val token = accessToken?.takeIf(String::isNotBlank)
    val hasDriveScope = grantedScopes.orEmpty().contains(ANNOTATION_DRIVE_SCOPE)
    if (!hasResolution && token != null && hasDriveScope) {
        return token
    }
    throw IOException("Open Settings and tap Save now to refresh Google Drive access.")
}
