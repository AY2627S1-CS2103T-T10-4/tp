package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.event.ActionEvent;
import javafx.scene.control.TextField;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.parser.exceptions.ParseException;

public class CommandBoxTest {

    @BeforeAll
    static void startJavaFxToolkit() {
        JavaFxTestUtils.startJavaFxToolkit();
    }

    @Test
    public void enterCommand_validCommand_executesAndClearsInput() {
        JavaFxTestUtils.runOnFxThread(() -> {
            AtomicReference<String> executedCommand = new AtomicReference<>();
            CommandBox commandBox = new CommandBox(commandText -> {
                executedCommand.set(commandText);
                return new CommandResult("Done");
            });
            TextField commandTextField = (TextField) commandBox.getRoot().lookup("#commandTextField");

            commandTextField.setText("list");
            submit(commandTextField);

            assertEquals("list", executedCommand.get());
            assertEquals("", commandTextField.getText());
        });
    }

    @Test
    public void enterCommand_emptyCommand_doesNotExecute() {
        JavaFxTestUtils.runOnFxThread(() -> {
            AtomicReference<String> executedCommand = new AtomicReference<>();
            CommandBox commandBox = new CommandBox(commandText -> {
                executedCommand.set(commandText);
                return new CommandResult("Done");
            });
            TextField commandTextField = (TextField) commandBox.getRoot().lookup("#commandTextField");

            submit(commandTextField);

            assertNull(executedCommand.get());
        });
    }

    @Test
    public void enterCommand_invalidCommand_marksInputUntilTextChanges() {
        JavaFxTestUtils.runOnFxThread(() -> {
            CommandBox commandBox = new CommandBox(commandText -> {
                throw new ParseException("Invalid command");
            });
            TextField commandTextField = (TextField) commandBox.getRoot().lookup("#commandTextField");

            commandTextField.setText("invalid");
            submit(commandTextField);
            assertTrue(commandTextField.getStyleClass().contains(CommandBox.ERROR_STYLE_CLASS));

            commandTextField.setText("list");
            assertFalse(commandTextField.getStyleClass().contains(CommandBox.ERROR_STYLE_CLASS));
        });
    }

    private static void submit(TextField commandTextField) {
        commandTextField.getOnAction().handle(new ActionEvent(commandTextField, commandTextField));
    }
}
