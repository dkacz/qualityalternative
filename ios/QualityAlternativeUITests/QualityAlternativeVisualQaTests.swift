import XCTest

@MainActor
final class QualityAlternativeVisualQaTests: XCTestCase {
    override func setUpWithError() throws {
        continueAfterFailure = false
    }

    func testSprint14SimulatorParityScreens() throws {
        capture(route: "home", name: "01_home_light")
        capture(route: "library", name: "02_library_light")
        capture(route: "intervention", name: "03_intervention_light", verifyInterventionActions: true)
        capture(route: "reader", name: "04_reader_light")
        capture(route: "handoff", name: "05_handoff_light")
        capture(route: "meditation", name: "06_meditation_light")
        capture(route: "progress", name: "07_progress_light")
        capture(route: "settings", name: "08_settings_light", verifyScreenTimeSetup: true)
        capture(route: "addLink", name: "09_add_link_light", verifyAddLink: true)
        capture(route: "addDocument", name: "10_add_document_light", verifyAddDocument: true)
        capture(route: "feedback", name: "11_feedback_light")
        capture(route: "intervention", name: "12_intervention_dark", dark: true, verifyInterventionActions: true)
        capture(route: "reader", name: "13_reader_dark", dark: true)
        capture(route: "meditation", name: "14_meditation_dark", dark: true)
        capture(route: "settings", name: "15_device_activity_light", verifyDeviceActivityMonitor: true)
        capture(route: "reader", name: "16_private_markdown_reader_light", contentID: "ios-private-markdown")
        capture(route: "reader", name: "17_private_epub_reader_light", contentID: "ios-private-epub")
        capture(route: "handoff", name: "18_private_pdf_handoff_light", contentID: "ios-private-pdf", verifyExternalHandoff: true)
        captureLibraryFilesFilter()
        captureSettingsDefaultSessionLength()
        captureActiveDelayAfterPause()
        assertAddLinkPersistsAfterRelaunch()
    }

    private func capture(
        route: String,
        name: String,
        dark: Bool = false,
        contentID: String? = nil,
        verifyInterventionActions: Bool = false,
        verifyScreenTimeSetup: Bool = false,
        verifyDeviceActivityMonitor: Bool = false,
        verifyAddLink: Bool = false,
        verifyAddDocument: Bool = false,
        verifyExternalHandoff: Bool = false
    ) {
        let app = XCUIApplication()
        app.launchArguments = ["--qa-route", route, "--qa-reset-local-state"]
        if let contentID {
            app.launchArguments += ["--qa-content-id", contentID]
        }
        if dark {
            app.launchArguments.append("--qa-dark")
        }
        app.launch()

        let expectedScreen = expectedScreenIdentifier(for: route)
        XCTAssertTrue(app.descendants(matching: .any)[expectedScreen].waitForExistence(timeout: 6))
        if verifyInterventionActions {
            assertInterventionActionSet(in: app)
        }
        if verifyScreenTimeSetup {
            assertScreenTimeSetup(in: app)
        }
        if verifyDeviceActivityMonitor {
            assertDeviceActivityMonitor(in: app)
        }
        if verifyAddLink {
            assertAddLink(in: app)
        }
        if verifyAddDocument {
            assertAddDocument(in: app)
        }
        if verifyExternalHandoff {
            assertExternalHandoff(in: app)
        }

        let attachment = XCTAttachment(screenshot: XCUIScreen.main.screenshot())
        attachment.name = name
        attachment.lifetime = .keepAlways
        add(attachment)

        app.terminate()
    }

    private func captureLibraryFilesFilter() {
        let app = XCUIApplication()
        app.launchArguments = ["--qa-route", "library", "--qa-reset-local-state"]
        app.launch()

        XCTAssertTrue(app.descendants(matching: .any)["library-screen"].waitForExistence(timeout: 6))
        app.buttons["Files"].tap()
        XCTAssertTrue(app.staticTexts["Private Markdown Note"].waitForExistence(timeout: 3))
        XCTAssertTrue(app.staticTexts["Private EPUB Extract"].exists)
        let pdfRow = app.staticTexts["Private PDF Handoff"]
        XCTAssertTrue(pdfRow.exists)
        let scrollView = app.scrollViews.firstMatch
        for _ in 0..<3 where !pdfRow.isHittable {
            scrollView.swipeUp()
        }
        XCTAssertTrue(pdfRow.isHittable)

        let attachment = XCTAttachment(screenshot: XCUIScreen.main.screenshot())
        attachment.name = "19_library_files_filter_light"
        attachment.lifetime = .keepAlways
        add(attachment)

        app.terminate()
    }

