package com.qualityalternative.app

import android.content.Intent
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import com.qualityalternative.app.interception.FixtureTargetRegistry
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class MainActivityTest {
    @get:Rule
    val composeRule = createEmptyComposeRule()

    private var scenario: ActivityScenario<MainActivity>? = null

    @Before
    fun resetAppState() {
        val targetContext = InstrumentationRegistry.getInstrumentation().targetContext
        targetContext.deleteDatabase("quality_alternative.db")
        targetContext.filesDir.resolve("datastore").deleteRecursively()
    }

    @After
    fun tearDown() {
        scenario?.close()
        scenario = null
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
    }

    @Test
    fun onboardingCompletesAndPersistsAfterColdRelaunch() {
        launchApp()
        composeRule.waitUntil(timeoutMillis = 10_000) {
            hasNode("Let’s set up your replacement loop") || hasNode("Quality Alternative")
        }

        if (hasNode("Let’s set up your replacement loop")) {
            composeRule.onNodeWithText("Let’s set up your replacement loop").assertIsDisplayed()
            composeRule.onNodeWithText("Complete setup", useUnmergedTree = true)
                .performScrollTo()
                .performClick()
        }

        composeRule.waitUntil(timeoutMillis = 10_000) {
            hasNode("Quality Alternative")
        }

        composeRule.onNodeWithText("Quality Alternative").assertIsDisplayed()

        scenario?.close()
        scenario = null
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        Thread.sleep(250)
        launchApp()

        composeRule.waitUntil(timeoutMillis = 10_000) {
            hasNode("Quality Alternative")
        }

        composeRule.onNodeWithText("Quality Alternative").assertIsDisplayed()
    }

    @Test
    fun systemInterceptionIntentShowsLiveInterventionForFixtureTarget() {
        launchApp()
        composeRule.waitUntil(timeoutMillis = 10_000) {
            hasNode("Let’s set up your replacement loop") || hasNode("Quality Alternative")
        }
        if (hasNode("Let’s set up your replacement loop")) {
            composeRule.onNodeWithText("Complete setup", useUnmergedTree = true)
                .performScrollTo()
                .performClick()
        }
        composeRule.waitUntil(timeoutMillis = 10_000) {
            hasNode("Quality Alternative")
        }

        scenario?.close()
        scenario = null
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        Thread.sleep(250)

        val targetContext = InstrumentationRegistry.getInstrumentation().targetContext
        launchApp(
            MainActivity.createSystemInterceptionIntent(
                context = targetContext,
                targetAppPackage = FixtureTargetRegistry.fixtureDistractors.first().packageName,
            ),
        )

        composeRule.waitUntil(timeoutMillis = 10_000) {
            hasNode("Pause before Fixture Feed One")
        }

        composeRule.onNodeWithText("Pause before Fixture Feed One").assertIsDisplayed()
        composeRule.onNodeWithText("Read now").assertIsDisplayed()
    }

    private fun hasNode(text: String): Boolean {
        return runCatching {
            composeRule.onNodeWithText(text).fetchSemanticsNode()
            true
        }.getOrDefault(false)
    }

    private fun launchApp(intent: Intent? = null) {
        val launchIntent = intent ?: Intent(
            InstrumentationRegistry.getInstrumentation().targetContext,
            MainActivity::class.java,
        ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        scenario = ActivityScenario.launch(
            launchIntent,
        )
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
    }
}
