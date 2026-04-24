import XCTest

final class QualityAlternativeVisualQaTests: XCTestCase {
    override func setUpWithError() throws {
        continueAfterFailure = false
    }

    func testSlice12VisualParityScreens() throws {
        capture(route: "home", name: "01_home_light")
        capture(route: "library", name: "02_library_light")
        capture(route: "intervention", name: "03_intervention_light")
        capture(route: "reader", name: "04_reader_light")
        capture(route: "handoff", name: "05_handoff_light")
        capture(route: "meditation", name: "06_meditation_light")
        capture(route: "progress", name: "07_progress_light")
        capture(route: "settings", name: "08_settings_light")
        capture(route: "intervention", name: "09_intervention_dark", dark: true)
        capture(route: "reader", name: "10_reader_dark", dark: true)
        capture(route: "meditation", name: "11_meditation_dark", dark: true)
    }

    private func capture(route: String, name: String, dark: Bool = false) {
        let app = XCUIApplication()
        app.launchArguments = ["--qa-route", route]
        if dark {
            app.launchArguments.append("--qa-dark")
        }
        app.launch()

        let expectedScreen = route == "handoff" ? "handoff-screen" : "\(route)-screen"
        XCTAssertTrue(app.descendants(matching: .any)[expectedScreen].waitForExistence(timeout: 6))

        let attachment = XCTAttachment(screenshot: XCUIScreen.main.screenshot())
        attachment.name = name
        attachment.lifetime = .keepAlways
        add(attachment)

        app.terminate()
    }
}
