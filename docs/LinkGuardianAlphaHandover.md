# Link guardian alpha testing

## Scope and baseline

Tested on 27 October 2026 through the repository Java parser, logic, model,
and JSON storage. No UI testing or production changes were made. The running
jar was not exercised, and its equivalence to these sources is unverified.

The supplied `test/data/addressbook.json` currently contains six students,
four guardians (including G-0004 Ann), three guardian links and eight group
assignments. `LinkGuardianAlphaTest` copies that file to a new temporary file
for each independent scenario; it does not modify the supplied baseline.
Existing tests use their own repository fixtures.

## Structure and expected behavior

`AddressBookParser` dispatches `link-guardian` to
`LinkGuardianCommandParser`. The parser accepts case-insensitive command and
prefix spelling, either prefix order, and whitespace, but validates uppercase
ID values. `LinkGuardianCommand` looks up both exact IDs in the complete
register and calls `model.linkGuardian`. `AddressBook` prevents duplicate links
and replacing an existing guardian. `LogicManager` saves and rolls back the
in-memory register on IOException. No model, storage, or UI implementation
changes were needed for this testing increment.

## New executable coverage

- All 24 baseline student–guardian pairs, each from a new temporary baseline:
  nine valid new links, three duplicate links and twelve attempts to replace
  an existing guardian. Same-name guardians and same-name students are checked
  by exact ID, not by name or list position.
- 24 missing, empty, repeated-identical, repeated-conflicting, unknown, malformed,
  swapped-prefix, lowercase-ID, Unicode-digit and command-suffix cases.
- Eight unknown-ID cases, including extra leading zeros and 10,000-digit IDs.
- Five supported command/prefix case, argument-order and whitespace variants.
- IOException and AccessDeniedException injected before storage writes, each
  verifying exact failure feedback, full rollback, unchanged student/guardian
  filters and saved JSON, then successful retry and reload.
- A sequence linking all initially unlinked students to one guardian, checking
  the complete expected register after every save and the exact sibling set.

Successful independent scenarios assert exact feedback, requested student ID,
returned workspace, the whole expected register, preserved guardian filter,
the student records exposed by the logic layer, persisted equality and the
exact relationship after reload. Rejected scenarios assert exact error text,
unchanged register, saved JSON, student filter records/summary and guardian
filter records. Returned command metadata is inspected without rendering UI.

## Execution evidence

Selected classes and results:

| Class | Methods | Failures |
| --- | ---: | ---: |
| LinkGuardianAlphaTest | 6 | 0 |
| LinkGuardianIntegrationTest | 6 | 0 |
| LinkGuardianCommandParserTest | 4 | 0 |
| LinkGuardianCommandTest | 5 | 0 |
| AddressBookGuardianLinkTest | 8 | 0 |
| GuardianLinkStorageTest | 3 | 0 |
| Total | 32 | 0 |

Gradle completed successfully. XML evidence is in `build/test-results/test/`
and the HTML report is `build/reports/tests/test/index.html`; subsequent runs
can replace these reports. No new link-guardian defect was established.

## Limits and next steps

This is finite scenario coverage, not proof of correctness for every input.
No application-process restart or jar execution was performed; JSON reload
was tested through actual storage. UI behavior was excluded as instructed.
The User Guide's Saving and reopening section explicitly documents that an
I/O failure partway through a normal save can leave a partially written file.
This shared storage limitation was not re-injected as a new link-specific bug.

The requested command format, one-guardian rule, duplicate/not-found feedback
and save-failure feedback agree between the specification and the User Guide.
The guide's example names differ from this fixture's actual records; expected
success feedback uses the exact stored records rather than example names.
Further work can verify the released jar against these sources and investigate
additional cross-command sequences if a concrete concern arises.
