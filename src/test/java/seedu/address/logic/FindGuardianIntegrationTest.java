package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.GuardianLinkTestUtil.getUnlinkedAddressBook;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.FindGuardianCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.UserPrefs;
import seedu.address.model.guardian.Guardian;
import seedu.address.model.guardian.GuardianId;
import seedu.address.model.guardian.GuardianName;
import seedu.address.model.guardian.GuardianPhone;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

class FindGuardianIntegrationTest {
    @TempDir
    Path temporaryFolder;

    private ModelManager model;
    private Logic logic;
    private StorageManager storage;
    private Path dataPath;

    @BeforeEach
    void setUp() throws Exception {
        model = new ModelManager(getUnlinkedAddressBook(), new UserPrefs());
        model.addGuardian(new Guardian(new GuardianId("G-0005"), new GuardianName("Anita Lim"),
                new GuardianPhone("81234567")));
        model.linkGuardian("S-0007", "G-0003");
        dataPath = temporaryFolder.resolve("register.json");
        new JsonAddressBookStorage(dataPath).saveAddressBook(model.getAddressBook());
        JsonAddressBookStorage readOnlyStorage = new JsonAddressBookStorage(dataPath) {
            @Override
            public void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
                throw new AssertionError("A guardian search must not save data");
            }
        };
        storage = new StorageManager(readOnlyStorage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("prefs.json")));
        logic = new LogicManager(model, storage);
    }

    @Test
    void execute_searchAndList_keepsDuplicateNamesAndPreservesStoredData() throws Exception {
        AddressBook before = new AddressBook(model.getAddressBook());
        String saved = Files.readString(dataPath);
        model.updateFilteredStudentList(unused -> false);

        CommandResult result = logic.execute("find-guardian n/ANITA");

        assertEquals("Found 2 guardian(s) matching \"ANITA\".", result.getFeedbackToUser());
        assertEquals(CommandResult.Workspace.GUARDIANS, result.getWorkspace());
        assertEquals(List.of("G-0003", "G-0005"), visibleIds());
        assertEquals(1, logic.getLinkedStudents("G-0003").size());
        logic.execute("find-guardian n/Raj");
        assertEquals(List.of("G-0004"), visibleIds());
        logic.execute("list-guardians");
        assertEquals(List.of("G-0003", "G-0005", "G-0004"), visibleIds());
        assertTrue(model.getStudentList().isEmpty());
        assertEquals(before, model.getAddressBook());
        assertEquals(saved, Files.readString(dataPath));
    }

    @Test
    void execute_unicodeCaseVariant_findsGuardianBeforeAndAfterReload() throws Exception {
        Guardian guardian = new Guardian(new GuardianId("G-0006"), new GuardianName("ΟΣ"),
                new GuardianPhone("60000001"));
        model.addGuardian(guardian);
        new JsonAddressBookStorage(dataPath).saveAddressBook(model.getAddressBook());
        String saved = Files.readString(dataPath);

        for (int attempt = 0; attempt < 2; attempt++) {
            CommandResult result = logic.execute("find-guardian n/οσ");
            assertEquals("Found 1 guardian(s) matching \"οσ\".", result.getFeedbackToUser());
            assertEquals(CommandResult.Workspace.GUARDIANS, result.getWorkspace());
            assertEquals(List.of("G-0006"), visibleIds());
            assertEquals("ΟΣ", logic.getGuardianList().getFirst().getName().toString());
            assertEquals(saved, Files.readString(dataPath));
            model = new ModelManager(storage.readAddressBook().orElseThrow(), new UserPrefs());
            logic = new LogicManager(model, storage);
        }
    }

    @Test
    void execute_noMatchOrInvalidQuery_clearsOnlyOnValidSearch() throws Exception {
        logic.execute("find-guardian n/Raj");
        assertThrows(ParseException.class, () -> logic.execute("find-guardian n/Anita n/Lim"));
        assertThrows(ParseException.class, () -> logic.execute("find-guardian n/91234567"));
        assertEquals(List.of("G-0004"), visibleIds());
        CommandResult result = logic.execute("find-guardian n/Mei");
        assertEquals("No guardians match \"Mei\"", result.getFeedbackToUser());
        assertEquals(CommandResult.Workspace.GUARDIANS, result.getWorkspace());
        assertTrue(visibleIds().isEmpty());
        assertEquals(3, model.getAddressBook().getGuardianList().size());
    }

    @Test
    void execute_recoveryMode_searchesWithoutWriting() throws Exception {
        Files.writeString(dataPath, "{invalid}");
        assertThrows(DataLoadingException.class, storage::readAddressBook);
        model.setAddressBook(new AddressBook());
        CommandResult result = logic.execute("find-guardian n/Anita");
        assertEquals("No guardians match \"Anita\"", result.getFeedbackToUser());
        assertTrue(new FindGuardianCommand(new GuardianName("Anita")).isReadOnly());
        assertEquals("{invalid}", Files.readString(dataPath));
        assertTrue(logic.getDataRecoveryReason().isPresent());
    }

    private List<String> visibleIds() {
        return logic.getGuardianList().stream().map(guardian -> guardian.getId().toString()).toList();
    }
}
