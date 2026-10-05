package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalPersons.ALICE;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;

public class PersonDetailsPanelTest {

    @BeforeAll
    static void startJavaFxToolkit() {
        JavaFxTestUtils.startJavaFxToolkit();
    }

    @Test
    public void setPerson_personGiven_displaysEveryPersonField() {
        JavaFxTestUtils.runOnFxThread(() -> {
            PersonDetailsPanel panel = new PersonDetailsPanel();
            panel.setPerson(ALICE);

            assertEquals(ALICE.getName().fullName, getLabel(panel, "name").getText());
            assertEquals(ALICE.getPhone().value, getLabel(panel, "phone").getText());
            assertEquals(ALICE.getEmail().value, getLabel(panel, "email").getText());
            assertEquals(ALICE.getAddress().value, getLabel(panel, "address").getText());

            FlowPane tags = (FlowPane) panel.getRoot().lookup("#tags");
            assertEquals(1, tags.getChildren().size());
            assertEquals("friends", ((Label) tags.getChildren().get(0)).getText());
            assertFalse(getPane(panel, "emptyState").isVisible());
            assertTrue(getPane(panel, "detailsContent").isVisible());
        });
    }

    @Test
    public void setPerson_nullGiven_displaysEmptyState() {
        JavaFxTestUtils.runOnFxThread(() -> {
            PersonDetailsPanel panel = new PersonDetailsPanel();
            panel.setPerson(ALICE);
            panel.setPerson(null);

            assertTrue(getPane(panel, "emptyState").isVisible());
            assertTrue(getPane(panel, "emptyState").isManaged());
            assertFalse(getPane(panel, "detailsContent").isVisible());
            assertFalse(getPane(panel, "detailsContent").isManaged());
        });
    }

    private static Label getLabel(PersonDetailsPanel panel, String id) {
        return (Label) panel.getRoot().lookup("#" + id);
    }

    private static VBox getPane(PersonDetailsPanel panel, String id) {
        return (VBox) panel.getRoot().lookup("#" + id);
    }
}
