package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javafx.event.ActionEvent;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import seedu.address.logic.Logic;
import seedu.address.logic.LogicManager;
import seedu.address.model.ModelManager;
import seedu.address.model.guardian.Guardian;
import seedu.address.model.guardian.GuardianId;
import seedu.address.model.guardian.GuardianName;
import seedu.address.model.guardian.GuardianPhone;
import seedu.address.model.student.AcademicLevel;
import seedu.address.model.student.Student;
import seedu.address.model.student.StudentId;
import seedu.address.model.student.StudentName;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

class ListGuardiansUiTest {
    @TempDir
    private Path temporaryFolder;

    @BeforeAll
    static void startJavaFxToolkit() {
        JavaFxTestUtils.startJavaFxToolkit();
    }

    @Test
    void tabsAndListCommand_showSortedGuardianWorkspaceWithDetails() {
        JavaFxTestUtils.runOnFxThread(() -> {
            ModelManager model = new ModelManager();
            Guardian raj = guardian("G-0402", "Raj Kumar", "61234567");
            Guardian anita = guardian("G-0401", "Anita Lim", "91234567");
            Student mei = new Student(new StudentId("S-0401"), new StudentName("Mei Lin"),
                    new AcademicLevel("Sec 2"));
            model.addGuardian(raj);
            model.addGuardian(anita);
            model.addStudent(mei);
            model.linkGuardian(mei.getId().value, anita.getId().toString());
            Path dataPath = temporaryFolder.resolve("guardians.json");
            Logic logic = new LogicManager(model, new StorageManager(new JsonAddressBookStorage(dataPath),
                    new JsonUserPrefsStorage(temporaryFolder.resolve("prefs.json"))));
            Stage stage = new Stage();
            MainWindow mainWindow = new MainWindow(stage, logic, dataPath);
            mainWindow.fillInnerParts();
            stage.getScene().getRoot().applyCss();

            ToggleButton studentsTab = toggle(stage, "#studentsTab");
            ToggleButton guardiansTab = toggle(stage, "#guardiansTab");
            ToggleButton groupsTab = toggle(stage, "#groupsTab");
            StackPane studentWorkspace = pane(stage, "#studentWorkspace");
            StackPane guardianWorkspace = pane(stage, "#guardianWorkspace");
            StackPane groupsWorkspace = pane(stage, "#groupsWorkspacePlaceholder");

            assertTrue(studentsTab.isSelected());
            assertFalse(guardiansTab.isSelected());
            assertFalse(groupsTab.isSelected());

            TextField input = (TextField) stage.getScene().lookup("#commandTextField");
            input.setText("LIST-GUARDIANS");
            submit(input);

            assertFalse(studentsTab.isSelected());
            assertTrue(guardiansTab.isSelected());
            assertFalse(groupsTab.isSelected());
            assertFalse(studentWorkspace.isVisible());
            assertTrue(guardianWorkspace.isVisible());
            assertFalse(groupsWorkspace.isVisible());
            assertEquals("Listed 2 guardian(s).", label(stage, "#resultDisplay").getText());
            assertEquals("2 guardians", label(stage, "#guardianCountLabel").getText());
            ListView<?> guardians = (ListView<?>) stage.getScene().lookup("#guardianListView");
            assertEquals(2, guardians.getItems().size());
            assertEquals(anita, guardians.getItems().getFirst());
            assertSame(anita, mainWindow.getGuardianListPanel().selectedGuardianProperty().get());
            VBox details = (VBox) stage.getScene().lookup("#guardianDetailsRoot");
            assertEquals("Anita Lim", ((Label) details.lookup("#name")).getText());
            assertEquals("+65 91234567", ((Label) details.lookup("#phone")).getText());
            VBox linkedStudents = (VBox) details.lookup("#linkedStudents");
            assertEquals("S-0401 - Mei Lin", ((Label) linkedStudents.getChildren().getFirst()).getText());

            studentsTab.fire();
            input.setText("list-guardians extra");
            submit(input);
            assertTrue(studentsTab.isSelected());
            assertFalse(guardiansTab.isSelected());
            assertEquals("Invalid command format. Use: list-guardians", label(stage, "#resultDisplay").getText());
            stage.close();
        });
    }

    private static Guardian guardian(String id, String name, String phone) {
        return new Guardian(new GuardianId(id), new GuardianName(name), new GuardianPhone(phone));
    }

    private static ToggleButton toggle(Stage stage, String selector) {
        return (ToggleButton) stage.getScene().lookup(selector);
    }

    private static StackPane pane(Stage stage, String selector) {
        return (StackPane) stage.getScene().lookup(selector);
    }

    private static Label label(Stage stage, String selector) {
        return (Label) stage.getScene().lookup(selector);
    }

    private static void submit(TextField input) {
        input.getOnAction().handle(new ActionEvent(input, input));
    }
}
