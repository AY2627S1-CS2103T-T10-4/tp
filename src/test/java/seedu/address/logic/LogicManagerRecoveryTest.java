package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.logic.commands.Command;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

class LogicManagerRecoveryTest {
    @TempDir
    private Path testFolder;

    @Test
    void execute_mutationDuringRecovery_rejectsBeforeChangingModelOrFile() throws Exception {
        StorageManager storage = createStorage();
        byte[] original = "invalid data\r\n".getBytes(StandardCharsets.UTF_8);
        Files.write(storage.getAddressBookFilePath(), original);
        assertThrows(DataLoadingException.class, storage::readAddressBook);
        Model model = new ModelManager();
        Logic logic = new LogicManager(model, storage);

        CommandException error = assertThrows(CommandException.class, () ->
                logic.execute("add-student n/Amy Tan al/Sec 3"));

        assertEquals("Could not load local data. Your original data file was not changed; "
                + "restore or replace it before saving.", error.getMessage());
        assertTrue(model.getStudentList().isEmpty());
        assertArrayEquals(original, Files.readAllBytes(storage.getAddressBookFilePath()));
    }

    @Test
    void execute_readOnlyCommandsDuringRecovery_succeedWithoutSavingOrClearingReason() throws Exception {
        StorageManager storage = createStorage();
        byte[] original = "invalid data".getBytes(StandardCharsets.UTF_8);
        Files.write(storage.getAddressBookFilePath(), original);
        assertThrows(DataLoadingException.class, storage::readAddressBook);
        Logic logic = new LogicManager(new ModelManager(), storage);
        String reason = logic.getDataRecoveryReason().orElseThrow();

        assertEquals("Listed 5 configured group(s).", logic.execute("list-groups").getFeedbackToUser());
        assertTrue(logic.execute("help").isShowHelp());
        assertTrue(logic.execute("exit").isExit());

        assertEquals(reason, logic.getDataRecoveryReason().orElseThrow());
        assertArrayEquals(original, Files.readAllBytes(storage.getAddressBookFilePath()));
        assertFalse(Files.exists(storage.getUserPrefsFilePath()));
    }

    @Test
    void execute_readOnlyCommandsNormally_doNotCreateDataFile() throws Exception {
        StorageManager storage = createStorage();
        assertTrue(storage.readAddressBook().isEmpty());
        Logic logic = new LogicManager(new ModelManager(), storage);

        logic.execute("help");
        logic.execute("list-groups");
        logic.execute("exit");

        assertTrue(logic.getDataRecoveryReason().isEmpty());
        assertFalse(Files.exists(storage.getAddressBookFilePath()));
    }

    @Test
    void newCommand_withoutExplicitReadOnlyDeclaration_defaultsToMutating() {
        Command futureCommand = new Command() {
            @Override
            public CommandResult execute(Model model) {
                return new CommandResult("A future command");
            }
        };

        assertFalse(futureCommand.isReadOnly());
    }

    private StorageManager createStorage() {
        return new StorageManager(new JsonAddressBookStorage(testFolder.resolve("students.json")),
                new JsonUserPrefsStorage(testFolder.resolve("prefs.json")));
    }
}
