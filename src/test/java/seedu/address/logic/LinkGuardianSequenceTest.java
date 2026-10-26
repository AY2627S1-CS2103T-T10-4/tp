package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.exceptions.DataLoadingException;
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

/** Reproducible state-machine tests using a separate string-to-string relationship oracle. */
class LinkGuardianSequenceTest {
    @TempDir
    Path directory;

    @Test
    void eightHundredSeededTransitionsMatchIndependentRelationshipMap() throws Exception {
        for (int seed = 0; seed < 20; seed++) {
            Path path = directory.resolve("seed-" + seed + ".json");
            Files.copy(Path.of("test/data/addressbook.json"), path);
            JsonAddressBookStorage storage = new JsonAddressBookStorage(path);
            AddressBook baseline = new AddressBook(storage.readAddressBook().orElseThrow());
            Map<String, String> expected = links(baseline);
            List<String> studentIds = baseline.getStudentList().stream().map(s -> s.getId().toString()).toList();
            List<String> guardianIds = baseline.getGuardianList().stream().map(g -> g.getId().toString()).toList();
            ModelManager model = new ModelManager(baseline, new UserPrefs());
            Logic logic = logic(model, storage);
            Random random = new Random(seed);
            for (int step = 0; step < 40; step++) {
                // Reopen actual saved data periodically, rather than merely comparing serialized strings.
                if (step % 7 == 0) {
                    model = new ModelManager(storage.readAddressBook().orElseThrow(), new UserPrefs());
                    logic = logic(model, storage);
                }
                logic.execute("find-student n/Mei Lin");
                logic.execute("find-guardian n/Anita");
                var visibleStudents = List.copyOf(model.getStudentList());
                var visibleGuardians = List.copyOf(model.getGuardianList());
                String summary = model.getStudentFilterSummary();
                AddressBook before = new AddressBook(model.getAddressBook());
                String saved = Files.readString(path);
                String sid = studentIds.get(random.nextInt(studentIds.size()));
                String gid = guardianIds.get(random.nextInt(guardianIds.size()));
                String previous = expected.get(sid);
                String command = "LiNk-GuArDiAn GID/" + gid + " SID/" + sid;
                String message = null;
                boolean parseFailure = false;
                boolean remove = false;
                switch (random.nextInt(8)) {
                case 0:
                    remove = true;
                    command = "unlink-guardian sid/" + sid;
                    if (previous == null) {
                        message = "Student " + sid + " has no guardian to unlink.";
                    }
                    break;
                case 1:
                    command += " sid/" + sid;
                    message = LinkGuardianCommand.MESSAGE_USAGE;
                    parseFailure = true;
                    break;
                case 2:
                    command = "link-guardian sid/S-9999 gid/" + gid;
                    message = "Student S-9999 does not exist.";
                    break;
                case 3:
                    command = "link-guardian sid/" + sid + " gid/G-9999";
                    message = "Guardian G-9999 does not exist.";
                    break;
                default:
                    if (previous != null) {
                        message = previous.equals(gid)
                                ? "Student " + sid + " is already linked to guardian " + gid + "."
                                : "Student " + sid + " already has guardian " + previous
                                        + ". Unlink it before linking another guardian.";
                    }
                }
                String context = "seed=" + seed + ", step=" + step + ", " + command;
                if (message != null) {
                    Logic currentLogic = logic;
                    String input = command;
                    Exception error = parseFailure
                            ? assertThrows(ParseException.class, () -> currentLogic.execute(input), context)
                            : assertThrows(CommandException.class, () -> currentLogic.execute(input), context);
                    assertEquals(message, error.getMessage(), context);
                    assertEquals(before, model.getAddressBook(), context);
                    assertEquals(saved, Files.readString(path), context);
                    assertEquals(visibleStudents, model.getStudentList(), context);
                    assertEquals(summary, model.getStudentFilterSummary(), context);
                } else {
                    var result = logic.execute(command);
                    if (remove) {
                        expected.remove(sid);
                        assertEquals("Removed guardian " + previous + " from student " + sid
                                + ". Guardian record was retained.", result.getFeedbackToUser(), context);
                    } else {
                        expected.put(sid, gid);
                        var student = baseline.findStudent(sid).orElseThrow();
                        var guardian = baseline.findGuardian(gid).orElseThrow();
                        assertEquals("Linked student " + sid + " (" + student.getName() + ") to guardian "
                                + gid + " (" + guardian.getName() + ", " + guardian.getPhone() + ").",
                                result.getFeedbackToUser(), context);
                    }
                    assertEquals(sid, result.getSelectedStudentId(), context);
                    boolean wasVisible = visibleStudents.stream().anyMatch(s -> s.getId().toString().equals(sid));
                    assertEquals(wasVisible ? visibleStudents : baseline.getStudentList(),
                            model.getStudentList(), context);
                    assertEquals(model.getAddressBook(), storage.readAddressBook().orElseThrow(), context);
                }
                assertEquals(expected, links(model.getAddressBook()), context);
                assertEquals(baseline.getStudentList(), model.getAddressBook().getStudentList(), context);
                assertEquals(baseline.getGuardianList(), model.getAddressBook().getGuardianList(), context);
                assertEquals(baseline.getGroupAssignments(), model.getAddressBook().getGroupAssignments(), context);
                assertEquals(baseline.getGroupCatalogue(), model.getAddressBook().getGroupCatalogue(), context);
                assertEquals(visibleGuardians, model.getGuardianList(), context);
                for (String guardian : guardianIds) {
                    var siblings = baseline.getStudentList().stream()
                            .filter(s -> guardian.equals(expected.get(s.getId().toString()))).toList();
                    assertEquals(siblings, logic.getLinkedStudents(guardian), context);
                }
            }
        }
    }

