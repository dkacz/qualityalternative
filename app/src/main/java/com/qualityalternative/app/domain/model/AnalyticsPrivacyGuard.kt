package com.qualityalternative.app.domain.model

import java.net.IDN

data class RemoteAnalyticsPayload(
    val type: AnalyticsEventType,
    val timestampMillis: Long,
    val semanticKey: String? = null,
    val interventionId: String? = null,
    val sessionId: String? = null,
    val targetClass: String? = null,
    val primaryContentId: String? = null,
    val backupContentIds: List<String> = emptyList(),
    val contentId: String? = null,
    val metadata: Map<String, String> = emptyMap(),
)

object AnalyticsPrivacyGuard {
    fun toRemotePayload(event: AnalyticsEvent): RemoteAnalyticsPayload {
        return RemoteAnalyticsPayload(
            type = event.type,
            timestampMillis = event.timestampMillis,
            semanticKey = event.semanticKey,
            interventionId = event.interventionId,
            sessionId = event.sessionId,
            targetClass = event.remoteTargetClass(),
            primaryContentId = event.primaryContentId,
            backupContentIds = event.backupContentIds,
            contentId = event.contentId,
            metadata = event.metadata
                .filter { (key, value) -> isRemoteSafeMetadataEntry(key, value) }
                .toSortedMap(),
        )
    }

    fun unsafeRemoteFields(payload: RemoteAnalyticsPayload): List<String> {
        return buildList {
            payload.semanticKey?.let { value ->
                if (!isRemoteSafeTopLevelValue(value)) add("semanticKey")
            }
            payload.interventionId?.let { value ->
                if (!isRemoteSafeTopLevelValue(value)) add("interventionId")
            }
            payload.sessionId?.let { value ->
                if (!isRemoteSafeTopLevelValue(value)) add("sessionId")
            }
            payload.targetClass?.let { value ->
                if (!isKnownRemoteTargetClass(value)) add("targetClass")
                if (!isRemoteSafeTopLevelValue(value)) add("targetClass.value")
            }
            payload.primaryContentId?.let { value ->
                if (!isRemoteSafeTopLevelValue(value)) add("primaryContentId")
            }
            payload.backupContentIds.forEachIndexed { index, value ->
                if (!isRemoteSafeTopLevelValue(value)) add("backupContentIds[$index]")
            }
            payload.contentId?.let { value ->
                if (!isRemoteSafeTopLevelValue(value)) add("contentId")
            }
            payload.metadata.forEach { (key, value) ->
                if (!isRemoteSafeKey(key)) add("metadata.$key")
                if (!isRemoteSafeMetadataValue(value)) add("metadata.$key.value")
                if (!isRemoteSafeMetadataEntry(key, value)) add("metadata.$key.entry")
            }
        }.distinct().sorted()
    }

    fun scrubDebugValue(value: String): String {
        return if (isRemoteSafeMetadataValue(value)) value else Redacted
    }

    fun remoteSafeDebugSummary(payload: RemoteAnalyticsPayload): String {
        val fields = buildList {
            add("type=${payload.type.name}")
            add("timestampMillis=${payload.timestampMillis}")
            payload.semanticKey?.let { value -> add("semanticKey=${scrubDebugValue(value)}") }
            payload.interventionId?.let { value -> add("interventionId=${scrubDebugValue(value)}") }
            payload.sessionId?.let { value -> add("sessionId=${scrubDebugValue(value)}") }
            payload.targetClass?.let { value -> add("targetClass=${scrubDebugValue(value)}") }
            payload.primaryContentId?.let { value -> add("primaryContentId=${scrubDebugValue(value)}") }
            if (payload.backupContentIds.isNotEmpty()) {
                add("backupContentIds=${payload.backupContentIds.map(::scrubDebugValue).joinToString(",")}")
            }
            payload.contentId?.let { value -> add("contentId=${scrubDebugValue(value)}") }
            payload.metadata.forEach { (key, value) ->
                add("metadata.$key=${scrubDebugValue(value)}")
            }
        }
        return fields.joinToString(" ")
    }

    private fun AnalyticsEvent.remoteTargetClass(): String? {
        return when (metadata["targetType"]) {
            WebsiteDomainTargetType -> RemoteTargetClassWebsite
            CustomAppTargetType -> RemoteTargetClassCustomApp
            StandardAppTargetType -> RemoteTargetClassStandardApp
            null -> targetAppPackage?.let { RemoteTargetClassApp }
            else -> targetAppPackage?.let { RemoteTargetClassApp }
        }
    }

