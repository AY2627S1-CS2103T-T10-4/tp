package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;
import static seedu.address.model.AddressBook.MESSAGE_UNKNOWN_GROUP;
import static seedu.address.model.AddressBook.MESSAGE_UNKNOWN_STUDENT;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.group.Group;
import seedu.address.model.group.GroupAssignment;
import seedu.address.model.group.GroupCode;
import seedu.address.model.student.Student;

/**
 * Assigns one configured group to an existing student, using its stable ID.
 */
public class AssignGroupCommand extends Command {
    public static final String COMMAND_WORD = "assign-group";
    public static final String MESSAGE_USAGE = "Invalid command format. Use: assign-group sid/STUDENT_ID g/GROUP_CODE";
    public static final String MESSAGE_SUCCESS = "Assigned group %s (%s) to student %s.";
    public static final String MESSAGE_SAVE_FAILURE = "Could not save changes. The group was not assigned.";

    private final String studentId;
    private final GroupCode groupCode;

    /**
     * Retains the parsed ID text without constructing or reserving a StudentId.
     * Reference existence and duplicate assignment are checked during execution.
     *
     * @param studentId The student ID text validated by the parser.
     * @param groupCode The canonical code of the requested group.
     * @throws NullPointerException If either argument is null.
     */
    public AssignGroupCommand(String studentId, GroupCode groupCode) {
        requireAllNonNull(studentId, groupCode);
        this.studentId = studentId;
        this.groupCode = groupCode;
    }

    /**
     * Validates the student, group and pair uniqueness before changing the model.
     *
     * @param model The authoritative student register and catalogue.
     * @return Success feedback requesting the Students workspace and the assigned student's profile.
     * @throws CommandException If the student or group is unknown, or the pair is already assigned.
     * @throws NullPointerException If {@code model} is null.
     */
    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        Student student = model.findStudent(studentId).orElseThrow(() ->
                new CommandException(String.format(MESSAGE_UNKNOWN_STUDENT, studentId)));
        Group group = model.getGroupCatalogue().findByCode(groupCode.getValue()).orElseThrow(() ->
                new CommandException(String.format(MESSAGE_UNKNOWN_GROUP, groupCode)));
        try {
            model.addGroupAssignment(new GroupAssignment(student.getId(), group.getCode()));
        } catch (IllegalArgumentException e) {
            throw new CommandException(e.getMessage(), e);
        }
        return new CommandResult(String.format(MESSAGE_SUCCESS, group.getCode(), group.getLabel(), studentId),
                studentId, CommandResult.Workspace.STUDENTS);
    }
}
