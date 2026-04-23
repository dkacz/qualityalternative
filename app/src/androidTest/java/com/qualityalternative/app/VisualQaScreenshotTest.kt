package com.qualityalternative.app

import android.content.Intent
import android.net.Uri
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
import com.qualityalternative.app.domain.model.AppThemeMode
import com.qualityalternative.app.domain.model.DurationBucket
import com.qualityalternative.app.domain.model.OnboardingSelection
import com.qualityalternative.app.domain.model.TopicTag
import com.qualityalternative.app.domain.model.UserDocumentDraft
import com.qualityalternative.app.domain.service.AddUserDocumentResult
import com.qualityalternative.app.interception.FixtureTargetRegistry
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlinx.coroutines.flow.first
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
    private lateinit var sprint10ScreenshotDir: File

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
        sprint10ScreenshotDir = File(targetContext.filesDir, "visual-qa/sprint10-reader-progress-meditation")
        sprint10ScreenshotDir.deleteRecursively()
        sprint10ScreenshotDir.mkdirs()
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

        seedPhilosophyReplacementSelection()
        launchFixtureSystemIntervention()
        composeRule.onNodeWithText("Read this", substring = true).performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("reader-screen") }
        assertPhilosophyReplacementReaderCopyIsDisplayed()
        capture("05b_reader_philosophy_replacement_light")

        seedScienceReplacementSelection()
        launchFixtureSystemIntervention()
        composeRule.onNodeWithText("Read this", substring = true).performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("reader-screen") }
        assertScienceReplacementReaderCopyIsDisplayed()
        capture("05c_reader_science_replacement_light")

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

        seedPhilosophyReplacementSelection()
        launchFixtureSystemIntervention()
        composeRule.onNodeWithText("Read this", substring = true).performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("reader-screen") }
        assertPhilosophyReplacementReaderCopyIsDisplayed()
        capture("14b_reader_philosophy_replacement_dark")

        seedScienceReplacementSelection()
        launchFixtureSystemIntervention()
        composeRule.onNodeWithText("Read this", substring = true).performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("reader-screen") }
        assertScienceReplacementReaderCopyIsDisplayed()
        capture("14c_reader_science_replacement_dark")

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

    @Test
    fun captureSprint10ReaderProgressStreakAndMeditationScreens() {
        launchOnboardedApp()
        seedUserEpubSelection()

        openTab("tab-library", "library-list")
        composeRule.onNodeWithText("Files").performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) { hasNode("The Long Quiet EPUB") }
        captureSprint10("01_library_epub_file_light")

        launchFixtureSystemIntervention()
        composeRule.onNodeWithText("The Long Quiet EPUB").assertIsDisplayed()
        captureSprint10("02_intervention_epub_light")

        composeRule.onNodeWithText("Read this", substring = true).performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("reader-screen") }
        composeRule.onNodeWithText("The Long Quiet EPUB").assertIsDisplayed()
        composeRule.onNodeWithText("Structured EPUB Notes").assertIsDisplayed()
        composeRule.onNodeWithText("First EPUB bullet with bold text", substring = true).assertIsDisplayed()
        assertTrue("Raw EPUB bold markers should not be visible", !hasNodeContaining("**bold**"))
        captureSprint10("03_reader_epub_start_light")

        composeRule.onNodeWithTag("reader-list")
            .performScrollToNode(hasText("Chapter Two"))
        composeRule.onNodeWithText("Chapter Two").assertIsDisplayed()
        captureSprint10("04_reader_epub_mid_progress_light")

        composeRule.onNodeWithTag("reader-list")
            .performScrollToNode(hasText("I'm done reading"))
        composeRule.onNodeWithText("I'm done reading").assertIsDisplayed()
        captureSprint10("05_reader_epub_done_light")

        composeRule.onNodeWithText("I'm done reading").performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("feedback-screen") }
        captureSprint10("06_feedback_epub_light")

        composeRule.onNodeWithText("Skip").performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("progress-list") }
        composeRule.onNodeWithText("Current reading streak").assertIsDisplayed()
        composeRule.onNodeWithText("Completed reads").assertIsDisplayed()
        composeRule.waitUntil(timeoutMillis = 10_000) {
            !hasNodeContaining("Feedback skipped for this session.")
        }
        captureSprint10("07_progress_streak_light")

        seedUserMarkdownSelection()
        launchFixtureSystemIntervention()
        composeRule.onNodeWithText("Imported Markdown Notes").assertIsDisplayed()
        captureSprint10("07b_intervention_markdown_light")
        composeRule.onNodeWithText("Read this", substring = true).performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("reader-screen") }
        composeRule.onNodeWithText("Imported Markdown Heading").assertIsDisplayed()
        composeRule.onNodeWithText("First item with bold text", substring = true).assertIsDisplayed()
        assertTrue("Raw bold markers should not be visible", !hasNodeContaining("**bold**"))
        assertTrue("Raw heading marker should not be visible", !hasNodeContaining("# Imported Markdown Heading"))
        captureSprint10("07c_reader_markdown_formatting_light")
        scenario?.close()
        scenario = null
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        launchOnboardedApp()

        openTab("tab-settings", "settings-list")
        composeRule.onNodeWithTag("settings-list")
            .performScrollToNode(hasTestTag("meditation-duration-5"))
        composeRule.onNodeWithTag("meditation-duration-5")
            .performSemanticsAction(SemanticsActions.OnClick)
        composeRule.waitForIdle()
        captureSprint10("08_settings_meditation_5m_light")

        seedMeditationSelection()
        launchFixtureSystemIntervention()
        captureSprint10("09_intervention_meditation_5m_light")
        composeRule.onNodeWithText("Start timer", substring = true).performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("meditation-timer-screen") }
        composeRule.onNodeWithText("No feed. Just 5 minutes back.", substring = true).assertIsDisplayed()
        captureSprint10("10_meditation_timer_5m_light")

        composeRule.onNodeWithText("End early").performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("home-list") }

        openTab("tab-settings", "settings-list")
        composeRule.onNodeWithTag("settings-list")
            .performScrollToNode(hasTestTag("theme-DARK"))
        composeRule.onNodeWithTag("theme-DARK")
            .performSemanticsAction(SemanticsActions.OnClick)
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("settings-list")
            .performScrollToNode(hasTestTag("meditation-duration-5"))
        captureSprint10("11_settings_meditation_5m_dark")

        seedUserEpubSelection(
            title = "The Night Quiet EPUB",
            fileName = "night-quiet.epub",
            nowMillis = 2_000L,
        )
        launchFixtureSystemIntervention()
        composeRule.onNodeWithText("The Night Quiet EPUB").assertIsDisplayed()
        captureSprint10("12_intervention_epub_dark")

        composeRule.onNodeWithText("Read this", substring = true).performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("reader-screen") }
        composeRule.onNodeWithText("The Night Quiet EPUB").assertIsDisplayed()
        composeRule.onNodeWithText("Structured EPUB Notes").assertIsDisplayed()
        composeRule.onNodeWithText("First EPUB bullet with bold text", substring = true).assertIsDisplayed()
        assertTrue("Raw dark-mode EPUB bold markers should not be visible", !hasNodeContaining("**bold**"))
        captureSprint10("13_reader_epub_start_dark")
        composeRule.onNodeWithTag("reader-list")
            .performScrollToNode(hasText("I'm done reading"))
        composeRule.onNodeWithText("I'm done reading").assertIsDisplayed()
        captureSprint10("13b_reader_epub_done_dark")

        seedUserMarkdownSelection(
            title = "Night Markdown Notes",
            fileName = "night-notes.md",
            nowMillis = 2_500L,
        )
        launchFixtureSystemIntervention()
        composeRule.onNodeWithText("Night Markdown Notes").assertIsDisplayed()
        captureSprint10("13c_intervention_markdown_dark")
        composeRule.onNodeWithText("Read this", substring = true).performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("reader-screen") }
        composeRule.onNodeWithText("Imported Markdown Heading").assertIsDisplayed()
        composeRule.onNodeWithText("First item with bold text", substring = true).assertIsDisplayed()
        assertTrue("Raw dark-mode Markdown bold markers should not be visible", !hasNodeContaining("**bold**"))
        captureSprint10("13d_reader_markdown_formatting_dark")

        resetForDarkMeditationFixture()
        launchFixtureSystemIntervention()
        captureSprint10("14_intervention_meditation_5m_dark")
        composeRule.onNodeWithText("Start timer", substring = true).performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("meditation-timer-screen") }
        composeRule.onNodeWithText("No feed. Just 5 minutes back.", substring = true).assertIsDisplayed()
        captureSprint10("15_meditation_timer_5m_dark")
    }

    private fun capture(name: String) {
        captureTo(screenshotDir, name)
    }

    private fun captureLegacy(name: String) {
        captureTo(legacyScreenshotDir, name)
    }

    private fun captureSprint10(name: String) {
        captureTo(sprint10ScreenshotDir, name)
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

    private fun seedPhilosophyReplacementSelection() = runBlocking {
        val repository = (InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as QualityAlternativeApplication)
            .appContainer
            .settingsRepository
        repository.saveOnboardingSelection(
            OnboardingSelection(
                selectedAppPackages = setOf(FixtureTargetRegistry.fixtureDistractors.first().packageName),
                preferredTopics = setOf(TopicTag.PHILOSOPHY, TopicTag.ESSAYS),
                preferredDurationBucket = DurationBucket.QUICK,
                selectedPackIds = setOf("philosophy"),
            ),
        )
    }

    private fun seedScienceReplacementSelection() = runBlocking {
        val repository = (InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as QualityAlternativeApplication)
            .appContainer
            .settingsRepository
        repository.saveOnboardingSelection(
            OnboardingSelection(
                selectedAppPackages = setOf(FixtureTargetRegistry.fixtureDistractors.first().packageName),
                preferredTopics = setOf(TopicTag.SCIENCE, TopicTag.PSYCHOLOGY),
                preferredDurationBucket = DurationBucket.QUICK,
                selectedPackIds = setOf("science"),
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

    private fun seedUserEpubSelection(
        title: String = "The Long Quiet EPUB",
        fileName: String = "long-quiet.epub",
        nowMillis: Long = 1_000L,
    ) = runBlocking {
        val targetContext = InstrumentationRegistry.getInstrumentation().targetContext
        val app = targetContext.applicationContext as QualityAlternativeApplication
        val fixture = File(targetContext.filesDir, "visual-qa-fixtures/$fileName")
        fixture.parentFile?.mkdirs()
        fixture.writeBytes(sprint10EpubBytes())

        app.appContainer.userDocumentRepository.observeReady().first { it }
        val result = app.appContainer.userDocumentRepository.addDocument(
            draft = UserDocumentDraft(
                uri = Uri.fromFile(fixture).toString(),
                displayName = fileName,
                mimeType = "application/epub+zip",
                title = title,
                durationMinutes = 20,
                topicTags = setOf(TopicTag.HISTORY, TopicTag.ESSAYS),
            ),
            nowMillis = nowMillis,
        )
        assertTrue("Expected EPUB fixture to be saved", result is AddUserDocumentResult.Added)
        app.appContainer.settingsRepository.saveOnboardingSelection(
            OnboardingSelection(
                selectedAppPackages = setOf(FixtureTargetRegistry.fixtureDistractors.first().packageName),
                preferredTopics = setOf(TopicTag.HISTORY, TopicTag.ESSAYS),
                preferredDurationBucket = DurationBucket.DEEP,
                selectedPackIds = emptySet(),
            ),
        )
    }

    private fun seedUserMarkdownSelection(
        title: String = "Imported Markdown Notes",
        fileName: String = "imported-notes.md",
        nowMillis: Long = 1_500L,
    ) = runBlocking {
        val targetContext = InstrumentationRegistry.getInstrumentation().targetContext
        val app = targetContext.applicationContext as QualityAlternativeApplication
        val fixture = File(targetContext.filesDir, "visual-qa-fixtures/$fileName")
        fixture.parentFile?.mkdirs()
        fixture.writeText(
            """
            # Imported Markdown Heading

            This paragraph has **bold** text, _italic_ emphasis, and `inline code` in one calm reader block.

            - First item with **bold** text
            - Second item with _italic_ text

            > A quoted line should look quieter than the body text.
            """.trimIndent(),
        )

        app.appContainer.userDocumentRepository.observeReady().first { it }
        val result = app.appContainer.userDocumentRepository.addDocument(
            draft = UserDocumentDraft(
                uri = Uri.fromFile(fixture).toString(),
                displayName = fileName,
                mimeType = "text/markdown",
                title = title,
                durationMinutes = 8,
                topicTags = setOf(TopicTag.ESSAYS, TopicTag.PSYCHOLOGY),
            ),
            nowMillis = nowMillis,
        )
        assertTrue("Expected Markdown fixture to be saved", result is AddUserDocumentResult.Added)
        app.appContainer.settingsRepository.saveOnboardingSelection(
            OnboardingSelection(
                selectedAppPackages = setOf(FixtureTargetRegistry.fixtureDistractors.first().packageName),
                preferredTopics = setOf(TopicTag.ESSAYS, TopicTag.PSYCHOLOGY),
                preferredDurationBucket = DurationBucket.FOCUS,
                selectedPackIds = emptySet(),
            ),
        )
    }

    private fun resetForDarkMeditationFixture() {
        scenario?.close()
        scenario = null
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        resetPersistentState()
        launchOnboardedApp()
        saveDarkThemeAndFiveMinuteMeditation()
        seedMeditationSelection()
    }

    private fun saveDarkThemeAndFiveMinuteMeditation() = runBlocking {
        val repository = (InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as QualityAlternativeApplication)
            .appContainer
            .settingsRepository
        repository.saveThemeMode(AppThemeMode.DARK)
        repository.saveMeditationDurationMinutes(5)
    }

    private fun sprint10EpubBytes(): ByteArray {
        return epubBytes(
            "META-INF/container.xml" to """
                <?xml version="1.0"?>
                <container>
                  <rootfiles>
                    <rootfile full-path="OPS/package.opf" media-type="application/oebps-package+xml"/>
                  </rootfiles>
                </container>
            """.trimIndent(),
            "OPS/package.opf" to """
                <package>
                  <manifest>
                    <item id="chapter-one" href="chapter1.xhtml" media-type="application/xhtml+xml"/>
                    <item id="chapter-two" href="chapter2.xhtml" media-type="application/xhtml+xml"/>
                    <item id="chapter-three" href="chapter3.xhtml" media-type="application/xhtml+xml"/>
                  </manifest>
                  <spine>
                    <itemref idref="chapter-one"/>
                    <itemref idref="chapter-two"/>
                    <itemref idref="chapter-three"/>
                  </spine>
                </package>
            """.trimIndent(),
            "OPS/chapter1.xhtml" to """
                <html><body>
                  <h1>Chapter One</h1>
                  <h2>Structured EPUB Notes</h2>
                  <p>This EPUB keeps <strong>bold</strong>, <em>italic</em>, and <code>inline code</code> details in the calm reader.</p>
                  <ul>
                    <li>First EPUB bullet with <strong>bold</strong> text</li>
                    <li>Second EPUB bullet with <em>italic</em> text</li>
                  </ul>
                  <blockquote><p>A quoted EPUB line should feel quieter than the body text.</p></blockquote>
                  <p>This private EPUB fixture is deliberately longer than a card. It gives the reader enough structure for visual progress to move as the text scrolls.</p>
                  <p>The point of the test is not literary quality. The point is proving that a user's own long document can become the primary replacement without leaving the finite intervention loop.</p>
                  <p>A calm reader should hold a line, keep the title legible, and avoid looking like a clipped browser view.</p>
                  <p>At the top of the document, the progress indicator should feel honest: started, but not nearly finished.</p>
                  <p>The fixture repeats a few ordinary sentences because emulator visual QA needs real scroll distance, not a perfect miniature essay.</p>
                  <p>Each paragraph gives typography, spacing, and color a chance to fail loudly if a later UI pass breaks them.</p>
                  <p>The reader should remain calm even when the imported file is much longer than the initial alternative cards.</p>
                </body></html>
            """.trimIndent(),
            "OPS/chapter2.xhtml" to """
                <html><body>
                  <h1>Chapter Two</h1>
                  <p>The second chapter keeps the emulator moving through enough paragraphs to expose spacing, typography, and progress-line behavior.</p>
                  <p>Longer personal files are where progress matters most: the user needs to know that five minutes of reading still counted.</p>
                  <p>If this screen regresses, the screenshot should make it visible before a release build goes to testers.</p>
                  <p>This is the midpoint marker. It should appear after a deliberate scroll, with the top bar showing a meaningfully advanced percentage.</p>
                  <p>The replacement loop should still feel finite here: one document, one reader, one completion path.</p>
                  <p>No feed, no browsing shelf, and no accidental discovery surface should appear between the user and the next paragraph.</p>
                  <p>The imported EPUB is private to the device, but the visual standard should match the shared renderable readings.</p>
                </body></html>
            """.trimIndent(),
            "OPS/chapter3.xhtml" to """
                <html><body>
                  <h1>Chapter Three</h1>
                  <p>The final section is a marker for the visual QA harness. It confirms that spine order, native rendering, and scroll-based progress survive the end-to-end flow.</p>
                  <p>After completion, the same session should feed the progress screen and count toward the current reading streak.</p>
                  <p>That closes the replacement loop: impulse, alternative, reading, completion, and visible momentum.</p>
                  <p>The bottom of a long document is also where the call to finish should be easy to find and hard to confuse with opening a feed.</p>
                  <p>When the user taps done, the app should move to feedback and then progress without losing the completed read.</p>
                  <p>This final paragraph is intentionally plain. If it is readable, the layout is doing its job.</p>
                </body></html>
            """.trimIndent(),
        )
    }

    private fun epubBytes(vararg entries: Pair<String, String>): ByteArray {
        val output = ByteArrayOutputStream()
        ZipOutputStream(output).use { zip ->
            entries.forEach { (name, body) ->
                zip.putNextEntry(ZipEntry(name))
                zip.write(body.toByteArray(Charsets.UTF_8))
                zip.closeEntry()
            }
        }
        return output.toByteArray()
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

    private fun assertPhilosophyReplacementReaderCopyIsDisplayed() {
        val titles = listOf(
            "Care for the Soul First",
            "Leave the Crowd",
            "Let the Pleasure Wait",
            "Neither Ask Nor Consent",
        )
        val authors = listOf("Plato", "Seneca", "Epictetus", "Cicero")

        assertTrue("Expected a real philosophy starter title in the reader", titles.any(::hasNode))
        assertTrue("Expected a public-domain philosophy source label in the reader", authors.any(::hasNodeContaining))
        assertTrue("Reader should not show the old editorial placeholder label", !hasNodeContaining("Quality Alternative Editorial"))
        assertTrue("Reader should not show provenance-heavy Project Gutenberg label", !hasNodeContaining("Project Gutenberg"))
    }

    private fun assertScienceReplacementReaderCopyIsDisplayed() {
        val titles = listOf(
            "A Candle Opens Natural Philosophy",
            "Water-Dust Becomes a Cloud",
            "Attention Comes in Beats",
        )
        val authors = listOf("Michael Faraday", "John Tyndall", "William James")

        assertTrue("Expected a real science starter title in the reader", titles.any(::hasNode))
        assertTrue("Expected a public-domain science source label in the reader", authors.any(::hasNodeContaining))
        assertTrue("Reader should not show the old editorial placeholder label", !hasNodeContaining("Quality Alternative Editorial"))
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
