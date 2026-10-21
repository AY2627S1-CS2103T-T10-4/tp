package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.GuardianLinkTestUtil.getUnlinkedAddressBook;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import seedu.address.logic.LogicManager;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

class FindGuardianUiTest {
    @TempDir
    Path temporaryFolder;

    @BeforeAll
    static void startJavaFxToolkit() {
        JavaFxTestUtils.startJavaFxToolkit();
    }

    @Test
    void submit_searchAndReset_displaysMatchingContactAndLinkedStudents() {
        JavaFxTestUtils.runOnFxThread(() -> {
            ModelManager model = modelWithLinkedGuardian();
            Stage stage = new Stage();
            try {
                MainWindow window = openWindow(stage, model);
                Object student = window.getStudentListPanel().selectedStudentProperty().get();
                submit(stage, "list-groups");
                submit(stage, "find-guardian n/Anita");
                assertTrue(tab(stage, "guardiansTab").isSelected());
                assertSame(student, window.getStudentListPanel().selectedStudentProperty().get());
                assertEquals(1, model.getGuardianList().size());

                ListView<?> list = (ListView<?>) stage.getScene().lookup("#guardianListView");
                list.getSelectionModel().selectFirst();
                Node details = stage.getScene().lookup("#guardianDetailsRoot");
                assertEquals("Anita Lim", ((Label) details.lookup("#name")).getText());
                assertEquals("+65 91234567", ((Label) details.lookup("#phone")).getText());
                VBox students = (VBox) details.lookup("#linkedStudents");
                assertEquals("S-0007 - Mei Lin", ((Label) students.getChildren().getFirst()).getText());

                submit(stage, "find-guardian n/Raj");
                assertEquals("G-0004", model.getGuardianList().getFirst().getId().toString());
                list.getSelectionModel().selectFirst();
                assertEquals("Raj Kumar", ((Label) details.lookup("#name")).getText());
                submit(stage, "list-guardians");
                assertEquals(2, model.getGuardianList().size());
                assertFalse(Files.exists(temporaryFolder.resolve("register.json")));
            } finally {
                stage.close();
            }
        });
    }

    @Test
    void submit_invalidAndEmptySearch_preservesStateOrShowsAccurateEmptyMessage() {
        JavaFxTestUtils.runOnFxThread(() -> {
            ModelManager model = modelWithLinkedGuardian();
            Stage stage = new Stage();
            try {
                openWindow(stage, model);
                submit(stage, "find-guardian n/Anita");
                tab(stage, "studentsTab").fire();
                submit(stage, "find-guardian n/Raj n/Anita");
                assertTrue(tab(stage, "studentsTab").isSelected());
                assertEquals("G-0003", model.getGuardianList().getFirst().getId().toString());

                submit(stage, "find-guardian n/Nobody");
                assertTrue(tab(stage, "guardiansTab").isSelected());
                ListView<?> list = (ListView<?>) stage.getScene().lookup("#guardianListView");
                assertTrue(list.getItems().isEmpty());
                assertEquals("No guardians match \"Nobody\"", ((Label) list.getPlaceholder()).getText());
                Node details = stage.getScene().lookup("#guardianDetailsRoot");
                assertFalse(details.lookup("#detailsContent").isVisible());
                submit(stage, "list-guardians");
                assertEquals(2, list.getItems().size());
            } finally {
                stage.close();
            }
        });
    }

    private ModelManager modelWithLinkedGuardian() {
        ModelManager model = new ModelManager(getUnlinkedAddressBook(), new UserPrefs());
        model.linkGuardian("S-0007", "G-0003");
        return model;
    }

    private MainWindow openWindow(Stage stage, ModelManager model) {
        Path dataPath = temporaryFolder.resolve("register.json");
        LogicManager logic = new LogicManager(model, new StorageManager(new JsonAddressBookStorage(dataPath),
                new JsonUserPrefsStorage(temporaryFolder.resolve("prefs.json"))));
        MainWindow window = new MainWindow(stage, logic, dataPath);
        window.fillInnerParts();
        stage.getScene().getRoot().applyCss();
        return window;
    }

    private static ToggleButton tab(Stage stage, String id) {
        return (ToggleButton) stage.getScene().lookup("#" + id);
    }

    private static void submit(Stage stage, String command) {
        TextField input = (TextField) stage.getScene().lookup("#commandTextField");
        input.setText(command);
        input.getOnAction().handle(new ActionEvent(input, input));
    }
}
