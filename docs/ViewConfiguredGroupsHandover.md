---
layout: page
title: View Configured Groups Handover
---

# View Configured Groups: Backend Handover

This PR for [issue #42](https://github.com/AY2627S1-CS2103T-T10-4/tp/issues/42), iteration v1.2,
implements only the backend group-catalogue foundation for Feature 8. **Feature 8 is not fully implemented.**
No production group records, persistence, assignment logic, application-model integration, CLI command or GUI
functionality have been introduced.

## Implemented API

The classes are in `src/main/java/seedu/address/model/group/`, package `seedu.address.model.group`.

| Type | Responsibility and public API |
|---|---|
| `GroupCode` | Canonical code identity. `GroupCode(String code)`, `String getValue()`, `String toString()`, `equals(Object)` and `hashCode()`. |
| `GroupKind` | Exactly two enum constants: `SUBJECT` and `CLASS`. |
| `Group` | One immutable entry. `Group(GroupCode code, String label, GroupKind kind)`, `getCode()`, `getLabel()`, `getKind()`, `equals(Object)` and `hashCode()`. |
| `GroupCatalogue` | Validated catalogue. `GroupCatalogue(Collection<Group> groups)`, `List<Group> getGroups()`, `int size()` and `Optional<Group> findByCode(String code)`. |

Validation messages are exposed through `GroupCode.MESSAGE_CONSTRAINTS`, `Group.MESSAGE_CONSTRAINTS`
and `GroupCatalogue.MESSAGE_DUPLICATE_CODES`.

## Behaviour

- **Code identity:** `GroupCode` strips surrounding Java whitespace, folds Unicode case using Java's
  case-insensitive character mapping, then uppercases with `Locale.ROOT`. Internal whitespace is preserved.
  For example, ` mat ` becomes `MAT`, and both `ß` and `ẞ` become `SS`. Equality and hashing use this
  canonical value. There are no code-pattern or length restrictions, and construction does not check membership.
- **Labels and kinds:** `Group` requires non-null fields and a non-blank label. It preserves the supplied label
  exactly, including casing and surrounding spaces. It does not infer kind from the code. Group equality and
  hashing include code, label and kind.
- **Uniqueness:** the catalogue checks canonical codes, independently of group equality. Repeated codes fail
  even when labels or kinds differ, with `IllegalArgumentException` and the exact message
  `Configured group codes must be unique.`
- **Ordering and immutability:** `List.copyOf()` creates an immutable snapshot in the input collection's iteration
  order. Later input-list changes do not affect it; mutations through `getGroups()` are rejected.
  Entries and codes are immutable. An empty catalogue is valid and has size zero.
- **Lookup and invalid input:** lookup normalizes through `GroupCode`; an unconfigured code returns
  `Optional.empty()`. Null constructor/lookup arguments or catalogue entries throw `NullPointerException`.
  Blank codes, blank labels and duplicate codes throw `IllegalArgumentException`. Invalid lookup input is
  rejected even when the catalogue is empty.

## Example

This in-memory example uses the specification's illustrative `MAT` entry; it is not production catalogue data.

```java
import java.util.List;
import java.util.Optional;

import seedu.address.model.group.Group;
import seedu.address.model.group.GroupCatalogue;
import seedu.address.model.group.GroupCode;
import seedu.address.model.group.GroupKind;

// Place these statements inside a method.
Group mathematics = new Group(new GroupCode(" mat "), "Mathematics", GroupKind.SUBJECT);
GroupCatalogue catalogue = new GroupCatalogue(List.of(mathematics));
Optional<Group> match = catalogue.findByCode("mAt");

System.out.println(match.orElseThrow().getCode().getValue()); // MAT
System.out.println(catalogue.size()); // 1
```

## Tests and verification

Tests are in `src/test/java/seedu/address/model/group/`.

| Test class | Coverage |
|---|---|
| `GroupCodeTest` | Normalization, Unicode case variants, locale independence, equality/hashing, hash collections, invalid inputs and canonical access. |
| `GroupKindTest` | Exactly the two supported kinds. |
| `GroupTest` | Field access, preserved labels, null/blank rejection and value equality/hashing. |
| `GroupCatalogueTest` | Duplicate rejection and exact error text, lookup, ordering, empty catalogues, size, defensive copying and unmodifiable access. |

Recorded implementation verification on **8 October 2026**, using Java 25.0.1 and Gradle 9.6.1:

- Focused `test --tests 'seedu.address.model.group.*'`: **38 tests passed**.
- Full `check coverage`: **289 tests passed**, with no failures, errors or skips; both Checkstyle tasks passed
  and coverage reports were generated. These runs used `--offline --no-daemon --rerun-tasks`.
- After the final comment changes, both Checkstyle tasks passed again. Javadoc generation scoped to the four
  group types passed with no warnings.
- Repository-wide Javadoc generation failed on a separate, pre-existing unresolved `Command` link in
  `CommandException.java`. Existing deprecation/native-access warnings were not changed.

These results verify the backend foundation, not a working `list-groups` command or UI flow.
See the [testing guide](Testing.md) for the repository workflow.

## Future integration

Follow the [Feature Specification](FeatureSpecification.md) when implementing the remaining work:

1. **Data and model:** agree on the deployed catalogue and its data source, then connect ownership/access through
   the application model and startup. This PR selects no configuration format or production dataset.
2. **Command/parser:** implement case-insensitive `list-groups` with no arguments. Extra input must report
   `Invalid command format. Use: list-groups`.
3. **Display and feedback:** show code, label and kind (Subject/Class) in the main list. Report the actual
   `size()` as `Listed N configured group(s).`; the specification's count of 24 is illustrative.
4. **Configuration errors:** reject invalid or duplicate catalogues at startup. An unreadable catalogue must
   trigger the specified recovery behaviour, preserve original data, disable saving and prevent assignments.
   Constructor validation alone does not implement recovery.
5. **Integration tests:** cover command casing/extra input, displayed values/counts, empty catalogues,
   repeated view changes, return to contacts, index safety and configuration failures.

## Integration risks

- `MainWindow` and `PersonListPanel` currently display `Person` objects. Contact count, selection and
  `PersonDetailsPanel` are connected to that list. Group display needs explicit view transitions, updated
  headings/counts and safe selection handling without accumulating panels or listeners. Current `ListCommand`
  only resets the Person filter; it does not request a view change. See the [UI guide](UI.md).
- `DeleteCommand` and `EditCommand` resolve indexes against `Model#getFilteredPersonList()`. Replacing only
  the visible panel could leave hidden contacts editable or deletable. Resolve this before enabling group view.
- `LogicManager` currently saves after every successful command, including read-only commands.
  `MainApp` falls back to an empty address book on load failure without disabling saving.
  Coordinate these behaviours with the future recovery implementation; an empty catalogue is not evidence
  that configuration loaded successfully.
