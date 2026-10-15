package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import seedu.address.commons.core.GuiSettings;
import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.logic.Logic;
import seedu.address.logic.LogicManager;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.ModelManager;
import seedu.address.model.group.ConfiguredGroups;
import seedu.address.model.group.Group;
import seedu.address.model.group.GroupCatalogue;
import seedu.address.model.student.AcademicLevel;
import seedu.address.model.student.Student;
import seedu.address.model.student.StudentId;
import seedu.address.model.student.StudentName;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

public class MainWindowTest {

    private static final Path DATA_FILE_PATH = Path.of("data", "addressbook.json");

    @TempDir
    private Path temporaryFolder;

    @BeforeAll
    static void startJavaFxToolkit() {
        JavaFxTestUtils.startJavaFxToolkit();
    }

    @Test
    public void startupRecovery_explainsFailureAndKeepsSavingDisabled() {
        JavaFxTestUtils.runOnFxThread(() -> {
            Path data = temporaryFolder.resolve("recovery.json");
            byte[] original = "{\"students\":[null]}".getBytes(StandardCharsets.UTF_8);
            Files.write(data, original);
            StorageManager storage = new StorageManager(new JsonAddressBookStorage(data),
                    new JsonUserPrefsStorage(temporaryFolder.resolve("prefs.json")));
            assertThrows(DataLoadingException.class, storage::readAddressBook);
            ModelManager model = new ModelManager();
            Stage stage = new Stage();
            MainWindow mainWindow = new MainWindow(stage, new LogicManager(model, storage), data);
            mainWindow.fillInnerParts();
            String recoveryMessage = "Could not load local data. Your original data file was not changed; "
                    + "restore or replace it before saving.";
            Label saveStatus = getLabel(stage, "#saveStatus");

            assertEquals(recoveryMessage, getLabel(stage, "#resultDisplay").getText());
            assertEquals("Saving disabled", saveStatus.getText());
            assertTrue(saveStatus.getTooltip().getText().contains("Students list contains a null record."));
            TextField input = (TextField) stage.getScene().lookup("#commandTextField");
            input.setText("list-groups");
            submit(input);
            assertEquals("Listed 5 configured group(s).", getLabel(stage, "#resultDisplay").getText());
            assertEquals("Saving disabled", saveStatus.getText());
            input.setText("add-student n/Amy Tan al/Sec 3");
            submit(input);
            assertEquals(recoveryMessage, getLabel(stage, "#resultDisplay").getText());
            assertEquals("Saving disabled", saveStatus.getText());
            input.setText("edit-student sid/S-0007 al/Sec 4");
            submit(input);
            assertEquals(recoveryMessage, getLabel(stage, "#resultDisplay").getText());
            assertEquals("Saving disabled", saveStatus.getText());
            input.setText("delete-student sid/S-0007");
            submit(input);
            assertEquals(recoveryMessage, getLabel(stage, "#resultDisplay").getText());
            assertEquals("Saving disabled", saveStatus.getText());
            assertTrue(model.getStudentList().isEmpty());
            assertArrayEquals(original, Files.readAllBytes(data));
            stage.close();
        });
    }

    @Test
    public void submitCommand_listGroups_showsCatalogueAndReturnsToWorkingStudents() {
        JavaFxTestUtils.runOnFxThread(() -> {
            ModelManager model = new ModelManager();
            Student first = new Student(new StudentId("S-0230"), new StudentName("Amy Tan"),
                    new AcademicLevel("Sec 2"));
            model.addStudent(first);
            Path dataPath = temporaryFolder.resolve("students.json");
            Logic logic = new LogicManager(model, new StorageManager(new JsonAddressBookStorage(dataPath),
                    new JsonUserPrefsStorage(temporaryFolder.resolve("prefs.json"))));
            Stage stage = new Stage();
            MainWindow mainWindow = new MainWindow(stage, logic, dataPath);
            mainWindow.fillInnerParts();
            TextField input = (TextField) stage.getScene().lookup("#commandTextField");
            ToggleButton studentsTab = getToggleButton(stage, "#studentsTab");
            ToggleButton groupsTab = getToggleButton(stage, "#groupsTab");
            StackPane studentWorkspace = getStackPane(stage, "#studentWorkspace");
            StackPane groupsWorkspace = getStackPane(stage, "#groupsWorkspacePlaceholder");

            input.setText("LIST-GROUPS");
            submit(input);

            assertGroupsWorkspaceActive(studentsTab, groupsTab, studentWorkspace, groupsWorkspace);
            assertEquals("Listed 5 configured group(s).", getLabel(stage, "#resultDisplay").getText());
            ListView<?> groups = (ListView<?>) stage.getScene().lookup("#groupListView");
            assertEquals(5, groups.getItems().size());
            assertEquals(first, mainWindow.getStudentListPanel().selectedStudentProperty().get());

            studentsTab.fire();
            input.setText("list-groups extra");
            submit(input);
            assertStudentWorkspaceActive(studentsTab, groupsTab, studentWorkspace, groupsWorkspace);
            assertEquals("Invalid command format. Use: list-groups", getLabel(stage, "#resultDisplay").getText());

            input.setText("list-groups");
            submit(input);
            assertEquals(1, groupsWorkspace.getChildren().size());
            assertSame(groups, stage.getScene().lookup("#groupListView"));
            studentsTab.fire();
            input.setText("add-student n/Mei Lin al/Sec 3");
            submit(input);
            assertStudentWorkspaceActive(studentsTab, groupsTab, studentWorkspace, groupsWorkspace);
            assertEquals(2, model.getStudentList().size());
            assertEquals("Mei Lin", mainWindow.getStudentListPanel().selectedStudentProperty().get()
                    .getName().toString());
            stage.close();
        });
    }

