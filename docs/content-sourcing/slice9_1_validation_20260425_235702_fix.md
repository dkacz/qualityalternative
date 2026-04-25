# Slice 9.1 Review-Fix Validation Transcript

Timestamp: 2026-04-25 23:57:02 Europe/Warsaw

## Commands

```sh
python3 -m json.tool docs/content-sourcing/content_candidate_backlog.schema.json >/dev/null
```

Result: pass.

```sh
python3 - <<'PY'
import csv,json
from pathlib import Path
root=Path('docs/content-sourcing')
schema=json.loads((root/'content_candidate_backlog.schema.json').read_text())
fields=[f['name'] for f in schema['fields']]
for name in ['content_candidate_backlog.csv','existing_inventory_audit.csv']:
    with (root/name).open(newline='') as f:
        rows=list(csv.reader(f))
    width=len(rows[0])
    bad=[i+1 for i,row in enumerate(rows) if len(row)!=width]
    if bad:
        raise SystemExit(f'{name}: inconsistent columns at rows {bad[:5]}')
    print(f'{name}: rows={len(rows)} columns={width}')
with (root/'content_candidate_backlog.csv').open(newline='') as f:
    reader=csv.DictReader(f)
    rows=list(reader)
assert reader.fieldnames == fields
assert len(rows)==24
assert sum(r['rights_class_candidate']=='RENDERABLE' for r in rows)==9
assert sum(r['rights_class_candidate']=='LINK_ONLY' for r in rows)==15
assert all(r['must_not_scrape_cache_or_summarize']=='true' for r in rows if r['rights_class_candidate']=='LINK_ONLY')
assert all((not r['canonical_url_verified_at'] and not r['canonical_url_verified_by']) for r in rows if r['verification_label']!='manually_verified_candidate')
print('slice9_1_csv_consistency_ok')
PY
```

Result:

```text
content_candidate_backlog.csv: rows=25 columns=80
existing_inventory_audit.csv: rows=46 columns=13
slice9_1_csv_consistency_ok
```

```sh
git diff --check
```

Result: pass.

```sh
git diff --name-only HEAD
git diff --exit-code -- app/src/main/assets/editorial/starter_packs.json
```

Result: changed files are limited to the Slice 9.1 CSV, summary, and unit test. `starter_packs.json` has no diff.

```sh
JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home \
ANDROID_HOME=/opt/homebrew/share/android-commandlinetools \
ANDROID_SDK_ROOT=/opt/homebrew/share/android-commandlinetools \
./gradlew testDebugUnitTest
```

Result: `BUILD SUCCESSFUL`.

```sh
JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home \
ANDROID_HOME=/opt/homebrew/share/android-commandlinetools \
ANDROID_SDK_ROOT=/opt/homebrew/share/android-commandlinetools \
./gradlew lintDebug
```

Result: `BUILD SUCCESSFUL`.
