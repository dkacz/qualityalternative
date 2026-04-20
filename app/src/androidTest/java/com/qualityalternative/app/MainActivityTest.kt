package com.qualityalternative.app

import android.content.Intent
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import com.qualityalternative.app.interception.FixtureTargetRegistry
import kotlinx.coroutines.runBlocking
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
        resetPersistentState()
    }

    @After
    fun tearDown() {
        scenario?.close()
        scenario = null
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        resetPersistentState()
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
        composeRule.waitUntil(timeoutMillis = 10_000) {
            hasNode("Read now") || hasNode("Open link")
        }
        val primaryActionLabel = if (hasNode("Read now")) "Read now" else "Open link"
        composeRule.onAllNodesWithText(primaryActionLabel)[0]
            .performScrollTo()
            .assertIsDisplayed()
    }

    @Test
    fun addLinkKeepsSaveDisabledForInvalidUrl() {
        launchOnboardedApp()

        composeRule.onNodeWithTag("home-add-link")
            .performClick()

        composeRule.waitUntil(timeoutMillis = 10_000) {
            hasNode("Add a replacement link")
        }
        composeRule.onNodeWithTag("add-link-url").performTextInput("quality://bad")
        composeRule.onNodeWithTag("add-link-title").performTextInput("Saved essay")
        composeRule.onNodeWithTag("add-link-topic-SCIENCE")
            .performScrollTo()
            .performClick()
        composeRule.onNodeWithText("Use a normal web link starting with http or https.")
            .performScrollTo()
            .assertIsDisplayed()

        composeRule.onNodeWithTag("add-link-save")
            .performScrollTo()
            .assertIsNotEnabled()
    }

    @Test
    fun addLinkKeepsSaveDisabledForMissingTopic() {
        launchOnboardedApp()

        composeRule.onNodeWithTag("home-add-link")
            .performClick()

        composeRule.waitUntil(timeoutMillis = 10_000) {
            hasNode("Add a replacement link")
        }
        composeRule.onNodeWithTag("add-link-url").performTextInput("https://example.com/essay")
        composeRule.onNodeWithTag("add-link-title").performTextInput("Saved essay")
        composeRule.onNodeWithText("Choose at least one topic so the app can rank this link.")
            .performScrollTo()
            .assertIsDisplayed()

        composeRule.onNodeWithTag("add-link-save")
            .performScrollTo()
            .assertIsNotEnabled()
    }

    @Test
    fun addLinkKeepsSaveDisabledForBlankUrl() {
        launchOnboardedApp()

        composeRule.onNodeWithTag("home-add-link")
            .performClick()

        composeRule.waitUntil(timeoutMillis = 10_000) {
            hasNode("Add a replacement link")
        }
        composeRule.onNodeWithTag("add-link-title").performTextInput("Saved essay")
        composeRule.onNodeWithTag("add-link-topic-SCIENCE")
            .performScrollTo()
            .performClick()
        composeRule.onNodeWithText("Add the link you want to save.")
            .performScrollTo()
            .assertIsDisplayed()

        composeRule.onNodeWithTag("add-link-save")
            .performScrollTo()
            .assertIsNotEnabled()
    }

    @Test
    fun addLinkSavesValidLinkAndReturnsHome() {
        launchOnboardedApp()

        composeRule.onNodeWithTag("home-add-link")
            .performClick()

        composeRule.waitUntil(timeoutMillis = 10_000) {
            hasNode("Add a replacement link")
        }
        composeRule.onNodeWithTag("add-link-url").performTextInput("https://example.com/essay")
        composeRule.onNodeWithTag("add-link-title").performTextInput("Saved essay")
        composeRule.onNodeWithTag("add-link-topic-SCIENCE")
            .performScrollTo()
            .performClick()
        composeRule.onNodeWithTag("add-link-save")
            .performScrollTo()
            .assertIsEnabled()
        composeRule.onNodeWithTag("add-link-save")
            .performScrollTo()
            .performClick()

        composeRule.waitUntil(timeoutMillis = 10_000) {
            hasNode("Quality Alternative") && hasNode("Personal library: 1 link saved.")
        }

        composeRule.onNodeWithText("Personal library: 1 link saved.").assertIsDisplayed()
    }

    @Test
    fun themeSettingSwitchesToInkMode() {
        launchOnboardedApp()

        composeRule.onNodeWithText("Theme: Light")
            .assertIsDisplayed()
        composeRule.onNodeWithTag("theme-INK")
            .performClick()

        composeRule.waitUntil(timeoutMillis = 10_000) {
            hasNode("Theme: Ink")
        }
        composeRule.onNodeWithTag("theme-INK")
            .assertIsSelected()
        composeRule.onNodeWithText("Theme: Ink")
            .assertIsDisplayed()
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

    private fun launchOnboardedApp() {
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
    }

    private fun resetPersistentState() = runBlocking {
        val targetContext = InstrumentationRegistry.getInstrumentation().targetContext
        (targetContext.applicationContext as QualityAlternativeApplication)
            .appContainer
            .resetPersistentStateForTests()
    }
}
