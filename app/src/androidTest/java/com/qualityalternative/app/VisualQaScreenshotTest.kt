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
    private lateinit var legacyScreenshotDir: File

    @Before
    fun resetAppState() {
        resetPersistentState()
        val targetContext = InstrumentationRegistry.getInstrumentation().targetContext
        screenshotDir = File(targetContext.filesDir, "visual-qa/sprint8-content-expansion")
        screenshotDir.deleteRecursively()
        screenshotDir.mkdirs()
        legacyScreenshotDir = File(targetContext.filesDir, "visual-qa/content-display")
        legacyScreenshotDir.deleteRecursively()
        legacyScreenshotDir.mkdirs()
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
        seedAllSharedContentSelection()

        capture("01_home_light")

        openTab("tab-library", "library-list")
        composeRule.onNodeWithTag("library-list")
            .performScrollToNode(hasText("A Naturalist Notices Everything"))
        capture("02_library_mixed_light")

        seedLinkOnlySelection()
        launchFixtureSystemIntervention()
        capture("03_intervention_link_only_light")

        composeRule.onNodeWithText("Open link", substring = true).performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("external-handoff-screen") }
        assertLinkOnlyHandoffCopyIsDisplayed()
        capture("04_external_handoff_light")

        seedPublicDomainExpansionSelection()
        launchFixtureSystemIntervention()
        composeRule.onNodeWithText("Read this", substring = true).performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("reader-screen") }
        assertPublicDomainExpansionReaderCopyIsDisplayed()
        capture("05_reader_renderable_v2_light")

        composeRule.onNodeWithTag("reader-list")
            .performScrollToNode(hasText("I'm done reading"))
        composeRule.onNodeWithText("I'm done reading").performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("feedback-screen") }
        capture("06_feedback_light")

        composeRule.onNodeWithText("Skip").performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("progress-list") }
        Thread.sleep(6_000)
        capture("07_progress_light")

        seedMeditationSelection()
        launchFixtureSystemIntervention()
        capture("08_intervention_meditation_light")
        composeRule.onNodeWithText("Start timer", substring = true).performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("meditation-timer-screen") }
        capture("09_meditation_timer_light")
        composeRule.onNodeWithText("End early").performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("home-list") }
        Thread.sleep(6_000)

        openTab("tab-settings", "settings-list")
        composeRule.onNodeWithTag("settings-list")
            .performScrollToNode(hasTestTag("theme-DARK"))
        composeRule.onNodeWithTag("theme-DARK")
            .performSemanticsAction(SemanticsActions.OnClick)
        composeRule.waitForIdle()
        capture("10_settings_dark")

        seedAllSharedContentSelection()
        openTab("tab-library", "library-list")
        composeRule.onNodeWithTag("library-list")
            .performScrollToNode(hasText("A Naturalist Notices Everything"))
        capture("11_library_mixed_dark")

        seedLinkOnlySelection()
        launchFixtureSystemIntervention()
        capture("12_intervention_link_only_dark")

        composeRule.onNodeWithText("Open link", substring = true).performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("external-handoff-screen") }
        assertLinkOnlyHandoffCopyIsDisplayed()
        capture("13_external_handoff_dark")

        seedPublicDomainExpansionSelection()
        launchFixtureSystemIntervention()
        composeRule.onNodeWithText("Read this", substring = true).performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("reader-screen") }
        assertPublicDomainExpansionReaderCopyIsDisplayed()
        capture("14_reader_renderable_v2_dark")

        composeRule.onNodeWithTag("reader-list")
            .performScrollToNode(hasText("I'm done reading"))
        composeRule.onNodeWithText("I'm done reading").performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("feedback-screen") }
        capture("15_feedback_dark")

        composeRule.onNodeWithText("Skip").performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("progress-list") }
        Thread.sleep(6_000)
        capture("16_progress_dark")

        seedMeditationSelection()
        launchFixtureSystemIntervention()
        capture("17_intervention_meditation_dark")
        composeRule.onNodeWithText("Start timer", substring = true).performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("meditation-timer-screen") }
        capture("18_meditation_timer_dark")

        captureLegacyContentDisplayScreens()
    }

    private fun capture(name: String) {
        captureTo(screenshotDir, name)
    }

    private fun captureLegacy(name: String) {
        captureTo(legacyScreenshotDir, name)
    }

    private fun captureTo(directory: File, name: String) {
        composeRule.waitForIdle()
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        Thread.sleep(350)
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        val file = File(directory, "$name.png")
        assertTrue("Could not capture $name into ${file.absolutePath}", device.takeScreenshot(file))
    }

    private fun captureLegacyContentDisplayScreens() {
        scenario?.close()
        scenario = null
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        resetPersistentState()

        launchOnboardedApp()
        seedAttentionClassicsSelection()
        captureLegacy("01_home_light")

        openTab("tab-library", "library-list")
        composeRule.onNodeWithTag("library-list")
            .performScrollToNode(hasText("Start With What Is Yours"))
        captureLegacy("02_library_attention_light")

        launchFixtureSystemIntervention()
        captureLegacy("03_intervention_light")

        composeRule.onNodeWithText("Read this", substring = true).performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("reader-screen") }
        assertAttentionClassicsReaderCopyIsDisplayed()
        captureLegacy("04_reader_attention_light")

        composeRule.onNodeWithTag("reader-list")
            .performScrollToNode(hasText("I'm done reading"))
        composeRule.onNodeWithText("I'm done reading").performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("feedback-screen") }
        captureLegacy("05_feedback_light")

        composeRule.onNodeWithText("Skip").performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("progress-list") }
        Thread.sleep(6_000)
        captureLegacy("06_progress_light")

        seedMeditationSelection()
        launchFixtureSystemIntervention()
        captureLegacy("07_intervention_meditation_light")
        composeRule.onNodeWithText("Start timer", substring = true).performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("meditation-timer-screen") }
        captureLegacy("08_meditation_timer_light")
        composeRule.onNodeWithText("End early").performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("home-list") }
        Thread.sleep(6_000)

        openTab("tab-settings", "settings-list")
        composeRule.onNodeWithTag("settings-list")
            .performScrollToNode(hasTestTag("theme-DARK"))
        composeRule.onNodeWithTag("theme-DARK")
            .performSemanticsAction(SemanticsActions.OnClick)
        composeRule.waitForIdle()
        captureLegacy("09_settings_dark")

        openTab("tab-home", "home-list")
        captureLegacy("10_home_dark")

        seedAttentionClassicsSelection()
        launchFixtureSystemIntervention()
        captureLegacy("11_intervention_dark")

        composeRule.onNodeWithText("Read this", substring = true).performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("reader-screen") }
        assertAttentionClassicsReaderCopyIsDisplayed()
        captureLegacy("12_reader_attention_dark")

        seedMeditationSelection()
        launchFixtureSystemIntervention()
        captureLegacy("13_intervention_meditation_dark")
        composeRule.onNodeWithText("Start timer", substring = true).performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("meditation-timer-screen") }
        captureLegacy("14_meditation_timer_dark")
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
                preferredTopics = setOf(TopicTag.PHILOSOPHY, TopicTag.HISTORY, TopicTag.ESSAYS),
                preferredDurationBucket = DurationBucket.FOCUS,
                selectedPackIds = setOf("attention-classics-v1"),
            ),
        )
    }

    private fun seedPublicDomainExpansionSelection() = runBlocking {
        val repository = (InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as QualityAlternativeApplication)
            .appContainer
            .settingsRepository
        repository.saveOnboardingSelection(
            OnboardingSelection(
                selectedAppPackages = setOf(FixtureTargetRegistry.fixtureDistractors.first().packageName),
                preferredTopics = setOf(TopicTag.SCIENCE, TopicTag.ESSAYS, TopicTag.PHILOSOPHY),
                preferredDurationBucket = DurationBucket.FOCUS,
                selectedPackIds = setOf("public-domain-expansion-v2"),
            ),
        )
    }

    private fun seedLinkOnlySelection() = runBlocking {
        val repository = (InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as QualityAlternativeApplication)
            .appContainer
            .settingsRepository
        repository.saveOnboardingSelection(
            OnboardingSelection(
                selectedAppPackages = setOf(FixtureTargetRegistry.fixtureDistractors.first().packageName),
                preferredTopics = setOf(TopicTag.PHILOSOPHY, TopicTag.SCIENCE, TopicTag.PSYCHOLOGY),
                preferredDurationBucket = DurationBucket.FOCUS,
                selectedPackIds = setOf("link-only-modern-v1"),
            ),
        )
    }

    private fun seedAllSharedContentSelection() = runBlocking {
        val repository = (InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as QualityAlternativeApplication)
            .appContainer
            .settingsRepository
        repository.saveOnboardingSelection(
            OnboardingSelection(
                selectedAppPackages = setOf(FixtureTargetRegistry.fixtureDistractors.first().packageName),
                preferredTopics = setOf(TopicTag.PHILOSOPHY, TopicTag.HISTORY, TopicTag.ESSAYS, TopicTag.SCIENCE),
                preferredDurationBucket = DurationBucket.FOCUS,
                selectedPackIds = setOf(
                    "attention-classics-v1",
                    "public-domain-expansion-v2",
                    "link-only-modern-v1",
                ),
            ),
        )
    }

    private fun seedMeditationSelection() = runBlocking {
        val repository = (InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as QualityAlternativeApplication)
            .appContainer
            .settingsRepository
        repository.saveOnboardingSelection(
            OnboardingSelection(
                selectedAppPackages = setOf(FixtureTargetRegistry.fixtureDistractors.first().packageName),
                preferredTopics = setOf(TopicTag.PSYCHOLOGY, TopicTag.PHILOSOPHY, TopicTag.ESSAYS),
                preferredDurationBucket = DurationBucket.QUICK,
                selectedPackIds = setOf("meditation-only-test-pack"),
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

    private fun assertPublicDomainExpansionReaderCopyIsDisplayed() {
        val titles = listOf(
            "A Naturalist Notices Everything",
            "A Doorway Into Learning",
            "Choose Your Own Plan",
            "Look at the Stars",
            "A Place of Business",
            "The Mind's Own Snare",
            "Rest Satisfied With What We Have",
            "Anger Divides What Life Joins",
            "Earnestness as an Island",
            "The Examined Life",
        )
        val authors = listOf(
            "Charles Darwin",
            "Booker T. Washington",
            "John Stuart Mill",
            "Ralph Waldo Emerson",
            "Henry David Thoreau",
            "Michel de Montaigne",
            "Seneca",
            "The Dhammapada",
            "Plato",
        )

        assertTrue("Expected a public-domain v2 title in the reader", titles.any(::hasNode))
        assertTrue("Expected an author-facing source label in the reader", authors.any(::hasNodeContaining))
        assertTrue("Reader should not show provenance-heavy Project Gutenberg label", !hasNodeContaining("Project Gutenberg"))
    }

    private fun assertLinkOnlyHandoffCopyIsDisplayed() {
        val sourceHints = listOf("longnow.org", "psyche.co", "aeon.co", "quantamagazine.org", "sapiens.org", "plato.stanford.edu", "iep.utm.edu", "nautil.us")

        assertTrue("Expected external reading label", hasNodeContaining("External reading"))
        assertTrue("Expected external handoff copy", hasNode("Opens in your browser"))
        assertTrue("Expected a canonical external URL", sourceHints.any(::hasNodeContaining))
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
