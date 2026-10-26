package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.ViewStudentCommand;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.UserPrefs;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

/** Profile command and data-contract tests; no JavaFX UI components are constructed. */
class ViewStudentAlphaTest {
    @TempDir
    Path directory;
    private int sequence;

    @Test
    void everyBaselineStudentAcrossFiltersOpensExactRecordWithoutSaving() throws Exception {
        Fixture baseline = fresh();
        for (var student : baseline.model.getAddressBook().getStudentList()) {
            for (String filter : new String[] {"list-students", "find-student n/Mei Lin",
                "find-student n/No Match", "filter-students g/MAT", "filter-students al/Sec 2"}) {
                Fixture f = fresh();
                f.logic.execute(filter);
                f.logic.execute("find-guardian n/Anita");
                String sid = student.getId().toString();
                f.open(sid);
                assertEquals(student, f.logic.findStudent(sid).orElseThrow());
                assertEquals(f.baseline.getLinkedGuardian(sid), f.logic.getLinkedGuardian(sid));
                var groups = f.baseline.getGroupCatalogue().getGroups().stream()
                        .filter(g -> f.baseline.getGroupAssignments().stream()
                                .anyMatch(a -> a.getStudentId().toString().equals(sid)
                                        && a.getGroupCode().equals(g.getCode()))).toList();
                assertEquals(groups, f.logic.getAssignedGroups(sid));
            }
        }
    }

    @Test
    void malformedMissingEmptyRepeatedUnknownAndInjectionInputsPreserveState() throws Exception {
        String[] args = {"", " ", "S-0007", "sid/", "sid/   ", "gid/G-0001", "sid/S-007",
            "sid/s-0007", "sid/G-0007", "sid/S--0007", "sid/S-000a", "sid/S- 0007",
            "sid/S-0007 sid/S-0007", "sid/S-0007 SID/S-0008", "sid/S-0007 x/value",
            "sid/S-0007 gid/G-0001", "sid/S-0007 extra", "unexpected sid/S-0007",
            "sid/S-0007/", "sid/S-0007sid/S-0008", "sid/S-０００７", "sid/S-٠٠٠٧",
            "sid/S-0007; delete-student sid/S-0007", "sid/S-0007\nlist-students",
            "sid/S-0007\u200B", "sid/S-0007\u202E", "sid/S-0007\u0000extra"};
        for (String input : args) {
            Fixture f = fresh();
            f.rejected("view-student " + input, ParseException.class, ViewStudentCommand.MESSAGE_USAGE);
        }
    }

    @Test
    void unknownAndHugeIdsReturnExactErrorWithoutAllocatingRecords() throws Exception {
        for (String id : new String[] {"S-0000", "S-9999", "S-00007", "S-" + "9".repeat(10000)}) {
            fresh().rejected("view-student sid/" + id, CommandException.class,
                    "Student " + id + " does not exist.");
        }
    }

    @Test
    void supportedCaseAndWhitespaceVariantsOpenExactId() throws Exception {
        for (String command : new String[] {"ViEw-StUdEnT SiD/S-0007", " view-student SID/ S-0007 ",
            "view-student\tsid/\tS-0007\t", "view-student\nsid/\nS-0007\n"}) {
            Fixture f = fresh();
            CommandResult result = f.logic.execute(command);
            assertEquals("Opened profile for S-0007.", result.getFeedbackToUser());
            assertEquals("S-0007", result.getSelectedStudentId());
            assertEquals(f.baseline, f.model.getAddressBook());
            assertEquals(f.saved, Files.readString(f.path));
            assertEquals(0, f.storage.attempts);
        }
    }

