package seedu.address.ui;

import java.nio.file.Path;
import java.util.logging.Logger;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextInputControl;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import seedu.address.commons.core.GuiSettings;
import seedu.address.commons.core.LogsCenter;
import seedu.address.logic.Logic;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * The Main Window. Provides the basic application layout containing
 * a menu bar and space where other JavaFX elements can be placed.
 */
public class MainWindow extends UiPart<Stage> {

    private static final String FXML = "MainWindow.fxml";
    private static final double MINIMUM_WINDOW_HEIGHT = 600;
    private static final double MINIMUM_WINDOW_WIDTH = 900;

    private final Logger logger = LogsCenter.getLogger(getClass());

    private final Stage primaryStage;
    private final Logic logic;
    private final Path dataFilePath;
    private final HelpWindow helpWindow;

    // Independent Ui parts residing in this Ui container
    private PersonListPanel personListPanel;
    private StudentListPanel studentListPanel;
    private ResultDisplay resultDisplay;

    @FXML
    private StackPane commandBoxPlaceholder;

    @FXML
    private MenuItem helpMenuItem;

    @FXML
    private StackPane personListPanelPlaceholder;

    @FXML
    private StackPane personDetailsPanelPlaceholder;

    @FXML
    private Label personCountLabel;

    @FXML
    private StackPane resultDisplayPlaceholder;

    @FXML
    private HBox resultSection;

    @FXML
    private StackPane statusbarPlaceholder;

