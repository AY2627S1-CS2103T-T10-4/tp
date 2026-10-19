package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.GuardianLinkTestUtil.getUnlinkedAddressBook;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.UnlinkGuardianCommand;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.UserPrefs;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

class UnlinkGuardianIntegrationTest {
    @TempDir
    Path temporaryFolder;

    private ModelManager model;
    private RejectableStorage storage;
    private StorageManager storageManager;
    private Logic logic;

    @BeforeEach
    void setUp() throws Exception {
        model = new ModelManager(getUnlinkedAddressBook(), new UserPrefs());
        model.linkGuardian("S-0007", "G-0003");
        model.linkGuardian("S-0008", "G-0003");
        storage = new RejectableStorage(temporaryFolder.resolve("register.json"));
        storage.saveAddressBook(model.getAddressBook());
        storage.saveAttempts = 0;
        storageManager = new StorageManager(storage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("prefs.json")));
        logic = new LogicManager(model, storageManager);
    }

    @Test
    void execute_hiddenLinkedStudent_persistsRemovalAndRevealsProfile() throws Exception {
        logic.execute("find-student n/No Match");
        CommandResult result = logic.execute("unlink-guardian sid/S-0007");

        assertEquals("Removed guardian G-0003 from student S-0007. Guardian record was retained.",
                result.getFeedbackToUser());
        assertEquals("S-0007", result.getSelectedStudentId());
        assertEquals(CommandResult.Workspace.STUDENTS, result.getWorkspace());
        assertEquals(2, model.getStudentList().size());
        assertEquals(1, storage.saveAttempts);
        ModelManager reloaded = new ModelManager(storage.readAddressBook().orElseThrow(), new UserPrefs());
        assertTrue(reloaded.getLinkedGuardian("S-0007").isEmpty());
        assertTrue(reloaded.getLinkedGuardian("S-0008").isPresent());
        assertEquals(2, reloaded.getGuardianList().size());
        logic.execute("link-guardian sid/S-0007 gid/G-0004");
        assertEquals(model.getAddressBook(), storage.readAddressBook().orElseThrow());
    }

    @Test
    void execute_unknownOrUnlinkedStudent_doesNotSave() throws Exception {
        logic.execute("unlink-guardian sid/S-0007");
        AddressBook before = new AddressBook(model.getAddressBook());
        CommandException unknown = assertThrows(CommandException.class, () ->
                logic.execute("unlink-guardian sid/S-9999"));
        CommandException unlinked = assertThrows(CommandException.class, () ->
                logic.execute("unlink-guardian sid/S-0007"));
        assertEquals("Student S-9999 does not exist.", unknown.getMessage());
        assertEquals("Student S-0007 has no guardian to unlink.", unlinked.getMessage());
        assertEquals(1, storage.saveAttempts);
        assertEquals(before, model.getAddressBook());
    }

    @Test
    void execute_saveFailure_restoresLinksAndPreservesSearch() throws Exception {
        logic.execute("assign-group sid/S-0007 g/MAT");
        logic.execute("find-student n/No Match");
        AddressBook before = new AddressBook(model.getAddressBook());
        String saved = Files.readString(storage.getAddressBookFilePath());
        IOException[] failures = {new IOException("Disk full"), new AccessDeniedException("register.json")};
        for (IOException failure : failures) {
            storage.failure = failure;
            CommandException error = assertThrows(CommandException.class, () ->
                    logic.execute("unlink-guardian sid/S-0007"));
            assertEquals(UnlinkGuardianCommand.MESSAGE_SAVE_FAILURE, error.getMessage());
            assertEquals(before, model.getAddressBook());
            assertTrue(model.getStudentList().isEmpty());
            assertEquals(saved, Files.readString(storage.getAddressBookFilePath()));
        }
        storage.failure = null;
        logic.execute("unlink-guardian sid/S-0007");
        assertEquals(model.getAddressBook(), storage.readAddressBook().orElseThrow());
    }

    @Test
    void execute_recoveryMode_rejectsUnlinkBeforeMutation() throws Exception {
        Files.writeString(storage.getAddressBookFilePath(), "{invalid}");
        assertThrows(DataLoadingException.class, storageManager::readAddressBook);
        AddressBook before = new AddressBook(model.getAddressBook());

        CommandException error = assertThrows(CommandException.class, () ->
                logic.execute("unlink-guardian sid/S-0007"));

        assertEquals(DataLoadingException.MESSAGE_RECOVERY, error.getMessage());
        assertEquals(before, model.getAddressBook());
        assertEquals(0, storage.saveAttempts);
        assertEquals("{invalid}", Files.readString(storage.getAddressBookFilePath()));
        assertFalse(new UnlinkGuardianCommand("S-0007").isReadOnly());
    }

    /** Rejects writes before modifying the file, to exercise model rollback. */
    private static class RejectableStorage extends JsonAddressBookStorage {
        private IOException failure;
        private int saveAttempts;

        RejectableStorage(Path path) {
            super(path);
        }

        @Override
        public void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
            saveAttempts++;
            if (failure != null) {
                throw failure;
            }
            super.saveAddressBook(addressBook);
        }
    }
}
