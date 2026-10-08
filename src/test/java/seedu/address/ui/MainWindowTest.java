package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BENSON;

import java.nio.file.Path;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import seedu.address.commons.core.GuiSettings;
import seedu.address.logic.Logic;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.Person;
import seedu.address.model.student.AcademicLevel;
import seedu.address.model.student.Student;
import seedu.address.model.student.StudentId;
import seedu.address.model.student.StudentName;

public class MainWindowTest {

    private static final Path DATA_FILE_PATH = Path.of("data", "addressbook.json");

    @BeforeAll
    static void startJavaFxToolkit() {
        JavaFxTestUtils.startJavaFxToolkit();
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
            assertEquals(ALICE, mainWindow.getPersonListPanel().selectedPersonProperty().get());
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

    private static Label getLabel(Stage stage, String selector) {
        return (Label) stage.getScene().lookup(selector);
    }

    private static void submit(TextField commandTextField) {
        commandTextField.getOnAction().handle(new ActionEvent(commandTextField, commandTextField));
    }

    private static class TestLogic implements Logic {
        private final ObservableList<Person> people = FXCollections.observableArrayList(ALICE, BENSON);
        private final ObservableList<Student> students = FXCollections.observableArrayList();
        private GuiSettings guiSettings;
        private String lastCommand;
        private ParseException commandFailure;
        private CommandResult commandResult;

        TestLogic(GuiSettings guiSettings) {
            this.guiSettings = guiSettings;
        }

        @Override
        public CommandResult execute(String commandText) throws CommandException, ParseException {
            lastCommand = commandText;
            if (commandFailure != null) {
                throw commandFailure;
            }
            return commandResult == null ? new CommandResult("Command completed") : commandResult;
        }

        @Override
        public ObservableList<Person> getFilteredPersonList() {
            return people;
        }

        @Override
        public ObservableList<Student> getStudentList() {
            return students;
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
