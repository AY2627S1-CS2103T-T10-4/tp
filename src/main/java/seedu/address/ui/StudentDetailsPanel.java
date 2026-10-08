package seedu.address.ui;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import seedu.address.model.student.Student;

/** Shows the selected student's available profile fields. */
public class StudentDetailsPanel extends UiPart<Region> {
    private static final String FXML = "StudentDetailsPanel.fxml";
    @FXML private VBox emptyState;
    @FXML private VBox detailsContent;
    @FXML private Label id;
    @FXML private Label name;
    @FXML private Label academicLevel;
    public StudentDetailsPanel() {
        super(FXML);
    }

    public void setStudent(Student student) {
        boolean selected = student != null;
        emptyState.setVisible(!selected);
        emptyState.setManaged(!selected);
        detailsContent.setVisible(selected);
        detailsContent.setManaged(selected);
        if (selected) {
            id.setText(student.getId().toString());
            name.setText(student.getName().toString());
            academicLevel.setText(student.getAcademicLevel().toString());
        }
    }
}
