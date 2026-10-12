package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.commons.util.JsonUtil;
import seedu.address.model.AddressBook;

class JsonSerializableAddressBookTest {
    private static final Path TEST_DATA_FOLDER = Paths.get("src", "test", "data", "JsonSerializableAddressBookTest");

    @Test
    void legacyPersonFiles_areLoadedAsEmptyStudentRegister() throws Exception {
        for (String file : List.of("typicalPersonsAddressBook.json", "invalidPersonAddressBook.json",
                "duplicatePersonAddressBook.json")) {
            JsonSerializableAddressBook json = JsonUtil.readJsonFile(TEST_DATA_FOLDER.resolve(file),
                    JsonSerializableAddressBook.class).orElseThrow();
            assertEquals(new AddressBook(), json.toModelType());
        }
    }
}