    private func captureSettingsDefaultSessionLength() {
        let app = XCUIApplication()
        app.launchArguments = ["--qa-route", "settings", "--qa-reset-local-state"]
        app.launch()

        XCTAssertTrue(app.descendants(matching: .any)["settings-screen"].waitForExistence(timeout: 6))
        let scrollView = app.scrollViews.firstMatch
        let title = app.staticTexts["Default session length"]
        for _ in 0..<5 where !title.isHittable {
            scrollView.swipeUp()
        }
        XCTAssertTrue(title.exists)
        XCTAssertTrue(app.buttons["duration-15"].exists)

        let attachment = XCTAttachment(screenshot: XCUIScreen.main.screenshot())
        attachment.name = "20_settings_default_session_light"
        attachment.lifetime = .keepAlways
        add(attachment)

        app.terminate()
    }

    private func captureActiveDelayAfterPause() {
        let app = XCUIApplication()
        app.launchArguments = ["--qa-route", "intervention", "--qa-reset-local-state"]
        app.launch()

        XCTAssertTrue(app.descendants(matching: .any)["intervention-screen"].waitForExistence(timeout: 6))
        app.buttons["Pause for 15 min"].tap()
        XCTAssertTrue(app.descendants(matching: .any)["home-screen"].waitForExistence(timeout: 3))
        let activeDelay = app.descendants(matching: .any)["active-delay-state"]
        XCTAssertTrue(activeDelay.waitForExistence(timeout: 3))
        let scrollView = app.scrollViews.firstMatch
        for _ in 0..<4 where !activeDelay.isHittable {
            scrollView.swipeUp()
        }
        XCTAssertTrue(activeDelay.isHittable)

        let attachment = XCTAttachment(screenshot: XCUIScreen.main.screenshot())
        attachment.name = "21_home_active_delay_light"
        attachment.lifetime = .keepAlways
        add(attachment)

        app.terminate()
    }

    private func assertAddLinkPersistsAfterRelaunch() {
        let app = XCUIApplication()
        app.launchArguments = ["--qa-route", "addLink", "--qa-reset-local-state"]
        app.launch()

        XCTAssertTrue(app.descendants(matching: .any)["add-link-screen"].waitForExistence(timeout: 6))
        replaceText(in: app.textFields["add-link-title-field"], with: "Simulator Saved Link")
        replaceText(in: app.textFields["add-link-url-field"], with: "https://example.com/simulator-saved-link")
        app.buttons["Add to library"].tap()
        XCTAssertTrue(app.descendants(matching: .any)["library-screen"].waitForExistence(timeout: 3))
        XCTAssertTrue(app.staticTexts["Simulator Saved Link"].waitForExistence(timeout: 3))

        app.terminate()
        let relaunched = XCUIApplication()
        relaunched.launchArguments = ["--qa-route", "library"]
        relaunched.launch()
        XCTAssertTrue(relaunched.descendants(matching: .any)["library-screen"].waitForExistence(timeout: 6))
        XCTAssertTrue(relaunched.staticTexts["Simulator Saved Link"].waitForExistence(timeout: 3))
        relaunched.terminate()
    }

    private func expectedScreenIdentifier(for route: String) -> String {
        switch route {
        case "addLink":
            "add-link-screen"
        case "addDocument":
            "add-document-screen"
        case "handoff":
            "handoff-screen"
        default:
            "\(route)-screen"
        }
    }

    private func assertInterventionActionSet(in app: XCUIApplication) {
        XCTAssertTrue(app.staticTexts["PROTECTED SELECTION"].exists)
        XCTAssertFalse(app.staticTexts["OPENING INSTAGRAM"].exists)
        XCTAssertTrue(app.buttons["Pause for 15 min"].exists)
        XCTAssertTrue(app.buttons["Continue intentionally"].exists)

        let actions = app.descendants(matching: .any)
        XCTAssertTrue(actions["primary-replacement-action"].exists)
        XCTAssertTrue(actions["backup-action-0"].exists)
        XCTAssertTrue(actions["backup-action-1"].exists)
        XCTAssertFalse(actions["backup-action-2"].exists)
        XCTAssertTrue(actions["pause-action"].exists)
        XCTAssertTrue(actions["continue-intentionally-action"].exists)
    }

