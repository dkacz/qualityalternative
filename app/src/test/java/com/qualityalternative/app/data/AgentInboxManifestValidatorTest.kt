package com.qualityalternative.app.data

import com.qualityalternative.app.domain.model.ContentFormat
import com.qualityalternative.app.domain.model.ContentRightsClass
import com.qualityalternative.app.domain.model.TopicTag
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AgentInboxManifestValidatorTest {
    @Test
    fun validate_acceptsMarkdownManifestWithHighPriorityIntent() {
        val bytes = "# Focus\n\nA private note.".toByteArray()
        val sha = AgentInboxManifestValidator.sha256(bytes)

        val result = AgentInboxManifestValidator.validate(
            manifestJson = """
                {
                  "schemaVersion": 1,
                  "title": "Agent Focus Notes",
                  "topics": ["ATTENTION", "PHILOSOPHY"],
                  "contentFile": "content.md",
                  "format": "MARKDOWN",
                  "rightsClass": "USER_PRIVATE",
                  "sourceLabel": "Codex",
                  "description": "Prepared for the next replacement moment.",
                  "priority": "high",
                  "documentSha256": "$sha",
                  "createdAt": "2026-06-12T10:00:00Z",
                  "ignoredFutureField": "safe to ignore"
                }
            """.trimIndent(),
            availableFileNames = setOf("manifest.json", "content.md"),
            actualContentSha256 = sha,
        )

        assertTrue(result.isValid)
        val manifest = requireNotNull(result.manifest)
        assertEquals("Agent Focus Notes", manifest.title)
        assertEquals(setOf(TopicTag.ATTENTION, TopicTag.PHILOSOPHY), manifest.topics)
        assertEquals("content.md", manifest.contentFile)
        assertEquals(ContentFormat.MARKDOWN, manifest.format)
        assertEquals(ContentRightsClass.USER_PRIVATE, manifest.rightsClass)
        assertEquals("Codex", manifest.sourceLabel)
        assertEquals(AgentInboxPriorityIntent.HIGH, manifest.priority)
        assertTrue(manifest.requestsHighPriority)
    }

    @Test
    fun validate_acceptsEpubManifestWithDefaultNormalPriority() {
        val result = AgentInboxManifestValidator.validate(
            manifestJson = """
                {
                  "schemaVersion": 1,
                  "title": "Long Calm Book",
                  "topics": ["ESSAYS"],
                  "contentFile": "book.epub",
                  "format": "EPUB",
                  "rightsClass": "USER_PRIVATE"
                }
            """.trimIndent(),
            availableFileNames = setOf("manifest.json", "book.epub"),
        )

        assertTrue(result.isValid)
        assertEquals(ContentFormat.EPUB, result.manifest?.format)
        assertEquals(AgentInboxPriorityIntent.NORMAL, result.manifest?.priority)
        assertFalse(requireNotNull(result.manifest).requestsHighPriority)
        assertNull(result.manifest?.documentSha256)
    }

    @Test
    fun validate_rejectsMalformedJson() {
        val result = AgentInboxManifestValidator.validate("{not-json")

        assertEquals(setOf(AgentInboxManifestValidationError.MALFORMED_JSON), result.errors)
        assertFalse(result.isValid)
        assertNull(result.manifest)
    }

    @Test
    fun validate_rejectsUnsupportedSchemaBlankTitleAndNoTopics() {
        val result = AgentInboxManifestValidator.validate(
            manifestJson = """
                {
                  "schemaVersion": 2,
                  "title": " ",
                  "topics": [],
                  "contentFile": "content.md",
                  "format": "MARKDOWN",
                  "rightsClass": "USER_PRIVATE"
                }
            """.trimIndent(),
            availableFileNames = setOf("content.md"),
        )

        assertEquals(
            setOf(
                AgentInboxManifestValidationError.UNSUPPORTED_SCHEMA_VERSION,
                AgentInboxManifestValidationError.BLANK_TITLE,
                AgentInboxManifestValidationError.NO_TOPICS,
            ),
            result.errors,
        )
        assertFalse(result.isValid)
    }

    @Test
    fun validate_rejectsUnknownTopicEvenWhenAnotherTopicIsValid() {
        val result = AgentInboxManifestValidator.validate(
            manifestJson = validManifest(
                topics = """["ATTENTION", "NOT_A_TOPIC"]""",
            ),
            availableFileNames = setOf("content.md"),
        )

        assertEquals(setOf(AgentInboxManifestValidationError.UNKNOWN_TOPIC), result.errors)
        assertFalse(result.isValid)
    }

    @Test
    fun validate_rejectsMissingContentFileFromPackage() {
        val result = AgentInboxManifestValidator.validate(
            manifestJson = validManifest(contentFile = "content.md"),
            availableFileNames = setOf("manifest.json", "other.md"),
        )

        assertEquals(setOf(AgentInboxManifestValidationError.MISSING_CONTENT_FILE), result.errors)
        assertFalse(result.isValid)
    }

    @Test
    fun validate_rejectsUnsafeContentFilePath() {
        val result = AgentInboxManifestValidator.validate(
            manifestJson = validManifest(contentFile = "../content.md"),
            availableFileNames = setOf("../content.md"),
        )

        assertEquals(setOf(AgentInboxManifestValidationError.UNSAFE_CONTENT_FILE), result.errors)
        assertFalse(result.isValid)
    }

    @Test
    fun validate_rejectsUnsupportedFormatAndMismatch() {
        val unsupported = AgentInboxManifestValidator.validate(
            manifestJson = validManifest(contentFile = "content.pdf", format = "PDF"),
            availableFileNames = setOf("content.pdf"),
        )
        assertEquals(
            setOf(
                AgentInboxManifestValidationError.UNSUPPORTED_FORMAT,
                AgentInboxManifestValidationError.UNSAFE_CONTENT_FILE,
            ),
            unsupported.errors,
        )

        val mismatch = AgentInboxManifestValidator.validate(
            manifestJson = validManifest(contentFile = "content.epub", format = "MARKDOWN"),
            availableFileNames = setOf("content.epub"),
        )
        assertEquals(setOf(AgentInboxManifestValidationError.FORMAT_CONTENT_FILE_MISMATCH), mismatch.errors)
    }

    @Test
    fun validate_rejectsNonPrivateRightsClass() {
        val result = AgentInboxManifestValidator.validate(
            manifestJson = validManifest(rightsClass = "RENDERABLE"),
            availableFileNames = setOf("content.md"),
        )

        assertEquals(setOf(AgentInboxManifestValidationError.RIGHTS_CLASS_MUST_BE_USER_PRIVATE), result.errors)
        assertFalse(result.isValid)
    }

    @Test
    fun validate_rejectsInvalidPriority() {
        val result = AgentInboxManifestValidator.validate(
            manifestJson = validManifest(priority = "urgent"),
            availableFileNames = setOf("content.md"),
        )

        assertEquals(setOf(AgentInboxManifestValidationError.INVALID_PRIORITY), result.errors)
        assertFalse(result.isValid)
    }

    @Test
    fun validate_rejectsInvalidOrMismatchedSha() {
        val invalid = AgentInboxManifestValidator.validate(
            manifestJson = validManifest(documentSha256 = "ABC"),
            availableFileNames = setOf("content.md"),
        )
        assertEquals(setOf(AgentInboxManifestValidationError.INVALID_DOCUMENT_SHA256), invalid.errors)

        val expected = "0".repeat(64)
        val actual = AgentInboxManifestValidator.sha256("different".toByteArray())
        val mismatch = AgentInboxManifestValidator.validate(
            manifestJson = validManifest(documentSha256 = expected),
            availableFileNames = setOf("content.md"),
            actualContentSha256 = actual,
        )
        assertEquals(setOf(AgentInboxManifestValidationError.DOCUMENT_SHA256_MISMATCH), mismatch.errors)
    }

    private fun validManifest(
        topics: String = """["ATTENTION"]""",
        contentFile: String = "content.md",
        format: String = "MARKDOWN",
        rightsClass: String = "USER_PRIVATE",
        priority: String? = "normal",
        documentSha256: String? = null,
    ): String {
        val priorityField = priority?.let { ""","priority":"$it"""" }.orEmpty()
        val shaField = documentSha256?.let { ""","documentSha256":"$it"""" }.orEmpty()
        return """
            {
              "schemaVersion": 1,
              "title": "Agent Note",
              "topics": $topics,
              "contentFile": "$contentFile",
              "format": "$format",
              "rightsClass": "$rightsClass"
              $priorityField
              $shaField
            }
        """.trimIndent()
    }
}
