package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.GuardianLinkTestUtil.getUnlinkedAddressBook;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javafx.collections.ListChangeListener;
import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.LinkGuardianCommand;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.UserPrefs;
import seedu.address.model.student.Student;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

class LinkGuardianIntegrationTest {
    @TempDir
    Path temporaryFolder;

    private ModelManager model;
    private TrackingStorage storage;
    private Logic logic;

    @BeforeEach
    void setUp() {
        model = new ModelManager(getUnlinkedAddressBook(), new UserPrefs());
        storage = new TrackingStorage(temporaryFolder.resolve("register.json"));
        logic = new LogicManager(model, new StorageManager(storage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("prefs.json"))));
    }

    @Test
    void execute_linkSiblings_persistsRelationshipsAndExposesCurrentGuardian() throws Exception {
        CommandResult result = logic.execute("link-guardian sid/S-0007 gid/G-0003");
        logic.execute("link-guardian gid/G-0003 sid/S-0008");

        assertEquals("S-0007", result.getSelectedStudentId());
        assertEquals(2, storage.saveAttempts);
        assertEquals(model.getAddressBook(), storage.readAddressBook().orElseThrow());
        ModelManager reloaded = new ModelManager(storage.readAddressBook().orElseThrow(), new UserPrefs());
        assertEquals("+65 91234567", reloaded.getLinkedGuardian("S-0007").orElseThrow().getPhone().toString());
        assertEquals(logic.getLinkedGuardian("S-0007"), logic.getLinkedGuardian("S-0008"));
        assertTrue(logic.getLinkedGuardian("S-9999").isEmpty());

        logic.execute("view-student sid/S-0007");
        assertEquals(2, storage.saveAttempts);
    }

    @Test
    void execute_linkHiddenStudent_revealsStudentAndPersistsRelationship() throws Exception {
        logic.execute("find-student n/No Match");
        assertTrue(model.getStudentList().isEmpty());
        assertEquals(0, storage.saveAttempts);

        logic.execute("link-guardian sid/S-0007 gid/G-0003");
        assertEquals(2, model.getStudentList().size());
        assertTrue(logic.getLinkedGuardian("S-0007").isPresent());
        assertEquals(model.getAddressBook(), storage.readAddressBook().orElseThrow());

        logic.execute("list-students");
        assertEquals(2, model.getStudentList().size());
        assertEquals(1, storage.saveAttempts);
    }

    @Test
    void execute_invalidOrDuplicateLink_doesNotAttemptSave() throws Exception {
        logic.execute("link-guardian sid/S-0007 gid/G-0003");
        AddressBook before = new AddressBook(model.getAddressBook());
        assertThrows(ParseException.class, () -> logic.execute("link-guardian sid/S-0007"));
        assertThrows(CommandException.class, () -> logic.execute("link-guardian sid/S-9999 gid/G-0003"));
        assertThrows(CommandException.class, () -> logic.execute("link-guardian sid/S-0008 gid/G-9999"));
        assertThrows(CommandException.class, () -> logic.execute("link-guardian sid/S-0007 gid/G-0003"));
        assertThrows(CommandException.class, () -> logic.execute("link-guardian sid/S-0007 gid/G-0004"));
        assertEquals(1, storage.saveAttempts);
        assertEquals(before, model.getAddressBook());
    }

    @Test
    void execute_saveFailure_restoresLinksWithoutStudentListChanges() throws Exception {
        logic.execute("link-guardian sid/S-0007 gid/G-0003");
        AddressBook before = new AddressBook(model.getAddressBook());
        String savedJson = Files.readString(storage.getAddressBookFilePath());
        AtomicInteger studentChanges = new AtomicInteger();
        model.getStudentList().addListener((ListChangeListener<Student>) change -> studentChanges.incrementAndGet());

        IOException[] failures = {new IOException("Disk full"), new AccessDeniedException("register.json")};
        for (IOException failure : failures) {
            storage.failure = failure;
            CommandException error = assertThrows(CommandException.class, () ->
                    logic.execute("link-guardian sid/S-0008 gid/G-0004"));
            assertEquals(LinkGuardianCommand.MESSAGE_SAVE_FAILURE, error.getMessage());
            assertEquals(before, model.getAddressBook());
            assertEquals(savedJson, Files.readString(storage.getAddressBookFilePath()));
            assertEquals(0, studentChanges.get());
            assertTrue(logic.getLinkedGuardian("S-0008").isEmpty());
        }
        storage.failure = null;
        logic.execute("link-guardian sid/S-0008 gid/G-0004");
        assertEquals(model.getAddressBook(), storage.readAddressBook().orElseThrow());
    }

    @Test
    void execute_groupAndGuardianChanges_preservesBothRelationshipsAcrossEditsAndDeletion() throws Exception {
        logic.execute("assign-group sid/S-0007 g/MAT");
        logic.execute("link-guardian sid/S-0007 gid/G-0003");
        logic.execute("link-guardian sid/S-0008 gid/G-0003");
        logic.execute("assign-group sid/S-0008 g/ENG");
        logic.execute("edit-student sid/S-0007 al/Sec 4");
        AddressBook before = new AddressBook(model.getAddressBook());
        assertEquals(before, storage.readAddressBook().orElseThrow());
        assertEquals(2, before.getGuardianLinks().size());
        assertEquals(2, before.getGroupAssignments().size());

        logic.execute("find-student n/No Match");
        storage.failure = new IOException("Disk full");
        assertThrows(CommandException.class, () -> logic.execute("delete-student sid/S-0007"));
        assertEquals(before, model.getAddressBook());
        assertTrue(model.getStudentList().isEmpty());
        storage.failure = null;
        logic.execute("delete-student sid/S-0007");
        assertEquals(1, model.getAddressBook().getGuardianLinks().size());
        assertEquals(1, model.getAddressBook().getGroupAssignments().size());
        assertTrue(logic.getLinkedGuardian("S-0008").isPresent());
        assertEquals(2, model.getGuardianList().size());
        assertEquals(model.getAddressBook(), storage.readAddressBook().orElseThrow());
    }

    @Test
    void execute_recoveryMode_blocksLinkWithoutChangingData() throws Exception {
        Files.writeString(storage.getAddressBookFilePath(), "{invalid}");
        StorageManager recovering = new StorageManager(storage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("prefs.json")));
        assertThrows(DataLoadingException.class, recovering::readAddressBook);
        Logic recoveringLogic = new LogicManager(model, recovering);
        AddressBook before = new AddressBook(model.getAddressBook());

        CommandException error = assertThrows(CommandException.class, () ->
                recoveringLogic.execute("link-guardian sid/S-0007 gid/G-0003"));

        assertEquals(DataLoadingException.MESSAGE_RECOVERY, error.getMessage());
        assertEquals(before, model.getAddressBook());
        assertEquals(0, storage.saveAttempts);
        assertEquals("{invalid}", Files.readString(storage.getAddressBookFilePath()));
        recoveringLogic.execute("view-student sid/S-0007");
    }

    /** Records write attempts and can reject a write before modifying the file. */
    private static class TrackingStorage extends JsonAddressBookStorage {
        private int saveAttempts;
        private IOException failure;

        TrackingStorage(Path path) {
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
