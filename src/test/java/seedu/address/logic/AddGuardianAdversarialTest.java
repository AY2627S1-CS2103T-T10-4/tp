package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.ArrayList;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.AddGuardianCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

/** Contract-based probes; every command starts from its own copy of the supplied baseline. */
class AddGuardianAdversarialTest {
    @TempDir
    Path directory;

    @Test
    void unknownTextMustUseSharedFormatError() throws Exception {
        List<String> failures = new ArrayList<>();
        String[] commands = {
            "add-guardian n/Ann p/61239999 extra",
            "add-guardian p/61239999 n/Ann 1/value",
            "add-guardian n/Ann p/61239999 x/value",
            "add-guardian n/Ann n/Ann p/61239999",
            "add-guardian n/Ann p/61239999 p/61239999",
            "add-guardian p/61239999",
            "add-guardian n/Ann",
            "add-guardian n/ p/61239999",
            "add-guardian n/Ann p/"
        };
        for (int i = 0; i < commands.length; i++) {
            Path file = directory.resolve("case-" + i + ".json");
            Files.copy(Path.of("test/data/addressbook.json"), file);
            JsonAddressBookStorage storage = new JsonAddressBookStorage(file);
            ModelManager model = new ModelManager(storage.readAddressBook().orElseThrow(), new UserPrefs());
            Logic logic = new LogicManager(model, new StorageManager(storage,
                    new JsonUserPrefsStorage(directory.resolve("prefs-" + i + ".json"))));
            logic.execute("find-student n/Mei Lin");
            AddressBook before = new AddressBook(model.getAddressBook());
            var visible = List.copyOf(model.getStudentList());
            String bytes = Files.readString(file);
            String command = commands[i];
            ParseException error = assertThrows(ParseException.class, () -> logic.execute(command), command);
            if (!AddGuardianCommand.MESSAGE_USAGE.equals(error.getMessage())) {
                failures.add(command + " -> " + error.getMessage());
            }
            assertEquals(before, model.getAddressBook(), command);
            assertEquals(visible, model.getStudentList(), command);
            assertEquals(bytes, Files.readString(file), command);
        }
        assertEquals(List.of(), failures, "Shared error contract violations");
    }
}
