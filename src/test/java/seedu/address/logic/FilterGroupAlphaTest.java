package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.FilterStudentsCommand;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.UserPrefs;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

/** Subject-group contract probes using fresh copies of the user's baseline. */
class FilterGroupAlphaTest {
    @TempDir
    Path directory;
    private int sequence;

    @Test
    void eachSubjectReplacesPreviousFilterAndReturnsExactIdsInOrderWithoutSaving() throws Exception {
        String[][] subjects = {{"ENG", "English", "S-0007"}, {"MAT", "Mathematics", "S-0003", "S-0005"},
            {"SCI", "Science", "S-0008", "S-0004", "S-0005"}, {"CHI", "Chinese"}};
        for (String[] subject : subjects) {
            for (String prior : List.of("find-student n/No Match", "find-student n/Mei Lin",
                    "filter-students al/IP 4", "filter-students g/SCI")) {
                Fixture f = fresh();
                f.logic.execute(prior);
                AddressBook before = new AddressBook(f.model.getAddressBook());
                String saved = Files.readString(f.path);
                var result = f.logic.execute("FiLtEr-StUdEnTs G/" + subject[0].toLowerCase(java.util.Locale.ROOT));
                List<String> expected = List.of(subject).subList(2, subject.length);
                assertEquals(expected, f.ids());
                assertEquals(expected.size() == 0 ? "No students are assigned to " + subject[0] + "."
                        : "Found " + expected.size() + " student(s) assigned to " + subject[0] + ".",
                        result.getFeedbackToUser());
                assertEquals("Filtered by group: " + subject[0] + " — " + subject[1],
                        f.model.getStudentFilterSummary());
                assertEquals(CommandResult.Workspace.STUDENTS, result.getWorkspace());
                assertEquals(before, f.model.getAddressBook());
                assertEquals(saved, Files.readString(f.path));
                assertEquals(0, f.storage.attempts);
                for (String id : expected) {
                    assertEquals(before.findStudent(id).orElseThrow(), f.logic.findStudent(id).orElseThrow());
                }
            }
        }
    }

    @Test
    void invalidArgumentsUnknownAndClassGroupsPreserveExactFilterAndFile() throws Exception {
        for (String args : List.of("", "g/", "g/  ", "MAT", "g/MAT g/MAT", "g/MAT G/SCI",
                "g/MAT al/Sec 2", "g/MAT x/value", "g/MAT 1/value", "extra g/MAT", "g/MAT extra",
                "g//MAT", "g/MAT/", "g/MATg/SCI", "g/M AT", "g/MAT\u00A0extra")) {
            fresh().reject("filter-students " + args, ParseException.class,
                    FilterStudentsCommand.MESSAGE_USAGE_GROUP);
        }
        fresh().reject("filter-students g/UNKNOWN", CommandException.class,
                "Group UNKNOWN is not configured. Run list-groups to see available groups.");
        fresh().reject("filter-students g/s2-mat-a", CommandException.class,
                "Group S2-MAT-A is a Class group; use the academic-level filter.");
    }

    @Test
    void failedAssignmentAndRemovalPreserveFilterThenRetryReloadsExactly() throws Exception {
        for (String command : List.of("assign-group sid/S-0008 g/MAT", "unassign-group sid/S-0003 g/MAT")) {
            Fixture f = fresh();
            f.storage.allowWrites = true;
            f.storage.failure = new IOException("Injected save failure");
            String error = command.startsWith("assign-") ? "Could not save changes. The group was not assigned."
                    : "Could not save changes. The group assignment was not removed.";
            f.reject(command, CommandException.class, error);
            f.storage.failure = null;
            f.logic.execute(command);
            AddressBook expected = new AddressBook(f.baseline);
            if (command.startsWith("assign-")) {
                expected.addGroupAssignment(new seedu.address.model.group.GroupAssignment(
                        expected.findStudent("S-0008").orElseThrow().getId(),
                        new seedu.address.model.group.GroupCode("MAT")));
            } else {
                expected.removeGroupAssignment(new seedu.address.model.group.GroupAssignment(
                        expected.findStudent("S-0003").orElseThrow().getId(),
                        new seedu.address.model.group.GroupCode("MAT")));
            }
            assertEquals(expected, f.model.getAddressBook());
            assertEquals(expected, f.storage.readAddressBook().orElseThrow());
            f.storage.allowWrites = false;
            f.logic.execute("filter-students g/MAT");
            assertEquals(command.startsWith("assign-") ? List.of("S-0008", "S-0003", "S-0005")
                    : List.of("S-0005"), f.ids());
        }
    }

