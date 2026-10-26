package seedu.address.ui;

import java.nio.file.Path;
import java.util.List;
import java.util.logging.Logger;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextInputControl;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import seedu.address.commons.core.GuiSettings;
import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.logic.Logic;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.guardian.Guardian;
import seedu.address.model.student.Student;

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
    private StudentListPanel studentListPanel;
    private StudentDetailsPanel studentDetailsPanel;
    private GuardianListPanel guardianListPanel;
    private GuardianDetailsPanel guardianDetailsPanel;
    private GroupListPanel groupListPanel;
    private ResultDisplay resultDisplay;

    @FXML
    private StackPane commandBoxPlaceholder;

    @FXML
    private MenuItem helpMenuItem;

    @FXML
    private StackPane studentListPanelPlaceholder;

    @FXML
    private StackPane studentDetailsPanelPlaceholder;

    @FXML
    private Label studentCountLabel;

    @FXML
    private Label studentFilterSummaryLabel;

    @FXML
    private StackPane guardianListPanelPlaceholder;

    @FXML
    private StackPane guardianDetailsPanelPlaceholder;

    @FXML
    private Label guardianCountLabel;

    @FXML
    private StackPane resultDisplayPlaceholder;

    @FXML
    private HBox resultSection;

    @FXML
    private ToggleButton studentsTab;

    @FXML
    private ToggleButton guardiansTab;

    @FXML
    private ToggleButton groupsTab;

    @FXML
    private StackPane studentWorkspace;

    @FXML
    private StackPane guardianWorkspace;

    @FXML
    private StackPane groupsWorkspacePlaceholder;

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
        fillStudentPanels();
        fillGuardianPanels();
        fillGroupPanel();
        fillResultDisplay();
        fillStatusBar();
        fillCommandBox();
    }

    private void fillStudentPanels() {
        studentListPanel = new StudentListPanel(logic.getStudentList());
        studentListPanelPlaceholder.getChildren().add(studentListPanel.getRoot());
        studentDetailsPanel = new StudentDetailsPanel();
        studentDetailsPanelPlaceholder.getChildren().add(studentDetailsPanel.getRoot());
        studentListPanel.selectedStudentProperty().addListener((observable, oldStudent, newStudent) ->
                refreshStudentDetails());
        studentCountLabel.textProperty().bind(
                Bindings.size(logic.getStudentList()).asString("%d students"));
        refreshStudentFilterSummary();
        studentListPanel.selectFirstStudent();
    }

    private void refreshStudentFilterSummary() {
        String summary = logic.getStudentFilterSummary();
        boolean hasFilter = !summary.isBlank();
        studentFilterSummaryLabel.setText(summary);
        studentFilterSummaryLabel.setVisible(hasFilter);
        studentFilterSummaryLabel.setManaged(hasFilter);
    }

    private void refreshStudentDetails() {
        Student selected = studentListPanel.selectedStudentProperty().get();
        studentDetailsPanel.setStudent(selected,
                selected == null ? List.of() : logic.getAssignedGroups(selected.getId().value),
                selected == null ? null : logic.getLinkedGuardian(selected.getId().value).orElse(null));
    }

    private void fillGuardianPanels() {
        guardianListPanel = new GuardianListPanel(logic.getGuardianList(), guardian ->
                logic.getLinkedStudents(guardian.getId().toString()).size());
        guardianListPanelPlaceholder.getChildren().add(guardianListPanel.getRoot());
        guardianDetailsPanel = new GuardianDetailsPanel();
        guardianDetailsPanelPlaceholder.getChildren().add(guardianDetailsPanel.getRoot());
        guardianListPanel.selectedGuardianProperty().addListener((observable, oldGuardian, newGuardian) ->
                refreshGuardianWorkspace());
        guardianCountLabel.textProperty().bind(
                Bindings.size(logic.getGuardianList()).asString("%d guardians"));
        guardianListPanel.selectFirstGuardian();
    }

    private void refreshGuardianWorkspace() {
        Guardian selected = guardianListPanel.selectedGuardianProperty().get();
        guardianDetailsPanel.setGuardian(selected, selected == null
                ? List.of() : logic.getLinkedStudents(selected.getId().toString()));
        guardianListPanel.refresh();
    }

    private void restoreStudentSelection(String studentId) {
        if (studentId == null || !studentListPanel.containsStudentId(studentId)) {
            studentListPanel.clearSelection();
        } else {
            studentListPanel.selectStudentId(studentId);
        }
        // Assignment-only changes keep the same Student object, so selection listeners may not fire.
        refreshStudentDetails();
    }

    private void fillGroupPanel() {
        groupListPanel = new GroupListPanel(logic.getGroupCatalogue());
        groupsWorkspacePlaceholder.getChildren().add(groupListPanel.getRoot());
    }

    private void fillResultDisplay() {
        resultDisplay = new ResultDisplay();
        resultDisplayPlaceholder.getChildren().add(resultDisplay.getRoot());
        if (logic.getDataRecoveryReason().isPresent()) {
            resultDisplay.setFeedbackToUser(DataLoadingException.MESSAGE_RECOVERY);
        }
        bindResultSectionHeight();
    }

    private void fillStatusBar() {
        StatusBarFooter statusBarFooter = new StatusBarFooter(dataFilePath, logic.getDataRecoveryReason());
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

    /** Shows the populated student workspace. */
    @FXML
    private void handleStudentsTab() {
        setActiveWorkspace(CommandResult.Workspace.STUDENTS);
    }

    /** Shows the populated guardian workspace. */
    @FXML
    private void handleGuardiansTab() {
        setActiveWorkspace(CommandResult.Workspace.GUARDIANS);
    }

    /** Shows the configured groups workspace. */
    @FXML
    private void handleGroupsTab() {
        setActiveWorkspace(CommandResult.Workspace.GROUPS);
    }

    private void setActiveWorkspace(CommandResult.Workspace workspace) {
        boolean studentsActive = workspace == CommandResult.Workspace.STUDENTS;
        boolean guardiansActive = workspace == CommandResult.Workspace.GUARDIANS;
        boolean groupsActive = workspace == CommandResult.Workspace.GROUPS;
        studentsTab.setSelected(studentsActive);
        guardiansTab.setSelected(guardiansActive);
        groupsTab.setSelected(groupsActive);
        setWorkspaceVisible(studentWorkspace, studentsActive);
        setWorkspaceVisible(guardianWorkspace, guardiansActive);
        setWorkspaceVisible(groupsWorkspacePlaceholder, groupsActive);
    }

    private void setWorkspaceVisible(StackPane workspace, boolean isVisible) {
        workspace.setVisible(isVisible);
        workspace.setManaged(isVisible);
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
        showHelp(null);
    }

    private void showHelp(String command) {
        helpWindow.setCommand(command);
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

    StudentListPanel getStudentListPanel() {
        return studentListPanel;
    }

    /** Returns the label that describes the active student filter. */
    Label getStudentFilterSummaryLabel() {
        return studentFilterSummaryLabel;
    }

    /** Returns the student details panel mounted in this window. */
    StudentDetailsPanel getStudentDetailsPanel() {
        return studentDetailsPanel;
    }

    /** Returns the guardian list panel mounted in this window. */
    GuardianListPanel getGuardianListPanel() {
        return guardianListPanel;
    }

    /** Returns the guardian details panel mounted in this window. */
    GuardianDetailsPanel getGuardianDetailsPanel() {
        return guardianDetailsPanel;
    }

    /** Empty compatibility panel for legacy UI tests; it is never attached to the window. */
    @Deprecated
    public PersonListPanel getPersonListPanel() {
        return new PersonListPanel(logic.getFilteredPersonList());
    }

    /**
     * Executes the command and returns the result.
     *
     * @see seedu.address.logic.Logic#execute(String)
     */
    private CommandResult executeCommand(String commandText) throws CommandException, ParseException {
        String previousStudentId = studentListPanel.getSelectedStudentId();
        Guardian previousGuardian = guardianListPanel.selectedGuardianProperty().get();
        String previousGuardianId = previousGuardian == null ? null : previousGuardian.getId().toString();
        try {
            CommandResult commandResult = logic.execute(commandText);
            logger.info("Result: " + commandResult.getFeedbackToUser());
            resultDisplay.setFeedbackToUser(commandResult.getFeedbackToUser());
            groupListPanel.refresh(logic.getGroupCatalogue());
            groupListPanel.selectGroupCode(commandResult.getSelectedGroupCode());
            refreshStudentFilterSummary();
            if (commandResult.getWorkspace() == CommandResult.Workspace.GROUPS) {
                handleGroupsTab();
            } else if (commandResult.getWorkspace() == CommandResult.Workspace.GUARDIANS) {
                guardianListPanel.setEmptyMessage(logic.getGuardianList().isEmpty()
                        ? commandResult.getFeedbackToUser() : "No guardians to display.");
                handleGuardiansTab();
            } else if (commandResult.getWorkspace() == CommandResult.Workspace.STUDENTS) {
                handleStudentsTab();
            }
            String requestedStudentId = commandResult.getSelectedStudentId();
            String nextStudentId = requestedStudentId == null ? previousStudentId : requestedStudentId;
            String idToClear = commandResult.getStudentIdToClearSelection();
            if (commandResult.shouldClearStudentSelection()
                    && (idToClear == null || idToClear.equals(previousStudentId))) {
                nextStudentId = null;
            }
            restoreStudentSelection(nextStudentId);
            refreshGuardianWorkspace();

            if (commandResult.isShowHelp()) {
                showHelp(commandResult.getHelpCommand());
            }

            if (commandResult.isExit()) {
                handleExit();
            }

            return commandResult;
        } catch (CommandException | ParseException e) {
            logger.fine("Command rejected: " + e.getClass().getSimpleName());
            resultDisplay.setFeedbackToUser(e.getMessage());
            restoreStudentSelection(previousStudentId);
            guardianListPanel.selectGuardianId(previousGuardianId);
            refreshGuardianWorkspace();
            throw e;
        }
    }
}
