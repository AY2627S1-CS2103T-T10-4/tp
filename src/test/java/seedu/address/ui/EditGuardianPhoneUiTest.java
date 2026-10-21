package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.GuardianLinkTestUtil.getUnlinkedAddressBook;

import java.nio.file.Path;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javafx.event.ActionEvent;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import seedu.address.logic.LogicManager;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

/** Verifies that editing a shared guardian record immediately refreshes open student profiles. */
class EditGuardianPhoneUiTest {
    @TempDir
    Path temporaryFolder;

    @BeforeAll
    static void startJavaFxToolkit() {
        JavaFxTestUtils.startJavaFxToolkit();
    }

    @Test
    void submit_editLinkedGuardianPhone_refreshesSelectedAndOtherStudentProfiles() {
        JavaFxTestUtils.runOnFxThread(() -> {
            ModelManager model = new ModelManager(getUnlinkedAddressBook(), new UserPrefs());
            model.linkGuardian("S-0007", "G-0003");
            model.linkGuardian("S-0008", "G-0003");
            JsonAddressBookStorage storage = new JsonAddressBookStorage(temporaryFolder.resolve("register.json"));
            Stage stage = new Stage();
            try {
                MainWindow window = openWindow(stage, model, storage);
                assertEquals("S-0007", window.getStudentListPanel().getSelectedStudentId());
                assertEquals("Anita Lim\n+65 91234567", guardianDetails(stage));

                submit(stage, "edit-guardian gid/G-0003 p/98765432");

                assertEquals("S-0007", window.getStudentListPanel().getSelectedStudentId());
                assertEquals("Anita Lim\n+65 98765432", guardianDetails(stage));
                submit(stage, "view-student sid/S-0008");
                assertEquals("Anita Lim\n+65 98765432", guardianDetails(stage));
            } finally {
                stage.close();
            }
        });
    }

    private MainWindow openWindow(Stage stage, ModelManager model, JsonAddressBookStorage storage) {
        LogicManager logic = new LogicManager(model, new StorageManager(storage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("prefs.json"))));
        MainWindow window = new MainWindow(stage, logic, storage.getAddressBookFilePath());
        window.fillInnerParts();
        stage.getScene().getRoot().applyCss();
        return window;
    }

    private static String guardianDetails(Stage stage) {
        return ((Label) stage.getScene().lookup("#guardianDetails")).getText();
    }

    private static void submit(Stage stage, String command) {
        TextField input = (TextField) stage.getScene().lookup("#commandTextField");
        input.setText(command);
        input.getOnAction().handle(new ActionEvent(input, input));
    }
}
