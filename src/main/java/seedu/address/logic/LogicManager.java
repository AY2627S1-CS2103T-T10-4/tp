package seedu.address.logic;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.util.logging.Logger;

import javafx.collections.ObservableList;
import seedu.address.commons.core.GuiSettings;
import seedu.address.commons.core.LogsCenter;
import seedu.address.logic.commands.AddGuardianCommand;
import seedu.address.logic.commands.AddStudentCommand;
import seedu.address.logic.commands.Command;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.DeleteStudentCommand;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.AddressBookParser;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.student.Student;
import seedu.address.storage.Storage;

/** Main logic manager for commands, model access, and persistence. */
public class LogicManager implements Logic {
    public static final String FILE_OPS_ERROR_FORMAT = "Could not save data due to the following error: %s";
    public static final String FILE_OPS_PERMISSION_ERROR_FORMAT =
            "Could not save data to file %s due to insufficient permissions to write to the file or the folder.";

    private final Logger logger = LogsCenter.getLogger(LogicManager.class);
    private final Model model;
    private final Storage storage;
    private final AddressBookParser parser;

    /** Creates the logic manager with its model and storage dependencies. */
    public LogicManager(Model model, Storage storage) {
        this.model = model;
        this.storage = storage;
        parser = new AddressBookParser();
    }

    @Override
    public CommandResult execute(String commandText) throws CommandException, ParseException {
        logger.info("----------------[USER COMMAND][" + commandText + "]");
        Command command = parser.parseCommand(commandText);
        AddressBook beforeCommand = new AddressBook(model.getAddressBook());
        CommandResult result = command.execute(model);
        if (beforeCommand.equals(model.getAddressBook())) {
            return result;
        }
        try {
            storage.saveAddressBook(model.getAddressBook());
        } catch (AccessDeniedException e) {
            rollbackChange(command, beforeCommand);
            throw new CommandException(getSaveError(command, e), e);
        } catch (IOException e) {
            rollbackChange(command, beforeCommand);
            throw new CommandException(getSaveError(command, e), e);
        }
        return result;
    }

    private void rollbackChange(Command command, AddressBook beforeCommand) {
        if (command instanceof AddStudentCommand || command instanceof AddGuardianCommand
                || command instanceof DeleteStudentCommand) {
            model.setAddressBook(beforeCommand);
        }
    }

    private String getSaveError(Command command, IOException cause) {
        if (command instanceof AddStudentCommand) {
            return "Could not save changes. No student was added.";
        }
        if (command instanceof DeleteStudentCommand deleteCommand) {
            return String.format(DeleteStudentCommand.MESSAGE_SAVE_FAILURE, deleteCommand.getStudentId());
        }
        if (command instanceof AddGuardianCommand) {
            return "Could not save changes. No guardian was added.";
        }
        if (cause instanceof AccessDeniedException) {
            return String.format(FILE_OPS_PERMISSION_ERROR_FORMAT, cause.getMessage());
        }
        return String.format(FILE_OPS_ERROR_FORMAT, cause.getMessage());
    }

    @Override
    public ObservableList<Student> getStudentList() {
        return model.getStudentList();
    }

    @Override
    public GuiSettings getGuiSettings() {
        return model.getGuiSettings();
    }

    @Override
    public void setGuiSettings(GuiSettings settings) {
        model.setGuiSettings(settings);
    }
}
