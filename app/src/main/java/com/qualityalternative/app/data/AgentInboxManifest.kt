package com.qualityalternative.app.data

import com.qualityalternative.app.domain.model.ContentFormat
import com.qualityalternative.app.domain.model.ContentRightsClass
import com.qualityalternative.app.domain.model.TopicTag
import java.security.MessageDigest
import java.util.Locale
import org.json.JSONException
import org.json.JSONObject

enum class AgentInboxPriorityIntent {
    NORMAL,
    HIGH,
}

data class AgentInboxManifest(
    val schemaVersion: Int,
    val title: String,
    val topics: Set<TopicTag>,
    val contentFile: String,
    val format: ContentFormat,
    val rightsClass: ContentRightsClass,
    val sourceLabel: String?,
    val description: String?,
    val priority: AgentInboxPriorityIntent,
    val documentSha256: String?,
    val createdAt: String?,
) {
    val requestsHighPriority: Boolean
        get() = priority == AgentInboxPriorityIntent.HIGH
}

enum class AgentInboxManifestValidationError {
    MALFORMED_JSON,
    UNSUPPORTED_SCHEMA_VERSION,
    BLANK_TITLE,
    NO_TOPICS,
    UNKNOWN_TOPIC,
    MISSING_CONTENT_FILE,
    UNSAFE_CONTENT_FILE,
    UNSUPPORTED_FORMAT,
    FORMAT_CONTENT_FILE_MISMATCH,
    RIGHTS_CLASS_MUST_BE_USER_PRIVATE,
    INVALID_PRIORITY,
    INVALID_DOCUMENT_SHA256,
    DOCUMENT_SHA256_MISMATCH,
}

data class AgentInboxManifestValidationResult(
    val manifest: AgentInboxManifest? = null,
    val errors: Set<AgentInboxManifestValidationError> = emptySet(),
) {
    val isValid: Boolean
        get() = errors.isEmpty() && manifest != null
}

object AgentInboxManifestValidator {
    const val CURRENT_SCHEMA_VERSION = 1

    fun validate(
        manifestJson: String,
        availableFileNames: Set<String> = emptySet(),
        actualContentSha256: String? = null,
    ): AgentInboxManifestValidationResult {
        val json = try {
            JSONObject(manifestJson)
        } catch (_: JSONException) {
            return AgentInboxManifestValidationResult(
                errors = setOf(AgentInboxManifestValidationError.MALFORMED_JSON),
            )
        }

        val errors = linkedSetOf<AgentInboxManifestValidationError>()
        val schemaVersion = json.optInt("schemaVersion", -1)
        if (schemaVersion != CURRENT_SCHEMA_VERSION) {
            errors += AgentInboxManifestValidationError.UNSUPPORTED_SCHEMA_VERSION
        }

        val title = json.optString("title").trim()
        if (title.isBlank()) {
            errors += AgentInboxManifestValidationError.BLANK_TITLE
        }

        val topics = parseTopics(json = json, errors = errors)
        if (topics.isEmpty()) {
            errors += AgentInboxManifestValidationError.NO_TOPICS
        }

        val contentFile = json.optString("contentFile").trim()
        if (contentFile.isBlank()) {
            errors += AgentInboxManifestValidationError.MISSING_CONTENT_FILE
        } else {
            if (!contentFile.isSafeAgentContentFileName()) {
                errors += AgentInboxManifestValidationError.UNSAFE_CONTENT_FILE
            }
            if (availableFileNames.isNotEmpty() && contentFile !in availableFileNames) {
                errors += AgentInboxManifestValidationError.MISSING_CONTENT_FILE
            }
        }

        val format = parseContentFormat(json.optString("format").trim(), errors)
        if (format != null && contentFile.isNotBlank() && format != contentFile.detectAgentContentFormat()) {
            errors += AgentInboxManifestValidationError.FORMAT_CONTENT_FILE_MISMATCH
        }

        val rightsClass = parseRightsClass(json.optString("rightsClass").trim(), errors)
        if (rightsClass != null && rightsClass != ContentRightsClass.USER_PRIVATE) {
            errors += AgentInboxManifestValidationError.RIGHTS_CLASS_MUST_BE_USER_PRIVATE
        }

        val priority = parsePriority(json.optString("priority").trim(), errors)
        val documentSha256 = json.optString("documentSha256").trim().ifBlank { null }
        if (documentSha256 != null && !documentSha256.matches(Sha256Regex)) {
            errors += AgentInboxManifestValidationError.INVALID_DOCUMENT_SHA256
        }
        val normalizedActualSha = actualContentSha256?.trim()?.lowercase(Locale.US)?.ifBlank { null }
        if (
            documentSha256 != null &&
            documentSha256.matches(Sha256Regex) &&
            normalizedActualSha != null &&
            documentSha256 != normalizedActualSha
        ) {
            errors += AgentInboxManifestValidationError.DOCUMENT_SHA256_MISMATCH
        }

        if (errors.isNotEmpty() || format == null || rightsClass == null || priority == null) {
            return AgentInboxManifestValidationResult(errors = errors)
        }

        return AgentInboxManifestValidationResult(
            manifest = AgentInboxManifest(
                schemaVersion = schemaVersion,
                title = title,
                topics = topics,
                contentFile = contentFile,
                format = format,
                rightsClass = rightsClass,
                sourceLabel = json.optString("sourceLabel").trim().ifBlank { null },
                description = json.optString("description").trim().ifBlank { null },
                priority = priority,
                documentSha256 = documentSha256,
                createdAt = json.optString("createdAt").trim().ifBlank { null },
            ),
        )
    }

