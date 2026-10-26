package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.model.AddressBook;
import seedu.address.model.group.ConfiguredGroups;
import seedu.address.model.group.Group;
import seedu.address.model.group.GroupCode;
import seedu.address.model.group.GroupKind;

class GroupCatalogueStorageTest {
    @TempDir
    private Path testFolder;

    @Test
    void saveAndReload_customCatalogue_preservesOrderAndMetadata() throws Exception {
        AddressBook source = new AddressBook();
        source.addGroup(new Group(new GroupCode("PHY"), "Physics", GroupKind.SUBJECT));
        Path path = testFolder.resolve("groups.json");
        JsonAddressBookStorage storage = new JsonAddressBookStorage(path);

        storage.saveAddressBook(source);
        var loaded = storage.readAddressBook().orElseThrow();

        assertEquals(source, loaded);
        assertEquals("PHY", loaded.getGroupCatalogue().getGroups().getLast().getCode().getValue());
    }

    @Test
    void read_legacyFileWithoutGroups_usesDefaultCatalogue() throws Exception {
        Path path = testFolder.resolve("legacy.json");
        Files.writeString(path, "{\"students\":[]}");

        var loaded = new JsonAddressBookStorage(path).readAddressBook().orElseThrow();

        assertEquals(ConfiguredGroups.getCatalogue(), loaded.getGroupCatalogue());
    }

    @Test
    void read_duplicateCodesOrInvalidFields_entersLoadFailure() throws Exception {
        String valid = "{\"code\":\"PHY\",\"label\":\"Physics\",\"kind\":\"Subject\"}";
        String[] groups = {
            "[" + valid + "," + valid.replace("Physics", "Other") + "]",
            "[{\"code\":\"P\",\"label\":\"Physics\",\"kind\":\"Subject\"}]",
            "[{\"code\":\"PHY\",\"label\":\"\",\"kind\":\"Subject\"}]",
            "[{\"code\":\"PHY\",\"label\":\"Physics\",\"kind\":\"Tutorial\"}]",
            "null", "[null]"
        };
        for (int i = 0; i < groups.length; i++) {
            Path path = testFolder.resolve("invalid-" + i + ".json");
            Files.writeString(path, "{\"students\":[],\"groups\":" + groups[i] + "}");

            assertThrows(DataLoadingException.class, () -> new JsonAddressBookStorage(path).readAddressBook(),
                    groups[i]);
        }
    }
}
