package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.model.AddressBook;
import seedu.address.model.ReadOnlyAddressBook;

class CombinedRegisterStorageTest {
    private static final String STUDENTS = "\"students\":[{\"id\":\"S-9201\",\"name\":\"Mei Lin\","
            + "\"academicLevel\":\"Sec 2\"}]";
    private static final String GUARDIAN = "{\"id\":\"G-9201\",\"name\":\"Anita Lim\",\"phone\":\"91234567\"}";
    private static final String ASSIGNMENT = "{\"studentId\":\"S-9201\",\"groupCode\":\"mat\"}";

    @TempDir
    private Path testFolder;

    @Test
    void roundTrip_preservesAllCollectionsAndCanonicalValues() throws Exception {
        Path path = testFolder.resolve("complete.json");
        Files.writeString(path, json("[" + GUARDIAN + "]", "[" + ASSIGNMENT + "]"));
        JsonAddressBookStorage storage = new JsonAddressBookStorage(path);
        ReadOnlyAddressBook loaded = storage.readAddressBook().orElseThrow();

        assertEquals(1, loaded.getStudentList().size());
        assertEquals("+65 91234567", loaded.getGuardianList().getFirst().getPhone().toString());
        assertEquals("MAT", loaded.getGroupAssignments().iterator().next().getGroupCode().getValue());
        storage.saveAddressBook(loaded);
        assertEquals(loaded, new JsonAddressBookStorage(path).readAddressBook().orElseThrow());
    }

    @Test
    void absentOptionalCollections_remainCompatible() throws Exception {
        List<String> documents = List.of("{" + STUDENTS + "}",
                "{" + STUDENTS + ",\"guardians\":[" + GUARDIAN + "]}",
                "{" + STUDENTS + ",\"groupAssignments\":[" + ASSIGNMENT + "]}");
        for (int i = 0; i < documents.size(); i++) {
            Path path = testFolder.resolve("legacy-" + i + ".json");
            Files.writeString(path, documents.get(i));
            ReadOnlyAddressBook loaded = new JsonAddressBookStorage(path).readAddressBook().orElseThrow();
            assertEquals(1, loaded.getStudentList().size());
            assertEquals(i == 1 ? 1 : 0, loaded.getGuardianList().size());
            assertEquals(i == 2 ? 1 : 0, loaded.getGroupAssignments().size());
        }
    }

    @Test
    void invalidGuardianData_entersRecoveryAndPreservesOriginalBytes() throws Exception {
        List<String> invalid = List.of("null", "[null]", "{}",
                "[" + GUARDIAN.replace("\"Anita Lim\"", "true") + "]",
                "[" + GUARDIAN.replace("\"91234567\"", "91234567") + "]",
                "[" + GUARDIAN.replace("G-9201", "invalid") + "]",
                "[" + GUARDIAN.replace("Anita Lim", "Anita2") + "]",
                "[" + GUARDIAN.replace("91234567", "51234567") + "]",
                "[" + GUARDIAN.replace(",\"phone\":\"91234567\"", "") + "]",
                "[" + GUARDIAN.replace("}", ",\"unsupported\":true}") + "]",
                "[" + GUARDIAN + "," + GUARDIAN.replace("91234567", "61234567") + "]",
                "[" + GUARDIAN + "," + GUARDIAN.replace("G-9201", "G-9202")
                        .replace("91234567", "+65 91234567") + "]");
        for (int i = 0; i < invalid.size(); i++) {
            assertRecovery("guardian-" + i, json(invalid.get(i), "[" + ASSIGNMENT + "]"));
        }
    }

    @Test
    void invalidAssignmentsWithValidGuardians_enterRecovery() throws Exception {
        List<String> invalid = List.of("null", "[null]",
                "[" + ASSIGNMENT.replace("S-9201", "S-9299") + "]",
                "[" + ASSIGNMENT.replace("mat", "UNKNOWN") + "]",
                "[" + ASSIGNMENT + "," + ASSIGNMENT.replace("mat", "MAT") + "]");
        for (int i = 0; i < invalid.size(); i++) {
            assertRecovery("assignment-" + i, json("[" + GUARDIAN + "]", invalid.get(i)));
        }
    }

    private void assertRecovery(String name, String json) throws Exception {
        Path path = testFolder.resolve(name + ".json");
        byte[] original = json.getBytes(StandardCharsets.UTF_8);
        Files.write(path, original);
        StorageManager storage = new StorageManager(new JsonAddressBookStorage(path),
                new JsonUserPrefsStorage(testFolder.resolve("prefs.json")));
        assertThrows(DataLoadingException.class, storage::readAddressBook, json);
        assertTrue(storage.getDataRecoveryReason().isPresent());
        assertThrows(IOException.class, () -> storage.saveAddressBook(new AddressBook()));
        assertArrayEquals(original, Files.readAllBytes(path));
    }

    private static String json(String guardians, String assignments) {
        return "{" + STUDENTS + ",\"guardians\":" + guardians + ",\"groupAssignments\":" + assignments + "}";
    }
}
