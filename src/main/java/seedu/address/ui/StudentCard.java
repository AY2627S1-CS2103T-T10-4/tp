package seedu.address.ui;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import seedu.address.model.student.Student;

/** A list card that presents a student's summary fields in aligned columns. */
public class StudentCard extends UiPart<Region> {
    private static final String FXML = "StudentListCard.fxml";

    @FXML
    private Label id;
    @FXML
    private Label name;
    @FXML
    private Label academicLevel;

    /** Creates a card for the supplied student. */
    public StudentCard(Student student) {
        super(FXML);
        id.setText(student.getId().toString());
        name.setText(student.getName().toString());
        academicLevel.setText(student.getAcademicLevel().toString());
    }
}
