package com.qualityalternative.app

import android.content.Intent
import android.net.Uri
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.click
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.longClick
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
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeRight
import androidx.compose.ui.test.swipeUp
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import com.qualityalternative.app.domain.model.AppThemeMode
import com.qualityalternative.app.domain.model.ContentFormat
import com.qualityalternative.app.domain.model.ContentItem
import com.qualityalternative.app.domain.model.DurationBucket
import com.qualityalternative.app.domain.model.AnalyticsEventType
import com.qualityalternative.app.domain.model.MEDITATION_TIMER_CONTENT_ID
import com.qualityalternative.app.domain.model.OnboardingSelection
import com.qualityalternative.app.domain.model.ReadingAnnotationDraft
import com.qualityalternative.app.domain.model.ReadingAnnotationSelector
import com.qualityalternative.app.domain.model.ReadingProgress
import com.qualityalternative.app.domain.model.TopicTag
import com.qualityalternative.app.domain.model.UserDocumentDraft
import com.qualityalternative.app.domain.service.AddUserDocumentResult
import com.qualityalternative.app.interception.FixtureTargetRegistry
import com.qualityalternative.app.interception.InterceptionRuntimeGate
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
    private val sprint15DriveSyncScreenshotDirName =
        "sprint15-drive-sync-${System.currentTimeMillis()}"
    private val sprint16PortableProfileScreenshotDirName =
        "sprint16-portable-profile-${System.currentTimeMillis()}"
    private val sprint16ProfileAutosaveScreenshotDirName =
        "sprint16-profile-autosave-${System.currentTimeMillis()}"
    private val sprint16AdaptiveReaderScreenshotDirName =
        "sprint16-adaptive-reader-${System.currentTimeMillis()}"

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
        composeRule.onNodeWithText("Open Fixture Feed One")
            .assertIsDisplayed()
            .assertIsEnabled()
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
        val deletedLinkId = "user-link:918123c9245605b90800494db814f2b6282ee47d915b479427b52aa7fe1b9805"
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
    fun readerFontSizeSettingUsesAppLevelPreference() {
        launchOnboardedApp()

        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("tab-settings") }
        composeRule.onNodeWithTag("tab-settings", useUnmergedTree = true)
            .performSemanticsAction(SemanticsActions.OnClick)
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("settings-list") }
        composeRule.onNodeWithTag("settings-list")
            .performScrollToNode(hasTestTag("reader-font-scale-130"))
        composeRule.onNodeWithTag("reader-font-scale-130")
            .assertIsDisplayed()
            .performSemanticsAction(SemanticsActions.OnClick)
        composeRule.waitUntil(timeoutMillis = 10_000) { currentReaderFontScale() == 1.3 }
        captureSprint16AdaptiveReaderScreenshot("00_reader_font_setting_xl")

        scenario?.close()
        scenario = null
        launchFixtureSystemIntervention()
        composeRule.onNodeWithText("Read this", substring = true)
            .assertIsDisplayed()
            .performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("reader-screen") }
        assertEquals(1.3, currentReaderFontScale(), 0.0)
        composeRule.onNodeWithTag("reader-page-label").assertIsDisplayed()
        captureSprint16AdaptiveReaderScreenshot("01_reader_xl_font_light")

        scenario?.onActivity { activity -> activity.mainViewModel.setReaderFontScale(0.9) }
        composeRule.waitUntil(timeoutMillis = 10_000) { currentReaderFontScale() == 0.9 }
        composeRule.onNodeWithTag("reader-page-label").assertIsDisplayed()
        captureSprint16AdaptiveReaderScreenshot("02_reader_small_font_light")
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
        composeRule.waitUntil(timeoutMillis = 10_000) { hasNodeContaining("$savedPercent%") }
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
        composeRule.onNodeWithContentDescription("Move end earlier")
            .assertIsDisplayed()
        composeRule.onNodeWithTag("reader-annotation-end-earlier")
            .assertIsDisplayed()
            .assertIsEnabled()
            .performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) {
            readerSelectedQuoteText(annotationParagraph).length < initialSelectedQuote.length
        }
        val refinedSelectedQuote = readerSelectedQuoteText(annotationParagraph)
        val refinedQuoteProbe = refinedSelectedQuote.take(40)
        assertTrue("Expected the icon range control to keep a non-empty selected quote", refinedQuoteProbe.isNotBlank())
        val firstNote = "Worth remembering when the impulse hits."
        composeRule.onNodeWithTag("reader-annotation-note-input-$annotationParagraph")
            .assertIsDisplayed()
            .performTextInput(firstNote)
        UiDevice.getInstance(InstrumentationRegistry.getInstrumentation()).pressBack()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("reader-annotation-save-$annotationParagraph").assertIsDisplayed()
        captureSprint14ReaderAnnotationScreenshot("01_reader_annotation_editor_light")

        composeRule.onNodeWithTag("reader-annotation-save-$annotationParagraph")
            .assertIsDisplayed()
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
            .performTextInput(updatedNoteSuffix)
        UiDevice.getInstance(InstrumentationRegistry.getInstrumentation()).pressBack()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("reader-annotation-save-$annotationParagraph")
            .assertIsDisplayed()
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
        val crossPageText = (1..120)
            .joinToString(separator = " ") { index -> "anchor$index" }
            .plus(".")
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
        composeRule.onNodeWithText("Save now").assertIsDisplayed()
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
    fun annotationDriveSyncSettingsShowsConnectAndRecoverableAuthFailure() {
        launchOnboardedApp()
        scenario?.onActivity { activity -> activity.mainViewModel.openSettings() }
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("settings-list") }
        composeRule.onNodeWithTag("settings-list")
            .performScrollToNode(hasTestTag("settings-annotation-drive-connect"))
        composeRule.onNodeWithText("Google Drive not connected").assertIsDisplayed()
        composeRule.onNodeWithTag("settings-annotation-drive-status").assertIsDisplayed()
        composeRule.onNodeWithText("Connect").assertIsDisplayed()
        composeRule.onNodeWithTag("settings-annotation-drive-disconnect").assertIsNotEnabled()
        captureSprint15DriveSyncScreenshot("01_drive_connect_light")

        scenario?.onActivity { activity ->
            activity.mainViewModel.beginAnnotationDriveAuthorization()
            activity.mainViewModel.reportAnnotationDriveAuthorizationFailure("Google Drive permission was not granted.")
        }
        composeRule.waitUntil(timeoutMillis = 10_000) {
            hasNode("GOOGLE DRIVE PERMISSION WAS NOT GRANTED")
        }
        composeRule.onNodeWithTag("settings-list")
            .performScrollToNode(hasTestTag("settings-annotation-drive-status"))
        composeRule.onNodeWithText("GOOGLE DRIVE PERMISSION WAS NOT GRANTED", substring = true).assertIsDisplayed()
        composeRule.onNodeWithTag("settings-annotation-drive-connect").assertIsEnabled()
        scenario?.onActivity { activity -> activity.mainViewModel.dismissMessage() }
        composeRule.waitForIdle()
        captureSprint15DriveSyncScreenshot("02_drive_auth_failure_light")
    }

    @Test
    fun accountLightImportSettingsShowsPreviewErrorsConfirmationAndSuccess() {
        launchOnboardedApp()
        scenario?.onActivity { activity -> activity.mainViewModel.openSettings() }
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("settings-list") }

        scrollToAccountLightSettings()
        composeRule.onNodeWithTag("settings-account-light-section").assertIsDisplayed()
        composeRule.onNodeWithTag("settings-account-light-import").assertIsDisplayed()
        captureSprint16PortableProfileScreenshot("01_import_entry_light")

        scenario?.onActivity { activity ->
            activity.mainViewModel.previewAccountLightImport(
                accountLightProfileJson(
                    schemaVersion = 1,
                    selectedAppPackages = listOf("com.instagram.android", "com.future.reader"),
                ),
            )
        }
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("settings-account-light-import-preview") }
        scrollToAccountLightSettings()
        composeRule.onNodeWithTag("settings-account-light-import-preview").assertIsDisplayed()
        composeRule.onNodeWithTag("settings-account-light-import-warning-summary").assertIsDisplayed()
        captureSprint16PortableProfileScreenshot("02_merge_preview_with_unsupported_app_light")

        composeRule.onNodeWithTag("settings-account-light-import-replace")
            .assertIsDisplayed()
            .performClick()
        composeRule.onNodeWithTag("settings-account-light-replace-confirm").assertIsDisplayed()
        composeRule.onNodeWithTag("settings-list")
            .performScrollToNode(hasTestTag("settings-account-light-replace-backup"))
        composeRule.onNodeWithTag("settings-account-light-replace-backup").assertIsDisplayed()
        composeRule.onNodeWithTag("settings-list")
            .performScrollToNode(hasTestTag("settings-account-light-replace-confirm-action"))
        captureSprint16PortableProfileScreenshot("03_replace_confirmation_light")

        composeRule.onNodeWithTag("settings-account-light-replace-confirm-action")
            .assertIsDisplayed()
            .performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) {
            hasNodeContaining("IMPORTED PROFILE REPLACED LOCAL PORTABLE SETTINGS AND LIBRARY")
        }
        scrollToAccountLightSettings()
        composeRule.onNodeWithTag("settings-account-light-status").assertIsDisplayed()
        captureSprint16PortableProfileScreenshot("04_import_success_dark")

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
        captureSprint16PortableProfileScreenshot("07_missing_document_library_dark")

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
        captureSprint16PortableProfileScreenshot("05_invalid_import_dark")

        scenario?.onActivity { activity ->
            activity.mainViewModel.previewAccountLightImport(accountLightProfileJson(schemaVersion = 99))
        }
        composeRule.waitUntil(timeoutMillis = 10_000) {
            hasNodeContaining("UNSUPPORTED PORTABLE PROFILE SCHEMA VERSION 99")
        }
        scrollToAccountLightSettings()
        composeRule.onNodeWithTag("settings-account-light-status").assertIsDisplayed()
        captureSprint16PortableProfileScreenshot("06_future_schema_import_dark")
    }

    @Test
    fun accountLightProfileAutosaveSettingsShowsDestinationSuccessAndRecoverableFailure() {
        launchOnboardedApp()
        scenario?.onActivity { activity -> activity.mainViewModel.openSettings() }
        composeRule.waitUntil(timeoutMillis = 10_000) { hasTag("settings-list") }

        scrollToAccountLightSettings()
        composeRule.onNodeWithTag("settings-account-light-autosave-status").assertIsDisplayed()
        composeRule.onNodeWithText("No autosave folder selected").assertIsDisplayed()
        composeRule.onNodeWithTag("settings-account-light-autosave-pick").assertIsDisplayed()
        composeRule.onNodeWithTag("settings-account-light-autosave-save-now").assertIsNotEnabled()
        captureSprint16ProfileAutosaveScreenshot("01_profile_autosave_empty_light")

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
        composeRule.onNodeWithText("AUTOSAVE FAILED", substring = true).assertIsDisplayed()
        composeRule.onNodeWithTag("settings-account-light-autosave-retry").assertIsDisplayed()
        scenario?.onActivity { activity -> activity.mainViewModel.dismissMessage() }
        composeRule.waitForIdle()
        captureSprint16ProfileAutosaveScreenshot("03_profile_autosave_failure_dark")
    }

    @Test
    fun meditationAlternativeOpensThreeMinuteTimer() {
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
                "readerFontScale": 1.25,
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
        return (0..80).filter { index ->
            hasContentDescriptionNode("reader-first-visible-paragraph-$index")
        }
    }

    private fun visibleAnnotationHighlightIndices(): List<Int> {
        return (0..120).filter { index ->
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

    private fun readerSelectedQuoteText(paragraphIndex: Int): String {
        return composeRule.onNodeWithTag("reader-annotation-selected-quote-$paragraphIndex")
            .fetchSemanticsNode()
            .config[SemanticsProperties.Text]
            .joinToString(separator = "\n") { text -> text.text }
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
            .performTextInput(noteText)
        UiDevice.getInstance(InstrumentationRegistry.getInstrumentation()).pressBack()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("reader-annotation-save-$paragraphIndex")
            .assertIsDisplayed()
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

    private fun captureSprint15DriveSyncScreenshot(name: String) {
        val outputDir = File("/sdcard/Download/qualityalternative/$sprint15DriveSyncScreenshotDirName")
        assertTrue("Expected screenshot output directory for $name", outputDir.mkdirs() || outputDir.exists())
        composeRule.waitForIdle()
        Thread.sleep(300)
        val output = File(outputDir, "$name.png")
        assertTrue("Expected screenshot capture for $name", UiDevice.getInstance(InstrumentationRegistry.getInstrumentation()).takeScreenshot(output))
        assertTrue("Expected screenshot file for $name", output.exists() && output.length() > 0L)
    }

    private fun captureSprint16PortableProfileScreenshot(name: String) {
        val outputDir = File("/sdcard/Download/qualityalternative/$sprint16PortableProfileScreenshotDirName")
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
