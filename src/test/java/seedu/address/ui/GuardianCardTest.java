package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.scene.control.Label;
import seedu.address.model.guardian.Guardian;
import seedu.address.model.guardian.GuardianId;
import seedu.address.model.guardian.GuardianName;
import seedu.address.model.guardian.GuardianPhone;

class GuardianCardTest {
    @BeforeAll
    static void startJavaFxToolkit() {
        JavaFxTestUtils.startJavaFxToolkit();
    }

    @Test
    void constructor_guardianAndCountGiven_displaysSummaryFields() {
        JavaFxTestUtils.runOnFxThread(() -> {
            Guardian guardian = new Guardian(new GuardianId("G-0301"), new GuardianName("Anita Lim"),
                    new GuardianPhone("91234567"));
            GuardianCard card = new GuardianCard(guardian, 2);

            assertEquals("G-0301", getLabel(card, "id").getText());
            assertEquals("Anita Lim", getLabel(card, "name").getText());
            assertEquals("+65 91234567", getLabel(card, "phone").getText());
            assertEquals("2", getLabel(card, "linkedStudentCount").getText());
        });
    }

    private static Label getLabel(GuardianCard card, String id) {
        return (Label) card.getRoot().lookup("#" + id);
    }
}
