# Source Family Caps

Status: Slice 9.0 decision for Pro review.

## Purpose

Source caps prevent the candidate pool and future packs from looking like a feed from one publication or an implicit partnership with one source family.

## Caps

| Scope | Cap |
|---|---:|
| One modern source family in the full 100-row pool | 10 candidates |
| One source family in a future 10-item pack | 4 candidates |
| One author/work in a future pack | 3 renderable candidates unless explicitly approved |
| Deep-reference pages such as SEP/IEP in a future pack | Prefer 1-2; use as backups, not default primaries |

## Source Family Grouping

Group by recognizable source family, not only by host:

- `Long Now`
- `Aeon/Psyche`
- `Quanta`
- `SAPIENS`
- `Nautilus`
- `SEP`
- `IEP`
- `Project Gutenberg`
- `Wikisource`
- `NASA`
- `NOAA`
- `OWID`
- `Museum/Public Institution`
- `First-party/Commissioned`
- `Independent/Author-approved`

## Integration Rules

- Link-only rows from modern publications must remain `EXTERNAL_HANDOFF`.
- No source label may imply affiliation, partnership, or endorsement unless there is a real agreement.
- Homepage, search, collection, and `Source TBD` rows cannot count as Pro-ready candidates.
- Source-family caps are checked before any future pack is integrated into `starter_packs.json`.

## Sprint 9.0 Current Inventory Check

The current integrated link-only pack already follows the future pack cap: no more than four items from a single modern source label.
