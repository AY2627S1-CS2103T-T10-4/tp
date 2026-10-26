package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.AddressBook;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.guardian.GuardianPhone;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

/** Additional boundary probes with an independent register for every input. */
class AddGuardianBoundaryTest {
    @TempDir
    Path directory;
    private int nextCase;

    @Test
    void phoneDigitLengthAndCountryPrefixMatrix() throws Exception {
        for (int digit = 0; digit <= 9; digit++) {
            for (int length = 1; length <= 12; length++) {
                String digits = digit + "0".repeat(length - 1);
                boolean valid = length == 8 && "3689".contains(Integer.toString(digit));
                for (String prefix : new String[] {"", "+65", "+65 "}) {
                    check("add-guardian n/Matrix Test p/" + prefix + digits,
                            valid, "Matrix Test", "+65 " + digits);
                }
            }
        }
    }

    @Test
    void malformedPhonesAndNamesRemainUnchanged() throws Exception {
        for (String phone : new String[] {"+65  60000000", "+65\t60000000", "65 60000000",
            "+6565 60000000", "6000-0000", "(60000000)", "60000000 ext 1",
            "６０００００００", "٦٠٠٠٠٠٠٠", "6000000O", "-60000000", "6e7", "60000000/"}) {
            check("add-guardian n/Matrix Test p/" + phone, false, null, null);
        }
        for (String name : new String[] {"A1", "A🙂", "A/B", "A\\B", "A_B", "A@B",
            "'Ann", "Ann'", "A..B", "A--B", "A".repeat(81)}) {
            check("add-guardian n/" + name + " p/60000000", false, null, null);
        }
    }

    @Test
    void ordinaryUnicodeAndPunctuationRoundTrip() throws Exception {
        for (String name : new String[] {"Élodie", "李 小龍", "Αλέξανδρος", "O'Connor",
            "Anne-Marie", "S. Kumar", "A", "A".repeat(80)}) {
            check("add-guardian n/" + name + " p/60000000", true, name, "+65 60000000");
        }
    }

    @Test
    void fortyOneSupplementaryUnicodeLettersShouldFitEightyCharacters() throws Exception {
        // U+10400 DESERET CAPITAL LETTER LONG I is a Unicode letter, encoded as two Java chars.
        String name = new String(Character.toChars(0x10400)).repeat(41);
        assertEquals(41, name.codePointCount(0, name.length()));
        check("add-guardian n/" + name + " p/60000000", true, name, "+65 60000000");
    }

    @Test
    void simulatedPartialWriteFailurePreservesDiskAndAllowsRetry() throws Exception {
        Path path = directory.resolve("partial-write.json");
        Files.copy(Path.of("test/data/addressbook.json"), path);
        boolean[] fail = {true};
        JsonAddressBookStorage storage = new JsonAddressBookStorage(path) {
            @Override
            public void saveAddressBook(ReadOnlyAddressBook book) throws IOException {
                if (fail[0]) {
                    // Simulate a writer that truncates the target before encountering an I/O error.
                    Files.writeString(path, "{");
                    throw new IOException("Injected failure after truncation");
                }
                super.saveAddressBook(book);
            }
        };
        ModelManager model = new ModelManager(storage.readAddressBook().orElseThrow(), new UserPrefs());
        Logic logic = new LogicManager(model, new StorageManager(storage,
                new JsonUserPrefsStorage(directory.resolve("prefs.json"))));
        logic.execute("find-student n/Mei Lin");
        AddressBook before = new AddressBook(model.getAddressBook());
        var visible = List.copyOf(model.getStudentList());
        String saved = Files.readString(path);
        CommandException error = assertThrows(CommandException.class,
                () -> logic.execute("add-guardian n/Ann p/60000000"));
        assertEquals("Could not save changes. No guardian was added.", error.getMessage());
        assertEquals(before, model.getAddressBook());
        assertEquals(visible, model.getStudentList());
        String afterFailure = Files.readString(path);
        fail[0] = false;
        logic.execute("add-guardian n/Ann p/60000000");
        assertEquals(model.getAddressBook(), storage.readAddressBook().orElseThrow());
        assertEquals(visible, model.getStudentList());
        assertEquals(saved, afterFailure, "Disk contents immediately after the simulated partial write failure");
    }

    private void check(String command, boolean valid, String name, String phone) throws Exception {
        Path path = directory.resolve("case-" + nextCase++ + ".json");
        Files.copy(Path.of("test/data/addressbook.json"), path);
        JsonAddressBookStorage storage = new JsonAddressBookStorage(path);
        ModelManager model = new ModelManager(storage.readAddressBook().orElseThrow(), new UserPrefs());
        Logic logic = new LogicManager(model, new StorageManager(storage,
                new JsonUserPrefsStorage(directory.resolve("prefs.json"))));
        logic.execute("find-student n/Mei Lin");
        AddressBook before = new AddressBook(model.getAddressBook());
        var visible = List.copyOf(model.getStudentList());
        String saved = Files.readString(path);
        if (!valid) {
            assertThrows(ParseException.class, () -> logic.execute(command), command);
            assertEquals(before, model.getAddressBook(), command);
            assertEquals(saved, Files.readString(path), command);
        } else {
            var result = logic.execute(command);
            var added = model.findGuardianByPhone(new GuardianPhone(phone)).orElseThrow();
            assertEquals(name, added.getName().toString(), command);
            assertEquals(phone, added.getPhone().toString(), command);
            assertTrue(before.findGuardian(added.getId().toString()).isEmpty(), command);
            assertEquals("Added guardian " + added.getId() + ": " + name + " (" + phone + ").",
                    result.getFeedbackToUser(), command);
            AddressBook expected = new AddressBook(before);
            expected.addGuardian(added);
            assertEquals(expected, model.getAddressBook(), command);
            assertEquals(expected, storage.readAddressBook().orElseThrow(), command);
        }
        assertEquals(visible, model.getStudentList(), command);
    }
}
