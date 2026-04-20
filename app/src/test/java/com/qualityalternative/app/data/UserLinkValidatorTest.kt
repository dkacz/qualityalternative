package com.qualityalternative.app.data

import com.qualityalternative.app.domain.model.TopicTag
import com.qualityalternative.app.domain.model.UserLinkDraft
import com.qualityalternative.app.domain.model.UserLinkValidationError
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UserLinkValidatorTest {
    @Test
    fun validate_acceptsHttpAndHttpsLinksWithRequiredMetadata() {
        val result = UserLinkValidator.validate(
            UserLinkDraft(
                url = " HTTPS://Example.com/Essay?q=1 ",
                title = "A saved essay",
                durationMinutes = 8,
                topicTags = setOf(TopicTag.PSYCHOLOGY),
            ),
        )

        assertTrue(result.isValid)
        assertEquals("https://example.com/Essay?q=1", result.normalizedUrl)
    }

    @Test
    fun validate_rejectsNonWebSchemes() {
        val result = UserLinkValidator.validate(
            UserLinkDraft(
                url = "file:///tmp/secret.txt",
                title = "Local file",
                durationMinutes = 8,
                topicTags = setOf(TopicTag.PSYCHOLOGY),
            ),
        )

        assertFalse(result.isValid)
        assertTrue(UserLinkValidationError.UNSUPPORTED_SCHEME in result.errors)
    }

    @Test
    fun validate_rejectsWebUrlsWithoutHost() {
        val result = UserLinkValidator.validate(
            UserLinkDraft(
                url = "https://",
                title = "Broken web URL",
                durationMinutes = 8,
                topicTags = setOf(TopicTag.PSYCHOLOGY),
            ),
        )

        assertFalse(result.isValid)
        assertTrue(UserLinkValidationError.MISSING_HOST in result.errors)
    }

    @Test
    fun validate_preservesEncodedPathQueryAndFragment() {
        val result = UserLinkValidator.validate(
            UserLinkDraft(
                url = "https://Example.com/%2F?q=a%2Fb#x%2Fy",
                title = "Encoded URL",
                durationMinutes = 8,
                topicTags = setOf(TopicTag.PSYCHOLOGY),
            ),
        )

        assertTrue(result.isValid)
        assertEquals("https://example.com/%2F?q=a%2Fb#x%2Fy", result.normalizedUrl)
    }

    @Test
    fun validate_reportsAllMissingRequiredFields() {
        val result = UserLinkValidator.validate(
            UserLinkDraft(
                url = "",
                title = "",
                durationMinutes = 0,
                topicTags = emptySet(),
            ),
        )

        assertEquals(
            setOf(
                UserLinkValidationError.EMPTY_URL,
                UserLinkValidationError.BLANK_TITLE,
                UserLinkValidationError.INVALID_DURATION,
                UserLinkValidationError.NO_TOPICS,
            ),
            result.errors,
        )
        assertEquals(null, result.normalizedUrl)
    }
}
