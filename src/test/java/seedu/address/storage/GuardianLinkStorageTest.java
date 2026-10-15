package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.GuardianLinkTestUtil.getUnlinkedAddressBook;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.commons.util.JsonUtil;
import seedu.address.model.AddressBook;

class GuardianLinkStorageTest {
    @TempDir
    Path temporaryFolder;

    @Test
    void saveAndRead_sharedGuardian_preservesAllRecordsAndLinks() throws Exception {
        AddressBook register = getUnlinkedAddressBook();
        register.linkGuardian("S-0007", "G-0003");
        register.linkGuardian("S-0008", "G-0003");
        JsonAddressBookStorage storage = new JsonAddressBookStorage(temporaryFolder.resolve("links.json"));
        storage.saveAddressBook(register);

        assertEquals(register, storage.readAddressBook().orElseThrow());
        String stored = Files.readString(storage.getAddressBookFilePath());
        assertTrue(stored.contains("\"studentId\" : \"S-0007\""));
        assertTrue(stored.contains("\"guardianId\" : \"G-0003\""));
    }

    @Test
    void readAddressBook_legacyFileWithoutLinks_loadsUnlinkedRecords() throws Exception {
        JsonSerializableAddressBook legacy = new JsonSerializableAddressBook(getUnlinkedAddressBook());
        String json = JsonUtil.toJsonString(legacy).replaceAll(",\\s*\"guardianLinks\"\\s*:\\s*\\[\\s*\\]", "");
        Path file = temporaryFolder.resolve("legacy.json");
        Files.writeString(file, json);
        assertEquals(getUnlinkedAddressBook(), new JsonAddressBookStorage(file).readAddressBook().orElseThrow());
    }

    @Test
    void readAddressBook_invalidLinks_reportsControlledErrorAndLeavesFileUntouched() throws Exception {
        String validLink = "{\"studentId\":\"S-0007\",\"guardianId\":\"G-0003\"}";
        String[] invalidLinks = {
            "null", "[null]", "[{}]", "[{\"studentId\":\"S-0007\"}]",
            "[{\"studentId\":null,\"guardianId\":\"G-0003\"}]",
            "[{\"studentId\":\"S-9999\",\"guardianId\":\"G-0003\"}]",
            "[{\"studentId\":\"S-0007\",\"guardianId\":\"G-9999\"}]",
            "[" + validLink + "," + validLink + "]",
            "[" + validLink + ",{\"studentId\":\"S-0007\",\"guardianId\":\"G-0004\"}]",
            "{}", "\"invalid\""
        };
        String baseJson = JsonUtil.toJsonString(new JsonSerializableAddressBook(getUnlinkedAddressBook()));
        Path file = temporaryFolder.resolve("invalid-links.json");
        for (String links : invalidLinks) {
            String json = baseJson.replaceAll("\"guardianLinks\"\\s*:\\s*\\[\\s*\\]",
                    "\"guardianLinks\": " + links);
            Files.writeString(file, json);
            assertThrows(DataLoadingException.class, () -> new JsonAddressBookStorage(file).readAddressBook(), links);
            assertEquals(json, Files.readString(file));
        }
    }
}
