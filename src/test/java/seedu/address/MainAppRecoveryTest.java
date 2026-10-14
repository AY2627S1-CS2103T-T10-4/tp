package seedu.address;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.LogicManager;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.UserPrefs;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

class MainAppRecoveryTest {
    @TempDir
    private Path testFolder;

    @Test
    void initModelManager_missingFile_loadsSamplesAndAllowsNormalSaving() throws Exception {
        StorageManager storage = createStorage();

        Model model = new MainApp().initModelManager(storage, new UserPrefs());

        assertTrue(storage.getDataRecoveryReason().isEmpty());
        assertEquals(List.of("Mei Lin", "S. Kumar"), model.getStudentList().stream()
                .map(student -> student.getName().toString()).toList());
        assertFalse(Files.exists(storage.getAddressBookFilePath()));
        new LogicManager(model, storage).execute("add-student n/Amy Tan al/Sec 3");
        assertEquals(model.getAddressBook(), storage.readAddressBook().orElseThrow());
    }

    @Test
    void initModelManager_validExistingData_loadsAndKeepsSavingEnabled() throws Exception {
        StorageManager storage = createStorage();
        String json = "{\"students\":[{\"id\":\"S-0501\",\"name\":\"Amy Tan\",\"academicLevel\":\"Sec 3\"}]}";
        Files.writeString(storage.getAddressBookFilePath(), json);

        Model model = new MainApp().initModelManager(storage, new UserPrefs());

        assertTrue(storage.getDataRecoveryReason().isEmpty());
        assertEquals(1, model.getStudentList().size());
        assertEquals("S-0501", model.getStudentList().get(0).getId().value);
        assertEquals(json, Files.readString(storage.getAddressBookFilePath()));
        new LogicManager(model, storage).execute("add-student n/Mei Lin al/Sec 2");
        assertEquals(model.getAddressBook(), storage.readAddressBook().orElseThrow());
    }

    @Test
    void initModelManager_validEmptyFile_doesNotUseSamplesOrEnterRecovery() throws Exception {
        StorageManager storage = createStorage();
        Files.writeString(storage.getAddressBookFilePath(), "{\"students\":[]}");

        Model model = new MainApp().initModelManager(storage, new UserPrefs());

        assertTrue(model.getStudentList().isEmpty());
        assertTrue(storage.getDataRecoveryReason().isEmpty());
    }

    @Test
    void initModelManager_malformedFile_entersEmptyWriteDisabledRecovery() throws Exception {
        assertRecovery("broken JSON\r\n");
    }

    @Test
    void initModelManager_invalidDomainData_entersEmptyWriteDisabledRecovery() throws Exception {
        assertRecovery("{\"students\":[{\"id\":\"S-0502\",\"name\":\"Mei Lin\",\"academicLevel\":\"Sec 9\"}]}");
    }

    private void assertRecovery(String json) throws Exception {
        StorageManager storage = createStorage();
        byte[] original = json.getBytes(StandardCharsets.UTF_8);
        Files.write(storage.getAddressBookFilePath(), original);

        Model model = new MainApp().initModelManager(storage, new UserPrefs());

        assertTrue(model.getStudentList().isEmpty());
        assertTrue(storage.getDataRecoveryReason().isPresent());
        assertThrows(CommandException.class, () -> new LogicManager(model, storage)
                .execute("add-student n/Amy Tan al/Sec 3"));
        assertTrue(model.getStudentList().isEmpty());
        assertArrayEquals(original, Files.readAllBytes(storage.getAddressBookFilePath()));
    }

    private StorageManager createStorage() {
        return new StorageManager(new JsonAddressBookStorage(testFolder.resolve("students.json")),
                new JsonUserPrefsStorage(testFolder.resolve("prefs.json")));
    }
}