    private func assertScreenTimeSetup(in app: XCUIApplication) {
        XCTAssertTrue(app.staticTexts["Screen Time setup"].exists)
        XCTAssertTrue(app.staticTexts["Permission not requested"].exists)
        XCTAssertTrue(app.staticTexts["No shield yet"].exists)
        XCTAssertTrue(app.staticTexts["No protected apps, categories, or websites selected yet."].exists)
        XCTAssertTrue(app.buttons["Request Screen Time access"].exists)
        XCTAssertTrue(app.buttons["Choose protected apps"].exists)
        XCTAssertTrue(app.staticTexts["Shield controls"].exists)
        XCTAssertTrue(app.staticTexts["No shield session"].exists)
        XCTAssertTrue(app.staticTexts["Needs setup"].exists)
        if !app.buttons["Apply shield rules"].exists {
            app.scrollViews.firstMatch.swipeUp()
        }
        XCTAssertTrue(app.buttons["Apply shield rules"].exists)
        XCTAssertTrue(app.staticTexts["Content priority"].exists)
        XCTAssertTrue(app.buttons["content-priority-balanced"].exists)
        XCTAssertTrue(app.buttons["content-priority-readings"].exists)
        XCTAssertTrue(app.buttons["content-priority-myFiles"].exists)
        XCTAssertTrue(app.buttons["content-priority-savedLinks"].exists)
        XCTAssertTrue(app.buttons["content-priority-meditation"].exists)
        XCTAssertTrue(app.staticTexts["Default session length"].exists)
        XCTAssertTrue(app.buttons["duration-3"].exists)
        XCTAssertTrue(app.buttons["duration-5"].exists)
    }

    private func assertDeviceActivityMonitor(in app: XCUIApplication) {
        let scrollView = app.scrollViews.firstMatch
        let title = app.staticTexts["Device Activity monitor"]
        for _ in 0..<4 where !title.isHittable {
            scrollView.swipeUp()
        }
        XCTAssertTrue(title.exists)
        XCTAssertTrue(title.isHittable)
        XCTAssertTrue(app.staticTexts["No monitor schedule"].exists)
        XCTAssertTrue(app.staticTexts["Needs setup"].exists)
        XCTAssertTrue(app.buttons["Start monitor schedule"].exists)
    }

    private func assertAddLink(in app: XCUIApplication) {
        let actions = app.descendants(matching: .any)
        XCTAssertTrue(app.staticTexts["SAVED LINK"].exists)
        XCTAssertTrue(app.staticTexts["Link"].exists)
        XCTAssertTrue(actions["add-link-save"].exists)
        XCTAssertTrue(actions["add-link-import-document"].exists)
    }

    private func assertAddDocument(in app: XCUIApplication) {
        let actions = app.descendants(matching: .any)
        XCTAssertTrue(app.staticTexts["PRIVATE FILE"].exists)
        XCTAssertTrue(app.staticTexts["EPUB fixture"].exists)
        XCTAssertTrue(actions["document-format-markdown"].exists)
        XCTAssertTrue(actions["document-format-epub"].exists)
        XCTAssertTrue(actions["document-format-pdf"].exists)
        XCTAssertTrue(actions["add-document-save"].exists)
        let pdfScopeCopy = app.staticTexts
            .matching(NSPredicate(format: "label CONTAINS %@", "PDF remains an external handoff"))
            .firstMatch
        XCTAssertTrue(pdfScopeCopy.exists)
    }

    private func assertExternalHandoff(in app: XCUIApplication) {
        XCTAssertTrue(app.descendants(matching: .any)["external-open-action"].exists)
        XCTAssertTrue(app.descendants(matching: .any)["external-link-done"].exists)
    }

    private func replaceText(in element: XCUIElement, with text: String) {
        XCTAssertTrue(element.waitForExistence(timeout: 3))
        element.tap()
        if let current = element.value as? String, !current.isEmpty {
            element.typeText(String(repeating: XCUIKeyboardKey.delete.rawValue, count: current.count))
        }
        element.typeText(text)
    }
}
