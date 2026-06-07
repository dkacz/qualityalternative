package com.qualityalternative.app.domain.model

import com.qualityalternative.app.analytics.InMemoryAnalyticsTracker
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AnalyticsPrivacyGuardTest {
    @Test
    fun toRemotePayload_keepsWebsiteRuleClassButDropsUrlHostPackageAndRuleId() {
        val event = AnalyticsEvent(
            type = AnalyticsEventType.INTERVENTION_SHOWN,
            timestampMillis = 10_000L,
            interventionId = "intervention-1",
            targetAppPackage = "com.android.chrome",
            primaryContentId = "editorial-deep-work",
            metadata = mapOf(
                "targetType" to "website_domain",
                "browserPackage" to "com.android.chrome",
                "browserSupportStatus" to "verified_host",
                "websiteRuleType" to WebsiteRuleType.EXACT_DOMAIN.name,
                "websiteRuleIncludesApex" to "false",
                "websiteRuleId" to "website-rule-11111111-1111-4111-8111-111111111111",
                "foregroundPackage" to "com.android.chrome",
                "foregroundClass" to "org.chromium.chrome.browser.ChromeTabbedActivity",
                "rawUrl" to "https://news.example/private/path?token=abc",
                "observedHost" to "news.example",
                "pageTitle" to "Private article title",
                "primary_sourceType" to ContentSourceType.EDITORIAL.name,
            ),
        )

        val payload = event.toRemoteAnalyticsPayload()
        val rendered = payload.renderForTest()

        assertEquals("supported_browser_website", payload.targetClass)
        assertEquals("website_domain", payload.metadata["targetType"])
        assertEquals("verified_host", payload.metadata["browserSupportStatus"])
        assertEquals(WebsiteRuleType.EXACT_DOMAIN.name, payload.metadata["websiteRuleType"])
        assertEquals("false", payload.metadata["websiteRuleIncludesApex"])
        assertEquals(ContentSourceType.EDITORIAL.name, payload.metadata["primary_sourceType"])
        assertFalse(rendered.contains("com.android.chrome"))
        assertFalse(rendered.contains("news.example"))
        assertFalse(rendered.contains("https://"))
        assertFalse(rendered.contains("Private article title"))
        assertFalse(rendered.contains("website-rule-11111111"))
        assertTrue(AnalyticsPrivacyGuard.unsafeRemoteFields(payload).isEmpty())
    }

    @Test
    fun toRemotePayload_classifiesCustomAppTargetWithoutExportingPackageIdentity() {
        val event = AnalyticsEvent(
            type = AnalyticsEventType.TARGET_APP_FOREGROUND_DETECTED,
            timestampMillis = 12_000L,
            targetAppPackage = "com.example.deepwork",
            metadata = mapOf(
                "triggerSource" to "accessibility_service",
                "interceptionStage" to "sprint26_custom_app",
                "foregroundPackage" to "com.example.deepwork",
                "foregroundClass" to "com.example.deepwork.MainActivity",
                "targetType" to "custom_app",
                "selectedPackCount" to "2",
            ),
        )

        val payload = event.toRemoteAnalyticsPayload()
        val rendered = payload.renderForTest()

        assertEquals("custom_app", payload.targetClass)
        assertEquals("accessibility_service", payload.metadata["triggerSource"])
        assertEquals("2", payload.metadata["selectedPackCount"])
        assertFalse(rendered.contains("com.example.deepwork"))
        assertFalse(rendered.contains("MainActivity"))
        assertTrue(AnalyticsPrivacyGuard.unsafeRemoteFields(payload).isEmpty())
    }

    @Test
    fun toRemotePayload_doesNotEchoUnknownTargetTypeIntoRemoteTargetClass() {
        val event = AnalyticsEvent(
            type = AnalyticsEventType.INTERVENTION_SHOWN,
            timestampMillis = 12_500L,
            targetAppPackage = "com.private.browser",
            metadata = mapOf(
                "targetType" to "example.com",
                "triggerSource" to "accessibility_service",
            ),
        )

        val payload = event.toRemoteAnalyticsPayload()
        val rendered = payload.renderForTest()

        assertEquals("app_target", payload.targetClass)
        assertFalse(payload.metadata.containsKey("targetType"))
        assertEquals("accessibility_service", payload.metadata["triggerSource"])
        assertFalse(rendered.contains("example.com"))
        assertFalse(rendered.contains("com.private.browser"))
        assertTrue(AnalyticsPrivacyGuard.unsafeRemoteFields(payload).isEmpty())
    }

    @Test
    fun toRemotePayload_rejectsIpPortTrailingDotAndUnicodeHostMetadataValues() {
        val event = AnalyticsEvent(
            type = AnalyticsEventType.INTERVENTION_SHOWN,
            timestampMillis = 12_750L,
            metadata = mapOf(
                "origin" to "192.168.1.1",
                "failureReason" to "2001:db8::1",
                "action" to "example.com:443",
                "availability" to "example.com.",
                "format" to "bücher.example",
                "reason" to "xn--e1afmkfd.xn--p1ai",
                "renderMode" to "example.xn--p1ai",
                "sourceType" to "192.168.1.1.",
                "targetType" to "custom_app",
            ),
        )

        val payload = event.toRemoteAnalyticsPayload()
        val rendered = payload.renderForTest()

        assertEquals("custom_app", payload.targetClass)
        assertEquals("custom_app", payload.metadata["targetType"])
        assertFalse(payload.metadata.containsKey("origin"))
        assertFalse(payload.metadata.containsKey("failureReason"))
        assertFalse(payload.metadata.containsKey("action"))
        assertFalse(payload.metadata.containsKey("availability"))
        assertFalse(payload.metadata.containsKey("format"))
        assertFalse(payload.metadata.containsKey("reason"))
        assertFalse(payload.metadata.containsKey("renderMode"))
        assertFalse(payload.metadata.containsKey("sourceType"))
        assertFalse(rendered.contains("192.168.1.1"))
        assertFalse(rendered.contains("2001:db8::1"))
        assertFalse(rendered.contains("example.com:443"))
        assertFalse(rendered.contains("example.com."))
        assertFalse(rendered.contains("bücher.example"))
        assertFalse(rendered.contains("xn--e1afmkfd.xn--p1ai"))
        assertFalse(rendered.contains("example.xn--p1ai"))
        assertTrue(AnalyticsPrivacyGuard.unsafeRemoteFields(payload).isEmpty())
    }

    @Test
    fun unsafeRemoteFields_checksTopLevelPayloadFields() {
        val unsafePayload = RemoteAnalyticsPayload(
            type = AnalyticsEventType.INTERVENTION_SHOWN,
            timestampMillis = 13_000L,
            semanticKey = "https://example.com/private",
            targetClass = "example.com",
            primaryContentId = "com.private.content",
            backupContentIds = listOf("192.168.1.1"),
            contentId = "bücher.example",
            metadata = mapOf(
                "origin" to "xn--e1afmkfd.xn--p1ai",
                "reason" to "example.xn--p1ai",
                "action" to "192.168.1.1.",
            ),
        )

        val unsafeFields = AnalyticsPrivacyGuard.unsafeRemoteFields(unsafePayload)

        assertTrue(unsafeFields.contains("semanticKey"))
        assertTrue(unsafeFields.contains("targetClass"))
        assertTrue(unsafeFields.contains("targetClass.value"))
        assertTrue(unsafeFields.contains("primaryContentId"))
        assertTrue(unsafeFields.contains("backupContentIds[0]"))
        assertTrue(unsafeFields.contains("contentId"))
        assertTrue(unsafeFields.contains("metadata.origin.value"))
        assertTrue(unsafeFields.contains("metadata.reason.value"))
        assertTrue(unsafeFields.contains("metadata.action.value"))
    }

    @Test
    fun scrubDebugValue_redactsUrlsHostsPackagesAndKeepsSafeCounters() {
        assertEquals("[redacted]", AnalyticsPrivacyGuard.scrubDebugValue("https://example.com/private"))
        assertEquals("[redacted]", AnalyticsPrivacyGuard.scrubDebugValue("example.com"))
        assertEquals("[redacted]", AnalyticsPrivacyGuard.scrubDebugValue("192.168.1.1"))
        assertEquals("[redacted]", AnalyticsPrivacyGuard.scrubDebugValue("2001:db8::1"))
        assertEquals("[redacted]", AnalyticsPrivacyGuard.scrubDebugValue("example.com:443"))
        assertEquals("[redacted]", AnalyticsPrivacyGuard.scrubDebugValue("example.com."))
        assertEquals("[redacted]", AnalyticsPrivacyGuard.scrubDebugValue("bücher.example"))
        assertEquals("[redacted]", AnalyticsPrivacyGuard.scrubDebugValue("xn--e1afmkfd.xn--p1ai"))
        assertEquals("[redacted]", AnalyticsPrivacyGuard.scrubDebugValue("example.xn--p1ai"))
        assertEquals("[redacted]", AnalyticsPrivacyGuard.scrubDebugValue("192.168.1.1."))
        assertEquals("[redacted]", AnalyticsPrivacyGuard.scrubDebugValue("com.example.deepwork"))
        assertEquals("verified_host", AnalyticsPrivacyGuard.scrubDebugValue("verified_host"))
        assertEquals("42", AnalyticsPrivacyGuard.scrubDebugValue("42"))
    }

    @Test
    fun analyticsTracker_exposesRemoteSafePayloadsWithoutLocalPackageIdentity() {
        val tracker = InMemoryAnalyticsTracker()
        tracker.record(
            AnalyticsEvent(
                type = AnalyticsEventType.OPEN_ANYWAY_SELECTED,
                timestampMillis = 13_000L,
                targetAppPackage = "com.private.social",
                metadata = mapOf(
                    "targetType" to "custom_app",
                    "foregroundPackage" to "com.private.social",
                    "openAnywayUnlockMinutes" to "60",
                ),
            ),
        )

        val payload = tracker.allRemoteSafePayloads().single()
        val rendered = payload.renderForTest()

        assertEquals("custom_app", payload.targetClass)
        assertEquals("60", payload.metadata["openAnywayUnlockMinutes"])
        assertFalse(rendered.contains("com.private.social"))
    }

    @Test
    fun analyticsTracker_exposesRemoteSafeDebugSummariesThroughScrubber() {
        val tracker = InMemoryAnalyticsTracker()
        tracker.record(
            AnalyticsEvent(
                type = AnalyticsEventType.INTERVENTION_SHOWN,
                timestampMillis = 14_000L,
                targetAppPackage = "com.private.browser",
                metadata = mapOf(
                    "targetType" to "example.com",
                    "origin" to "https://example.com/private",
                    "triggerSource" to "accessibility_service",
                ),
            ),
        )

        val summary = tracker.allRemoteSafeDebugSummaries().single()

        assertTrue(summary.contains("targetClass=app_target"))
        assertTrue(summary.contains("metadata.triggerSource=accessibility_service"))
        assertFalse(summary.contains("com.private.browser"))
        assertFalse(summary.contains("example.com"))
        assertFalse(summary.contains("https://"))
    }

    private fun RemoteAnalyticsPayload.renderForTest(): String {
        return listOf(
            type.name,
            semanticKey.orEmpty(),
            interventionId.orEmpty(),
            sessionId.orEmpty(),
            targetClass.orEmpty(),
            primaryContentId.orEmpty(),
            backupContentIds.joinToString(","),
            contentId.orEmpty(),
            metadata.entries.joinToString(";") { (key, value) -> "$key=$value" },
        ).joinToString("|")
    }
}