    @Test
    public void submitCommand_legacyIndexSelection_doesNotSelectStudent() {
        JavaFxTestUtils.runOnFxThread(() -> {
            TestLogic logic = new TestLogic(new GuiSettings());
            Student first = new Student(new StudentId("S-0201"), new StudentName("Amy Tan"),
                    new AcademicLevel("Sec 2"));
            Student second = new Student(new StudentId("S-0202"), new StudentName("Mei Lin"),
                    new AcademicLevel("Sec 3"));
            logic.students.setAll(first, second);
            Stage stage = new Stage();
            MainWindow mainWindow = new MainWindow(stage, logic, DATA_FILE_PATH);
            mainWindow.fillInnerParts();
            stage.getScene().getRoot().applyCss();
            TextField input = (TextField) stage.getScene().lookup("#commandTextField");
            logic.commandResult = new CommandResult("Opened profile", INDEX_SECOND_PERSON);

            input.setText("profile-selection-test");
            submit(input);

            assertEquals(first, mainWindow.getStudentListPanel().selectedStudentProperty().get());
            assertEquals(first.getName().toString(), getDetailsLabel(stage, "#name").getText());
            assertEquals("Opened profile", getLabel(stage, "#resultDisplay").getText());

            logic.commandResult = new CommandResult("Command completed");
            input.setText("list");
            submit(input);
            assertEquals(first, mainWindow.getStudentListPanel().selectedStudentProperty().get());

            logic.commandFailure = new ParseException("Invalid command format!");
            input.setText("invalid");
            submit(input);
            assertEquals(first, mainWindow.getStudentListPanel().selectedStudentProperty().get());
            assertEquals(first.getName().toString(), getDetailsLabel(stage, "#name").getText());
            stage.close();
        });
    }

    @Test
    public void constructorAndFillInnerParts_validDependencies_buildsLandscapeWindow() {
        JavaFxTestUtils.runOnFxThread(() -> {
            TestLogic logic = new TestLogic(new GuiSettings(500, 400, 20, 30));
            Stage stage = new Stage();
            MainWindow mainWindow = new MainWindow(stage, logic, DATA_FILE_PATH);

            mainWindow.fillInnerParts();

            assertSame(stage, mainWindow.getPrimaryStage());
            assertEquals(900, stage.getWidth());
            assertEquals(600, stage.getHeight());
            assertEquals(20, stage.getX());
            assertEquals(30, stage.getY());
            assertNull(mainWindow.getStudentListPanel().selectedStudentProperty().get());
            assertEquals("Saved locally", getLabel(stage, "#saveStatus").getText());
            stage.close();
        });
    }

