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

        if (draft.url.isBlank()) {
            errors += UserLinkValidationError.EMPTY_URL
        } else {
            urlValidation.error?.let { errors += it }
        }
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

    private fun validateUrl(rawUrl: String): UrlValidation {
        val trimmed = rawUrl.trim()
        if (trimmed.isBlank()) {
            return UrlValidation()
        }

        val parsed = runCatching { URI(trimmed) }.getOrNull()
            ?: return UrlValidation(error = malformedUrlError(trimmed))
        val scheme = parsed.scheme?.lowercase()
            ?: return UrlValidation(error = UserLinkValidationError.UNSUPPORTED_SCHEME)
        if (scheme !in SUPPORTED_SCHEMES) {
            return UrlValidation(error = UserLinkValidationError.UNSUPPORTED_SCHEME)
        }

        val host = parsed.host?.lowercase()
            ?: return UrlValidation(error = UserLinkValidationError.MISSING_HOST)
        val normalizedUrl = runCatching {
            URI(
                scheme,
                parsed.userInfo,
                host,
                parsed.port,
                parsed.path,
                parsed.query,
                parsed.fragment,
            ).toASCIIString()
        }.getOrNull()
        return UrlValidation(
            normalizedUrl = normalizedUrl,
            error = if (normalizedUrl == null) UserLinkValidationError.UNSUPPORTED_SCHEME else null,
        )
    }

    private data class UrlValidation(
        val normalizedUrl: String? = null,
        val error: UserLinkValidationError? = null,
    )

    private fun malformedUrlError(rawUrl: String): UserLinkValidationError {
        val lower = rawUrl.lowercase()
        return if (lower == "http://" || lower == "https://") {
            UserLinkValidationError.MISSING_HOST
        } else {
            UserLinkValidationError.UNSUPPORTED_SCHEME
        }
    }

    private val SUPPORTED_SCHEMES = setOf("http", "https")
    private const val MIN_DURATION_MINUTES = 1
    private const val MAX_DURATION_MINUTES = 60
}
