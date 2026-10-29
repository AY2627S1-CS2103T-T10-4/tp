package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.model.AddressBook;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.guardian.Guardian;
import seedu.address.model.guardian.GuardianId;
import seedu.address.model.guardian.GuardianName;
import seedu.address.model.guardian.GuardianPhone;
import seedu.address.model.student.AcademicLevel;
import seedu.address.model.student.Student;
import seedu.address.model.student.StudentName;

class JsonAddressBookStorageTest {
    private static final Path TEST_DATA_FOLDER = Paths.get("src", "test", "data", "JsonAddressBookStorageTest");

    @TempDir
    Path testFolder;

    @Test
    void missingAndMalformedFiles_areHandled() throws Exception {
        JsonAddressBookStorage storage = new JsonAddressBookStorage(testFolder.resolve("students.json"));
        assertFalse(storage.readAddressBook(testFolder.resolve("missing.json")).isPresent());
        Path malformedFile = TEST_DATA_FOLDER.resolve("notJsonFormatAddressBook.json");
        assertThrows(DataLoadingException.class, () -> storage.readAddressBook(malformedFile));
    }

    @Test
    void nullGuardianEntry_throwsDataLoadingException() {
        JsonAddressBookStorage storage = new JsonAddressBookStorage(testFolder.resolve("students.json"));
        Path nullGuardianFile = TEST_DATA_FOLDER.resolve("nullGuardianAddressBook.json");

        assertThrows(DataLoadingException.class, () -> storage.readAddressBook(nullGuardianFile));
    }

    @Test
    void legacyPersonFiles_doNotLoadHiddenRecords() throws Exception {
        JsonAddressBookStorage storage = new JsonAddressBookStorage(testFolder.resolve("students.json"));
        ReadOnlyAddressBook loaded = storage.readAddressBook(TEST_DATA_FOLDER.resolve("invalidPersonAddressBook.json"))
                .orElseThrow();
        assertEquals(new AddressBook(), new AddressBook(loaded));
    }

    @Test
    void studentRecords_roundTrip() throws Exception {
        Path file = testFolder.resolve("students.json");
        JsonAddressBookStorage storage = new JsonAddressBookStorage(file);
        AddressBook source = new AddressBook();
        source.addStudent(new Student(new StudentName("Mei Lin"), new AcademicLevel("Sec 2")));
        storage.saveAddressBook(source);
        assertEquals(source, new AddressBook(storage.readAddressBook().orElseThrow()));
    }

    @Test
    void guardianWithSupplementaryUnicodeName_roundTrips() throws Exception {
        Path file = testFolder.resolve("guardians.json");
        JsonAddressBookStorage storage = new JsonAddressBookStorage(file);
        String name = "𐐀".repeat(41);
        AddressBook source = new AddressBook();
        source.addGuardian(new Guardian(new GuardianId("G-0001"), new GuardianName(name),
                new GuardianPhone("91234567")));

        storage.saveAddressBook(source);
        AddressBook restored = new AddressBook(storage.readAddressBook().orElseThrow());

        assertEquals(name, restored.getGuardianList().getFirst().getName().toString());
        assertEquals(source, restored);
    }

    @Test
    void save_nullArguments_throwNullPointerException() {
        JsonAddressBookStorage storage = new JsonAddressBookStorage(testFolder.resolve("students.json"));
        assertThrows(NullPointerException.class, () -> storage.saveAddressBook(null, testFolder.resolve("x.json")));
        assertThrows(NullPointerException.class, () -> save(storage, new AddressBook(), null));
    }

    private static void save(JsonAddressBookStorage storage, ReadOnlyAddressBook data, Path path) {
        try {
            storage.saveAddressBook(data, path);
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }
}
