package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.LinkGuardianCommand;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.UserPrefs;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

/** Alpha tests: new temporary baseline for every independent command scenario. */
class LinkGuardianAlphaTest {
    @TempDir
    Path directory;
    private int sequence;

    @Test
    void everyBaselineStudentGuardianPairChecksIdentityAndOneGuardianLimit() throws Exception {
        Fixture baseline = fresh();
        for (var student : baseline.model.getAddressBook().getStudentList()) {
            for (var guardian : baseline.model.getAddressBook().getGuardianList()) {
                Fixture f = fresh();
                String sid = student.getId().toString();
                String gid = guardian.getId().toString();
                String command = "link-guardian sid/" + sid + " gid/" + gid;
                var existing = f.before.getGuardianLinks().get(student.getId());
                if (existing == null) {
                    f.success(command, sid, gid);
                } else {
                    String message = existing.equals(guardian.getId())
                            ? "Student " + sid + " is already linked to guardian " + existing + "."
                            : "Student " + sid + " already has guardian " + existing
                                    + ". Unlink it before linking another guardian.";
                    f.rejected(command, CommandException.class, message);
                }
            }
        }
    }

    @Test
    void malformedMissingEmptyRepeatedAndUnknownArgumentsPreserveBothFilters() throws Exception {
        String[] arguments = {
            "", "sid/S-0007", "gid/G-0003", "sid/ gid/G-0003", "sid/S-0007 gid/",
            "sid/S-0007 sid/S-0007 gid/G-0003", "sid/S-0007 sid/S-0008 gid/G-0003",
            "sid/S-0007 gid/G-0003 gid/G-0003", "sid/S-0007 gid/G-0003 gid/G-0002",
            "sid/S-0007 gid/G-0003 x/value", "unexpected sid/S-0007 gid/G-0003",
            "sid/S-0007 gid/G-0003 extra", "sid/S-0007 gid/G-0003 1/value",
            "sid/7 gid/3", "sid/S-007 gid/G-0003", "sid/S-0007 gid/G-003",
            "sid/s-0007 gid/G-0003", "sid/S-0007 gid/g-0003",
            "sid/G-0003 gid/S-0007", "sid/S-0007gid/G-0003",
            "sid/S- 0007 gid/G-0003", "sid/S-0007 gid/G-000x",
            "sid/S-０００７ gid/G-0003", "sid/S-0007 gid/G-0003; list-students"
        };
        for (String args : arguments) {
            fresh().rejected("link-guardian " + args, ParseException.class, LinkGuardianCommand.MESSAGE_USAGE);
        }
    }

    @Test
    void unknownAndVeryLargeIdsReportExactNotFoundWithoutMutation() throws Exception {
        for (String sid : new String[] {"S-0000", "S-9999", "S-00007", "S-" + "9".repeat(10000)}) {
            fresh().rejected("link-guardian sid/" + sid + " gid/G-0003", CommandException.class,
                    "Student " + sid + " does not exist.");
        }
        for (String gid : new String[] {"G-0000", "G-9999", "G-00003", "G-" + "9".repeat(10000)}) {
            fresh().rejected("link-guardian sid/S-0007 gid/" + gid, CommandException.class,
                    "Guardian " + gid + " does not exist.");
        }
    }

    @Test
    void prefixOrderCasingAndWhitespacePreserveExactTarget() throws Exception {
        for (String command : new String[] {
            "link-guardian gid/G-0003 sid/S-0007",
            "LiNk-GuArDiAn GiD/G-0003 SiD/S-0007",
            "  link-guardian  sid/ S-0007   gid/ G-0003  ",
            "link-guardian\tsid/S-0007\tgid/G-0003",
            "link-guardian\nsid/S-0007\ngid/G-0003"
        }) {
            fresh().success(command, "S-0007", "G-0003");
        }
    }

    @Test
    void saveFailuresRestoreStateAndFiltersThenRetryPersists() throws Exception {
        for (IOException failure : new IOException[] {
            new IOException("Injected full disk"), new AccessDeniedException("register.json")
        }) {
            Fixture f = fresh();
            f.storage.failure = failure;
            f.rejected("link-guardian sid/S-0007 gid/G-0003", CommandException.class,
                    LinkGuardianCommand.MESSAGE_SAVE_FAILURE);
            assertEquals(1, f.storage.attempts);
            f.storage.failure = null;
            f.success("link-guardian sid/S-0007 gid/G-0003", "S-0007", "G-0003");
            assertEquals(2, f.storage.attempts);
        }
    }

