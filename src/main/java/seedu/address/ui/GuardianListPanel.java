package seedu.address.ui;

import static java.util.Objects.requireNonNull;

import java.util.function.ToIntFunction;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.Region;
import seedu.address.model.guardian.Guardian;

/** Displays guardians and supports stable-ID selection. */
public class GuardianListPanel extends UiPart<Region> {
    private static final String FXML = "GuardianListPanel.fxml";

    private final ToIntFunction<Guardian> linkedStudentCountProvider;

    @FXML private ListView<Guardian> guardianListView;

    /** Creates the panel backed by the supplied observable guardian list. */
    public GuardianListPanel(ObservableList<Guardian> guardians,
            ToIntFunction<Guardian> linkedStudentCountProvider) {
        super(FXML);
        this.linkedStudentCountProvider = linkedStudentCountProvider;
        guardianListView.setItems(guardians);
        guardianListView.setPlaceholder(new Label("No guardians registered."));
        guardianListView.setCellFactory(unused -> new GuardianListViewCell());
    }

    /** Sets the empty-list feedback for the most recent successful guardian query. */
    public void setEmptyMessage(String message) {
        guardianListView.setPlaceholder(new Label(requireNonNull(message)));
    }

    /** Returns the guardian currently selected in the list. */
    public ReadOnlyObjectProperty<Guardian> selectedGuardianProperty() {
        return guardianListView.getSelectionModel().selectedItemProperty();
    }

    /** Selects the first visible guardian, if one exists. */
    public void selectFirstGuardian() {
        guardianListView.getSelectionModel().selectFirst();
    }

    /** Rebuilds visible cards after relationship-only changes. */
    public void refresh() {
        guardianListView.refresh();
    }

    private class GuardianListViewCell extends ListCell<Guardian> {
        @Override
        protected void updateItem(Guardian guardian, boolean empty) {
            super.updateItem(guardian, empty);
            if (empty || guardian == null) {
                setGraphic(null);
                setText(null);
                return;
            }
            int linkedStudentCount = linkedStudentCountProvider.applyAsInt(guardian);
            setGraphic(new GuardianCard(guardian, linkedStudentCount).getRoot());
            setText(null);
        }
    }
}
