package seedu.address.ui;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import seedu.address.model.guardian.Guardian;

/** A list card that presents a guardian's contact summary and linked-student count. */
public class GuardianCard extends UiPart<Region> {
    private static final String FXML = "GuardianListCard.fxml";

    @FXML private Label id;
    @FXML private Label name;
    @FXML private Label phone;
    @FXML private Label linkedStudentCount;

    /** Creates a card for the supplied guardian and current relationship count. */
    public GuardianCard(Guardian guardian, int studentCount) {
        super(FXML);
        id.setText(guardian.getId().toString());
        name.setText(guardian.getName().toString());
        phone.setText(guardian.getPhone().toString());
        linkedStudentCount.setText(Integer.toString(studentCount));
    }
}