    fun sha256(bytes: ByteArray): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(bytes)
        return digest.joinToString("") { byte -> "%02x".format(byte) }
    }

    private fun parseTopics(
        json: JSONObject,
        errors: MutableSet<AgentInboxManifestValidationError>,
    ): Set<TopicTag> {
        val topicsJson = json.optJSONArray("topics")
        if (topicsJson == null || topicsJson.length() == 0) {
            return emptySet()
        }
        return buildSet {
            for (index in 0 until topicsJson.length()) {
                val raw = topicsJson.optString(index).trim()
                val topic = runCatching { TopicTag.valueOf(raw.uppercase(Locale.US)) }.getOrNull()
                if (topic == null) {
                    errors += AgentInboxManifestValidationError.UNKNOWN_TOPIC
                } else {
                    add(topic)
                }
            }
        }
    }

    private fun parseContentFormat(
        raw: String,
        errors: MutableSet<AgentInboxManifestValidationError>,
    ): ContentFormat? {
        val format = runCatching { ContentFormat.valueOf(raw.uppercase(Locale.US)) }.getOrNull()
        if (format != ContentFormat.MARKDOWN && format != ContentFormat.EPUB) {
            errors += AgentInboxManifestValidationError.UNSUPPORTED_FORMAT
            return null
        }
        return format
    }

    private fun parseRightsClass(
        raw: String,
        errors: MutableSet<AgentInboxManifestValidationError>,
    ): ContentRightsClass? {
        val rightsClass = runCatching { ContentRightsClass.valueOf(raw.uppercase(Locale.US)) }.getOrNull()
        if (rightsClass == null) {
            errors += AgentInboxManifestValidationError.RIGHTS_CLASS_MUST_BE_USER_PRIVATE
        }
        return rightsClass
    }

    private fun parsePriority(
        raw: String,
        errors: MutableSet<AgentInboxManifestValidationError>,
    ): AgentInboxPriorityIntent? {
        return when (raw.lowercase(Locale.US)) {
            "", "normal" -> AgentInboxPriorityIntent.NORMAL
            "high" -> AgentInboxPriorityIntent.HIGH
            else -> {
                errors += AgentInboxManifestValidationError.INVALID_PRIORITY
                null
            }
        }
    }
}

private fun String.isSafeAgentContentFileName(): Boolean {
    if (isBlank()) {
        return false
    }
    if (contains('/') || contains('\\') || contains("..")) {
        return false
    }
    return detectAgentContentFormat() in setOf(ContentFormat.MARKDOWN, ContentFormat.EPUB)
}

private fun String.detectAgentContentFormat(): ContentFormat? {
    val lower = lowercase(Locale.US)
    return when {
        lower.endsWith(".md") || lower.endsWith(".markdown") -> ContentFormat.MARKDOWN
        lower.endsWith(".epub") -> ContentFormat.EPUB
        else -> null
    }
}

private val Sha256Regex = Regex("^[0-9a-f]{64}$")
