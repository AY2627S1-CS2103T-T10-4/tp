package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.model.AddressBook;
import seedu.address.model.ReadOnlyAddressBook;

class StorageManagerRecoveryTest {
    @TempDir
    private Path testFolder;

    @Test
    void saveAddressBook_afterLoadFailure_rejectsWriteAndPreservesOriginalBytes() throws Exception {
        Path data = testFolder.resolve("students.json");
        byte[] original = "malformed original data\r\n".getBytes(StandardCharsets.UTF_8);
        Files.write(data, original);
        StorageManager storage = new StorageManager(new JsonAddressBookStorage(data),
                new JsonUserPrefsStorage(testFolder.resolve("prefs.json")));
        assertThrows(DataLoadingException.class, storage::readAddressBook);

        IOException error = assertThrows(IOException.class, () -> storage.saveAddressBook(new AddressBook()));

        assertEquals("Could not load local data. Your original data file was not changed; "
                + "restore or replace it before saving.", error.getMessage());
        assertTrue(storage.getDataRecoveryReason().isPresent());
        assertArrayEquals(original, Files.readAllBytes(data));
    }

    @Test
    void readAddressBook_nullOrIncompleteJson_rejectsInvalidDataWithoutWriting() throws Exception {
        String[] invalidFiles = {"null", "", "[]", "{\"students\":null}", "{\"students\":[null]}",
            "{\"students\":[]} {\"students\":[]}", "{\"students\":[]} trailing",
            "{\"students\":[],\"students\":[]}", "{\"unsupportedRecords\":[{}]}",
            "{\"students\":[{\"id\":\"S-0401\",\"name\":\"Mei Lin\",\"academicLevel\":\"Sec 2\",\"extra\":1}]}"};
        for (int i = 0; i < invalidFiles.length; i++) {
            Path data = testFolder.resolve("invalid-" + i + ".json");
            byte[] original = invalidFiles[i].getBytes(StandardCharsets.UTF_8);
            Files.write(data, original);
            StorageManager storage = new StorageManager(new JsonAddressBookStorage(data),
                    new JsonUserPrefsStorage(testFolder.resolve("prefs.json")));

            assertThrows(DataLoadingException.class, storage::readAddressBook, invalidFiles[i]);
            assertThrows(IOException.class, () -> storage.saveAddressBook(new AddressBook()));
            assertArrayEquals(original, Files.readAllBytes(data));
        }
    }

    @Test
    void readAddressBook_invalidStudentData_entersRecoveryAndPreservesBytes() throws Exception {
        String first = "{\"id\":\"S-0401\",\"name\":\"Mei Lin\",\"academicLevel\":\"Sec 2\"}";
        String duplicateId = "{\"id\":\"S-0401\",\"name\":\"Amy Tan\",\"academicLevel\":\"Sec 3\"}";
        String duplicateIdentity = "{\"id\":\"S-0402\",\"name\":\"mei lin\",\"academicLevel\":\"sec 2\"}";
        String[] invalidRecords = {first.replace("Sec 2", "Sec 9"), first.replace("S-0401", "invalid"),
            "{\"name\":\"Mei Lin\",\"academicLevel\":\"Sec 2\"}",
            first + "," + duplicateId, first + "," + duplicateIdentity};
        for (int i = 0; i < invalidRecords.length; i++) {
            Path data = testFolder.resolve("domain-" + i + ".json");
            byte[] original = ("{\"students\":[" + invalidRecords[i] + "]}").getBytes(StandardCharsets.UTF_8);
            Files.write(data, original);
            StorageManager storage = new StorageManager(new JsonAddressBookStorage(data),
                    new JsonUserPrefsStorage(testFolder.resolve("prefs.json")));

            assertThrows(DataLoadingException.class, storage::readAddressBook);
            assertTrue(storage.getDataRecoveryReason().isPresent());
            assertThrows(IOException.class, () -> storage.saveAddressBook(new AddressBook()));
            assertArrayEquals(original, Files.readAllBytes(data));
        }
    }

