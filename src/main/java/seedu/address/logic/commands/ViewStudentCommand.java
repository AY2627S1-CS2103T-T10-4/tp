package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;

/** Opens an existing student's read-only profile using its stable ID. */
public class ViewStudentCommand extends Command {

    public static final String COMMAND_WORD = "view-student";
    public static final String MESSAGE_USAGE = "Invalid command format. Use: view-student sid/STUDENT_ID";
    public static final String MESSAGE_SUCCESS = "Opened profile for %s.";
    public static final String MESSAGE_STUDENT_NOT_FOUND = "Student %s does not exist.";

    private final String studentId;

    /**
     * Creates a command for a syntactically valid student ID.
     * Keeps the lookup value as text because constructing a StudentId advances the ID generator.
     */
    public ViewStudentCommand(String studentId) {
        this.studentId = requireNonNull(studentId);
    }

    @Override
    public boolean isReadOnly() {
        return true;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        if (model.findStudent(studentId).isEmpty()) {
            throw new CommandException(String.format(MESSAGE_STUDENT_NOT_FOUND, studentId));
        }
        return new CommandResult(String.format(MESSAGE_SUCCESS, studentId), false, false, studentId);
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof ViewStudentCommand otherCommand
                && studentId.equals(otherCommand.studentId);
    }

    @Override
    public int hashCode() {
        return studentId.hashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("studentId", studentId).toString();
    }
}