    @Test
    void guardianUpdatesAndStudentPhoneStayIndependentAcrossReloadAndUnlink() throws Exception {
        Fixture f = fresh();
        f.open("S-0003");
        f.storage.allowWrites = true;
        f.logic.execute("edit-student sid/S-0003 p/60001111 al/Sec 4");
        f.logic.execute("edit-guardian gid/G-0001 p/90002222");
        f.storage.allowWrites = false;
        f.open("S-0003");
        assertEquals("Sec 4", f.logic.findStudent("S-0003").orElseThrow().getAcademicLevel().toString());
        assertEquals("+65 60001111", f.logic.findStudent("S-0003").orElseThrow().getPhone().orElseThrow().toString());
        assertEquals("G-0001", f.logic.getLinkedGuardian("S-0003").orElseThrow().getId().toString());
        assertEquals("Anita Lim", f.logic.getLinkedGuardian("S-0003").orElseThrow().getName().toString());
        assertEquals("+65 90002222", f.logic.getLinkedGuardian("S-0003").orElseThrow().getPhone().toString());
        f.open("S-0005");
        assertEquals("+65 90002222", f.logic.getLinkedGuardian("S-0005").orElseThrow().getPhone().toString());
        assertTrue(f.logic.findStudent("S-0005").orElseThrow().getPhone().isEmpty());
        f.open("S-0004");
        assertEquals("G-0002", f.logic.getLinkedGuardian("S-0004").orElseThrow().getId().toString());
        assertEquals("+65 81234567", f.logic.getLinkedGuardian("S-0004").orElseThrow().getPhone().toString());
        f.reload();
        f.open("S-0003");
        assertEquals("+65 90002222", f.logic.getLinkedGuardian("S-0003").orElseThrow().getPhone().toString());
        f.storage.allowWrites = true;
        f.logic.execute("unlink-guardian sid/S-0003");
        f.storage.allowWrites = false;
        f.open("S-0003");
        assertTrue(f.logic.getLinkedGuardian("S-0003").isEmpty());
        assertEquals("+65 60001111", f.logic.findStudent("S-0003").orElseThrow().getPhone().orElseThrow().toString());
        assertTrue(f.logic.getLinkedGuardian("S-0005").isPresent());
    }

    @Test
    void assignmentsReflectRemovalAndReadditionWithoutSavingOnOpen() throws Exception {
        Fixture f = fresh();
        assertEquals(List.of("MAT", "S2-MAT-A"),
                f.logic.getAssignedGroups("S-0003").stream().map(g -> g.getCode().toString()).toList());
        assertEquals(List.of("Mathematics", "Secondary 2 Mathematics A"),
                f.logic.getAssignedGroups("S-0003").stream().map(g -> g.getLabel()).toList());
        f.storage.allowWrites = true;
        f.logic.execute("unassign-group sid/S-0003 g/MAT");
        f.storage.allowWrites = false;
        f.open("S-0003");
        assertEquals(List.of("S2-MAT-A"),
                f.logic.getAssignedGroups("S-0003").stream().map(g -> g.getCode().toString()).toList());
        f.reload();
        f.open("S-0003");
        assertEquals(List.of("S2-MAT-A"),
                f.logic.getAssignedGroups("S-0003").stream().map(g -> g.getCode().toString()).toList());
        f.storage.allowWrites = true;
        f.logic.execute("assign-group sid/S-0003 g/MAT");
        f.storage.allowWrites = false;
        f.open("S-0003");
        assertEquals(List.of("MAT", "S2-MAT-A"),
                f.logic.getAssignedGroups("S-0003").stream().map(g -> g.getCode().toString()).toList());
        f.storage.allowWrites = true;
        f.logic.execute("unassign-group sid/S-0003 g/MAT");
        f.logic.execute("unassign-group sid/S-0003 g/S2-MAT-A");
        f.storage.allowWrites = false;
        f.reload();
        f.open("S-0003");
        assertEquals(List.of(), f.logic.getAssignedGroups("S-0003"));
    }

