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
 * Removes one configured group assignment from an existing student using its stable ID.
 * This mutating command delegates validation and removal to the model; persistence belongs to the logic component.
 */
public class UnassignGroupCommand extends Command {
    /** Command word used for group unassignment. */
    public static final String COMMAND_WORD = "unassign-group";
    /** Feedback for malformed unassignment arguments. */
    public static final String MESSAGE_USAGE =
            "Invalid command format. Use: unassign-group sid/STUDENT_ID g/GROUP_CODE";
    /** Success feedback containing the canonical group code and student ID. */
    public static final String MESSAGE_SUCCESS = "Removed group %s from student %s.";
    /** Feedback for the logic component when saving the removal fails. */
    public static final String MESSAGE_SAVE_FAILURE = "Could not save changes. The group assignment was not removed.";

    private final String studentId;
    private final GroupCode groupCode;

    /**
     * Retains parsed ID text without constructing a StudentId, parsing its numeric suffix or reserving an ID.
     * Reference existence and assignment membership are checked during execution.
     *
     * @param studentId The student ID text whose syntax has been validated by the parser.
     * @param groupCode The canonical code of the requested group.
     * @throws NullPointerException If either argument is null.
     */
    public UnassignGroupCommand(String studentId, GroupCode groupCode) {
        requireAllNonNull(studentId, groupCode);
        this.studentId = studentId;
        this.groupCode = groupCode;
    }

    /**
     * Removes the requested assignment after resolving the student in the full register and the group in the catalogue.
     * Uses the resolved student's existing ID and allows removal of the student's final assignment.
     * This method changes only the in-memory model; it does not change the student filter or save data to disk.
     *
     * @param model The authoritative student register and configured catalogue.
     * @return Success feedback requesting the target student's profile in the Students workspace.
     * @throws CommandException If the student or group is unknown, or the pair is not assigned.
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
            model.removeGroupAssignment(new GroupAssignment(student.getId(), group.getCode()));
        } catch (IllegalArgumentException e) {
            throw new CommandException(e.getMessage(), e);
        }
        return new CommandResult(String.format(MESSAGE_SUCCESS, group.getCode(), studentId),
                studentId, CommandResult.Workspace.STUDENTS);
    }
}
