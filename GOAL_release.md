# /goal (zwięzły)

Doprowadź gałąź `codex/sprint25-md-image-embeds` do wydania nowego debug APK po pełnej zielonej bramce e2e.

Napraw wszystkie znaleziska z `CODE_REVIEW_E2E_2026-06-03.md` (24 plus realny błąd postępu czytania). Kontrakt przed kodem dla każdego plastra. Dwie duże pozycje, rozbicie `MainViewModel` i scalanie Drive (If-Match), jako osobne plastry z pełnym e2e po każdej.

Sukces: 386 testów jednostkowych zielonych ORAZ `connectedDebugAndroidTest` 126/126, w tym wszystkie 16 `VisualQaScreenshotTest` i oba naprawione `MainActivityTest`. Weryfikacja z XML `app/build/outputs/androidTest-results/connected/debug/*.xml` (`failures=0`, `errors=0`). Następnie zbudowany APK debug plus `.sha256`, podbity `versionCode`/`versionName`, scommitowane na gałęzi, bez merge do `main`.

Twarda precondition: wizualne tylko na emulatorze z akceleracją (`qaApi36` z `-gpu host`). Najpierw zielony baseline sześciu dziś padających wizualnych BEZ zmian w kodzie. Dowód, że to środowisko: `evidence/sprint25_markdown_media_tables/android-results-r3/`.

Długie biegi e2e prowadź przez `/quasi-goal`. Pełny plan, kolejność plastrów, zakazy i lista znalezisk: `CODE_REVIEW_E2E_2026-06-03.md`. Otwarta decyzja: debug czy release.
