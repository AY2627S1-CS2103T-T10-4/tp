package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BENSON;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.collections.FXCollections;

public class PersonListPanelTest {

    @BeforeAll
    static void startJavaFxToolkit() {
        JavaFxTestUtils.startJavaFxToolkit();
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
