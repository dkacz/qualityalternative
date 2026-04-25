# Slice 9.3 Review-Fix Validation Transcript

Timestamp: 2026-04-26 01:31:32 Europe/Warsaw

Branch: `codex/android-content-source-expansion-sprint9`

## Fix Scope

GPT Pro returned `REVISE` for two metadata consistency issues:

- `s9-3-r02-fabre-life-spider` mentioned diagram/species-image avoidance in prose but did not set structured image-dependency risk fields.
- Four Quanta rows described an open-page spot check in prose but still used the search-result-only verification method.

The fix keeps Slice 9.3 sourcing-only and does not integrate app content or change runtime behavior.

## Commands

```sh
python3 -m json.tool docs/content-sourcing/content_candidate_backlog.schema.json >/dev/null
```

Result: pass.

```sh
python3 - <<'PY'
import csv, re
from collections import Counter
from pathlib import Path

root = Path('/Users/omare/Documents/qualityalternative')
rows = list(csv.DictReader((root / 'docs/content-sourcing/content_candidate_backlog.csv').open()))
slice93 = [row for row in rows if row['vertical_slice'] == '9.3']
by_id = {row['candidate_id']: row for row in slice93}
source_counts = Counter(row['source_family_cap_group'] for row in rows)
starter = (root / 'app/src/main/assets/editorial/starter_packs.json').read_text()
shipped_urls = set(re.findall(r'"sourceUrl"\s*:\s*"([^"]+)"', starter))

assert len(rows) == 70
assert Counter(row['vertical_slice'] for row in rows) == Counter({'9.1': 24, '9.2': 20, '9.3': 26})
assert sum(row['rights_class_candidate'] == 'RENDERABLE' for row in slice93) == 11
assert sum(row['rights_class_candidate'] == 'LINK_ONLY' for row in slice93) == 15
assert Counter(row['replacement_moment'] for row in slice93) == Counter({'WONDER_CURIOSITY': 14, 'SCIENCE_CURIOSITY': 12})
assert all(row['must_not_scrape_cache_or_summarize'] == 'true' for row in slice93 if row['rights_class_candidate'] == 'LINK_ONLY')
assert all(row['candidate_status'] != 'approved_for_future_integration' for row in rows)
assert all(count <= 10 for group, count in source_counts.items() if group != 'Project Gutenberg')
assert all(row['canonical_url'] not in shipped_urls for row in slice93)
assert by_id['s9-3-r02-fabre-life-spider']['third_party_asset_risk'] == 'diagram_image_review_needed'
assert by_id['s9-3-r02-fabre-life-spider']['image_chart_dependency'] == 'image_dependent_sections_avoid'
for candidate_id in [
    's9-3-l01-quanta-life-complexity',
    's9-3-l02-quanta-quantumness',
    's9-3-l03-quanta-uncertainty-measurements',
    's9-3-l04-quanta-universe-shape',
]:
    assert by_id[candidate_id]['url_verification_method'] == 'open_page_spot_check_needs_manual_verification'
print('slice9_3_review_fix_csv_consistency_ok')
PY
```

Result:

```text
slice9_3_review_fix_csv_consistency_ok
```

```sh
git diff --check
```

Result: pass.

```sh
env JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home ANDROID_HOME=/opt/homebrew/share/android-commandlinetools ANDROID_SDK_ROOT=/opt/homebrew/share/android-commandlinetools ./gradlew testDebugUnitTest
```

Result: `BUILD SUCCESSFUL`.

```sh
env JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home ANDROID_HOME=/opt/homebrew/share/android-commandlinetools ANDROID_SDK_ROOT=/opt/homebrew/share/android-commandlinetools ./gradlew lintDebug
```

Result: `BUILD SUCCESSFUL`.
