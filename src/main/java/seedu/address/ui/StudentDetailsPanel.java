package seedu.address.ui;

import static java.util.Objects.requireNonNull;

import java.util.List;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import seedu.address.model.group.Group;
import seedu.address.model.student.Student;

/**
 * Shows the selected student's fields and assigned groups supplied by the model.
 */
public class StudentDetailsPanel extends UiPart<Region> {
    private static final String FXML = "StudentDetailsPanel.fxml";
    @FXML private VBox emptyState;
    @FXML private VBox detailsContent;
    @FXML private Label id;
    @FXML private Label name;
    @FXML private Label academicLevel;
    @FXML private VBox assignments;

    /**
     * Creates an initially unselected profile.
     */
    public StudentDetailsPanel() {
        super(FXML);
    }

    /**
     * Renders a student with no assignments, or the empty selection state.
     * Call the two-argument overload when displaying persisted group relationships.
     *
     * @param student Student to display, or null to clear the profile.
     */
    public void setStudent(Student student) {
        setStudent(student, List.of());
    }

    /**
     * Replaces the rendered profile, including when the same Student object is supplied again.
     * The supplied collection is consumed immediately and is not retained.
     *
     * @param student Student to display, or null to show the empty selection state.
     * @param assignedGroups Non-null model projection in display order, containing valid groups.
     * @throws NullPointerException If assignedGroups is null.
     */
    public void setStudent(Student student, List<Group> assignedGroups) {
        requireNonNull(assignedGroups);
        boolean selected = student != null;
        emptyState.setVisible(!selected);
        emptyState.setManaged(!selected);
        detailsContent.setVisible(selected);
        detailsContent.setManaged(selected);
        assignments.getChildren().clear();
        if (selected) {
            id.setText(student.getId().toString());
            name.setText(student.getName().toString());
            academicLevel.setText(student.getAcademicLevel().toString());
            if (assignedGroups.isEmpty()) {
                addAssignmentLabel("No groups assigned.");
            } else {
                assignedGroups.forEach(group -> addAssignmentLabel(group.getCode() + " — " + group.getLabel()));
            }
        }
    }

    private void addAssignmentLabel(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("detail-value");
        label.setWrapText(true);
        label.setMaxWidth(Double.MAX_VALUE);
        assignments.getChildren().add(label);
    }
}
