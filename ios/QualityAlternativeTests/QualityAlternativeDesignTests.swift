import XCTest
@testable import QualityAlternative

final class QualityAlternativeDesignTests: XCTestCase {
    func testLightAndDarkTokensMatchAndroidPalette() {
        XCTAssertEqual(QATokens.tokens(for: .light).colors, QAColors.light)
        XCTAssertEqual(QATokens.tokens(for: .dark).colors, QAColors.dark)
    }

    func testSampleReplacementSessionStaysFinite() {
        let session = QASampleData.session

        XCTAssertEqual(session.backups.count, 2)
        XCTAssertEqual(session.primary.renderMode, .inAppReader)
        XCTAssertEqual(Set(session.backups.map(\.renderMode)), [.externalHandoff, .meditationTimer])
    }

    func testLibraryIncludesRenderableLinkOnlyAndMeditationItems() {
        let modes = Set(QASampleData.library.map(\.renderMode))

        XCTAssertTrue(modes.contains(.inAppReader))
        XCTAssertTrue(modes.contains(.externalHandoff))
        XCTAssertTrue(modes.contains(.meditationTimer))
    }
}