    /**
     * Creates a {@code MainWindow} with the given {@code Stage}, {@code Logic},
     * and the data file path to show in the status bar.
     */
    public MainWindow(Stage primaryStage, Logic logic, Path dataFilePath) {
        super(FXML, primaryStage);

        // Set dependencies
        this.primaryStage = primaryStage;
        this.logic = logic;
        this.dataFilePath = dataFilePath;

        // Configure the UI
        setWindowDefaultSize(logic.getGuiSettings());

        setAccelerators();

        helpWindow = new HelpWindow();
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    private void setAccelerators() {
        setAccelerator(helpMenuItem, KeyCombination.valueOf("F1"));
    }

    /**
     * Sets the accelerator of a MenuItem.
     * @param keyCombination the KeyCombination value of the accelerator
     */
    private void setAccelerator(MenuItem menuItem, KeyCombination keyCombination) {
        menuItem.setAccelerator(keyCombination);

        /*
         * TODO: the code below can be removed once the bug reported here
         * https://bugs.openjdk.java.net/browse/JDK-8131666
         * is fixed in a later version of the SDK.
         *
         * According to the bug report, TextInputControl will consume function-key
         * events. Because CommandBox contains a TextField, some accelerators (e.g., F1)
         * will not work when the focus is in it because the key event is consumed by
         * the TextInputControl.
         *
         * For now, we add the following event filter to capture such key events and open
         * the help window purposely so as to support accelerators even when focus is
         * in CommandBox.
         */
        getRoot().addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getTarget() instanceof TextInputControl && keyCombination.match(event)) {
                menuItem.getOnAction().handle(new ActionEvent());
                event.consume();
            }
        });
    }

    /**
     * Fills up all the placeholders of this window.
     */
    void fillInnerParts() {
        fillPersonPanels();
        fillResultDisplay();
        fillStatusBar();
        fillCommandBox();
    }

    private void fillPersonPanels() {
        personListPanel = new PersonListPanel(logic.getFilteredPersonList()); // retained for legacy UI callers
        personListPanel.selectFirstPerson();
        studentListPanel = new StudentListPanel(logic.getStudentList());
        personListPanelPlaceholder.getChildren().add(studentListPanel.getRoot());
        StudentDetailsPanel detailsPanel = new StudentDetailsPanel();
        personDetailsPanelPlaceholder.getChildren().add(detailsPanel.getRoot());
        studentListPanel.selectedStudentProperty().addListener((observable, oldStudent, newStudent) ->
                detailsPanel.setStudent(newStudent));
        personCountLabel.textProperty().bind(
                Bindings.size(logic.getStudentList()).asString("%d students"));
        studentListPanel.selectFirstStudent();
    }

    private void fillResultDisplay() {
        resultDisplay = new ResultDisplay();
        resultDisplayPlaceholder.getChildren().add(resultDisplay.getRoot());
        bindResultSectionHeight();
    }

    private void fillStatusBar() {
        StatusBarFooter statusBarFooter = new StatusBarFooter(dataFilePath);
        statusbarPlaceholder.getChildren().add(statusBarFooter.getRoot());
    }

    private void fillCommandBox() {
        CommandBox commandBox = new CommandBox(this::executeCommand);
        commandBoxPlaceholder.getChildren().add(commandBox.getRoot());
    }

    /**
     * Keeps the complete result row in step with wrapped or multi-line feedback.
     */
    private void bindResultSectionHeight() {
        DoubleBinding resultSectionHeight = Bindings.createDoubleBinding(() ->
                resultDisplay.getRoot().getPrefHeight()
                        + resultSection.getInsets().getTop()
                        + resultSection.getInsets().getBottom(),
                resultDisplay.getRoot().prefHeightProperty(), resultSection.insetsProperty());
        resultSection.minHeightProperty().bind(resultSectionHeight);
        resultSection.prefHeightProperty().bind(resultSectionHeight);
        resultSection.maxHeightProperty().bind(resultSectionHeight);
    }

    /**
     * Sets the default size based on {@code guiSettings}.
     */
    private void setWindowDefaultSize(GuiSettings guiSettings) {
        primaryStage.setHeight(Math.max(MINIMUM_WINDOW_HEIGHT, guiSettings.getWindowHeight()));
        primaryStage.setWidth(Math.max(MINIMUM_WINDOW_WIDTH, guiSettings.getWindowWidth()));
        if (guiSettings.getWindowCoordinates() != null) {
            primaryStage.setX(guiSettings.getWindowCoordinates().getX());
            primaryStage.setY(guiSettings.getWindowCoordinates().getY());
        }
    }

    /**
     * Opens the help window or focuses on it if it's already opened.
     */
    @FXML
    public void handleHelp() {
        if (!helpWindow.isShowing()) {
            helpWindow.show();
        } else {
            helpWindow.focus();
        }
    }

    void show() {
        primaryStage.show();
    }

    /**
     * Closes the application.
     */
    @FXML
    private void handleExit() {
        GuiSettings guiSettings = new GuiSettings(primaryStage.getWidth(), primaryStage.getHeight(),
                (int) primaryStage.getX(), (int) primaryStage.getY());
        logic.setGuiSettings(guiSettings);
        helpWindow.hide();
        primaryStage.hide();
    }

    public PersonListPanel getPersonListPanel() {
        return personListPanel;
    }

    StudentListPanel getStudentListPanel() {
        return studentListPanel;
    }

    /**
     * Executes the command and returns the result.
     *
     * @see seedu.address.logic.Logic#execute(String)
     */
    private CommandResult executeCommand(String commandText) throws CommandException, ParseException {
        try {
            CommandResult commandResult = logic.execute(commandText);
            logger.info("Result: " + commandResult.getFeedbackToUser());
            resultDisplay.setFeedbackToUser(commandResult.getFeedbackToUser());
            if (commandResult.getSelectedStudentId() != null) {
                studentListPanel.selectStudentId(commandResult.getSelectedStudentId());
            }

            commandResult.getSelectedIndex().ifPresent(personListPanel::selectPerson);

            if (commandResult.isShowHelp()) {
                handleHelp();
            }

            if (commandResult.isExit()) {
                handleExit();
            }

            return commandResult;
        } catch (CommandException | ParseException e) {
            logger.info("An error occurred while executing command: " + commandText);
            resultDisplay.setFeedbackToUser(e.getMessage());
            throw e;
        }
    }
}
