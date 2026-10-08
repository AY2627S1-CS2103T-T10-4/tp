package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BENSON;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.scene.Scene;
import javafx.scene.control.ScrollToEvent;
import seedu.address.model.person.Person;

public class PersonListPanelTest {

    @BeforeAll
    static void startJavaFxToolkit() {
        JavaFxTestUtils.startJavaFxToolkit();
    }

    @Test
    public void selectPerson_validIndex_selectsPersonAndRequestsScroll() {
        JavaFxTestUtils.runOnFxThread(() -> {
            PersonListPanel panel = new PersonListPanel(FXCollections.observableArrayList(ALICE, BENSON));
            new Scene(panel.getRoot());
            panel.getRoot().applyCss();
            panel.selectFirstPerson();
            int[] scrollTargets = {-1};
            panel.getRoot().addEventFilter(ScrollToEvent.scrollToTopIndex(), event ->
                    scrollTargets[0] = event.getScrollTarget());

            panel.selectPerson(INDEX_SECOND_PERSON);

            assertEquals(BENSON, panel.selectedPersonProperty().get());
            assertEquals(1, scrollTargets[0]);
            panel.selectPerson(INDEX_SECOND_PERSON);
            assertEquals(BENSON, panel.selectedPersonProperty().get());
        });
    }

    @Test
    public void selectPerson_filteredList_usesVisibleIndex() {
        JavaFxTestUtils.runOnFxThread(() -> {
            ObservableList<Person> people = FXCollections.observableArrayList(ALICE, BENSON);
            FilteredList<Person> filteredPeople = new FilteredList<>(people, BENSON::equals);
            PersonListPanel panel = new PersonListPanel(filteredPeople);

            panel.selectPerson(INDEX_FIRST_PERSON);

            assertEquals(BENSON, panel.selectedPersonProperty().get());
            assertEquals(2, people.size());
        });
    }

    @Test
    public void selectPerson_invalidIndex_preservesSelection() {
        JavaFxTestUtils.runOnFxThread(() -> {
            PersonListPanel panel = new PersonListPanel(FXCollections.observableArrayList(ALICE));
            panel.selectFirstPerson();

            assertThrows(IllegalArgumentException.class, () -> panel.selectPerson(INDEX_SECOND_PERSON));
            assertThrows(NullPointerException.class, () -> panel.selectPerson(null));
            assertEquals(ALICE, panel.selectedPersonProperty().get());

            PersonListPanel emptyPanel = new PersonListPanel(FXCollections.observableArrayList());
            assertThrows(IllegalArgumentException.class, () -> emptyPanel.selectPerson(INDEX_FIRST_PERSON));
            assertNull(emptyPanel.selectedPersonProperty().get());
        });
    }

    @Test
    public void selectFirstPerson_peoplePresent_selectsFirstPerson() {
        JavaFxTestUtils.runOnFxThread(() -> {
            PersonListPanel panel = new PersonListPanel(FXCollections.observableArrayList(ALICE, BENSON));

            assertNull(panel.selectedPersonProperty().get());
            panel.selectFirstPerson();

            assertEquals(ALICE, panel.selectedPersonProperty().get());
        });
    }

    @Test
    public void selectFirstPerson_emptyList_selectionRemainsEmpty() {
        JavaFxTestUtils.runOnFxThread(() -> {
            PersonListPanel panel = new PersonListPanel(FXCollections.observableArrayList());

            panel.selectFirstPerson();

            assertNull(panel.selectedPersonProperty().get());
        });
    }
}
