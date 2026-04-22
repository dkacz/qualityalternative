package com.qualityalternative.app

import android.content.Intent
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performSemanticsAction
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import com.qualityalternative.app.domain.model.DurationBucket
import com.qualityalternative.app.domain.model.OnboardingSelection
import com.qualityalternative.app.domain.model.TopicTag
import com.qualityalternative.app.interception.FixtureTargetRegistry
import java.io.File
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class VisualQaScreenshotTest {
    @get:Rule
    val composeRule = createEmptyComposeRule()

    private var scenario: ActivityScenario<MainActivity>? = null
    private lateinit var screenshotDir: File

    @Before
    fun resetAppState() {
        resetPersistentState()
        val targetContext = InstrumentationRegistry.getInstrumentation().targetContext
        screenshotDir = File(targetContext.filesDir, "visual-qa/content-display")
        screenshotDir.deleteRecursively()
        screenshotDir.mkdirs()
    }

    @After
    fun tearDown() {
        scenario?.close()
        scenario = null
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        resetPersistentState()
    }

    @Test
    fun captureCoreContentScreensInLightAndDark() {
        launchOnboardedApp()
        seedAttentionClassicsSelection()

        capture("01_home_light")

        openTab("tab-library", "library-list")
        composeRule.onNodeWithTag("library-list")
            .performScrollToNode(hasText("Start With What Is Yours"))
        capture("02_library_attention_light")

        launchFixtureSystemIntervention()
        capture("03_intervention_light")

        composeRule.onNodeWithText("Read this", substring = true).performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("reader-screen") }
        assertAttentionClassicsReaderCopyIsDisplayed()
        capture("04_reader_attention_light")

        composeRule.onNodeWithTag("reader-list")
            .performScrollToNode(hasText("I'm done reading"))
        composeRule.onNodeWithText("I'm done reading").performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("feedback-screen") }
        capture("05_feedback_light")

        composeRule.onNodeWithText("Skip").performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("progress-list") }
        Thread.sleep(6_000)
        capture("06_progress_light")

        openTab("tab-settings", "settings-list")
        composeRule.onNodeWithTag("settings-list")
            .performScrollToNode(hasTestTag("theme-DARK"))
        composeRule.onNodeWithTag("theme-DARK")
            .performSemanticsAction(SemanticsActions.OnClick)
        composeRule.waitForIdle()
        capture("07_settings_dark")

        openTab("tab-home", "home-list")
        capture("08_home_dark")

        launchFixtureSystemIntervention()
        capture("09_intervention_dark")

        composeRule.onNodeWithText("Read this", substring = true).performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("reader-screen") }
        assertAttentionClassicsReaderCopyIsDisplayed()
        capture("10_reader_attention_dark")
    }

    private fun capture(name: String) {
        composeRule.waitForIdle()
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        Thread.sleep(350)
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        val file = File(screenshotDir, "$name.png")
        assertTrue("Could not capture $name into ${file.absolutePath}", device.takeScreenshot(file))
    }

    private fun openTab(tabTag: String, expectedScreenTag: String) {
        composeRule.onNodeWithTag(tabTag, useUnmergedTree = true)
            .performSemanticsAction(SemanticsActions.OnClick)
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag(expectedScreenTag) }
    }

    private fun launchApp(intent: Intent? = null) {
        val launchIntent = intent ?: Intent(
            InstrumentationRegistry.getInstrumentation().targetContext,
            MainActivity::class.java,
        ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        scenario = ActivityScenario.launch(launchIntent)
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
    }

    private fun launchOnboardedApp() {
        launchApp()
        completeOnboardingIfNeeded()
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("home-list") }
    }

    private fun completeOnboardingIfNeeded() {
        composeRule.waitUntil(timeoutMillis = 10_000) {
            hasNodeContaining("Turn an impulse") ||
                hasNode("You're set up for quieter reading today.") ||
                hasNode("Interception needs one more step.")
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

    private fun seedAttentionClassicsSelection() = runBlocking {
        val repository = (InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as QualityAlternativeApplication)
            .appContainer
            .settingsRepository
        repository.saveOnboardingSelection(
            OnboardingSelection(
                selectedAppPackages = setOf(FixtureTargetRegistry.fixtureDistractors.first().packageName),
                preferredTopics = setOf(TopicTag.PHILOSOPHY, TopicTag.PSYCHOLOGY, TopicTag.ESSAYS),
                preferredDurationBucket = DurationBucket.QUICK,
                selectedPackIds = setOf("attention-classics-v1"),
            ),
        )
    }

    private fun assertAttentionClassicsReaderCopyIsDisplayed() {
        val titles = listOf(
            "Start With What Is Yours",
            "The Morning Test",
            "The Flywheel of Habit",
            "Live Deliberately",
            "Walk Before You Scroll",
            "Trust the First Honest Thought",
            "The Desert Resets the Eye",
            "Read to Weigh",
        )
        val authors = listOf(
            "Epictetus",
            "Marcus Aurelius",
            "William James",
            "Henry David Thoreau",
            "Ralph Waldo Emerson",
            "Mary Austin",
            "Francis Bacon",
        )

        assertTrue("Expected an Attention Classics title in the reader", titles.any(::hasNode))
        assertTrue("Expected an author-facing source label in the reader", authors.any(::hasNodeContaining))
        assertTrue("Reader should not show provenance-heavy Project Gutenberg label", !hasNodeContaining("Project Gutenberg"))
    }

    private fun resetPersistentState() = runBlocking {
        val targetContext = InstrumentationRegistry.getInstrumentation().targetContext
        (targetContext.applicationContext as QualityAlternativeApplication)
            .appContainer
            .resetPersistentStateForTests()
    }

    private fun hasNode(text: String): Boolean = runCatching {
        composeRule.onNodeWithText(text).fetchSemanticsNode()
        true
    }.getOrDefault(false)

    private fun hasNodeContaining(text: String): Boolean = runCatching {
        composeRule.onNodeWithText(text, substring = true, ignoreCase = true).fetchSemanticsNode()
        true
    }.getOrDefault(false)

    private fun hasTag(tag: String): Boolean = runCatching {
        composeRule.onNodeWithTag(tag).fetchSemanticsNode()
        true
    }.getOrDefault(false)
}
