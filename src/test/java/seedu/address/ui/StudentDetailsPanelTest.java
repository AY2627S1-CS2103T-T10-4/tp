package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import seedu.address.model.student.AcademicLevel;
import seedu.address.model.student.Student;
import seedu.address.model.student.StudentId;
import seedu.address.model.student.StudentName;

class StudentDetailsPanelTest {
    @BeforeAll
    static void startJavaFxToolkit() {
        JavaFxTestUtils.startJavaFxToolkit();
    }

    @Test
    void setStudent_displaysStudentProfile() {
        JavaFxTestUtils.runOnFxThread(() -> {
            StudentDetailsPanel panel = new StudentDetailsPanel();
            Student student = new Student(new StudentId("S-0090"), new StudentName("Mei Lin"),
                    new AcademicLevel("Sec 2"));
            panel.setStudent(student);
            assertEquals("S-0090", getLabel(panel, "id").getText());
            assertEquals("Mei Lin", getLabel(panel, "name").getText());
            assertEquals("Sec 2", getLabel(panel, "academicLevel").getText());
            assertFalse(getPane(panel, "emptyState").isVisible());
            assertTrue(getPane(panel, "detailsContent").isVisible());
        });
    }

    @Test
    void setStudent_nullShowsEmptyState() {
        JavaFxTestUtils.runOnFxThread(() -> {
            StudentDetailsPanel panel = new StudentDetailsPanel();
            panel.setStudent(new Student(new StudentId("S-0091"), new StudentName("Amy Tan"),
                    new AcademicLevel("IP 1")));
            panel.setStudent(null);
            assertTrue(getPane(panel, "emptyState").isVisible());
            assertTrue(getPane(panel, "emptyState").isManaged());
            assertFalse(getPane(panel, "detailsContent").isVisible());
            assertFalse(getPane(panel, "detailsContent").isManaged());
        });
    }

    private static Label getLabel(StudentDetailsPanel panel, String id) {
        return (Label) panel.getRoot().lookup("#" + id);
    }

    private static VBox getPane(StudentDetailsPanel panel, String id) {
        return (VBox) panel.getRoot().lookup("#" + id);
    }
}
