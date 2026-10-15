package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.GuardianLinkTestUtil.getUnlinkedAddressBook;

import java.io.IOException;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javafx.event.ActionEvent;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import seedu.address.logic.Logic;
import seedu.address.logic.LogicManager;
import seedu.address.logic.commands.LinkGuardianCommand;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.UserPrefs;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

/** Exercises guardian profile updates through real commands and persistence. */
class LinkGuardianProfileTest {
    @TempDir
    Path temporaryFolder;

    @BeforeAll
    static void startJavaFxToolkit() {
        JavaFxTestUtils.startJavaFxToolkit();
    }

    @Test
    void submit_linkSelectedAndDifferentStudent_refreshesGuardianAndRestoresAfterReload() {
        JavaFxTestUtils.runOnFxThread(() -> {
            JsonAddressBookStorage storage = new JsonAddressBookStorage(temporaryFolder.resolve("register.json"));
            ModelManager model = new ModelManager(getUnlinkedAddressBook(), new UserPrefs());
            Stage stage = new Stage();
            try {
                openWindow(stage, model, storage);
                assertEquals("Guardian: none", label(stage, "guardianDetails"));

                submit(stage, "assign-group sid/S-0007 g/MAT");

                submit(stage, "link-guardian sid/S-0007 gid/G-0003");
                assertEquals("S-0007", label(stage, "id"));
                assertEquals("Anita Lim\n+65 91234567", label(stage, "guardianDetails"));
                VBox assignments = (VBox) stage.getScene().lookup("#assignments");
                assertTrue(((Label) assignments.getChildren().get(0)).getText().startsWith("MAT"));

                submit(stage, "view-student sid/S-0008");
                assertEquals("Guardian: none", label(stage, "guardianDetails"));
                submit(stage, "view-student sid/S-0007");
                submit(stage, "link-guardian sid/S-0008 gid/G-0004");
                assertEquals("S-0008", label(stage, "id"));
                assertEquals("Raj Kumar\n+65 61234567", label(stage, "guardianDetails"));

                stage.close();
                ModelManager reloaded = new ModelManager(storage.readAddressBook().orElseThrow(), new UserPrefs());
                openWindow(stage, reloaded, storage);
                assertEquals("Anita Lim\n+65 91234567", label(stage, "guardianDetails"));
            } finally {
                stage.close();
            }
        });
    }

    @Test
    void submit_linkHiddenStudent_revealsStudentAndOpensProfile() {
        JavaFxTestUtils.runOnFxThread(() -> {
            ModelManager model = new ModelManager(getUnlinkedAddressBook(), new UserPrefs());
            Stage stage = new Stage();
            try {
                MainWindow window = openWindow(stage, model,
                        new JsonAddressBookStorage(temporaryFolder.resolve("register.json")));
                submit(stage, "find-student n/No Match");
                ToggleButton groupsTab = (ToggleButton) stage.getScene().lookup("#groupsTab");
                groupsTab.fire();
                submit(stage, "link-guardian sid/S-0007 gid/G-0003");

                assertEquals(2, model.getStudentList().size());
                assertEquals("S-0007", window.getStudentListPanel().getSelectedStudentId());
                assertTrue(((ToggleButton) stage.getScene().lookup("#studentsTab")).isSelected());
                assertEquals("S-0007", label(stage, "id"));
                assertEquals("Anita Lim\n+65 91234567", label(stage, "guardianDetails"));
            } finally {
                stage.close();
            }
        });
    }

    @Test
    void submit_failedSave_keepsExistingProfileAndGuardian() {
        JavaFxTestUtils.runOnFxThread(() -> {
            ModelManager model = new ModelManager(getUnlinkedAddressBook(), new UserPrefs());
            model.linkGuardian("S-0007", "G-0003");
            JsonAddressBookStorage storage = new JsonAddressBookStorage(temporaryFolder.resolve("register.json")) {
                @Override
                public void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
                    throw new IOException("Disk full");
                }
            };
            Stage stage = new Stage();
            try {
                openWindow(stage, model, storage);
                submit(stage, "link-guardian sid/S-0008 gid/G-0004");
                assertEquals(LinkGuardianCommand.MESSAGE_SAVE_FAILURE, label(stage, "resultDisplay"));
                assertEquals("S-0007", label(stage, "id"));
                assertEquals("Anita Lim\n+65 91234567", label(stage, "guardianDetails"));
                submit(stage, "view-student sid/S-0008");
                assertEquals("Guardian: none", label(stage, "guardianDetails"));
            } finally {
                stage.close();
            }
        });
    }

    private MainWindow openWindow(Stage stage, ModelManager model, JsonAddressBookStorage storage) {
        Logic logic = new LogicManager(model, new StorageManager(storage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("prefs.json"))));
        MainWindow window = new MainWindow(stage, logic, storage.getAddressBookFilePath());
        window.fillInnerParts();
        stage.getScene().getRoot().applyCss();
        return window;
    }

    private static String label(Stage stage, String id) {
        return ((Label) stage.getScene().lookup("#" + id)).getText();
    }

    private static void submit(Stage stage, String command) {
        TextField input = (TextField) stage.getScene().lookup("#commandTextField");
        input.setText(command);
        input.getOnAction().handle(new ActionEvent(input, input));
    }
}
