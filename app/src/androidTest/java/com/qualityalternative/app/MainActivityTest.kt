package com.qualityalternative.app

import android.content.Intent
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import com.qualityalternative.app.interception.FixtureTargetRegistry
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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
        completeOnboardingIfNeeded()
        waitForHome()
        composeRule.onNodeWithText("You're set up for quieter reading today.").assertIsDisplayed()

        scenario?.close()
        scenario = null
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        Thread.sleep(250)
        launchApp()

        waitForHome()
        composeRule.onNodeWithText("You're set up for quieter reading today.").assertIsDisplayed()
    }

    @Test
    fun systemInterceptionIntentShowsLiveInterventionForFixtureTarget() {
        launchFixtureSystemIntervention()

        composeRule.onNodeWithText("You reached for Fixture Feed One").assertIsDisplayed()
        composeRule.onNodeWithText("A BRIEF DETOUR, IF YOU'D LIKE ONE")
            .assertIsDisplayed()
        composeRule.onNodeWithTag("intervention-backup-action-0")
            .assertIsDisplayed()
            .assertIsEnabled()
        composeRule.onNodeWithTag("intervention-backup-action-1")
            .assertIsDisplayed()
            .assertIsEnabled()
        composeRule.waitUntil(timeoutMillis = 10_000) {
            hasNodeContaining("Read this")
        }
        composeRule.onNodeWithText("Read this", substring = true)
            .assertIsDisplayed()
            .assertIsEnabled()
        composeRule.onNodeWithText("Delay 15 min")
            .assertIsDisplayed()
            .assertIsEnabled()
        composeRule.onNodeWithText("Open Fixture Feed One")
            .assertIsDisplayed()
            .assertIsEnabled()
    }

    @Test
    fun systemInterventionDelayActionIsClickableWithoutScrolling() {
        launchFixtureSystemIntervention()

        composeRule.onNodeWithText("Delay 15 min")
            .assertIsDisplayed()
            .assertIsEnabled()
            .performClick()

        waitForHome()
        composeRule.onNodeWithTag("home-list")
            .performScrollToNode(hasText("FIXTURE FEED ONE DELAYED", substring = true))
    }

    @Test
    fun systemInterventionOpenAnywayActionIsClickableWithoutScrolling() {
        launchFixtureSystemIntervention()

        composeRule.onNodeWithText("Open Fixture Feed One")
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
            hasTag("reader-screen") || hasTag("external-handoff-screen")
        }
    }

    @Test
    fun homeShowsReadinessAndCompactLibrarySummary() {
        launchOnboardedApp()

        composeRule.onNodeWithText("You're set up for quieter reading today.")
            .assertIsDisplayed()
        composeRule.onNodeWithTag("home-list")
            .performScrollToNode(hasText("SETUP"))
        composeRule.onNodeWithText("SETUP")
            .assertIsDisplayed()
        composeRule.onNodeWithText("INTERCEPTING")
            .assertIsDisplayed()
        composeRule.onNodeWithTag("home-list")
            .performScrollToNode(hasText("YOUR LIBRARY"))
        composeRule.onNodeWithText("YOUR LIBRARY")
            .assertIsDisplayed()
        composeRule.onNodeWithTag("home-list")
            .performScrollToNode(hasText("Editorial picks"))
        composeRule.onNodeWithText("Editorial picks")
            .assertIsDisplayed()
        composeRule.onNodeWithTag("home-list")
            .performScrollToNode(hasText("Your added links"))
        composeRule.onNodeWithText("Your added links")
            .assertIsDisplayed()
        assertFalse(hasNode("Local analytics ledger"))
        assertFalse(hasNode("Local preferences"))
        assertFalse(hasNode("Recent replacement history"))
        composeRule.onNodeWithTag("home-list")
            .performScrollToNode(hasTestTag("home-add-link"))
        composeRule.onNodeWithTag("home-add-link").assertIsDisplayed()
        composeRule.onNodeWithTag("home-list")
            .performScrollToNode(hasText("Preview intervention"))
        composeRule.onAllNodesWithText("Preview intervention")
            .fetchSemanticsNodes()
            .also { nodes -> assertEquals(1, nodes.size) }
    }

    @Test
    fun addLinkKeepsSaveDisabledForInvalidUrl() {
        launchOnboardedApp()

        openAddLinkFromHome()

        composeRule.waitUntil(timeoutMillis = 10_000) { hasNodeContaining("Add to your quality") }
        composeRule.onNodeWithText("Add to your quality", substring = true)
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
            .assertIsNotEnabled()
    }

    @Test
    fun addLinkKeepsSaveDisabledForMissingTopic() {
        launchOnboardedApp()

        openAddLinkFromHome()

        composeRule.waitUntil(timeoutMillis = 10_000) { hasNodeContaining("Add to your quality") }
        composeRule.onNodeWithTag("add-link-url").performTextInput("https://example.com/essay")
        composeRule.onNodeWithTag("add-link-title").performTextInput("Saved essay")
        composeRule.onNodeWithText("Choose at least one topic so the app can rank this link.")
            .performScrollTo()
            .assertIsDisplayed()

        composeRule.onNodeWithTag("add-link-save")
            .assertIsNotEnabled()
    }

    @Test
    fun addLinkKeepsSaveDisabledForBlankUrl() {
        launchOnboardedApp()

        openAddLinkFromHome()

        composeRule.waitUntil(timeoutMillis = 10_000) { hasNodeContaining("Add to your quality") }
        composeRule.onNodeWithTag("add-link-title").performTextInput("Saved essay")
        composeRule.onNodeWithTag("add-link-topic-SCIENCE")
            .performScrollTo()
            .performClick()
        composeRule.onNodeWithText("Add the link you want to save.")
            .performScrollTo()
            .assertIsDisplayed()

        composeRule.onNodeWithTag("add-link-save")
            .assertIsNotEnabled()
    }

    @Test
    fun addLinkSavesValidLinkAndReturnsLibrary() {
        launchOnboardedApp()

        openAddLinkFromHome()

        composeRule.waitUntil(timeoutMillis = 10_000) { hasNodeContaining("Add to your quality") }
        composeRule.onNodeWithTag("add-link-url").performTextInput("https://example.com/essay")
        composeRule.onNodeWithTag("add-link-title").performTextInput("Saved essay")
        composeRule.onNodeWithTag("add-link-topic-SCIENCE")
            .performScrollTo()
            .performClick()
        composeRule.onNodeWithTag("add-link-save")
            .assertIsEnabled()
        composeRule.onNodeWithTag("add-link-save")
            .performClick()

        composeRule.waitUntil(timeoutMillis = 10_000) { hasNode("Ready when you are") }
        composeRule.onNodeWithTag("add-link-done").performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) { hasNode("Library") || hasNode("Saved essay") }
        composeRule.onNodeWithText("Saved essay").assertIsDisplayed()
    }

    @Test
    fun themeSettingSwitchesToDarkMode() {
        launchOnboardedApp()

        if (hasNode("Fix in Settings")) {
            composeRule.onNodeWithText("Fix in Settings").performClick()
        } else {
            composeRule.onNodeWithTag("tab-settings").performClick()
        }
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("settings-list") }
        composeRule.onNodeWithTag("settings-list")
            .performScrollToNode(hasTestTag("theme-DARK"))
        composeRule.onNodeWithTag("theme-DARK")
            .assertIsDisplayed()
            .performSemanticsAction(SemanticsActions.OnClick)
        composeRule.waitForIdle()

        composeRule.onNodeWithText("Dark")
            .assertIsDisplayed()
    }

    @Test
    fun readerFeedbackAndProgressUseFiniteReplacementCopy() {
        launchFixtureSystemIntervention()

        composeRule.onNodeWithText("Read this", substring = true)
            .assertIsDisplayed()
            .assertIsEnabled()
            .performClick()

        composeRule.waitUntil(timeoutMillis = 10_000) {
            hasTag("reader-screen")
        }
        composeRule.onNodeWithTag("reader-screen").assertIsDisplayed()
        composeRule.onNodeWithText("figure - editorial image", ignoreCase = true)
            .assertIsDisplayed()
        composeRule.onNodeWithTag("reader-list")
            .performScrollToNode(hasText("I'm done reading"))
        composeRule.onNodeWithText("I'm done reading")
            .assertIsDisplayed()
            .performClick()

        composeRule.waitUntil(timeoutMillis = 10_000) {
            hasNode("Two quick questions.")
        }
        composeRule.onNodeWithTag("feedback-screen").assertIsDisplayed()
        composeRule.onNodeWithText("Was this a good fit?").assertIsDisplayed()
        composeRule.onNodeWithText("Did it help you skip the scroll?").assertIsDisplayed()
        composeRule.onNodeWithText("Yes, more like this").performClick()
        composeRule.onNodeWithText("Yes").performClick()
        composeRule.onNodeWithTag("feedback-log")
            .assertIsDisplayed()
            .performClick()

        composeRule.waitUntil(timeoutMillis = 10_000) {
            hasTag("progress-card") || hasNode("Progress")
        }
        composeRule.onNodeWithTag("progress-card").assertIsDisplayed()
        composeRule.onNodeWithText("days converted").assertIsDisplayed()
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

    private fun hasTag(tag: String): Boolean {
        return runCatching {
            composeRule.onNodeWithTag(tag).fetchSemanticsNode()
            true
        }.getOrDefault(false)
    }

    private fun openAddLinkFromHome() {
        composeRule.onNodeWithTag("home-list")
            .performScrollToNode(hasTestTag("home-add-link"))
        composeRule.onNodeWithTag("home-add-link")
            .performClick()
    }

    private fun waitForHome() {
        composeRule.waitUntil(timeoutMillis = 10_000) {
            hasNode("You're set up for quieter reading today.")
        }
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
        completeOnboardingIfNeeded()
        waitForHome()
    }

    private fun completeOnboardingIfNeeded() {
        composeRule.waitUntil(timeoutMillis = 10_000) {
            hasNodeContaining("Turn an impulse") ||
                hasNode("You're set up for quieter reading today.")
        }
        if (!hasNodeContaining("Turn an impulse")) {
            return
        }
        composeRule.onNodeWithText("Begin").performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) { hasNode("Which apps pull at you?") }
        composeRule.onNodeWithText("Continue").performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) { hasNode("What would you rather read?") }
        composeRule.onNodeWithText("Continue").performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) { hasNode("How long should a session feel?") }
        composeRule.onNodeWithText("Continue").performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) { hasNode("Two small permissions.") }
        composeRule.onNodeWithText("Grant & finish").performClick()
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
            hasNode("You reached for Fixture Feed One")
        }
    }

    private fun resetPersistentState() = runBlocking {
        val targetContext = InstrumentationRegistry.getInstrumentation().targetContext
        (targetContext.applicationContext as QualityAlternativeApplication)
            .appContainer
            .resetPersistentStateForTests()
    }
}