    private fun isRemoteSafeMetadataEntry(key: String, value: String): Boolean {
        if (!isRemoteSafeKey(key)) return false
        if (!isRemoteSafeMetadataValue(value)) return false
        return when (key) {
            "targetType" -> value in KnownRemoteTargetTypes
            else -> true
        }
    }

    private fun isRemoteSafeKey(key: String): Boolean {
        val lower = key.lowercase()
        if (lower in SensitiveExactKeys) return false
        return key.isAllowlistedRemoteMetadataKey() &&
            SensitiveKeyTokens.none { token -> lower.contains(token) }
    }

    private fun isRemoteSafeMetadataValue(value: String): Boolean {
        return isRemoteSafeValue(value = value, rejectColon = true)
    }

    private fun isRemoteSafeTopLevelValue(value: String): Boolean {
        return isRemoteSafeValue(value = value, rejectColon = false)
    }

    private fun isRemoteSafeValue(value: String, rejectColon: Boolean): Boolean {
        val trimmed = value.trim()
        if (trimmed.isBlank()) return true
        val lower = trimmed.lowercase()
        val canonical = canonicalizeHostCandidate(trimmed)
        val canonicalLower = canonical.lowercase()
        if (trimmed.any(Char::isISOControl)) return false
        if (trimmed.any { it == '/' || it == '\\' || it == '?' || it == '#' || it == '@' }) return false
        if ("://" in lower || "content:" in lower || "file:" in lower) return false
        if (isBracketedIpLiteral(canonicalLower)) return false
        if (isIpLiteral(canonicalLower)) return false
        if (isHostWithPort(canonicalLower)) return false
        if (rejectColon && ':' in trimmed) return false
        if (isHostLike(trimmed)) return false
        if (trimmed.any { it.code > AsciiMax }) return false
        if (PackageLikeRegex.matches(trimmed)) return false
        return true
    }

    private const val WebsiteDomainTargetType = "website_domain"
    private const val CustomAppTargetType = "custom_app"
    private const val StandardAppTargetType = "standard_app"
    private const val RemoteTargetClassWebsite = "supported_browser_website"
    private const val RemoteTargetClassCustomApp = "custom_app"
    private const val RemoteTargetClassStandardApp = "standard_app"
    private const val RemoteTargetClassApp = "app_target"
    private const val Redacted = "[redacted]"
    private const val AsciiMax = 0x7F

    private val KnownRemoteTargetTypes = setOf(
        WebsiteDomainTargetType,
        CustomAppTargetType,
        StandardAppTargetType,
    )

    private val KnownRemoteTargetClasses = setOf(
        RemoteTargetClassWebsite,
        RemoteTargetClassCustomApp,
        RemoteTargetClassStandardApp,
        RemoteTargetClassApp,
    )

    private val RemoteSafeMetadataKeys = setOf(
        "action",
        "acceptedPriority",
        "availability",
        "backupCount",
        "bedtimeActive",
        "bedtimeActivatedAfterInterventionShown",
        "bedtimeEndMinutes",
        "bedtimeStartMinutes",
        "browserSupportStatus",
        "completed",
        "completedAtMillis",
        "completedContentCount",
        "delayId",
        "displayNameSource",
        "documentErrorCount",
        "durationMinutes",
        "eligibleEditorialCount",
        "eligibleInventoryCount",
        "eligibleMeditationCount",
        "eligibleUserDocumentCount",
        "eligibleUserLinkCount",
        "elapsedMillis",
        "estimateSource",
        "failureReason",
        "format",
        "formUnlockWaitMillis",
        "inboxCandidateCount",
        "inboxDuplicateCount",
        "inboxInvalidCount",
        "inboxReadyCount",
        "importStatus",
        "interventionMode",
        "lastVisibleParagraphIndex",
        "lastVisibleTextOffset",
        "openAnywayUnlockAvailableAtMillis",
        "openAnywayUnlockDelayMillis",
        "openAnywayUnlockMinutes",
        "openAnywayUnlockUntilMillis",
        "origin",
        "packId",
        "paragraphCount",
        "priorityContentCount",
        "priorityRequested",
        "progressPercent",
        "reason",
        "remainingMillis",
        "renderMode",
        "rightsClass",
        "reviewAction",
        "selectedPackCount",
        "selectedPackIds",
        "sourceType",
        "synced",
        "targetType",
        "triggerSource",
        "unfinishedContentCount",
        "unavailableUserDocumentCount",
        "unavailableUserLinkCount",
        "updatedAtMillis",
        "validationErrorCount",
        "websiteRuleIncludesApex",
        "websiteRuleType",
        "wordCount",
    )

