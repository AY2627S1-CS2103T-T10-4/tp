---
layout: page
title: List Guardians Handover
---

# Feature 16: List guardians

## Delivered behavior

Mentora now has three navigation tabs in this order: **Students**, **Guardians**, and **Groups**. The Guardians
workspace follows the Students workspace's two-pane layout. Its list is sorted by normalized guardian name and then
stable guardian ID, and each row shows name, ID, canonical phone number, and current linked-student count. Selecting a
guardian displays the guardian's name, ID, phone number, and linked students (stable ID plus name) in the detail pane.

`list-guardians` is case-insensitive and accepts no arguments. A valid invocation clears any Guardian-list predicate,
switches to Guardians, and reports `Listed N guardian(s).`. With no records it reports `No guardians registered.` and
the empty list displays the same message. Extra input reports
`Invalid command format. Use: list-guardians`; because parsing fails before execution, the active tab and list remain
unchanged. The command is read-only, so it works during storage recovery and never triggers a save.

## Implementation map

Java paths below are relative to `src/main/java/seedu/address/`.

| Area | Files and responsibilities |
| --- | --- |
| Command and parser | `logic/commands/ListGuardiansCommand` clears the Guardian filter, creates exact feedback, and requests `CommandResult.Workspace.GUARDIANS`. `logic/parser/ListGuardiansCommandParser` rejects all non-blank arguments. `AddressBookParser` routes command names case-insensitively. |
| Command/UI boundary | `CommandResult.Workspace` includes `GUARDIANS`. `MainWindow` applies this request only after successful command execution, preserving the current workspace on parse/command failures. |
| Model | `Model` and `ModelManager` expose an independent `FilteredList<Guardian>` and `getLinkedStudents(String)`. Relationship projection uses the aggregate's stable-ID link map and returns Students sorted by normalized name then ID. The persisted `AddressBook` schema is unchanged. |
| Logic | `Logic` and `LogicManager` expose the observable Guardian view and linked-Student projection to the UI without leaking the mutable aggregate. |
| UI | `GuardianListPanel` owns the observable list and stable selection, `GuardianCard` renders row fields/counts, and `GuardianDetailsPanel` renders the selected contact and linked Students. `MainWindow` mounts one instance of each panel and reuses them across tab switches. |
| Layout and style | `MainWindow.fxml` defines the middle Guardians tab and two-pane workspace. Guardian-specific FXML files mirror existing Student components. `DarkTheme.css` adds phone/count styling and shares the existing details background. |

## Data and refresh behavior

Guardian records and student-to-guardian links remain owned by `AddressBook`; Feature 16 adds no duplicate storage and
does not change JSON. `ModelManager` wraps the aggregate Guardian list in a `FilteredList`, so future guardian search can
replace its predicate without rebuilding the UI. `list-guardians` restores `PREDICATE_SHOW_ALL_GUARDIANS`.

Linked-student counts and detail rows are computed from the current link map. MainWindow refreshes Guardian cards and
details after successful commands, including relationship-only changes that do not replace a Guardian object. Tab
switches retain both Student and Guardian selections. A failed command restores the existing Student selection and does
not change the Guardian workspace or filter.

## Tests

| Test | Coverage |
| --- | --- |
| `ListGuardiansCommandTest` | Count/empty feedback, filter clearing, Guardians workspace request, null model. |
| `ListGuardiansCommandParserTest` | Blank input, casing/whitespace, extra text, and exact format error. |
| `GuardianWorkspaceModelTest` | Independent Guardian filtering and sorted linked-Student projection. |
| `GuardianCardTest` / `GuardianDetailsPanelTest` | Row fields/count and selected/empty detail states. |
| `ListGuardiansUiTest` | Three-tab order/state, real command execution, sorted observable rows, contact/link details, and rejected-command workspace preservation. |

Recommended verification from the repository root:

```powershell
.\gradlew.bat check coverage shadowJar --rerun-tasks
git diff --check
```

## Extension points and cautions

- Feature 17 can add a Guardian-name predicate and `find-guardian` command against the existing filtered view. Keep
  authoritative ID lookup and persistence independent of the filter.
- A future guardian edit command must explicitly refresh the selected details after replacing an immutable Guardian,
  just as Student edits are refreshed now.
- A future unlink command must refresh both the count cards and selected profile after persistence succeeds. Do not use
  UI metadata as the source of relationship truth.
- Keep `list-guardians` read-only and retain its no-arguments parser contract; recovery mode depends on that classification.
