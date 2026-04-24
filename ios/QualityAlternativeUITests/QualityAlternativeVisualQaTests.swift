import XCTest

@MainActor
final class QualityAlternativeVisualQaTests: XCTestCase {
    override func setUpWithError() throws {
        continueAfterFailure = false
    }

    func testSlice12VisualParityScreens() throws {
        capture(route: "home", name: "01_home_light")
        capture(route: "library", name: "02_library_light")
        capture(route: "intervention", name: "03_intervention_light", verifyInterventionActions: true)
        capture(route: "reader", name: "04_reader_light")
        capture(route: "handoff", name: "05_handoff_light")
        capture(route: "meditation", name: "06_meditation_light")
        capture(route: "progress", name: "07_progress_light")
        capture(route: "settings", name: "08_settings_light")
        capture(route: "intervention", name: "09_intervention_dark", dark: true, verifyInterventionActions: true)
        capture(route: "reader", name: "10_reader_dark", dark: true)
        capture(route: "meditation", name: "11_meditation_dark", dark: true)
    }

    private func capture(
        route: String,
        name: String,
        dark: Bool = false,
        verifyInterventionActions: Bool = false
    ) {
        let app = XCUIApplication()
        app.launchArguments = ["--qa-route", route]
        if dark {
            app.launchArguments.append("--qa-dark")
        }
        app.launch()

        let expectedScreen = route == "handoff" ? "handoff-screen" : "\(route)-screen"
        XCTAssertTrue(app.descendants(matching: .any)[expectedScreen].waitForExistence(timeout: 6))
        if verifyInterventionActions {
            assertInterventionActionSet(in: app)
        }

        let attachment = XCTAttachment(screenshot: XCUIScreen.main.screenshot())
        attachment.name = name
        attachment.lifetime = .keepAlways
        add(attachment)

        app.terminate()
    }

    private func assertInterventionActionSet(in app: XCUIApplication) {
        XCTAssertTrue(app.buttons["Read this"].exists)
        XCTAssertTrue(app.buttons["Open link-only backup - 8 min"].exists)
        XCTAssertTrue(app.buttons["Start meditation - 3 min"].exists)
        XCTAssertTrue(app.buttons["Pause for 15 min"].exists)
        XCTAssertTrue(app.buttons["Continue intentionally"].exists)

        let actions = app.descendants(matching: .any)
        XCTAssertTrue(actions["backup-action-psyche-boredom"].exists)
        XCTAssertTrue(actions["backup-action-meditation-3m"].exists)
        XCTAssertFalse(actions["backup-action-naturalist-notices"].exists)
        XCTAssertTrue(actions["pause-action"].exists)
        XCTAssertTrue(actions["continue-intentionally-action"].exists)
    }
}