    @Test
    public void navigationTabs_switchTabs_updatesVisibleWorkspace() {
        JavaFxTestUtils.runOnFxThread(() -> {
            TestLogic logic = new TestLogic(new GuiSettings());
            Stage stage = new Stage();
            MainWindow mainWindow = new MainWindow(stage, logic, DATA_FILE_PATH);
            mainWindow.fillInnerParts();
            ToggleButton studentsTab = getToggleButton(stage, "#studentsTab");
            ToggleButton groupsTab = getToggleButton(stage, "#groupsTab");
            StackPane studentWorkspace = getStackPane(stage, "#studentWorkspace");
            StackPane groupsWorkspace = getStackPane(stage, "#groupsWorkspacePlaceholder");

            assertStudentWorkspaceActive(studentsTab, groupsTab, studentWorkspace, groupsWorkspace);

            studentsTab.fire();
            assertStudentWorkspaceActive(studentsTab, groupsTab, studentWorkspace, groupsWorkspace);

            groupsTab.fire();
            assertGroupsWorkspaceActive(studentsTab, groupsTab, studentWorkspace, groupsWorkspace);

            groupsTab.fire();
            assertGroupsWorkspaceActive(studentsTab, groupsTab, studentWorkspace, groupsWorkspace);

            studentsTab.fire();
            assertStudentWorkspaceActive(studentsTab, groupsTab, studentWorkspace, groupsWorkspace);
            stage.close();
        });
    }

    @Test
    public void submitCommand_successAndFailure_updatesResultDisplay() {
        JavaFxTestUtils.runOnFxThread(() -> {
            TestLogic logic = new TestLogic(new GuiSettings());
            Stage stage = new Stage();
            MainWindow mainWindow = new MainWindow(stage, logic, DATA_FILE_PATH);
            mainWindow.fillInnerParts();
            TextField commandTextField = (TextField) stage.getScene().lookup("#commandTextField");

            commandTextField.setText("list");
            submit(commandTextField);
            assertEquals("list", logic.lastCommand);
            assertEquals("Command completed", getLabel(stage, "#resultDisplay").getText());

            logic.commandFailure = new ParseException("Invalid command format!");
            commandTextField.setText("invalid");
            submit(commandTextField);
            assertEquals("invalid", logic.lastCommand);
            assertEquals("Invalid command format!", getLabel(stage, "#resultDisplay").getText());
            stage.close();
        });
    }

    @Test
    public void submitCommand_selectedStudent_selectsMatchingStudentInList() {
        JavaFxTestUtils.runOnFxThread(() -> {
            TestLogic logic = new TestLogic(new GuiSettings());
            Student first = new Student(new StudentId("S-0210"), new StudentName("Amy Tan"),
                    new AcademicLevel("Sec 2"));
            Student selected = new Student(new StudentId("S-0211"), new StudentName("Mei Lin"),
                    new AcademicLevel("Sec 3"));
            logic.students.setAll(first, selected);
            logic.commandResult = new CommandResult("Added student", false, false, "S-0211");
            Stage stage = new Stage();
            MainWindow mainWindow = new MainWindow(stage, logic, DATA_FILE_PATH);
            mainWindow.fillInnerParts();

            TextField commandTextField = (TextField) stage.getScene().lookup("#commandTextField");
            commandTextField.setText("add-student");
            submit(commandTextField);

            assertEquals(selected, mainWindow.getStudentListPanel().selectedStudentProperty().get());
            stage.close();
        });
    }

    @Test
    public void submitCommand_removingDifferentStudent_preservesSelection() {
        JavaFxTestUtils.runOnFxThread(() -> {
            TestLogic logic = new TestLogic(new GuiSettings());
            Student amy = new Student(new StudentId("S-0220"), new StudentName("Amy Tan"),
                    new AcademicLevel("Sec 2"));
            Student mei = new Student(new StudentId("S-0221"), new StudentName("Mei Lin"),
                    new AcademicLevel("Sec 3"));
            logic.students.setAll(amy, mei);
            logic.studentIdToRemoveOnExecute = mei.getId().value;
            Stage stage = new Stage();
            MainWindow mainWindow = new MainWindow(stage, logic, DATA_FILE_PATH);
            mainWindow.fillInnerParts();
            mainWindow.getStudentListPanel().selectStudentId(amy.getId().value);

            TextField commandTextField = (TextField) stage.getScene().lookup("#commandTextField");
            commandTextField.setText("delete-student sid/S-0221");
            submit(commandTextField);

            assertEquals(amy, mainWindow.getStudentListPanel().selectedStudentProperty().get());

            logic.studentIdToRemoveOnExecute = amy.getId().value;
            commandTextField.setText("delete-student sid/S-0220");
            submit(commandTextField);
            assertNull(mainWindow.getStudentListPanel().selectedStudentProperty().get());
            stage.close();
        });
    }

