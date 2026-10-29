package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javafx.event.ActionEvent;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.stage.Stage;
import seedu.address.logic.LogicManager;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.student.AcademicLevel;
import seedu.address.model.student.Student;
import seedu.address.model.student.StudentId;
import seedu.address.model.student.StudentName;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

class StudentQueryNavigationTest {
    @TempDir
    private Path testFolder;

    @BeforeAll
    static void startJavaFxToolkit() {
        JavaFxTestUtils.startJavaFxToolkit();
    }

    @Test
    void successfulQueries_fromOtherTabs_showStudentsIncludingEmptyResults() {
        JavaFxTestUtils.runOnFxThread(() -> {
            ModelManager model = new ModelManager();
            model.addStudent(student("S-1601", "Ben Tan"));
            model.addStudent(student("S-1602", "Ben Lim"));
            model.addStudent(student("S-1603", "Amy Tan"));
            Stage stage = new Stage();
            try {
                MainWindow window = createWindow(stage, model);
                for (String tab : List.of("groupsTab", "guardiansTab")) {
                    switchTo(stage, tab);
                    submit(stage, "find-student n/ben");
                    assertStudentWorkspace(stage);
                    assertEquals(List.of("Ben Lim", "Ben Tan"), names(model));

                    switchTo(stage, tab);
                    submit(stage, "find-student n/Ben Tan");
                    assertStudentWorkspace(stage);
                    assertEquals(List.of("Ben Tan"), names(model));

                    switchTo(stage, tab);
                    submit(stage, "find-student n/Zara");
                    assertStudentWorkspace(stage);
                    assertTrue(model.getStudentList().isEmpty());
                    assertEquals("No students match \"Zara\".", feedback(stage));

                    model.updateFilteredStudentList(student -> false, "Previous filter");
                    switchTo(stage, tab);
                    submit(stage, "list-students");
                    assertStudentWorkspace(stage);
                    assertEquals(List.of("Amy Tan", "Ben Lim", "Ben Tan"), names(model));
                    assertFalse(window.getStudentFilterSummaryLabel().isVisible());
                }
            } finally {
                stage.close();
            }
        });
    }

    @Test
    void listEmptyRegister_fromOtherTabs_showsStudentsAndEmptyFeedback() {
        JavaFxTestUtils.runOnFxThread(() -> {
            Stage stage = new Stage();
            try {
                createWindow(stage, new ModelManager());
                for (String tab : List.of("groupsTab", "guardiansTab")) {
                    switchTo(stage, tab);
                    submit(stage, "list-students");
                    assertStudentWorkspace(stage);
                    assertEquals("No students registered.", feedback(stage));
                }
            } finally {
                stage.close();
            }
        });
    }

    @Test
    void invalidQueries_preserveTabListAndSelection() {
        JavaFxTestUtils.runOnFxThread(() -> {
            ModelManager model = new ModelManager();
            model.addStudent(student("S-1601", "Ben Tan"));
            Stage stage = new Stage();
            try {
                MainWindow window = createWindow(stage, model);
                for (String tab : List.of("groupsTab", "guardiansTab")) {
                    switchTo(stage, tab);
                    for (String command : List.of("list-students extra", "find-student", "find-student n/123")) {
                        submit(stage, command);
                        assertTrue(((ToggleButton) stage.getScene().lookup("#" + tab)).isSelected());
                        assertFalse(stage.getScene().lookup("#studentWorkspace").isVisible());
                        assertEquals(List.of("Ben Tan"), names(model));
                        assertEquals("S-1601", window.getStudentListPanel().getSelectedStudentId());
                    }
                }
            } finally {
                stage.close();
            }
        });
    }

    private MainWindow createWindow(Stage stage, ModelManager model) {
        JsonAddressBookStorage data = new JsonAddressBookStorage(testFolder.resolve("data.json")) {
            @Override
            public void saveAddressBook(ReadOnlyAddressBook addressBook) {
                throw new AssertionError("Student query commands must not save data");
            }
        };
        LogicManager logic = new LogicManager(model, new StorageManager(data,
                new JsonUserPrefsStorage(testFolder.resolve("prefs.json"))));
        MainWindow window = new MainWindow(stage, logic, data.getAddressBookFilePath());
        window.fillInnerParts();
        return window;
    }

    private static void assertStudentWorkspace(Stage stage) {
        assertTrue(((ToggleButton) stage.getScene().lookup("#studentsTab")).isSelected());
        assertFalse(((ToggleButton) stage.getScene().lookup("#groupsTab")).isSelected());
        assertFalse(((ToggleButton) stage.getScene().lookup("#guardiansTab")).isSelected());
        assertTrue(stage.getScene().lookup("#studentWorkspace").isVisible());
        assertTrue(stage.getScene().lookup("#studentWorkspace").isManaged());
        assertFalse(stage.getScene().lookup("#guardianWorkspace").isVisible());
        assertFalse(stage.getScene().lookup("#groupsWorkspacePlaceholder").isVisible());
    }

    private static void switchTo(Stage stage, String tab) {
        ToggleButton button = (ToggleButton) stage.getScene().lookup("#" + tab);
        button.fire();
    }

    private static List<String> names(ModelManager model) {
        return model.getStudentList().stream().map(student -> student.getName().toString()).toList();
    }

    private static String feedback(Stage stage) {
        return ((Label) stage.getScene().lookup("#resultDisplay")).getText();
    }

    private static void submit(Stage stage, String command) {
        TextField input = (TextField) stage.getScene().lookup("#commandTextField");
        input.setText(command);
        input.getOnAction().handle(new ActionEvent(input, input));
    }

    private static Student student(String id, String name) {
        return new Student(new StudentId(id), new StudentName(name), new AcademicLevel("Sec 2"));
    }
}
