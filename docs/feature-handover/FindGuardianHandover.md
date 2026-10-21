# Search guardians by name (Feature 17)

`find-guardian n/QUERY` searches guardian names using a normalized,
case-insensitive substring. Multiword queries are one phrase, not separate
keywords. Phone numbers, IDs and student names are not searched.

## Contract

- The parser requires exactly one nonempty `n/` value and rejects additional
  prefixes before execution. Command and prefix casing are flexible.
- Queries follow the shared 1-80 character name rules. Invalid query values
  report `Search query must be 1-80 characters and contain name characters only.`
  Missing, blank, repeated or additional parameters report the command usage.
- `GuardianNameContainsQueryPredicate` reuses `GuardianName` normalization.
  The filtered view preserves the aggregate's name/ID order and distinct
  guardians with the same name. Every search considers the complete source list.
- The command is read-only and remains available during recovery. It changes
  neither persisted data nor the independent Student filter.
- A successful search requests the Guardians workspace. The existing list and
  detail pane display contact information, linked-student counts, and students.
  An empty result displays the query-specific no-matches message. Invalid input
  preserves the prior tab, results and selection.
- `list-guardians` clears the predicate and restores all contacts. Authoritative
  guardian lookup and relationships remain independent of the search filter.

## Verification

Predicate and parser tests cover case, whitespace, Unicode normalization,
partial phrases, length boundaries, and invalid or repeated parameters. Logic
tests cover duplicate names, repeated searches, list reset, independent Student
filtering, unchanged storage, and recovery. JavaFX tests exercise navigation,
contact details, linked students, rejected input, and empty-result feedback.

Run `./gradlew.bat check coverage` on Windows.