    @Test
    public void submitCommand_conditionalClear_usesThePreviousSelection() {
        JavaFxTestUtils.runOnFxThread(() -> {
            TestLogic logic = new TestLogic(new GuiSettings());
            Student amy = new Student(new StudentId("S-0220"), new StudentName("Amy Tan"),
                    new AcademicLevel("Sec 2"));
            Student mei = new Student(new StudentId("S-0221"), new StudentName("Mei Lin"),
                    new AcademicLevel("Sec 3"));
            logic.students.setAll(amy, mei);
            Stage stage = new Stage();
            MainWindow window = new MainWindow(stage, logic, DATA_FILE_PATH);
            window.fillInnerParts();
            TextField input = (TextField) stage.getScene().lookup("#commandTextField");
            input.setText("selection-metadata-test");
            logic.commandResult = new CommandResult("Clear another student", false, false, null, true, "S-0221");

            submit(input);
            assertSame(amy, window.getStudentListPanel().selectedStudentProperty().get());
            logic.commandResult = new CommandResult("Clear Amy", false, false, null, true, "S-0220");
            input.setText("selection-metadata-test");
            submit(input);
            assertNull(window.getStudentListPanel().selectedStudentProperty().get());
            window.getStudentListPanel().selectStudentId("S-0221");
            logic.commandResult = new CommandResult("Clear selection", false, false, null, true);
            input.setText("selection-metadata-test");
            submit(input);
            assertNull(window.getStudentListPanel().selectedStudentProperty().get());
            stage.close();
        });
    }

    private static Label getDetailsLabel(Stage stage, String selector) {
        return (Label) stage.getScene().lookup("#studentDetailsPanelPlaceholder").lookup(selector);
    }

    private static Label getLabel(Stage stage, String selector) {
        return (Label) stage.getScene().lookup(selector);
    }

    private static ToggleButton getToggleButton(Stage stage, String selector) {
        return (ToggleButton) stage.getScene().lookup(selector);
    }

    private static StackPane getStackPane(Stage stage, String selector) {
        return (StackPane) stage.getScene().lookup(selector);
    }

    private static void assertStudentWorkspaceActive(ToggleButton studentsTab, ToggleButton groupsTab,
            StackPane studentWorkspace, StackPane groupsWorkspace) {
        assertTrue(studentsTab.isSelected());
        assertFalse(groupsTab.isSelected());
        assertTrue(studentWorkspace.isVisible());
        assertTrue(studentWorkspace.isManaged());
        assertFalse(groupsWorkspace.isVisible());
        assertFalse(groupsWorkspace.isManaged());
    }

    private static void assertGroupsWorkspaceActive(ToggleButton studentsTab, ToggleButton groupsTab,
            StackPane studentWorkspace, StackPane groupsWorkspace) {
        assertFalse(studentsTab.isSelected());
        assertTrue(groupsTab.isSelected());
        assertFalse(studentWorkspace.isVisible());
        assertFalse(studentWorkspace.isManaged());
        assertTrue(groupsWorkspace.isVisible());
        assertTrue(groupsWorkspace.isManaged());
    }

    private static void submit(TextField commandTextField) {
        commandTextField.getOnAction().handle(new ActionEvent(commandTextField, commandTextField));
    }

    private static class TestLogic implements Logic {
        private final ObservableList<Student> students = FXCollections.observableArrayList();
        private GuiSettings guiSettings;
        private String lastCommand;
        private ParseException commandFailure;
        private String studentIdToRemoveOnExecute;
        private CommandResult commandResult = new CommandResult("Command completed");

        TestLogic(GuiSettings guiSettings) {
            this.guiSettings = guiSettings;
        }

        @Override
        public CommandResult execute(String commandText) throws CommandException, ParseException {
            lastCommand = commandText;
            if (commandFailure != null) {
                throw commandFailure;
            }
            if (studentIdToRemoveOnExecute != null) {
                students.removeIf(student -> student.getId().value.equals(studentIdToRemoveOnExecute));
            }
            return commandResult;
        }

        @Override
        public ObservableList<Student> getStudentList() {
            return students;
        }

        @Override
        public GroupCatalogue getGroupCatalogue() {
            return ConfiguredGroups.getCatalogue();
        }

        @Override
        public Optional<String> getDataRecoveryReason() {
            return Optional.empty();
        }

        @Override
        public List<Group> getAssignedGroups(String studentId) {
            return List.of();
        }

        @Override
        public GuiSettings getGuiSettings() {
            return guiSettings;
        }

        @Override
        public void setGuiSettings(GuiSettings guiSettings) {
            this.guiSettings = guiSettings;
        }
    }
}
