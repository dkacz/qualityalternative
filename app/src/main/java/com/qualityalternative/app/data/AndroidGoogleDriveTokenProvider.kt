package com.qualityalternative.app.data

import android.content.Context
import com.google.android.gms.auth.api.identity.AuthorizationRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.common.api.Scope
import com.qualityalternative.app.domain.service.ANNOTATION_DRIVE_SCOPE
import com.qualityalternative.app.domain.service.ReadingAnnotationDriveTokenProvider
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine

class AndroidGoogleDriveTokenProvider(
    context: Context,
) : ReadingAnnotationDriveTokenProvider {
    private val appContext = context.applicationContext

    override suspend fun driveAccessToken(): String = suspendCancellableCoroutine { continuation ->
        val request = AuthorizationRequest.builder()
            .setRequestedScopes(listOf(Scope(ANNOTATION_DRIVE_SCOPE)))
            .build()
        Identity.getAuthorizationClient(appContext)
            .authorize(request)
            .addOnSuccessListener { result ->
                val token = result.accessToken?.takeIf(String::isNotBlank)
                val hasDriveScope = result.grantedScopes.orEmpty().contains(ANNOTATION_DRIVE_SCOPE)
                if (!result.hasResolution() && token != null && hasDriveScope) {
                    continuation.resume(token)
                } else {
                    continuation.resumeWithException(
                        IOException("Open Settings and tap Save now to refresh Google Drive access."),
                    )
                }
            }
            .addOnFailureListener { error ->
                continuation.resumeWithException(error)
            }
    }
}
