package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.GuardianLinkTestUtil.getUnlinkedAddressBook;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import seedu.address.logic.LogicManager;
import seedu.address.logic.commands.UnlinkGuardianCommand;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.UserPrefs;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

class UnlinkGuardianUiTest {
    @TempDir
    Path temporaryFolder;

    @BeforeAll
    static void startJavaFxToolkit() {
        JavaFxTestUtils.startJavaFxToolkit();
    }

    @Test
    void submit_unlinkSelectedStudent_refreshesBothProfilesAndGuardianCount() {
        JavaFxTestUtils.runOnFxThread(() -> {
            ModelManager model = linkedModel();
            Stage stage = new Stage();
            try {
                MainWindow window = openWindow(stage, model,
                        new JsonAddressBookStorage(temporaryFolder.resolve("register.json")));
                Object selected = window.getStudentListPanel().selectedStudentProperty().get();
                submit(stage, "unlink-guardian sid/S-0007");

                assertSame(selected, window.getStudentListPanel().selectedStudentProperty().get());
                assertEquals("Guardian: none", studentGuardian(stage));
                assertLinkedStudents(stage, "S-0008 - Mei Ling");
                submit(stage, "list-guardians");
                stage.getScene().getRoot().applyCss();
                stage.getScene().getRoot().layout();
                Node anitaCard = stage.getScene().getRoot().lookupAll("#cardPane").stream()
                        .filter(card -> card.lookup("#id") instanceof Label id
                                && id.getText().equals("G-0003"))
                        .findFirst().orElseThrow();
                assertEquals("1", ((Label) anitaCard.lookup("#linkedStudentCount")).getText());

                submit(stage, "unlink-guardian sid/S-0008");
                assertEquals("S-0008", window.getStudentListPanel().getSelectedStudentId());
                assertEquals("Guardian: none", studentGuardian(stage));
                assertLinkedStudents(stage, "No linked students.");
                assertEquals(2, model.getGuardianList().size());
                assertTrue(((ToggleButton) stage.getScene().lookup("#studentsTab")).isSelected());
            } finally {
                stage.close();
            }
        });
    }

    @Test
    void submit_saveFailure_preservesSelectionProfilesAndWorkspace() {
        JavaFxTestUtils.runOnFxThread(() -> {
            ModelManager model = linkedModel();
            JsonAddressBookStorage storage = new JsonAddressBookStorage(temporaryFolder.resolve("failed.json")) {
                @Override
                public void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
                    throw new IOException("Disk full");
                }
            };
            Stage stage = new Stage();
            try {
                MainWindow window = openWindow(stage, model, storage);
                submit(stage, "list-guardians");
                Object guardian = window.getGuardianListPanel().selectedGuardianProperty().get();
                submit(stage, "unlink-guardian sid/S-0007");

                Label result = (Label) stage.getScene().lookup("#resultDisplay");
                assertEquals(UnlinkGuardianCommand.MESSAGE_SAVE_FAILURE, result.getText());
                assertEquals("S-0007", window.getStudentListPanel().getSelectedStudentId());
                assertEquals("Anita Lim\n+65 91234567", studentGuardian(stage));
                assertSame(guardian, window.getGuardianListPanel().selectedGuardianProperty().get());
                assertTrue(((ToggleButton) stage.getScene().lookup("#guardiansTab")).isSelected());
                assertLinkedStudents(stage, "S-0007 - Mei Lin", "S-0008 - Mei Ling");
            } finally {
                stage.close();
            }
        });
    }

    private ModelManager linkedModel() {
        ModelManager model = new ModelManager(getUnlinkedAddressBook(), new UserPrefs());
        model.linkGuardian("S-0007", "G-0003");
        model.linkGuardian("S-0008", "G-0003");
        return model;
    }

    private MainWindow openWindow(Stage stage, ModelManager model, JsonAddressBookStorage storage) {
        LogicManager logic = new LogicManager(model, new StorageManager(storage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("prefs.json"))));
        MainWindow window = new MainWindow(stage, logic, storage.getAddressBookFilePath());
        window.fillInnerParts();
        stage.getScene().getRoot().resize(1000, 700);
        stage.getScene().getRoot().applyCss();
        stage.getScene().getRoot().layout();
        return window;
    }

    private static String studentGuardian(Stage stage) {
        return ((Label) stage.getScene().lookup("#guardianDetails")).getText();
    }

    private static void assertLinkedStudents(Stage stage, String... expected) {
        Node details = stage.getScene().lookup("#guardianDetailsRoot");
        VBox students = (VBox) details.lookup("#linkedStudents");
        assertEquals(List.of(expected), students.getChildren().stream()
                .map(node -> ((Label) node).getText()).toList());
    }

    private static void submit(Stage stage, String command) {
        TextField input = (TextField) stage.getScene().lookup("#commandTextField");
        input.setText(command);
        input.getOnAction().handle(new ActionEvent(input, input));
    }
}
