package com.qualityalternative.app.interception

import com.qualityalternative.app.data.WebsiteRuleNormalizer
import com.qualityalternative.app.domain.model.DistractingApp
import com.qualityalternative.app.domain.model.WebsiteRule
import com.qualityalternative.app.domain.model.WebsiteRuleType

object WebsiteInterceptionResolver {
    const val TARGET_TYPE = "website_domain"
    const val BROWSER_SUPPORT_VERIFIED_HOST = "verified_host"

    fun resolve(
        browserPackage: String,
        browserDisplayName: String,
        observedHost: String,
        websiteRules: List<WebsiteRule>,
    ): WebsiteInterceptionTarget? {
        val matchingRule = websiteRules
            .asSequence()
            .filter(WebsiteRule::enabled)
            .filter { rule -> WebsiteRuleNormalizer.matches(rule, observedHost) }
            .sortedWith(websiteRulePriority)
            .firstOrNull()
            ?: return null

        val safeBrowserName = browserDisplayName.trim().takeIf(String::isNotBlank) ?: "Browser"
        return WebsiteInterceptionTarget(
            targetApp = DistractingApp(
                packageName = browserPackage,
                displayName = "$safeBrowserName website",
            ),
            suppressionKey = suppressionKeyFor(browserPackage),
            websiteRuleType = matchingRule.type,
            websiteRuleIncludesApex = matchingRule.includeApex,
            analyticsMetadata = mapOf(
                "targetType" to TARGET_TYPE,
                "browserPackage" to browserPackage,
                "browserSupportStatus" to BROWSER_SUPPORT_VERIFIED_HOST,
                "websiteRuleType" to matchingRule.type.name,
                "websiteRuleIncludesApex" to matchingRule.includeApex.toString(),
            ),
        )
    }

    fun suppressionKeyFor(browserPackage: String): String = "website-domain:$browserPackage"

    private val websiteRulePriority = compareBy<WebsiteRule>(
        { if (it.type == WebsiteRuleType.EXACT_DOMAIN) 0 else 1 },
        { -it.host.length },
        { it.id },
    )
}

data class WebsiteInterceptionTarget(
    val targetApp: DistractingApp,
    val suppressionKey: String,
    val websiteRuleType: WebsiteRuleType,
    val websiteRuleIncludesApex: Boolean,
    val analyticsMetadata: Map<String, String>,
)
