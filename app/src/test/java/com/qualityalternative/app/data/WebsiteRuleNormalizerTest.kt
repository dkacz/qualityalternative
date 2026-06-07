package com.qualityalternative.app.data

import com.qualityalternative.app.domain.model.WebsiteRule
import com.qualityalternative.app.domain.model.WebsiteRuleType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WebsiteRuleNormalizerTest {
    @Test
    fun normalize_stripsSchemePathPortAndTrailingDot() {
        val result = WebsiteRuleNormalizer.normalize(
            input = "HTTPS://Example.COM:443/articles/today?utm=1#top.",
            wildcard = false,
        )

        assertEquals(
            WebsiteRuleDraftResult.Valid(
                host = "example.com",
                type = WebsiteRuleType.EXACT_DOMAIN,
            ),
            result,
        )
    }

    @Test
    fun normalize_convertsUnicodeDomainsToAscii() {
        val result = WebsiteRuleNormalizer.normalize(input = "MÜNICH.example", wildcard = false)

        assertEquals(
            WebsiteRuleDraftResult.Valid(
                host = "xn--mnich-kva.example",
                type = WebsiteRuleType.EXACT_DOMAIN,
            ),
            result,
        )
    }

    @Test
    fun normalize_convertsSchemedUnicodeHostToAscii() {
        val result = WebsiteRuleNormalizer.normalize(
            input = "https://MÜNICH.example/lesen?utm=private#top",
            wildcard = false,
        )

        assertEquals(
            WebsiteRuleDraftResult.Valid(
                host = "xn--mnich-kva.example",
                type = WebsiteRuleType.EXACT_DOMAIN,
            ),
            result,
        )
    }

    @Test
    fun normalize_detectsExplicitWildcardPrefix() {
        val result = WebsiteRuleNormalizer.normalize(input = "*.News.Example", wildcard = false)

        assertEquals(
            WebsiteRuleDraftResult.Valid(
                host = "news.example",
                type = WebsiteRuleType.WILDCARD_SUBDOMAINS,
            ),
            result,
        )
    }

    @Test
    fun normalize_rejectsLocalPrivateIpAndAmbiguousHosts() {
        listOf(
            "localhost",
            "printer.local",
            "192.168.1.20",
            "10.0.0.1",
            "172.16.4.2",
            "8.8.8.8",
            "1.1.1.1",
            "999.1.1.1",
            "1.2.3",
            "[2001:db8::1]",
            "https://[2001:db8::1]/",
            "social",
            "bad_domain.example",
            "example..com",
            "reader@example.com",
            "news.example:bad-port",
            "what is example.com",
        ).forEach { input ->
            assertTrue(
                "$input should be invalid",
                WebsiteRuleNormalizer.normalize(input, wildcard = false) is WebsiteRuleDraftResult.Invalid,
            )
        }
    }

    @Test
    fun matches_exactDomainOnlyMatchesThatDomain() {
        val rule = WebsiteRule(
            id = "website-rule-11111111-1111-4111-8111-111111111111",
            type = WebsiteRuleType.EXACT_DOMAIN,
            host = "example.com",
            createdAtMillis = 1_000L,
            updatedAtMillis = 1_000L,
        )

        assertTrue(WebsiteRuleNormalizer.matches(rule, "https://example.com/path"))
        assertFalse(WebsiteRuleNormalizer.matches(rule, "www.example.com"))
        assertFalse(WebsiteRuleNormalizer.matches(rule, "notexample.com"))
        assertFalse(WebsiteRuleNormalizer.matches(rule, "example.com.evil.example"))
    }

    @Test
    fun matches_wildcardSubdomainsCanOptionallyIncludeApex() {
        val subdomainOnly = WebsiteRule(
            id = "website-rule-22222222-2222-4222-8222-222222222222",
            type = WebsiteRuleType.WILDCARD_SUBDOMAINS,
            host = "example.com",
            includeApex = false,
            createdAtMillis = 1_000L,
            updatedAtMillis = 1_000L,
        )
        val includeApex = subdomainOnly.copy(includeApex = true)

        assertTrue(WebsiteRuleNormalizer.matches(subdomainOnly, "m.example.com"))
        assertTrue(WebsiteRuleNormalizer.matches(subdomainOnly, "deep.m.example.com"))
        assertFalse(WebsiteRuleNormalizer.matches(subdomainOnly, "example.com"))
        assertTrue(WebsiteRuleNormalizer.matches(includeApex, "example.com"))
    }
}
