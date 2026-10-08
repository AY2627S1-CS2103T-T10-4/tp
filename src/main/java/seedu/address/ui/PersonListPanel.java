package seedu.address.ui;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.Region;
import seedu.address.commons.core.index.Index;
import seedu.address.model.person.Person;

/**
 * Panel containing the list of persons.
 */
public class PersonListPanel extends UiPart<Region> {
    private static final String FXML = "PersonListPanel.fxml";

    @FXML
    private ListView<Person> personListView;

    /**
     * Creates a {@code PersonListPanel} with the given {@code ObservableList}.
     */
    public PersonListPanel(ObservableList<Person> personList) {
        super(FXML);
        personListView.setItems(personList);
        personListView.setCellFactory(listView -> new PersonListViewCell());
    }

    /**
     * Returns the person currently selected in the list.
     */
    public ReadOnlyObjectProperty<Person> selectedPersonProperty() {
        return personListView.getSelectionModel().selectedItemProperty();
    }

    /**
     * Selects the first visible person, if one exists.
     */
    public void selectFirstPerson() {
        personListView.getSelectionModel().selectFirst();
    }

    /**
     * Selects and scrolls to the person at {@code index} in the current visible list.
     * Existing selection listeners update the details panel without moving keyboard focus.
     *
     * @throws NullPointerException if {@code index} is null.
     * @throws IllegalArgumentException if {@code index} is outside the visible list.
     */
    public void selectPerson(Index index) {
        requireNonNull(index);
        int row = index.getZeroBased();
        checkArgument(row < personListView.getItems().size(), "Selection index must refer to a visible person");
        personListView.getSelectionModel().clearAndSelect(row);
        personListView.scrollTo(row);
    }

    /**
     * Custom {@code ListCell} that displays the graphics of a {@code Person} using a {@code PersonCard}.
     */
    class PersonListViewCell extends ListCell<Person> {
        @Override
        protected void updateItem(Person person, boolean empty) {
            super.updateItem(person, empty);

            if (empty || person == null) {
                setGraphic(null);
                setText(null);
            } else {
                setGraphic(new PersonCard(person, getIndex() + 1).getRoot());
            }
        }
    }

}
