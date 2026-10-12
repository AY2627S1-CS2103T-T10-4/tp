package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

class LogicManagerTest {
    @TempDir
    Path temporaryFolder;

    private ModelManager model;
    private Logic logic;

    @BeforeEach
    void setUp() {
        model = new ModelManager();
        logic = createLogic(new JsonAddressBookStorage(temporaryFolder.resolve("students.json")));
    }

    @Test
    void execute_unknownCommand_throwsParseException() {
        assertThrows(ParseException.class, () -> logic.execute("add n/Hidden Person"));
    }

    @Test
    void execute_addStudent_savesAndSelectsStudentById() throws Exception {
        CommandResult result = logic.execute("add-student n/Mei Lin al/Sec 2");
        assertEquals(1, logic.getStudentList().size());
        assertEquals("Mei Lin", logic.getStudentList().get(0).getName().toString());
        assertEquals(logic.getStudentList().get(0).getId().value, result.getSelectedStudentId());
    }

    @Test
    void execute_saveFailure_rollsBackStudent() {
        JsonAddressBookStorage failingStorage = new JsonAddressBookStorage(temporaryFolder.resolve("failure.json")) {
            @Override
            public void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
                throw new IOException("disk unavailable");
            }
        };
        logic = createLogic(failingStorage);

        CommandException error = assertThrows(CommandException.class, () ->
                logic.execute("add-student n/Mei Lin al/Sec 2"));

        assertEquals("Could not save changes. No student was added.", error.getMessage());
        assertEquals(0, model.getStudentList().size());
    }

    private Logic createLogic(JsonAddressBookStorage addressBookStorage) {
        JsonUserPrefsStorage prefsStorage = new JsonUserPrefsStorage(temporaryFolder.resolve("prefs.json"));
        return new LogicManager(model, new StorageManager(addressBookStorage, prefsStorage));
    }
}
