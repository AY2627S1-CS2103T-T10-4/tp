package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.guardian.Guardian;
import seedu.address.model.guardian.GuardianId;
import seedu.address.model.guardian.GuardianName;
import seedu.address.model.guardian.GuardianPhone;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

class ListGuardiansLogicIntegrationTest {
    @TempDir
    private Path temporaryFolder;

    @Test
    void execute_listGuardians_clearsFilterWithoutSaving() throws Exception {
        ModelManager model = new ModelManager();
        model.addGuardian(new Guardian(new GuardianId("G-0501"), new GuardianName("Anita Lim"),
                new GuardianPhone("91234567")));
        model.updateFilteredGuardianList(unused -> false);
        Path dataPath = temporaryFolder.resolve("must-not-be-written.json");
        JsonAddressBookStorage forbiddenStorage = new JsonAddressBookStorage(dataPath) {
            @Override
            public void saveAddressBook(ReadOnlyAddressBook addressBook) {
                throw new AssertionError("list-guardians must not save register data");
            }
        };
        Logic logic = new LogicManager(model, new StorageManager(forbiddenStorage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("prefs.json"))));

        var result = logic.execute("LIST-GUARDIANS");

        assertEquals("Listed 1 guardian(s).", result.getFeedbackToUser());
        assertEquals(1, logic.getGuardianList().size());
        assertFalse(Files.exists(dataPath));
    }
}
