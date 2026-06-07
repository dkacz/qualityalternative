package com.qualityalternative.app.interception

import com.qualityalternative.app.domain.model.WebsiteRule
import com.qualityalternative.app.domain.model.WebsiteRuleType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class WebsiteInterceptionResolverTest {
    @Test
    fun resolve_matchesEnabledRulesWithoutDomainInAnalytics() {
        val target = WebsiteInterceptionResolver.resolve(
            browserPackage = VerifiedBrowserHostAdapter.CHROME_PACKAGE,
            browserDisplayName = "Chrome",
            observedHost = "news.example.com",
            websiteRules = listOf(
                WebsiteRule(
                    id = "website-rule-00000000-0000-0000-0000-000000000001",
                    type = WebsiteRuleType.WILDCARD_SUBDOMAINS,
                    host = "example.com",
                    includeApex = false,
                    enabled = true,
                    createdAtMillis = 1L,
                    updatedAtMillis = 1L,
                ),
            ),
        )

        assertNotNull(target)
        requireNotNull(target)
        assertEquals("Chrome website", target.targetApp.displayName)
        assertEquals(VerifiedBrowserHostAdapter.CHROME_PACKAGE, target.targetApp.packageName)
        assertEquals("website-domain:${VerifiedBrowserHostAdapter.CHROME_PACKAGE}", target.suppressionKey)
        assertEquals(WebsiteInterceptionResolver.TARGET_TYPE, target.analyticsMetadata["targetType"])
        assertEquals(WebsiteInterceptionResolver.BROWSER_SUPPORT_VERIFIED_HOST, target.analyticsMetadata["browserSupportStatus"])
        val serializedMetadata = target.analyticsMetadata.entries.joinToString(" ")
        assertFalse(serializedMetadata.contains("example"))
        assertFalse(target.suppressionKey.contains("example"))
    }

    @Test
    fun resolve_prefersExactRuleAndHonorsApexFlag() {
        val target = WebsiteInterceptionResolver.resolve(
            browserPackage = VerifiedBrowserHostAdapter.CHROME_PACKAGE,
            browserDisplayName = "Chrome",
            observedHost = "example.com",
            websiteRules = listOf(
                WebsiteRule(
                    id = "website-rule-00000000-0000-0000-0000-000000000002",
                    type = WebsiteRuleType.WILDCARD_SUBDOMAINS,
                    host = "example.com",
                    includeApex = true,
                    enabled = true,
                    createdAtMillis = 1L,
                    updatedAtMillis = 1L,
                ),
                WebsiteRule(
                    id = "website-rule-00000000-0000-0000-0000-000000000003",
                    type = WebsiteRuleType.EXACT_DOMAIN,
                    host = "example.com",
                    includeApex = false,
                    enabled = true,
                    createdAtMillis = 2L,
                    updatedAtMillis = 2L,
                ),
            ),
        )

        assertEquals(WebsiteRuleType.EXACT_DOMAIN.name, target?.analyticsMetadata?.get("websiteRuleType"))
        assertEquals("false", target?.analyticsMetadata?.get("websiteRuleIncludesApex"))
    }

    @Test
    fun resolve_ignoresDisabledRulesAndBoundarySpoofingHosts() {
        val disabledRule = WebsiteRule(
            id = "website-rule-00000000-0000-0000-0000-000000000004",
            type = WebsiteRuleType.EXACT_DOMAIN,
            host = "example.com",
            enabled = false,
            createdAtMillis = 1L,
            updatedAtMillis = 1L,
        )
        val wildcardRule = disabledRule.copy(
            id = "website-rule-00000000-0000-0000-0000-000000000005",
            type = WebsiteRuleType.WILDCARD_SUBDOMAINS,
            enabled = true,
        )

        assertNull(
            WebsiteInterceptionResolver.resolve(
                browserPackage = VerifiedBrowserHostAdapter.CHROME_PACKAGE,
                browserDisplayName = "Chrome",
                observedHost = "example.com",
                websiteRules = listOf(disabledRule),
            ),
        )
        assertNull(
            WebsiteInterceptionResolver.resolve(
                browserPackage = VerifiedBrowserHostAdapter.CHROME_PACKAGE,
                browserDisplayName = "Chrome",
                observedHost = "example.com.evil.test",
                websiteRules = listOf(wildcardRule),
            ),
        )
    }
}