    @Test
    void newlyCreatedStudentUsesGeneratedIdAndIndependentPhoneWithoutAssignments() throws Exception {
        Fixture f = fresh();
        f.storage.allowWrites = true;
        f.logic.execute("add-student n/Profile Probe al/Sec 1 p/60001111");
        var student = f.model.getAddressBook().getStudentList().stream()
                .filter(s -> s.getName().toString().equals("Profile Probe")).findFirst().orElseThrow();
        String sid = student.getId().toString();
        assertTrue(f.baseline.findStudent(sid).isEmpty());
        f.storage.allowWrites = false;
        f.logic.execute("find-student n/Mei Lin");
        f.open(sid);
        assertEquals(student, f.logic.findStudent(sid).orElseThrow());
        assertEquals("Sec 1", student.getAcademicLevel().toString());
        assertEquals("+65 60001111", student.getPhone().orElseThrow().toString());
        assertEquals(List.of(), f.logic.getAssignedGroups(sid));
        assertTrue(f.logic.getLinkedGuardian(sid).isEmpty());
        f.storage.allowWrites = true;
        f.logic.execute("link-guardian sid/" + sid + " gid/G-0001");
        f.storage.allowWrites = false;
        f.reload();
        f.open(sid);
        assertEquals(student, f.logic.findStudent(sid).orElseThrow());
        assertEquals("G-0001", f.logic.getLinkedGuardian(sid).orElseThrow().getId().toString());
        assertEquals("+65 91234567", f.logic.getLinkedGuardian(sid).orElseThrow().getPhone().toString());
        assertEquals(List.of(), f.logic.getAssignedGroups(sid));
    }

    @Test
    void failedGuardianEditCannotLeakUnpersistedContactIntoProfile() throws Exception {
        Fixture f = fresh();
        f.storage.allowWrites = true;
        f.storage.failure = new IOException("Injected save failure");
        CommandException error = assertThrows(CommandException.class,
                () -> f.logic.execute("edit-guardian gid/G-0001 p/90002222"));
        assertEquals("Could not save changes. Guardian G-0001 was not updated.", error.getMessage());
        assertEquals(f.baseline, f.model.getAddressBook());
        assertEquals(f.saved, Files.readString(f.path));
        f.storage.failure = null;
        f.storage.allowWrites = false;
        f.open("S-0003");
        assertEquals("+65 91234567", f.logic.getLinkedGuardian("S-0003").orElseThrow().getPhone().toString());
        f.storage.allowWrites = true;
        f.logic.execute("edit-guardian gid/G-0001 p/90002222");
        f.storage.allowWrites = false;
        f.reload();
        f.open("S-0003");
        assertEquals("+65 90002222", f.logic.getLinkedGuardian("S-0003").orElseThrow().getPhone().toString());
    }

    @Test
    void deletedStudentCannotBeOpenedOrConfusedWithSameNameSurvivor() throws Exception {
        Fixture f = fresh();
        f.open("S-0003");
        f.storage.allowWrites = true;
        f.logic.execute("delete-student sid/S-0003");
        f.storage.allowWrites = false;
        f.reload();
        f.rejected("view-student sid/S-0003", CommandException.class, "Student S-0003 does not exist.");
        f.open("S-0004");
        assertEquals("Mei Lin", f.logic.findStudent("S-0004").orElseThrow().getName().toString());
        assertEquals("Sec 3", f.logic.findStudent("S-0004").orElseThrow().getAcademicLevel().toString());
        assertEquals("G-0002", f.logic.getLinkedGuardian("S-0004").orElseThrow().getId().toString());
    }

    @Test
    void readOnlyOpenWorksDuringRecoveryWithoutTouchingBrokenFile() throws Exception {
        Fixture f = fresh();
        Files.writeString(f.path, "{broken");
        assertThrows(DataLoadingException.class, f.manager::readAddressBook);
        f.open("S-0003");
        assertEquals("{broken", Files.readString(f.path));
        // Actual startup recovery uses an empty model; it must return a controlled unknown-ID error.
        f.model = new ModelManager();
        f.logic = new LogicManager(f.model, f.manager);
        f.rejected("view-student sid/S-0003", CommandException.class, "Student S-0003 does not exist.");
    }

