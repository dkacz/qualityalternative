# Sprint 19 Intervention Mode Settings Fix

## Problem

Settings always rendered `Soft intervention` as selected and `Firm intervention` as unselected because the Mode section was hard-coded. The actual intervention behaved like Firm because `Open anyway` always had a 5-second form-intervention wait.

## Fix

- Added persisted `InterventionMode` with `SOFT` and `FIRM` values.
- Default remains `FIRM` to preserve the currently released behavior unless the user changes it.
- Settings Mode rows are now clickable, selectable, and backed by real app state.
- `SOFT` mode leaves `Open anyway` immediately available and does not record form-intervention wait events.
- `FIRM` mode keeps the 5-second wait and existing form-intervention analytics.
- Portable Profile export/import carries `interventionMode`, while older profiles without the field still import with the default.
- PRD FR7 now explicitly distinguishes Soft immediate override from Firm 5-second pause.

## Validation

- Unit: `logs/unit.log` -> `BUILD SUCCESSFUL`
- Connected visual E2E: `logs/connected_intervention_mode.log` -> `BUILD SUCCESSFUL`

## Visual Evidence

- `screenshots/13_intervention_mode_soft_selected.png`
- `screenshots/14_soft_mode_open_anyway_immediate.png`
- `screenshots/15_firm_mode_open_anyway_wait.png`
