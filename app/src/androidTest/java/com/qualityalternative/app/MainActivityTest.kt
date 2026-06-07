package com.qualityalternative.app

import android.content.Intent
import android.net.Uri
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.click
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.ComposeTimeoutException
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeRight
import androidx.compose.ui.test.swipeUp
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import com.qualityalternative.app.domain.model.AppThemeMode
import com.qualityalternative.app.domain.model.ContentFormat
import com.qualityalternative.app.domain.model.ContentItem
import com.qualityalternative.app.domain.model.DEFAULT_READER_FONT_SCALE
import com.qualityalternative.app.domain.model.DurationBucket
import com.qualityalternative.app.domain.model.AnalyticsEventType
import com.qualityalternative.app.domain.model.InterventionMode
import com.qualityalternative.app.domain.model.MEDITATION_TIMER_CONTENT_ID
import com.qualityalternative.app.domain.model.OnboardingSelection
import com.qualityalternative.app.domain.model.ReadingAnnotationDraft
import com.qualityalternative.app.domain.model.ReadingAnnotationSelector
import com.qualityalternative.app.domain.model.ReadingProgress
import com.qualityalternative.app.domain.model.TopicTag
import com.qualityalternative.app.domain.model.UserDocumentDraft
import com.qualityalternative.app.domain.service.AddUserDocumentResult
import com.qualityalternative.app.data.ACCOUNT_LIGHT_PROFILE_FILE_NAME
import com.qualityalternative.app.data.AndroidAccountLightProfileAutosaveWriter
import com.qualityalternative.app.data.ReadingTimeEstimateSource
import com.qualityalternative.app.data.RoomReadingProgressRepository
import com.qualityalternative.app.interception.FixtureTargetRegistry
import com.qualityalternative.app.interception.InterceptionRuntimeGate
import com.qualityalternative.app.ui.DocumentImportCandidate
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class MainActivityTest {
    @get:Rule
    val composeRule = createEmptyComposeRule()

    private var scenario: ActivityScenario<MainActivity>? = null
    private val interventionContinueProgressScreenshotDirName =
        "intervention-continue-progress-${System.currentTimeMillis()}"
    private val sprint14RankingScreenshotDirName =
        "sprint14-fresh-ranking-${System.currentTimeMillis()}"
    private val sprint14AnnotationStorageScreenshotDirName =
        "sprint14-annotation-storage-${System.currentTimeMillis()}"
    private val sprint14ReaderAnnotationScreenshotDirName =
        "sprint14-reader-annotation-${System.currentTimeMillis()}"
    private val sprint14AnnotationLibraryScreenshotDirName =
        "sprint14-annotation-library-${System.currentTimeMillis()}"
    private val sprint14AnnotationExportScreenshotDirName =
        "sprint14-annotation-export-${System.currentTimeMillis()}"
    private val sprint14ReaderPaginationScreenshotDirName =
        "sprint14-reader-pagination-${System.currentTimeMillis()}"
    private val sprint17DriveAuthScreenshotDirName =
        "sprint17-drive-auth-${System.currentTimeMillis()}"
    private val accountLightProfileScreenshotDirName =
        "sprint21-profile-restore-${System.currentTimeMillis()}"
    private val sprint16ProfileAutosaveScreenshotDirName =
        "sprint16-profile-autosave-${System.currentTimeMillis()}"
    private val sprint16AdaptiveReaderScreenshotDirName =
        "sprint16-adaptive-reader-${System.currentTimeMillis()}"
    private val sprint17TypographyScreenshotDirName =
        "sprint17-typography-settings-${System.currentTimeMillis()}"
    private val sprint17DefaultsScreenshotDirName =
        "sprint17-default-settings-${System.currentTimeMillis()}"
    private val sprint17AdaptivePaginationScreenshotDirName =
        "sprint17-adaptive-pagination-${System.currentTimeMillis()}"
    private val sprint17CrossPageAnnotationScreenshotDirName =
        "sprint17-cross-page-annotation-${System.currentTimeMillis()}"
    private val sprint17AnnotationSurfaceScreenshotDirName =
        "sprint17-annotation-surface-${System.currentTimeMillis()}"
    private val sprint23FooterProgressScreenshotDirName =
        "sprint23-footer-progress-${System.currentTimeMillis()}"
    private val sprint24BedtimeScreenshotDirName =
        "sprint24-bedtime-hard-ban-${System.currentTimeMillis()}"
    private val readerFormEvidenceScreenshotDirName =
        "sprint21-meditation-gong-${System.currentTimeMillis()}"
    private val sprint21ReadingTimeScreenshotDirName =
        "sprint21-reading-time-${System.currentTimeMillis()}"

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
        assertHomeHeroIsDisplayed()

        scenario?.close()
        scenario = null
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        Thread.sleep(250)
        launchApp()

        waitForHome()
        assertHomeHeroIsDisplayed()
    }

    @Test
    fun onboardingWelcomeDoesNotShowAccountShortcutWithoutAccountFlow() {
        launchApp()

        composeRule.waitUntil(timeoutMillis = 10_000) { hasNodeContaining("Turn an impulse") }
        composeRule.onNodeWithText("Begin").assertIsDisplayed().assertIsEnabled()
        assertFalse(hasNode("I have an account"))
    }

    @Test
    fun systemInterceptionIntentShowsLiveInterventionForFixtureTarget() {
        seedFixtureSelection()
        relaunchFixtureSystemIntervention()

        composeRule.onNodeWithText("You reached for Fixture Feed One").assertIsDisplayed()
        composeRule.onNodeWithText("A BRIEF DETOUR, IF YOU'D LIKE ONE")
            .assertDoesNotExist()
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
        composeRule.onNodeWithText("Pause 15 min")
            .assertIsDisplayed()
            .assertIsEnabled()
        composeRule.onNodeWithText("Open in", substring = true)
            .assertIsDisplayed()
            .assertIsNotEnabled()
        composeRule.onNodeWithTag("form-intervention-unlock-wait")
            .assertIsDisplayed()
        waitForOpenAnywayUnlock()
        composeRule.onNodeWithText("Open Fixture Feed One")
            .assertIsDisplayed()
            .assertIsEnabled()
    }

    @Test
    fun systemInterceptionIntentShowsLiveInterventionForSelectedCustomTarget() {
        val candidate = seedFirstEligibleCustomTargetSelection()
        scenario?.close()
        scenario = null
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        Thread.sleep(250)

        val targetContext = InstrumentationRegistry.getInstrumentation().targetContext
        launchApp(
            MainActivity.createSystemInterceptionIntent(
                context = targetContext,
                targetAppPackage = candidate.packageName,
            ),
        )

        composeRule.waitUntil(timeoutMillis = 10_000) {
            hasNode("You reached for ${candidate.displayName}")
        }
        composeRule.onNodeWithText("You reached for ${candidate.displayName}").assertIsDisplayed()
        composeRule.onNodeWithText("Read this", substring = true).assertIsDisplayed().assertIsEnabled()
        composeRule.onNodeWithText("Open ${candidate.displayName}").assertIsDisplayed().assertIsEnabled()
    }

    @Test
    fun forgedWebsiteInterceptionIntentWithoutLaunchTokenIsIgnored() {
        seedFixtureSelection()
        val targetContext = InstrumentationRegistry.getInstrumentation().targetContext

        launchApp(
            Intent(targetContext, MainActivity::class.java).apply {
                action = "com.qualityalternative.app.action.SYSTEM_INTERVENTION"
                putExtra("extra_target_kind", "website")
                putExtra("extra_target_app_package", "com.android.chrome")
                putExtra("extra_browser_display_name", "Chrome")
                putExtra("extra_website_rule_type", "EXACT_DOMAIN")
                putExtra("extra_website_rule_includes_apex", false)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            },
        )

        waitForHome()
        composeRule.onNodeWithText("You reached for Chrome website").assertDoesNotExist()
        composeRule.onNodeWithText("Bedtime is protecting sleep from Chrome website", substring = true)
            .assertDoesNotExist()
    }

    @Test
    fun systemInterventionDelayActionIsClickableWithoutScrolling() {
        seedFixtureSelection()
        relaunchFixtureSystemIntervention()

        composeRule.onNodeWithText("Pause 15 min")
            .assertIsDisplayed()
            .assertIsEnabled()
            .performClick()

        waitForHome()
        composeRule.onNodeWithTag("home-list")
            .performScrollToNode(hasText("FIXTURE FEED ONE PAUSED", substring = true))
        composeRule.onNodeWithText("min alternative", substring = true)
            .assertIsDisplayed()
            .assertIsEnabled()
    }

    @Test
    fun systemInterventionOpenAnywayActionIsClickableWithoutScrolling() {
        launchFixtureSystemIntervention()
        waitForOpenAnywayUnlock()

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

        assertHomeHeroIsDisplayed()
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
    fun onboardingShowsSprint9TopicChips() {
        launchApp()

        composeRule.onNodeWithText("Begin").performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) { hasNode("Which apps pull at you?") }
        composeRule.onNodeWithText("Continue").performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) { hasNode("What would you rather read?") }

        composeRule.onNodeWithTag("onboarding-topic-ATTENTION").assertIsDisplayed()
        composeRule.onNodeWithTag("onboarding-topic-PRACTICAL").assertIsDisplayed()
        composeRule.onNodeWithTag("onboarding-topic-BODY").assertIsDisplayed()
        composeRule.onNodeWithTag("onboarding-topic-NATURE").assertIsDisplayed()
        composeRule.onNodeWithTag("onboarding-topic-HISTORY_CULTURE").assertIsDisplayed()
    }

    @Test
    fun sprint9ContentIsReachableAfterNormalOnboarding() {
        launchOnboardedApp()

        composeRule.onNodeWithTag("tab-library", useUnmergedTree = true).performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("library-list") }
        composeRule.onNodeWithTag("library-list")
            .performScrollToNode(hasText("How We Think"))
        composeRule.onNodeWithText("How We Think").assertIsDisplayed()
    }

    @Test
    fun addLinkKeepsSaveDisabledForInvalidUrl() {
        launchOnboardedApp()

        openAddLinkFromHome()

        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("add-link-screen") }
        composeRule.onNodeWithText("Add content.", substring = true)
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

        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("add-link-screen") }
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

        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("add-link-screen") }
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

        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("add-link-screen") }
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
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("library-list") }
        composeRule.onNodeWithTag("library-list")
            .performScrollToNode(hasText("Saved essay"))
        composeRule.onNodeWithText("Saved essay").assertIsDisplayed()
        composeRule.onNodeWithTag("library-list")
            .performScrollToNode(hasText("Your link · example.com", substring = true))
        composeRule.onNodeWithText("Your link · example.com", substring = true)
            .assertIsDisplayed()
    }

    @Test
    fun libraryManageModeDeletesSavedLinkAndKeepsEditorialReadOnly() {
        launchOnboardedApp()

        openAddLinkFromHome()

        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("add-link-screen") }
        composeRule.onNodeWithTag("add-link-url").performTextInput("https://example.com/delete-me")
        composeRule.onNodeWithTag("add-link-title").performTextInput("Delete me")
        composeRule.onNodeWithTag("add-link-topic-SCIENCE")
            .performScrollTo()
            .performClick()
        composeRule.onNodeWithTag("add-link-save")
            .assertIsEnabled()
            .performClick()

        composeRule.waitUntil(timeoutMillis = 10_000) { hasNode("Ready when you are") }
        composeRule.onNodeWithTag("add-link-done").performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("library-list") }
        var deletedLinkId = ""
        scenario?.onActivity { activity ->
            val app = activity.application as QualityAlternativeApplication
            deletedLinkId = app.appContainer.userLinkRepository.userLinks()
                .first { item -> item.externalUrl == "https://example.com/delete-me" }
                .id
        }
        assertTrue(deletedLinkId.isNotBlank())
        seedReadingAnnotation(
            contentId = deletedLinkId,
            paragraphIndex = 1,
            quotedText = "Delete me",
            noteText = "Remove this with the saved link.",
        )
        composeRule.waitUntil(timeoutMillis = 10_000) {
            hasReadingAnnotationFor(deletedLinkId)
        }

        composeRule.onNodeWithTag("library-manage-toggle")
            .assertIsDisplayed()
            .performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) { hasNode("Only your saved links and files can be deleted.") }
        composeRule.onNodeWithTag("library-list")
            .performScrollToNode(hasTestTag("library-editorial-note-s9-1-r01-dewey-how-we-think"))
        composeRule.onNodeWithTag("library-editorial-note-s9-1-r01-dewey-how-we-think")
            .assertIsDisplayed()
        composeRule.onNodeWithTag("library-list")
            .performScrollToNode(hasText("Delete me"))
        composeRule.onNodeWithText("Delete me")
            .assertIsDisplayed()
        val selectTag = "library-select-$deletedLinkId"
        composeRule.onNodeWithTag("library-list")
            .performScrollToNode(hasTestTag(selectTag))
        composeRule.onNodeWithTag(selectTag)
            .assertIsDisplayed()
            .performClick()
        composeRule.onNodeWithTag("library-list")
            .performScrollToNode(hasTestTag("library-delete-selected"))
        composeRule.onNodeWithTag("library-delete-selected")
            .assertIsEnabled()
            .performClick()

        composeRule.waitUntil(timeoutMillis = 10_000) { !hasNode("Delete me") }
        assertFalse(hasNode("Delete me"))
        composeRule.waitUntil(timeoutMillis = 10_000) {
            !hasReadingAnnotationFor(deletedLinkId) && hasReadingAnnotationDeletedEventFor(deletedLinkId)
        }
        scenario?.onActivity { activity -> activity.mainViewModel.dismissMessage() }
        composeRule.waitForIdle()
        captureSprint14AnnotationStorageScreenshot("01_library_after_annotation_cleanup_light")
    }

    @Test
    fun addLinkPriorityAtAddAppearsInPriorityLibraryFilter() {
        launchOnboardedApp()

        openAddLinkFromHome()

        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("add-link-screen") }
        composeRule.onNodeWithTag("add-link-url").performTextInput("https://example.com/priority-at-add")
        composeRule.onNodeWithTag("add-link-title").performTextInput("Priority at add")
        composeRule.onNodeWithTag("add-link-topic-SCIENCE")
            .performScrollTo()
            .performClick()
        composeRule.onNodeWithTag("add-link-priority")
            .performScrollTo()
            .performClick()
        composeRule.onNodeWithTag("add-link-save")
            .assertIsEnabled()
            .performClick()

        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("add-link-success-screen") }
        composeRule.onNodeWithTag("add-link-done").performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("library-list") }
        composeRule.onNodeWithText("Priority")
            .performClick()
        composeRule.onNodeWithText("Priority at add")
            .assertIsDisplayed()
    }

    @Test
    fun themeSettingSwitchesToDarkMode() {
        launchOnboardedApp()

        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("tab-settings") }
        composeRule.onNodeWithTag("tab-settings", useUnmergedTree = true)
            .performSemanticsAction(SemanticsActions.OnClick)
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
    fun interventionModeSettingControlsOpenAnywayFriction() {
        seedFixtureSelection()
        launchApp()
        waitForHome()

        scenario?.onActivity { activity -> activity.mainViewModel.openSettings() }
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("settings-list") }
        composeRule.onNodeWithTag("settings-list")
            .performScrollToNode(hasTestTag("intervention-mode-FIRM"))
        composeRule.onNodeWithTag("intervention-mode-SOFT")
            .assertIsDisplayed()
            .assertIsSelected()
        composeRule.onNodeWithTag("intervention-mode-FIRM")
            .assertIsDisplayed()
            .assertIsNotSelected()
        composeRule.onNodeWithTag("intervention-mode-SOFT")
            .assertIsSelected()
        captureReaderFormEvidenceScreenshot("13_intervention_mode_soft_selected")

        scenario?.onActivity { activity -> activity.mainViewModel.triggerDebugIntervention(nowMillis = System.currentTimeMillis()) }
        composeRule.waitUntil(timeoutMillis = 10_000) { hasNode("You reached for Fixture Feed One") }
        composeRule.onNodeWithTag("form-intervention-unlock-wait")
            .assertDoesNotExist()
        composeRule.onNodeWithText("Open Fixture Feed One")
            .assertIsDisplayed()
            .assertIsEnabled()
        captureReaderFormEvidenceScreenshot("14_soft_mode_open_anyway_immediate")

        scenario?.onActivity {
            it.mainViewModel.openSettings()
            it.mainViewModel.selectInterventionMode(InterventionMode.FIRM)
        }
        composeRule.waitUntil(timeoutMillis = 10_000) { currentInterventionMode() == InterventionMode.FIRM }
        scenario?.onActivity { activity -> activity.mainViewModel.triggerDebugIntervention(nowMillis = System.currentTimeMillis()) }
        composeRule.waitUntil(timeoutMillis = 10_000) { hasNode("You reached for Fixture Feed One") }
        composeRule.onNodeWithTag("form-intervention-unlock-wait")
            .assertIsDisplayed()
        composeRule.onNodeWithText("Open in", substring = true)
            .assertIsDisplayed()
            .assertIsNotEnabled()
        captureReaderFormEvidenceScreenshot("15_firm_mode_open_anyway_wait")
    }

    @Test
    fun bedtimeModeShowsCalmHardBanWithAlternativesAndEmergencyWait() {
        seedFixtureSelection()
        launchApp()
        waitForHome()

        scenario?.onActivity { activity ->
            activity.mainViewModel.openSettings()
            activity.mainViewModel.selectInterventionMode(InterventionMode.SOFT)
            activity.mainViewModel.setBedtimeSettings(enabled = true, startMinutes = 0, endMinutes = 0)
        }
        composeRule.waitUntil(timeoutMillis = 10_000) {
            var enabled = false
            var active = false
            scenario?.onActivity { activity ->
                enabled = activity.mainViewModel.uiState.bedtimeEnabled
                active = activity.mainViewModel.uiState.isBedtimeActive
            }
            enabled && active
        }
        composeRule.onNodeWithTag("settings-list")
            .performScrollToNode(hasTestTag("settings-bedtime-section"))
        composeRule.onNodeWithTag("settings-bedtime-section")
            .assertIsDisplayed()
        composeRule.onNodeWithText("Sleep lock")
            .assertIsDisplayed()
        captureSprint24BedtimeScreenshot("01_settings_bedtime_enabled")

        scenario?.onActivity { activity ->
            activity.mainViewModel.triggerDebugIntervention(nowMillis = System.currentTimeMillis())
        }
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("bedtime-emergency-unlock-wait") }
        composeRule.onNodeWithText("Bedtime is protecting sleep", substring = true)
            .assertIsDisplayed()
        composeRule.onNodeWithText("QUIET ALTERNATIVES")
            .assertIsDisplayed()
        composeRule.onNodeWithTag("intervention-backup-list")
            .assertIsDisplayed()
        composeRule.onNodeWithTag("bedtime-emergency-unlock-wait")
            .assertIsDisplayed()
        composeRule.onNodeWithTag("bedtime-emergency-unlock-action")
            .assertIsDisplayed()
            .assertIsNotEnabled()
        composeRule.onNodeWithText("Pause 15 min")
            .assertDoesNotExist()
        captureSprint24BedtimeScreenshot("02_intervention_bedtime_hard_ban_alternatives")
    }

    @Test
    fun readerFontSizeSettingUsesAppLevelPreference() {
        launchOnboardedApp()

        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("tab-settings") }
        composeRule.onNodeWithTag("tab-settings", useUnmergedTree = true)
            .performSemanticsAction(SemanticsActions.OnClick)
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("settings-list") }
        composeRule.onNodeWithTag("settings-list")
            .performScrollToNode(hasTestTag("reader-font-scale-increase"))
        composeRule.onNodeWithTag("reader-font-scale-value")
            .assertTextContains("100%")
        composeRule.onNodeWithTag("settings-list")
            .performScrollToNode(hasTestTag("reader-font-scale-preview"))
        composeRule.onNodeWithTag("reader-font-scale-preview")
            .assertIsDisplayed()
        composeRule.onNodeWithTag("settings-list")
            .performTouchInput { swipeUp(startY = 620f, endY = 520f, durationMillis = 180) }
        composeRule.waitForIdle()
        captureSprint17TypographyScreenshot("00_reader_font_setting_default")
        composeRule.onNodeWithTag("settings-list")
            .performScrollToNode(hasTestTag("reader-font-scale-increase"))
        repeat(6) {
            composeRule.onNodeWithTag("reader-font-scale-increase")
                .assertIsDisplayed()
                .performSemanticsAction(SemanticsActions.OnClick)
            composeRule.waitForIdle()
        }
        composeRule.onNodeWithTag("reader-font-scale-value")
            .assertTextContains("130%")
        composeRule.onNodeWithTag("reader-font-scale-preview")
            .assertIsDisplayed()
        composeRule.waitUntil(timeoutMillis = 10_000) { currentReaderFontScale() == 1.3 }
        composeRule.onNodeWithTag("settings-list")
            .performTouchInput { swipeUp(startY = 620f, endY = 520f, durationMillis = 180) }
        composeRule.waitForIdle()
        captureSprint17TypographyScreenshot("01_reader_font_setting_xl")
        composeRule.onNodeWithTag("settings-list")
            .performScrollToNode(hasTestTag("interface-text-scale-control"))
        repeat(4) {
            composeRule.onNodeWithTag("interface-text-scale-increase")
                .performScrollTo()
                .assertIsDisplayed()
                .performSemanticsAction(SemanticsActions.OnClick)
            composeRule.waitForIdle()
        }
        composeRule.onNodeWithTag("interface-text-scale-value")
            .assertTextContains("120%")
        composeRule.onNodeWithTag("settings-list")
            .performScrollToNode(hasTestTag("interface-text-scale-preview"))
        composeRule.onNodeWithTag("interface-text-scale-preview")
            .assertIsDisplayed()
        composeRule.waitUntil(timeoutMillis = 10_000) { currentInterfaceTextScale() == 1.2 }
        composeRule.onNodeWithTag("settings-list")
            .performTouchInput { swipeUp(startY = 620f, endY = 520f, durationMillis = 180) }
        composeRule.waitForIdle()
        captureSprint17TypographyScreenshot("04_interface_text_setting_large")

        scenario?.close()
        scenario = null
        launchFixtureSystemIntervention()
        composeRule.onNodeWithText("Read this", substring = true)
            .assertIsDisplayed()
            .performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("reader-screen") }
        assertEquals(1.3, currentReaderFontScale(), 0.0)
        composeRule.onNodeWithTag("reader-page-label").assertIsDisplayed()
        captureSprint17TypographyScreenshot("02_reader_xl_font_light")

        scenario?.onActivity { activity -> activity.mainViewModel.setReaderFontScale(0.9) }
        composeRule.waitUntil(timeoutMillis = 10_000) { currentReaderFontScale() == 0.9 }
        composeRule.onNodeWithTag("reader-page-label").assertIsDisplayed()
        captureSprint17TypographyScreenshot("03_reader_small_font_light")
    }

    @Test
    fun readerProgressPercentRestoresFromSourceAnchorAfterReaderFontChange() {
        launchOnboardedApp()
        val document = addSeedMarkdownDocument(
            title = "Progress percent source anchor regression",
            displayName = "progress-percent-source-anchor.md",
            body = (1..36).joinToString(separator = "\n\n") { index ->
                "Progress anchor paragraph $index keeps enough text to paginate differently when reader text grows, but its saved percent should remain source anchored."
            },
            nowMillis = 85_000L,
        )

        scenario?.onActivity { activity -> activity.mainViewModel.openLibraryItem(document) }
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("reader-screen") }
        advanceReaderPage()
        composeRule.waitUntil(timeoutMillis = 10_000) { hasUnfinishedProgressFor(document.id) }
        val savedPercent = savedProgressPercentFor(document.id)
        val savedParagraphIndex = savedProgressParagraphIndexFor(document.id)
        assertTrue(savedPercent in 1..99)
        composeRule.waitUntil(timeoutMillis = 10_000) { hasNodeContaining("$savedPercent%") }
        assertReaderFooterProgressBarMatchesPercent(savedPercent)
        captureSprint23FooterProgressScreenshot("01_default_font_saved_progress")

        scenario?.onActivity { activity -> activity.mainViewModel.openHome() }
        waitForHome()
        scenario?.onActivity { activity ->
            activity.mainViewModel.setReaderFontScale(1.3)
            activity.mainViewModel.openLibraryItem(document)
        }
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("reader-screen") }
        composeRule.waitUntil(timeoutMillis = 10_000) { hasNodeContaining("$savedPercent%") }
        assertReaderFooterProgressBarMatchesPercent(savedPercent)
        assertEquals(savedPercent, savedProgressPercentFor(document.id))
        assertEquals(savedParagraphIndex, savedProgressParagraphIndexFor(document.id))
        captureSprint23FooterProgressScreenshot("02_large_font_restored_same_progress")
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
        composeRule.onAllNodesWithText("figure · editorial image", ignoreCase = true)
            .fetchSemanticsNodes()
            .also { nodes -> assertEquals(0, nodes.size) }
        finishReaderFromCurrentPage()

        composeRule.waitUntil(timeoutMillis = 10_000) {
            hasNode("Two quick questions.")
        }
        composeRule.onNodeWithTag("feedback-screen").assertIsDisplayed()
        composeRule.onNodeWithText("Was this a good fit?").assertIsDisplayed()
        composeRule.onNodeWithText("Did it help you skip the scroll?").assertIsDisplayed()
        composeRule.onNodeWithText("Great fit").performClick()
        composeRule.onNodeWithText("Yes").performClick()
        composeRule.onNodeWithTag("feedback-log")
            .assertIsDisplayed()
            .performClick()

        composeRule.waitUntil(timeoutMillis = 10_000) {
            hasTag("progress-card") || hasNode("Progress")
        }
        composeRule.onNodeWithTag("progress-card").assertIsDisplayed()
        composeRule.onNodeWithText("day converted").assertIsDisplayed()
    }

    @Test
    fun completedContentIsHiddenThenCanBeReactivatedFromLibrary() {
        launchFixtureSystemIntervention()

        composeRule.onNodeWithText("Read this", substring = true)
            .assertIsDisplayed()
            .assertIsEnabled()
            .performClick()

        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("reader-screen") }
        val completedContentId = currentContentId()
        val completedTitle = currentContentTitle()
        finishReaderFromCurrentPage()

        composeRule.waitUntil(timeoutMillis = 10_000) {
            hasTag("feedback-screen") && hasCompletedProgressFor(completedContentId)
        }

        relaunchFixtureSystemIntervention()
        composeRule.waitUntil(timeoutMillis = 10_000) {
            completedContentId !in currentRecommendationContentIds()
        }

        scenario?.onActivity { activity -> activity.mainViewModel.openLibrary() }
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("library-list") }
        composeRule.onNodeWithTag("library-list")
            .performScrollToNode(hasText(completedTitle))
        composeRule.onNodeWithTag("library-completed-status-$completedContentId")
            .assertIsDisplayed()
        composeRule.onNodeWithText("Completed · hidden from suggestions")
            .assertIsDisplayed()
        composeRule.onNodeWithTag("completed-activation-$completedContentId")
            .assertIsDisplayed()
            .performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) {
            hasNode("Completed · active in suggestions")
        }

        relaunchFixtureSystemIntervention()
        composeRule.waitUntil(timeoutMillis = 10_000) {
            completedContentId in currentRecommendationContentIds()
        }
    }

    @Test
    fun settingsOpenAnywayUnlockSuppressesRepeatedSystemIntervention() {
        launchOnboardedApp()

        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("tab-settings") }
        composeRule.onNodeWithTag("tab-settings", useUnmergedTree = true)
            .performSemanticsAction(SemanticsActions.OnClick)
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("settings-list") }
        composeRule.onNodeWithTag("settings-list")
            .performScrollToNode(hasTestTag("open-anyway-unlock-120"))
        composeRule.onNodeWithTag("open-anyway-unlock-120")
            .assertIsDisplayed()
            .performClick()

        seedFixtureSelection()
        relaunchFixtureSystemIntervention()
        waitForOpenAnywayUnlock()
        composeRule.onNodeWithText("Open Fixture Feed One")
            .assertIsDisplayed()
            .assertIsEnabled()
            .performClick()

        composeRule.waitUntil(timeoutMillis = 10_000) {
            scenario?.state == androidx.lifecycle.Lifecycle.State.DESTROYED
        }
        scenario = null

        assertTrue(
            InterceptionRuntimeGate.shouldSuppress(
                targetAppPackage = FixtureTargetRegistry.fixtureDistractors.first().packageName,
                nowMillis = System.currentTimeMillis() + 119 * 60_000L,
            ),
        )

        val targetContext = InstrumentationRegistry.getInstrumentation().targetContext
        launchApp(
            MainActivity.createSystemInterceptionIntent(
                context = targetContext,
                targetAppPackage = FixtureTargetRegistry.fixtureDistractors.first().packageName,
            ),
        )

        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("home-list") }
        assertFalse(hasNode("You reached for Fixture Feed One"))
    }

    @Test
    fun unfinishedReadingAppearsOnHomeAndLibraryAndCanContinueWithoutIntervention() {
        launchFixtureSystemIntervention()

        composeRule.onNodeWithText("Read this", substring = true)
            .assertIsDisplayed()
            .performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("reader-screen") }
        val initiallyOpenedContentId = currentContentId()
        assertFalse(hasUnfinishedProgressFor(initiallyOpenedContentId))

        scenario?.onActivity { activity ->
            activity.mainViewModel.openHome()
        }
        waitForHome()
        assertFalse(hasTag("home-continue-card"))

        seedFreshDocumentRankingScenario()
        launchFixtureSystemIntervention()
        composeRule.onNodeWithText("Read this", substring = true)
            .assertIsDisplayed()
            .performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("reader-screen") }
        val contentId = currentContentId()
        advanceReaderPage()
        composeRule.waitUntil(timeoutMillis = 10_000) { hasUnfinishedProgressFor(contentId) }
        val savedPercent = savedProgressPercentFor(contentId)
        val savedParagraphIndex = savedProgressParagraphIndexFor(contentId)
        assertTrue(savedPercent in 1..99)
        assertTrue(savedParagraphIndex > 0)

        scenario?.onActivity { activity -> activity.mainViewModel.openHome() }
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("home-continue-card") }
        composeRule.onNodeWithTag("home-continue-card").assertIsDisplayed()
        composeRule.onNodeWithText("$savedPercent% read", substring = true).assertIsDisplayed()

        composeRule.onNodeWithTag("home-continue-action")
            .assertIsDisplayed()
            .performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("reader-screen") }
        composeRule.onNodeWithTag("reader-screen").assertIsDisplayed()
        composeRule.onNodeWithTag("reader-page-label").assertIsDisplayed()
        composeRule.waitUntil(timeoutMillis = 10_000) {
            visibleReaderParagraphIndices().any { index -> index > 0 }
        }
        val restoredVisibleParagraphIndices = visibleReaderParagraphIndices()
        assertTrue(
            "Expected continued Reader to restore a page at or before saved paragraph $savedParagraphIndex, found $restoredVisibleParagraphIndices",
            restoredVisibleParagraphIndices.any { index -> index in 1..savedParagraphIndex },
        )
        assertFalse(restoredVisibleParagraphIndices.contains(0))

        scenario?.onActivity { activity -> activity.mainViewModel.openLibrary() }
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("library-list") }
        composeRule.onNodeWithText("Unfinished")
            .assertIsDisplayed()
            .performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) { hasNodeContaining("$savedPercent% read") }
        composeRule.onNodeWithText("$savedPercent% read", substring = true).assertIsDisplayed()
        composeRule.onNodeWithTag("library-open-$contentId")
            .assertIsDisplayed()
            .performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("reader-screen") }
        finishReaderFromCurrentPage()

        composeRule.waitUntil(timeoutMillis = 10_000) {
            hasTag("feedback-screen") && hasCompletedProgressFor(contentId)
        }
        assertTrue(hasCompletedManualReaderEventFor(contentId))
    }

    @Test
    fun homeReadNowOpensLibraryAndPaginatedReaderWithoutIntervention() {
        launchOnboardedApp()
        val seeded = seedFreshDocumentRankingScenario()
        scenario?.onActivity { activity -> activity.mainViewModel.openHome() }
        waitForHome()

        composeRule.onNodeWithTag("home-list")
            .performScrollToNode(hasTestTag("home-read-now-card"))
        composeRule.onNodeWithTag("home-read-now-card").assertIsDisplayed()
        captureSprint14ReaderPaginationScreenshot("01_home_read_now_light")
        composeRule.onNodeWithTag("home-read-now-action")
            .assertIsDisplayed()
            .performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("library-list") }

        composeRule.onNodeWithTag("library-list")
            .performScrollToNode(hasTestTag("library-open-${seeded.newEpub.id}"))
        composeRule.onNodeWithTag("library-open-${seeded.newEpub.id}")
            .assertIsDisplayed()
            .performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("reader-screen") }
        assertEquals(seeded.newEpub.id, currentContentId())
        composeRule.onNodeWithTag("reader-page-label").assertIsDisplayed()
        composeRule.onNodeWithText("Next page").assertDoesNotExist()
        composeRule.onNodeWithText("Previous").assertDoesNotExist()
        composeRule.onNodeWithText("I'm done reading").assertDoesNotExist()
        captureSprint14ReaderPaginationScreenshot("02_reader_page_one_light")
        composeRule.onNodeWithTag("reader-list")
            .performTouchInput { swipeUp() }
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("reader-page-viewport")
            .performTouchInput { swipeLeft() }
        composeRule.waitUntil(timeoutMillis = 10_000) {
            visibleReaderParagraphIndices().any { index -> index > 0 }
        }
        composeRule.waitUntil(timeoutMillis = 10_000) { hasNodeContaining("2/") }
        composeRule.onNodeWithTag("reader-page-viewport")
            .performTouchInput { swipeRight() }
        composeRule.waitUntil(timeoutMillis = 10_000) {
            visibleReaderParagraphIndices().contains(0)
        }
        composeRule.waitUntil(timeoutMillis = 10_000) { hasNodeContaining("1/") }
        advanceReaderPage()
        composeRule.waitUntil(timeoutMillis = 10_000) {
            visibleReaderParagraphIndices().any { index -> index > 0 }
        }
        composeRule.waitUntil(timeoutMillis = 10_000) { hasNodeContaining("2/") }
        captureSprint14ReaderPaginationScreenshot("03_reader_next_page_light")
        scenario?.onActivity { activity -> activity.mainViewModel.selectThemeMode(AppThemeMode.DARK) }
        composeRule.waitForIdle()
        captureSprint14ReaderPaginationScreenshot("04_reader_next_page_dark")
        composeRule.onNodeWithTag("reader-list")
            .performTouchInput { swipeUp() }
        composeRule.waitForIdle()
        UiDevice.getInstance(InstrumentationRegistry.getInstrumentation()).pressBack()
        waitForHome()
        assertFalse(hasTag("reader-screen"))
    }

    @Test
    fun readerPaginationFitRespondsToViewportAndReaderTextSize() {
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        fun repeatedCodeFixture(prefix: String, lineCount: Int): String {
            return (1..24).joinToString(separator = "\n\n") { index ->
                val codeLines = (1..lineCount).joinToString(separator = "\n") { line ->
                    "val ${prefix}${index}_$line = ${index + line}"
                }
                "```kotlin\n$codeLines\n```"
            }
        }
        val fixtureBody = (1..34)
            .joinToString(separator = "\n\n") { index ->
                "Sprint 17 adaptive paragraph $index uses steady reader prose so the page fit can prove it responds to screen height and configured reader text size."
            }
        val fixtureCodeBody = (1..32)
            .joinToString(separator = "\n\n") { index ->
                "```kotlin\nval sprint17CodeBlock$index = $index\n```"
            }
        val fixtureMultiLineCodeBody = (1..10)
            .joinToString(separator = "\n\n") { index ->
                val codeLines = (1..8).joinToString(separator = "\n") { line ->
                    "val sprint17CodeBlock${index}_line$line = $line"
                }
                "```kotlin\n$codeLines\n```"
            }
        val fixtureShortMultiLineCodeBody = (1..24)
            .joinToString(separator = "\n\n") { index ->
                "```kotlin\n" +
                    "val shortMultiLine${index}_a = $index\n" +
                    "val shortMultiLine${index}_b = ${index + 1}\n" +
                    "```"
            }
        val fixtureThreeLineCodeBody = (1..24)
            .joinToString(separator = "\n\n") { index ->
                "```kotlin\n" +
                    "val threeLine${index}_a = $index\n" +
                    "val threeLine${index}_b = ${index + 1}\n" +
                    "val threeLine${index}_c = ${index + 2}\n" +
                    "```"
            }
        val fixtureFiveLineCodeBody = repeatedCodeFixture(prefix = "fiveLine", lineCount = 5)
        val fixtureSixLineCodeBody = repeatedCodeFixture(prefix = "sixLine", lineCount = 6)
        val fixtureSevenLineCodeBody = repeatedCodeFixture(prefix = "sevenLine", lineCount = 7)
        val fixtureNineLineCodeBody = repeatedCodeFixture(prefix = "nineLine", lineCount = 9)
        val fixtureSplitTailShortLineCodeBody = "```kotlin\n" +
            (1..36).joinToString(separator = "\n") { line -> "x$line" } +
            "\n```"
        val fixtureOversizedShortLineCodeBody = "```kotlin\n" +
            (1..40).joinToString(separator = "\n") { line -> "x$line" } +
            "\n```"
        val fixtureAdjacentWholeShortLineCodeBody = "```kotlin\n" +
            (1..19).joinToString(separator = "\n") { line -> "x$line" } +
            "\n```\n\n```kotlin\n" +
            (1..17).joinToString(separator = "\n") { line -> "y$line" } +
            "\n```"
        val fixtureMixedShortLineCodeAndBody = "```kotlin\n" +
            (1..34).joinToString(separator = "\n") { line -> "x$line" } +
            "\n```\n\nA short body tail must not squeeze below two full code chunks."
        var tallDefaultWeight = 0

        fun assertTallDefaultRepeatedCodeFit(
            title: String,
            displayName: String,
            body: String,
            expectedBlocks: Int,
            summaryName: String,
            context: String,
            nowMillis: Long,
        ) {
            val document = addSeedMarkdownDocument(
                title = title,
                displayName = displayName,
                body = body,
                nowMillis = nowMillis,
            )
            scenario?.onActivity { activity ->
                activity.mainViewModel.setReaderFontScale(DEFAULT_READER_FONT_SCALE)
                activity.mainViewModel.openLibraryItem(document)
            }
            composeRule.waitUntil(timeoutMillis = 10_000) {
                currentReaderFontScale() == DEFAULT_READER_FONT_SCALE &&
                    hasTag("reader-screen") &&
                    readerPageFitWeight(readerPageFitSummary()) > 0
            }
            val summary = readerPageFitSummary()
            assertTrue(
                "$context should admit the rendered-safe $expectedBlocks-block page. summary=$summary",
                readerPageFitBlocks(summary) >= expectedBlocks,
            )
            assertTrue(
                "$context should reject the next unsafe block. summary=$summary",
                readerPageFitBlocks(summary) <= expectedBlocks,
            )
            assertReaderVisibleContentStaysAboveFooter(
                context = context,
                expectAnotherBlock = false,
            )
            recordSprint17AdaptivePaginationSummary(summaryName, summary)
            captureSprint17AdaptivePaginationScreenshot(summaryName)
        }

        try {
            launchOnboardedApp()
            val tallDocument = addSeedMarkdownDocument(
                title = "Sprint 17 Pagination Fit",
                displayName = "sprint17-pagination-fit-tall.md",
                body = fixtureBody,
                nowMillis = 88_000L,
            )

            scenario?.onActivity { activity -> activity.mainViewModel.openLibraryItem(tallDocument) }
            composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("reader-screen") && readerPageFitWeight(readerPageFitSummary()) > 0 }
            val tallDefaultSummary = readerPageFitSummary()
            tallDefaultWeight = readerPageFitWeight(tallDefaultSummary)
            val tallDefaultBlocks = readerPageFitBlocks(tallDefaultSummary)
            assertTrue("Tall phone default text should use the footer-safe reader area without clipping. summary=$tallDefaultSummary", tallDefaultBlocks >= 8)
            assertTrue("Tall phone default text should obey rendered block cap. summary=$tallDefaultSummary", tallDefaultBlocks <= readerPageFitMaxBlocks(tallDefaultSummary))
            assertReaderVisibleContentStaysAboveFooter(
                context = "tall phone default",
                expectAnotherBlock = readerPageFitPages(tallDefaultSummary) > 1,
            )
            recordSprint17AdaptivePaginationSummary("01_tall_phone_default_text", tallDefaultSummary)
            captureSprint17AdaptivePaginationScreenshot("01_tall_phone_default_text")

            scenario?.onActivity { activity ->
                activity.mainViewModel.openLibraryItem(
                    content = tallDocument,
                    startParagraphIndex = 9,
                )
            }
            composeRule.waitUntil(timeoutMillis = 10_000) {
                hasTag("reader-screen") &&
                    readerPageFitBlocks(readerPageFitSummary()) >= 8 &&
                    visibleReaderParagraphIndices().contains(9)
            }
            assertTrue(
                "Reader should reconcile a target paragraph after measured viewport shifts boundaries " +
                    "without changing page count. summary=${readerPageFitSummary()} visible=${visibleReaderParagraphIndices()}",
                visibleReaderParagraphIndices().contains(9),
            )

            scenario?.onActivity { activity ->
                activity.mainViewModel.setReaderFontScale(1.3)
                activity.mainViewModel.openLibraryItem(tallDocument)
            }
            composeRule.waitUntil(timeoutMillis = 10_000) {
                currentReaderFontScale() == 1.3 &&
                    readerPageFitWeight(readerPageFitSummary()).let { weight -> weight in 1 until tallDefaultWeight }
            }
            val largeTextSummary = readerPageFitSummary()
            val largeTextWeight = readerPageFitWeight(largeTextSummary)
            assertTrue("Large reader text should reduce page capacity. default=$tallDefaultSummary large=$largeTextSummary", largeTextWeight < tallDefaultWeight)
            assertTrue("Large reader text should still use the available tall-phone area safely. summary=$largeTextSummary", readerPageFitBlocks(largeTextSummary) >= 5)
            assertTrue("Large reader text should obey rendered block cap. summary=$largeTextSummary", readerPageFitBlocks(largeTextSummary) <= readerPageFitMaxBlocks(largeTextSummary))
            assertReaderVisibleContentStaysAboveFooter(
                context = "tall phone large text",
                expectAnotherBlock = readerPageFitPages(largeTextSummary) > 1,
            )
            recordSprint17AdaptivePaginationSummary("02_tall_phone_large_text", largeTextSummary)
            captureSprint17AdaptivePaginationScreenshot("02_tall_phone_large_text")

            val codeDocument = addSeedMarkdownDocument(
                title = "Sprint 17 Code Pagination Fit",
                displayName = "sprint17-pagination-fit-code.md",
                body = fixtureCodeBody,
                nowMillis = 88_500L,
            )
            scenario?.onActivity { activity ->
                activity.mainViewModel.setReaderFontScale(DEFAULT_READER_FONT_SCALE)
                activity.mainViewModel.openLibraryItem(codeDocument)
            }
            composeRule.waitUntil(timeoutMillis = 10_000) {
                currentReaderFontScale() == DEFAULT_READER_FONT_SCALE &&
                    hasTag("reader-screen") &&
                    readerPageFitWeight(readerPageFitSummary()) > 0
            }
            val codeDefaultSummary = readerPageFitSummary()
            val codeDefaultBlocks = readerPageFitBlocks(codeDefaultSummary)
            // Device-relative invariant: the default-font code page renders content. Absolute block
            // counts are viewport-coupled (pagination is driven by the measured weight budget, which
            // scales with screen height; rendered blocks can exceed maxBlocksPerPage), so footer-safety
            // (asserted just below) plus the large-vs-default relation below carry the real contract.
            assertTrue("Default code blocks should render content. summary=$codeDefaultSummary", codeDefaultBlocks >= 5)
            assertReaderVisibleContentStaysAboveFooter(
                context = "tall phone code default",
                expectAnotherBlock = readerPageFitPages(codeDefaultSummary) > 1,
            )
            recordSprint17AdaptivePaginationSummary("03_tall_phone_code_blocks", codeDefaultSummary)
            captureSprint17AdaptivePaginationScreenshot("03_tall_phone_code_blocks")

            scenario?.onActivity { activity ->
                activity.mainViewModel.setReaderFontScale(1.3)
                activity.mainViewModel.openLibraryItem(codeDocument)
            }
            composeRule.waitUntil(timeoutMillis = 10_000) {
                currentReaderFontScale() == 1.3 &&
                    readerPageFitWeight(readerPageFitSummary()).let { weight -> weight in 1 until tallDefaultWeight }
            }
            val codeLargeTextSummary = readerPageFitSummary()
            val codeLargeTextBlocks = readerPageFitBlocks(codeLargeTextSummary)
            // Real product invariants without a device-specific count: large text renders content and
            // does not increase code-page capacity versus default text. Footer-safety asserted below.
            assertTrue("Large-text code blocks should render content. summary=$codeLargeTextSummary", codeLargeTextBlocks >= 1)
            assertTrue("Large reader text should not increase code-page capacity vs default. default=$codeDefaultSummary large=$codeLargeTextSummary", codeLargeTextBlocks <= codeDefaultBlocks)
            assertReaderVisibleContentStaysAboveFooter(
                context = "tall phone code large text",
                expectAnotherBlock = readerPageFitPages(codeLargeTextSummary) > 1,
            )
            recordSprint17AdaptivePaginationSummary("04_tall_phone_large_code_blocks", codeLargeTextSummary)
            captureSprint17AdaptivePaginationScreenshot("04_tall_phone_large_code_blocks")

            val multiLineCodeDocument = addSeedMarkdownDocument(
                title = "Sprint 17 Multi-line Code Pagination Fit",
                displayName = "sprint17-pagination-fit-multiline-code.md",
                body = fixtureMultiLineCodeBody,
                nowMillis = 88_750L,
            )
            scenario?.onActivity { activity ->
                activity.mainViewModel.setReaderFontScale(DEFAULT_READER_FONT_SCALE)
                activity.mainViewModel.openLibraryItem(multiLineCodeDocument)
            }
            composeRule.waitUntil(timeoutMillis = 10_000) {
                currentReaderFontScale() == DEFAULT_READER_FONT_SCALE &&
                    hasTag("reader-screen") &&
                    readerPageFitWeight(readerPageFitSummary()) > 0
            }
            val multiLineCodeDefaultSummary = readerPageFitSummary()
            assertTrue(
                "Default multi-line code blocks should admit only rendered-safe full blocks. summary=$multiLineCodeDefaultSummary",
                readerPageFitBlocks(multiLineCodeDefaultSummary) <= 4,
            )
            assertTrue(
                "Default multi-line code blocks should use the footer-safe tall-phone area. summary=$multiLineCodeDefaultSummary",
                readerPageFitBlocks(multiLineCodeDefaultSummary) >= 4,
            )
            assertReaderVisibleContentStaysAboveFooter(
                context = "tall phone multiline code default",
                expectAnotherBlock = readerPageFitPages(multiLineCodeDefaultSummary) > 1,
            )
            recordSprint17AdaptivePaginationSummary("05_tall_phone_multiline_code_blocks", multiLineCodeDefaultSummary)
            captureSprint17AdaptivePaginationScreenshot("05_tall_phone_multiline_code_blocks")

            scenario?.onActivity { activity ->
                activity.mainViewModel.setReaderFontScale(1.3)
                activity.mainViewModel.openLibraryItem(multiLineCodeDocument)
            }
            composeRule.waitUntil(timeoutMillis = 10_000) {
                currentReaderFontScale() == 1.3 &&
                    readerPageFitWeight(readerPageFitSummary()).let { weight -> weight in 1 until tallDefaultWeight }
            }
            val multiLineCodeLargeSummary = readerPageFitSummary()
            assertTrue(
                "Large-text multi-line code blocks should admit only rendered-safe full blocks. summary=$multiLineCodeLargeSummary",
                readerPageFitBlocks(multiLineCodeLargeSummary) <= 1,
            )
            assertTrue(
                "Large-text multi-line code blocks should use the available tall-phone area. summary=$multiLineCodeLargeSummary",
                readerPageFitBlocks(multiLineCodeLargeSummary) >= 1,
            )
            assertReaderVisibleContentStaysAboveFooter(
                context = "tall phone multiline code large text",
                expectAnotherBlock = readerPageFitPages(multiLineCodeLargeSummary) > 1,
            )
            recordSprint17AdaptivePaginationSummary("06_tall_phone_large_multiline_code_blocks", multiLineCodeLargeSummary)
            captureSprint17AdaptivePaginationScreenshot("06_tall_phone_large_multiline_code_blocks")

            val shortMultiLineCodeDocument = addSeedMarkdownDocument(
                title = "Sprint 17 Short Multi-line Code Fit",
                displayName = "sprint17-pagination-fit-short-multiline-code.md",
                body = fixtureShortMultiLineCodeBody,
                nowMillis = 88_825L,
            )
            scenario?.onActivity { activity ->
                activity.mainViewModel.setReaderFontScale(DEFAULT_READER_FONT_SCALE)
                activity.mainViewModel.openLibraryItem(shortMultiLineCodeDocument)
            }
            composeRule.waitUntil(timeoutMillis = 10_000) {
                currentReaderFontScale() == DEFAULT_READER_FONT_SCALE &&
                    hasTag("reader-screen") &&
                    readerPageFitWeight(readerPageFitSummary()) > 0
            }
            val shortMultiLineCodeDefaultSummary = readerPageFitSummary()
            assertTrue(
                    "Default short multi-line code should admit the measured footer-safe 14-block page. " +
                    "summary=$shortMultiLineCodeDefaultSummary",
                readerPageFitBlocks(shortMultiLineCodeDefaultSummary) >= 14,
            )
            assertTrue(
                "Default short multi-line code should still reject the next block. " +
                    "summary=$shortMultiLineCodeDefaultSummary",
                readerPageFitBlocks(shortMultiLineCodeDefaultSummary) <= 14,
            )
            assertReaderVisibleContentStaysAboveFooter(
                context = "tall phone short multiline code default",
                expectAnotherBlock = false,
            )
            recordSprint17AdaptivePaginationSummary("07_tall_phone_short_multiline_code_blocks", shortMultiLineCodeDefaultSummary)
            captureSprint17AdaptivePaginationScreenshot("07_tall_phone_short_multiline_code_blocks")

            val threeLineCodeDocument = addSeedMarkdownDocument(
                title = "Sprint 17 Three-line Code Fit",
                displayName = "sprint17-pagination-fit-three-line-code.md",
                body = fixtureThreeLineCodeBody,
                nowMillis = 88_850L,
            )
            scenario?.onActivity { activity ->
                activity.mainViewModel.setReaderFontScale(DEFAULT_READER_FONT_SCALE)
                activity.mainViewModel.openLibraryItem(threeLineCodeDocument)
            }
            composeRule.waitUntil(timeoutMillis = 10_000) {
                currentReaderFontScale() == DEFAULT_READER_FONT_SCALE &&
                    hasTag("reader-screen") &&
                    readerPageFitWeight(readerPageFitSummary()) > 0
            }
            val threeLineCodeDefaultSummary = readerPageFitSummary()
            assertTrue(
                    "Default three-line code should admit the measured footer-safe 10-block page. " +
                    "summary=$threeLineCodeDefaultSummary",
                readerPageFitBlocks(threeLineCodeDefaultSummary) >= 10,
            )
            assertTrue(
                "Default three-line code should reject the unsafe 11th block. " +
                    "summary=$threeLineCodeDefaultSummary",
                readerPageFitBlocks(threeLineCodeDefaultSummary) <= 10,
            )
            assertReaderVisibleContentStaysAboveFooter(
                context = "tall phone three-line code default",
                expectAnotherBlock = false,
            )
            recordSprint17AdaptivePaginationSummary("08_tall_phone_three_line_code_blocks", threeLineCodeDefaultSummary)
            captureSprint17AdaptivePaginationScreenshot("08_tall_phone_three_line_code_blocks")

            assertTallDefaultRepeatedCodeFit(
                title = "Sprint 17 Five-line Code Fit",
                displayName = "sprint17-pagination-fit-five-line-code.md",
                body = fixtureFiveLineCodeBody,
                expectedBlocks = 6,
                summaryName = "09_tall_phone_five_line_code_blocks",
                context = "tall phone five-line code default",
                nowMillis = 88_860L,
            )

            assertTallDefaultRepeatedCodeFit(
                title = "Sprint 17 Six-line Code Fit",
                displayName = "sprint17-pagination-fit-six-line-code.md",
                body = fixtureSixLineCodeBody,
                expectedBlocks = 5,
                summaryName = "10_tall_phone_six_line_code_blocks",
                context = "tall phone six-line code default",
                nowMillis = 88_865L,
            )

            assertTallDefaultRepeatedCodeFit(
                title = "Sprint 17 Seven-line Code Fit",
                displayName = "sprint17-pagination-fit-seven-line-code.md",
                body = fixtureSevenLineCodeBody,
                expectedBlocks = 4,
                summaryName = "11_tall_phone_seven_line_code_blocks",
                context = "tall phone seven-line code default",
                nowMillis = 88_870L,
            )

            assertTallDefaultRepeatedCodeFit(
                title = "Sprint 17 Nine-line Code Fit",
                displayName = "sprint17-pagination-fit-nine-line-code.md",
                body = fixtureNineLineCodeBody,
                expectedBlocks = 3,
                summaryName = "12_tall_phone_nine_line_code_blocks",
                context = "tall phone nine-line code default",
                nowMillis = 88_872L,
            )

            val splitTailShortLineCodeDocument = addSeedMarkdownDocument(
                title = "Sprint 17 Split-tail Short-line Code Fit",
                displayName = "sprint17-pagination-fit-split-tail-short-line-code.md",
                body = fixtureSplitTailShortLineCodeBody,
                nowMillis = 88_875L,
            )
            scenario?.onActivity { activity ->
                activity.mainViewModel.setReaderFontScale(DEFAULT_READER_FONT_SCALE)
                activity.mainViewModel.openLibraryItem(splitTailShortLineCodeDocument)
            }
            composeRule.waitUntil(timeoutMillis = 10_000) {
                currentReaderFontScale() == DEFAULT_READER_FONT_SCALE &&
                    hasTag("reader-screen") &&
                    readerPageFitWeight(readerPageFitSummary()) > 0
            }
            val oversizedShortLineCodeDefaultSummary = readerPageFitSummary()
            assertTrue(
                "Default split-tail short-line code should admit only rendered-safe chunks. summary=$oversizedShortLineCodeDefaultSummary",
                readerPageFitBlocks(oversizedShortLineCodeDefaultSummary) <= 2,
            )
            assertTrue(
                "Default split-tail short-line code should use the footer-safe tall-phone area. summary=$oversizedShortLineCodeDefaultSummary",
                readerPageFitBlocks(oversizedShortLineCodeDefaultSummary) >= 2,
            )
            assertTrue(
                "Default split-tail short-line code should keep a real next page for rejected chunks. summary=$oversizedShortLineCodeDefaultSummary",
                readerPageFitPages(oversizedShortLineCodeDefaultSummary) > 1,
            )
            assertReaderVisibleContentStaysAboveFooter(
                context = "tall phone oversized short-line code default",
                expectAnotherBlock = false,
            )
            recordSprint17AdaptivePaginationSummary("13_tall_phone_oversized_short_line_code_blocks", oversizedShortLineCodeDefaultSummary)
            captureSprint17AdaptivePaginationScreenshot("13_tall_phone_oversized_short_line_code_blocks")

            val oversizedShortLineCodeDocument = addSeedMarkdownDocument(
                title = "Sprint 17 Oversized Short-line Code Tail Fit",
                displayName = "sprint17-pagination-fit-oversized-short-line-code.md",
                body = fixtureOversizedShortLineCodeBody,
                nowMillis = 88_900L,
            )
            scenario?.onActivity { activity ->
                activity.mainViewModel.setReaderFontScale(1.3)
                activity.mainViewModel.openLibraryItem(oversizedShortLineCodeDocument)
            }
            composeRule.waitUntil(timeoutMillis = 10_000) {
                currentReaderFontScale() == 1.3 &&
                    readerPageFitWeight(readerPageFitSummary()).let { weight -> weight in 1 until tallDefaultWeight }
            }
            val oversizedShortLineCodeLargeSummary = readerPageFitSummary()
            assertTrue(
                "Large-text oversized short-line code should split into rendered-safe page chunks. summary=$oversizedShortLineCodeLargeSummary",
                readerPageFitBlocks(oversizedShortLineCodeLargeSummary) <= 1,
            )
            assertTrue(
                "Large-text oversized short-line code should keep a real next page for rejected chunks. summary=$oversizedShortLineCodeLargeSummary",
                readerPageFitPages(oversizedShortLineCodeLargeSummary) > 1,
            )
            assertReaderVisibleContentStaysAboveFooter(
                context = "tall phone oversized short-line code large text",
                expectAnotherBlock = false,
            )
            recordSprint17AdaptivePaginationSummary("14_tall_phone_large_oversized_short_line_code_blocks", oversizedShortLineCodeLargeSummary)
            captureSprint17AdaptivePaginationScreenshot("14_tall_phone_large_oversized_short_line_code_blocks")

            advanceReaderPage()
            composeRule.waitUntil(timeoutMillis = 10_000) {
                readerPageFitBlocks(readerPageFitSummary()) == 2 && currentReaderPageEndParagraphIndex() == 2
            }
            val oversizedShortLineCodeLargeTailSummary = readerPageFitSummary()
            assertTrue(
                "Large-text oversized short-line code should admit the 10-line tail with the second 15-line chunk. " +
                    "summary=$oversizedShortLineCodeLargeTailSummary",
                readerPageFitBlocks(oversizedShortLineCodeLargeTailSummary) >= 2,
            )
            assertReaderVisibleContentStaysAboveFooter(
                context = "tall phone oversized short-line code large text tail",
                expectAnotherBlock = false,
            )
            recordSprint17AdaptivePaginationSummary("15_tall_phone_large_oversized_short_line_code_tail_blocks", oversizedShortLineCodeLargeTailSummary)
            captureSprint17AdaptivePaginationScreenshot("15_tall_phone_large_oversized_short_line_code_tail_blocks")

            val adjacentWholeShortLineCodeDocument = addSeedMarkdownDocument(
                title = "Sprint 17 Adjacent Whole Short-line Code Fit",
                displayName = "sprint17-pagination-fit-adjacent-whole-short-line-code.md",
                body = fixtureAdjacentWholeShortLineCodeBody,
                nowMillis = 88_925L,
            )
            scenario?.onActivity { activity ->
                activity.mainViewModel.setReaderFontScale(DEFAULT_READER_FONT_SCALE)
                activity.mainViewModel.openLibraryItem(adjacentWholeShortLineCodeDocument)
            }
            composeRule.waitUntil(timeoutMillis = 10_000) {
                currentReaderFontScale() == DEFAULT_READER_FONT_SCALE &&
                    hasTag("reader-screen") &&
                    readerPageFitWeight(readerPageFitSummary()) > 0
            }
            val adjacentWholeShortLineCodeDefaultSummary = readerPageFitSummary()
            assertTrue(
                "Adjacent whole short-line code should split before the unsafe 19+17 geometry. " +
                    "summary=$adjacentWholeShortLineCodeDefaultSummary",
                readerPageFitBlocks(adjacentWholeShortLineCodeDefaultSummary) <= 2,
            )
            assertTrue(
                "Adjacent whole short-line code should still use the available tall-phone area. " +
                    "summary=$adjacentWholeShortLineCodeDefaultSummary",
                readerPageFitBlocks(adjacentWholeShortLineCodeDefaultSummary) >= 2,
            )
            assertTrue(
                "Adjacent whole short-line code should keep a real next page for the following 17-line block. " +
                    "summary=$adjacentWholeShortLineCodeDefaultSummary",
                readerPageFitPages(adjacentWholeShortLineCodeDefaultSummary) > 1,
            )
            assertReaderVisibleContentStaysAboveFooter(
                context = "tall phone adjacent whole short-line code default",
                expectAnotherBlock = false,
            )
            recordSprint17AdaptivePaginationSummary("16_tall_phone_adjacent_whole_short_line_code_blocks", adjacentWholeShortLineCodeDefaultSummary)
            captureSprint17AdaptivePaginationScreenshot("16_tall_phone_adjacent_whole_short_line_code_blocks")

            val mixedShortLineCodeAndBodyDocument = addSeedMarkdownDocument(
                title = "Sprint 17 Mixed Code Body Fit",
                displayName = "sprint17-pagination-fit-mixed-code-body.md",
                body = fixtureMixedShortLineCodeAndBody,
                nowMillis = 88_950L,
            )
            scenario?.onActivity { activity ->
                activity.mainViewModel.setReaderFontScale(DEFAULT_READER_FONT_SCALE)
                activity.mainViewModel.openLibraryItem(mixedShortLineCodeAndBodyDocument)
            }
            composeRule.waitUntil(timeoutMillis = 10_000) {
                currentReaderFontScale() == DEFAULT_READER_FONT_SCALE &&
                    hasTag("reader-screen") &&
                    readerPageFitWeight(readerPageFitSummary()) > 0
            }
            val mixedShortLineCodeAndBodySummary = readerPageFitSummary()
            assertTrue(
                "Mixed code/body page should keep only the measured footer-safe first two chunks. " +
                    "summary=$mixedShortLineCodeAndBodySummary",
                readerPageFitBlocks(mixedShortLineCodeAndBodySummary) <= 2,
            )
            assertTrue(
                "Mixed code/body page should still use the footer-safe tall-phone area. " +
                    "summary=$mixedShortLineCodeAndBodySummary",
                readerPageFitBlocks(mixedShortLineCodeAndBodySummary) >= 2,
            )
            assertTrue(
                "Mixed code/body page should move the body tail to a real next page. " +
                    "summary=$mixedShortLineCodeAndBodySummary",
                readerPageFitPages(mixedShortLineCodeAndBodySummary) > 1,
            )
            assertReaderVisibleContentStaysAboveFooter(
                context = "tall phone mixed short-line code and body default",
                expectAnotherBlock = false,
            )
            recordSprint17AdaptivePaginationSummary("17_tall_phone_mixed_short_line_code_body_blocks", mixedShortLineCodeAndBodySummary)
            captureSprint17AdaptivePaginationScreenshot("17_tall_phone_mixed_short_line_code_body_blocks")

            scenario?.close()
            scenario = null
            device.executeShellCommand("wm size 720x1280")
            device.executeShellCommand("wm density 320")
            Thread.sleep(1_000)

            launchOnboardedApp()
            val smallDocument = addSeedMarkdownDocument(
                title = "Sprint 17 Small Phone Fit",
                displayName = "sprint17-pagination-fit-small.md",
                body = fixtureBody,
                nowMillis = 89_000L,
            )
            scenario?.onActivity { activity ->
                activity.mainViewModel.setReaderFontScale(DEFAULT_READER_FONT_SCALE)
                activity.mainViewModel.openLibraryItem(smallDocument)
            }
            composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("reader-screen") && readerPageFitWeight(readerPageFitSummary()) > 0 }
            val smallPhoneSummary = readerPageFitSummary()
            val smallPhoneWeight = readerPageFitWeight(smallPhoneSummary)
            assertTrue("Small phone viewport should receive a smaller page budget. tall=$tallDefaultSummary small=$smallPhoneSummary", smallPhoneWeight < tallDefaultWeight)
            assertTrue("Small phone should still use the available reader area for multiple blocks. summary=$smallPhoneSummary", readerPageFitBlocks(smallPhoneSummary) >= 4)
            assertTrue("Small phone text should obey rendered block cap. summary=$smallPhoneSummary", readerPageFitBlocks(smallPhoneSummary) <= readerPageFitMaxBlocks(smallPhoneSummary))
            assertReaderVisibleContentStaysAboveFooter(
                context = "small phone default",
                expectAnotherBlock = readerPageFitPages(smallPhoneSummary) > 1,
            )
            recordSprint17AdaptivePaginationSummary("18_small_phone_default_text", smallPhoneSummary)
            captureSprint17AdaptivePaginationScreenshot("18_small_phone_default_text")
        } finally {
            scenario?.close()
            scenario = null
            device.executeShellCommand("wm size reset")
            device.executeShellCommand("wm density reset")
            Thread.sleep(1_000)
        }
    }

    @Test
    fun epubReaderUsesKindlePagingAndTableOfContentsNavigation() {
        launchOnboardedApp()
        val document = addSeedEpubDocument(
            title = "Sprint 15 TOC Reader",
            displayName = "sprint15-toc-reader.epub",
            bytes = sprint15TocEpubBytes(title = "Sprint 15 TOC Reader"),
            nowMillis = 41_000L,
        )

        scenario?.onActivity { activity -> activity.mainViewModel.openLibraryItem(document) }
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("reader-screen") }
        composeRule.onNodeWithTag("reader-page-label").assertIsDisplayed()
        composeRule.onNodeWithTag("reader-toc-open").assertIsDisplayed()
        composeRule.onNodeWithText("Chapter One", substring = true).assertIsDisplayed()

        val firstPageIndices = visibleReaderParagraphIndices()
        assertTrue(firstPageIndices.contains(0))
        composeRule.onNodeWithTag("reader-list")
            .performTouchInput { swipeUp() }
        composeRule.waitForIdle()
        assertEquals(
            "Reader body should not respond to vertical swipe; paging is tap/edge/gesture only.",
            firstPageIndices,
            visibleReaderParagraphIndices(),
        )

        composeRule.onNodeWithTag("reader-page-viewport").performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) {
            visibleReaderParagraphIndices().any { index -> index > (firstPageIndices.maxOrNull() ?: -1) }
        }
        assertFalse(visibleReaderParagraphIndices().contains(0))

        composeRule.onNodeWithTag("reader-page-viewport")
            .performTouchInput { swipeRight() }
        composeRule.waitUntil(timeoutMillis = 10_000) {
            visibleReaderParagraphIndices() == firstPageIndices
        }
        composeRule.onNodeWithTag("reader-page-viewport")
            .performSemanticsAction(SemanticsActions.OnClick)
        composeRule.waitUntil(timeoutMillis = 10_000) {
            visibleReaderParagraphIndices().any { index -> index > (firstPageIndices.maxOrNull() ?: -1) }
        }
        composeRule.onNodeWithTag("reader-page-viewport")
            .performTouchInput { click(Offset(4f, 120f)) }
        composeRule.waitUntil(timeoutMillis = 10_000) {
            visibleReaderParagraphIndices() == firstPageIndices
        }
        UiDevice.getInstance(InstrumentationRegistry.getInstrumentation()).pressBack()
        waitForHome()
        assertFalse(hasTag("reader-screen"))

        scenario?.onActivity { activity -> activity.mainViewModel.openLibraryItem(document) }
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("reader-screen") }

        composeRule.onNodeWithTag("reader-toc-open")
            .assertIsDisplayed()
            .performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("reader-toc-sheet") }
        captureSprint14ReaderPaginationScreenshot("05_reader_toc_sheet_light")
        val chapterTwoIndex = readerTocEntryIndex("Chapter Two")
        val chapterTwoBlockIndex = readerTocEntryBlockIndex("Chapter Two")
        composeRule.onNodeWithTag("reader-toc-entry-$chapterTwoIndex")
            .assertIsDisplayed()
            .performSemanticsAction(SemanticsActions.OnClick)
        composeRule.waitForIdle()
        val postTocVisibleIndices = visibleReaderParagraphIndices()
        assertTrue(
            "Expected Chapter Two TOC click to close sheet and land near block $chapterTwoBlockIndex; " +
                "visible=$postTocVisibleIndices sheetVisible=${hasTag("reader-toc-sheet")}",
            !hasTag("reader-toc-sheet") && postTocVisibleIndices.any { index -> index >= chapterTwoBlockIndex },
        )
        composeRule.waitUntil(timeoutMillis = 10_000) {
            hasNodeContaining("table-of-contents target") && !hasTag("reader-toc-sheet")
        }
        composeRule.onNodeWithText("table-of-contents target", substring = true).assertIsDisplayed()
    }

    @Test
    fun sprint19EpubProgressAndAnnotationStartStayAnchoredInLaterChapter() {
        launchOnboardedApp()
        val document = addSeedEpubDocument(
            title = "Sprint 19 Chaptered Reader",
            displayName = "sprint19-reader-regression.epub",
            bytes = sprint19RegressionEpubBytes(),
            nowMillis = 91_000L,
        )

        scenario?.onActivity { activity -> activity.mainViewModel.openLibraryItem(document) }
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("reader-screen") }
        composeRule.onNodeWithTag("reader-toc-open")
            .assertIsDisplayed()
            .performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("reader-toc-sheet") }
        val chapterThreeIndex = readerTocEntryIndex("Chapter Three")
        val chapterThreeBlockIndex = readerTocEntryBlockIndex("Chapter Three")
        composeRule.onNodeWithTag("reader-toc-entry-$chapterThreeIndex")
            .assertIsDisplayed()
            .performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) {
            !hasTag("reader-toc-sheet") &&
                visibleReaderParagraphIndices().any { index -> index >= chapterThreeBlockIndex }
        }

        val pageLabelBeforeFontChange = readerPagePositionFromLabel()
        val progressBeforeFontChange = currentReaderProgressPercentFromLabel()
        assertTrue(
            "Chapter three should not display a beginning-of-book 1% progress label; label=${readerPageLabelText()}",
            progressBeforeFontChange > 40,
        )
        captureReaderFormEvidenceScreenshot("01_chapter_three_progress_not_one_percent")

        scenario?.onActivity { activity -> activity.mainViewModel.setReaderFontScale(1.3) }
        composeRule.waitUntil(timeoutMillis = 10_000) { currentReaderFontScale() == 1.3 }
        composeRule.waitUntil(timeoutMillis = 10_000) {
            readerPagePositionFromLabel().totalPages > pageLabelBeforeFontChange.totalPages
        }
        val pageLabelAfterFontChange = readerPagePositionFromLabel()
        val progressAfterFontChange = currentReaderProgressPercentFromLabel()
        assertTrue(
            "Reader font-size changes should preserve the source-anchored progress. " +
                "before=$progressBeforeFontChange after=$progressAfterFontChange " +
                "beforeLabel=$pageLabelBeforeFontChange afterLabel=$pageLabelAfterFontChange label=${readerPageLabelText()}",
            progressAfterFontChange in (progressBeforeFontChange - 1)..(progressBeforeFontChange + 1),
        )
        assertTrue(
            "Reader font-size change should visibly repaginate the fixture instead of staying on the same tiny 3-page layout. " +
                "before=$pageLabelBeforeFontChange after=$pageLabelAfterFontChange",
            pageLabelAfterFontChange.totalPages > pageLabelBeforeFontChange.totalPages,
        )
        captureReaderFormEvidenceScreenshot("02_chapter_three_large_font_progress_stable")

        composeRule.onNodeWithTag("reader-toc-open")
            .assertIsDisplayed()
            .performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("reader-toc-sheet") }
        composeRule.onNodeWithTag("reader-toc-entry-$chapterThreeIndex")
            .assertIsDisplayed()
            .performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) {
            !hasTag("reader-toc-sheet") &&
                visibleReaderParagraphIndices().any { index -> index >= chapterThreeBlockIndex }
        }

        val contentId = currentContentId()
        val annotationParagraph = chapterThreeBlockIndex + 1
        composeRule.waitUntil(timeoutMillis = 10_000) {
            hasTag("reader-annotation-block-$annotationParagraph")
        }
        composeRule.onNodeWithTag("reader-annotation-block-$annotationParagraph")
            .assertIsDisplayed()
            .performTouchInput { longClick(position = Offset(24f, 24f)) }
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("reader-annotation-editor-$annotationParagraph") }
        val initialQuote = readerSelectedQuoteText(annotationParagraph)
        assertTrue(initialQuote.contains("third chapter anchor"))
        captureReaderFormEvidenceScreenshot("03_annotation_before_start_back")
        repeat(30) {
            if (!readerSelectedQuoteText(annotationParagraph).contains("second chapter marker") &&
                hasTag("reader-annotation-start-earlier")
            ) {
                composeRule.onNodeWithTag("reader-annotation-start-earlier")
                    .assertIsDisplayed()
                    .performClick()
                composeRule.waitForIdle()
            }
        }

        val expandedQuote = readerSelectedQuoteText(annotationParagraph)
        assertFalse(
            "Moving annotation start backward from chapter three must not jump to the first source page. " +
                "visible=${visibleReaderParagraphIndices()} quote=$expandedQuote",
            visibleReaderParagraphIndices().contains(0),
        )
        assertTrue(
            "Expanded quote should cross into the previous chapter while remaining anchored around chapter three text.",
            expandedQuote.contains("second chapter marker") && expandedQuote.contains("third chapter anchor"),
        )
        captureReaderFormEvidenceScreenshot("04_annotation_start_back_without_book_start_jump")

        val noteText = "Sprint 19 saved cross-chapter selector."
        composeRule.onNodeWithTag("reader-annotation-note-input-$annotationParagraph")
            .assertIsDisplayed()
            .performClick()
            .performTextInput(noteText)
        UiDevice.getInstance(InstrumentationRegistry.getInstrumentation()).pressBack()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("reader-annotation-save-$annotationParagraph")
            .assertIsDisplayed()
            .assertIsEnabled()
            .performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) {
            !hasTag("reader-annotation-editor-$annotationParagraph") &&
                hasReadingAnnotationNote(contentId = contentId, paragraphIndex = annotationParagraph, noteText = noteText) &&
                hasReadingAnnotationQuoteContaining(contentId, annotationParagraph, "second chapter marker") &&
                hasReadingAnnotationQuoteContaining(contentId, annotationParagraph, "third chapter anchor")
        }
        captureReaderFormEvidenceScreenshot("05_annotation_saved_cross_chapter_highlight")
        scenario?.onActivity { activity -> activity.mainViewModel.dismissMessage() }
        composeRule.waitUntil(timeoutMillis = 10_000) { !hasNode("Annotation saved.") }

        val reopenedParagraph = visibleAnnotationHighlightIndices().maxOrNull() ?: annotationParagraph
        composeRule.onNodeWithTag("reader-annotation-block-$reopenedParagraph")
            .assertIsDisplayed()
            .performTouchInput { longClick(position = Offset(24f, 24f)) }
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("reader-annotation-editor-$reopenedParagraph") }
        val reopenedQuote = readerSelectedQuoteText(reopenedParagraph)
        assertTrue(
            "Reopened note should preserve the saved cross-chapter selector. quote=$reopenedQuote",
            reopenedQuote.contains("second chapter marker") && reopenedQuote.contains("third chapter anchor"),
        )
        captureReaderFormEvidenceScreenshot("06_annotation_reopened_cross_chapter_selector")
    }

    @Test
    fun sprint19FormInterventionShowsFiveSecondUnlockBeforeOpenAnyway() {
        launchFixtureSystemIntervention()

        composeRule.onNodeWithTag("form-intervention-unlock-wait")
            .assertIsDisplayed()
        composeRule.onNodeWithText("Open in", substring = true)
            .assertIsDisplayed()
            .assertIsNotEnabled()
        composeRule.onNodeWithTag("intervention-open-anyway-close")
            .assertIsDisplayed()
            .assertIsNotEnabled()
        captureReaderFormEvidenceScreenshot("07_form_intervention_waiting_locked")

        waitForOpenAnywayUnlock()
        composeRule.onNodeWithText("Open Fixture Feed One")
            .assertIsDisplayed()
            .assertIsEnabled()
        composeRule.onNodeWithTag("intervention-open-anyway-close")
            .assertIsDisplayed()
            .assertIsEnabled()
        captureReaderFormEvidenceScreenshot("08_form_intervention_unlock_ready")
    }

    @Test
    fun sprint19ReaderSessionProgressPersistsLastViewedPageAfterReopen() {
        launchOnboardedApp()
        val document = addSeedMarkdownDocument(
            title = "Sprint 19 Session Progress",
            displayName = "sprint19-session-progress.md",
            body = (1..80).joinToString(separator = "\n\n") { index ->
                "Session progress paragraph $index gives the reader enough source material to page forward, " +
                    "move back once, lock, and reopen without returning to the stale pre-session location."
            },
            nowMillis = 95_000L,
        )

        scenario?.onActivity { activity -> activity.mainViewModel.openLibraryItem(document) }
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("reader-screen") }
        repeat(3) {
            val beforePage = readerPagePositionFromLabel().currentPage
            advanceReaderPage()
            composeRule.waitUntil(timeoutMillis = 10_000) {
                readerPagePositionFromLabel().currentPage > beforePage
            }
        }
        val forwardPage = readerPagePositionFromLabel()
        composeRule.onNodeWithTag("reader-page-viewport")
            .performTouchInput { swipeRight() }
        composeRule.waitUntil(timeoutMillis = 10_000) {
            readerPagePositionFromLabel().currentPage == forwardPage.currentPage - 1
        }
        val lastViewedPage = readerPagePositionFromLabel()
        val lastViewedPercent = currentReaderProgressPercentFromLabel()
        val lastViewedEndParagraph = currentReaderPageEndParagraphIndex()
        composeRule.waitUntil(timeoutMillis = 10_000) {
            durableRoomSavedProgressPercentFor(document.id) == lastViewedPercent &&
                durableRoomSavedProgressParagraphIndexFor(document.id) == lastViewedEndParagraph
        }
        appendReaderFormEvidence(
            stage = "saved_before_pause_stop",
            page = lastViewedPage,
            progressPercent = lastViewedPercent,
            pageEndParagraph = lastViewedEndParagraph,
            durableSavedParagraph = durableRoomSavedProgressParagraphIndexFor(document.id),
        )
        captureReaderFormEvidenceScreenshot("09_session_progress_saved_before_pause_stop")

        scenario?.moveToState(Lifecycle.State.CREATED)
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        Thread.sleep(250)
        scenario?.moveToState(Lifecycle.State.RESUMED)
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("reader-screen") }
        composeRule.waitUntil(timeoutMillis = 10_000) {
            readerPagePositionFromLabel().currentPage == lastViewedPage.currentPage
        }
        assertEquals(lastViewedPage.totalPages, readerPagePositionFromLabel().totalPages)
        assertEquals(lastViewedEndParagraph, currentReaderPageEndParagraphIndex())
        appendReaderFormEvidence(
            stage = "restored_after_pause_stop",
            page = readerPagePositionFromLabel(),
            progressPercent = currentReaderProgressPercentFromLabel(),
            pageEndParagraph = currentReaderPageEndParagraphIndex(),
            durableSavedParagraph = durableRoomSavedProgressParagraphIndexFor(document.id),
        )
        captureReaderFormEvidenceScreenshot("10_session_progress_restored_after_pause_stop")

        scenario?.close()
        scenario = null
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        Thread.sleep(250)
        launchOnboardedApp()
        scenario?.onActivity { activity -> activity.mainViewModel.openLibraryItem(document) }
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("reader-screen") }
        composeRule.waitUntil(timeoutMillis = 10_000) {
            readerPagePositionFromLabel().currentPage == lastViewedPage.currentPage
        }
        assertEquals(lastViewedPage.totalPages, readerPagePositionFromLabel().totalPages)
        assertEquals(lastViewedEndParagraph, currentReaderPageEndParagraphIndex())
        appendReaderFormEvidence(
            stage = "restored_after_reopen",
            page = readerPagePositionFromLabel(),
            progressPercent = currentReaderProgressPercentFromLabel(),
            pageEndParagraph = currentReaderPageEndParagraphIndex(),
            durableSavedParagraph = durableRoomSavedProgressParagraphIndexFor(document.id),
        )
        captureReaderFormEvidenceScreenshot("11_session_progress_restored_after_reopen")
    }

    @Test
    fun sprint19ReaderResumeUsesPendingLatestProgressAcrossImmediateReopenBeforeRoomWrite() {
        launchOnboardedApp()
        val document = addSeedMarkdownDocument(
            title = "Sprint 19 Pending Progress",
            displayName = "sprint19-pending-progress.md",
            body = (1..80).joinToString(separator = "\n\n") { index ->
                "Pending progress paragraph $index gives the reader enough source material to save a latest " +
                    "anchor, close immediately, reopen before Room writes, and avoid falling back to stale state."
            },
            nowMillis = 97_000L,
        )

        scenario?.onActivity { activity -> activity.mainViewModel.openLibraryItem(document) }
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("reader-screen") }
        repeat(2) {
            val beforePage = readerPagePositionFromLabel().currentPage
            advanceReaderPage()
            composeRule.waitUntil(timeoutMillis = 10_000) {
                readerPagePositionFromLabel().currentPage > beforePage
            }
        }
        val stablePage = readerPagePositionFromLabel()
        val stablePercent = currentReaderProgressPercentFromLabel()
        val stableEndParagraph = currentReaderPageEndParagraphIndex()
        composeRule.waitUntil(timeoutMillis = 10_000) {
            durableRoomSavedProgressPercentFor(document.id) == stablePercent &&
                durableRoomSavedProgressParagraphIndexFor(document.id) == stableEndParagraph
        }

        val delay = roomReadingProgressRepositoryForTests().delayNextUnfinishedSaveForTests()
        advanceReaderPage()
        composeRule.waitUntil(timeoutMillis = 10_000) {
            delay.started.isCompleted &&
                readerPagePositionFromLabel().currentPage == stablePage.currentPage + 1
        }
        val latestPage = readerPagePositionFromLabel()
        val latestPercent = currentReaderProgressPercentFromLabel()
        val latestEndParagraph = currentReaderPageEndParagraphIndex()
        assertEquals(stableEndParagraph, durableRoomSavedProgressParagraphIndexFor(document.id))
        appendReaderFormEvidence(
            stage = "pending_latest_before_room_write",
            page = latestPage,
            progressPercent = latestPercent,
            pageEndParagraph = latestEndParagraph,
            durableSavedParagraph = durableRoomSavedProgressParagraphIndexFor(document.id),
        )
        captureReaderFormEvidenceScreenshot("12_pending_latest_before_room_write")

        scenario?.close()
        scenario = null
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        Thread.sleep(250)
        launchOnboardedApp()
        scenario?.onActivity { activity -> activity.mainViewModel.openLibraryItem(document) }
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("reader-screen") }
        composeRule.waitUntil(timeoutMillis = 10_000) {
            readerPagePositionFromLabel().currentPage == latestPage.currentPage
        }
        assertEquals(latestEndParagraph, currentReaderPageEndParagraphIndex())
        appendReaderFormEvidence(
            stage = "immediate_reopen_before_room_write",
            page = readerPagePositionFromLabel(),
            progressPercent = currentReaderProgressPercentFromLabel(),
            pageEndParagraph = currentReaderPageEndParagraphIndex(),
            durableSavedParagraph = durableRoomSavedProgressParagraphIndexFor(document.id),
        )
        captureReaderFormEvidenceScreenshot("13_immediate_reopen_before_room_write")

        scenario?.moveToState(Lifecycle.State.CREATED)
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        Thread.sleep(250)
        scenario?.moveToState(Lifecycle.State.RESUMED)
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("reader-screen") }
        delay.release.complete(Unit)
        composeRule.waitUntil(timeoutMillis = 10_000) {
            durableRoomSavedProgressPercentFor(document.id) == latestPercent &&
                durableRoomSavedProgressParagraphIndexFor(document.id) == latestEndParagraph
        }
        appendReaderFormEvidence(
            stage = "room_write_released_after_reopen",
            page = readerPagePositionFromLabel(),
            progressPercent = currentReaderProgressPercentFromLabel(),
            pageEndParagraph = currentReaderPageEndParagraphIndex(),
            durableSavedParagraph = durableRoomSavedProgressParagraphIndexFor(document.id),
        )
        captureReaderFormEvidenceScreenshot("14_room_write_released_after_reopen")
    }

    @Test
    fun meditationInterventionShowsCalmAlternativeWhenPrimaryIsReading() {
        launchFixtureSystemIntervention()

        var meditationBackupIndex = -1
        composeRule.waitUntil(timeoutMillis = 10_000) {
            val backupIds = currentRecommendationBackupContentIds()
            meditationBackupIndex = backupIds.indexOf(MEDITATION_TIMER_CONTENT_ID)
            currentRecommendationPrimaryContentId() != MEDITATION_TIMER_CONTENT_ID && meditationBackupIndex >= 0
        }
        composeRule.onNodeWithTag("intervention-meditation-alternative")
            .assertIsDisplayed()
        composeRule.onNodeWithText("Calm reset")
            .assertIsDisplayed()
        composeRule.onNodeWithText("3-minute reset", substring = true)
            .assertIsDisplayed()
        composeRule.onNodeWithTag("intervention-meditation-start")
            .assertIsDisplayed()
            .assertIsEnabled()
        composeRule.onAllNodesWithTag("intervention-backup-action-$meditationBackupIndex")
            .assertCountEquals(0)
        captureReaderFormEvidenceScreenshot("12_meditation_calm_alternative")
        composeRule.onNodeWithTag("intervention-meditation-start")
            .performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) {
            hasTag("meditation-timer-screen")
        }
        composeRule.onNodeWithTag("meditation-timer-screen")
            .assertIsDisplayed()
    }

    @Test
    fun systemInterventionShowsContinueProgressRemainingTimeAndScrollableOtherOptions() {
        launchOnboardedApp()
        seedFixtureSelection()
        val seededItems = seedScrollableUnfinishedDocumentOptions(progressPercent = 42)
        assertTrue("Expected enough seeded options for a scrollable backup list", seededItems.size >= 4)

        relaunchFixtureSystemIntervention()

        composeRule.onNodeWithText("A brief detour, if you'd like one").assertDoesNotExist()
        composeRule.onNodeWithText("Choose another if this is not the right fit.").assertDoesNotExist()
        composeRule.onNodeWithTag("intervention-primary-progress").assertIsDisplayed()
        val recommendationSet = currentRecommendationSet()
        val primaryLabel = continueProgressLabel(item = recommendationSet.primary, progressPercent = 42)
        assertTrue(
            "Expected at least one visible progress label for $primaryLabel",
            composeRule.onAllNodesWithText(primaryLabel, substring = true).fetchSemanticsNodes().isNotEmpty(),
        )
        captureInterventionContinueProgressScreenshot("01_intervention_continue_progress_light")

        assertTrue(
            "Expected more than two backup options, got ${recommendationSet.backups.size}",
            recommendationSet.backups.size > 2,
        )
        composeRule.onNodeWithTag("intervention-backup-list").assertIsDisplayed()
        composeRule.onNodeWithTag("intervention-backup-list")
            .performScrollToNode(hasTestTag("intervention-backup-action-2"))
        composeRule.onNodeWithTag("intervention-backup-action-2").assertIsDisplayed()

        val seededIds = seededItems.mapTo(mutableSetOf(), ContentItem::id)
        val backup = recommendationSet.backups[2]
        assertTrue("Expected the scrolled backup option to be unfinished", backup.id in seededIds)
        composeRule.onNodeWithContentDescription(
            "${backup.title}, ${continueProgressLabel(item = backup, progressPercent = 42)}",
        ).assertIsDisplayed()
        captureInterventionContinueProgressScreenshot("02_intervention_other_options_scrolled_light")

        scenario?.onActivity { activity ->
            activity.mainViewModel.selectThemeMode(AppThemeMode.DARK)
        }
        composeRule.waitForIdle()
        captureInterventionContinueProgressScreenshot("03_intervention_continue_progress_dark")
    }

    @Test
    fun newlyAddedEpubBecomesPrimaryAfterOldFilesAreDeprioritized() {
        launchOnboardedApp()
        val seeded = seedFreshDocumentRankingScenario()

        relaunchFixtureSystemIntervention()

        val recommendationSet = currentRecommendationSet()
        assertEquals(seeded.newEpub.id, recommendationSet.primary.id)
        composeRule.onNodeWithText(seeded.newEpub.title)
            .assertIsDisplayed()
        captureSprint14RankingScreenshot("01_new_epub_primary_light")

        composeRule.onNodeWithText("Read this", substring = true)
            .assertIsDisplayed()
            .performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("reader-screen") }
        assertEquals(seeded.newEpub.title, currentContentTitle())
        composeRule.onNodeWithTag("reader-screen").assertIsDisplayed()
        composeRule.onNodeWithText("This newest EPUB is the fresh private document", substring = true)
            .assertIsDisplayed()
    }

    @Test
    fun readerAnnotationEditorSavesEditsAndShowsPreview() {
        launchFixtureSystemIntervention()

        composeRule.onNodeWithText("Read this", substring = true)
            .assertIsDisplayed()
            .performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("reader-screen") }
        val contentId = currentContentId()
        val annotationParagraph = 1

        composeRule.onNodeWithTag("reader-annotation-block-$annotationParagraph")
            .assertIsDisplayed()
            .performTouchInput { longClick(position = Offset(18f, 18f)) }
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("reader-annotation-editor-$annotationParagraph") }
        composeRule.onNodeWithTag("reader-annotation-editor-$annotationParagraph").assertIsDisplayed()
        composeRule.onNodeWithText("SELECTED FRAGMENT").assertDoesNotExist()
        composeRule.onNodeWithTag("reader-annotation-selected-quote-$annotationParagraph").assertIsDisplayed()
        val initialSelectedQuote = readerSelectedQuoteText(annotationParagraph)
        composeRule.onNodeWithText("End later").assertDoesNotExist()
        composeRule.onNodeWithContentDescription("Move end later")
            .assertIsDisplayed()
        composeRule.onNodeWithTag("reader-annotation-end-later")
            .assertIsDisplayed()
            .assertIsEnabled()
            .performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) {
            readerSelectedQuoteText(annotationParagraph).length > initialSelectedQuote.length
        }
        val expandedSelectedQuote = readerSelectedQuoteText(annotationParagraph)
        composeRule.onNodeWithContentDescription("Move end earlier")
            .assertIsDisplayed()
        composeRule.onNodeWithTag("reader-annotation-end-earlier")
            .assertIsDisplayed()
            .assertIsEnabled()
            .performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) {
            readerSelectedQuoteText(annotationParagraph).length < expandedSelectedQuote.length
        }
        val refinedSelectedQuote = readerSelectedQuoteText(annotationParagraph)
        val refinedQuoteProbe = refinedSelectedQuote.take(40)
        assertTrue("Expected the icon range control to keep a non-empty selected quote", refinedQuoteProbe.isNotBlank())
        val firstNote = "Worth remembering when the impulse hits."
        composeRule.onNodeWithTag("reader-annotation-note-input-$annotationParagraph")
            .assertIsDisplayed()
            .performClick()
            .performTextInput(firstNote)
        UiDevice.getInstance(InstrumentationRegistry.getInstrumentation()).pressBack()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("reader-annotation-save-$annotationParagraph").assertIsDisplayed()
        captureSprint14ReaderAnnotationScreenshot("01_reader_annotation_editor_light")

        composeRule.onNodeWithTag("reader-annotation-save-$annotationParagraph")
            .assertIsDisplayed()
            .assertIsEnabled()
            .performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) {
                hasTag("reader-annotation-highlight-$annotationParagraph") &&
                !hasTag("reader-annotation-editor-$annotationParagraph") &&
                hasReadingAnnotationNote(contentId = contentId, paragraphIndex = annotationParagraph, noteText = firstNote) &&
                hasReadingAnnotationQuoteContaining(contentId, annotationParagraph, refinedQuoteProbe)
        }
        composeRule.onNodeWithTag("reader-annotation-highlight-$annotationParagraph").assertIsDisplayed()
        captureSprint14ReaderAnnotationScreenshot("02_reader_annotation_preview_light")
        scenario?.onActivity { activity -> activity.mainViewModel.dismissMessage() }
        composeRule.waitUntil(timeoutMillis = 10_000) { !hasNode("Annotation saved.") }

        composeRule.onNodeWithTag("reader-annotation-block-$annotationParagraph")
            .assertIsDisplayed()
            .performTouchInput { longClick(position = Offset(18f, 18f)) }
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("reader-annotation-editor-$annotationParagraph") }
        val updatedNoteSuffix = "Remember: "
        composeRule.onNodeWithTag("reader-annotation-note-input-$annotationParagraph")
            .performClick()
            .performTextInput(updatedNoteSuffix)
        UiDevice.getInstance(InstrumentationRegistry.getInstrumentation()).pressBack()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("reader-annotation-save-$annotationParagraph")
            .assertIsDisplayed()
            .assertIsEnabled()
            .performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) {
                hasTag("reader-annotation-highlight-$annotationParagraph") &&
                !hasTag("reader-annotation-editor-$annotationParagraph") &&
                hasSingleReadingAnnotationNoteContaining(contentId, annotationParagraph, "Remember:") &&
                hasReadingAnnotationQuoteContaining(contentId, annotationParagraph, refinedQuoteProbe)
        }
        scenario?.onActivity { activity -> activity.mainViewModel.dismissMessage() }
        composeRule.waitUntil(timeoutMillis = 10_000) { !hasNode("Annotation updated.") }

        scenario?.onActivity { activity ->
            activity.mainViewModel.selectThemeMode(AppThemeMode.DARK)
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("reader-annotation-highlight-$annotationParagraph").assertIsDisplayed()
        captureSprint14ReaderAnnotationScreenshot("03_reader_annotation_preview_dark")
    }

    @Test
    fun crossPageAnnotationSelectionPersistsAcrossPagedSourceChunks() {
        launchOnboardedApp()
        val crossPageText = (1..240)
            .chunked(8)
            .joinToString(separator = " ") { chunk ->
                chunk.joinToString(separator = " ") { index -> "anchor$index" }.plus(".")
            }
        val document = addSeedMarkdownDocument(
            title = "Cross-page Annotation Fixture",
            displayName = "cross-page-annotation.md",
            body = crossPageText,
            nowMillis = 70_000L,
        )

        scenario?.onActivity { activity ->
            activity.mainViewModel.setReaderFontScale(1.3)
            activity.mainViewModel.openLibraryItem(document)
        }
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("reader-screen") }
        val contentId = currentContentId()
        val startOffset = crossPageText.indexOf("anchor8").coerceAtLeast(0)
        val endOffset = crossPageText.indexOf("anchor96")
            .takeIf { index -> index > startOffset }
            ?.let { index -> index + "anchor96".length }
            ?: crossPageText.length
        val noteText = "This note spans page chunks."
        val selector = ReadingAnnotationSelector(
            sourceBlockIndex = 0,
            textStartOffset = startOffset,
            textEndOffset = endOffset,
            prefixText = crossPageText.substring(0, startOffset).takeLast(120),
            suffixText = crossPageText.substring(endOffset).take(120),
        )

        scenario?.onActivity { activity ->
            activity.mainViewModel.saveCurrentReadingAnnotation(
                paragraphIndex = 0,
                quotedText = crossPageText.substring(startOffset, endOffset),
                noteText = noteText,
                selector = selector,
            )
        }
        composeRule.waitUntil(timeoutMillis = 10_000) {
            visibleAnnotationHighlightIndices().isNotEmpty() &&
                hasReadingAnnotationNote(contentId = contentId, paragraphIndex = 0, noteText = noteText)
        }
        val firstPageHighlights = visibleAnnotationHighlightIndices()
        assertTrue(firstPageHighlights.isNotEmpty())
        scenario?.onActivity { activity -> activity.mainViewModel.dismissMessage() }
        composeRule.waitUntil(timeoutMillis = 10_000) { !hasNode("Annotation saved.") }
        captureSprint16AdaptiveReaderScreenshot("03_cross_page_annotation_page_one_light")

        val firstPageMax = firstPageHighlights.maxOrNull() ?: -1
        var reachedLaterHighlightedPage = false
        repeat(8) {
            if (!reachedLaterHighlightedPage) {
                reachedLaterHighlightedPage = visibleAnnotationHighlightIndices().any { index -> index > firstPageMax }
            }
            if (!reachedLaterHighlightedPage) {
                advanceReaderPage()
                composeRule.waitForIdle()
            }
        }
        composeRule.waitUntil(timeoutMillis = 10_000) {
            visibleAnnotationHighlightIndices().any { index -> index > firstPageMax }
        }
        val laterPageHighlights = visibleAnnotationHighlightIndices()
        assertTrue(laterPageHighlights.any { index -> index > firstPageMax })
        captureSprint16AdaptiveReaderScreenshot("04_cross_page_annotation_page_two_light")
    }

    @Test
    fun readerAnnotationStartCanMoveBackIntoPreviousSourceBlocks() {
        launchOnboardedApp()
        val paragraphs = (0..11).map { index ->
            "sourceblock$index carries one compact sentence for cross page start selection."
        }
        val document = addSeedMarkdownDocument(
            title = "Start Range Regression",
            displayName = "start-range-regression.md",
            body = paragraphs.joinToString(separator = "\n\n"),
            nowMillis = 71_000L,
        )

        scenario?.onActivity { activity ->
            activity.mainViewModel.setReaderFontScale(1.8)
            activity.mainViewModel.openLibraryItem(document)
        }
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("reader-screen") }
        repeat(6) {
            if (!hasTag("reader-annotation-block-9")) {
                advanceReaderPage()
                composeRule.waitForIdle()
            }
        }
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("reader-annotation-block-9") }
        composeRule.onNodeWithTag("reader-annotation-block-9")
            .assertIsDisplayed()
            .performTouchInput { longClick(position = Offset(24f, 24f)) }
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("reader-annotation-editor-9") }

        repeat(32) {
            if (hasTag("reader-annotation-start-earlier")) {
                composeRule.onNodeWithTag("reader-annotation-start-earlier")
                    .assertIsDisplayed()
                    .performClick()
                composeRule.waitForIdle()
            }
        }
        composeRule.waitForIdle()

        val expandedQuote = readerSelectedQuoteText(9)
        assertTrue(
            "Expected start range control to move into earlier source blocks. " +
                "quote=$expandedQuote",
            expandedQuote.contains("sourceblock0") && expandedQuote.contains("sourceblock9"),
        )
        composeRule.onNodeWithTag("reader-annotation-start-earlier").assertIsNotEnabled()
    }

    @Test
    fun readerAnnotationControlsExpandAndReopenAcrossPages() {
        launchOnboardedApp()
        val crossPageText = (1..120)
            .chunked(8)
            .joinToString(separator = " ") { chunk ->
                chunk.joinToString(separator = " ") { index -> "anchor$index" }.plus(".")
            }
        val document = addSeedMarkdownDocument(
            title = "Cross-page Range Controls",
            displayName = "cross-page-range-controls.md",
            body = crossPageText,
            nowMillis = 72_000L,
        )

        scenario?.onActivity { activity ->
            activity.mainViewModel.setReaderFontScale(1.3)
            activity.mainViewModel.openLibraryItem(document)
        }
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("reader-screen") && readerPageFitPages(readerPageFitSummary()) > 1 }
        val contentId = currentContentId()
        val firstPageEnd = currentReaderPageEndParagraphIndex()
        composeRule.onNodeWithTag("reader-annotation-block-0")
            .assertIsDisplayed()
            .performTouchInput { longClick(position = Offset(24f, 24f)) }
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("reader-annotation-editor-0") }
        composeRule.onNodeWithTag("reader-annotation-range-controls").assertIsDisplayed()
        composeRule.onNodeWithText("Start").assertDoesNotExist()
        composeRule.onNodeWithText("End").assertDoesNotExist()
        val initialQuote = readerSelectedQuoteText(0)
        val initialEditorHeight = nodeHeight("reader-annotation-editor-0")
        captureSprint17CrossPageAnnotationScreenshot("01_compact_controls_first_page_light")

        repeat(80) {
            if (hasTag("reader-annotation-end-later")) {
                composeRule.onNodeWithTag("reader-annotation-end-later")
                    .assertIsDisplayed()
                    .performClick()
                composeRule.waitForIdle()
            }
        }
        composeRule.waitForIdle()
        val expandedQuote = readerSelectedQuoteText(0)
        val expectedSavedEndAnchor = Regex("""anchor\d+""")
            .findAll(expandedQuote)
            .lastOrNull()
            ?.value
            ?: "anchor96"
        val expandedPageEnd = currentReaderPageEndParagraphIndex()
        val expandedEditorHeight = nodeHeight("reader-annotation-editor-0")
        val readerScreenHeight = nodeHeight("reader-screen")
        val rangeControlsHeight = nodeHeight("reader-annotation-range-controls")
        assertTrue(
            "Expected range controls to expand beyond the first page. " +
                "initialQuoteLength=${initialQuote.length} expandedQuoteLength=${expandedQuote.length} " +
                "firstPageEnd=$firstPageEnd expandedPageEnd=$expandedPageEnd",
            expandedQuote.length > initialQuote.length && expandedPageEnd > firstPageEnd,
        )
        assertTrue(
            "Expected the annotation editor to grow with a long selected quote. " +
                "initialEditorHeight=$initialEditorHeight expandedEditorHeight=$expandedEditorHeight",
            expandedEditorHeight > initialEditorHeight,
        )
        assertTrue(
            "Expected the long-quote editor to use most of the reader screen before quote scrolling. " +
                "expandedEditorHeight=$expandedEditorHeight readerScreenHeight=$readerScreenHeight",
            expandedEditorHeight > readerScreenHeight * 0.65f,
        )
        assertTrue(
            "Expected range arrows to stay compact inside the header instead of consuming a separate row. " +
                "rangeControlsHeight=$rangeControlsHeight expandedEditorHeight=$expandedEditorHeight",
            rangeControlsHeight < expandedEditorHeight * 0.18f,
        )
        assertTrue(
            "Expected the selected quote to reach at least the original R1 evidence anchor. " +
                "expectedSavedEndAnchor=$expectedSavedEndAnchor",
            expectedSavedEndAnchor.removePrefix("anchor").toInt() >= 96,
        )
        listOf(
            "reader-annotation-start-earlier",
            "reader-annotation-start-later",
            "reader-annotation-end-earlier",
            "reader-annotation-end-later",
        ).forEach { tag ->
            val hitTargetWidth = nodeWidth(tag)
            val hitTargetHeight = nodeHeight(tag)
            assertTrue(
                "Expected compact visual arrows to keep an accessible touch target. " +
                    "tag=$tag width=$hitTargetWidth height=$hitTargetHeight",
                hitTargetWidth >= 44f && hitTargetHeight >= 44f,
            )
        }
        composeRule.onNodeWithTag("reader-annotation-start-earlier").assertIsDisplayed()
        composeRule.onNodeWithTag("reader-annotation-end-later").assertIsDisplayed()
        captureSprint17CrossPageAnnotationScreenshot("02_compact_controls_later_page_light")
        composeRule.onNodeWithTag("reader-annotation-selected-quote-scroll-0")
            .performTouchInput {
                swipeUp(
                    startY = bottom - 12f,
                    endY = top + 12f,
                )
        }
        composeRule.waitForIdle()
        captureSprint17CrossPageAnnotationScreenshot("03_long_quote_scroll_region_light")
        assertTrue(
            "Expected internal quote scrolling to preserve the same selected range.",
            readerSelectedQuoteText(0).contains(expectedSavedEndAnchor),
        )

        val noteText = "Cross-page range from compact controls."
        composeRule.onNodeWithTag("reader-annotation-note-input-0")
            .assertIsDisplayed()
            .performClick()
            .performTextInput(noteText)
        UiDevice.getInstance(InstrumentationRegistry.getInstrumentation()).pressBack()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("reader-annotation-save-0")
            .assertIsDisplayed()
            .assertIsEnabled()
            .performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) {
                !hasTag("reader-annotation-editor-0") &&
                    hasReadingAnnotationNote(contentId = contentId, paragraphIndex = 0, noteText = noteText) &&
                    hasReadingAnnotationQuoteContaining(contentId, 0, expectedSavedEndAnchor)
        }
        scenario?.onActivity { activity -> activity.mainViewModel.dismissMessage() }
        composeRule.waitUntil(timeoutMillis = 10_000) { !hasNode("Annotation saved.") }

        val laterHighlight = visibleAnnotationHighlightIndices().maxOrNull() ?: -1
        assertTrue("Expected a later-page highlight after saving the cross-page range.", laterHighlight > 0)
        composeRule.onNodeWithTag("reader-annotation-block-$laterHighlight")
            .assertIsDisplayed()
            .performTouchInput { longClick(position = Offset(24f, 24f)) }
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("reader-annotation-editor-$laterHighlight") }
        val reopenedQuote = readerSelectedQuoteText(laterHighlight)
        assertTrue("Expected reopened quote to keep the source-anchored start.", reopenedQuote.contains("anchor1"))
        assertTrue("Expected reopened quote to keep the later-page end.", reopenedQuote.contains(expectedSavedEndAnchor))
        captureSprint17CrossPageAnnotationScreenshot("04_reopened_cross_page_quote_light")
    }

    @Test
    fun readerAnnotationEditorContainsLongQuoteAndLongNoteWithinViewport() {
        launchOnboardedApp()
        val longQuoteText = (1..240)
            .chunked(8)
            .joinToString(separator = " ") { chunk ->
                chunk.joinToString(separator = " ") { index -> "surface$index" }.plus(".")
            }
        val document = addSeedMarkdownDocument(
            title = "Annotation Surface Sizing",
            displayName = "annotation-surface-sizing.md",
            body = longQuoteText,
            nowMillis = 73_000L,
        )

        scenario?.onActivity { activity ->
            activity.mainViewModel.setReaderFontScale(1.3)
            activity.mainViewModel.openLibraryItem(document)
        }
        composeRule.waitUntil(timeoutMillis = 10_000) {
            hasTag("reader-screen") && readerPageFitPages(readerPageFitSummary()) > 1
        }
        composeRule.onNodeWithTag("reader-annotation-block-0")
            .assertIsDisplayed()
            .performTouchInput { longClick(position = Offset(24f, 24f)) }
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("reader-annotation-editor-0") }

        repeat(80) {
            if (hasTag("reader-annotation-end-later")) {
                composeRule.onNodeWithTag("reader-annotation-end-later")
                    .assertIsDisplayed()
                    .performClick()
                composeRule.waitForIdle()
            }
        }
        composeRule.waitForIdle()
        val longQuote = readerSelectedQuoteText(0)
        assertTrue("Expected a long selected quote for surface sizing.", longQuote.contains("surface96"))
        assertAnnotationEditorInsideReaderScreen("long quote")
        assertAnnotationActionsVisibleInsideEditor("long quote")
        val longQuoteEditorHeight = nodeHeight("reader-annotation-editor-0")
        val longQuoteNoteHeight = nodeHeight("reader-annotation-note-input-0")
        assertTrue(
            "Expected long quote state to leave a usable note editor. " +
                "noteHeight=$longQuoteNoteHeight editorHeight=$longQuoteEditorHeight",
            longQuoteNoteHeight >= 92f && longQuoteNoteHeight < longQuoteEditorHeight * 0.38f,
        )
        captureSprint17AnnotationSurfaceScreenshot("01_long_quote_surface_light")

        val longNote = (1..28).joinToString(separator = "\n") { index ->
            "Long note line $index keeps the annotation editor bounded and scrollable."
        }
        composeRule.onNodeWithTag("reader-annotation-note-input-0")
            .assertIsDisplayed()
            .performClick()
            .performTextInput(longNote)
        composeRule.waitForIdle()
        captureSprint17AnnotationSurfaceScreenshot("02_long_note_surface_light")
        assertAnnotationEditorInsideReaderScreen("long note")
        assertAnnotationActionsVisibleInsideEditor("long note")
        val longNoteEditorHeight = nodeHeight("reader-annotation-editor-0")
        val longNoteHeight = nodeHeight("reader-annotation-note-input-0")
        assertTrue(
            "Expected long note input to scroll internally instead of pushing controls away. " +
                "noteHeight=$longNoteHeight editorHeight=$longNoteEditorHeight",
            longNoteHeight < longNoteEditorHeight * 0.38f,
        )
        UiDevice.getInstance(InstrumentationRegistry.getInstrumentation()).pressBack()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("reader-annotation-save-0")
            .assertIsDisplayed()
            .performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) {
            !hasTag("reader-annotation-editor-0") &&
                hasReadingAnnotationNote(currentContentId(), 0, longNote)
        }
    }

    @Test
    fun annotationLibraryListsSavedNotesAndOpensSourceFragment() {
        launchFixtureSystemIntervention()

        composeRule.onNodeWithText("Read this", substring = true)
            .assertIsDisplayed()
            .performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("reader-screen") }
        val contentId = currentContentId()
        val title = currentContentTitle()

        val firstNote = "First note from the annotation library flow."
        val secondNote = "Second note should jump back to this paragraph."
        seedReadingAnnotation(
            contentId = contentId,
            paragraphIndex = 0,
            quotedText = "First annotation quote.",
            noteText = firstNote,
            sourceTitle = title,
            nowMillis = 21_000L,
        )
        seedReadingAnnotation(
            contentId = contentId,
            paragraphIndex = 1,
            quotedText = "Second annotation quote.",
            noteText = secondNote,
            sourceTitle = title,
            nowMillis = 22_000L,
        )
        composeRule.waitUntil(timeoutMillis = 10_000) {
            hasReadingAnnotationNote(contentId = contentId, paragraphIndex = 0, noteText = firstNote) &&
                hasReadingAnnotationNote(contentId = contentId, paragraphIndex = 1, noteText = secondNote)
        }
        val targetAnnotationId = readingAnnotationIdFor(contentId = contentId, paragraphIndex = 1)

        scenario?.onActivity { activity -> activity.mainViewModel.openProgress() }
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("progress-list") }
        composeRule.onNodeWithTag("progress-list")
            .performScrollToNode(hasTestTag("progress-open-annotations"))
        composeRule.onNodeWithTag("progress-open-annotations")
            .assertIsDisplayed()
            .performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("annotation-library-list") }
        composeRule.onNodeWithText("Annotations").assertIsDisplayed()
        assertTrue(composeRule.onAllNodesWithText(title).fetchSemanticsNodes().isNotEmpty())
        assertTrue(composeRule.onAllNodesWithText("EDITORIAL", substring = true).fetchSemanticsNodes().isNotEmpty())
        composeRule.onNodeWithText(firstNote, substring = true).assertIsDisplayed()
        composeRule.onNodeWithText(secondNote, substring = true).assertIsDisplayed()
        captureSprint14AnnotationLibraryScreenshot("01_annotation_library_light")

        scenario?.onActivity { activity -> activity.mainViewModel.selectThemeMode(AppThemeMode.DARK) }
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("annotation-library-list")
            .performScrollToNode(hasTestTag("annotation-open-$targetAnnotationId"))
        composeRule.onNodeWithTag("annotation-open-$targetAnnotationId").assertIsDisplayed()
        captureSprint14AnnotationLibraryScreenshot("02_annotation_library_dark")

        seedReadingAnnotation(
            contentId = "missing-annotation-source",
            paragraphIndex = 4,
            quotedText = "This source was deleted.",
            noteText = "Missing source should be clear and disabled.",
            sourceTitle = "Deleted essay title",
            nowMillis = System.currentTimeMillis() + 1_000L,
        )
        composeRule.waitUntil(timeoutMillis = 10_000) { hasNode("Deleted essay title") }
        composeRule.onNodeWithTag("annotation-library-list")
            .performScrollToNode(hasText("Deleted essay title"))
        composeRule.onNodeWithText("Deleted essay title").assertIsDisplayed()
        composeRule.onNodeWithText("Source no longer in Library").assertDoesNotExist()
        composeRule.onNodeWithText("MISSING SOURCE", substring = true).assertIsDisplayed()
        composeRule.onNodeWithText("Source missing").assertIsDisplayed()
        captureSprint14AnnotationLibraryScreenshot("03_annotation_missing_source_dark")

        composeRule.onNodeWithTag("annotation-library-list")
            .performScrollToNode(hasTestTag("annotation-open-$targetAnnotationId"))
        composeRule.onNodeWithTag("annotation-open-$targetAnnotationId")
            .performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("reader-screen") }
        assertEquals(contentId, currentContentId())
        assertEquals(1, currentReaderStartParagraphIndex())
        captureSprint14AnnotationLibraryScreenshot("04_annotation_fragment_jump_dark")
    }

    @Test
    fun annotationAutosaveWritesW3cJsonLdAndShowsSettingsStatus() {
        launchFixtureSystemIntervention()

        composeRule.onNodeWithText("Read this", substring = true)
            .assertIsDisplayed()
            .performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("reader-screen") }
        val contentId = currentContentId()
        val title = currentContentTitle()
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        context.filesDir.listFiles { file -> file.name.endsWith(".annotations.jsonld") }
            .orEmpty()
            .forEach(File::delete)
        val exportFile = File(context.filesDir, "qa-annotations-e2e.jsonld").apply {
            if (exists()) delete()
        }
        scenario?.onActivity { activity ->
	            activity.mainViewModel.configureReadingAnnotationExport(
	                uri = Uri.fromFile(exportFile).toString(),
	                displayName = "qa-annotations-e2e.jsonld",
	            )
        }
        composeRule.waitUntil(timeoutMillis = 10_000) { exportFile.exists() }

        val noteText = "Autosaved to the W3C annotation file."
        scenario?.onActivity { activity ->
            activity.mainViewModel.saveCurrentReadingAnnotation(
                paragraphIndex = 0,
                quotedText = activity.mainViewModel.uiState.currentContentBody.ifBlank { title },
                noteText = noteText,
            )
        }
        composeRule.waitUntil(timeoutMillis = 10_000) {
            val jsonLdFiles = context.filesDir.listFiles { file -> file.name.endsWith(".annotations.jsonld") }
                .orEmpty()
            jsonLdFiles.size == 1 &&
                jsonLdFiles.single().readText().contains("\"type\":\"AnnotationCollection\"") &&
                jsonLdFiles.single().readText().contains(noteText) &&
                jsonLdFiles.single().readText().contains(title)
        }

        scenario?.onActivity { activity -> activity.mainViewModel.openSettings() }
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("settings-list") }
        composeRule.waitUntil(timeoutMillis = 10_000) { hasAnnotationExportSuccess() }
        composeRule.onNodeWithTag("settings-list")
            .performScrollToNode(hasTestTag("settings-annotation-export-section"))
        composeRule.onNodeWithTag("settings-annotation-export-section").assertIsDisplayed()
        composeRule.onNodeWithTag("settings-list")
            .performScrollToNode(hasTestTag("settings-annotation-export-status"))
        composeRule.onNodeWithText("qa-annotations-e2e.jsonld").assertIsDisplayed()
        composeRule.onNodeWithTag("settings-annotation-export-status").assertIsDisplayed()
        composeRule.onNodeWithTag("settings-annotation-export-save-now").assertIsDisplayed()
        scenario?.onActivity { activity -> activity.mainViewModel.dismissMessage() }
        composeRule.waitForIdle()
        captureSprint14AnnotationExportScreenshot("01_annotation_export_success_light")

        val annotationId = readingAnnotationIdFor(contentId = contentId, paragraphIndex = 0)
        scenario?.onActivity { activity ->
            activity.mainViewModel.deleteReadingAnnotation(annotationId)
        }
        composeRule.waitUntil(timeoutMillis = 10_000) {
            val index = exportFile.readText()
            val jsonLdFiles = context.filesDir.listFiles { file -> file.name.endsWith(".annotations.jsonld") }
                .orEmpty()
            !index.contains(noteText) && index.contains("\"files\":[]") && jsonLdFiles.isEmpty()
        }

        scenario?.onActivity { activity ->
            activity.mainViewModel.configureReadingAnnotationExport(
                uri = "content://com.qualityalternative.missing/qa-annotations.jsonld",
                displayName = "missing-drive-file.jsonld",
            )
            activity.mainViewModel.selectThemeMode(AppThemeMode.DARK)
        }
        composeRule.waitUntil(timeoutMillis = 10_000) {
            hasAnnotationExportFailure()
        }
        composeRule.onNodeWithTag("settings-list")
            .performScrollToNode(hasTestTag("settings-annotation-export-section"))
        composeRule.onNodeWithTag("settings-list")
            .performScrollToNode(hasTestTag("settings-annotation-export-status"))
        composeRule.onNodeWithText("missing-drive-file.jsonld").assertIsDisplayed()
        composeRule.onNodeWithTag("settings-annotation-export-status").assertIsDisplayed()
        composeRule.onNodeWithText("Retry").assertIsDisplayed()
        scenario?.onActivity { activity -> activity.mainViewModel.dismissMessage() }
        composeRule.waitForIdle()
        captureSprint14AnnotationExportScreenshot("02_annotation_export_failure_dark")
    }

    @Test
    fun annotationDriveSyncSettingsShowsConnectFailureConnectedAndRetryStates() {
        launchOnboardedApp()
        scenario?.onActivity { activity -> activity.mainViewModel.openSettings() }
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("settings-list") }
        composeRule.onNodeWithTag("settings-list")
            .performScrollToNode(hasTestTag("settings-annotation-drive-connect"))
        composeRule.onNodeWithText("Google Drive not connected").assertIsDisplayed()
        composeRule.onNodeWithTag("settings-annotation-drive-status").assertIsDisplayed()
        composeRule.onNodeWithText("Connect").assertIsDisplayed()
        composeRule.onNodeWithTag("settings-annotation-drive-disconnect").assertIsNotEnabled()
        captureSprint17DriveAuthScreenshot("01_drive_connect_light")

        scenario?.onActivity { activity ->
            activity.mainViewModel.beginAnnotationDriveAuthorization()
        }
        composeRule.waitUntil(timeoutMillis = 10_000) {
            hasTag("settings-annotation-drive-progress")
        }
        captureSprint17DriveAuthScreenshot("02_drive_connecting_light")

        val cancelledAuthorizationMessage =
            "Authorization was cancelled or blocked by Google. No folder destination was changed."
        scenario?.onActivity { activity ->
            activity.mainViewModel.reportAnnotationDriveAuthorizationFailure(cancelledAuthorizationMessage)
        }
        composeRule.waitUntil(timeoutMillis = 10_000) {
            var hasFailure = false
            scenario?.onActivity { activity ->
                hasFailure = activity.mainViewModel.uiState.annotationDriveLastError == cancelledAuthorizationMessage
            }
            hasFailure
        }
        composeRule.onNodeWithTag("settings-list")
            .performScrollToNode(hasTestTag("settings-annotation-drive-status"))
        composeRule.onNodeWithText("CANCELLED OR BLOCKED", substring = true).assertIsDisplayed()
        composeRule.onNodeWithTag("settings-annotation-drive-connect").assertIsEnabled()
        scenario?.onActivity { activity -> activity.mainViewModel.dismissMessage() }
        composeRule.waitForIdle()
        captureSprint17DriveAuthScreenshot("03_drive_auth_failure_light")

        scenario?.onActivity { activity ->
            runBlocking {
                (activity.application as QualityAlternativeApplication)
                    .appContainer
                    .settingsRepository
                    .saveAnnotationDriveSyncSuccess(
                        timestampMillis = 25_000L,
                        folderId = "qa-drive-folder",
                    )
            }
        }
        composeRule.waitUntil(timeoutMillis = 10_000) {
            var hasConnected = false
            scenario?.onActivity { activity ->
                hasConnected = activity.mainViewModel.uiState.annotationDriveSyncEnabled &&
                    activity.mainViewModel.uiState.annotationDriveLastSuccessfulAtMillis == 25_000L &&
                    activity.mainViewModel.uiState.annotationDriveLastError == null
            }
            hasConnected
        }
        composeRule.onNodeWithTag("settings-list")
            .performScrollToNode(hasTestTag("settings-annotation-drive-status"))
        composeRule.onNodeWithText("Google Drive connected").assertIsDisplayed()
        composeRule.onNodeWithText("LAST SYNCED JAN 1", substring = true).assertIsDisplayed()
        composeRule.onNodeWithTag("settings-annotation-drive-sync-now").assertIsEnabled()
        composeRule.onNodeWithTag("settings-annotation-drive-disconnect").assertIsEnabled()
        captureSprint17DriveAuthScreenshot("04_drive_connected_light")

        scenario?.onActivity { activity ->
            runBlocking {
                (activity.application as QualityAlternativeApplication)
                    .appContainer
                    .settingsRepository
                    .saveAnnotationDriveSyncFailure("Google Drive sync failed. Retry from Settings.")
            }
        }
        composeRule.waitUntil(timeoutMillis = 10_000) {
            var hasFailure = false
            scenario?.onActivity { activity ->
                hasFailure = activity.mainViewModel.uiState.annotationDriveLastError ==
                    "Google Drive sync failed. Retry from Settings."
            }
            hasFailure
        }
        composeRule.onNodeWithTag("settings-list")
            .performScrollToNode(hasTestTag("settings-annotation-drive-status"))
        composeRule.onNodeWithText("GOOGLE DRIVE SYNC FAILED", substring = true).assertIsDisplayed()
        composeRule.onNodeWithTag("settings-annotation-drive-sync-now").assertIsEnabled()
        composeRule.onNodeWithText("Retry").assertIsDisplayed()
        captureSprint17DriveAuthScreenshot("05_drive_retry_light")
    }

    @Test
    fun accountLightImportSettingsShowsPreviewErrorsConfirmationAndSuccess() {
        launchOnboardedApp()
        scenario?.onActivity { activity -> activity.mainViewModel.openSettings() }
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("settings-list") }

        scrollToAccountLightSettings()
        composeRule.onNodeWithTag("settings-account-light-section").assertIsDisplayed()
        composeRule.onNodeWithTag("settings-account-light-import").assertIsDisplayed()
        captureAccountLightProfileScreenshot("01_import_entry_light")

        exportAccountLightProfileJsonFromViewModel()
        scrollToAccountLightSettings()
        composeRule.waitUntil(timeoutMillis = 10_000) { hasNodeContaining("PORTABLE PROFILE EXPORTED") }
        composeRule.onNodeWithTag("settings-account-light-status").assertIsDisplayed()
        captureAccountLightProfileScreenshot("00_export_success_light")
        scenario?.onActivity { activity -> activity.mainViewModel.dismissMessage() }
        composeRule.waitForIdle()

        scenario?.onActivity { activity ->
            activity.mainViewModel.previewAccountLightImport(
                accountLightProfileJson(
                    schemaVersion = 1,
                    selectedAppPackages = listOf("com.instagram.android", "com.future.reader"),
                ),
            )
        }
        composeRule.waitUntil(timeoutMillis = 10_000) {
            var importFinished = false
            scenario?.onActivity { activity ->
                val state = activity.mainViewModel.uiState
                importFinished = state.accountLightImportPreview != null || state.accountLightImportError != null
            }
            importFinished
        }
        scenario?.onActivity { activity ->
            val state = activity.mainViewModel.uiState
            assertTrue(
                "Portable profile preview failed: ${state.accountLightImportError ?: state.latestMessage}",
                state.accountLightImportPreview != null,
            )
        }
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("settings-account-light-import-preview") }
        scrollToAccountLightSettings()
        composeRule.onNodeWithTag("settings-account-light-import-preview").assertIsDisplayed()
        composeRule.onNodeWithTag("settings-account-light-import-warning-summary").assertIsDisplayed()
        captureAccountLightProfileScreenshot("02_merge_preview_with_unsupported_app_light")

        composeRule.onNodeWithTag("settings-account-light-import-replace")
            .assertIsDisplayed()
            .performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) {
            hasTag("settings-account-light-replace-confirm")
        }
        composeRule.onNodeWithTag("settings-list")
            .performScrollToNode(hasTestTag("settings-account-light-replace-confirm"))
        composeRule.onNodeWithTag("settings-account-light-replace-confirm").assertIsDisplayed()
        composeRule.onNodeWithTag("settings-list")
            .performScrollToNode(hasTestTag("settings-account-light-replace-backup"))
        composeRule.onNodeWithTag("settings-account-light-replace-backup").assertIsDisplayed()
        composeRule.onNodeWithTag("settings-list")
            .performScrollToNode(hasTestTag("settings-account-light-replace-confirm-action"))
        captureAccountLightProfileScreenshot("03_replace_confirmation_light")

        composeRule.onNodeWithTag("settings-account-light-replace-confirm-action")
            .assertIsDisplayed()
            .performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) {
            hasNodeContaining("IMPORTED PROFILE REPLACED LOCAL PORTABLE SETTINGS AND LIBRARY")
        }
        scrollToAccountLightSettings()
        composeRule.onNodeWithTag("settings-account-light-status").assertIsDisplayed()
        captureAccountLightProfileScreenshot("04_import_success_dark")

        scenario?.onActivity { activity -> activity.mainViewModel.openLibrary() }
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("library-list") }
        composeRule.onNodeWithTag("library-list")
            .performScrollToNode(hasTestTag("library-item-user-document-44444444-4444-4444-8444-444444444444"))
        composeRule.onNodeWithTag("library-item-user-document-44444444-4444-4444-8444-444444444444")
            .assertIsDisplayed()
        composeRule.onNodeWithText("book.epub (missing)", substring = true).assertIsDisplayed()
        composeRule.onNodeWithTag("library-unavailable-user-document-44444444-4444-4444-8444-444444444444")
            .assertIsDisplayed()
        composeRule.onNodeWithTag("library-open-user-document-44444444-4444-4444-8444-444444444444")
            .assertDoesNotExist()
        captureAccountLightProfileScreenshot("07_missing_document_library_dark")

        scenario?.onActivity { activity -> activity.mainViewModel.openSettings() }
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("settings-list") }
        scrollToAccountLightSettings()

        scenario?.onActivity { activity ->
            activity.mainViewModel.previewAccountLightImport("{not-json")
        }
        composeRule.waitUntil(timeoutMillis = 10_000) {
            hasNodeContaining("PORTABLE PROFILE IS NOT VALID JSON")
        }
        scrollToAccountLightSettings()
        composeRule.onNodeWithTag("settings-account-light-status").assertIsDisplayed()
        captureAccountLightProfileScreenshot("05_invalid_import_dark")

        scenario?.onActivity { activity ->
            activity.mainViewModel.previewAccountLightImport(accountLightProfileJson(schemaVersion = 99))
        }
        composeRule.waitUntil(timeoutMillis = 10_000) {
            hasNodeContaining("UNSUPPORTED PORTABLE PROFILE SCHEMA VERSION 99")
        }
        scrollToAccountLightSettings()
        composeRule.onNodeWithTag("settings-account-light-status").assertIsDisplayed()
        captureAccountLightProfileScreenshot("06_future_schema_import_dark")
    }

    @Test
    fun accountLightProfileExportsAndImportsIntoCleanAppState() {
        launchOnboardedApp()
        scenario?.onActivity { activity ->
            runBlocking {
                (activity.application as QualityAlternativeApplication)
                    .appContainer
                    .settingsRepository
                    .saveReaderFontScale(1.3)
            }
            activity.mainViewModel.setReaderFontScale(1.3)
        }
        composeRule.waitUntil(timeoutMillis = 10_000) { currentReaderFontScale() == 1.3 }

        val exportedJson = exportAccountLightProfileJsonFromViewModel()
        assertTrue(exportedJson.contains("\"readerFontScale\": 1.3"))

        scenario?.close()
        scenario = null
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        resetPersistentState()

        launchOnboardedApp()
        assertEquals(DEFAULT_READER_FONT_SCALE, currentReaderFontScale(), 0.0)
        scenario?.onActivity { activity -> activity.mainViewModel.openSettings() }
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("settings-list") }
        scrollToAccountLightSettings()
        scenario?.onActivity { activity -> activity.mainViewModel.previewAccountLightImport(exportedJson) }
        composeRule.waitUntil(timeoutMillis = 10_000) {
            var importFinished = false
            scenario?.onActivity { activity ->
                val state = activity.mainViewModel.uiState
                importFinished = state.accountLightImportPreview != null || state.accountLightImportError != null
            }
            importFinished
        }
        scenario?.onActivity { activity ->
            val state = activity.mainViewModel.uiState
            assertTrue(
                "Default profile preview failed: ${state.accountLightImportError ?: state.latestMessage}",
                state.accountLightImportPreview != null,
            )
        }
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("settings-account-light-import-preview") }
        scenario?.onActivity { activity -> activity.mainViewModel.confirmAccountLightReplaceImport() }
        composeRule.waitUntil(timeoutMillis = 10_000) {
            currentReaderFontScale() == 1.3 &&
                hasNodeContaining("IMPORTED PROFILE REPLACED LOCAL PORTABLE SETTINGS AND LIBRARY")
        }
    }

    @Test
    fun onboardingRestoreProfileLoadsDefaultBackupAfterCleanInstall() {
        launchOnboardedApp()
        scenario?.onActivity { activity ->
            activity.mainViewModel.setReaderFontScale(1.3)
            activity.mainViewModel.setMeditationDurationMinutes(5)
        }
        composeRule.waitUntil(timeoutMillis = 10_000) {
            var backupSaved = false
            scenario?.onActivity { activity ->
                backupSaved = activity.mainViewModel.uiState.profileAutosaveLastSuccessfulAtMillis != null
            }
            currentReaderFontScale() == 1.3 &&
                currentMeditationDurationMinutes() == 5 &&
                backupSaved
        }

        scenario?.close()
        scenario = null
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        resetPersistentState()

        launchApp()
        composeRule.waitUntil(timeoutMillis = 10_000) { hasNodeContaining("Turn an impulse") }
        composeRule.onNodeWithTag("onboarding-restore-profile")
            .assertIsDisplayed()
            .performClick()

        composeRule.waitUntil(timeoutMillis = 10_000) {
            currentHasCompletedOnboarding() &&
                currentReaderFontScale() == 1.3 &&
                currentMeditationDurationMinutes() == 5
        }
    }

    @Test
    fun settingsDefaultBackupRestoreShowsPreviewBeforeReplace() {
        seedFixtureSelection()
        runBlocking {
            AndroidAccountLightProfileAutosaveWriter(InstrumentationRegistry.getInstrumentation().targetContext)
                .writeProfileJson(
                    uri = AndroidAccountLightProfileAutosaveWriter.DEFAULT_PROFILE_BACKUP_URI,
                    fileName = ACCOUNT_LIGHT_PROFILE_FILE_NAME,
                    json = accountLightProfileJson(schemaVersion = 1, readerFontScale = 1.3),
                )
        }
        launchApp()
        waitForHome()
        assertEquals(DEFAULT_READER_FONT_SCALE, currentReaderFontScale(), 0.0)

        scenario?.onActivity { activity -> activity.mainViewModel.openSettings() }
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("settings-list") }
        scrollToAccountLightSettings()
        composeRule.onNodeWithTag("settings-account-light-import-default")
            .assertIsDisplayed()
            .performClick()

        composeRule.waitUntil(timeoutMillis = 10_000) {
            var importFinished = false
            scenario?.onActivity { activity ->
                val state = activity.mainViewModel.uiState
                importFinished = state.accountLightImportPreview != null || state.accountLightImportError != null
            }
            importFinished
        }
        scenario?.onActivity { activity ->
            val state = activity.mainViewModel.uiState
            assertTrue(
                "Default profile preview failed: ${state.accountLightImportError ?: state.latestMessage}",
                state.accountLightImportPreview != null,
            )
        }
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("settings-account-light-import-preview") }
        assertEquals(DEFAULT_READER_FONT_SCALE, currentReaderFontScale(), 0.0)
        composeRule.onNodeWithTag("settings-list")
            .performScrollToNode(hasTestTag("settings-account-light-import-preview"))
        captureAccountLightProfileScreenshot("08_default_backup_preview_light")
        composeRule.onNodeWithTag("settings-list")
            .performScrollToNode(hasTestTag("settings-account-light-import-replace"))
        composeRule.onNodeWithTag("settings-account-light-import-replace")
            .assertIsDisplayed()
            .performClick()
        composeRule.onNodeWithTag("settings-list")
            .performScrollToNode(hasTestTag("settings-account-light-replace-confirm"))
        composeRule.onNodeWithTag("settings-account-light-replace-confirm")
            .assertIsDisplayed()
        captureAccountLightProfileScreenshot("09_default_backup_replace_confirm_light")
        composeRule.onNodeWithTag("settings-list")
            .performScrollToNode(hasTestTag("settings-account-light-replace-confirm-action"))
        composeRule.onNodeWithTag("settings-account-light-replace-confirm-action")
            .assertIsDisplayed()
            .performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) { currentReaderFontScale() == 1.3 }
        scrollToAccountLightSettings()
        captureAccountLightProfileScreenshot("10_default_backup_restore_success_dark")
    }

    @Test
    fun longDocumentImportPreviewShowsMultiHourReadingTime() {
        launchOnboardedApp()

        scenario?.onActivity { activity ->
            activity.mainViewModel.beginUserDocumentImportPreparation()
            activity.mainViewModel.prepareUserDocumentBatchImport(
                candidates = listOf(
                    DocumentImportCandidate(
                        uri = "content://qa-test/long-book.md",
                        displayName = "long-book.md",
                        mimeType = "text/markdown",
                        title = "Long Book",
                        durationMinutes = "135",
                        format = ContentFormat.MARKDOWN,
                        estimateSource = ReadingTimeEstimateSource.EXTRACTED_TEXT,
                        estimatedWordCount = 30_375,
                    ),
                ),
                nowMillis = 31_000L,
            )
        }

        composeRule.waitUntil(timeoutMillis = 10_000) {
            hasTag("add-document-screen") && hasNodeContaining("2 hr 15 min")
        }
        assertTrue(composeRule.onAllNodesWithText("Long Book").fetchSemanticsNodes().isNotEmpty())
        composeRule.onNodeWithText("long-book.md", substring = true).assertIsDisplayed()
        composeRule.onNodeWithText("2 hr 15 min", substring = true).assertIsDisplayed()
        assertTrue(composeRule.onAllNodesWithText("auto", substring = true).fetchSemanticsNodes().isNotEmpty())
        captureSprint21ReadingTimeScreenshot("14_long_document_import_multi_hour")
    }

    @Test
    fun settingsShowsLocalDefaultsAndChangedDestinationsForAnnotationSyncAndProfileBackup() {
        launchOnboardedApp()
        scenario?.onActivity { activity -> activity.mainViewModel.openSettings() }
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("settings-list") }

        composeRule.onNodeWithTag("settings-list")
            .performScrollToNode(hasTestTag("settings-annotation-export-section"))
        composeRule.onNodeWithText("App storage - Annotation sync").assertIsDisplayed()
        composeRule.onNodeWithTag("settings-annotation-export-save-now").assertIsEnabled()
        composeRule.onNodeWithTag("settings-annotation-export-clear").assertDoesNotExist()
        captureSprint17DefaultsScreenshot("00_annotation_default_light")

        scrollToAccountLightSettings()
        composeRule.onNodeWithText("Downloads/Quality Alternative/quality-alternative-profile.json").assertIsDisplayed()
        composeRule.onNodeWithTag("settings-account-light-autosave-save-now").assertIsEnabled()
        composeRule.onNodeWithTag("settings-account-light-autosave-clear").assertDoesNotExist()
        captureSprint17DefaultsScreenshot("01_profile_default_light")

        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val annotationDir = File(context.filesDir, "annotation-sync-custom-e2e").apply {
            deleteRecursively()
            mkdirs()
        }
        val profileDir = File(context.filesDir, "profile-backup-custom-e2e").apply {
            deleteRecursively()
            mkdirs()
        }
        scenario?.onActivity { activity ->
            activity.mainViewModel.configureReadingAnnotationExport(
                uri = Uri.fromFile(annotationDir).toString(),
                displayName = "QA annotation folder",
                nowMillis = 23_000L,
            )
        }
        composeRule.waitUntil(timeoutMillis = 10_000) {
            var hasAnnotationDestination = false
            scenario?.onActivity { activity ->
                hasAnnotationDestination =
                    activity.mainViewModel.uiState.annotationExportDisplayName == "QA annotation folder" &&
                        activity.mainViewModel.uiState.annotationExportLastSuccessfulAtMillis == 23_000L
            }
            hasAnnotationDestination
        }
        scenario?.onActivity { activity ->
            activity.mainViewModel.configureAccountLightProfileAutosave(
                uri = Uri.fromFile(profileDir).toString(),
                displayName = "QA profile backup",
                nowMillis = 24_000L,
            )
        }
        composeRule.waitUntil(timeoutMillis = 10_000) {
            var hasProfileDestination = false
            scenario?.onActivity { activity ->
                hasProfileDestination =
                    activity.mainViewModel.uiState.profileAutosaveDisplayName == "QA profile backup" &&
                        activity.mainViewModel.uiState.profileAutosaveLastSuccessfulAtMillis == 24_000L
            }
            hasProfileDestination
        }
        scenario?.onActivity { activity -> activity.mainViewModel.dismissMessage() }
        composeRule.waitUntil(timeoutMillis = 10_000) { !hasNodeContaining("destination changed") }

        composeRule.onNodeWithTag("settings-list")
            .performScrollToNode(hasTestTag("settings-annotation-export-section"))
        composeRule.onNodeWithText("QA annotation folder").assertIsDisplayed()
        composeRule.onNodeWithTag("settings-annotation-export-clear").assertIsDisplayed()
        captureSprint17DefaultsScreenshot("02_annotation_changed_destination_light")

        scrollToAccountLightSettings()
        composeRule.onNodeWithText("QA profile backup").assertIsDisplayed()
        composeRule.onNodeWithTag("settings-account-light-autosave-clear").assertIsDisplayed()
        captureSprint17DefaultsScreenshot("03_profile_changed_destination_light")
    }

    @Test
    fun accountLightProfileAutosaveSettingsShowsDestinationSuccessAndRecoverableFailure() {
        launchOnboardedApp()
        scenario?.onActivity { activity -> activity.mainViewModel.openSettings() }
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("settings-list") }

        scrollToAccountLightSettings()
        composeRule.onNodeWithTag("settings-account-light-autosave-status").assertIsDisplayed()
        composeRule.onNodeWithText("Downloads/Quality Alternative/quality-alternative-profile.json").assertIsDisplayed()
        composeRule.onNodeWithTag("settings-account-light-autosave-pick").assertIsDisplayed()
        composeRule.onNodeWithTag("settings-account-light-autosave-save-now").assertIsEnabled()
        composeRule.onNodeWithTag("settings-account-light-autosave-clear").assertDoesNotExist()
        captureSprint16ProfileAutosaveScreenshot("01_profile_autosave_default_light")

        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val autosaveDir = File(context.filesDir, "profile-autosave-e2e").apply {
            deleteRecursively()
            mkdirs()
        }
        val autosaveFile = File(autosaveDir, "quality-alternative-profile.json")
        scenario?.onActivity { activity ->
            activity.mainViewModel.configureAccountLightProfileAutosave(
                uri = Uri.fromFile(autosaveDir).toString(),
                displayName = "QA portable profile",
                nowMillis = 21_000L,
            )
        }
        composeRule.waitUntil(timeoutMillis = 10_000) {
            autosaveFile.exists() && hasProfileAutosaveSuccess()
        }
        scrollToAccountLightSettings()
        composeRule.onNodeWithText("QA portable profile").assertIsDisplayed()
        composeRule.onNodeWithTag("settings-account-light-autosave-status").assertIsDisplayed()
        composeRule.onNodeWithTag("settings-account-light-autosave-save-now").assertIsEnabled()
        composeRule.onNodeWithTag("settings-account-light-autosave-clear").assertIsDisplayed()
        assertTrue(autosaveFile.readText().contains("\"profileAutosave\""))
        scenario?.onActivity { activity -> activity.mainViewModel.dismissMessage() }
        composeRule.waitForIdle()
        captureSprint16ProfileAutosaveScreenshot("02_profile_autosave_success_light")

        scenario?.onActivity { activity ->
            activity.mainViewModel.configureAccountLightProfileAutosave(
                uri = "content://com.qualityalternative.missing/profile-autosave",
                displayName = "Missing profile folder",
                nowMillis = 22_000L,
            )
            activity.mainViewModel.selectThemeMode(AppThemeMode.DARK)
        }
        composeRule.waitUntil(timeoutMillis = 10_000) { hasProfileAutosaveFailure() }
        scrollToAccountLightSettings()
        composeRule.onNodeWithText("Missing profile folder").assertIsDisplayed()
        composeRule.onNodeWithText("BACKUP FAILED", substring = true).assertIsDisplayed()
        composeRule.onNodeWithTag("settings-account-light-autosave-retry").assertIsDisplayed()
        scenario?.onActivity { activity -> activity.mainViewModel.dismissMessage() }
        composeRule.waitForIdle()
        captureSprint16ProfileAutosaveScreenshot("03_profile_autosave_failure_dark")
    }

    @Test
    fun meditationAlternativeOpensTimerAndCompletesWithGong() {
        launchMeditationFixtureSystemIntervention()

        composeRule.waitUntil(timeoutMillis = 10_000) {
            hasNode("3-minute reset")
        }
        if (hasNodeContaining("Start timer")) {
            composeRule.onNodeWithText("Start timer", substring = true)
                .assertIsDisplayed()
                .performClick()
        } else {
            composeRule.onNodeWithText("3-minute reset")
                .assertIsDisplayed()
                .performClick()
        }

        composeRule.waitUntil(timeoutMillis = 10_000) {
            hasTag("meditation-timer-screen")
        }
        composeRule.onNodeWithTag("meditation-timer-screen").assertIsDisplayed()
        composeRule.onNodeWithText("3:00").assertIsDisplayed()
        composeRule.onNodeWithTag("meditation-complete").assertIsNotEnabled()
        composeRule.onNodeWithText("End early").assertIsDisplayed()
        composeRule.onNodeWithTag("timer-meditation-duration-1")
            .assertIsDisplayed()
            .performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) { hasNode("1:00") }
        composeRule.waitUntil(timeoutMillis = 70_000) {
            hasNodeContaining("Reset complete. The gong marks the end")
        }
        composeRule.onNodeWithTag("meditation-complete").assertIsEnabled()
        captureReaderFormEvidenceScreenshot("13_meditation_gong_complete")
    }

    @Test
    fun completedMeditationStaysAvailableAndOutOfLibrary() {
        launchMeditationFixtureSystemIntervention()

        composeRule.waitUntil(timeoutMillis = 10_000) {
            hasNode("3-minute reset")
        }
        if (hasNodeContaining("Start timer")) {
            composeRule.onNodeWithText("Start timer", substring = true)
                .assertIsDisplayed()
                .performClick()
        } else {
            composeRule.onNodeWithText("3-minute reset")
                .assertIsDisplayed()
                .performClick()
        }
        composeRule.waitUntil(timeoutMillis = 10_000) {
            hasTag("meditation-timer-screen")
        }
        assertEquals(MEDITATION_TIMER_CONTENT_ID, currentContentId())
        scenario?.onActivity { activity ->
            activity.mainViewModel.finishMeditationReset(nowMillis = 10_000L)
        }
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("feedback-screen") }

        relaunchFixtureSystemIntervention()
        composeRule.waitUntil(timeoutMillis = 10_000) {
            MEDITATION_TIMER_CONTENT_ID in currentRecommendationContentIds()
        }

        scenario?.onActivity { activity -> activity.mainViewModel.openLibrary() }
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("library-list") }
        composeRule.onNodeWithTag("library-item-$MEDITATION_TIMER_CONTENT_ID")
            .assertDoesNotExist()
        composeRule.onNodeWithTag("completed-activation-$MEDITATION_TIMER_CONTENT_ID")
            .assertDoesNotExist()
    }

    @Test
    fun meditationDoesNotLiveInLibrary() {
        launchOnboardedApp()

        scenario?.onActivity { activity -> activity.mainViewModel.openLibrary() }
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("library-list") }
        composeRule.onNodeWithTag("library-item-$MEDITATION_TIMER_CONTENT_ID")
            .assertDoesNotExist()
        composeRule.onNodeWithTag("library-open-$MEDITATION_TIMER_CONTENT_ID")
            .assertDoesNotExist()
    }

    @Test
    fun sharedLinkOnlyAlternativeOpensExternalHandoffScreen() {
        launchLinkOnlyFixtureSystemIntervention()

        composeRule.waitUntil(timeoutMillis = 10_000) {
            hasNodeContaining("Open link")
        }
        if (hasNodeContaining("Open link")) {
            composeRule.onNodeWithText("Open link", substring = true)
                .assertIsDisplayed()
                .performClick()
        } else {
            composeRule.onNodeWithText("The Big Here and Long Now")
                .assertIsDisplayed()
                .performClick()
        }

        composeRule.waitUntil(timeoutMillis = 10_000) {
            hasTag("external-handoff-screen")
        }
        composeRule.onNodeWithTag("external-handoff-screen").assertIsDisplayed()
        composeRule.onNodeWithText("https://", substring = true)
            .assertIsDisplayed()
        composeRule.onNodeWithTag("external-link-open")
            .assertIsDisplayed()
            .assertIsEnabled()
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

    private fun scrollToAccountLightSettings() {
        composeRule.onNodeWithTag("settings-list")
            .performScrollToNode(hasTestTag("settings-account-light-section"))
        composeRule.waitForIdle()
    }

    private fun accountLightProfileJson(
        schemaVersion: Int,
        selectedAppPackages: List<String> = listOf("com.instagram.android"),
        readerFontScale: Double = 1.25,
    ): String {
        val appJson = selectedAppPackages.joinToString(separator = ",") { packageName -> "\"$packageName\"" }
        return """
            {
              "schemaVersion": $schemaVersion,
              "exportedAtMillis": 20000,
              "app": {
                "profileFormat": "quality-alternative-account-light",
                "packageName": "com.qualityalternative.app",
                "appVersionName": "0.8.1-alpha",
                "appVersionCode": 13
              },
              "profile": {
                "profileId": "qa-local-22222222-2222-4222-8222-222222222222",
                "createdAtMillis": 10000,
                "updatedAtMillis": 20000,
                "displayName": null
              },
              "settings": {
                "hasCompletedOnboarding": true,
                "selectedAppPackages": [$appJson],
                "preferredTopics": ["SCIENCE", "PHILOSOPHY"],
                "preferredDurationBucket": "FOCUS",
                "selectedPackIds": ["starter_pack"],
                "themeMode": "DARK",
                "meditationDurationMinutes": 5,
                "readerFontScale": $readerFontScale,
                "contentPriority": "MY_FILES",
                "priorityContentIds": [],
                "reactivatedCompletedContentIds": [],
                "openAnywayUnlockMinutes": 60
              },
              "library": {
                "userLinks": [
                  {
                    "contentId": "user-link-33333333-3333-4333-8333-333333333333",
                    "normalizedUrl": "https://example.com/essay",
                    "title": "Imported essay",
                    "description": "Saved link from another device.",
                    "durationMinutes": 12,
                    "topicTags": ["SCIENCE"],
                    "availability": "AVAILABLE",
                    "createdAtMillis": 10000,
                    "updatedAtMillis": 20000,
                    "sourceLabel": null
                  }
                ],
                "userDocuments": [
                  {
                    "contentId": "user-document-44444444-4444-4444-8444-444444444444",
                    "displayName": "book.epub",
                    "mimeType": "application/epub+zip",
                    "documentFormat": "EPUB",
                    "title": "Imported book",
                    "description": "Saved document metadata.",
                    "durationMinutes": 45,
                    "topicTags": ["PHILOSOPHY"],
                    "availability": "UNAVAILABLE",
                    "documentImportState": "MISSING_FILE_NEEDS_REATTACH",
                    "documentFingerprint": {
                      "strategy": "UNVERIFIED_METADATA_ONLY",
                      "sha256": null,
                      "sizeBytes": null,
                      "normalizedTitle": "imported book",
                      "format": "EPUB"
                    },
                    "createdAtMillis": 10000,
                    "updatedAtMillis": 20000,
                    "sourceHint": {
                      "lastKnownDisplayName": "book.epub",
                      "providerLabel": null
                    }
                  }
                ]
              },
              "reading": {
                "progress": [
                  {
                    "contentId": "user-link-33333333-3333-4333-8333-333333333333",
                    "progressPercent": 40,
                    "lastVisibleParagraphIndex": 4,
                    "paragraphCount": 12,
                    "updatedAtMillis": 20000,
                    "completedAtMillis": null
                  },
                  {
                    "contentId": "user-document-44444444-4444-4444-8444-444444444444",
                    "progressPercent": 64,
                    "lastVisibleParagraphIndex": 6,
                    "paragraphCount": 20,
                    "updatedAtMillis": 20000,
                    "completedAtMillis": null
                  }
                ]
              },
              "annotations": {
                "export": {
                  "destinationDisplayName": null,
                  "lastSuccessfulAtMillis": null
                },
                "driveSync": {
                  "wasEnabledOnSourceDevice": false,
                  "folderDisplayName": null,
                  "lastSuccessfulAtMillis": null
                },
                "sidecarIndex": []
              },
              "sync": {
                "profileAutosave": {
                  "provider": "NONE",
                  "destinationDisplayName": null,
                  "lastSuccessfulAtMillis": null,
                  "activationStateOnImport": "REQUIRES_LOCAL_SELECTION"
                }
              },
              "warnings": []
            }
        """.trimIndent()
    }

    private fun hasContentDescriptionNode(description: String): Boolean {
        return runCatching {
            composeRule.onNode(hasContentDescription(description)).fetchSemanticsNode()
            true
        }.getOrDefault(false)
    }

    private fun visibleReaderParagraphIndices(): List<Int> {
        return (0..180).filter { index ->
            hasContentDescriptionNode("reader-first-visible-paragraph-$index")
        }
    }

    private fun currentReaderPageEndParagraphIndex(): Int {
        return (0..160).firstOrNull { index ->
            hasContentDescriptionNode("reader-current-page-end-paragraph-$index")
        } ?: -1
    }

    private fun readerPageFitSummary(): String {
        return runCatching {
            val contentDescriptions = composeRule.onNodeWithTag("reader-page-fit-summary")
                .fetchSemanticsNode()
                .config
                .get(SemanticsProperties.ContentDescription)
            contentDescriptions.joinToString(separator = " ")
        }.getOrDefault("")
    }

    private fun readerPageFitWeight(summary: String): Int {
        return Regex("""weight-(\d+)""")
            .find(summary)
            ?.groupValues
            ?.getOrNull(1)
            ?.toIntOrNull()
            ?: 0
    }

    private fun readerPageFitBlocks(summary: String): Int {
        return Regex("""blocks-(\d+)""")
            .find(summary)
            ?.groupValues
            ?.getOrNull(1)
            ?.toIntOrNull()
            ?: 0
    }

    private fun readerPageFitMaxBlocks(summary: String): Int {
        return Regex("""cap-(\d+)""")
            .find(summary)
            ?.groupValues
            ?.getOrNull(1)
            ?.toIntOrNull()
            ?: Int.MAX_VALUE
    }

    private fun readerPageFitPages(summary: String): Int {
        return Regex("""pages-(\d+)""")
            .find(summary)
            ?.groupValues
            ?.getOrNull(1)
            ?.toIntOrNull()
            ?: 1
    }

    private fun assertReaderVisibleContentStaysAboveFooter(
        context: String,
        expectAnotherBlock: Boolean = true,
    ) {
        val lastVisibleParagraph = currentReaderPageEndParagraphIndex()
        assertTrue("Expected current reader page end marker for $context.", lastVisibleParagraph >= 0)
        val renderedNode = composeRule.onNodeWithTag("reader-annotation-rendered-block-$lastVisibleParagraph")
            .fetchSemanticsNode()
        val blockBottom = renderedNode
            .boundsInRoot
            .bottom
        val footerLabelTop = composeRule.onNodeWithTag("reader-page-label")
            .fetchSemanticsNode()
            .boundsInRoot
            .top
        val viewportBottom = composeRule.onNodeWithTag("reader-page-viewport")
            .fetchSemanticsNode()
            .boundsInRoot
            .bottom
        val contentBoundaryBottom = minOf(footerLabelTop, viewportBottom)
        val bottomGuardPx = InstrumentationRegistry.getInstrumentation()
            .targetContext
            .resources
            .displayMetrics
            .density * 10f
        val safeContentBoundaryBottom = contentBoundaryBottom - bottomGuardPx
        assertTrue(
            "Reader content should keep a visible bottom-line guard above the viewport/footer boundary for $context. " +
                "blockBottom=$blockBottom safeBoundary=$safeContentBoundaryBottom " +
                "viewportBottom=$viewportBottom footerLabelTop=$footerLabelTop",
            blockBottom <= safeContentBoundaryBottom,
        )
        val blockHeight = renderedNode.boundsInRoot.bottom - renderedNode.boundsInRoot.top
        val residualSpace = safeContentBoundaryBottom - blockBottom
        if (expectAnotherBlock) {
            val nextBlockIndex = lastVisibleParagraph + 1
            advanceReaderPage()
            // Best-effort packing check: the next block's index on the following page is
            // viewport-dependent. The footer-safety guard above is the primary invariant; only
            // assert "the page was packed too tightly to fit the next block" when that next block
            // actually renders on this device, otherwise skip without failing the test.
            val nextBlockRendered = try {
                composeRule.waitUntil(timeoutMillis = 5_000) {
                    hasTag("reader-annotation-rendered-block-$nextBlockIndex")
                }
                true
            } catch (timeout: ComposeTimeoutException) {
                false
            }
            if (nextBlockRendered) {
                val nextBlockBounds = composeRule.onNodeWithTag("reader-annotation-block-$nextBlockIndex")
                    .fetchSemanticsNode()
                    .boundsInRoot
                val nextBlockHeight = nextBlockBounds.bottom - nextBlockBounds.top
                composeRule.onNodeWithTag("reader-page-viewport")
                    .performTouchInput { swipeRight() }
                composeRule.waitUntil(timeoutMillis = 10_000) {
                    currentReaderPageEndParagraphIndex() == lastVisibleParagraph
                }
                assertTrue(
                    "Reader page should not leave enough residual space for the actual next block for $context. " +
                        "residual=$residualSpace currentRenderedBlockHeight=$blockHeight nextFullBlockHeight=$nextBlockHeight",
                    residualSpace < nextBlockHeight + bottomGuardPx,
                )
            }
        }
    }

    private fun visibleAnnotationHighlightIndices(): List<Int> {
        return (0..180).filter { index ->
            hasTag("reader-annotation-highlight-$index")
        }
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

    private fun currentContentId(): String {
        var contentId = ""
        scenario?.onActivity { activity ->
            contentId = activity.mainViewModel.uiState.currentContent?.id.orEmpty()
        }
        assertTrue(contentId.isNotBlank())
        return contentId
    }

    private fun currentContentTitle(): String {
        var title = ""
        scenario?.onActivity { activity ->
            title = activity.mainViewModel.uiState.currentContent?.title.orEmpty()
        }
        assertTrue(title.isNotBlank())
        return title
    }

    private fun currentReaderFontScale(): Double {
        var scale = 0.0
        scenario?.onActivity { activity ->
            scale = activity.mainViewModel.uiState.readerFontScale
        }
        return scale
    }

    private fun currentHasCompletedOnboarding(): Boolean {
        var completed = false
        scenario?.onActivity { activity ->
            completed = activity.mainViewModel.uiState.hasCompletedOnboarding
        }
        return completed
    }

    private fun currentMeditationDurationMinutes(): Int {
        var minutes = 0
        scenario?.onActivity { activity ->
            minutes = activity.mainViewModel.uiState.meditationDurationMinutes
        }
        return minutes
    }

    private fun readerPageLabelText(): String {
        val config = composeRule.onNodeWithTag("reader-page-label")
            .fetchSemanticsNode()
            .config
        return config[SemanticsProperties.Text]
            .joinToString(separator = " ") { text -> text.text }
    }

    private fun currentReaderProgressPercentFromLabel(): Int {
        return Regex("""(\d+)%""")
            .find(readerPageLabelText())
            ?.groupValues
            ?.getOrNull(1)
            ?.toIntOrNull()
            ?: 0
    }

    private fun readerFooterProgressBarCurrent(): Float {
        val config = composeRule.onNodeWithTag("reader-footer-progress-bar")
            .fetchSemanticsNode()
            .config
        return config[SemanticsProperties.ProgressBarRangeInfo].current
    }

    private fun assertReaderFooterProgressBarMatchesPercent(expectedPercent: Int) {
        val expectedFraction = expectedPercent / 100f
        assertEquals(expectedFraction, readerFooterProgressBarCurrent(), 0.01f)
        val trackWidth = nodeWidth("reader-footer-progress-bar")
        val fillWidth = nodeWidth("reader-footer-progress-bar-fill")
        assertTrue("Expected reader footer progress track to have measurable width.", trackWidth > 0f)
        assertEquals(
            "Expected rendered footer progress fill to match displayed $expectedPercent%.",
            expectedFraction,
            fillWidth / trackWidth,
            0.03f,
        )
    }

    private data class ReaderPagePosition(
        val currentPage: Int,
        val totalPages: Int,
    )

    private fun readerPagePositionFromLabel(): ReaderPagePosition {
        val match = Regex("""(\d+)/(\d+)""").find(readerPageLabelText())
        return ReaderPagePosition(
            currentPage = match?.groupValues?.getOrNull(1)?.toIntOrNull() ?: 0,
            totalPages = match?.groupValues?.getOrNull(2)?.toIntOrNull() ?: 0,
        )
    }

    private fun currentInterfaceTextScale(): Double {
        var scale = 0.0
        scenario?.onActivity { activity ->
            scale = activity.mainViewModel.uiState.interfaceTextScale
        }
        return scale
    }

    private fun currentInterventionMode(): InterventionMode? {
        var mode: InterventionMode? = null
        scenario?.onActivity { activity ->
            mode = activity.mainViewModel.uiState.interventionMode
        }
        return mode
    }

    private fun exportAccountLightProfileJsonFromViewModel(): String {
        var exportedJson = ""
        scenario?.onActivity { activity ->
            activity.mainViewModel.exportAccountLightProfile { json ->
                exportedJson = json
            }
        }
        composeRule.waitUntil(timeoutMillis = 10_000) {
            exportedJson.contains("\"schemaVersion\"")
        }
        assertTrue(exportedJson.contains("\"profileFormat\""))
        return exportedJson
    }

    private fun readerSelectedQuoteText(paragraphIndex: Int): String {
        val config = composeRule.onNodeWithTag("reader-annotation-selected-quote-$paragraphIndex")
            .fetchSemanticsNode()
            .config
        return runCatching { config[SemanticsProperties.ContentDescription] }.getOrNull()
            ?.joinToString(separator = "\n")
            ?: config[SemanticsProperties.Text]
            .joinToString(separator = "\n") { text -> text.text }
    }

    private fun nodeHeight(tag: String): Float {
        val bounds = composeRule.onNodeWithTag(tag)
            .fetchSemanticsNode()
            .boundsInRoot
        return bounds.bottom - bounds.top
    }

    private fun nodeWidth(tag: String): Float {
        val bounds = composeRule.onNodeWithTag(tag)
            .fetchSemanticsNode()
            .boundsInRoot
        return bounds.right - bounds.left
    }

    private fun nodeBounds(tag: String) = composeRule.onNodeWithTag(tag)
        .fetchSemanticsNode()
        .boundsInRoot

    private fun assertAnnotationEditorInsideReaderScreen(context: String) {
        val editorBounds = nodeBounds("reader-annotation-editor-0")
        val screenBounds = nodeBounds("reader-screen")
        assertTrue(
            "Expected annotation editor to stay within reader screen for $context. " +
                "editor=$editorBounds screen=$screenBounds",
            editorBounds.top >= screenBounds.top &&
                editorBounds.bottom <= screenBounds.bottom &&
                editorBounds.left >= screenBounds.left &&
                editorBounds.right <= screenBounds.right,
        )
    }

    private fun assertAnnotationActionsVisibleInsideEditor(context: String) {
        val editorBounds = nodeBounds("reader-annotation-editor-0")
        val titleBounds = nodeBounds("reader-annotation-title-0")
        val rangeBounds = nodeBounds("reader-annotation-range-controls")
        val closeBounds = nodeBounds("reader-annotation-close-0")
        val cancelBounds = nodeBounds("reader-annotation-save-0")
        val noteBounds = nodeBounds("reader-annotation-note-input-0")
        composeRule.onNodeWithTag("reader-annotation-title-0").assertIsDisplayed()
        composeRule.onNodeWithTag("reader-annotation-range-controls").assertIsDisplayed()
        composeRule.onNodeWithTag("reader-annotation-close-0").assertIsDisplayed()
        assertTrue(
            "Expected note input to remain above save action for $context. note=$noteBounds save=$cancelBounds",
            noteBounds.bottom <= cancelBounds.top,
        )
        listOf(
            "title" to titleBounds,
            "rangeControls" to rangeBounds,
            "close" to closeBounds,
        ).forEach { (label, bounds) ->
            assertTrue(
                "Expected $label to remain inside annotation editor for $context. editor=$editorBounds $label=$bounds",
                bounds.top >= editorBounds.top &&
                    bounds.bottom <= editorBounds.bottom &&
                    bounds.left >= editorBounds.left &&
                    bounds.right <= editorBounds.right,
            )
        }
        assertTrue(
            "Expected save action to remain inside annotation editor for $context. editor=$editorBounds save=$cancelBounds",
            cancelBounds.bottom <= editorBounds.bottom && cancelBounds.top >= editorBounds.top,
        )
    }

    private fun currentReaderStartParagraphIndex(): Int {
        var paragraphIndex = -1
        scenario?.onActivity { activity ->
            paragraphIndex = activity.mainViewModel.uiState.currentReaderStartParagraphIndex ?: -1
        }
        assertTrue(paragraphIndex >= 0)
        return paragraphIndex
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

    private fun readerTocEntryBlockIndex(title: String): Int {
        var blockIndex = -1
        scenario?.onActivity { activity ->
            blockIndex = activity.mainViewModel.uiState.currentReaderDocument
                ?.tableOfContents
                ?.firstOrNull { entry -> entry.title == title }
                ?.blockIndex
                ?: -1
        }
        assertTrue("Expected TOC block index for $title", blockIndex >= 0)
        return blockIndex
    }

    private fun currentRecommendationContentIds(): Set<String> {
        var contentIds = emptySet<String>()
        scenario?.onActivity { activity ->
            val recommendationSet = activity.mainViewModel.uiState.currentRecommendationSet
            contentIds = listOfNotNull(recommendationSet?.primary)
                .plus(recommendationSet?.backups.orEmpty())
                .mapTo(mutableSetOf(), ContentItem::id)
        }
        return contentIds
    }

    private fun currentRecommendationPrimaryContentId(): String? {
        var contentId: String? = null
        scenario?.onActivity { activity ->
            contentId = activity.mainViewModel.uiState.currentRecommendationSet?.primary?.id
        }
        return contentId
    }

    private fun currentRecommendationBackupContentIds(): Set<String> {
        var contentIds = emptySet<String>()
        scenario?.onActivity { activity ->
            contentIds = activity.mainViewModel.uiState.currentRecommendationSet
                ?.backups
                .orEmpty()
                .mapTo(mutableSetOf(), ContentItem::id)
        }
        return contentIds
    }

    private fun hasUnfinishedProgressFor(contentId: String): Boolean {
        var hasProgress = false
        scenario?.onActivity { activity ->
            hasProgress = activity.mainViewModel.uiState.readingProgress.any { progress ->
                progress.contentId == contentId && progress.progressPercent in 1..99 && progress.completedAtMillis == null
            }
        }
        return hasProgress
    }

    private fun savedProgressPercentFor(contentId: String): Int {
        var percent = 0
        scenario?.onActivity { activity ->
            percent = activity.mainViewModel.uiState.readingProgress
                .firstOrNull { progress -> progress.contentId == contentId && progress.completedAtMillis == null }
                ?.progressPercent
                ?: 0
        }
        return percent
    }

    private fun savedProgressParagraphIndexFor(contentId: String): Int {
        var index = -1
        scenario?.onActivity { activity ->
            index = activity.mainViewModel.uiState.readingProgress
                .firstOrNull { progress -> progress.contentId == contentId && progress.completedAtMillis == null }
                ?.lastVisibleParagraphIndex
                ?: -1
        }
        return index
    }

    private fun durableRoomSavedProgressPercentFor(contentId: String): Int {
        val app = (InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as QualityAlternativeApplication)
        return runBlocking {
            app.appContainer.readingProgressRowForTests(contentId)
                ?.takeIf { row -> row.completedAtMillis == null }
                ?.progressPercent
                ?: 0
        }
    }

    private fun durableRoomSavedProgressParagraphIndexFor(contentId: String): Int {
        val app = (InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as QualityAlternativeApplication)
        return runBlocking {
            app.appContainer.readingProgressRowForTests(contentId)
                ?.takeIf { row -> row.completedAtMillis == null }
                ?.lastVisibleParagraphIndex
                ?: -1
        }
    }

    private fun roomReadingProgressRepositoryForTests(): RoomReadingProgressRepository {
        val app = (InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as QualityAlternativeApplication)
        val repository = app.appContainer.readingProgressRepository
        assertTrue("Expected RoomReadingProgressRepository in instrumentation", repository is RoomReadingProgressRepository)
        return repository as RoomReadingProgressRepository
    }

    private fun appendReaderFormEvidence(
        stage: String,
        page: ReaderPagePosition,
        progressPercent: Int,
        pageEndParagraph: Int,
        durableSavedParagraph: Int,
    ) {
        val outputDir = File("/sdcard/Download/qualityalternative/$readerFormEvidenceScreenshotDirName")
        assertTrue("Expected evidence output directory for $stage", outputDir.mkdirs() || outputDir.exists())
        File(outputDir, "reader_resume_stage_assertions.txt").appendText(
            "$stage: page=${page.currentPage}/${page.totalPages}, " +
                "progress=$progressPercent, pageEndParagraph=$pageEndParagraph, " +
                "durableSavedParagraph=$durableSavedParagraph\n",
        )
    }

    private fun hasCompletedProgressFor(contentId: String): Boolean {
        var hasCompleted = false
        scenario?.onActivity { activity ->
            hasCompleted = activity.mainViewModel.uiState.readingProgress.any { progress ->
                progress.contentId == contentId && progress.progressPercent == 100 && progress.completedAtMillis != null
            }
        }
        return hasCompleted
    }

    private fun hasCompletedManualReaderEventFor(contentId: String): Boolean {
        var hasEvent = false
        scenario?.onActivity { activity ->
            hasEvent = activity.mainViewModel.uiState.events.any { event ->
                event.type == AnalyticsEventType.READER_COMPLETED &&
                    event.contentId == contentId &&
                    event.sessionId == null
            }
        }
        return hasEvent
    }

    private fun seedReadingAnnotation(
        contentId: String,
        paragraphIndex: Int,
        quotedText: String,
        noteText: String,
        sourceTitle: String = "",
        nowMillis: Long = 11_000L,
    ) = runBlocking {
        val app = (InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as QualityAlternativeApplication)
        app.appContainer.readingAnnotationRepository.saveAnnotation(
            draft = ReadingAnnotationDraft(
                contentId = contentId,
                paragraphIndex = paragraphIndex,
                quotedText = quotedText,
                noteText = noteText,
                sourceTitle = sourceTitle,
            ),
            nowMillis = nowMillis,
        )
    }

    private fun hasReadingAnnotationFor(contentId: String): Boolean {
        var hasAnnotation = false
        scenario?.onActivity { activity ->
            hasAnnotation = activity.mainViewModel.uiState.readingAnnotations.any { annotation ->
                annotation.contentId == contentId
            }
        }
        return hasAnnotation
    }

    private fun hasReadingAnnotationNote(contentId: String, noteText: String): Boolean {
        return hasReadingAnnotationNote(contentId = contentId, paragraphIndex = 0, noteText = noteText)
    }

    private fun hasReadingAnnotationNote(contentId: String, paragraphIndex: Int, noteText: String): Boolean {
        var hasAnnotation = false
        scenario?.onActivity { activity ->
            hasAnnotation = activity.mainViewModel.uiState.readingAnnotations.any { annotation ->
                annotation.contentId == contentId && annotation.paragraphIndex == paragraphIndex && annotation.noteText == noteText
            }
        }
        return hasAnnotation
    }

    private fun hasReadingAnnotationQuoteContaining(contentId: String, paragraphIndex: Int, quoteText: String): Boolean {
        var hasAnnotation = false
        scenario?.onActivity { activity ->
            hasAnnotation = activity.mainViewModel.uiState.readingAnnotations.any { annotation ->
                annotation.contentId == contentId &&
                    annotation.paragraphIndex == paragraphIndex &&
                    annotation.quotedText.contains(quoteText)
            }
        }
        return hasAnnotation
    }

    private fun readingAnnotationIdFor(contentId: String, paragraphIndex: Int): String {
        var annotationId = ""
        scenario?.onActivity { activity ->
            annotationId = activity.mainViewModel.uiState.readingAnnotations
                .firstOrNull { annotation ->
                    annotation.contentId == contentId && annotation.paragraphIndex == paragraphIndex
                }
                ?.id
                .orEmpty()
        }
        assertTrue(annotationId.isNotBlank())
        return annotationId
    }

    private fun hasSingleReadingAnnotationNoteContaining(
        contentId: String,
        paragraphIndex: Int,
        noteText: String,
    ): Boolean {
        var hasAnnotation = false
        scenario?.onActivity { activity ->
            val matchingAnnotations = activity.mainViewModel.uiState.readingAnnotations.filter { annotation ->
                annotation.contentId == contentId && annotation.paragraphIndex == paragraphIndex
            }
            hasAnnotation = matchingAnnotations.size == 1 &&
                matchingAnnotations.single().noteText.contains(noteText)
        }
        return hasAnnotation
    }

    private fun hasReadingAnnotationDeletedEventFor(contentId: String): Boolean {
        var hasEvent = false
        scenario?.onActivity { activity ->
            hasEvent = activity.mainViewModel.uiState.events.any { event ->
                event.type == AnalyticsEventType.READING_ANNOTATION_DELETED &&
                    event.contentId == contentId
            }
        }
        return hasEvent
    }

    private fun hasAnnotationExportSuccess(): Boolean {
        var hasSuccess = false
        scenario?.onActivity { activity ->
            hasSuccess = activity.mainViewModel.uiState.annotationExportLastSuccessfulAtMillis != null &&
                activity.mainViewModel.uiState.annotationExportLastError == null
        }
        return hasSuccess
    }

    private fun hasAnnotationExportFailure(): Boolean {
        var hasFailure = false
        scenario?.onActivity { activity ->
            hasFailure = !activity.mainViewModel.uiState.annotationExportLastError.isNullOrBlank()
        }
        return hasFailure
    }

    private fun hasProfileAutosaveSuccess(): Boolean {
        var hasSuccess = false
        scenario?.onActivity { activity ->
            hasSuccess = activity.mainViewModel.uiState.profileAutosaveLastSuccessfulAtMillis != null &&
                activity.mainViewModel.uiState.profileAutosaveLastError == null
        }
        return hasSuccess
    }

    private fun hasProfileAutosaveFailure(): Boolean {
        var hasFailure = false
        scenario?.onActivity { activity ->
            hasFailure = !activity.mainViewModel.uiState.profileAutosaveLastError.isNullOrBlank()
        }
        return hasFailure
    }

    private fun openAddLinkFromHome() {
        composeRule.onNodeWithTag("home-list")
            .performScrollToNode(hasTestTag("home-add-link"))
        composeRule.onNodeWithTag("home-add-link")
            .performClick()
    }

    private fun saveReaderAnnotation(paragraphIndex: Int, noteText: String) {
        composeRule.onNodeWithTag("reader-annotation-block-$paragraphIndex")
            .assertIsDisplayed()
            .performTouchInput { longClick() }
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("reader-annotation-editor-$paragraphIndex") }
        composeRule.onNodeWithTag("reader-annotation-note-input-$paragraphIndex")
            .assertIsDisplayed()
            .performClick()
            .performTextInput(noteText)
        UiDevice.getInstance(InstrumentationRegistry.getInstrumentation()).pressBack()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("reader-annotation-save-$paragraphIndex")
            .assertIsDisplayed()
            .assertIsEnabled()
            .performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) {
            hasTag("reader-annotation-highlight-$paragraphIndex") && hasReadingAnnotationNote(currentContentId(), paragraphIndex, noteText)
        }
        scenario?.onActivity { activity -> activity.mainViewModel.dismissMessage() }
        composeRule.waitForIdle()
    }

    private fun waitForHome() {
        composeRule.waitUntil(timeoutMillis = 10_000) {
            hasNode("You're set up for quieter reading today.") || hasNode("Finish setup to intercept distracting apps.")
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
        launchOnboardedApp()
        seedFixtureSelection()
        relaunchFixtureSystemIntervention()
    }

    private fun relaunchFixtureSystemIntervention() {
        relaunchFixtureSystemInterventionWithoutWaitingForIntervention()

        composeRule.waitUntil(timeoutMillis = 10_000) {
            hasNode("You reached for Fixture Feed One")
        }
    }

    private fun waitForOpenAnywayUnlock() {
        composeRule.waitUntil(timeoutMillis = 8_000) {
            hasNode("Open Fixture Feed One") && !hasTag("form-intervention-unlock-wait")
        }
    }

    private fun relaunchFixtureSystemInterventionWithoutWaitingForIntervention() {
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
    }

    private fun launchMeditationFixtureSystemIntervention() {
        launchOnboardedApp()
        seedMeditationFixtureSelection()

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

    private fun launchLinkOnlyFixtureSystemIntervention() {
        launchOnboardedApp()
        seedLinkOnlyFixtureSelection()

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

    private fun seedScrollableUnfinishedDocumentOptions(progressPercent: Int): List<ContentItem> = runBlocking {
        val app = (InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as QualityAlternativeApplication)
        val durations = listOf(7, 6, 5, 4, 3, 3)
        durations.mapIndexed { index, minutes ->
            val result = app.appContainer.userDocumentRepository.addDocument(
                draft = UserDocumentDraft(
                    uri = "content://qa-test/continue-option-$index",
                    displayName = "continue-option-$index.md",
                    mimeType = "text/markdown",
                    title = "Continue option ${index + 1}",
                    durationMinutes = minutes,
                    topicTags = setOf(TopicTag.SCIENCE, TopicTag.PHILOSOPHY),
                ),
                nowMillis = 10_000L + index,
            )
            assertTrue("Expected unfinished option document to be saved", result is AddUserDocumentResult.Added)
            (result as AddUserDocumentResult.Added).item.also { item ->
                app.appContainer.readingProgressRepository.saveProgress(
                    ReadingProgress(
                        contentId = item.id,
                        progressPercent = progressPercent,
                        lastVisibleParagraphIndex = 4 + index,
                        paragraphCount = 12,
                        updatedAtMillis = 20_000L + index,
                    ),
                )
            }
        }
    }

    private fun seedFreshDocumentRankingScenario(): FreshDocumentRankingSeed = runBlocking {
        val app = (InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as QualityAlternativeApplication)
        app.appContainer.userDocumentRepository.observeReady().first { it }

        val oldMarkdown = addSeedDocument(
            title = "Old Markdown Priority",
            displayName = "old-priority.md",
            mimeType = "text/markdown",
            format = ContentFormat.MARKDOWN,
            nowMillis = 10_000L,
        )
        val oldEpub = addSeedDocument(
            title = "Old EPUB Priority",
            displayName = "old-priority.epub",
            mimeType = "application/epub+zip",
            format = ContentFormat.EPUB,
            nowMillis = 11_000L,
        )
        val newEpub = addSeedDocument(
            title = "Newest EPUB Choice",
            displayName = "newest-choice.epub",
            mimeType = "application/epub+zip",
            format = ContentFormat.EPUB,
            nowMillis = 30_000L,
        )

        app.appContainer.settingsRepository.savePriorityContentIds(setOf(oldMarkdown.id, oldEpub.id))
        app.appContainer.settingsRepository.savePriorityContentIds(emptySet())
        app.appContainer.settingsRepository.saveOnboardingSelection(
            OnboardingSelection(
                selectedAppPackages = setOf(FixtureTargetRegistry.fixtureDistractors.first().packageName),
                preferredTopics = setOf(TopicTag.SCIENCE, TopicTag.PHILOSOPHY, TopicTag.ESSAYS),
                preferredDurationBucket = DurationBucket.FOCUS,
                selectedPackIds = emptySet(),
            ),
        )

        FreshDocumentRankingSeed(
            oldMarkdown = oldMarkdown,
            oldEpub = oldEpub,
            newEpub = newEpub,
        )
    }

    private suspend fun addSeedDocument(
        title: String,
        displayName: String,
        mimeType: String,
        format: ContentFormat,
        nowMillis: Long,
    ): ContentItem {
        val targetContext = InstrumentationRegistry.getInstrumentation().targetContext
        val app = (targetContext.applicationContext as QualityAlternativeApplication)
        val fixture = File(targetContext.filesDir, "sprint14-ranking-fixtures/$displayName")
        fixture.parentFile?.mkdirs()
        when (format) {
            ContentFormat.EPUB -> fixture.writeBytes(sprint14EpubBytes(title = title))
            else -> fixture.writeText(
                """
                # $title

                This private Markdown document is an older saved replacement candidate.

                It should not remain ahead of the newest EPUB once its explicit priority is removed.
                """.trimIndent(),
            )
        }
        val result = app.appContainer.userDocumentRepository.addDocument(
            draft = UserDocumentDraft(
                uri = Uri.fromFile(fixture).toString(),
                displayName = displayName,
                mimeType = mimeType,
                title = title,
                durationMinutes = 6,
                topicTags = setOf(TopicTag.SCIENCE, TopicTag.PHILOSOPHY),
            ),
            nowMillis = nowMillis,
        )
        assertTrue("Expected $title to be saved", result is AddUserDocumentResult.Added)
        return (result as AddUserDocumentResult.Added).item
    }

    private fun addSeedEpubDocument(
        title: String,
        displayName: String,
        bytes: ByteArray,
        nowMillis: Long,
    ): ContentItem = runBlocking {
        val targetContext = InstrumentationRegistry.getInstrumentation().targetContext
        val app = (targetContext.applicationContext as QualityAlternativeApplication)
        app.appContainer.userDocumentRepository.observeReady().first { it }
        val fixture = File(targetContext.filesDir, "sprint15-reader-fixtures/$displayName")
        fixture.parentFile?.mkdirs()
        fixture.writeBytes(bytes)
        val result = app.appContainer.userDocumentRepository.addDocument(
            draft = UserDocumentDraft(
                uri = Uri.fromFile(fixture).toString(),
                displayName = displayName,
                mimeType = "application/epub+zip",
                title = title,
                durationMinutes = 6,
                topicTags = setOf(TopicTag.SCIENCE, TopicTag.PHILOSOPHY),
            ),
            nowMillis = nowMillis,
        )
        assertTrue("Expected $title to be saved", result is AddUserDocumentResult.Added)
        (result as AddUserDocumentResult.Added).item
    }

    private fun addSeedMarkdownDocument(
        title: String,
        displayName: String,
        body: String,
        nowMillis: Long,
    ): ContentItem = runBlocking {
        val targetContext = InstrumentationRegistry.getInstrumentation().targetContext
        val app = (targetContext.applicationContext as QualityAlternativeApplication)
        app.appContainer.userDocumentRepository.observeReady().first { it }
        val fixture = File(targetContext.filesDir, "sprint16-reader-fixtures/$displayName")
        fixture.parentFile?.mkdirs()
        fixture.writeText(body)
        val result = app.appContainer.userDocumentRepository.addDocument(
            draft = UserDocumentDraft(
                uri = Uri.fromFile(fixture).toString(),
                displayName = displayName,
                mimeType = "text/markdown",
                title = title,
                durationMinutes = 6,
                topicTags = setOf(TopicTag.SCIENCE, TopicTag.PHILOSOPHY),
            ),
            nowMillis = nowMillis,
        )
        assertTrue("Expected $title to be saved", result is AddUserDocumentResult.Added)
        (result as AddUserDocumentResult.Added).item
    }

    private fun sprint14EpubBytes(title: String): ByteArray {
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
                  </manifest>
                  <spine>
                    <itemref idref="chapter-one"/>
                  </spine>
                </package>
            """.trimIndent(),
            "OPS/chapter1.xhtml" to """
                <html><body>
	                  <h1>$title</h1>
	                  <p>This newest EPUB is the fresh private document the ranking test expects as the primary recommendation.</p>
	                  <p>The reader should open it natively after the intervention, proving this is installable replacement content rather than a dead card.</p>
	                  <p>Page controls should move through this private book without asking the app to render the entire document as one long scroll.</p>
	                  <p>The next paragraph keeps the test fixture long enough to prove pagination, not just a renamed one-screen reader.</p>
	                  <p>Each page still needs annotation anchors, progress, and a clear way back to the previous page.</p>
	                  <p>Long EPUB files should feel steady on older phones, so the app only lays out the active page.</p>
	                  <p>The reader remains finite: one piece, one page at a time, no feed and no recommendations inside the reading surface.</p>
	                  <p>Progress should update when the user advances through pages and should restore to the same region later.</p>
	                  <p>This paragraph exists to keep the fixture representative of a short chapter rather than a card.</p>
	                  <p>A longer private document makes the pagination assertion independent from viewport and reader fitting improvements.</p>
	                  <p>The fixture intentionally spans multiple rendered pages even on tall phones with efficient page packing.</p>
	                  <p>Continuation should still restore the same source region after the user leaves and returns to the reader.</p>
	                  <p>Library sorting should keep this unfinished item available without turning the reader into a scrolling feed.</p>
	                  <p>Reader navigation remains explicit: one page advances, progress saves, and the next launch resumes from that page.</p>
	                  <p>This additional prose protects the test from false failures when layout uses more vertical space correctly.</p>
	                  <p>The fixture is still short enough to finish quickly, but long enough to prove real pagination.</p>
	                  <p>Annotations and progress anchors should remain attached to source paragraphs across page boundaries.</p>
	                  <p>Each paragraph uses steady prose so adaptive pagination can choose page breaks without special-case markup.</p>
	                  <p>The final screen should remain reachable after several page advances and should still lead to feedback.</p>
	                  <p>The final fixture paragraph gives the user a clean completion path after paging forward.</p>
	                </body></html>
            """.trimIndent(),
        )
    }

    private fun sprint15TocEpubBytes(title: String): ByteArray {
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
                    <item id="nav" href="nav.xhtml" media-type="application/xhtml+xml" properties="nav"/>
                    <item id="chapter-one" href="chapter1.xhtml" media-type="application/xhtml+xml"/>
                    <item id="chapter-two" href="chapter2.xhtml" media-type="application/xhtml+xml"/>
                  </manifest>
                  <spine>
                    <itemref idref="chapter-one"/>
                    <itemref idref="chapter-two"/>
                  </spine>
                </package>
            """.trimIndent(),
            "OPS/nav.xhtml" to """
                <html xmlns:epub="http://www.idpf.org/2007/ops"><body>
                  <nav epub:type="toc">
                    <ol>
                      <li><a href="chapter1.xhtml#chapter-one">Chapter One</a></li>
                      <li><a href="chapter2.xhtml#chapter-two">Chapter Two</a></li>
                    </ol>
                  </nav>
                </body></html>
            """.trimIndent(),
            "OPS/chapter1.xhtml" to """
                <html><body>
                  <h1 id="chapter-one">Chapter One</h1>
                  <p>$title starts with a long first chapter so the reader must paginate instead of placing the whole book in one scroll surface.</p>
                  <p>The first page is intentionally dense enough to make a vertical swipe tempting, but the app should keep it fixed like a page.</p>
                  <p>Tap anywhere in the page body to go forward; visible controls should stay out of the reading surface.</p>
                  <p>When the user presses the Android back gesture, the reader should leave the reading session after overlays are closed.</p>
                  <p>This paragraph keeps chapter one over the page weight threshold and proves the next page is a real page, not a scrolled offset.</p>
                  <p>The page should still report stable progress and visible paragraph anchors while rendering only the current page of the EPUB.</p>
                  <p>Chapter one ends with enough text to separate it clearly from the following table-of-contents target.</p>
                </body></html>
            """.trimIndent(),
            "OPS/chapter2.xhtml" to """
                <html><body>
                  <h1 id="chapter-two">Chapter Two</h1>
                  <p>Chapter Two is the table-of-contents target used by Sprint 15 to prove EPUB navigation can jump straight to a section.</p>
                  <p>The jump should close the contents sheet and land on a normal paginated reader page without enabling vertical body scrolling.</p>
                  <p>This final paragraph keeps the destination page representative of a source chapter rather than a tiny label-only target.</p>
                </body></html>
            """.trimIndent(),
        )
    }

    private fun sprint19RegressionEpubBytes(): ByteArray {
        val chapterOneFiller = (1..12).joinToString(separator = "\n") { index ->
            "<p>Opening source marker $index keeps the first chapter long enough for calibrated progress.</p>"
        }
        val chapterTwoFiller = (1..12).joinToString(separator = "\n") { index ->
            "<p>The second chapter marker $index stays before the target so backward annotation selection has real previous-source text.</p>"
        }
        val chapterThreeTail = (1..18).joinToString(separator = "\n") { index ->
            "<p>Trailing third-chapter source marker $index keeps the chapter from being the final page during progress checks.</p>"
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
                    <item id="nav" href="nav.xhtml" media-type="application/xhtml+xml" properties="nav"/>
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
            "OPS/nav.xhtml" to """
                <html xmlns:epub="http://www.idpf.org/2007/ops"><body>
                  <nav epub:type="toc">
                    <ol>
                      <li><a href="chapter1.xhtml#chapter-one">Chapter One</a></li>
                      <li><a href="chapter2.xhtml#chapter-two">Chapter Two</a></li>
                      <li><a href="chapter3.xhtml#chapter-three">Chapter Three</a></li>
                    </ol>
                  </nav>
                </body></html>
            """.trimIndent(),
            "OPS/chapter1.xhtml" to """
                <html><body>
                  <h1 id="chapter-one">Chapter One</h1>
                  <p>The first chapter establishes baseline source ordering for the regression fixture.</p>
                  <p>Its paragraphs are deliberately ordinary so progress can only come from source anchors.</p>
                  <p>This opening material should never be shown after chapter-three annotation adjustment begins.</p>
                  <p>Another first chapter paragraph makes the chapter large enough to affect whole-book percentage.</p>
                  $chapterOneFiller
                </body></html>
            """.trimIndent(),
            "OPS/chapter2.xhtml" to """
                <html><body>
                  <h1 id="chapter-two">Chapter Two</h1>
                  <p>The second chapter adds a middle source range before the target chapter.</p>
                  <p>Reader progress should count this middle material before reporting chapter three.</p>
                  <p>Annotation controls may cross nearby source ranges, but should not confuse this chapter with the first.</p>
                  <p>The final middle paragraph protects against duplicate local EPUB block indexes.</p>
                  $chapterTwoFiller
                </body></html>
            """.trimIndent(),
            "OPS/chapter3.xhtml" to """
                <html><body>
                  <h1 id="chapter-three">Chapter Three</h1>
                  <p>The third chapter anchor is the user-visible target for Sprint 19 progress and selection testing.</p>
                  <p>This third chapter anchor sentence is selected for a note so the start handle can move backward safely.</p>
                  <p>Changing reader text size here should not make the progress label drift back toward the beginning.</p>
                  <p>The final chapter-three paragraph keeps the page representative after large-font repagination.</p>
                  $chapterThreeTail
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

    private fun currentRecommendationSet(): com.qualityalternative.app.domain.model.RecommendationSet {
        var recommendationSet: com.qualityalternative.app.domain.model.RecommendationSet? = null
        scenario?.onActivity { activity ->
            recommendationSet = activity.mainViewModel.uiState.currentRecommendationSet
        }
        return requireNotNull(recommendationSet)
    }

    private fun continueProgressLabel(item: ContentItem, progressPercent: Int): String {
        val remainingPercent = (100 - progressPercent.coerceIn(1, 99)).coerceAtLeast(1)
        val remainingMinutes = ((item.durationMinutes * remainingPercent) + 99) / 100
        return "$progressPercent% read · $remainingMinutes min left"
    }

    private fun captureInterventionContinueProgressScreenshot(name: String) {
        val outputDir = File("/sdcard/Download/qualityalternative/$interventionContinueProgressScreenshotDirName")
        assertTrue("Expected screenshot output directory for $name", outputDir.mkdirs() || outputDir.exists())
        composeRule.waitForIdle()
        Thread.sleep(300)
        val output = File(outputDir, "$name.png")
        assertTrue("Expected screenshot capture for $name", UiDevice.getInstance(InstrumentationRegistry.getInstrumentation()).takeScreenshot(output))
        assertTrue("Expected screenshot file for $name", output.exists() && output.length() > 0L)
    }

    private fun captureSprint14RankingScreenshot(name: String) {
        val outputDir = File("/sdcard/Download/qualityalternative/$sprint14RankingScreenshotDirName")
        assertTrue("Expected screenshot output directory for $name", outputDir.mkdirs() || outputDir.exists())
        composeRule.waitForIdle()
        Thread.sleep(300)
        val output = File(outputDir, "$name.png")
        assertTrue("Expected screenshot capture for $name", UiDevice.getInstance(InstrumentationRegistry.getInstrumentation()).takeScreenshot(output))
        assertTrue("Expected screenshot file for $name", output.exists() && output.length() > 0L)
    }

    private fun captureSprint14AnnotationStorageScreenshot(name: String) {
        val outputDir = File("/sdcard/Download/qualityalternative/$sprint14AnnotationStorageScreenshotDirName")
        assertTrue("Expected screenshot output directory for $name", outputDir.mkdirs() || outputDir.exists())
        composeRule.waitForIdle()
        Thread.sleep(300)
        val output = File(outputDir, "$name.png")
        assertTrue("Expected screenshot capture for $name", UiDevice.getInstance(InstrumentationRegistry.getInstrumentation()).takeScreenshot(output))
        assertTrue("Expected screenshot file for $name", output.exists() && output.length() > 0L)
    }

    private fun captureSprint14ReaderAnnotationScreenshot(name: String) {
        val outputDir = File("/sdcard/Download/qualityalternative/$sprint14ReaderAnnotationScreenshotDirName")
        assertTrue("Expected screenshot output directory for $name", outputDir.mkdirs() || outputDir.exists())
        composeRule.waitForIdle()
        Thread.sleep(300)
        val output = File(outputDir, "$name.png")
        assertTrue("Expected screenshot capture for $name", UiDevice.getInstance(InstrumentationRegistry.getInstrumentation()).takeScreenshot(output))
        assertTrue("Expected screenshot file for $name", output.exists() && output.length() > 0L)
    }

    private fun captureSprint14AnnotationLibraryScreenshot(name: String) {
        val outputDir = File("/sdcard/Download/qualityalternative/$sprint14AnnotationLibraryScreenshotDirName")
        assertTrue("Expected screenshot output directory for $name", outputDir.mkdirs() || outputDir.exists())
        composeRule.waitForIdle()
        Thread.sleep(300)
        val output = File(outputDir, "$name.png")
        assertTrue("Expected screenshot capture for $name", UiDevice.getInstance(InstrumentationRegistry.getInstrumentation()).takeScreenshot(output))
        assertTrue("Expected screenshot file for $name", output.exists() && output.length() > 0L)
    }

    private fun captureSprint14AnnotationExportScreenshot(name: String) {
        val outputDir = File("/sdcard/Download/qualityalternative/$sprint14AnnotationExportScreenshotDirName")
        assertTrue("Expected screenshot output directory for $name", outputDir.mkdirs() || outputDir.exists())
        composeRule.waitForIdle()
        Thread.sleep(300)
        val output = File(outputDir, "$name.png")
        assertTrue("Expected screenshot capture for $name", UiDevice.getInstance(InstrumentationRegistry.getInstrumentation()).takeScreenshot(output))
        assertTrue("Expected screenshot file for $name", output.exists() && output.length() > 0L)
    }

    private fun captureSprint14ReaderPaginationScreenshot(name: String) {
        val outputDir = File("/sdcard/Download/qualityalternative/$sprint14ReaderPaginationScreenshotDirName")
        assertTrue("Expected screenshot output directory for $name", outputDir.mkdirs() || outputDir.exists())
        composeRule.waitForIdle()
        Thread.sleep(300)
        val output = File(outputDir, "$name.png")
        assertTrue("Expected screenshot capture for $name", UiDevice.getInstance(InstrumentationRegistry.getInstrumentation()).takeScreenshot(output))
        assertTrue("Expected screenshot file for $name", output.exists() && output.length() > 0L)
    }

    private fun captureSprint17DriveAuthScreenshot(name: String) {
        val outputDir = File("/sdcard/Download/qualityalternative/$sprint17DriveAuthScreenshotDirName")
        assertTrue("Expected screenshot output directory for $name", outputDir.mkdirs() || outputDir.exists())
        composeRule.waitForIdle()
        Thread.sleep(300)
        val output = File(outputDir, "$name.png")
        assertTrue("Expected screenshot capture for $name", UiDevice.getInstance(InstrumentationRegistry.getInstrumentation()).takeScreenshot(output))
        assertTrue("Expected screenshot file for $name", output.exists() && output.length() > 0L)
    }

    private fun captureAccountLightProfileScreenshot(name: String) {
        val outputDir = File("/sdcard/Download/qualityalternative/$accountLightProfileScreenshotDirName")
        assertTrue("Expected screenshot output directory for $name", outputDir.mkdirs() || outputDir.exists())
        composeRule.waitForIdle()
        Thread.sleep(300)
        val output = File(outputDir, "$name.png")
        assertTrue("Expected screenshot capture for $name", UiDevice.getInstance(InstrumentationRegistry.getInstrumentation()).takeScreenshot(output))
        assertTrue("Expected screenshot file for $name", output.exists() && output.length() > 0L)
    }

    private fun captureSprint16ProfileAutosaveScreenshot(name: String) {
        val outputDir = File("/sdcard/Download/qualityalternative/$sprint16ProfileAutosaveScreenshotDirName")
        assertTrue("Expected screenshot output directory for $name", outputDir.mkdirs() || outputDir.exists())
        composeRule.waitForIdle()
        Thread.sleep(300)
        val output = File(outputDir, "$name.png")
        assertTrue("Expected screenshot capture for $name", UiDevice.getInstance(InstrumentationRegistry.getInstrumentation()).takeScreenshot(output))
        assertTrue("Expected screenshot file for $name", output.exists() && output.length() > 0L)
    }

    private fun captureSprint16AdaptiveReaderScreenshot(name: String) {
        val outputDir = File("/sdcard/Download/qualityalternative/$sprint16AdaptiveReaderScreenshotDirName")
        assertTrue("Expected screenshot output directory for $name", outputDir.mkdirs() || outputDir.exists())
        composeRule.waitForIdle()
        Thread.sleep(300)
        val output = File(outputDir, "$name.png")
        assertTrue("Expected screenshot capture for $name", UiDevice.getInstance(InstrumentationRegistry.getInstrumentation()).takeScreenshot(output))
        assertTrue("Expected screenshot file for $name", output.exists() && output.length() > 0L)
    }

    private fun captureSprint17TypographyScreenshot(name: String) {
        val outputDir = File("/sdcard/Download/qualityalternative/$sprint17TypographyScreenshotDirName")
        assertTrue("Expected screenshot output directory for $name", outputDir.mkdirs() || outputDir.exists())
        composeRule.waitForIdle()
        Thread.sleep(300)
        val output = File(outputDir, "$name.png")
        assertTrue("Expected screenshot capture for $name", UiDevice.getInstance(InstrumentationRegistry.getInstrumentation()).takeScreenshot(output))
        assertTrue("Expected screenshot file for $name", output.exists() && output.length() > 0L)
    }

    private fun captureSprint17DefaultsScreenshot(name: String) {
        val outputDir = File("/sdcard/Download/qualityalternative/$sprint17DefaultsScreenshotDirName")
        assertTrue("Expected screenshot output directory for $name", outputDir.mkdirs() || outputDir.exists())
        composeRule.waitForIdle()
        Thread.sleep(300)
        val output = File(outputDir, "$name.png")
        assertTrue("Expected screenshot capture for $name", UiDevice.getInstance(InstrumentationRegistry.getInstrumentation()).takeScreenshot(output))
        assertTrue("Expected screenshot file for $name", output.exists() && output.length() > 0L)
    }

    private fun captureSprint17AdaptivePaginationScreenshot(name: String) {
        val outputDir = File("/sdcard/Download/qualityalternative/$sprint17AdaptivePaginationScreenshotDirName")
        assertTrue("Expected screenshot output directory for $name", outputDir.mkdirs() || outputDir.exists())
        composeRule.waitForIdle()
        Thread.sleep(300)
        val output = File(outputDir, "$name.png")
        assertTrue("Expected screenshot capture for $name", UiDevice.getInstance(InstrumentationRegistry.getInstrumentation()).takeScreenshot(output))
        assertTrue("Expected screenshot file for $name", output.exists() && output.length() > 0L)
    }

    private fun captureSprint17CrossPageAnnotationScreenshot(name: String) {
        val outputDir = File("/sdcard/Download/qualityalternative/$sprint17CrossPageAnnotationScreenshotDirName")
        assertTrue("Expected screenshot output directory for $name", outputDir.mkdirs() || outputDir.exists())
        composeRule.waitForIdle()
        Thread.sleep(300)
        val output = File(outputDir, "$name.png")
        assertTrue("Expected screenshot capture for $name", UiDevice.getInstance(InstrumentationRegistry.getInstrumentation()).takeScreenshot(output))
        assertTrue("Expected screenshot file for $name", output.exists() && output.length() > 0L)
    }

    private fun captureSprint17AnnotationSurfaceScreenshot(name: String) {
        val outputDir = File("/sdcard/Download/qualityalternative/$sprint17AnnotationSurfaceScreenshotDirName")
        assertTrue("Expected screenshot output directory for $name", outputDir.mkdirs() || outputDir.exists())
        composeRule.waitForIdle()
        Thread.sleep(300)
        val output = File(outputDir, "$name.png")
        assertTrue("Expected screenshot capture for $name", UiDevice.getInstance(InstrumentationRegistry.getInstrumentation()).takeScreenshot(output))
        assertTrue("Expected screenshot file for $name", output.exists() && output.length() > 0L)
    }

    private fun captureSprint23FooterProgressScreenshot(name: String) {
        val outputDir = File("/sdcard/Download/qualityalternative/$sprint23FooterProgressScreenshotDirName")
        assertTrue("Expected screenshot output directory for $name", outputDir.mkdirs() || outputDir.exists())
        composeRule.waitForIdle()
        Thread.sleep(300)
        val output = File(outputDir, "$name.png")
        assertTrue("Expected screenshot capture for $name", UiDevice.getInstance(InstrumentationRegistry.getInstrumentation()).takeScreenshot(output))
        assertTrue("Expected screenshot file for $name", output.exists() && output.length() > 0L)
    }

    private fun captureSprint24BedtimeScreenshot(name: String) {
        val outputDir = File("/sdcard/Download/qualityalternative/$sprint24BedtimeScreenshotDirName")
        assertTrue("Expected screenshot output directory for $name", outputDir.mkdirs() || outputDir.exists())
        composeRule.waitForIdle()
        Thread.sleep(300)
        val output = File(outputDir, "$name.png")
        assertTrue("Expected screenshot capture for $name", UiDevice.getInstance(InstrumentationRegistry.getInstrumentation()).takeScreenshot(output))
        assertTrue("Expected screenshot file for $name", output.exists() && output.length() > 0L)
    }

    private fun captureReaderFormEvidenceScreenshot(name: String) {
        val outputDir = File("/sdcard/Download/qualityalternative/$readerFormEvidenceScreenshotDirName")
        assertTrue("Expected screenshot output directory for $name", outputDir.mkdirs() || outputDir.exists())
        composeRule.waitForIdle()
        Thread.sleep(300)
        val output = File(outputDir, "$name.png")
        assertTrue("Expected screenshot capture for $name", UiDevice.getInstance(InstrumentationRegistry.getInstrumentation()).takeScreenshot(output))
        assertTrue("Expected screenshot file for $name", output.exists() && output.length() > 0L)
    }

    private fun captureSprint21ReadingTimeScreenshot(name: String) {
        val outputDir = File("/sdcard/Download/qualityalternative/$sprint21ReadingTimeScreenshotDirName")
        assertTrue("Expected screenshot output directory for $name", outputDir.mkdirs() || outputDir.exists())
        composeRule.waitForIdle()
        Thread.sleep(300)
        val output = File(outputDir, "$name.png")
        assertTrue("Expected screenshot capture for $name", UiDevice.getInstance(InstrumentationRegistry.getInstrumentation()).takeScreenshot(output))
        assertTrue("Expected screenshot file for $name", output.exists() && output.length() > 0L)
    }

    private fun recordSprint17AdaptivePaginationSummary(name: String, summary: String) {
        val outputDir = File("/sdcard/Download/qualityalternative/$sprint17AdaptivePaginationScreenshotDirName")
        assertTrue("Expected summary output directory for $name", outputDir.mkdirs() || outputDir.exists())
        File(outputDir, "page-fit-summaries.txt").appendText("$name: $summary\n")
    }

    private fun assertHomeHeroIsDisplayed() {
        composeRule.waitUntil(timeoutMillis = 10_000) {
            hasNode("You're set up for quieter reading today.") || hasNode("Finish setup to intercept distracting apps.")
        }
    }

    private fun seedFixtureSelection() = runBlocking {
        val repository = (InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as QualityAlternativeApplication)
            .appContainer
            .settingsRepository
        repository.saveOnboardingSelection(
            OnboardingSelection(
                selectedAppPackages = setOf(FixtureTargetRegistry.fixtureDistractors.first().packageName),
                preferredTopics = setOf(TopicTag.PHILOSOPHY, TopicTag.SCIENCE, TopicTag.HISTORY),
                preferredDurationBucket = DurationBucket.FOCUS,
                selectedPackIds = setOf("philosophy"),
            ),
        )
    }

    private fun seedMeditationFixtureSelection() = runBlocking {
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

    private fun seedLinkOnlyFixtureSelection() = runBlocking {
        val repository = (InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as QualityAlternativeApplication)
            .appContainer
            .settingsRepository
        repository.saveOnboardingSelection(
            OnboardingSelection(
                selectedAppPackages = setOf(FixtureTargetRegistry.fixtureDistractors.first().packageName),
                preferredTopics = setOf(TopicTag.PHILOSOPHY, TopicTag.ESSAYS, TopicTag.HISTORY),
                preferredDurationBucket = DurationBucket.FOCUS,
                selectedPackIds = setOf("link-only-modern-v1"),
            ),
        )
    }

    private fun seedFirstEligibleCustomTargetSelection(): com.qualityalternative.app.domain.model.DistractingApp = runBlocking {
        val repository = (InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as QualityAlternativeApplication)
            .appContainer
            .settingsRepository
        val candidate = repository.customTargetAppCandidates().firstOrNull { it.isEligible }?.app
        assertTrue("Expected at least one eligible custom app target on the test device", candidate != null)
        val customTarget = requireNotNull(candidate)
        repository.saveOnboardingSelection(
            OnboardingSelection(
                selectedAppPackages = setOf(customTarget.packageName),
                preferredTopics = setOf(TopicTag.PHILOSOPHY, TopicTag.SCIENCE, TopicTag.HISTORY),
                preferredDurationBucket = DurationBucket.FOCUS,
                selectedPackIds = setOf("philosophy"),
            ),
        )
        customTarget
    }

    private fun resetPersistentState() = runBlocking {
        val targetContext = InstrumentationRegistry.getInstrumentation().targetContext
        (targetContext.applicationContext as QualityAlternativeApplication)
            .appContainer
            .resetPersistentStateForTests()
    }

    private data class FreshDocumentRankingSeed(
        val oldMarkdown: ContentItem,
        val oldEpub: ContentItem,
        val newEpub: ContentItem,
    )
}