    private Fixture fresh() throws Exception {
        Path path = directory.resolve("case-" + sequence++ + ".json");
        Files.copy(Path.of("test/data/addressbook.json"), path);
        return new Fixture(path);
    }

    private class Fixture {
        private final Path path;
        private final NoWriteStorage storage;
        private final StorageManager manager;
        private final AddressBook baseline;
        private final String saved;
        private ModelManager model;
        private Logic logic;

        Fixture(Path path) throws Exception {
            this.path = path;
            storage = new NoWriteStorage(path);
            manager = new StorageManager(storage, new JsonUserPrefsStorage(directory.resolve("prefs.json")));
            baseline = new AddressBook(manager.readAddressBook().orElseThrow());
            saved = Files.readString(path);
            model = new ModelManager(baseline, new UserPrefs());
            logic = new LogicManager(model, manager);
            logic.execute("find-student n/Mei Lin");
            logic.execute("find-guardian n/Anita");
        }

        void reload() throws Exception {
            model = new ModelManager(storage.readAddressBook().orElseThrow(), new UserPrefs());
            logic = new LogicManager(model, manager);
        }

        void open(String sid) throws Exception {
            AddressBook before = new AddressBook(model.getAddressBook());
            String bytes = Files.readString(path);
            var students = List.copyOf(model.getStudentList());
            var guardians = List.copyOf(model.getGuardianList());
            String summary = model.getStudentFilterSummary();
            int saves = storage.attempts;
            CommandResult result = logic.execute("view-student sid/" + sid);
            assertEquals("Opened profile for " + sid + ".", result.getFeedbackToUser());
            assertEquals(sid, result.getSelectedStudentId());
            assertEquals(CommandResult.Workspace.UNCHANGED, result.getWorkspace());
            assertFalse(result.isExit());
            assertFalse(result.isShowHelp());
            assertEquals(before, model.getAddressBook());
            assertEquals(bytes, Files.readString(path));
            assertEquals(saves, storage.attempts);
            assertEquals(guardians, model.getGuardianList());
            boolean visible = students.stream().anyMatch(s -> s.getId().toString().equals(sid));
            assertEquals(visible ? students : before.getStudentList(), model.getStudentList());
            assertEquals(visible ? summary : "", model.getStudentFilterSummary());
        }

        <T extends Exception> void rejected(String command, Class<T> type, String message) throws Exception {
            AddressBook before = new AddressBook(model.getAddressBook());
            var students = List.copyOf(model.getStudentList());
            var guardians = List.copyOf(model.getGuardianList());
            String summary = model.getStudentFilterSummary();
            String bytes = Files.readString(path);
            int saves = storage.attempts;
            T error = assertThrows(type, () -> logic.execute(command), command);
            assertEquals(message, error.getMessage(), command);
            assertEquals(before, model.getAddressBook(), command);
            assertEquals(students, model.getStudentList(), command);
            assertEquals(guardians, model.getGuardianList(), command);
            assertEquals(summary, model.getStudentFilterSummary(), command);
            assertEquals(bytes, Files.readString(path), command);
            assertEquals(saves, storage.attempts, command);
        }
    }

    private static class NoWriteStorage extends JsonAddressBookStorage {
        private boolean allowWrites;
        private IOException failure;
        private int attempts;

        NoWriteStorage(Path path) {
            super(path);
        }

        @Override
        public void saveAddressBook(ReadOnlyAddressBook register) throws IOException {
            attempts++;
            if (!allowWrites) {
                throw new AssertionError("Opening a profile must never attempt a save");
            }
            if (failure != null) {
                throw failure;
            }
            super.saveAddressBook(register);
        }
    }
}
