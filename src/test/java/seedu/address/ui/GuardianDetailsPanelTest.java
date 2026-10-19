package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import seedu.address.model.guardian.Guardian;
import seedu.address.model.guardian.GuardianId;
import seedu.address.model.guardian.GuardianName;
import seedu.address.model.guardian.GuardianPhone;
import seedu.address.model.student.AcademicLevel;
import seedu.address.model.student.Student;
import seedu.address.model.student.StudentId;
import seedu.address.model.student.StudentName;

class GuardianDetailsPanelTest {
    @BeforeAll
    static void startJavaFxToolkit() {
        JavaFxTestUtils.startJavaFxToolkit();
    }

    @Test
    void setGuardian_displaysContactAndLinkedStudents() {
        JavaFxTestUtils.runOnFxThread(() -> {
            GuardianDetailsPanel panel = new GuardianDetailsPanel();
            new Scene(panel.getRoot());
            panel.getRoot().applyCss();
            Guardian guardian = guardian();
            Student student = new Student(new StudentId("S-0301"), new StudentName("Mei Lin"),
                    new AcademicLevel("Sec 2"));

            panel.setGuardian(guardian, List.of(student));

            assertEquals("G-0301", getLabel(panel, "id").getText());
            assertEquals("Anita Lim", getLabel(panel, "name").getText());
            assertEquals("+65 91234567", getLabel(panel, "phone").getText());
            VBox students = getPane(panel, "linkedStudents");
            assertEquals("S-0301 - Mei Lin", ((Label) students.getChildren().getFirst()).getText());
            assertFalse(getPane(panel, "emptyState").isVisible());
            assertTrue(getPane(panel, "detailsContent").isVisible());
        });
    }

    @Test
    void setGuardian_nullShowsEmptyState() {
        JavaFxTestUtils.runOnFxThread(() -> {
            GuardianDetailsPanel panel = new GuardianDetailsPanel();
            panel.setGuardian(guardian(), List.of());
            panel.setGuardian(null, List.of());

            assertTrue(getPane(panel, "emptyState").isVisible());
            assertFalse(getPane(panel, "detailsContent").isVisible());
        });
    }

    private static Guardian guardian() {
        return new Guardian(new GuardianId("G-0301"), new GuardianName("Anita Lim"),
                new GuardianPhone("91234567"));
    }

    private static Label getLabel(GuardianDetailsPanel panel, String id) {
        return (Label) panel.getRoot().lookup("#" + id);
    }

    private static VBox getPane(GuardianDetailsPanel panel, String id) {
        return (VBox) panel.getRoot().lookup("#" + id);
    }
}
