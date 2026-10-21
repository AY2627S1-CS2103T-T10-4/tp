# Update Guardian Phone Handover

## Implemented behavior

`edit-guardian gid/GUARDIAN_ID p/PHONE` parses the guardian ID as stable text and validates the phone through the shared `GuardianPhone` value type. The value type normalizes supported Singapore numbers to `+65 XXXXXXXX`. Duplicate phones are rejected with the ID of the guardian already using the number. Unknown guardian IDs and malformed parameters use the command contract's specified errors.

## Implementation flow

- `AddressBookParser` routes `edit-guardian` to `EditGuardianCommandParser`.
- `EditGuardianCommand` resolves the guardian, checks phone uniqueness, and replaces that record with the same ID and name and the new phone. The success result reports the number of linked students.
- `Model` and `ModelManager` expose guardian replacement; `AddressBook.updateGuardian` validates identity and replaces the guardian in its observable list. Guardian links remain keyed by the unchanged ID, so all linked student profiles resolve the updated phone.
- `LogicManager` persists the mutation with the other commands. Its address-book snapshot rollback restores the old number when saving fails; the command-specific failure text reports that the guardian was not updated.
- After each successful command, `MainWindow` refreshes the selected student details from the model, so an open linked profile displays the latest guardian phone.

## Verification

Tests cover malformed, missing, repeated and unknown parameters; phone validation and canonicalization; multiple linked students; normalized duplicate detection; persistence after reload; and save-failure rollback. A JavaFX test checks that the currently open profile and another linked student's profile immediately display the changed number. Run:

```sh
./gradlew test --tests seedu.address.logic.parser.EditGuardianCommandParserTest \
  --tests seedu.address.logic.commands.EditGuardianCommandTest \
  --tests seedu.address.logic.LogicManagerTest \
  --tests seedu.address.ui.EditGuardianPhoneUiTest
```

The parser, command, persistence/rollback tests and Checkstyle passed. The JavaFX UI test passed in one run but timed out waiting for the JavaFX application thread in a later run, as did an unrelated existing UI test during full-suite execution; rerun UI tests in a stable JavaFX test environment.

## Limitations

This change updates the guardian's single stored contact record; it does not create separate phone values on student records. The guardian list/detail UI refresh follows the existing command completion refresh path.
