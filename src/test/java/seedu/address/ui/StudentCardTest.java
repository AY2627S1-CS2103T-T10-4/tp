package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.scene.control.Label;
import seedu.address.model.student.AcademicLevel;
import seedu.address.model.student.Student;
import seedu.address.model.student.StudentId;
import seedu.address.model.student.StudentName;

class StudentCardTest {
    @BeforeAll
    static void startJavaFxToolkit() {
        JavaFxTestUtils.startJavaFxToolkit();
    }

    @Test
    void constructor_studentGiven_displaysExistingSummaryFields() {
        JavaFxTestUtils.runOnFxThread(() -> {
            Student student = new Student(new StudentId("S-0090"), new StudentName("Mei Lin"),
                    new AcademicLevel("Sec 2"));
            StudentCard card = new StudentCard(student);

            assertEquals("S-0090", getLabel(card, "id").getText());
            assertEquals("Mei Lin", getLabel(card, "name").getText());
            assertEquals("Sec 2", getLabel(card, "academicLevel").getText());
        });
    }

    private static Label getLabel(StudentCard card, String id) {
        return (Label) card.getRoot().lookup("#" + id);
    }
}
