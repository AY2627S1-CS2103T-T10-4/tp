package seedu.address.ui;

import static java.util.Objects.requireNonNull;

import java.util.List;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import seedu.address.model.guardian.Guardian;
import seedu.address.model.student.Student;

/** Shows the selected guardian's contact details and linked students. */
public class GuardianDetailsPanel extends UiPart<Region> {
    private static final String FXML = "GuardianDetailsPanel.fxml";

    @FXML private VBox emptyState;
    @FXML private VBox detailsContent;
    @FXML private Label id;
    @FXML private Label name;
    @FXML private Label phone;
    @FXML private VBox linkedStudents;

    /** Creates an initially unselected guardian profile. */
    public GuardianDetailsPanel() {
        super(FXML);
    }

    /** Replaces the rendered profile and linked-student list. */
    public void setGuardian(Guardian guardian, List<Student> students) {
        requireNonNull(students);
        boolean selected = guardian != null;
        emptyState.setVisible(!selected);
        emptyState.setManaged(!selected);
        detailsContent.setVisible(selected);
        detailsContent.setManaged(selected);
        linkedStudents.getChildren().clear();
        if (!selected) {
            return;
        }
        id.setText(guardian.getId().toString());
        name.setText(guardian.getName().toString());
        phone.setText(guardian.getPhone().toString());
        if (students.isEmpty()) {
            addStudentLabel("No linked students.");
        } else {
            students.forEach(student -> addStudentLabel(student.getId() + " - " + student.getName()));
        }
    }

    private void addStudentLabel(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("detail-value");
        label.setWrapText(true);
        label.setMaxWidth(Double.MAX_VALUE);
        linkedStudents.getChildren().add(label);
    }
}
