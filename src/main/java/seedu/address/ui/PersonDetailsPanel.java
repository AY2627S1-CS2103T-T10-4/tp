package seedu.address.ui;

import java.util.Comparator;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import seedu.address.model.person.Person;

/**
 * A read-only panel that displays every contact field for the selected {@link Person}.
 */
public class PersonDetailsPanel extends UiPart<Region> {

    private static final String FXML = "PersonDetailsPanel.fxml";

    @FXML
    private VBox emptyState;

    @FXML
    private VBox detailsContent;

    @FXML
    private Label name;

    @FXML
    private Label phone;

    @FXML
    private Label email;

    @FXML
    private Label address;

    @FXML
    private FlowPane tags;

    /**
     * Creates an empty details panel ready to display a selected person.
     */
    public PersonDetailsPanel() {
        super(FXML);
    }

    /**
     * Displays {@code person}, or the empty state when no person is selected.
     */
    public void setPerson(Person person) {
        boolean hasSelection = person != null;
        setSelectionVisibility(hasSelection);

        if (!hasSelection) {
            return;
        }

        displayPerson(person);
    }

    private void setSelectionVisibility(boolean hasSelection) {
        emptyState.setManaged(!hasSelection);
        emptyState.setVisible(!hasSelection);
        detailsContent.setManaged(hasSelection);
        detailsContent.setVisible(hasSelection);
    }

    private void displayPerson(Person person) {
        name.setText(person.getName().fullName);
        phone.setText(person.getPhone().value);
        email.setText(person.getEmail().value);
        address.setText(person.getAddress().value);
        displayTags(person);
    }

    private void displayTags(Person person) {
        tags.getChildren().clear();
        person.getTags().stream()
                .sorted(Comparator.comparing(tag -> tag.tagName))
                .forEach(tag -> tags.getChildren().add(new Label(tag.tagName)));
    }
}
