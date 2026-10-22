package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
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
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.group.GroupAssignment;
import seedu.address.model.group.GroupCode;
import seedu.address.model.student.AcademicLevel;
import seedu.address.model.student.Student;
import seedu.address.model.student.StudentId;
import seedu.address.model.student.StudentName;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

class FilterStudentsUiTest {
    @TempDir
    private Path testFolder;

    @BeforeAll
    static void startJavaFxToolkit() {
        JavaFxTestUtils.startJavaFxToolkit();
    }

    @Test
    void filters_switchToStudentsShowSummaryAndPreserveStateOnError() {
        JavaFxTestUtils.runOnFxThread(() -> {
            ModelManager model = createModel();
            LogicManager logic = new LogicManager(model, new StorageManager(
                    new JsonAddressBookStorage(testFolder.resolve("students.json")),
                    new JsonUserPrefsStorage(testFolder.resolve("preferences.json"))));
            Stage stage = new Stage();
            MainWindow window = new MainWindow(stage, logic, testFolder.resolve("students.json"));
            window.fillInnerParts();
            ToggleButton studentsTab = (ToggleButton) stage.getScene().lookup("#studentsTab");
            ToggleButton groupsTab = (ToggleButton) stage.getScene().lookup("#groupsTab");
            Label summary = window.getStudentFilterSummaryLabel();

            groupsTab.fire();
            submit(stage, "filter-students g/MAT");

            assertTrue(studentsTab.isSelected());
            assertEquals(List.of("Mei Lin"), visibleNames(logic));
            assertEquals("Found 1 student(s) assigned to MAT.", feedback(stage));
            assertEquals("Filtered by group: MAT — Mathematics", summary.getText());
            assertTrue(summary.isVisible());
            assertTrue(summary.isManaged());

            submit(stage, "filter-students al/Sec 2");
            assertEquals(List.of("Amy Tan", "Mei Lin"), visibleNames(logic));
            assertEquals("Filtered by academic level: Sec 2", summary.getText());

            groupsTab.fire();
            submit(stage, "filter-students g/S2-MAT-A");
            assertTrue(groupsTab.isSelected());
            assertEquals(List.of("Amy Tan", "Mei Lin"), visibleNames(logic));
            assertEquals("Filtered by academic level: Sec 2", summary.getText());
            assertEquals("Group S2-MAT-A is a Class group; use the academic-level filter.", feedback(stage));

            submit(stage, "filter-students g//MAT");
            assertEquals("Invalid command format. Use: filter-students g/GROUP_CODE", feedback(stage));
            assertTrue(groupsTab.isSelected());
            assertEquals(List.of("Amy Tan", "Mei Lin"), visibleNames(logic));
            assertEquals("Filtered by academic level: Sec 2", summary.getText());

            submit(stage, "filter-students al/Sec 2 /extra");
            assertEquals("Invalid command format. Use: filter-students al/ACADEMIC_LEVEL", feedback(stage));
            assertTrue(groupsTab.isSelected());
            assertEquals(List.of("Amy Tan", "Mei Lin"), visibleNames(logic));
            assertEquals("Filtered by academic level: Sec 2", summary.getText());

            submit(stage, "list-students");
            assertEquals(List.of("Amy Tan", "Mei Lin", "S. Kumar"), visibleNames(logic));
            assertFalse(summary.isVisible());
            assertFalse(summary.isManaged());
            stage.close();
        });
    }

    @Test
    void failedAssignment_restoresFilteredRowsSummarySelectionAndWorkspace() {
        assertFailedSavePreservesGroupFilter("assign-group sid/S-1851 g/MAT",
                "Could not save changes. The group was not assigned.");
    }

    @Test
    void failedUnassignment_restoresFilteredRowsSummarySelectionAndWorkspace() {
        assertFailedSavePreservesGroupFilter("unassign-group sid/S-1852 g/MAT",
                "Could not save changes. The group assignment was not removed.");
    }

    private void assertFailedSavePreservesGroupFilter(String command, String expectedFeedback) {
        JavaFxTestUtils.runOnFxThread(() -> {
            ModelManager model = createModel();
            AddressBook original = new AddressBook(model.getAddressBook());
            JsonAddressBookStorage data = new JsonAddressBookStorage(testFolder.resolve("students.json")) {
                @Override
                public void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
                    throw new IOException("Simulated save failure");
                }
            };
            LogicManager logic = new LogicManager(model, new StorageManager(data,
                    new JsonUserPrefsStorage(testFolder.resolve("preferences.json"))));
            Stage stage = new Stage();
            try {
                MainWindow window = new MainWindow(stage, logic, testFolder.resolve("students.json"));
                window.fillInnerParts();
                submit(stage, "filter-students g/MAT");
                window.getStudentListPanel().selectStudentId("S-1852");
                ToggleButton groupsTab = (ToggleButton) stage.getScene().lookup("#groupsTab");
                groupsTab.fire();

                submit(stage, command);

                assertEquals(expectedFeedback, feedback(stage));
                assertEquals(original, model.getAddressBook());
                assertEquals(List.of("Mei Lin"), visibleNames(logic));
                assertEquals("S-1852", window.getStudentListPanel().getSelectedStudentId());
                assertEquals("Filtered by group: MAT — Mathematics",
                        window.getStudentFilterSummaryLabel().getText());
                assertTrue(window.getStudentFilterSummaryLabel().isVisible());
                assertTrue(groupsTab.isSelected());
            } finally {
                stage.close();
            }
        });
    }

    private static ModelManager createModel() {
        ModelManager model = new ModelManager();
        Student amy = student("S-1851", "Amy Tan", "Sec 2");
        Student mei = student("S-1852", "Mei Lin", "Sec 2");
        Student kumar = student("S-1853", "S. Kumar", "IP 4");
        model.addStudent(kumar);
        model.addStudent(mei);
        model.addStudent(amy);
        model.addGroupAssignment(new GroupAssignment(mei.getId(), new GroupCode("MAT")));
        return model;
    }

    private static List<String> visibleNames(LogicManager logic) {
        return logic.getStudentList().stream().map(student -> student.getName().toString()).toList();
    }

    private static String feedback(Stage stage) {
        return ((Label) stage.getScene().lookup("#resultDisplay")).getText();
    }

    private static void submit(Stage stage, String command) {
        TextField input = (TextField) stage.getScene().lookup("#commandTextField");
        input.setText(command);
        input.getOnAction().handle(new ActionEvent(input, input));
    }

    private static Student student(String id, String name, String level) {
        return new Student(new StudentId(id), new StudentName(name), new AcademicLevel(level));
    }
}
