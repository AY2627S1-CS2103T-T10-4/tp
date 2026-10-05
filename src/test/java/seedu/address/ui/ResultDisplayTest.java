package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.scene.control.Label;
import javafx.scene.layout.Region;

public class ResultDisplayTest {

    @BeforeAll
    static void startJavaFxToolkit() {
        JavaFxTestUtils.startJavaFxToolkit();
    }

    @Test
    public void setFeedbackToUser_multiLineFeedback_displaysAndGrows() {
        JavaFxTestUtils.runOnFxThread(() -> {
            ResultDisplay resultDisplay = new ResultDisplay();
            Label feedbackLabel = (Label) resultDisplay.getRoot().lookup("#resultDisplay");
            feedbackLabel.resize(500, feedbackLabel.getMinHeight());
            double emptyHeight = resultDisplay.getRoot().getPrefHeight();
            String feedback = "Invalid command format!\nUsage line one\nUsage line two";

            resultDisplay.setFeedbackToUser(feedback);

            assertEquals(feedback, feedbackLabel.getText());
            assertTrue(resultDisplay.getRoot().getPrefHeight() > emptyHeight);
            assertUsesExactPreferredHeight(resultDisplay.getRoot());
        });
    }

    @Test
    public void setFeedbackToUser_narrowWidth_wrapsAndGrows() {
        JavaFxTestUtils.runOnFxThread(() -> {
            ResultDisplay resultDisplay = new ResultDisplay();
            Label feedbackLabel = (Label) resultDisplay.getRoot().lookup("#resultDisplay");
            feedbackLabel.resize(80, feedbackLabel.getMinHeight());
            double emptyHeight = resultDisplay.getRoot().getPrefHeight();

            resultDisplay.setFeedbackToUser("This feedback should wrap over several visual lines.");

            assertTrue(resultDisplay.getRoot().getPrefHeight() > emptyHeight);
        });
    }

    @Test
    public void setFeedbackToUser_nullFeedback_throwsNullPointerException() {
        JavaFxTestUtils.runOnFxThread(() -> {
            ResultDisplay resultDisplay = new ResultDisplay();

            assertThrows(NullPointerException.class, () -> resultDisplay.setFeedbackToUser(null));
        });
    }

    private static void assertUsesExactPreferredHeight(Region region) {
        assertEquals(region.getPrefHeight(), region.getMinHeight());
        assertEquals(region.getPrefHeight(), region.getMaxHeight());
    }
}