    @Test
    void listAndSearchReplaceFilterAndHiddenProfileClearsIt() throws Exception {
        for (String next : List.of("list-students", "find-student n/Mei Lin", "view-student sid/S-0008")) {
            Fixture f = fresh();
            f.logic.execute(next);
            assertEquals(next.startsWith("find-") ? List.of("S-0003", "S-0004")
                    : f.baseline.getStudentList().stream().map(s -> s.getId().toString()).toList(), f.ids());
            assertEquals("", f.model.getStudentFilterSummary());
            assertEquals(f.baseline, f.model.getAddressBook());
            assertEquals(0, f.storage.attempts);
        }
    }

    @Test
    void recreatedGroupMustNotKeepPreviousGroupsLabelInActiveSummary() throws Exception {
        Fixture f = fresh();
        f.logic.execute("filter-students g/CHI");
        f.storage.allowWrites = true;
        f.logic.execute("delete-group g/CHI");
        f.logic.execute("add-group g/CHI l/Mandarin k/Subject");
        assertEquals(f.model.getAddressBook(), f.storage.readAddressBook().orElseThrow());
        assertEquals(List.of(), f.ids());
        assertEquals("Mandarin", f.model.getGroupCatalogue().findByCode("CHI").orElseThrow().getLabel());
        assertEquals("Filtered by group: CHI — Mandarin", f.model.getStudentFilterSummary());
    }

    private Fixture fresh() throws Exception {
        Path path = directory.resolve("case-" + sequence++ + ".json");
        Files.copy(Path.of("test/data/addressbook.json"), path);
        return new Fixture(path);
    }

    private class Fixture {
        private final Path path;
        private final TestStorage storage;
        private final ModelManager model;
        private final Logic logic;
        private final AddressBook baseline;

        Fixture(Path path) throws Exception {
            this.path = path;
            storage = new TestStorage(path);
            model = new ModelManager(storage.readAddressBook().orElseThrow(), new UserPrefs());
            baseline = new AddressBook(model.getAddressBook());
            logic = new LogicManager(model, new StorageManager(storage,
                    new JsonUserPrefsStorage(directory.resolve("prefs.json"))));
            logic.execute("filter-students g/MAT");
            logic.execute("find-guardian n/Anita");
        }

        List<String> ids() {
            return model.getStudentList().stream().map(s -> s.getId().toString()).toList();
        }

        <T extends Exception> void reject(String command, Class<T> type, String message) throws Exception {
            AddressBook before = new AddressBook(model.getAddressBook());
            String saved = Files.readString(path);
            var students = List.copyOf(model.getStudentList());
            var guardians = List.copyOf(model.getGuardianList());
            String summary = model.getStudentFilterSummary();
            T error = assertThrows(type, () -> logic.execute(command), command);
            assertEquals(message, error.getMessage(), command);
            assertEquals(before, model.getAddressBook(), command);
            assertEquals(saved, Files.readString(path), command);
            assertEquals(students, model.getStudentList(), command);
            assertEquals(guardians, model.getGuardianList(), command);
            assertEquals(summary, model.getStudentFilterSummary(), command);
        }
    }

    private static class TestStorage extends JsonAddressBookStorage {
        private boolean allowWrites;
        private IOException failure;
        private int attempts;

        TestStorage(Path path) {
            super(path);
        }

        @Override
        public void saveAddressBook(ReadOnlyAddressBook book) throws IOException {
            attempts++;
            if (!allowWrites) {
                throw new AssertionError("Filtering must not save");
            }
            if (failure != null) {
                throw failure;
            }
            super.saveAddressBook(book);
        }
    }
}
