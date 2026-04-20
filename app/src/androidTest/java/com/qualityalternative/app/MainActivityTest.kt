package com.qualityalternative.app

import android.content.Intent
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import com.qualityalternative.app.interception.FixtureTargetRegistry
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
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
        launchFixtureSystemIntervention()

        composeRule.onNodeWithText("Pause before Fixture Feed One").assertIsDisplayed()
        composeRule.onNodeWithText("One thoughtful alternative")
            .assertIsDisplayed()
        composeRule.onNodeWithText("Two backup choices")
            .assertIsDisplayed()
        assertEquals(2, composeRule.onAllNodesWithText("Choose backup").fetchSemanticsNodes().size)
        composeRule.onNodeWithTag("intervention-backup-action-0")
            .assertIsDisplayed()
            .assertIsEnabled()
        composeRule.onNodeWithTag("intervention-backup-action-1")
            .assertIsDisplayed()
            .assertIsEnabled()
        composeRule.onNodeWithText("Your call, deliberately made")
            .assertIsDisplayed()
        composeRule.waitUntil(timeoutMillis = 10_000) {
            hasNode("Read now") || hasNode("Open link")
        }
        val primaryActionLabel = if (hasNode("Read now")) "Read now" else "Open link"
        composeRule.onAllNodesWithText(primaryActionLabel)[0]
            .assertIsDisplayed()
            .assertIsEnabled()
        composeRule.onNodeWithText("Delay for 15 minutes")
            .assertIsDisplayed()
            .assertIsEnabled()
        composeRule.onNodeWithText("Open anyway")
            .assertIsDisplayed()
            .assertIsEnabled()
    }

    @Test
    fun systemInterventionDelayActionIsClickableWithoutScrolling() {
        launchFixtureSystemIntervention()

        composeRule.onNodeWithText("Delay for 15 minutes")
            .assertIsDisplayed()
            .assertIsEnabled()
            .performClick()

        composeRule.waitUntil(timeoutMillis = 10_000) {
            hasNode("Quality Alternative") && hasNodeContaining("Fixture Feed One delayed until")
        }
    }

    @Test
    fun systemInterventionOpenAnywayActionIsClickableWithoutScrolling() {
        launchFixtureSystemIntervention()

        composeRule.onNodeWithText("Open anyway")
            .assertIsDisplayed()
            .assertIsEnabled()
            .performClick()

        composeRule.waitUntil(timeoutMillis = 10_000) {
            scenario?.state == androidx.lifecycle.Lifecycle.State.DESTROYED
        }
    }

    @Test
    fun systemInterventionBackupActionIsClickableWithoutScrolling() {
        launchFixtureSystemIntervention()

        composeRule.onNodeWithTag("intervention-backup-action-0")
            .assertIsDisplayed()
            .assertIsEnabled()
            .performClick()

        composeRule.waitUntil(timeoutMillis = 10_000) {
            hasNode("Finish session") || hasNode("Open external link")
        }
    }

    @Test
    fun homeShowsReadinessAndCompactLibrarySummary() {
        launchOnboardedApp()

        composeRule.onNodeWithText("You're set up for quieter reading today.")
            .assertIsDisplayed()
        composeRule.onNodeWithText("Setup")
            .assertIsDisplayed()
        composeRule.onNodeWithText("Your library")
            .assertIsDisplayed()
        composeRule.onNodeWithText("Editorial picks")
            .assertIsDisplayed()
        composeRule.onNodeWithText("Your added links")
            .assertIsDisplayed()
        composeRule.onNodeWithTag("home-add-link")
            .assertIsDisplayed()
        composeRule.onNodeWithTag("home-list")
            .performScrollToNode(hasText("Preview intervention"))
        composeRule.onAllNodesWithText("Preview intervention")
            .fetchSemanticsNodes()
            .also { nodes -> assertEquals(2, nodes.size) }
    }

    @Test
    fun addLinkKeepsSaveDisabledForInvalidUrl() {
        launchOnboardedApp()

        composeRule.onNodeWithTag("home-add-link")
            .performClick()

        composeRule.waitUntil(timeoutMillis = 10_000) {
            hasNode("Add a replacement link")
        }
        composeRule.onNodeWithText("Add to your quality alternative.")
            .assertIsDisplayed()
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

        composeRule.onNodeWithTag("home-list")
            .performScrollToNode(hasText("Personal library: 1 link saved."))
        composeRule.onNodeWithText("Personal library: 1 link saved.").assertIsDisplayed()
        composeRule.onNodeWithTag("home-list")
            .performScrollToNode(hasText("Saved essay"))
        composeRule.onNodeWithText("Saved essay").assertIsDisplayed()
    }

    @Test
    fun themeSettingSwitchesToDarkMode() {
        launchOnboardedApp()

        composeRule.onNodeWithTag("home-list")
            .performScrollToNode(hasText("Theme: Light"))
        composeRule.onNodeWithText("Theme: Light").assertIsDisplayed()
        composeRule.onNodeWithTag("theme-DARK")
            .performClick()

        composeRule.waitUntil(timeoutMillis = 10_000) {
            hasNode("Theme: Dark")
        }
        composeRule.onNodeWithTag("theme-DARK")
            .assertIsSelected()
        composeRule.onNodeWithText("Theme: Dark")
            .assertIsDisplayed()
    }

    private fun hasNode(text: String): Boolean {
        return runCatching {
            composeRule.onNodeWithText(text).fetchSemanticsNode()
            true
        }.getOrDefault(false)
    }

    private fun hasNodeContaining(text: String): Boolean {
        return runCatching {
            composeRule.onNodeWithText(text, substring = true).fetchSemanticsNode()
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

    private fun launchFixtureSystemIntervention() {
        launchOnboardedApp()

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
    }

    private fun resetPersistentState() = runBlocking {
        val targetContext = InstrumentationRegistry.getInstrumentation().targetContext
        (targetContext.applicationContext as QualityAlternativeApplication)
            .appContainer
            .resetPersistentStateForTests()
    }
}
