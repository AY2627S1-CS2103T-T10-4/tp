package seedu.address.ui;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Region;
import seedu.address.commons.exceptions.DataLoadingException;

/**
 * A UI for the status bar that is displayed at the footer of the application.
 */
public class StatusBarFooter extends UiPart<Region> {

    private static final String FXML = "StatusBarFooter.fxml";

    @FXML
    private Label saveLocationStatus;

    @FXML
    private Label saveStatus;

    /**
     * Creates a {@code StatusBarFooter} with the given {@code Path}.
     */
    public StatusBarFooter(Path saveLocation) {
        this(saveLocation, Optional.empty());
    }

    /**
     * Creates a footer that retains recovery feedback for the lifetime of the storage session.
     *
     * @param saveLocation Data-file path to display.
     * @param recoveryReason First load-failure reason, or empty for a writable session.
     */
    public StatusBarFooter(Path saveLocation, Optional<String> recoveryReason) {
        super(FXML);
        saveLocationStatus.setText(Paths.get(".").resolve(saveLocation).toString());
        recoveryReason.ifPresent(reason -> {
            saveStatus.setText("Saving disabled");
            saveStatus.setTooltip(new Tooltip(DataLoadingException.MESSAGE_RECOVERY + "\nReason: " + reason));
        });
    }

}
