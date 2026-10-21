package seedu.address.logic;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

import javafx.collections.ObservableList;
import seedu.address.commons.core.GuiSettings;
import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.logic.commands.AddGuardianCommand;
import seedu.address.logic.commands.AddStudentCommand;
import seedu.address.logic.commands.AssignGroupCommand;
import seedu.address.logic.commands.Command;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.DeleteStudentCommand;
import seedu.address.logic.commands.EditGuardianCommand;
import seedu.address.logic.commands.EditStudentCommand;
import seedu.address.logic.commands.LinkGuardianCommand;
import seedu.address.logic.commands.UnassignGroupCommand;
import seedu.address.logic.commands.UnlinkGuardianCommand;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.AddressBookParser;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.group.Group;
import seedu.address.model.group.GroupCatalogue;
import seedu.address.model.guardian.Guardian;
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
        if (command.isReadOnly()) {
            return revealRequestedStudent(command.execute(model));
        }
        if (storage.getDataRecoveryReason().isPresent()) {
            throw new CommandException(DataLoadingException.MESSAGE_RECOVERY);
        }
        AddressBook beforeCommand = new AddressBook(model.getAddressBook());
        CommandResult result = command.execute(model);
        if (beforeCommand.equals(model.getAddressBook())) {
            return revealRequestedStudent(result);
        }
        try {
            storage.saveAddressBook(model.getAddressBook());
        } catch (IOException e) {
            model.setAddressBook(beforeCommand);
            throw new CommandException(getSaveError(command, e), e);
        }
        return revealRequestedStudent(result);
    }

    /** Reveals a hidden selection target only after execution and any required save have succeeded. */
    private CommandResult revealRequestedStudent(CommandResult result) {
        String studentId = result.getSelectedStudentId();
        if (studentId != null && model.findStudent(studentId).isPresent()
                && model.getStudentList().stream().noneMatch(student -> student.getId().value.equals(studentId))) {
            model.updateFilteredStudentList(Model.PREDICATE_SHOW_ALL_STUDENTS);
        }
        return result;
    }

    private String getSaveError(Command command, IOException cause) {
        if (command instanceof UnlinkGuardianCommand) {
            return UnlinkGuardianCommand.MESSAGE_SAVE_FAILURE;
        }
        if (command instanceof LinkGuardianCommand) {
            return LinkGuardianCommand.MESSAGE_SAVE_FAILURE;
        }
        if (command instanceof AssignGroupCommand) {
            return AssignGroupCommand.MESSAGE_SAVE_FAILURE;
        }
        if (command instanceof UnassignGroupCommand) {
            return UnassignGroupCommand.MESSAGE_SAVE_FAILURE;
        }
        if (command instanceof AddStudentCommand) {
            return "Could not save changes. No student was added.";
        }
        if (command instanceof DeleteStudentCommand deleteCommand) {
            return String.format(DeleteStudentCommand.MESSAGE_SAVE_FAILURE, deleteCommand.getStudentId());
        }
        if (command instanceof EditStudentCommand edit) {
            return "Could not save changes. Student " + edit.getStudentId() + " was not updated.";
        }
        if (command instanceof EditGuardianCommand edit) {
            return String.format(EditGuardianCommand.MESSAGE_SAVE_FAILURE_FORMAT, edit.getGuardianId());
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
    public ObservableList<Guardian> getGuardianList() {
        return model.getGuardianList();
    }

    @Override
    public GroupCatalogue getGroupCatalogue() {
        return model.getGroupCatalogue();
    }

    @Override
    public Optional<String> getDataRecoveryReason() {
        return storage.getDataRecoveryReason();
    }

    @Override
    public List<Group> getAssignedGroups(String studentId) {
        return model.getAssignedGroups(studentId);
    }

    @Override
    public Optional<Student> findStudent(String studentId) {
        return model.findStudent(studentId);
    }

    @Override
    public Optional<Guardian> getLinkedGuardian(String studentId) {
        return model.getLinkedGuardian(studentId);
    }

    @Override
    public List<Student> getLinkedStudents(String guardianId) {
        return model.getLinkedStudents(guardianId);
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
