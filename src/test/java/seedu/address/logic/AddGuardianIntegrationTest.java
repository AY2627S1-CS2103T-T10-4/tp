package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.AddGuardianCommand;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.UserPrefs;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

class AddGuardianIntegrationTest {
    @TempDir
    Path temporaryFolder;

    private ModelManager model;
    private RejectableStorage registerStorage;
    private Logic logic;

    @BeforeEach
    void setUp() throws Exception {
        Path baseline = Path.of("test", "data", "addressbook.json");
        Path testRegister = temporaryFolder.resolve("register.json");
        Files.copy(baseline, testRegister);
        registerStorage = new RejectableStorage(testRegister);
        model = new ModelManager(registerStorage.readAddressBook().orElseThrow(), new UserPrefs());
        logic = new LogicManager(model, new StorageManager(registerStorage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("prefs.json"))));
    }

    @Test
    void execute_addGuardian_persistsNewRecordAndLeavesExistingRegisterIntact() throws Exception {
        AddressBook before = new AddressBook(model.getAddressBook());

        CommandResult result = logic.execute("add-guardian n/Anita Lim p/61234567");

        String addedId = result.getFeedbackToUser().substring("Added guardian ".length(),
                result.getFeedbackToUser().indexOf(':'));
        assertTrue(addedId.matches("G-[0-9]{4,}"));
        assertEquals("Added guardian " + addedId + ": Anita Lim (+65 61234567).", result.getFeedbackToUser());
        assertEquals(4, model.getGuardianList().size());
        assertEquals(before.getStudentList(), model.getAddressBook().getStudentList());
        assertEquals(before.getGuardianLinks(), model.getAddressBook().getGuardianLinks());
        assertEquals(before.getGroupAssignments(), model.getAddressBook().getGroupAssignments());
        assertEquals(model.getAddressBook(), registerStorage.readAddressBook().orElseThrow());
        ModelManager reloaded = new ModelManager(registerStorage.readAddressBook().orElseThrow(), new UserPrefs());
        assertEquals("Anita Lim", reloaded.findGuardian(addedId).orElseThrow().getName().toString());
        assertEquals("+65 61234567", reloaded.findGuardian(addedId).orElseThrow().getPhone().toString());
    }

    @Test
    void execute_duplicatePhoneAndMalformedInputs_leaveDataUnchanged() throws Exception {
        AddressBook before = new AddressBook(model.getAddressBook());
        CommandException duplicate = assertThrows(CommandException.class, () ->
                logic.execute("add-guardian n/Different Name p/+65 91234567"));
        assertEquals("A guardian with phone +65 91234567 already exists (G-0001). Link that guardian instead.",
                duplicate.getMessage());

        assertThrows(ParseException.class, () -> logic.execute("add-guardian n/Anita Lim"));
        assertThrows(ParseException.class, () -> logic.execute("add-guardian n/ p/61234567"));
        assertThrows(ParseException.class, () -> logic.execute("add-guardian n/Anita2 p/61234567"));
        assertThrows(ParseException.class, () -> logic.execute("add-guardian n/Anita Lim p/51234567"));
        assertThrows(ParseException.class, () -> logic.execute("add-guardian n/Anita Lim p/61234567 x/value"));
        assertEquals(before, model.getAddressBook());
        assertEquals(0, registerStorage.saveAttempts);
    }

    @Test
    void execute_repeatedAndConflictingParameters_rejectedWithoutMutation() throws Exception {
        AddressBook before = new AddressBook(model.getAddressBook());
        assertThrows(ParseException.class, () ->
                logic.execute("add-guardian n/Anita Lim n/Raj Kumar p/61234567"));
        assertThrows(ParseException.class, () ->
                logic.execute("add-guardian n/Anita Lim p/61234567 p/81234567"));
        assertEquals(before, model.getAddressBook());
        assertEquals(0, registerStorage.saveAttempts);
    }

    @Test
    void execute_caseInsensitivePrefixesAndWhitespaceNormalize_success() throws Exception {
        CommandResult result = logic.execute("ADD-GUARDIAN P/+65 61234567 N/  Raj   Kumar  ");
        assertTrue(result.getFeedbackToUser().matches("Added guardian G-[0-9]{4,}: Raj Kumar \\(\\+65 61234567\\)\\."));
        assertEquals(4, model.getGuardianList().size());
    }

    @Test
    void execute_nameLengthBoundaryAndUnicodeNames() throws Exception {
        String eightyCharacters = "A".repeat(80);
        CommandResult maxLength = logic.execute("add-guardian n/" + eightyCharacters + " p/61234567");
        assertTrue(maxLength.getFeedbackToUser().contains(": " + eightyCharacters + " (+65 61234567)."));

        String overLimit = "A".repeat(81);
        assertThrows(ParseException.class, () ->
                logic.execute("add-guardian n/" + overLimit + " p/71234567"));
        CommandResult unicode = logic.execute("add-guardian n/李 小龍 p/61230001");
        assertTrue(unicode.getFeedbackToUser().contains(": 李 小龍 (+65 61230001)."));
        assertEquals(5, model.getGuardianList().size());
    }

    @Test
    void execute_whitespaceCollapsingAtLengthBoundary() throws Exception {
        // The raw input is over 80 characters, but its normalized value is within the specified limit.
        String name = "A".repeat(20) + " " + "A".repeat(20) + " ".repeat(40) + "B";
        int normalizedLength = name.trim().replaceAll("\\s+", " ").length();
        assertTrue(normalizedLength <= 80);
        CommandResult accepted = logic.execute("add-guardian n/" + name + " p/61230001");
        String normalizedName = name.trim().replaceAll("\\s+", " ");
        assertTrue(accepted.getFeedbackToUser().contains(": " + normalizedName + " (+65 61230001)."));

        String normalizedOverLimit = "A".repeat(81);
        assertThrows(ParseException.class, () ->
                logic.execute("add-guardian n/" + normalizedOverLimit + " p/61230002"));
    }

    @Test
    void execute_invalidCommandsPreserveActiveStudentFilterAndRegister() throws Exception {
        logic.execute("find-student n/No Match");
        assertTrue(model.getStudentList().isEmpty());
        AddressBook before = new AddressBook(model.getAddressBook());

        assertThrows(ParseException.class, () -> logic.execute("add-guardian n/Anita Lim p/61234567 p/81234567"));
        assertThrows(ParseException.class, () -> logic.execute("add-guardian n/Anita Lim p/61234567 extra"));
        assertThrows(ParseException.class, () -> logic.execute("add-guardian n/Anita Lim p/61234567 gid/G-0001"));
        assertThrows(CommandException.class, () -> logic.execute("add-guardian n/Other Name p/91234567"));
        assertEquals(before, model.getAddressBook());
        assertTrue(model.getStudentList().isEmpty());
        assertEquals(0, registerStorage.saveAttempts);
    }

    @Test
    void execute_phonePrefixBoundariesAndCanonicalDuplicates() throws Exception {
        for (String number : new String[] {"31230001", "61230002", "81230003", "91230004"}) {
            CommandResult result = logic.execute("add-guardian n/Phone Test p/" + number);
            assertTrue(result.getFeedbackToUser().endsWith("(+65 " + number + ")."));
        }
        AddressBook beforeDuplicate = new AddressBook(model.getAddressBook());
        CommandException duplicate = assertThrows(CommandException.class, () ->
                logic.execute("add-guardian n/Equivalent Phone p/+65 91230004"));
        assertTrue(duplicate.getMessage().contains("phone +65 91230004 already exists"));
        assertEquals(beforeDuplicate, model.getAddressBook());
    }

    @Test
    void execute_namePunctuationAndInvalidBoundaries() throws Exception {
        String[] names = {"O'Connor", "S. Kumar", "Anne-Marie", "李 小龍"};
        String[] numbers = {"61230010", "61230011", "61230012", "61230013"};
        for (int i = 0; i < names.length; i++) {
            CommandResult result = logic.execute("add-guardian n/" + names[i] + " p/" + numbers[i]);
            assertTrue(result.getFeedbackToUser().contains(": " + names[i] + " (+65 " + numbers[i] + ")."));
        }
        for (String name : new String[] {"-Anne", "Anne-", ".Anne", "Anne.", "Anne 2", "A..B", "A--B"}) {
            assertThrows(ParseException.class, () -> logic.execute("add-guardian n/" + name + " p/61230014"), name);
        }
    }

    @Test
    void execute_prefixLikeTextAndNonSpaceSeparators() throws Exception {
        CommandResult tabName = logic.execute("add-guardian n/Ann\tTan p/61230015");
        assertTrue(tabName.getFeedbackToUser().contains(": Ann Tan (+65 61230015)."));
        CommandResult newlineName = logic.execute("add-guardian n/Ann\nTan p/61230016");
        assertTrue(newlineName.getFeedbackToUser().contains(": Ann Tan (+65 61230016)."));
        assertThrows(ParseException.class, () -> logic.execute("add-guardian n/Ann p/61230017 x/value"));
        AddressBook before = new AddressBook(model.getAddressBook());
        assertEquals(before, model.getAddressBook());
        assertEquals(2, registerStorage.saveAttempts);
    }

    @Test
    void execute_saveFailureRollsBackAndRetryPersists() throws Exception {
        AddressBook before = new AddressBook(model.getAddressBook());
        String baselineJson = Files.readString(registerStorage.getAddressBookFilePath());
        registerStorage.failure = new IOException("Disk full");

        CommandException error = assertThrows(CommandException.class, () ->
                logic.execute("add-guardian n/Grace Tan p/61234567"));

        assertEquals("Could not save changes. No guardian was added.", error.getMessage());
        assertEquals(before, model.getAddressBook());
        assertEquals(3, model.getGuardianList().size());
        assertEquals(baselineJson, Files.readString(registerStorage.getAddressBookFilePath()));
        assertEquals(3, model.getGuardianList().size());

        registerStorage.failure = null;
        CommandResult result = logic.execute("add-guardian n/Grace Tan p/61234567");
        assertEquals(4, model.getGuardianList().size());
        String addedId = result.getFeedbackToUser().substring("Added guardian ".length(),
                result.getFeedbackToUser().indexOf(':'));
        assertTrue(model.findGuardian(addedId).isPresent());
        assertEquals(model.getAddressBook(), registerStorage.readAddressBook().orElseThrow());
    }

    private static final class RejectableStorage extends JsonAddressBookStorage {
        private IOException failure;
        private int saveAttempts;

        private RejectableStorage(Path path) {
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