    @Test
    void readAddressBook_nonStringStudentName_entersRecoveryWithoutCoercion() throws Exception {
        Path data = testFolder.resolve("boolean-name.json");
        byte[] original = "{\"students\":[{\"id\":\"S-0401\",\"name\":true,\"academicLevel\":\"Sec 2\"}]}"
                .getBytes(StandardCharsets.UTF_8);
        Files.write(data, original);
        StorageManager storage = new StorageManager(new JsonAddressBookStorage(data),
                new JsonUserPrefsStorage(testFolder.resolve("prefs.json")));

        assertThrows(DataLoadingException.class, storage::readAddressBook);
        assertTrue(storage.getDataRecoveryReason().isPresent());
        assertThrows(IOException.class, () -> storage.saveAddressBook(new AddressBook()));
        assertArrayEquals(original, Files.readAllBytes(data));
    }

    @Test
    void readAddressBook_accessDenied_entersRecoveryBeforeAnyWrite() throws Exception {
        Path data = testFolder.resolve("unreadable.json");
        byte[] original = "{\"students\":[]}".getBytes(StandardCharsets.UTF_8);
        Files.write(data, original);
        JsonAddressBookStorage deniedStorage = new JsonAddressBookStorage(data) {
            @Override
            public Optional<ReadOnlyAddressBook> readAddressBook() throws DataLoadingException {
                throw new DataLoadingException(new AccessDeniedException(data.toString(), null, "read denied"));
            }

            @Override
            public void saveAddressBook(ReadOnlyAddressBook ignored) {
                throw new AssertionError("Recovery must reject before reaching the file writer.");
            }
        };
        StorageManager storage = new StorageManager(deniedStorage,
                new JsonUserPrefsStorage(testFolder.resolve("prefs.json")));

        assertThrows(DataLoadingException.class, storage::readAddressBook);
        assertTrue(storage.getDataRecoveryReason().orElseThrow().contains("read denied"));
        assertThrows(IOException.class, () -> storage.saveAddressBook(new AddressBook()));
        assertArrayEquals(original, Files.readAllBytes(data));
    }

    @Test
    void readAddressBook_directoryAtDataPath_isNotTreatedAsMissing() throws Exception {
        Path data = Files.createDirectory(testFolder.resolve("directory.json"));
        StorageManager storage = new StorageManager(new JsonAddressBookStorage(data),
                new JsonUserPrefsStorage(testFolder.resolve("prefs.json")));

        assertThrows(DataLoadingException.class, storage::readAddressBook);
        assertTrue(storage.getDataRecoveryReason().isPresent());
        assertThrows(IOException.class, () -> storage.saveAddressBook(new AddressBook()));
        assertTrue(Files.isDirectory(data));
    }

    @Test
    void recovery_successfulReread_doesNotUnlockUntilNewSession() throws Exception {
        Path data = testFolder.resolve("repaired.json");
        Files.writeString(data, "malformed");
        StorageManager storage = new StorageManager(new JsonAddressBookStorage(data),
                new JsonUserPrefsStorage(testFolder.resolve("prefs.json")));
        assertThrows(DataLoadingException.class, storage::readAddressBook);
        String firstReason = storage.getDataRecoveryReason().orElseThrow();
        byte[] repaired = "{\"students\":[]}".getBytes(StandardCharsets.UTF_8);
        Files.write(data, repaired);

        assertTrue(storage.readAddressBook().isPresent());
        assertEquals(firstReason, storage.getDataRecoveryReason().orElseThrow());
        assertThrows(IOException.class, () -> storage.saveAddressBook(new AddressBook()));
        assertArrayEquals(repaired, Files.readAllBytes(data));

        StorageManager restarted = new StorageManager(new JsonAddressBookStorage(data),
                new JsonUserPrefsStorage(testFolder.resolve("new-prefs.json")));
        assertTrue(restarted.readAddressBook().isPresent());
        assertFalse(restarted.getDataRecoveryReason().isPresent());
        restarted.saveAddressBook(new AddressBook());
    }
}
