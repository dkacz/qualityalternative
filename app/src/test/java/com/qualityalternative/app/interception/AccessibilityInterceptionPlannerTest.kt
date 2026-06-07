package com.qualityalternative.app.interception

import com.qualityalternative.app.domain.model.DistractingApp
import com.qualityalternative.app.domain.model.WebsiteRule
import com.qualityalternative.app.domain.model.WebsiteRuleType
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AccessibilityInterceptionPlannerTest {
    @After
    fun tearDown() {
        InterceptionRuntimeGate.clearAll()
    }

    @Test
    fun websiteSuppressionFallsThroughToSelectedChromeAppTarget() {
        val nowMillis = 10_000L
        val chromeAppTarget = DistractingApp(
            packageName = VerifiedBrowserHostAdapter.CHROME_PACKAGE,
            displayName = "Chrome",
        )
        val websiteTarget = requireNotNull(
            WebsiteInterceptionResolver.resolve(
                browserPackage = VerifiedBrowserHostAdapter.CHROME_PACKAGE,
                browserDisplayName = "Chrome",
                observedHost = "news.example.com",
                websiteRules = listOf(
                    WebsiteRule(
                        id = "website-rule-00000000-0000-0000-0000-000000000010",
                        type = WebsiteRuleType.WILDCARD_SUBDOMAINS,
                        host = "example.com",
                        includeApex = false,
                        enabled = true,
                        createdAtMillis = 1L,
                        updatedAtMillis = 1L,
                    ),
                ),
            ),
        )

        InterceptionRuntimeGate.suppressPackage(
            targetAppPackage = websiteTarget.targetApp.packageName,
            targetKey = websiteTarget.suppressionKey,
            untilMillis = nowMillis + 60_000L,
        )

        assertTrue(
            AccessibilityInterceptionPlanner.shouldEvaluateAppTargetAfterWebsiteResult(
                InterceptionProcessingResult.Suppressed,
            ),
        )
        assertFalse(
            InterceptionRuntimeGate.shouldSuppress(
                targetAppPackage = chromeAppTarget.packageName,
                targetKey = chromeAppTarget.packageName,
                nowMillis = nowMillis,
            ),
        )
        assertEquals(
            chromeAppTarget,
            InterceptionTargetResolver.resolve(
                foregroundPackage = VerifiedBrowserHostAdapter.CHROME_PACKAGE,
                foregroundClass = "org.chromium.chrome.browser.ChromeTabbedActivity",
                selectedPackages = setOf(VerifiedBrowserHostAdapter.CHROME_PACKAGE),
                knownTargets = listOf(chromeAppTarget),
                appPackage = "com.qualityalternative.app",
            ),
        )
    }

    @Test
    fun onlySuppressedWebsiteResultFallsThroughToAppTarget() {
        assertFalse(
            AccessibilityInterceptionPlanner.shouldEvaluateAppTargetAfterWebsiteResult(
                InterceptionProcessingResult.Handled,
            ),
        )
        assertFalse(
            AccessibilityInterceptionPlanner.shouldEvaluateAppTargetAfterWebsiteResult(
                InterceptionProcessingResult.Duplicate,
            ),
        )
        assertFalse(
            AccessibilityInterceptionPlanner.shouldEvaluateAppTargetAfterWebsiteResult(
                InterceptionProcessingResult.NotReady,
            ),
        )
    }
}
