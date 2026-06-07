package com.qualityalternative.app.data

import com.qualityalternative.app.domain.model.WebsiteRule
import com.qualityalternative.app.domain.model.WebsiteRuleType
import java.net.IDN
import java.net.URI
import java.util.Locale

object WebsiteRuleNormalizer {
    fun normalize(input: String, wildcard: Boolean): WebsiteRuleDraftResult {
        val trimmed = input.trim()
        if (trimmed.isBlank()) return WebsiteRuleDraftResult.Invalid("Enter a domain.")

        val explicitWildcard = trimmed.startsWith("*.")
        val rawHost = rawHostFrom(trimmed.removePrefix("*."))
            ?: return WebsiteRuleDraftResult.Invalid("Enter a valid domain, not a search or path-only value.")
        if (isIpLiteralOrNumericHost(rawHost)) {
            return WebsiteRuleDraftResult.Invalid("Private, local, and IP hosts are not website rules.")
        }
        val normalized = normalizeHost(rawHost)
            ?: return WebsiteRuleDraftResult.Invalid("Enter a valid public domain.")

        if (isPrivateOrAmbiguousHost(normalized)) {
            return WebsiteRuleDraftResult.Invalid("Private, local, and IP hosts are not website rules.")
        }
        if (normalized.count { it == '.' } < 1) {
            return WebsiteRuleDraftResult.Invalid("Use a full domain such as example.com.")
        }

        return WebsiteRuleDraftResult.Valid(
            host = normalized,
            type = if (wildcard || explicitWildcard) WebsiteRuleType.WILDCARD_SUBDOMAINS else WebsiteRuleType.EXACT_DOMAIN,
        )
    }

    fun normalizeObservedHost(input: String): String? {
        return normalizeHost(rawHostFrom(input) ?: input.trim())
    }

    fun matches(rule: WebsiteRule, observedHost: String): Boolean {
        val host = normalizeObservedHost(observedHost) ?: return false
        return when (rule.type) {
            WebsiteRuleType.EXACT_DOMAIN -> host == rule.host
            WebsiteRuleType.WILDCARD_SUBDOMAINS -> {
                host.endsWith(".${rule.host}") || (rule.includeApex && host == rule.host)
            }
        }
    }

    private fun rawHostFrom(input: String): String? {
        val withoutWhitespace = input.trim()
        if (withoutWhitespace.any(Char::isWhitespace)) return null
        if ("://" !in withoutWhitespace && "@" in withoutWhitespace) return null
        val candidate = if ("://" in withoutWhitespace) withoutWhitespace else "https://$withoutWhitespace"
        return runCatching { URI(candidate).host }.getOrNull()
            ?: withoutWhitespace
                .takeUnless { ':' in it }
                ?.substringBefore('/')
                ?.substringBefore('?')
                ?.substringBefore('#')
                ?.substringBefore('@')
                ?.takeIf(String::isNotBlank)
    }

    private fun normalizeHost(raw: String): String? {
        val lowered = raw.trim().trimEnd('.').lowercase(Locale.US)
        if (lowered.isBlank() || lowered.length > 253 || lowered.contains('_') || lowered.contains("..")) return null
        if (lowered.startsWith("-") || lowered.endsWith("-")) return null
        if (lowered.contains(':')) return null
        val labels = lowered.split('.')
        if (labels.any { label -> label.isBlank() || label.length > 63 || label.startsWith("-") || label.endsWith("-") }) {
            return null
        }
        return runCatching {
            labels.joinToString(".") { label ->
                IDN.toASCII(label, IDN.USE_STD3_ASCII_RULES).lowercase(Locale.US)
            }
        }.getOrNull()?.takeIf { ascii ->
            ascii.all { char -> char.isLetterOrDigit() || char == '-' || char == '.' }
        }
    }

    private fun isPrivateOrAmbiguousHost(host: String): Boolean {
        if (host == "localhost" || host.endsWith(".local")) return true
        val parts = host.split('.')
        if (parts.all { part -> part.all(Char::isDigit) }) return true
        return false
    }

    private fun isIpLiteralOrNumericHost(rawHost: String): Boolean {
        val host = rawHost.trim().trim('[', ']').trimEnd('.').lowercase(Locale.US)
        if (':' in host) return true
        val labels = host.split('.')
        return labels.isNotEmpty() && labels.all { label ->
            label.isNotBlank() && label.all(Char::isDigit)
        }
    }
}

sealed class WebsiteRuleDraftResult {
    data class Valid(
        val host: String,
        val type: WebsiteRuleType,
    ) : WebsiteRuleDraftResult()

    data class Invalid(val message: String) : WebsiteRuleDraftResult()
}