    @Test
    void sharedGuardianCanBeLinkedToAllInitiallyUnlinkedStudents() throws Exception {
        Fixture f = fresh();
        AddressBook expected = new AddressBook(f.before);
        for (var student : f.before.getStudentList()) {
            if (!f.before.getGuardianLinks().containsKey(student.getId())) {
                String sid = student.getId().toString();
                f.logic.execute("link-guardian sid/" + sid + " gid/G-0001");
                expected.linkGuardian(sid, "G-0001");
                assertEquals(expected, f.model.getAddressBook());
                assertEquals(expected, f.storage.readAddressBook().orElseThrow());
            }
        }
        var expectedSiblings = expected.getStudentList().stream()
                .filter(s -> expected.getLinkedGuardian(s.getId().toString()).orElseThrow()
                        .getId().toString().equals("G-0001")).toList();
        assertEquals(expectedSiblings, f.logic.getLinkedStudents("G-0001"));
    }

    private Fixture fresh() throws Exception {
        Path path = directory.resolve("register-" + sequence++ + ".json");
        Files.copy(Path.of("test/data/addressbook.json"), path);
        return new Fixture(path);
    }

    private class Fixture {
        private final FailingStorage storage;
        private final ModelManager model;
        private final Logic logic;
        private final AddressBook before;
        private final String saved;
        private final List<?> students;
        private final List<?> guardians;
        private final String summary;

        Fixture(Path path) throws Exception {
            storage = new FailingStorage(path);
            model = new ModelManager(storage.readAddressBook().orElseThrow(), new UserPrefs());
            logic = new LogicManager(model, new StorageManager(storage,
                    new JsonUserPrefsStorage(directory.resolve("prefs.json"))));
            logic.execute("find-student n/Mei Lin");
            logic.execute("find-guardian n/Anita");
            before = new AddressBook(model.getAddressBook());
            saved = Files.readString(path);
            students = List.copyOf(model.getStudentList());
            guardians = List.copyOf(model.getGuardianList());
            summary = model.getStudentFilterSummary();
        }

        <T extends Exception> void rejected(String command, Class<T> type, String message) throws Exception {
            T error = assertThrows(type, () -> logic.execute(command), command);
            assertEquals(message, error.getMessage(), command);
            assertEquals(before, model.getAddressBook(), command);
            assertEquals(saved, Files.readString(storage.getAddressBookFilePath()), command);
            assertEquals(students, model.getStudentList(), command);
            assertEquals(guardians, model.getGuardianList(), command);
            assertEquals(summary, model.getStudentFilterSummary(), command);
        }

        void success(String command, String sid, String gid) throws Exception {
            var student = before.findStudent(sid).orElseThrow();
            var guardian = before.findGuardian(gid).orElseThrow();
            AddressBook expected = new AddressBook(before);
            expected.linkGuardian(sid, gid);
            CommandResult result = logic.execute(command);
            assertEquals("Linked student " + sid + " (" + student.getName() + ") to guardian "
                    + gid + " (" + guardian.getName() + ", " + guardian.getPhone() + ").",
                    result.getFeedbackToUser(), command);
            assertEquals(sid, result.getSelectedStudentId());
            assertEquals(CommandResult.Workspace.STUDENTS, result.getWorkspace());
            assertEquals(expected, model.getAddressBook(), command);
            assertEquals(expected, storage.readAddressBook().orElseThrow(), command);
            ModelManager reloaded = new ModelManager(storage.readAddressBook().orElseThrow(), new UserPrefs());
            assertEquals(guardian, reloaded.getLinkedGuardian(sid).orElseThrow());
            assertEquals(guardians, model.getGuardianList());
            // Hidden successful targets are revealed by the logic layer; no UI rendering is tested.
            assertEquals(expected.getStudentList(), model.getStudentList());
            assertTrue(model.getStudentList().contains(student));
        }
    }

    private static class FailingStorage extends JsonAddressBookStorage {
        private IOException failure;
        private int attempts;

        FailingStorage(Path path) {
            super(path);
        }

        @Override
        public void saveAddressBook(ReadOnlyAddressBook book) throws IOException {
            attempts++;
            if (failure != null) {
                throw failure;
            }
            super.saveAddressBook(book);
        }
    }
}
