package com.qualityalternative.app.data

import com.qualityalternative.app.domain.model.UserLinkDraft
import com.qualityalternative.app.domain.model.UserLinkValidationError
import com.qualityalternative.app.domain.model.UserLinkValidationResult
import java.net.URI

object UserLinkValidator {
    fun validate(draft: UserLinkDraft): UserLinkValidationResult {
        val errors = mutableSetOf<UserLinkValidationError>()
        val urlValidation = validateUrl(draft.url)
        val normalizedUrl = urlValidation.normalizedUrl

        errors += urlValidation.errors
        if (draft.title.isBlank()) {
            errors += UserLinkValidationError.BLANK_TITLE
        }
        if (draft.durationMinutes !in MIN_DURATION_MINUTES..MAX_DURATION_MINUTES) {
            errors += UserLinkValidationError.INVALID_DURATION
        }
        if (draft.topicTags.isEmpty()) {
            errors += UserLinkValidationError.NO_TOPICS
        }

        return UserLinkValidationResult(
            normalizedUrl = normalizedUrl.takeIf { errors.isEmpty() },
            errors = errors,
        )
    }

    fun validateUrl(rawUrl: String): UserLinkValidationResult {
        val trimmed = rawUrl.trim()
        if (trimmed.isBlank()) {
            return UserLinkValidationResult(errors = setOf(UserLinkValidationError.EMPTY_URL))
        }

        val parsed = runCatching { URI(trimmed) }.getOrNull()
            ?: return UserLinkValidationResult(errors = setOf(malformedUrlError(trimmed)))
        val scheme = parsed.scheme?.lowercase()
            ?: return UserLinkValidationResult(errors = setOf(UserLinkValidationError.UNSUPPORTED_SCHEME))
        if (scheme !in SUPPORTED_SCHEMES) {
            return UserLinkValidationResult(errors = setOf(UserLinkValidationError.UNSUPPORTED_SCHEME))
        }

        val host = parsed.host?.lowercase()
            ?: return UserLinkValidationResult(errors = setOf(UserLinkValidationError.MISSING_HOST))
        val normalizedUrl = buildNormalizedUrl(
            scheme = scheme,
            rawUserInfo = parsed.rawUserInfo,
            host = host,
            port = parsed.port,
            rawPath = parsed.rawPath,
            rawQuery = parsed.rawQuery,
            rawFragment = parsed.rawFragment,
        )
        return UserLinkValidationResult(
            normalizedUrl = normalizedUrl,
        )
    }

    private fun malformedUrlError(rawUrl: String): UserLinkValidationError {
        val lower = rawUrl.lowercase()
        return if (lower == "http://" || lower == "https://") {
            UserLinkValidationError.MISSING_HOST
        } else {
            UserLinkValidationError.UNSUPPORTED_SCHEME
        }
    }

    private fun buildNormalizedUrl(
        scheme: String,
        rawUserInfo: String?,
        host: String,
        port: Int,
        rawPath: String?,
        rawQuery: String?,
        rawFragment: String?,
    ): String {
        return buildString {
            append(scheme)
            append("://")
            if (!rawUserInfo.isNullOrBlank()) {
                append(rawUserInfo)
                append("@")
            }
            append(host)
            if (port >= 0) {
                append(":")
                append(port)
            }
            append(rawPath.orEmpty())
            if (rawQuery != null) {
                append("?")
                append(rawQuery)
            }
            if (rawFragment != null) {
                append("#")
                append(rawFragment)
            }
        }
    }

    private val SUPPORTED_SCHEMES = setOf("http", "https")
    private const val MIN_DURATION_MINUTES = 1
    private const val MAX_DURATION_MINUTES = 60
}