    private val RemoteSafeContentMetadataSuffixes = setOf(
        "availability",
        "format",
        "packId",
        "renderMode",
        "rightsClass",
        "sourceType",
    )

    private val SensitiveExactKeys = setOf(
        "browserpackage",
        "foregroundpackage",
        "foregroundclass",
        "targetapppackage",
        "websiteruleid",
        "ruleid",
        "rawurl",
        "externalurl",
        "urlbartext",
        "pagetitle",
    )

    private val SensitiveKeyTokens = listOf(
        "url",
        "uri",
        "host",
        "domain",
        "title",
        "package",
        "class",
        "path",
        "query",
        "address",
        "omnibox",
    )

    private val PackageLikeRegex = Regex("[A-Za-z][A-Za-z0-9_]*(\\.[A-Za-z][A-Za-z0-9_]*)+")
    private val HostLikeRegex = Regex("([a-z0-9-]+\\.)+[a-z0-9-]+")
    private val Ipv4LikeRegex = Regex("(\\d{1,3}\\.){3}\\d{1,3}")
    private val Ipv6LikeRegex = Regex("[0-9a-f:]*:[0-9a-f:]+")
    private val BracketedIpv6LikeRegex = Regex("\\[[0-9a-f:]+]")
    private val HostWithPortRegex = Regex("(.+):(\\d{1,5})")
    private val DnsLabelRegex = Regex("[a-z0-9]([a-z0-9-]{0,61}[a-z0-9])?")

    private fun String.isAllowlistedRemoteMetadataKey(): Boolean {
        if (this in RemoteSafeMetadataKeys) return true
        return Regex("^(primary|backup[0-9]+)_([A-Za-z0-9]+)$").matchEntire(this)
            ?.groupValues
            ?.getOrNull(2)
            ?.let(RemoteSafeContentMetadataSuffixes::contains)
            ?: false
    }

    private fun isKnownRemoteTargetClass(value: String): Boolean {
        return value in KnownRemoteTargetClasses
    }

    private fun isIpLiteral(lower: String): Boolean {
        return Ipv4LikeRegex.matches(lower) || Ipv6LikeRegex.matches(lower)
    }

    private fun isBracketedIpLiteral(lower: String): Boolean {
        return BracketedIpv6LikeRegex.matches(lower)
    }

    private fun isHostWithPort(lower: String): Boolean {
        val match = HostWithPortRegex.matchEntire(lower) ?: return false
        val hostPart = canonicalizeHostCandidate(match.groupValues[1]).lowercase()
        return HostLikeRegex.matches(hostPart) || Ipv4LikeRegex.matches(hostPart)
    }

    private fun isHostLike(value: String): Boolean {
        val asciiCandidate = canonicalizeHostCandidate(value)
        if (asciiCandidate.isBlank()) return false
        val lowerAscii = asciiCandidate.lowercase()
        if (isDnsStyleValue(lowerAscii)) return true
        val idnAscii = runCatching { IDN.toASCII(asciiCandidate).lowercase() }.getOrNull()
        return idnAscii != null && isDnsStyleValue(idnAscii)
    }

    private fun canonicalizeHostCandidate(value: String): String {
        return value.trim().removeSurrounding("[", "]").trimEnd('.')
    }

    private fun isDnsStyleValue(lowerAscii: String): Boolean {
        val labels = lowerAscii.split('.')
        if (labels.size < 2) return false
        return labels.all { label -> DnsLabelRegex.matches(label) }
    }
}

fun AnalyticsEvent.toRemoteAnalyticsPayload(): RemoteAnalyticsPayload {
    return AnalyticsPrivacyGuard.toRemotePayload(this)
}

fun RemoteAnalyticsPayload.toRemoteSafeDebugSummary(): String {
    return AnalyticsPrivacyGuard.remoteSafeDebugSummary(this)
}