    @Test
    void recoveryBlocksValidLinksWithoutChangingBrokenFileAndNewSessionCanRetry() throws Exception {
        for (String command : new String[] {"link-guardian sid/S-0007 gid/G-0003",
            "link-guardian sid/S-0003 gid/G-0001", "link-guardian sid/S-9999 gid/G-0003"}) {
            Path path = directory.resolve("recovery-" + Math.abs(command.hashCode()) + ".json");
            Files.copy(Path.of("test/data/addressbook.json"), path);
            JsonAddressBookStorage storage = new JsonAddressBookStorage(path);
            AddressBook before = new AddressBook(storage.readAddressBook().orElseThrow());
            String saved = Files.readString(path);
            ModelManager model = new ModelManager(before, new UserPrefs());
            StorageManager manager = new StorageManager(storage,
                    new JsonUserPrefsStorage(directory.resolve("recovery-prefs.json")));
            Logic logic = new LogicManager(model, manager);
            logic.execute("find-student n/Mei Lin");
            var visible = List.copyOf(model.getStudentList());
            Files.writeString(path, "{broken");
            assertThrows(DataLoadingException.class, manager::readAddressBook);
            var error = assertThrows(CommandException.class, () -> logic.execute(command));
            assertEquals(DataLoadingException.MESSAGE_RECOVERY, error.getMessage());
            assertEquals(before, model.getAddressBook());
            assertEquals(visible, model.getStudentList());
            assertEquals("{broken", Files.readString(path));
            Files.writeString(path, saved);
            Logic restarted = logic(new ModelManager(storage.readAddressBook().orElseThrow(), new UserPrefs()), storage);
            restarted.execute("link-guardian sid/S-0007 gid/G-0003");
            AddressBook expected = new AddressBook(before);
            expected.linkGuardian("S-0007", "G-0003");
            assertEquals(expected, storage.readAddressBook().orElseThrow());
        }
    }

    private Logic logic(ModelManager model, JsonAddressBookStorage storage) {
        return new LogicManager(model, new StorageManager(storage,
                new JsonUserPrefsStorage(directory.resolve("prefs.json"))));
    }

    private static Map<String, String> links(ReadOnlyAddressBook register) {
        Map<String, String> result = new LinkedHashMap<>();
        register.getGuardianLinks().forEach((s, g) -> result.put(s.toString(), g.toString()));
        return result;
    }
}
