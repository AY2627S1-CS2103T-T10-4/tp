package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import seedu.address.model.student.AcademicLevel;
import seedu.address.model.student.Student;
import seedu.address.model.student.StudentId;
import seedu.address.model.student.StudentName;

class StudentListPanelTest {
    @BeforeAll
    static void startJavaFxToolkit() {
        JavaFxTestUtils.startJavaFxToolkit();
    }

    @Test
    void selectionMethods_selectByStableIdAndFirstStudent() {
        JavaFxTestUtils.runOnFxThread(() -> {
            Student first = student("S-0092", "Amy Tan");
            Student second = student("S-0093", "Mei Lin");
            ObservableList<Student> students = FXCollections.observableArrayList(first, second);
            StudentListPanel panel = new StudentListPanel(students);

            panel.selectStudentId("S-0093");
            assertEquals(second, panel.selectedStudentProperty().get());
            panel.selectFirstStudent();
            assertEquals(first, panel.selectedStudentProperty().get());
            panel.selectStudentId("S-9999");
            assertEquals(first, panel.selectedStudentProperty().get());
        });
    }

    @Test
    void selectFirstStudent_emptyListLeavesSelectionEmpty() {
        JavaFxTestUtils.runOnFxThread(() -> {
            StudentListPanel panel = new StudentListPanel(FXCollections.observableArrayList());
            panel.selectFirstStudent();
            panel.selectStudentId("S-0001");
            assertNull(panel.selectedStudentProperty().get());
        });
    }

    private static Student student(String id, String name) {
        return new Student(new StudentId(id), new StudentName(name), new AcademicLevel("Sec 2"));
    }
}
