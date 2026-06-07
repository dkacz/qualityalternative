package com.qualityalternative.app

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.click
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeRight
import androidx.compose.ui.test.swipeUp
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import com.qualityalternative.app.domain.model.AppThemeMode
import com.qualityalternative.app.domain.model.ContentFormat
import com.qualityalternative.app.domain.model.ContentItem
import com.qualityalternative.app.domain.model.ContentPriority
import com.qualityalternative.app.domain.model.DurationBucket
import com.qualityalternative.app.domain.model.InterventionMode
import com.qualityalternative.app.domain.model.MEDITATION_TIMER_CONTENT_ID
import com.qualityalternative.app.domain.model.OnboardingSelection
import com.qualityalternative.app.domain.model.ReadingProgress
import com.qualityalternative.app.domain.model.TopicTag
import com.qualityalternative.app.domain.model.UserDocumentDraft
import com.qualityalternative.app.domain.model.UserLinkDraft
import com.qualityalternative.app.domain.model.WebsiteRuleType
import com.qualityalternative.app.domain.service.AddUserDocumentResult
import com.qualityalternative.app.domain.service.AddUserLinkResult
import com.qualityalternative.app.interception.FixtureTargetRegistry
import com.qualityalternative.app.interception.VerifiedBrowserHostAdapter
import com.qualityalternative.app.data.ReadingTimeEstimateSource
import com.qualityalternative.app.ui.DocumentImportCandidate
import java.io.ByteArrayInputStream
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
    private val sprint13ScreenshotDirName = "sprint13-completed-unlock-${System.currentTimeMillis()}"
    private val sprint15ScreenshotDirName = "sprint15-slice15-0-auto-time-${System.currentTimeMillis()}"
    private val sprint15Slice151ScreenshotDirName = "sprint15-slice15-1-epub-toc-${System.currentTimeMillis()}"
    private val sprint15Slice152ScreenshotDirName = "sprint15-slice15-2-kindle-toc-${System.currentTimeMillis()}"
    private val sprint20ScreenshotDirName = "sprint20-epub-loading-performance-${System.currentTimeMillis()}"
    private val sprint22ScreenshotDirName = "sprint22-reading-time-remaining-${System.currentTimeMillis()}"
    private val sprint25ScreenshotDirName = "sprint25-markdown-media-tables-${System.currentTimeMillis()}"
    private val sprint26ScreenshotDirName = "sprint26-custom-targets-${System.currentTimeMillis()}"
    private lateinit var screenshotDir: File
    private lateinit var legacyScreenshotDir: File
    private lateinit var sprint10ScreenshotDir: File
    private lateinit var sprint9ScreenshotDir: File
    private lateinit var sprint12ScreenshotDir: File
    private lateinit var sprint12FinalScreenshotDir: File
    private lateinit var sprint13ScreenshotDir: File
    private lateinit var sprint15ScreenshotDir: File
    private lateinit var sprint15Slice151ScreenshotDir: File
    private lateinit var sprint15Slice152ScreenshotDir: File
    private lateinit var sprint20ScreenshotDir: File
    private lateinit var sprint22ScreenshotDir: File
    private lateinit var sprint25ScreenshotDir: File
    private lateinit var sprint26ScreenshotDir: File

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
        sprint9ScreenshotDir = File(targetContext.filesDir, "visual-qa/sprint9-content-expansion")
        sprint9ScreenshotDir.deleteRecursively()
        sprint9ScreenshotDir.mkdirs()
        sprint12ScreenshotDir = File(targetContext.filesDir, "visual-qa/sprint12-content-management")
        sprint12ScreenshotDir.deleteRecursively()
        sprint12ScreenshotDir.mkdirs()
        val sprint12FinalRoot = targetContext.getExternalFilesDir(null) ?: targetContext.filesDir
        sprint12FinalScreenshotDir = File(sprint12FinalRoot, "visual-qa/sprint12-final-journey")
        sprint12FinalScreenshotDir.deleteRecursively()
        sprint12FinalScreenshotDir.mkdirs()
        sprint13ScreenshotDir = File("/sdcard/Download/qualityalternative/$sprint13ScreenshotDirName")
        sprint13ScreenshotDir.deleteRecursively()
        sprint13ScreenshotDir.mkdirs()
        sprint15ScreenshotDir = File("/sdcard/Download/qualityalternative/$sprint15ScreenshotDirName")
        sprint15ScreenshotDir.deleteRecursively()
        sprint15ScreenshotDir.mkdirs()
        sprint15Slice151ScreenshotDir = File("/sdcard/Download/qualityalternative/$sprint15Slice151ScreenshotDirName")
        sprint15Slice151ScreenshotDir.deleteRecursively()
        sprint15Slice151ScreenshotDir.mkdirs()
        sprint15Slice152ScreenshotDir = File("/sdcard/Download/qualityalternative/$sprint15Slice152ScreenshotDirName")
        sprint15Slice152ScreenshotDir.deleteRecursively()
        sprint15Slice152ScreenshotDir.mkdirs()
        sprint20ScreenshotDir = File("/sdcard/Download/qualityalternative/$sprint20ScreenshotDirName")
        sprint20ScreenshotDir.deleteRecursively()
        sprint20ScreenshotDir.mkdirs()
        sprint22ScreenshotDir = File("/sdcard/Download/qualityalternative/$sprint22ScreenshotDirName")
        sprint22ScreenshotDir.deleteRecursively()
        sprint22ScreenshotDir.mkdirs()
        sprint25ScreenshotDir = File("/sdcard/Download/qualityalternative/$sprint25ScreenshotDirName")
        sprint25ScreenshotDir.deleteRecursively()
        sprint25ScreenshotDir.mkdirs()
        sprint26ScreenshotDir = File("/sdcard/Download/qualityalternative/$sprint26ScreenshotDirName")
        sprint26ScreenshotDir.deleteRecursively()
        sprint26ScreenshotDir.mkdirs()
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
        launchFreshAppThroughTopicVisualQa()
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

        finishReaderFromCurrentPage()
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("feedback-screen") }
        capture("06_feedback_light")

        composeRule.onNodeWithText("Skip").performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("progress-list") }
        Thread.sleep(6_000)
        capture("07_progress_light")

        seedMeditationSelection()
        launchFixtureSystemIntervention()
        capture("08_intervention_meditation_light")
        startMeditationFromIntervention()
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

        resetPersistentState()
        launchOnboardedApp()
        saveDarkTheme()
        composeRule.waitForIdle()

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

        finishReaderFromCurrentPage()
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("feedback-screen") }
        capture("15_feedback_dark")

        composeRule.onNodeWithText("Skip").performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("progress-list") }
        Thread.sleep(6_000)
        capture("16_progress_dark")

        seedMeditationSelection()
        launchFixtureSystemIntervention()
        capture("17_intervention_meditation_dark")
        startMeditationFromIntervention()
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("meditation-timer-screen") }
        capture("18_meditation_timer_dark")

        captureLegacyContentDisplayScreens()
    }

    @Test
    fun captureSprint25MarkdownMediaAndTableScreens() {
        launchFreshAppThroughTopicVisualQa()
        val lightContent = seedMarkdownMediaTableSelection()
        launchFixtureSystemIntervention()
        assertTrue("Expected Markdown media/table title in intervention", hasAnyNode("Markdown Media Table Notes"))
        captureSprint25("01_intervention_markdown_media_table_light")

        scenario?.onActivity { activity -> activity.mainViewModel.openLibraryItem(lightContent) }
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("reader-screen") }
        captureSprint25("02_reader_markdown_media_table_light")
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("reader-markdown-image") }
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("reader-markdown-table") }
        composeRule.onNodeWithText("Calm blue square").assertIsDisplayed()
        composeRule.onNodeWithText("Read").assertIsDisplayed()
        composeRule.onNodeWithText("20 min").assertIsDisplayed()
        assertTrue("Raw Markdown image syntax should not be visible", !hasNodeContaining("![Calm blue square]"))
        assertTrue("Raw Markdown table pipes should not be visible", !hasNodeContaining("| Signal |"))
        assertTrue("Raw Markdown table delimiter should not be visible", !hasNodeContaining("---"))
        captureSprint25("03_reader_markdown_media_table_light_verified")

        scenario?.close()
        scenario = null
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        resetPersistentState()
        launchOnboardedApp()
        saveDarkTheme()
        val darkContent = seedMarkdownMediaTableSelection(title = "Night Markdown Media Table Notes", nowMillis = 2_600L)
        launchFixtureSystemIntervention()
        assertTrue("Expected night Markdown media/table title in intervention", hasAnyNode("Night Markdown Media Table Notes"))
        scenario?.onActivity { activity -> activity.mainViewModel.openLibraryItem(darkContent) }
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("reader-screen") }
        captureSprint25("04_reader_markdown_media_table_dark")
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("reader-markdown-image") }
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("reader-markdown-table") }
        captureSprint25("05_reader_markdown_media_table_dark_verified")
    }

    @Test
    fun captureSprint25WideMarkdownTableHorizontalScrollDoesNotAdvanceReaderPage() {
        launchOnboardedApp()
        val content = seedWideMarkdownTableSelection()

        scenario?.onActivity { activity -> activity.mainViewModel.openLibraryItem(content) }
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("reader-screen") }
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("reader-markdown-table") }
        captureSprint25("06_wide_table_before_horizontal_scroll_light")

        composeRule.onNodeWithTag("reader-markdown-table")
            .performTouchInput { swipeLeft() }
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("reader-screen").assertIsDisplayed()
        composeRule.onNodeWithTag("reader-markdown-table").assertIsDisplayed()
        captureSprint25("07_wide_table_after_horizontal_scroll_still_reader_light")
    }

    @Test
    fun captureSprint25OrdinaryTextNavigationStillWorksAfterTableGestureGuard() {
        launchOnboardedApp()
        val content = seedPagedMarkdownNavigationSelection()

        scenario?.onActivity { activity -> activity.mainViewModel.openLibraryItem(content) }
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("reader-screen") }
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("reader-annotation-rendered-block-0") }
        composeRule.onNodeWithText("Navigation paragraph 01", substring = true).assertIsDisplayed()
        captureSprint25("08_text_navigation_before_tap_light")

        composeRule.onNodeWithTag("reader-annotation-rendered-block-0")
            .performTouchInput { click() }
        composeRule.waitUntil(timeoutMillis = 10_000) { hasNodeContaining("2/") }
        captureSprint25("09_text_tap_advances_page_light")

        composeRule.onNodeWithTag("reader-page-viewport")
            .performTouchInput { swipeRight() }
        composeRule.waitUntil(timeoutMillis = 10_000) { hasNodeContaining("1/") }

        composeRule.onNodeWithTag("reader-annotation-rendered-block-0")
            .performTouchInput { swipeLeft() }
        composeRule.waitUntil(timeoutMillis = 10_000) { hasNodeContaining("2/") }
        captureSprint25("10_text_swipe_advances_page_light")
    }

    @Test
    fun captureSprint26CustomTargetSettingsScreens() {
        launchOnboardedApp()
        openTab("tab-settings", "settings-list")
        composeRule.onNodeWithTag("settings-list")
            .performScrollToNode(hasTestTag("settings-custom-apps-section"))
        composeRule.onNodeWithTag("settings-custom-app-search").assertIsDisplayed()
        captureSprint26("01_custom_app_search_empty_light")

        composeRule.onNodeWithTag("settings-custom-app-search").performTextInput("Quality")
        composeRule.onNodeWithTag("settings-custom-app-search").performImeAction()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("settings-app-com.qualityalternative.app").assertIsDisplayed()
        captureSprint26("02_custom_app_self_excluded_light")

        val eligibleCandidate = runBlocking {
            val app = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as QualityAlternativeApplication
            app.appContainer.settingsRepository.customTargetAppCandidates().firstOrNull { it.isEligible }
        }
        assertTrue("Expected at least one eligible launchable custom app on the visual QA device", eligibleCandidate != null)
        val candidate = requireNotNull(eligibleCandidate)
        composeRule.onNodeWithTag("settings-custom-app-search").performTextClearance()
        composeRule.onNodeWithTag("settings-custom-app-search").performTextInput(candidate.app.packageName)
        composeRule.onNodeWithTag("settings-custom-app-search").performImeAction()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("settings-app-${candidate.app.packageName}").assertIsDisplayed()
        captureSprint26("03_custom_app_eligible_search_light")

        composeRule.onNodeWithTag("settings-app-${candidate.app.packageName}").performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) {
            runBlocking {
                val app = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as QualityAlternativeApplication
                candidate.app.packageName in app.appContainer.settingsRepository.observeAppSettings().first().selectedAppPackages
            }
        }
        composeRule.onNodeWithTag("settings-app-${candidate.app.packageName}").assertIsSelected()
        captureSprint26("04_custom_app_selected_light")

        scenario?.close()
        scenario = null
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        launchOnboardedApp()
        openTab("tab-settings", "settings-list")
        composeRule.onNodeWithTag("settings-list")
            .performScrollToNode(hasTestTag("settings-custom-apps-section"))
        composeRule.onNodeWithTag("settings-custom-app-search").performTextInput(candidate.app.packageName)
        composeRule.onNodeWithTag("settings-custom-app-search").performImeAction()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("settings-app-${candidate.app.packageName}").assertIsSelected()
        captureSprint26("05_custom_app_persisted_after_restart_light")

        composeRule.onNodeWithTag("settings-app-${candidate.app.packageName}").performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) {
            runBlocking {
                val app = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as QualityAlternativeApplication
                candidate.app.packageName !in app.appContainer.settingsRepository.observeAppSettings().first().selectedAppPackages
            }
        }
        composeRule.onNodeWithTag("settings-app-${candidate.app.packageName}").assertIsNotSelected()
        captureSprint26("06_custom_app_removed_light")

        scenario?.onActivity { activity ->
            activity.mainViewModel.requestSystemInterception(
                targetAppPackage = candidate.app.packageName,
                nowMillis = System.currentTimeMillis(),
            )
        }
        composeRule.waitForIdle()
        assertTrue("Unselected custom app should not trigger intervention", !hasTag("intervention-screen"))
        captureSprint26("07_custom_app_unselected_no_intervention_light")

        composeRule.onNodeWithTag("settings-app-${candidate.app.packageName}").performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) {
            runBlocking {
                val app = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as QualityAlternativeApplication
                candidate.app.packageName in app.appContainer.settingsRepository.observeAppSettings().first().selectedAppPackages
            }
        }
        composeRule.onNodeWithTag("settings-app-${candidate.app.packageName}").assertIsSelected()

        scenario?.close()
        scenario = null
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        val targetContext = InstrumentationRegistry.getInstrumentation().targetContext
        launchApp(
            MainActivity.createSystemInterceptionIntent(
                context = targetContext,
                targetAppPackage = candidate.app.packageName,
            ),
        )
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("intervention-screen") }
        composeRule.onNodeWithTag("intervention-primary-explanation").assertIsDisplayed()
        assertTrue("Soft mode should not show form-intervention wait", !hasTag("form-intervention-unlock-wait"))
        composeRule.onNodeWithText("Open ${candidate.app.displayName}").assertIsDisplayed().assertIsEnabled()
        captureSprint26("08_custom_app_soft_intervention_light")

        scenario?.onActivity { activity ->
            activity.mainViewModel.selectInterventionMode(InterventionMode.FIRM)
            activity.mainViewModel.requestSystemInterception(
                targetAppPackage = candidate.app.packageName,
                nowMillis = System.currentTimeMillis(),
            )
        }
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("form-intervention-unlock-wait") }
        composeRule.onNodeWithText("Open in", substring = true).assertIsDisplayed().assertIsNotEnabled()
        captureSprint26("09_custom_app_firm_intervention_wait_light")

        scenario?.onActivity { activity ->
            activity.mainViewModel.setBedtimeSettings(enabled = true, startMinutes = 0, endMinutes = 0)
            activity.mainViewModel.requestSystemInterception(
                targetAppPackage = candidate.app.packageName,
                nowMillis = System.currentTimeMillis(),
            )
        }
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("bedtime-emergency-unlock-wait") }
        composeRule.onNodeWithText("Bedtime is protecting sleep", substring = true).assertIsDisplayed()
        composeRule.onNodeWithTag("bedtime-emergency-unlock-action").assertIsDisplayed().assertIsNotEnabled()
        captureSprint26("10_custom_app_bedtime_intervention_light")
    }

    @Test
    fun captureSprint26WebsiteRuleSettingsScreens() {
        launchOnboardedApp()
        openTab("tab-settings", "settings-list")
        composeRule.onNodeWithTag("settings-list")
            .performScrollToNode(hasTestTag("settings-website-rules-section"))
        composeRule.onNodeWithTag("settings-website-rule-input").assertIsDisplayed()
        composeRule.onNodeWithText("No website rules yet.").assertIsDisplayed()
        captureSprint26("11_website_rules_empty_light")

        composeRule.onNodeWithTag("settings-website-rule-input").performTextInput("192.168.1.5")
        composeRule.onNodeWithTag("settings-website-rule-save").performClick()
        composeRule.onNodeWithTag("settings-website-rule-error").assertIsDisplayed()
        captureSprint26("12_website_rules_private_ip_rejected_light")

        composeRule.onNodeWithTag("settings-website-rule-input").performTextClearance()
        composeRule.onNodeWithTag("settings-website-rule-input").performTextInput("8.8.8.8")
        composeRule.onNodeWithTag("settings-website-rule-save").performClick()
        composeRule.onNodeWithTag("settings-website-rule-error").assertIsDisplayed()
        captureSprint26("13_website_rules_public_ip_rejected_light")

        composeRule.onNodeWithTag("settings-website-rule-input").performTextClearance()
        composeRule.onNodeWithTag("settings-website-rule-input").performTextInput("HTTPS://Example.COM:443/deep/read")
        composeRule.onNodeWithTag("settings-website-rule-save").performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) {
            runBlocking {
                val app = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as QualityAlternativeApplication
                app.appContainer.settingsRepository.observeAppSettings().first().websiteRules.any { it.host == "example.com" }
            }
        }
        composeRule.onNodeWithText("example.com").assertIsDisplayed()
        waitForTransientMessageToClear("Website rule saved.")
        captureSprint26("14_website_rules_exact_saved_light")

        composeRule.onNodeWithTag("settings-website-rule-input").performTextInput("*.News.Example")
        composeRule.onNodeWithTag("settings-website-rule-include-apex").assertIsDisplayed()
        UiDevice.getInstance(InstrumentationRegistry.getInstrumentation()).pressBack()
        composeRule.waitForIdle()
        captureSprint26("15_website_rules_typed_wildcard_toggle_visible_light")
        composeRule.onNodeWithTag("settings-website-rule-save").performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) {
            runBlocking {
                val app = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as QualityAlternativeApplication
                app.appContainer.settingsRepository.observeAppSettings().first().websiteRules.any { rule ->
                    rule.host == "news.example" && !rule.includeApex
                }
            }
        }
        composeRule.onNodeWithText("*.news.example").assertIsDisplayed()
        waitForTransientMessageToClear("Website rule saved.")
        captureSprint26("16_website_rules_typed_wildcard_subdomain_only_light")

        val newsRuleId = runBlocking {
            val app = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as QualityAlternativeApplication
            app.appContainer.settingsRepository.observeAppSettings().first().websiteRules.first { it.host == "news.example" }.id
        }
        composeRule.onNodeWithTag("settings-website-rule-edit-$newsRuleId").performClick()
        composeRule.onNodeWithTag("settings-website-rule-include-apex").assertIsDisplayed()
        composeRule.onNodeWithTag("settings-website-rule-include-apex").performClick()
        composeRule.onNodeWithTag("settings-website-rule-save").performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) {
            runBlocking {
                val app = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as QualityAlternativeApplication
                app.appContainer.settingsRepository.observeAppSettings().first().websiteRules.any { rule ->
                    rule.id == newsRuleId && rule.includeApex
                }
            }
        }
        composeRule.onNodeWithText("*.news.example + news.example").assertIsDisplayed()
        waitForTransientMessageToClear("Website rule updated.")
        captureSprint26("17_website_rules_visible_apex_toggle_saved_light")

        val exactRuleId = runBlocking {
            val app = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as QualityAlternativeApplication
            app.appContainer.settingsRepository.observeAppSettings().first().websiteRules.first { it.host == "example.com" }.id
        }
        composeRule.onNodeWithTag("settings-website-rule-toggle-$exactRuleId").performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) {
            runBlocking {
                val app = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as QualityAlternativeApplication
                app.appContainer.settingsRepository.observeAppSettings().first().websiteRules.first { it.id == exactRuleId }.enabled.not()
            }
        }
        waitForTransientMessageToClear("Website rule updated.")
        captureSprint26("18_website_rules_exact_paused_light")

        composeRule.onNodeWithTag("settings-website-rule-edit-$exactRuleId").performClick()
        composeRule.onNodeWithTag("settings-website-rule-input").assertIsDisplayed()
        composeRule.onNodeWithTag("settings-website-rule-input").performTextClearance()
        composeRule.onNodeWithTag("settings-website-rule-input").performTextInput("example.org")
        composeRule.onNodeWithTag("settings-website-rule-cancel").performClick()
        composeRule.onNodeWithText("example.com").assertIsDisplayed()
        captureSprint26("19_website_rules_edit_cancel_keeps_original_light")

        composeRule.onNodeWithTag("settings-website-rule-edit-$exactRuleId").performClick()
        composeRule.onNodeWithTag("settings-website-rule-input").assertIsDisplayed()
        composeRule.onNodeWithTag("settings-website-rule-input").performTextClearance()
        composeRule.onNodeWithTag("settings-website-rule-input").performTextInput("example.org")
        composeRule.onNodeWithTag("settings-website-rule-save").performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) {
            runBlocking {
                val app = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as QualityAlternativeApplication
                app.appContainer.settingsRepository.observeAppSettings().first().websiteRules.any { rule ->
                    rule.id == exactRuleId && rule.host == "example.org"
                }
            }
        }
        composeRule.onNodeWithText("example.org").assertIsDisplayed()
        waitForTransientMessageToClear("Website rule updated.")
        captureSprint26("20_website_rules_edit_saved_light")

        composeRule.onNodeWithTag("settings-website-rule-delete-$exactRuleId").performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) {
            runBlocking {
                val app = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as QualityAlternativeApplication
                app.appContainer.settingsRepository.observeAppSettings().first().websiteRules.none { it.id == exactRuleId }
            }
        }
        waitForTransientMessageToClear("Website rule deleted.")
        captureSprint26("21_website_rules_delete_keeps_wildcard_light")

        composeRule.onNodeWithTag("settings-list")
            .performScrollToNode(hasTestTag("settings-browser-support-matrix"))
        composeRule.onNodeWithTag("settings-browser-support-chrome").assertIsDisplayed()
        composeRule.onNodeWithTag("settings-browser-support-other").assertIsDisplayed()
        waitForTransientMessageToClear("Website rule deleted.")
        captureSprint26("22_website_rules_browser_support_matrix_light")
    }

    @Test
    fun captureSprint26ChromeWebsiteInterventionScreens() {
        launchOnboardedApp()
        scenario?.close()
        scenario = null
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()

        val targetContext = InstrumentationRegistry.getInstrumentation().targetContext
        launchApp(
            MainActivity.createWebsiteInterceptionIntent(
                context = targetContext,
                browserPackage = VerifiedBrowserHostAdapter.CHROME_PACKAGE,
                browserDisplayName = "Chrome",
                websiteRuleType = WebsiteRuleType.EXACT_DOMAIN.name,
                websiteRuleIncludesApex = false,
            ),
        )
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("intervention-screen") }
        assertTrue(
            "Expected Chrome website copy in intervention",
            composeRule.onAllNodesWithText("Chrome website", substring = true).fetchSemanticsNodes().isNotEmpty(),
        )
        composeRule.onNodeWithText("Open Chrome website").assertIsDisplayed().assertIsEnabled()
        assertTrue("Website soft mode should not show form-intervention wait", !hasTag("form-intervention-unlock-wait"))
        captureSprint26("23_website_chrome_verified_host_soft_intervention_light")

        scenario?.onActivity { activity ->
            activity.mainViewModel.selectInterventionMode(InterventionMode.FIRM)
        }
        composeRule.waitForIdle()
        scenario?.close()
        scenario = null
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        launchApp(
            MainActivity.createWebsiteInterceptionIntent(
                context = targetContext,
                browserPackage = VerifiedBrowserHostAdapter.CHROME_PACKAGE,
                browserDisplayName = "Chrome",
                websiteRuleType = WebsiteRuleType.WILDCARD_SUBDOMAINS.name,
                websiteRuleIncludesApex = true,
            ),
        )
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("form-intervention-unlock-wait") }
        assertTrue(
            "Expected Chrome website copy in firm intervention",
            composeRule.onAllNodesWithText("Chrome website", substring = true).fetchSemanticsNodes().isNotEmpty(),
        )
        composeRule.onNodeWithText("Open in", substring = true).assertIsDisplayed().assertIsNotEnabled()
        captureSprint26("24_website_chrome_verified_host_firm_wait_light")

        scenario?.onActivity { activity ->
            activity.mainViewModel.setBedtimeSettings(enabled = true, startMinutes = 0, endMinutes = 0)
        }
        composeRule.waitUntil(timeoutMillis = 10_000) {
            runBlocking {
                val app = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as QualityAlternativeApplication
                app.appContainer.settingsRepository.observeAppSettings().first().bedtimeEnabled
            }
        }
        scenario?.close()
        scenario = null
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        launchApp(
            MainActivity.createWebsiteInterceptionIntent(
                context = targetContext,
                browserPackage = VerifiedBrowserHostAdapter.CHROME_PACKAGE,
                browserDisplayName = "Chrome",
                websiteRuleType = WebsiteRuleType.WILDCARD_SUBDOMAINS.name,
                websiteRuleIncludesApex = true,
            ),
        )
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("bedtime-emergency-unlock-wait") }
        composeRule.onNodeWithText("Bedtime is protecting sleep from Chrome website", substring = true).assertIsDisplayed()
        composeRule.onNodeWithTag("intervention-meditation-alternative").assertIsDisplayed()
        composeRule.onNodeWithTag("bedtime-emergency-unlock-action").assertIsDisplayed().assertIsNotEnabled()
        assertTrue("Bedtime website flow should hide Pause 15 min", !hasNode("Pause 15 min"))
        captureSprint26("25_website_chrome_bedtime_emergency_unlock_light")
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
        assertNodeFullyWithinRoot("intervention-backup-action-0")
        assertNodeFullyWithinRoot("intervention-backup-action-1")
        assertNodeFullyWithinRoot("intervention-bottom-actions")
        captureSprint10("02_intervention_epub_light")

        composeRule.onNodeWithText("Read this", substring = true).performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("reader-screen") }
        composeRule.onNodeWithText("The Long Quiet EPUB").assertIsDisplayed()
        composeRule.onNodeWithText("Structured EPUB Notes").assertIsDisplayed()
        composeRule.onNodeWithText("First EPUB bullet with bold text", substring = true).assertIsDisplayed()
        assertTrue("Raw EPUB bold markers should not be visible", !hasNodeContaining("**bold**"))
        captureSprint10("03_reader_epub_start_light")

        advanceReaderToText("Chapter Two")
        composeRule.onNodeWithText("Chapter Two").assertIsDisplayed()
        captureSprint10("04_reader_epub_mid_progress_light")

        advanceReaderToLastPage()
        composeRule.onNodeWithTag("reader-page-viewport").assertIsDisplayed()
        captureSprint10("05_reader_epub_done_light")

        finishReaderFromCurrentPage()
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
        seedSupportedAppSelection()
        scenario?.close()
        scenario = null
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        launchOnboardedApp()

        openTab("tab-settings", "settings-list")
        composeRule.onNodeWithTag("settings-list")
            .performScrollToNode(hasText("APPS TO INTERRUPT"))
        composeRule.onNodeWithText("APPS TO INTERRUPT").assertIsDisplayed()
        composeRule.onNodeWithText("Instagram").assertIsDisplayed()
        composeRule.onNodeWithText("X").assertIsDisplayed()
        assertSupportedAppRowsShowCheckedAndUncheckedState()
        captureSprint10("08b_settings_app_selection_light")
        composeRule.onNodeWithTag("settings-list")
            .performScrollToNode(hasTestTag("content-priority-MEDITATION"))
        captureSprint10("08a_settings_content_priority_light")
        composeRule.onNodeWithTag("settings-list")
            .performScrollToNode(hasTestTag("meditation-duration-5"))
        composeRule.onNodeWithTag("meditation-duration-5")
            .performSemanticsAction(SemanticsActions.OnClick)
        composeRule.waitForIdle()
        captureSprint10("08_settings_meditation_5m_light")

        seedMeditationSelection()
        launchFixtureSystemIntervention()
        captureSprint10("09_intervention_meditation_5m_light")
        startMeditationFromIntervention()
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("meditation-timer-screen") }
        composeRule.onNodeWithText("No feed. Just 5 min back.", substring = true).assertIsDisplayed()
        assertNodeFullyWithinRoot("meditation-countdown")
        assertNodeFullyWithinRoot("meditation-timer-card")
        assertNodeFullyWithinRoot("meditation-complete")
        captureSprint10("10_meditation_timer_5m_light")

        composeRule.onNodeWithText("End early").performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("home-list") }
        seedSupportedAppSelection()
        scenario?.close()
        scenario = null
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        launchOnboardedApp()

        openTab("tab-settings", "settings-list")
        composeRule.onNodeWithTag("settings-list")
            .performScrollToNode(hasTestTag("theme-DARK"))
        composeRule.onNodeWithTag("theme-DARK")
            .performSemanticsAction(SemanticsActions.OnClick)
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("settings-list")
            .performScrollToNode(hasText("APPS TO INTERRUPT"))
        composeRule.onNodeWithText("APPS TO INTERRUPT").assertIsDisplayed()
        composeRule.onNodeWithText("Instagram").assertIsDisplayed()
        composeRule.onNodeWithText("X").assertIsDisplayed()
        assertSupportedAppRowsShowCheckedAndUncheckedState()
        captureSprint10("11b_settings_app_selection_dark")
        composeRule.onNodeWithTag("settings-list")
            .performScrollToNode(hasTestTag("content-priority-BALANCED"))
        captureSprint10("11a_settings_content_priority_dark")
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
        assertNodeFullyWithinRoot("intervention-backup-action-0")
        assertNodeFullyWithinRoot("intervention-backup-action-1")
        assertNodeFullyWithinRoot("intervention-bottom-actions")
        captureSprint10("12_intervention_epub_dark")

        composeRule.onNodeWithText("Read this", substring = true).performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("reader-screen") }
        composeRule.onNodeWithText("The Night Quiet EPUB").assertIsDisplayed()
        composeRule.onNodeWithText("Structured EPUB Notes").assertIsDisplayed()
        composeRule.onNodeWithText("First EPUB bullet with bold text", substring = true).assertIsDisplayed()
        assertTrue("Raw dark-mode EPUB bold markers should not be visible", !hasNodeContaining("**bold**"))
        captureSprint10("13_reader_epub_start_dark")
        advanceReaderToText("Chapter Two")
        composeRule.onNodeWithText("Chapter Two").assertIsDisplayed()
        captureSprint10("13a_reader_epub_mid_progress_dark")
        advanceReaderToLastPage()
        composeRule.onNodeWithTag("reader-page-viewport").assertIsDisplayed()
        captureSprint10("13b_reader_epub_done_dark")
        finishReaderFromCurrentPage()
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("feedback-screen") }
        captureSprint10("13e_feedback_epub_dark")
        composeRule.onNodeWithText("Skip").performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("progress-list") }
        composeRule.onNodeWithText("Current reading streak").assertIsDisplayed()
        composeRule.onNodeWithText("Completed reads").assertIsDisplayed()
        composeRule.waitUntil(timeoutMillis = 10_000) {
            !hasNodeContaining("Feedback skipped for this session.")
        }
        captureSprint10("13f_progress_streak_dark")

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
        startMeditationFromIntervention()
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("meditation-timer-screen") }
        composeRule.onNodeWithText("No feed. Just 5 min back.", substring = true).assertIsDisplayed()
        assertNodeFullyWithinRoot("meditation-countdown")
        assertNodeFullyWithinRoot("meditation-timer-card")
        assertNodeFullyWithinRoot("meditation-complete")
        captureSprint10("15_meditation_timer_5m_dark")
    }

    @Test
    fun captureSprint9ContentExpansionScreens() {
        launchOnboardedApp()
        seedSprint9AllSelection()

        openTab("tab-library", "library-list")
        composeRule.onNodeWithTag("library-list")
            .performScrollToNode(hasText("How We Think"))
        composeRule.onNodeWithText("How We Think").assertIsDisplayed()
        captureSprint9("01_library_sprint9_light")

        seedSprint9RenderableSelection()
        launchFixtureSystemIntervention()
        composeRule.onNodeWithText("Five Minutes of Nonsense").assertIsDisplayed()
        captureSprint9("02_intervention_sprint9_renderable_light")

        openAlternativeFromIntervention(
            title = "Five Minutes of Nonsense",
            primaryActionText = "Read this",
            expectedScreenTag = "reader-screen",
        )
        assertSprint9RenderableReaderCopyIsDisplayed()
        captureSprint9("03_reader_sprint9_renderable_light")

        seedSprint9WonderReaderSelection("s9-3-r08-darwin-insectivorous-plants")
        launchFixtureSystemIntervention()
        openAlternativeFromIntervention(
            title = "Plants That Hunt",
            primaryActionText = "Read this",
            expectedScreenTag = "reader-screen",
        )
        assertSprint9DarwinReaderCopyIsDisplayed()
        captureSprint9("03b_reader_sprint9_darwin_light")

        seedSprint9WonderReaderSelection("s9-3-r11-figuier-ocean-world")
        launchFixtureSystemIntervention()
        openAlternativeFromIntervention(
            title = "The Sea as a World",
            primaryActionText = "Read this",
            expectedScreenTag = "reader-screen",
        )
        assertSprint9FiguierReaderCopyIsDisplayed()
        captureSprint9("03c_reader_sprint9_figuier_light")

        seedSprint9WonderReaderSelection("s9-3-r03-fabre-life-fly")
        launchFixtureSystemIntervention()
        openAlternativeFromIntervention(
            title = "The Fly Under Attention",
            primaryActionText = "Read this",
            expectedScreenTag = "reader-screen",
        )
        assertSprint9FabreFlyReaderCopyIsDisplayed()
        captureSprint9("03d_reader_sprint9_fabre_fly_light")

        seedSprint9LinkOnlySelection()
        launchFixtureSystemIntervention()
        composeRule.onNodeWithText("A Six-Dot Graph Puzzle").assertIsDisplayed()
        captureSprint9("04_intervention_sprint9_link_only_light")

        openAlternativeFromIntervention(
            title = "A Six-Dot Graph Puzzle",
            primaryActionText = "Open link",
            expectedScreenTag = "external-handoff-screen",
        )
        assertSprint9LinkOnlyHandoffCopyIsDisplayed()
        captureSprint9("05_external_handoff_sprint9_light")

        saveDarkTheme()
        seedSprint9DarkHistorySelection()
        launchFixtureSystemIntervention()
        composeRule.onNodeWithText("History at Human Scale").assertIsDisplayed()
        captureSprint9("06_intervention_sprint9_dark")

        openAlternativeFromIntervention(
            title = "History at Human Scale",
            primaryActionText = "Read this",
            expectedScreenTag = "reader-screen",
        )
        composeRule.onNodeWithText("History at Human Scale").assertIsDisplayed()
        assertTrue("Sprint 9 dark reader should show shipped body text", hasNodeContaining("The story of our world"))
        assertTrue("Sprint 9 dark reader should not show source boilerplate", !hasNodeContaining("Project Gutenberg"))
        captureSprint9("07_reader_sprint9_dark")
    }

    @Test
    fun captureSprint12LibraryManageScreens() {
        launchOnboardedApp()
        val savedLinkId = seedSprint12LibraryManageContent()

        openTab("tab-library", "library-list")
        composeRule.onNodeWithTag("library-manage-toggle")
            .assertIsDisplayed()
            .performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("library-manage-panel") }
        captureSprint12("01_library_manage_panel_light")
        composeRule.onNodeWithTag("library-list")
            .performScrollToNode(hasTestTag("library-editorial-note-start-with-what-is-yours"))
        composeRule.onNodeWithTag("library-editorial-note-start-with-what-is-yours")
            .assertIsDisplayed()
        composeRule.onNodeWithTag("library-list")
            .performScrollToNode(hasText("Saved Sprint 12 link"))
        captureSprint12("02_library_manage_content_light")

        composeRule.onNodeWithTag("library-list")
            .performScrollToNode(hasText("Your links"))
        composeRule.onNodeWithText("Your links")
            .assertIsDisplayed()
            .performClick()

        val selectTag = "library-select-$savedLinkId"
        composeRule.onNodeWithTag("library-list")
            .performScrollToNode(hasTestTag(selectTag))
        composeRule.onNodeWithTag(selectTag)
            .assertIsDisplayed()
            .performClick()
        composeRule.onNodeWithTag("library-list")
            .performScrollToNode(hasText("Saved Sprint 12 link"))
        composeRule.onNodeWithText("Selected").assertIsDisplayed()
        captureSprint12("03_library_selected_light")

        openTab("tab-settings", "settings-list")
        composeRule.onNodeWithTag("settings-list")
            .performScrollToNode(hasTestTag("theme-DARK"))
        composeRule.onNodeWithTag("theme-DARK")
            .performSemanticsAction(SemanticsActions.OnClick)
        composeRule.waitForIdle()
        openTab("tab-library", "library-list")
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("library-manage-panel") }
        composeRule.onNodeWithTag("library-list")
            .performScrollToNode(hasText("Your links"))
        composeRule.onNodeWithText("Your links")
            .assertIsDisplayed()
            .performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) { hasNode("Saved Sprint 12 link") }
        captureSprint12("04_library_selected_dark")

        composeRule.onNodeWithTag("library-list")
            .performScrollToNode(hasTestTag("library-delete-selected"))
        composeRule.onNodeWithTag("library-delete-selected")
            .assertIsDisplayed()
            .performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) { !hasNode("Saved Sprint 12 link") }
        captureSprint12("05_library_after_delete_dark")
    }

    @Test
    fun captureSprint12AddFlowScreens() {
        launchOnboardedApp()

        scenario?.onActivity { activity -> activity.mainViewModel.openAddLink() }
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("add-link-screen") }
        captureSprint12("06_add_content_light")

        scenario?.onActivity { activity -> activity.mainViewModel.selectThemeMode(AppThemeMode.DARK) }
        composeRule.waitForIdle()
        captureSprint12("07_add_content_dark")

        scenario?.onActivity { activity -> activity.mainViewModel.selectThemeMode(AppThemeMode.LIGHT) }
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("add-link-url").performTextInput("https://example.com/sprint-12-priority")
        composeRule.onNodeWithTag("add-link-title").performTextInput("Priority link")
        UiDevice.getInstance(InstrumentationRegistry.getInstrumentation()).pressBack()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("add-link-topic-SCIENCE")
            .performScrollTo()
            .performClick()
        composeRule.onNodeWithTag("add-link-priority")
            .performScrollTo()
            .performClick()
        captureSprint12("08_add_link_priority_light")

        scenario?.onActivity { activity -> activity.mainViewModel.selectThemeMode(AppThemeMode.DARK) }
        composeRule.waitForIdle()
        captureSprint12("09_add_link_priority_dark")

        scenario?.onActivity { activity ->
            activity.mainViewModel.selectThemeMode(AppThemeMode.LIGHT)
            activity.mainViewModel.prepareUserDocumentBatchImport(
                candidates = listOf(
                    DocumentImportCandidate(
                        uri = "content://visual/short-notes",
                        displayName = "short-notes.md",
                        mimeType = "text/markdown",
                        title = "Short notes",
                        durationMinutes = "3",
                        format = ContentFormat.MARKDOWN,
                        estimateSource = ReadingTimeEstimateSource.EXTRACTED_TEXT,
                        estimatedWordCount = 420,
                    ),
                    DocumentImportCandidate(
                        uri = "content://visual/deep-book",
                        displayName = "deep-book.epub",
                        mimeType = "application/epub+zip",
                        title = "Deep book",
                        durationMinutes = "20",
                        format = ContentFormat.EPUB,
                        estimateSource = ReadingTimeEstimateSource.EXTRACTED_TEXT,
                        estimatedWordCount = 12_000,
                    ),
                    DocumentImportCandidate(
                        uri = "content://visual/archive",
                        displayName = "archive.zip",
                        mimeType = "application/zip",
                        title = "Archive",
                        durationMinutes = "10",
                        format = null,
                    ),
                ),
            )
        }
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("add-document-screen") }
        captureSprint12("10_batch_import_files_light")

        composeRule.onNodeWithTag("add-document-topic-SCIENCE")
            .performScrollTo()
            .performClick()
        composeRule.onNodeWithTag("add-document-priority")
            .performScrollTo()
            .performClick()
        captureSprint12("11_batch_import_priority_light")

        scenario?.onActivity { activity -> activity.mainViewModel.selectThemeMode(AppThemeMode.DARK) }
        composeRule.waitForIdle()
        captureSprint12("12_batch_import_priority_dark")

        scenario?.onActivity { activity -> activity.mainViewModel.selectThemeMode(AppThemeMode.LIGHT) }
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("add-document-save")
            .performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("add-link-success-screen") }
        captureSprint12("13_batch_import_result_light")

        scenario?.onActivity { activity -> activity.mainViewModel.selectThemeMode(AppThemeMode.DARK) }
        composeRule.waitForIdle()
        captureSprint12("14_batch_import_result_dark")
    }

    @Test
    fun captureSprint12ContinueReadingScreens() {
        val document = seedSprint12ContinueReadingDocument()
        launchApp()
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("home-list") }

        openTab("tab-library", "library-list")
        composeRule.onNodeWithTag("library-list")
            .performScrollToNode(hasText(document.title))
        composeRule.onNodeWithTag("library-open-${document.id}")
            .assertIsDisplayed()
            .performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("reader-screen") }
        captureSprint12("15_reader_start_light")

        advanceReaderToText("Chapter Two")
        captureSprint12("16_reader_mid_light")

        scenario?.onActivity { activity ->
            activity.mainViewModel.saveCurrentReadingProgress(
                progressPercent = 58,
                lastVisibleParagraphIndex = 6,
                paragraphCount = 14,
            )
            activity.mainViewModel.openHome()
        }
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("home-continue-card") }
        captureSprint12("17_home_continue_light")

        composeRule.onNodeWithTag("home-continue-action")
            .performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("reader-screen") }
        captureSprint12("18_reader_continued_light")

        scenario?.onActivity { activity -> activity.mainViewModel.openLibrary() }
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("library-list") }
        composeRule.onNodeWithText("Unfinished")
            .assertIsDisplayed()
            .performClick()
        composeRule.onNodeWithTag("library-manage-toggle")
            .assertIsDisplayed()
            .performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("library-manage-panel") && hasNodeContaining("% read") }
        captureSprint12("19_library_unfinished_manage_light")

        scenario?.onActivity { activity -> activity.mainViewModel.triggerDebugIntervention(nowMillis = 2_500L) }
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("intervention-screen") && hasNode("Unfinished") }
        captureSprint12("20_intervention_unfinished_priority_light")

        scenario?.onActivity { activity ->
            activity.mainViewModel.selectThemeMode(AppThemeMode.DARK)
            activity.mainViewModel.openHome()
        }
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("home-continue-card") }
        captureSprint12("21_home_continue_dark")

        composeRule.onNodeWithTag("home-continue-action")
            .performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("reader-screen") }
        captureSprint12("22_reader_continued_dark")

        scenario?.onActivity { activity -> activity.mainViewModel.openLibrary() }
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("library-list") }
        composeRule.onNodeWithText("Unfinished")
            .assertIsDisplayed()
            .performClick()
        composeRule.onNodeWithTag("library-manage-toggle")
            .assertIsDisplayed()
            .performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("library-manage-panel") && hasNodeContaining("% read") }
        captureSprint12("23_library_unfinished_manage_dark")

        scenario?.onActivity { activity -> activity.mainViewModel.triggerDebugIntervention(nowMillis = 3_500L) }
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("intervention-screen") && hasNode("Unfinished") }
        captureSprint12("24_intervention_unfinished_priority_dark")

        val darkReaderDocument = seedSprint12ContinueReadingDocument(
            fileName = "sprint12-dark-reader.epub",
            title = "Dark Reader Fixture",
            nowMillis = 2_100L,
        )
        scenario?.onActivity { activity -> activity.mainViewModel.openLibrary() }
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("library-list") }
        composeRule.onNodeWithTag("library-list")
            .performScrollToNode(hasTestTag("library-open-${darkReaderDocument.id}"))
        composeRule.onNodeWithTag("library-open-${darkReaderDocument.id}")
            .assertIsDisplayed()
            .performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("reader-screen") }
        captureSprint12("25_reader_start_dark")

        advanceReaderToText("Chapter Two")
        captureSprint12("26_reader_mid_dark")
    }

    @Test
    fun captureSprint22ReadingTimeRemainingRepair() {
        seedSprint22LegacyLongContinueDocument()
        launchApp()
        composeRule.waitUntil(timeoutMillis = 10_000) {
            hasTag("home-continue-card") && hasNodeContaining("41% read")
        }
        captureSprint22("00_home_continue_before_repair_assertion")
        composeRule.waitUntil(timeoutMillis = 15_000) {
            hasTag("home-continue-card") &&
                !hasNodeContaining("12 min left") &&
                hasNodeContaining("1 hr 20 min left")
        }
        captureSprint22("01_home_continue_after_repair_wait")
        var repairedDurationMinutes = 0
        scenario?.onActivity { activity ->
            repairedDurationMinutes = activity.mainViewModel.uiState.userDocuments.single().durationMinutes
        }
        assertTrue("Repaired duration should replace the legacy 20-minute estimate", repairedDurationMinutes > 20)
        assertTrue("Legacy 20-minute estimate should not remain visible", !hasNodeContaining("12 min left"))
        assertTrue("Expected corrected remaining-time label to be visible", hasNodeContaining("1 hr 20 min left"))
        captureSprint22("02_home_continue_repaired_remaining_time")
    }

    @Test
    fun captureSprint12FinalJourneyScreens() {
        launchOnboardedApp()
        captureSprint12Final("01_home_light")

        openAddLinkFromHome()
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("add-link-screen") }
        captureSprint12Final("02_add_content_light")

        composeRule.onNodeWithTag("add-link-url").performTextInput("https://example.com/final-sprint-12-link")
        composeRule.onNodeWithTag("add-link-title").performTextInput("Final Sprint 12 link")
        UiDevice.getInstance(InstrumentationRegistry.getInstrumentation()).pressBack()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("add-link-topic-SCIENCE")
            .performScrollTo()
            .performClick()
        composeRule.onNodeWithTag("add-link-priority")
            .performScrollTo()
            .performClick()
        captureSprint12Final("03_add_link_priority_light")

        composeRule.onNodeWithTag("add-link-save")
            .assertIsDisplayed()
            .performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("add-link-success-screen") }
        captureSprint12Final("04_add_link_success_light")
        composeRule.onNodeWithTag("add-link-done").performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("library-list") }
        composeRule.waitUntil(timeoutMillis = 10_000) {
            !hasNodeContaining("Saved for future replacement moments.")
        }

        scenario?.onActivity { activity ->
            activity.mainViewModel.prepareUserDocumentBatchImport(
                candidates = listOf(
                    DocumentImportCandidate(
                        uri = "content://final-visual/short-notes",
                        displayName = "final-short-notes.md",
                        mimeType = "text/markdown",
                        title = "Final short notes",
                        durationMinutes = "3",
                        format = ContentFormat.MARKDOWN,
                        estimateSource = ReadingTimeEstimateSource.EXTRACTED_TEXT,
                        estimatedWordCount = 420,
                    ),
                    DocumentImportCandidate(
                        uri = "content://final-visual/deep-book",
                        displayName = "final-deep-book.epub",
                        mimeType = "application/epub+zip",
                        title = "Final deep book",
                        durationMinutes = "20",
                        format = ContentFormat.EPUB,
                        estimateSource = ReadingTimeEstimateSource.EXTRACTED_TEXT,
                        estimatedWordCount = 12_000,
                    ),
                    DocumentImportCandidate(
                        uri = "content://final-visual/archive",
                        displayName = "final-archive.zip",
                        mimeType = "application/zip",
                        title = "Archive",
                        durationMinutes = "10",
                        format = null,
                    ),
                ),
            )
        }
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("add-document-screen") }
        captureSprint12Final("05_batch_import_files_light")

        composeRule.onNodeWithTag("add-document-topic-SCIENCE")
            .performScrollTo()
            .performClick()
        composeRule.onNodeWithTag("add-document-priority")
            .performScrollTo()
            .performClick()
        captureSprint12Final("06_batch_import_priority_light")

        scenario?.onActivity { activity ->
            activity.mainViewModel.saveUserDocument(persistReadPermission = {})
        }
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("add-link-success-screen") }
        captureSprint12Final("07_batch_import_result_light")
        composeRule.onNodeWithTag("add-link-done").performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("library-list") }
        composeRule.onNodeWithTag("library-manage-toggle")
            .assertIsDisplayed()
            .performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("library-manage-panel") }
        composeRule.onNodeWithTag("library-list")
            .performScrollToNode(hasText("Final Sprint 12 link"))
        captureSprint12Final("08_library_manage_light")

        scenario?.onActivity { activity ->
            activity.mainViewModel.selectThemeMode(AppThemeMode.DARK)
            activity.mainViewModel.openHome()
        }
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("home-list") }
        openAddLinkFromHome()
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("add-link-screen") }
        captureSprint12Final("09_add_content_dark")

        composeRule.onNodeWithTag("add-link-url").performTextInput("https://example.com/final-sprint-12-dark-link")
        composeRule.onNodeWithTag("add-link-title").performTextInput("Final Sprint 12 dark link")
        UiDevice.getInstance(InstrumentationRegistry.getInstrumentation()).pressBack()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("add-link-topic-SCIENCE")
            .performScrollTo()
            .performClick()
        composeRule.onNodeWithTag("add-link-priority")
            .performScrollTo()
            .performClick()
        captureSprint12Final("10_add_link_priority_dark")

        composeRule.onNodeWithTag("add-link-save")
            .assertIsDisplayed()
            .performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("add-link-success-screen") }
        captureSprint12Final("11_add_link_success_dark")
        composeRule.onNodeWithTag("add-link-done").performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("library-list") }
        composeRule.waitUntil(timeoutMillis = 10_000) {
            !hasNodeContaining("Saved for future replacement moments.")
        }

        scenario?.onActivity { activity ->
            activity.mainViewModel.prepareUserDocumentBatchImport(
                candidates = listOf(
                    DocumentImportCandidate(
                        uri = "content://final-visual-dark/short-notes",
                        displayName = "final-dark-short-notes.md",
                        mimeType = "text/markdown",
                        title = "Final dark short notes",
                        durationMinutes = "3",
                        format = ContentFormat.MARKDOWN,
                        estimateSource = ReadingTimeEstimateSource.EXTRACTED_TEXT,
                        estimatedWordCount = 420,
                    ),
                    DocumentImportCandidate(
                        uri = "content://final-visual-dark/deep-book",
                        displayName = "final-dark-deep-book.epub",
                        mimeType = "application/epub+zip",
                        title = "Final dark deep book",
                        durationMinutes = "20",
                        format = ContentFormat.EPUB,
                        estimateSource = ReadingTimeEstimateSource.EXTRACTED_TEXT,
                        estimatedWordCount = 12_000,
                    ),
                    DocumentImportCandidate(
                        uri = "content://final-visual-dark/archive",
                        displayName = "final-dark-archive.zip",
                        mimeType = "application/zip",
                        title = "Archive",
                        durationMinutes = "10",
                        format = null,
                    ),
                ),
            )
        }
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("add-document-screen") }
        captureSprint12Final("12_batch_import_files_dark")

        composeRule.onNodeWithTag("add-document-topic-SCIENCE")
            .performScrollTo()
            .performClick()
        composeRule.onNodeWithTag("add-document-priority")
            .performScrollTo()
            .performClick()
        captureSprint12Final("13_batch_import_priority_dark")

        scenario?.onActivity { activity ->
            activity.mainViewModel.saveUserDocument(persistReadPermission = {})
        }
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("add-link-success-screen") }
        captureSprint12Final("14_batch_import_result_dark")
        composeRule.onNodeWithTag("add-link-done").performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("library-list") }
        composeRule.waitUntil(timeoutMillis = 10_000) {
            !hasNodeContaining("Saved for future replacement moments.")
        }
        if (!hasTag("library-manage-panel")) {
            composeRule.onNodeWithTag("library-manage-toggle")
                .assertIsDisplayed()
                .performClick()
            composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("library-manage-panel") }
        }
        composeRule.onNodeWithTag("library-list")
            .performScrollToNode(hasText("Final Sprint 12 dark link"))
        captureSprint12Final("15_library_manage_dark")

        val continueDocument = seedSprint12ContinueReadingDocument(
            fileName = "final-journey-continue.epub",
            title = "Final Journey Continue",
            nowMillis = 2_300L,
        )
        scenario?.onActivity { activity ->
            activity.mainViewModel.selectThemeMode(AppThemeMode.LIGHT)
            activity.mainViewModel.openHome()
            activity.mainViewModel.openLibrary()
        }
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("library-list") }
        composeRule.onNodeWithTag("library-list")
            .performScrollToNode(hasText(continueDocument.title))
        scenario?.onActivity { activity ->
            activity.mainViewModel.openLibraryItem(continueDocument)
        }
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("reader-screen") }
        captureSprint12Final("16_reader_start_light")

        advanceReaderToText("Chapter Two")
        captureSprint12Final("17_reader_mid_light")

        scenario?.onActivity { activity ->
            activity.mainViewModel.saveCurrentReadingProgress(
                progressPercent = 58,
                lastVisibleParagraphIndex = 6,
                paragraphCount = 14,
            )
            activity.mainViewModel.openHome()
        }
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("home-continue-card") }
        captureSprint12Final("18_home_continue_light")

        scenario?.onActivity { activity -> activity.mainViewModel.openLibrary() }
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("library-list") }
        composeRule.onNodeWithText("Unfinished")
            .assertIsDisplayed()
            .performClick()
        composeRule.onNodeWithTag("library-manage-toggle")
            .assertIsDisplayed()
            .performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("library-manage-panel") && hasNodeContaining("% read") }
        captureSprint12Final("19_library_unfinished_light")

        scenario?.onActivity { activity -> activity.mainViewModel.triggerDebugIntervention(nowMillis = 4_500L) }
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("intervention-screen") && hasNode("Unfinished") }
        captureSprint12Final("20_intervention_unfinished_light")

        scenario?.onActivity { activity ->
            activity.mainViewModel.selectThemeMode(AppThemeMode.DARK)
            activity.mainViewModel.openHome()
        }
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("home-list") }
        captureSprint12Final("21_home_dark")
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("home-continue-card") }
        captureSprint12Final("22_home_continue_dark")

        composeRule.onNodeWithTag("home-continue-action")
            .assertIsDisplayed()
            .performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("reader-screen") }
        captureSprint12Final("23_reader_continued_dark")

        scenario?.onActivity { activity -> activity.mainViewModel.openLibrary() }
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("library-list") }
        composeRule.onNodeWithText("Unfinished")
            .assertIsDisplayed()
            .performClick()
        composeRule.onNodeWithTag("library-manage-toggle")
            .assertIsDisplayed()
            .performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("library-manage-panel") && hasNodeContaining("% read") }
        captureSprint12Final("24_library_unfinished_dark")

        scenario?.onActivity { activity -> activity.mainViewModel.triggerDebugIntervention(nowMillis = 5_500L) }
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("intervention-screen") && hasNode("Unfinished") }
        captureSprint12Final("25_intervention_unfinished_dark")

        val darkReaderProofDocument = seedSprint12ContinueReadingDocument(
            fileName = "final-dark-reader-proof.epub",
            title = "Final Dark Reader Proof",
            nowMillis = 2_700L,
        )
        scenario?.onActivity { activity ->
            activity.mainViewModel.openLibrary()
            activity.mainViewModel.openLibraryItem(darkReaderProofDocument)
        }
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("reader-screen") }
        captureSprint12Final("26_reader_start_dark")

        advanceReaderToText("Chapter Two")
        captureSprint12Final("27_reader_mid_dark")
    }

    @Test
    fun captureSprint13CompletedActivationAndUnlockScreens() {
        launchOnboardedApp()
        seedAttentionClassicsSelection()
        launchFixtureSystemIntervention()

        composeRule.onNodeWithText("Read this", substring = true).performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("reader-screen") }
        val completedContent = currentContentIdAndTitle()
        finishReaderFromCurrentPage()
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("feedback-screen") }

        scenario?.onActivity { activity -> activity.mainViewModel.openLibrary() }
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("library-list") }
        composeRule.onNodeWithTag("library-list")
            .performScrollToNode(hasTestTag("library-open-${completedContent.first}"))
            .performTouchInput { swipeUp(startY = 1_850f, endY = 1_650f) }
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("library-completed-status-${completedContent.first}") }
        captureSprint13("01_library_completed_hidden_light")

        composeRule.onNodeWithTag("completed-activation-${completedContent.first}").performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) { hasNode("Completed · active in suggestions") }
        scenario?.onActivity { activity -> activity.mainViewModel.dismissMessage() }
        composeRule.onNodeWithTag("library-list")
            .performScrollToNode(hasTestTag("library-open-${completedContent.first}"))
            .performTouchInput { swipeUp(startY = 1_850f, endY = 1_650f) }
        composeRule.waitForIdle()
        Thread.sleep(500)
        captureSprint13("02_library_completed_reactivated_light")

        seedMeditationSelection()
        launchFixtureSystemIntervention()
        composeRule.waitUntil(timeoutMillis = 10_000) { hasNode("3-minute reset") }
        captureSprint13("03_intervention_meditation_available_before_completion_light")
        startMeditationFromIntervention()
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("meditation-timer-screen") }
        scenario?.onActivity { activity ->
            activity.mainViewModel.finishMeditationReset(nowMillis = 12_000L)
        }
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("feedback-screen") }
        launchFixtureSystemIntervention()
        composeRule.waitUntil(timeoutMillis = 10_000) { hasNode("3-minute reset") }
        captureSprint13("04_intervention_meditation_available_after_completion_light")

        scenario?.onActivity { activity -> activity.mainViewModel.openLibrary() }
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("library-list") }
        assertTrue("Meditation timer should not appear in the library", !hasTag("library-item-$MEDITATION_TIMER_CONTENT_ID"))
        captureSprint13("05_library_without_meditation_light")

        openTab("tab-settings", "settings-list")
        composeRule.onNodeWithTag("settings-list")
            .performScrollToNode(hasTestTag("open-anyway-unlock-120"))
        composeRule.onNodeWithTag("open-anyway-unlock-120").performClick()
        composeRule.waitForIdle()
        captureSprint13("06_settings_open_anyway_unlock_light")

        scenario?.onActivity { activity ->
            activity.mainViewModel.selectThemeMode(AppThemeMode.DARK)
        }
        composeRule.waitForIdle()
        captureSprint13("07_settings_open_anyway_unlock_dark")

        seedAttentionClassicsSelection()
        launchFixtureSystemIntervention()
        composeRule.waitUntil(timeoutMillis = 8_000) {
            hasNode("Open Fixture Feed One") && !hasTag("form-intervention-unlock-wait")
        }
        composeRule.onNodeWithText("Open Fixture Feed One").performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) {
            scenario?.state == androidx.lifecycle.Lifecycle.State.DESTROYED
        }
        scenario = null

        val targetContext = InstrumentationRegistry.getInstrumentation().targetContext
        launchApp(
            MainActivity.createSystemInterceptionIntent(
                context = targetContext,
                targetAppPackage = FixtureTargetRegistry.fixtureDistractors.first().packageName,
            ),
        )
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("home-list") }
        captureSprint13("08_open_anyway_unlocked_home_dark")
    }

    @Test
    fun captureSprint15AutoTimeImportScreens() {
        launchOnboardedApp()

        scenario?.onActivity { activity ->
            activity.mainViewModel.prepareUserDocumentBatchImport(
                candidates = listOf(
                    DocumentImportCandidate(
                        uri = "content://visual/deep-book",
                        displayName = "deep-book.epub",
                        mimeType = "application/epub+zip",
                        title = "Deep book",
                        durationMinutes = "20",
                        format = ContentFormat.EPUB,
                        estimateSource = ReadingTimeEstimateSource.EXTRACTED_TEXT,
                        estimatedWordCount = 12_000,
                    ),
                    DocumentImportCandidate(
                        uri = "content://visual/short-notes",
                        displayName = "short-notes.md",
                        mimeType = "text/markdown",
                        title = "Short notes",
                        durationMinutes = "3",
                        format = ContentFormat.MARKDOWN,
                        estimateSource = ReadingTimeEstimateSource.EXTRACTED_TEXT,
                        estimatedWordCount = 420,
                    ),
                ),
            )
        }
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("add-document-screen") }
        assertTrue("Estimated session row should be hidden during auto-time import", !hasNode("Estimated session"))
        captureSprint15("01_auto_time_import_light")

        composeRule.onNodeWithTag("add-document-topic-SCIENCE")
            .performScrollTo()
            .performClick()
        composeRule.onNodeWithTag("add-document-priority")
            .performScrollTo()
            .performClick()
        captureSprint15("02_auto_time_priority_light")

        scenario?.onActivity { activity -> activity.mainViewModel.selectThemeMode(AppThemeMode.DARK) }
        composeRule.waitForIdle()
        captureSprint15("03_auto_time_priority_dark")
    }

    @Test
    fun captureSprint15EpubStructuredDocumentSmokeScreens() {
        launchOnboardedApp()
        val content = seedUserEpubSelection(title = "Structured EPUB TOC Fixture", fileName = "structured-toc.epub")

        scenario?.onActivity { activity -> activity.mainViewModel.openLibraryItem(content) }
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("reader-screen") }
        composeRule.onNodeWithText("Structured EPUB TOC Fixture").assertIsDisplayed()
        composeRule.onNodeWithText("Structured EPUB Notes").assertIsDisplayed()
        composeRule.onNodeWithText("First EPUB bullet with bold text", substring = true).assertIsDisplayed()
        assertTrue("Raw EPUB bold markers should not be visible", !hasNodeContaining("**bold**"))
        captureSprint15Slice151("01_epub_structured_reader_light")

        advanceReaderToText("Chapter Two")
        composeRule.onNodeWithText("Chapter Two").assertIsDisplayed()
        captureSprint15Slice151("02_epub_structured_reader_mid_light")

        scenario?.onActivity { activity -> activity.mainViewModel.selectThemeMode(AppThemeMode.DARK) }
        composeRule.waitForIdle()
        captureSprint15Slice151("03_epub_structured_reader_mid_dark")
    }

    @Test
    fun captureSprint20EpubLoadingBusyStatesScreens() {
        launchOnboardedApp()

        scenario?.onActivity { activity ->
            activity.mainViewModel.prepareUserDocumentImport(
                uri = "content://visual/slow-import.epub",
                displayName = "slow-import.epub",
                mimeType = "application/epub+zip",
            ) {
                Thread.sleep(4_000)
                ByteArrayInputStream(sprint10EpubBytes())
            }
        }
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("add-document-screen") }
        composeRule.onNodeWithText("Preparing selected files...", ignoreCase = true).performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Preparing the book. Large EPUBs can take a moment.").performScrollTo().assertIsDisplayed()
        captureSprint20("01_epub_import_preparing_light")

        composeRule.waitUntil(timeoutMillis = 10_000) { hasNodeContaining("ready") }

        val content = seedUserEpubSelection(
            title = "Large EPUB Opening Fixture",
            fileName = "large-opening.epub",
            epubBytes = sprint20LargeOpeningEpubBytes(),
        )
        scenario?.onActivity { activity -> activity.mainViewModel.openLibraryItem(content) }
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("reader-opening-overlay") }
        composeRule.onNodeWithTag("reader-opening-overlay").assertIsDisplayed()
        captureSprint20("02_reader_opening_overlay_light")
    }

    @Test
    fun captureSprint15KindlePagingAndTocScreens() {
        launchOnboardedApp()
        val content = seedUserEpubSelection(title = "Kindle Paging EPUB", fileName = "kindle-paging-toc.epub")

        scenario?.onActivity { activity -> activity.mainViewModel.openLibraryItem(content) }
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("reader-screen") }
        composeRule.onNodeWithTag("reader-page-viewport").assertIsDisplayed()
        composeRule.onNodeWithTag("reader-toc-open").assertIsDisplayed()
        composeRule.onNodeWithText("Chapter One").assertIsDisplayed()
        captureSprint15Slice152("01_page_one_fixed_viewport_light")

        composeRule.onNodeWithTag("reader-list").performTouchInput { swipeUp() }
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Chapter One").assertIsDisplayed()
        captureSprint15Slice152("02_swipe_does_not_scroll_reader_light")

        composeRule.onNodeWithTag("reader-page-viewport").performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) { hasNodeContaining("2/") }
        captureSprint15Slice152("03_tap_advances_page_light")

        composeRule.onNodeWithTag("reader-page-viewport").performTouchInput { swipeRight() }
        composeRule.waitUntil(timeoutMillis = 10_000) { hasNodeContaining("1/") && hasNode("Chapter One") }
        composeRule.onNodeWithTag("reader-toc-open").performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("reader-toc-sheet") }
        captureSprint15Slice152("04_contents_sheet_light")

        val chapterTwoIndex = readerTocEntryIndex("Chapter Two")
        composeRule.onNodeWithTag("reader-toc-entry-$chapterTwoIndex")
            .assertIsDisplayed()
            .performSemanticsAction(SemanticsActions.OnClick)
        composeRule.waitUntil(timeoutMillis = 10_000) {
            hasNodeContaining("second chapter keeps") && !hasTag("reader-toc-sheet")
        }
        captureSprint15Slice152("05_toc_jump_chapter_two_light")

        advanceReaderToTextContaining("private to the device")
        composeRule.onNodeWithText("private to the device", substring = true).assertIsDisplayed()
        captureSprint15Slice152("06_chapter_two_continuation_light")

        advanceReaderToTextContaining("Long single paragraph starts")
        composeRule.onNodeWithText("Long single paragraph starts", substring = true).assertIsDisplayed()
        captureSprint15Slice152("07_long_single_paragraph_start_light")

        advanceReaderToTextContaining("Long single paragraph ends")
        composeRule.onNodeWithText("Long single paragraph ends", substring = true).assertIsDisplayed()
        captureSprint15Slice152("08_long_single_paragraph_end_light")

        scenario?.onActivity { activity -> activity.mainViewModel.selectThemeMode(AppThemeMode.DARK) }
        composeRule.waitForIdle()
        captureSprint15Slice152("09_long_single_paragraph_end_dark")
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

    private fun captureSprint9(name: String) {
        captureTo(sprint9ScreenshotDir, name)
    }

    private fun captureSprint12(name: String) {
        captureTo(sprint12ScreenshotDir, name)
    }

    private fun captureSprint12Final(name: String) {
        captureTo(sprint12FinalScreenshotDir, name)
    }

    private fun captureSprint13(name: String) {
        captureTo(sprint13ScreenshotDir, name)
    }

    private fun captureSprint15(name: String) {
        captureTo(sprint15ScreenshotDir, name)
    }

    private fun captureSprint15Slice151(name: String) {
        captureTo(sprint15Slice151ScreenshotDir, name)
    }

    private fun captureSprint15Slice152(name: String) {
        captureTo(sprint15Slice152ScreenshotDir, name)
    }

    private fun captureSprint20(name: String) {
        captureTo(sprint20ScreenshotDir, name)
    }

    private fun captureSprint22(name: String) {
        captureTo(sprint22ScreenshotDir, name)
    }

    private fun captureSprint25(name: String) {
        captureTo(sprint25ScreenshotDir, name)
    }

    private fun captureSprint26(name: String) {
        captureTo(sprint26ScreenshotDir, name)
    }

    private fun captureTo(directory: File, name: String) {
        composeRule.waitForIdle()
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        Thread.sleep(350)
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        val file = File(directory, "$name.png")
        var captured = false
        repeat(3) { attempt ->
            captured = device.takeScreenshot(file) && file.length() > 10_000L
            if (!captured) {
                file.delete()
                Thread.sleep(500L + (attempt * 250L))
            }
        }
        assertTrue("Could not capture non-empty $name into ${file.absolutePath}", captured)
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

        finishReaderFromCurrentPage()
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("feedback-screen") }
        captureLegacy("05_feedback_light")

        composeRule.onNodeWithText("Skip").performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("progress-list") }
        Thread.sleep(6_000)
        captureLegacy("06_progress_light")

        seedMeditationSelection()
        launchFixtureSystemIntervention()
        captureLegacy("07_intervention_meditation_light")
        startMeditationFromIntervention()
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
        startMeditationFromIntervention()
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("meditation-timer-screen") }
        captureLegacy("14_meditation_timer_dark")
    }

    private fun openTab(tabTag: String, expectedScreenTag: String) {
        composeRule.onNodeWithTag(tabTag, useUnmergedTree = true)
            .performSemanticsAction(SemanticsActions.OnClick)
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag(expectedScreenTag) }
    }

    private fun openAddLinkFromHome() {
        composeRule.onNodeWithTag("home-list")
            .performScrollToNode(hasTestTag("home-add-link"))
        composeRule.onNodeWithTag("home-add-link")
            .assertIsDisplayed()
            .performClick()
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

    private fun launchFreshAppThroughTopicVisualQa() {
        launchApp()
        composeRule.waitUntil(timeoutMillis = 10_000) { hasNodeContaining("Turn an impulse") }
        composeRule.onNodeWithText("Begin").performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) { hasNode("Which apps pull at you?") }
        composeRule.onNodeWithText("Continue").performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) { hasNode("What would you rather read?") }
        composeRule.onNodeWithText("Attention").assertIsDisplayed()
        composeRule.onNodeWithText("Practical").assertIsDisplayed()
        composeRule.onNodeWithText("Creativity").assertIsDisplayed()
        capture("00_onboarding_topics_light")
        composeRule.onNodeWithText("Continue").performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) { hasNode("How long should a session feel?") }
        composeRule.onNodeWithText("Continue").performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) { hasNode("Two small permissions.") }
        composeRule.onNodeWithText("Grant & finish").performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("home-list") }
    }

    private fun completeOnboardingIfNeeded() {
        composeRule.waitUntil(timeoutMillis = 10_000) {
            hasNodeContaining("Turn an impulse") ||
                hasNode("You're set up for quieter reading today.") ||
                hasNode("Finish setup to intercept distracting apps.")
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
        composeRule.onNodeWithTag("intervention-primary-explanation").assertIsDisplayed()
        assertFiniteChoicesAboveBottomActions()
    }

    private fun openAlternativeFromIntervention(
        title: String,
        primaryActionText: String,
        expectedScreenTag: String,
    ) {
        val openedFromVisibleRow = runCatching {
            composeRule.onNodeWithText(title).performClick()
            composeRule.waitUntil(timeoutMillis = 3_000) { hasTag(expectedScreenTag) }
            true
        }.getOrDefault(false)
        if (!openedFromVisibleRow) {
            composeRule.onNodeWithText(primaryActionText, substring = true).performClick()
            composeRule.waitUntil(timeoutMillis = 10_000) { hasTag(expectedScreenTag) }
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

    private fun seedSprint9AllSelection() = runBlocking {
        val repository = (InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as QualityAlternativeApplication)
            .appContainer
            .settingsRepository
        repository.saveOnboardingSelection(
            OnboardingSelection(
                selectedAppPackages = setOf(FixtureTargetRegistry.fixtureDistractors.first().packageName),
                preferredTopics = setOf(
                    TopicTag.ATTENTION,
                    TopicTag.PRACTICAL,
                    TopicTag.BODY,
                    TopicTag.NATURE,
                    TopicTag.HISTORY_CULTURE,
                    TopicTag.CREATIVITY,
                    TopicTag.SCIENCE,
                ),
                preferredDurationBucket = DurationBucket.FOCUS,
                selectedPackIds = sprint9PackIds(),
            ),
        )
    }

    private fun seedSprint9RenderableSelection() = runBlocking {
        val repository = (InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as QualityAlternativeApplication)
            .appContainer
            .settingsRepository
        repository.saveOnboardingSelection(
            OnboardingSelection(
                selectedAppPackages = setOf(FixtureTargetRegistry.fixtureDistractors.first().packageName),
                preferredTopics = setOf(TopicTag.CREATIVITY, TopicTag.PHILOSOPHY, TopicTag.PSYCHOLOGY),
                preferredDurationBucket = DurationBucket.FOCUS,
                selectedPackIds = setOf("creativity_play_v1"),
            ),
        )
        repository.savePriorityContentIds(setOf("s9-5-r02-lear-book-nonsense"))
    }

    private fun seedSprint9LinkOnlySelection() = runBlocking {
        val repository = (InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as QualityAlternativeApplication)
            .appContainer
            .settingsRepository
        repository.saveOnboardingSelection(
            OnboardingSelection(
                selectedAppPackages = setOf(FixtureTargetRegistry.fixtureDistractors.first().packageName),
                preferredTopics = setOf(TopicTag.CREATIVITY, TopicTag.SCIENCE, TopicTag.TECH),
                preferredDurationBucket = DurationBucket.FOCUS,
                selectedPackIds = setOf("creativity_play_v1"),
            ),
        )
        repository.savePriorityContentIds(setOf("s9-5-l01-quanta-local-global-graph"))
    }

    private fun seedSprint9WonderReaderSelection(contentId: String) = runBlocking {
        val repository = (InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as QualityAlternativeApplication)
            .appContainer
            .settingsRepository
        repository.saveOnboardingSelection(
            OnboardingSelection(
                selectedAppPackages = setOf(FixtureTargetRegistry.fixtureDistractors.first().packageName),
                preferredTopics = setOf(TopicTag.SCIENCE, TopicTag.NATURE),
                preferredDurationBucket = DurationBucket.FOCUS,
                selectedPackIds = setOf("wonder_science_v1"),
            ),
        )
        repository.savePriorityContentIds(setOf(contentId))
    }

    private fun seedSprint9DarkHistorySelection() = runBlocking {
        val repository = (InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as QualityAlternativeApplication)
            .appContainer
            .settingsRepository
        repository.saveOnboardingSelection(
            OnboardingSelection(
                selectedAppPackages = setOf(FixtureTargetRegistry.fixtureDistractors.first().packageName),
                preferredTopics = setOf(TopicTag.HISTORY_CULTURE, TopicTag.SCIENCE, TopicTag.PHILOSOPHY),
                preferredDurationBucket = DurationBucket.FOCUS,
                selectedPackIds = setOf("long_view_history_v1"),
            ),
        )
        repository.savePriorityContentIds(setOf("s9-4-r01-wells-short-history-world"))
    }

    private fun sprint9PackIds(): Set<String> = setOf(
        "attention_practical_agency_v1",
        "embodied_calm_v1",
        "wonder_science_v1",
        "long_view_history_v1",
        "creativity_play_v1",
    )

    private fun seedSprint12LibraryManageContent(): String = runBlocking {
        val app = (InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as QualityAlternativeApplication)
        app.appContainer.userLinkRepository.observeReady().first { it }
        val result = app.appContainer.userLinkRepository.addLink(
            draft = UserLinkDraft(
                url = "https://example.com/sprint-12-manage",
                title = "Saved Sprint 12 link",
                description = "A saved link for visual deletion coverage.",
                durationMinutes = 8,
                topicTags = setOf(TopicTag.SCIENCE, TopicTag.ESSAYS),
            ),
            nowMillis = 12_000L,
        )
        assertTrue("Expected Sprint 12 visual link to be saved", result is AddUserLinkResult.Added)
        app.appContainer.settingsRepository.saveOnboardingSelection(
            OnboardingSelection(
                selectedAppPackages = setOf(FixtureTargetRegistry.fixtureDistractors.first().packageName),
                preferredTopics = setOf(TopicTag.SCIENCE, TopicTag.ESSAYS),
                preferredDurationBucket = DurationBucket.FOCUS,
                selectedPackIds = setOf("attention-classics-v1"),
            ),
        )
        (result as AddUserLinkResult.Added).item.id
    }

    private fun seedSupportedAppSelection() = runBlocking {
        val repository = (InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as QualityAlternativeApplication)
            .appContainer
            .settingsRepository
        repository.saveOnboardingSelection(
            OnboardingSelection(
                selectedAppPackages = setOf(
                    "com.instagram.android",
                    "com.twitter.android",
                    "com.reddit.frontpage",
                ),
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

    private fun assertSupportedAppRowsShowCheckedAndUncheckedState() {
        composeRule.onNodeWithTag("settings-app-com.instagram.android")
            .assertIsSelected()
        composeRule.onNodeWithTag("settings-app-com.google.android.youtube")
            .assertIsNotSelected()
    }

    private fun startMeditationFromIntervention() {
        if (hasTag("intervention-meditation-start")) {
            composeRule.onNodeWithTag("intervention-meditation-start")
                .assertIsDisplayed()
                .performClick()
            return
        }

        if (hasNodeContaining("Start timer")) {
            composeRule.onNodeWithText("Start timer", substring = true).performClick()
            return
        }

        var meditationBackupIndex = -1
        scenario?.onActivity { activity ->
            meditationBackupIndex = activity.mainViewModel.uiState.currentRecommendationSet
                ?.backups
                ?.indexOfFirst { item -> item.id == MEDITATION_TIMER_CONTENT_ID }
                ?: -1
        }
        assertTrue("Expected meditation to be available as a primary or backup option", meditationBackupIndex >= 0)
        val backupTag = "intervention-backup-action-$meditationBackupIndex"
        composeRule.onNodeWithTag("intervention-backup-list")
            .performScrollToNode(hasTestTag(backupTag))
        composeRule.onNodeWithTag(backupTag)
            .assertIsDisplayed()
            .performClick()
    }

    private fun advanceReaderToText(text: String, maxPages: Int = 12) {
        repeat(maxPages) {
            if (hasNode(text)) {
                return
            }
            advanceReaderPage()
        }
    }

    private fun advanceReaderToTextContaining(text: String, maxPages: Int = 12) {
        repeat(maxPages) {
            if (hasNodeContaining(text)) {
                return
            }
            advanceReaderPage()
        }
    }

    private fun advanceReaderToLastPage(maxPages: Int = 20) {
        repeat(maxPages) {
            // The reader progress label can cap below 100% on the final page (progress is anchored to
            // the end of the visible page), so detect the last page from the "current/total" page
            // label instead of waiting for a "100%" that may never appear. Also stop if the reader
            // viewport is gone, to avoid tapping into a missing node / past the end into feedback.
            if (hasNodeContaining("100%") || readerIsOnLastPage()) {
                return
            }
            if (!hasTag("reader-page-viewport")) {
                return
            }
            advanceReaderPage()
        }
    }

    private fun readerIsOnLastPage(): Boolean {
        if (!hasTag("reader-page-label")) {
            return false
        }
        val label = composeRule.onNodeWithTag("reader-page-label")
            .fetchSemanticsNode()
            .config
            .getOrNull(SemanticsProperties.Text)
            ?.joinToString(separator = "") { it.text }
            ?: return false
        val match = Regex("(\\d+)\\s*/\\s*(\\d+)").find(label) ?: return false
        return match.groupValues[1] == match.groupValues[2]
    }

    private fun advanceReaderPage() {
        composeRule.onNodeWithTag("reader-page-viewport")
            .assertIsDisplayed()
            .performSemanticsAction(SemanticsActions.OnClick)
        composeRule.waitForIdle()
    }

    private fun finishReaderFromCurrentPage(maxTaps: Int = 20) {
        repeat(maxTaps) {
            if (hasTag("feedback-screen")) {
                return
            }
            advanceReaderPage()
            composeRule.waitForIdle()
        }
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("feedback-screen") }
    }

    private fun seedMeditationSelection() = runBlocking {
        val appContainer = (InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as QualityAlternativeApplication)
            .appContainer
        val repository = appContainer.settingsRepository
        appContainer.readingProgressRepository.readingProgress()
            .map { progress -> progress.contentId }
            .forEach { contentId -> appContainer.readingProgressRepository.deleteProgress(contentId) }
        repository.saveOnboardingSelection(
            OnboardingSelection(
                selectedAppPackages = setOf(FixtureTargetRegistry.fixtureDistractors.first().packageName),
                preferredTopics = setOf(TopicTag.PSYCHOLOGY, TopicTag.PHILOSOPHY, TopicTag.ESSAYS),
                preferredDurationBucket = DurationBucket.QUICK,
                selectedPackIds = setOf("meditation-only-test-pack"),
            ),
        )
        repository.savePriorityContentIds(emptySet())
        repository.saveContentPriority(ContentPriority.MEDITATION)
    }

    private fun seedUserEpubSelection(
        title: String = "The Long Quiet EPUB",
        fileName: String = "long-quiet.epub",
        nowMillis: Long = 1_000L,
        epubBytes: ByteArray = sprint10EpubBytes(),
    ) = runBlocking {
        val targetContext = InstrumentationRegistry.getInstrumentation().targetContext
        val app = targetContext.applicationContext as QualityAlternativeApplication
        val fixture = File(targetContext.filesDir, "visual-qa-fixtures/$fileName")
        fixture.parentFile?.mkdirs()
        fixture.writeBytes(epubBytes)

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
        (result as AddUserDocumentResult.Added).item
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

    private fun seedMarkdownMediaTableSelection(
        title: String = "Markdown Media Table Notes",
        fileName: String = "markdown-media-table-notes.md",
        nowMillis: Long = 2_500L,
    ): ContentItem = runBlocking {
        val targetContext = InstrumentationRegistry.getInstrumentation().targetContext
        val app = targetContext.applicationContext as QualityAlternativeApplication
        val fixture = File(targetContext.filesDir, "visual-qa-fixtures/$fileName")
        fixture.parentFile?.mkdirs()
        val imageFile = File(fixture.parentFile, "calm-blue.png")
        val bitmap = Bitmap.createBitmap(80, 80, Bitmap.Config.ARGB_8888)
        bitmap.eraseColor(Color.rgb(70, 126, 178))
        imageFile.outputStream().use { output ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)
        }
        bitmap.recycle()
        fixture.writeText(
            """
            ![Calm blue square](calm-blue.png "Calm blue square")

            | Signal | Response | Time |
            |:-------|:--------:|-----:|
            | Read   | Reader   | 20 min |
            | Breathe | Pause   | 5 min |
            """.trimIndent(),
        )

        app.appContainer.userDocumentRepository.observeReady().first { it }
        val result = app.appContainer.userDocumentRepository.addDocument(
            draft = UserDocumentDraft(
                uri = Uri.fromFile(fixture).toString(),
                displayName = fileName,
                mimeType = "text/markdown",
                title = title,
                durationMinutes = 6,
                topicTags = setOf(TopicTag.ESSAYS, TopicTag.PSYCHOLOGY),
            ),
            nowMillis = nowMillis,
        )
        assertTrue("Expected Markdown media/table fixture to be saved", result is AddUserDocumentResult.Added)
        app.appContainer.settingsRepository.saveOnboardingSelection(
            OnboardingSelection(
                selectedAppPackages = setOf(FixtureTargetRegistry.fixtureDistractors.first().packageName),
                preferredTopics = setOf(TopicTag.ESSAYS, TopicTag.PSYCHOLOGY),
                preferredDurationBucket = DurationBucket.FOCUS,
                selectedPackIds = emptySet(),
            ),
        )
        (result as AddUserDocumentResult.Added).item
    }

    private fun seedWideMarkdownTableSelection(
        title: String = "Wide Markdown Table Notes",
        fileName: String = "wide-markdown-table-notes.md",
        nowMillis: Long = 2_700L,
    ): ContentItem = runBlocking {
        val targetContext = InstrumentationRegistry.getInstrumentation().targetContext
        val app = targetContext.applicationContext as QualityAlternativeApplication
        val fixture = File(targetContext.filesDir, "visual-qa-fixtures/$fileName")
        fixture.parentFile?.mkdirs()
        fixture.writeText(
            """
            | First | Second | Third | Fourth | Fifth | Sixth |
            |:------|:------:|:------|:-------|:------|------:|
            | Start | Middle | Table | Scroll target | Hidden before drag | 60 min |
            | Read  | Breathe | Note | Keep page | Reveal by swiping | 5 min |
            """.trimIndent(),
        )

        app.appContainer.userDocumentRepository.observeReady().first { it }
        val result = app.appContainer.userDocumentRepository.addDocument(
            draft = UserDocumentDraft(
                uri = Uri.fromFile(fixture).toString(),
                displayName = fileName,
                mimeType = "text/markdown",
                title = title,
                durationMinutes = 6,
                topicTags = setOf(TopicTag.ESSAYS, TopicTag.PSYCHOLOGY),
            ),
            nowMillis = nowMillis,
        )
        assertTrue("Expected wide Markdown table fixture to be saved", result is AddUserDocumentResult.Added)
        app.appContainer.settingsRepository.saveOnboardingSelection(
            OnboardingSelection(
                selectedAppPackages = setOf(FixtureTargetRegistry.fixtureDistractors.first().packageName),
                preferredTopics = setOf(TopicTag.ESSAYS, TopicTag.PSYCHOLOGY),
                preferredDurationBucket = DurationBucket.FOCUS,
                selectedPackIds = emptySet(),
            ),
        )
        (result as AddUserDocumentResult.Added).item
    }

    private fun seedPagedMarkdownNavigationSelection(
        title: String = "Paged Markdown Navigation Notes",
        fileName: String = "paged-markdown-navigation-notes.md",
        nowMillis: Long = 2_800L,
    ): ContentItem = runBlocking {
        val targetContext = InstrumentationRegistry.getInstrumentation().targetContext
        val app = targetContext.applicationContext as QualityAlternativeApplication
        val fixture = File(targetContext.filesDir, "visual-qa-fixtures/$fileName")
        fixture.parentFile?.mkdirs()
        fixture.writeText(
            (1..24).joinToString(separator = "\n\n") { index ->
                val paragraphId = index.toString().padStart(2, '0')
                "Navigation paragraph $paragraphId keeps normal text gestures active for Sprint 25. " +
                    "This ordinary reader paragraph has enough words to make the Markdown document span " +
                    "multiple pages without relying on images or tables. ".repeat(3)
            },
        )

        app.appContainer.userDocumentRepository.observeReady().first { it }
        val result = app.appContainer.userDocumentRepository.addDocument(
            draft = UserDocumentDraft(
                uri = Uri.fromFile(fixture).toString(),
                displayName = fileName,
                mimeType = "text/markdown",
                title = title,
                durationMinutes = 12,
                topicTags = setOf(TopicTag.ESSAYS, TopicTag.PSYCHOLOGY),
            ),
            nowMillis = nowMillis,
        )
        assertTrue("Expected paged Markdown navigation fixture to be saved", result is AddUserDocumentResult.Added)
        app.appContainer.settingsRepository.saveOnboardingSelection(
            OnboardingSelection(
                selectedAppPackages = setOf(FixtureTargetRegistry.fixtureDistractors.first().packageName),
                preferredTopics = setOf(TopicTag.ESSAYS, TopicTag.PSYCHOLOGY),
                preferredDurationBucket = DurationBucket.FOCUS,
                selectedPackIds = emptySet(),
            ),
        )
        (result as AddUserDocumentResult.Added).item
    }

    private fun seedSprint12ContinueReadingDocument(
        fileName: String = "sprint12-continue.epub",
        title: String = "Continue Reading Fixture",
        nowMillis: Long = 1_900L,
    ): ContentItem = runBlocking {
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
                durationMinutes = 12,
                topicTags = setOf(TopicTag.ESSAYS, TopicTag.PSYCHOLOGY),
            ),
            nowMillis = nowMillis,
        )
        assertTrue("Expected Sprint 12 continue fixture to be saved", result is AddUserDocumentResult.Added)
        val item = (result as AddUserDocumentResult.Added).item
        app.appContainer.settingsRepository.saveOnboardingSelection(
            OnboardingSelection(
                selectedAppPackages = setOf(FixtureTargetRegistry.fixtureDistractors.first().packageName),
                preferredTopics = setOf(TopicTag.ESSAYS, TopicTag.PSYCHOLOGY),
                preferredDurationBucket = DurationBucket.FOCUS,
                selectedPackIds = emptySet(),
            ),
        )
        item
    }

    private fun seedSprint22LegacyLongContinueDocument(): ContentItem = runBlocking {
        val targetContext = InstrumentationRegistry.getInstrumentation().targetContext
        val app = targetContext.applicationContext as QualityAlternativeApplication
        val fixture = File(targetContext.filesDir, "visual-qa-fixtures/sprint22-long-legacy.md")
        fixture.parentFile?.mkdirs()
        val longBody = List(30_000) { index -> "word${index % 97}" }.joinToString(" ")
        fixture.writeText(
            """
            # Long imported book

            $longBody
            """.trimIndent(),
        )

        app.appContainer.userDocumentRepository.observeReady().first { it }
        val result = app.appContainer.userDocumentRepository.addDocument(
            draft = UserDocumentDraft(
                uri = Uri.fromFile(fixture).toString(),
                displayName = "sprint22-long-legacy.md",
                mimeType = "text/markdown",
                title = "Long imported book",
                durationMinutes = 20,
                topicTags = setOf(TopicTag.ESSAYS, TopicTag.PSYCHOLOGY),
            ),
            nowMillis = 22_000L,
        )
        assertTrue("Expected Sprint 22 long fixture to be saved", result is AddUserDocumentResult.Added)
        val item = (result as AddUserDocumentResult.Added).item
        app.appContainer.readingProgressRepository.saveProgress(
            ReadingProgress(
                contentId = item.id,
                progressPercent = 41,
                lastVisibleParagraphIndex = 1,
                lastVisibleTextOffset = 0,
                paragraphCount = 2,
                updatedAtMillis = 22_100L,
                completedAtMillis = null,
            ),
        )
        app.appContainer.settingsRepository.saveOnboardingSelection(
            OnboardingSelection(
                selectedAppPackages = setOf(FixtureTargetRegistry.fixtureDistractors.first().packageName),
                preferredTopics = setOf(TopicTag.ESSAYS, TopicTag.PSYCHOLOGY),
                preferredDurationBucket = DurationBucket.FOCUS,
                selectedPackIds = emptySet(),
            ),
        )
        item
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

    private fun saveDarkTheme() = runBlocking {
        val repository = (InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as QualityAlternativeApplication)
            .appContainer
            .settingsRepository
        repository.saveThemeMode(AppThemeMode.DARK)
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
                  <p>Long single paragraph starts with one deliberately oversized paragraph that should never be clipped by a fixed no-scroll page. It keeps adding plain reader prose so the pagination code has to split a single source block into more than one reachable page. The middle of this paragraph describes a user reading a private EPUB with no feed, no toolbar, and no vertical scrolling, while the words continue past the height that previously caused risk. The sentence keeps going with enough ordinary language to require another page turn, proving that the reader can preserve all text even when a source file uses very long paragraphs instead of short blocks. Long single paragraph ends after the continuation has been shown on a later reader page.</p>
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

    private fun sprint20LargeOpeningEpubBytes(): ByteArray {
        val paragraphs = (1..15_000).joinToString("\n") { index ->
            "<p>Large EPUB paragraph $index keeps the reader-opening overlay visible while the private book is parsed off the interface path.</p>"
        }
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
                    <item id="chapter" href="chapter.xhtml" media-type="application/xhtml+xml"/>
                  </manifest>
                  <spine>
                    <itemref idref="chapter"/>
                  </spine>
                </package>
            """.trimIndent(),
            "OPS/chapter.xhtml" to "<html><body><h1>Large Opening Fixture</h1>$paragraphs</body></html>",
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
        assertTrue(
            "Reader should keep the content surface minimal instead of showing author labels as extra chrome",
            authors.none(::hasNodeContaining),
        )
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
        assertTrue(
            "Reader should keep public-domain v2 pages minimal instead of showing author labels as extra chrome",
            authors.none(::hasNodeContaining),
        )
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
        assertTrue(
            "Reader should keep philosophy pages minimal instead of showing author labels as extra chrome",
            authors.none(::hasNodeContaining),
        )
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
        assertTrue(
            "Reader should keep science pages minimal instead of showing author labels as extra chrome",
            authors.none(::hasNodeContaining),
        )
        assertTrue("Reader should not show the old editorial placeholder label", !hasNodeContaining("Quality Alternative Editorial"))
        assertTrue("Reader should not show provenance-heavy Project Gutenberg label", !hasNodeContaining("Project Gutenberg"))
    }

    private fun assertLinkOnlyHandoffCopyIsDisplayed() {
        val sourceHints = listOf("longnow.org", "psyche.co", "aeon.co", "quantamagazine.org", "sapiens.org", "plato.stanford.edu", "iep.utm.edu", "nautil.us")

        assertTrue("Expected external reading label", hasNodeContaining("External reading"))
        assertTrue("Expected external handoff copy", hasNode("Opens in your browser"))
        assertTrue("Expected a canonical external URL", sourceHints.any(::hasNodeContaining))
    }

    private fun assertSprint9RenderableReaderCopyIsDisplayed() {
        composeRule.onNodeWithText("Five Minutes of Nonsense").assertIsDisplayed()
        assertTrue(
            "Reader should keep the Sprint 9 renderable page minimal instead of showing author labels as extra chrome",
            !hasNodeContaining("Edward Lear"),
        )
        assertTrue("Expected Sprint 9 reader body text", hasNodeContaining("There was an Old Derry down Derry"))
        assertTrue("Reader should not show source boilerplate", !hasNodeContaining("Project Gutenberg"))
        assertTrue("Reader should not show producer boilerplate", !hasNodeContaining("Produced by"))
    }

    private fun assertSprint9DarwinReaderCopyIsDisplayed() {
        composeRule.onNodeWithText("Plants That Hunt").assertIsDisplayed()
        assertTrue("Darwin reader should keep author labels out of minimal reader chrome", !hasNodeContaining("Charles Darwin"))
        assertTrue("Expected Darwin body text", hasNodeContaining("During the summer of 1860"))
        assertTrue("Darwin reader should not show bibliography footnote", !hasNodeContaining("bibliography of Drosera"))
        assertTrue("Darwin reader should not show figure-list text", !hasNodeContaining("FIG. 1"))
    }

    private fun assertSprint9FiguierReaderCopyIsDisplayed() {
        composeRule.onNodeWithText("The Sea as a World").assertIsDisplayed()
        assertTrue("Figuier reader should keep author labels out of minimal reader chrome", !hasNodeContaining("Louis Figuier"))
        assertTrue("Expected Figuier body text", hasNodeContaining("living wonders of the deep"))
        assertTrue("Figuier reader should not show book-purpose framing", !hasNodeContaining("It is proposed in"))
        assertTrue("Figuier reader should not show title-page framing", !hasNodeContaining("Title-page"))
    }

    private fun assertSprint9FabreFlyReaderCopyIsDisplayed() {
        composeRule.onNodeWithText("The Fly Under Attention").assertIsDisplayed()
        assertTrue("Fabre reader should keep author labels out of minimal reader chrome", !hasNodeContaining("Jean-Henri Fabre"))
        assertTrue("Expected fly-specific body text", hasNodeContaining("flies that glitter"))
        assertTrue("Fabre fly reader should not show laboratory mismatch", !hasNodeContaining("long-wished-for laboratory"))
        assertTrue("Fabre fly reader should not show harmas mismatch", !hasNodeContaining("hoc erat in votis"))
    }

    private fun assertSprint9LinkOnlyHandoffCopyIsDisplayed() {
        assertTrue("Expected external reading label", hasNodeContaining("External reading"))
        assertTrue("Expected external handoff copy", hasNode("Opens in your browser"))
        assertTrue("Expected Sprint 9 canonical external URL", hasNodeContaining("quantamagazine.org"))
        assertTrue("Link-only handoff should not expose a reader body", !hasNodeContaining("There was an Old Derry down Derry"))
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

    private fun waitForTransientMessageToClear(text: String) {
        composeRule.waitUntil(timeoutMillis = 15_000) { !hasNode(text) }
    }

    private fun hasNodeContaining(text: String): Boolean = runCatching {
        composeRule.onNodeWithText(text, substring = true, ignoreCase = true).fetchSemanticsNode()
        true
    }.getOrDefault(false)

    private fun hasAnyNode(text: String): Boolean = runCatching {
        composeRule.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty()
    }.getOrDefault(false)

    private fun hasTag(tag: String): Boolean = runCatching {
        composeRule.onNodeWithTag(tag).fetchSemanticsNode()
        true
    }.getOrDefault(false)

    private fun currentContentIdAndTitle(): Pair<String, String> {
        var id = ""
        var title = ""
        scenario?.onActivity { activity ->
            val content = activity.mainViewModel.uiState.currentContent
            id = content?.id.orEmpty()
            title = content?.title.orEmpty()
        }
        assertTrue("Expected current content id", id.isNotBlank())
        assertTrue("Expected current content title", title.isNotBlank())
        return id to title
    }

    private fun readerTocEntryIndex(title: String): Int {
        var entryIndex = -1
        scenario?.onActivity { activity ->
            entryIndex = activity.mainViewModel.uiState.currentReaderDocument
                ?.tableOfContents
                ?.indexOfFirst { entry -> entry.title == title }
                ?: -1
        }
        assertTrue("Expected TOC entry for $title", entryIndex >= 0)
        return entryIndex
    }

    private fun assertNodeFullyWithinRoot(tag: String) {
        composeRule.waitForIdle()
        val rootBounds = composeRule.onRoot().fetchSemanticsNode().boundsInRoot
        val nodeBounds = composeRule.onNodeWithTag(tag).fetchSemanticsNode().boundsInRoot
        val tolerance = 1f
        assertTrue("$tag left is clipped: node=$nodeBounds root=$rootBounds", nodeBounds.left >= rootBounds.left - tolerance)
        assertTrue("$tag top is clipped: node=$nodeBounds root=$rootBounds", nodeBounds.top >= rootBounds.top - tolerance)
        assertTrue("$tag right is clipped: node=$nodeBounds root=$rootBounds", nodeBounds.right <= rootBounds.right + tolerance)
        assertTrue("$tag bottom is clipped: node=$nodeBounds root=$rootBounds", nodeBounds.bottom <= rootBounds.bottom + tolerance)
    }

    private fun assertFiniteChoicesAboveBottomActions() {
        assertNodeFullyWithinRoot("intervention-bottom-actions")
        if (hasTag("intervention-backup-list")) {
            assertNodeAboveBottomActions("intervention-backup-list")
        }
        if (hasTag("intervention-backup-action-0")) {
            assertNodeAboveBottomActions("intervention-backup-action-0")
        }
        if (!hasTag("intervention-backup-action-0")) {
            composeRule.onNodeWithTag("intervention-empty-backups").assertIsDisplayed()
            assertNodeAboveBottomActions("intervention-empty-backups")
        }
    }

    private fun assertNodeAboveBottomActions(tag: String) {
        composeRule.waitForIdle()
        val nodeBounds = composeRule.onNodeWithTag(tag).fetchSemanticsNode().boundsInRoot
        val bottomActionsBounds = composeRule.onNodeWithTag("intervention-bottom-actions").fetchSemanticsNode().boundsInRoot
        val tolerance = 1f
        assertTrue(
            "$tag is obscured by bottom actions: node=$nodeBounds actions=$bottomActionsBounds",
            nodeBounds.bottom <= bottomActionsBounds.top + tolerance,
        )
    }
}
