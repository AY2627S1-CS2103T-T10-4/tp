package seedu.address.ui;

import static java.util.Objects.requireNonNull;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;

/**
 * A UI component that displays the result of a command execution.
 */
public class ResultDisplay extends UiPart<Region> {

    private static final String FXML = "ResultDisplay.fxml";
    private static final double MINIMUM_LINE_HEIGHT = 16;
    private static final double TEXT_HEIGHT_ALLOWANCE = 2;

    @FXML
    private StackPane resultDisplayContainer;

    @FXML
    private Label resultDisplay;

    private final Text textMeasurer = new Text();

    /**
     * Creates a result display that grows with its wrapped feedback text.
     */
    public ResultDisplay() {
        super(FXML);
        resultDisplay.widthProperty().addListener((observable, oldWidth, newWidth) -> updatePreferredHeight());
        resultDisplay.fontProperty().addListener((observable, oldFont, newFont) -> updatePreferredHeight());
        updatePreferredHeight();
    }

    /**
     * Displays the specified command feedback and resizes the result area to fit it.
     */
    public void setFeedbackToUser(String feedbackToUser) {
        requireNonNull(feedbackToUser);
        resultDisplay.setText(feedbackToUser);
        updatePreferredHeight();
    }

    /**
     * Sizes the result label to one line when empty and to its wrapped content otherwise.
     */
    private void updatePreferredHeight() {
        double contentHeight = calculateContentHeight();
        resizeResultDisplay(contentHeight);
    }

    private double calculateContentHeight() {
        textMeasurer.setFont(resultDisplay.getFont());
        textMeasurer.setText(resultDisplay.getText().isEmpty() ? " " : resultDisplay.getText());
        textMeasurer.setWrappingWidth(Math.max(1, resultDisplay.getWidth()));
        double measuredHeight = Math.ceil(textMeasurer.getLayoutBounds().getHeight()) + TEXT_HEIGHT_ALLOWANCE;
        return Math.max(MINIMUM_LINE_HEIGHT, measuredHeight);
    }

    private void resizeResultDisplay(double height) {
        setExactHeight(resultDisplay, height);
        setExactHeight(resultDisplayContainer, height);
        resultDisplayContainer.requestLayout();
    }

    private static void setExactHeight(Region region, double height) {
        region.setMinHeight(height);
        region.setPrefHeight(height);
        region.setMaxHeight(height);
    }

}
